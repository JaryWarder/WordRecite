# 背单词网页项目重构与演进计划

> **Goal:** 将基于老旧技术栈（Spring 4.x + 外部 Tomcat + Vue 2）的背单词项目，平滑重构为现代化的 **Spring Boot 3.x + Vue 3/Vite** 架构。
> **核心原则:**
> 1. **纯 MySQL 架构**：移除引入 Redis 缓存的计划，所有业务逻辑（包括高频打卡和复习算法）均直接使用 MySQL 持久化，保持部署极简。
> 2. **画风不变**：前端完全复用原有 CSS 样式和 UI 组件布局，用户端零感知升级。
> 3. **稳步推进**：优先实现原有基础功能闭环，测试无误后再规划后续的新功能开发。
> 4. **Skill 驱动执行**：在开始任何具体任务的思考和代码编写之前，必须先检查并评估当前可用的 Skills（如 `brainstorming`, `executing-plans`, `frontend-design`, `springboot-patterns` 等）。如果发现适用的 Skill，必须在第一步予以调用。

---

## 阶段一：项目工程初始化与基建搭建 (Project Setup)

**目标：** 搭建全新的前后端基础工程，摆脱旧版 Tomcat 和 Webpack 的依赖。

- [ ] **Step 1.1: 创建后端 Spring Boot 工程**
  - 在 `Demo` 目录下（或新建 `backend` 目录）使用 Spring Initializr 初始化 Spring Boot 项目（推荐 JDK 17/21 + Spring Boot 3.x）。
  - 引入核心依赖：`spring-boot-starter-web`, `mybatis-plus-boot-starter`, `mysql-connector-j`, `jjwt` (用于替换原有 Session 或补全 Token 校验)。
- [ ] **Step 1.2: 基础配置与连通性测试**
  - 编写 `application.yml` 配置 MySQL 连接信息。
  - 编写跨域配置类 `CorsConfig`，替代旧版的 `cors-filter`。
  - 编写全局异常处理 `GlobalExceptionHandler`。
  - 测试数据库连通，确保能正常连接并读取现有 `DB` 目录下的表。
- [ ] **Step 1.3: 创建前端 Vite + Vue 3 工程**
  - 在 `front` 同级目录（或直接改造 `front`）使用 `npm create vite@latest` 初始化 Vue 3 项目。
  - 引入核心依赖：`vue-router` (v4), `pinia` (替代 vuex 或旧版状态管理), `axios`。
- [ ] **Step 1.4: 静态资源迁移**
  - 将旧版 `front/src/assets` 下的图片库和 `statics/css/test.css` 完整拷贝到新工程，确保全局样式基调一致。

---

## 阶段二：后端核心业务层迁移 (Backend Migration)

**目标：** 将旧版 `Controller`, `Service`, `JDBCTemplate DAO` 平滑迁移为 MyBatis-Plus 架构。

- [ ] **Step 2.1: 实体类与数据访问层 (DAO) 重构**
  - 迁移 `pojo` 目录下的所有实体类（`User`, `WordBook`, `WordEntry`, `Daily`, `PrivateBooks` 等）。
  - 废弃 `JDBCTemplate`，使用 MyBatis-Plus 创建对应的 `Mapper` 接口（如 `UserMapper`, `WordBookMapper`）。
- [ ] **Step 2.2: 用户认证与鉴权模块迁移**
  - 重写 `LoginController` 和 `SignupController`。
  - 实现基于 JWT 的无状态登录，替换可能的 Session 依赖。
  - 重写密码加密逻辑（保留原有的 `RSAUtil` 或引入 `BCrypt` 兼容旧数据）。
- [ ] **Step 2.3: 词库与生词本模块迁移**
  - 重写 `LoadBooksController`（词库列表获取）。
  - 重写 `PrivateTableController` 和 `AddPrivateController`（用户专属生词本的增删改查）。
- [ ] **Step 2.4: 核心记忆算法与复习模块迁移（纯 MySQL 版）**
  - 重写 `MemorizeController`（新词学习进度提交）。
  - 重写 `ReviewTableController`（根据艾宾浩斯记忆曲线计算下次复习时间的逻辑）。
  - **数据库优化策略**：针对纯 MySQL 方案，为 `Review` 相关表的高频查询字段（如 `user_id`, `next_review_time`）建立联合索引，提升查询性能。
- [ ] **Step 2.5: 边缘辅助模块迁移**
  - 迁移 `EmailController`（邮件发送验证）。
  - 迁移 `PlanController`, `ProgressController`（学习计划与进度统计）。

---

## 阶段三：前端现代化重构 (Frontend Migration)

**目标：** 用 Composition API (setup) 重写旧版 Vue 2 组件，保留原有的 `<template>` 和 `<style>`。

- [ ] **Step 3.1: 路由与状态管理构建**
  - 根据旧版 `router/index.js` 重构 Vue Router 4 路由表。
  - 使用 Pinia 构建全局 User Store（管理 Token、当前选中的词库 ID、打卡状态等）。
- [ ] **Step 3.2: 网络请求层封装**
  - 封装 Axios 实例，统一处理请求拦截（携带 Token）和响应拦截（处理 401 登出、统一错误提示）。
- [ ] **Step 3.3: 核心页面组件无损迁移**
  - 迁移 `HomeIndex.vue` / `Start.vue`（主页与启动页）。
  - 迁移 `WordCard.vue` / `TestCard.vue`（核心背单词与测试卡片，复用原有 CSS 类名和翻转动画）。
  - 迁移 `WordBooks.vue` / `PrivateTable.vue`（词库列表与生词本）。
  - 迁移 `Review.vue` / `Progress.vue`（复习页面与进度数据图表）。
- [ ] **Step 3.4: 弹窗与通用组件迁移**
  - 迁移 `LoginDialog.vue`, `SignUpDialog.vue` 等通用弹窗组件。

---

## 阶段四：系统联调与回归验证 (Integration & Testing)

**目标：** 确保新旧系统逻辑一致，业务流程完整闭环。

- [ ] **Step 4.1: 核心链路联调**
  - 测试链路 A：新用户注册 -> 登录 -> 选词库 -> 背单词 10 个 -> 生成今日打卡记录。
  - 测试链路 B：老用户登录 -> 读取复习队列 -> 完成复习 -> 查看看板进度。
- [ ] **Step 4.2: 并发与压力测试（MySQL 性能摸底）**
  - 针对无 Redis 缓存的设计，模拟多个用户同时点击“认识/不认识”提交复习状态。
  - 监控 MySQL 的 CPU 与 IO 占用，必要时对高频 UPDATE 的 SQL 语句进行批量化改造（如前端防抖，攒 5 个单词再发一次批量更新请求）。
- [ ] **Step 4.3: 部署测试**
  - 后端打出 `jar` 包并使用 `java -jar` 独立运行。
  - 前端使用 `npm run build` 打包出静态文件，配合 Nginx 或 Spring Boot 静态资源目录进行全链路访问测试。
