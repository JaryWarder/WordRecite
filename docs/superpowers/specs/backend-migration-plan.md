# Backend Migration Functional Steps (Spring Boot + MyBatis-Plus)

> **目标**: 将基于 Spring MVC (4.2.x) 和 JDBCTemplate 的老旧后端代码，按照功能模块精心更替为基于 Spring Boot 3.x、MyBatis-Plus 的纯 MySQL 现代化架构。

---

## 阶段一：基础架构搭建与安全认证层 (Foundation & Auth)

**涉及模块**：工程初始化、数据库连接、用户实体、注册与登录逻辑、密码加密、会话状态保持。

### 1. Spring Boot 基础骨架搭建
- **操作**：新建/修改 `Demo` 工程为 Spring Boot Maven 项目（引入 `spring-boot-starter-web`, `mybatis-plus-boot-starter`, `mysql-connector-j`）。
- **配置**：编写 `application.yml` 配置数据源连接。
- **全局处理**：
  - 编写 `GlobalExceptionHandler` 处理全局异常。
  - 编写 `CorsConfig` 实现全局跨域支持（替代原有的 `cors-filter` 依赖）。
  - 编写统一定义的响应封装类 `Result<T>`。

### 2. 用户域基础数据层迁移
- **涉及类**：`pojo.User`, `dao.UserJDBCTemplate` (废弃), `dao.UserMapper` (重构)。
- **操作**：
  - 将 `User` 实体类添加 MyBatis-Plus 的 `@TableName`, `@TableId` 注解。
  - 创建 MyBatis-Plus 的 `UserMapper` 接口（继承 `BaseMapper<User>`），直接获得单表 CRUD 能力。
  - 对于原来复杂的 SQL 拼接（如果有），转移到 XML 映射文件中或使用 Wrapper 构建器。

### 3. 认证功能迁移 (JWT 化)
- **涉及类**：`CookieController` (废弃), `LoginController`, `SignupController`, `RSAUtil`。
- **操作**：
  - **引入 JWT**：由于前端分离和微服务化趋势，放弃传统的 Session 保持。引入 `jjwt` 依赖。
  - **重构登录 (`LoginController`)**：校验账号密码，成功后生成 JWT Token 返回给前端。
  - **重构注册 (`SignupController`)**：保留或重写原有的 `RSAUtil` 逻辑（如果是前端加密传后端的方案），将用户存入 MySQL。
  - **拦截器/过滤器**：编写 `JwtAuthInterceptor`，拦截需要登录的接口，从请求头解析 Token 放入当前请求上下文中。

---

## 阶段二：公共词库与查词模块 (Word Books & Dictionary)

**涉及模块**：词库列表的加载、单个词汇信息的查询。

### 1. 词库与单词域数据层迁移
- **涉及类**：`pojo.WordBook`, `pojo.WordEntry`, `dao.WordBookJDBCTemplate` (废弃), `dao.WordEntryJDBCTemplate` (废弃)。
- **操作**：
  - 为 `WordBook` 和 `WordEntry` 创建实体类并添加 MyBatis-Plus 注解。
  - 创建 `WordBookMapper` 和 `WordEntryMapper`。

### 2. 词库查询接口迁移
- **涉及类**：`LoadBooksController`。
- **操作**：
  - 重写为 RESTful 风格的 API（如 `GET /api/books`）。
  - 利用 `WordBookMapper` 查询所有可用词库列表，返回给前端。

### 3. 查词与单词详情接口迁移
- **涉及类**：`WordInfoController`。
- **操作**：
  - 重写根据单词 ID 或拼写查询单词详情的接口（如 `GET /api/words/{id}`）。
  - 利用 `WordEntryMapper` 获取例句、音标、释义等。

---

## 阶段三：核心背单词与记忆算法模块 (Learning & Ebbinghaus)

**涉及模块**：背新词、复习旧词、艾宾浩斯记忆曲线算法的纯 MySQL 实现。

### 1. 核心学习逻辑迁移
- **涉及类**：`MemorizeController`, `StartController`。
- **操作**：
  - 重写 `StartController`，获取当前用户在所选词库中的“学习进度”（比如今天需要背的 10 个新词）。
  - 重写 `MemorizeController`。当用户背完一个词点击“认识/不认识”时，接收请求，计算该词的首次“熟悉度”并落库。

### 2. 复习算法引擎重构 (ReviewTableController)
- **涉及类**：`ReviewTableController` (核心重点重构)。
- **操作**：
  - **原逻辑梳理**：原代码根据记忆曲线计算下次复习时间。
  - **纯 MySQL 方案**：不使用 Redis，复习队列的计算直接通过 SQL。为复习表（可能是 `Review` 或 `Daily` 的变体）建立联合索引 `(user_id, next_review_time)`。
  - **获取复习任务 API**：执行类似 `SELECT * FROM user_review_table WHERE user_id = ? AND next_review_time <= NOW() ORDER BY next_review_time ASC LIMIT 20` 的高效查询。
  - **更新复习状态 API**：用户提交复习结果后，根据算法（如“认识”间隔乘2，“不认识”间隔归零）计算出新的 `next_review_time` 并 `UPDATE` 入库。
  - **优化**：在 Controller 中处理前端传来的批量状态（如果前端做了防抖优化），使用 MyBatis-Plus 的批量更新方法减轻数据库压力。

---

## 阶段四：打卡进度与生词本辅助模块 (Progress & Extras)

**涉及模块**：每日打卡记录、学习看板、个人专属生词本管理。

### 1. 进度与打卡域重构
- **涉及类**：`pojo.Daily`, `dao.DailyJDBCTemplate` (废弃), `ProgressController`, `PlanController`。
- **操作**：
  - 创建 `DailyMapper`。
  - 迁移 `PlanController`，允许用户设置每天的目标背词数。
  - 迁移 `ProgressController`，利用 MySQL 聚合查询（如 `GROUP BY DATE`）计算用户本周的打卡记录、已背词汇总数等数据。

### 2. 生词本域重构
- **涉及类**：`pojo.PrivateBooks`, `dao.PrivateBooksJDBCTemplate` (废弃), `PrivateTableController`, `AddPrivateController`。
- **操作**：
  - 创建 `PrivateBooksMapper`。
  - 迁移 `AddPrivateController`（添加生词到私人本）、`PrivateTableController`（分页查询、删除私人本词汇）。

### 3. 工具类与服务类迁移
- **涉及类**：`EmailController`, `RealEmailService`。
- **操作**：
  - 引入 `spring-boot-starter-mail`。
  - 将原有的原生 JavaMail 逻辑重构为 Spring Boot 的 `JavaMailSender` 自动装配模式，提升代码整洁度。
  - 修改 `EmailController` 提供发邮件的 REST 接口（如用于注册验证码或密码重置）。

---

## 阶段五：清理与收尾 (Cleanup & Verification)

### 1. 冗余代码清理
- 删除旧版的所有 `*JDBCTemplate` DAO 实现类。
- 删除 `applicationContext.xml`, `dispatcher-servlet.xml` 等陈旧配置。
- 清理废弃的 Servlet/JSP 依赖和过期的 `cors-filter` 等包。

### 2. 接口契约校验
- 使用 Postman 或 Apifox 测试所有新重构的 RESTful 接口。
- 确保新接口的入参出参 JSON 格式与前端 Vue 的预期完全一致。
- 重点验证“复习算法”返回的下一次复习时间是否符合原逻辑的预期。