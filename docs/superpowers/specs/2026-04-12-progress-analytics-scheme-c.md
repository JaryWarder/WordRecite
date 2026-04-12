# WordRecite 进度分析（方案 C：词粒度画像 + SRS）设计文档

**目标**：在不牺牲向后兼容的前提下，为每个用户在每本词书下的每个单词建立“词粒度学习画像”，支持：
- 进度看板（完成度、掌握度分布、趋势）
- 待复习列表（按 next_review_at 生成）
- 后续智能复习/错题强化（可迭代到更复杂模型）

**范围**：仅新增数据表 + 后端写入/查询接口 + 前端图表数据契约。暂不强制替换现有 daily 复习链路，可并行跑通后再迁移。

---

## 1. 现状与约束

### 1.1 现状（基于当前代码库）
- `user.studied` 代表“当前词书学习进度范围”（通常可理解为 `id <= studied`）
- `daily` 用于复习/打卡记录（当前库结构含 `review_date`、`origin`、`status`、`id` 等字段）
- 后端暂未发现已有的 stage/间隔/SRS 实现（已在代码中检索 stage/interval/next_review 等关键字未命中）
- 旧版前端存在 `Progress.vue`（front 目录）走的是历史接口 `Hello/get_progress`，与当前 Spring Boot v3（Demo + front-v3）不一致，仅作参考，不作为方案 C 的实现入口

### 1.2 约束
- **stage 最大值 = 5**
- **答错直接归零（stage = 0）**
- 用户身份以 JWT Token 为准（后端从拦截器注入的 request attribute username 获取），禁止越权写入/查询

---

## 2. 数据模型设计

### 2.1 核心表：user_word_progress

**设计意图**：一行代表 “username + origin(词书) + word_id(词条)” 的长期画像。

```sql
CREATE TABLE IF NOT EXISTS user_word_progress (
  progress_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(20) NOT NULL,
  origin VARCHAR(50) NOT NULL,
  word_id INT NOT NULL,
  word VARCHAR(100) NOT NULL,
  stage TINYINT NOT NULL DEFAULT 0,
  correct_cnt INT NOT NULL DEFAULT 0,
  wrong_cnt INT NOT NULL DEFAULT 0,
  streak INT NOT NULL DEFAULT 0,
  last_result TINYINT NULL,
  first_seen_at DATETIME NOT NULL,
  last_seen_at DATETIME NOT NULL,
  next_review_at DATETIME NULL,
  UNIQUE KEY uk_user_origin_word (username, origin, word_id),
  INDEX idx_user_origin_next (username, origin, next_review_at),
  INDEX idx_user_origin_last (username, origin, last_seen_at),
  INDEX idx_user_origin_stage (username, origin, stage)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**字段说明**
- `origin`：词书表名（`TOEFLWordBook/CET6WordBook/GREWordBook`），与现有链路一致
- `word_id`：词书表自增 id
- `word`：冗余英文单词，避免频繁 join 动态表名
- `stage`：0~5，掌握程度
- `streak`：连对次数（答错清零）
- `next_review_at`：下一次建议复习时间（用于 due 列表）

### 2.2 可选表：user_word_event（推荐）

**设计意图**：记录事件流用于趋势/回放/重算。若不建此表，也能做基础进度，但趋势会更依赖 progress 的 last_seen_at/计数差分。

```sql
CREATE TABLE IF NOT EXISTS user_word_event (
  event_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(20) NOT NULL,
  origin VARCHAR(50) NOT NULL,
  word_id INT NOT NULL,
  result TINYINT NOT NULL,
  event_type VARCHAR(10) NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_user_origin_time (username, origin, created_at),
  INDEX idx_user_origin_word_time (username, origin, word_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

`event_type` 建议枚举值：`learn` / `review` / `test`

---

## 3. SRS 规则（stage=5，答错归零）

### 3.1 stage 更新规则
- 答对：`stage = min(stage + 1, 5)`，`streak += 1`，`correct_cnt += 1`，`last_result = 1`
- 答错：`stage = 0`，`streak = 0`，`wrong_cnt += 1`，`last_result = 0`

### 3.2 next_review_at 间隔表

| stage | 间隔 |
|------:|:-----|
| 0 | 10 分钟 |
| 1 | 1 天 |
| 2 | 2 天 |
| 3 | 4 天 |
| 4 | 7 天 |
| 5 | 15 天（或 30 天，后续可配置） |

计算逻辑：
- 答对：`next_review_at = now + interval(stage)`
- 答错：`next_review_at = now + 10分钟`

---

## 4. 数据写入点（后端改造建议）

### 4.1 学习提交（learn）
入口：`/api/memorize/submit_list`
- yes/no 列表里包含 `id`（词条 id）与 `word`（英文词）
- 对每条记录执行 upsert：
  - 若不存在：插入初始行（first_seen_at/last_seen_at=now，stage=0）后再应用本次对错更新
  - 若存在：更新统计字段与 next_review_at
- 同时（若启用 event 表）：插入 `user_word_event`（event_type=learn）

### 4.2 测试提交（test）
当前测试链路为前端本地判分。方案 C 建议新增：
- `POST /api/test/submit`：上传 history（origin、[{wordId, result}]）
- 后端按 test 事件更新 `user_word_progress`（可与 learn/review 用同一方法）
- 若启用 event：写入 event_type=test

### 4.3 复习提交（review）
如你后续将复习也落到 progress：
- 当用户在复习列表里点击“认识/不认识”时，后端同样 upsert，并写 event_type=review

---

## 5. 查询接口（供进度分析页/个人中心/复习列表）

### 5.1 进度看板
`GET /api/progress/dashboard?origin=TOEFLWordBook&days=30`

返回结构建议：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "origin": "TOEFLWordBook",
    "bookSize": 1300,
    "learnedUnique": 215,
    "masteredCount": 28,
    "dueCount": 14,
    "stageDist": [
      {"stage":0,"count":120},
      {"stage":1,"count":40},
      {"stage":2,"count":25},
      {"stage":3,"count":12},
      {"stage":4,"count":10},
      {"stage":5,"count":8}
    ],
    "series": {
      "dailyLearned": [{"date":"2026-04-06","count":20}],
      "dailyReviewed": [{"date":"2026-04-06","count":18}],
      "dailyAccuracy": [{"date":"2026-04-06","accuracy":0.72}]
    }
  }
}
```

**口径建议**
- `learnedUnique`：progress 表去重 count（username+origin）
- `masteredCount`：stage >= 4
- `dueCount`：next_review_at <= now
- 趋势曲线：
  - 若启用 event 表：按 created_at group by date 聚合最准确
  - 未启用 event 表：只能用 progress.last_seen_at 做近似（不推荐）

### 5.2 待复习列表（核心业务增量）
`GET /api/progress/due?origin=TOEFLWordBook&limit=50`
- 查询：`next_review_at <= now order by next_review_at asc`
- 返回最小字段：`word_id, word, stage, next_review_at`

### 5.3 词条画像（用于错题本/复盘）
`GET /api/progress/word?origin=TOEFLWordBook&wordId=123`
- 返回：stage、错对次数、最近记录、下次复习时间

---

## 6. 迁移与回填（从现有数据生成初始画像）

### 6.1 快速回填（基于 user.studied）
对每个用户：
- origin = user.studying
- word_id ∈ [1..user.studied]
- 插入 progress（stage=0，first_seen_at/last_seen_at=now，next_review_at=now）

优点：最快让系统“有数据可用”。缺点：无法体现对错/掌握度。

### 6.2 进阶回填（基于 daily 回放）
如果 daily 完整字段可用（origin、id、status、review_date）：
- 将 daily 的 status 映射到 result（yes=1/no=0）
- 按日期回放更新 progress（last_seen_at 取当日，next_review_at 依规则计算）

---

## 7. 安全与一致性
- 所有写/读接口必须用 JWT username（request attribute），不接受前端随意传 username
- origin/tableName 必须继续做白名单/正则校验（仅字母数字下划线），防止 SQL 注入（动态表名）

---

## 8. 实施顺序（推荐）
1. 建表：`user_word_progress`（可选 `user_word_event`）
2. 在学习提交入口落库（learn → progress/event）
3. 增加 dashboard 接口（供前端进度分析页）
4. 增加 due 接口（供复习列表迁移）
5. 增加 test submit 接口（让测试也纳入画像）

---

## 9. 备注：与现有逻辑的关系
- 本方案不要求立即替换 `daily`，可以并行写入 progress/event，等数据稳定后再切换“复习列表生成”到 due 接口
- 若你后续发现“已有类似逻辑”，优先以现有逻辑为准，调整本方案中的 stage/间隔映射以保持一致性（当前代码库未检索到 stage/interval 相关实现）

