# 玲珑

> 一款专为中小企业设计的 **IPD 研发管理平台**

---

## 🎯 产品定位

| 维度 | 描述 |
|------|------|
| 目标用户 | 50-500人研发团队的 CTO、PMO、项目经理，开发测试人员 |
| 核心场景 | 研发管理（软件/硬件/制造）|
| 关键价值 | 完整 IPD 流程支撑 + 轻量化 + 现代化体验 |

## 核心功能特性

### Sprint 1 — 基础框架
- ✅ 用户认证（JWT Token）
- ✅ 角色权限（10种内置角色）
- ✅ 需求管理
- ✅ 项目管理
- ✅ 任务管理
- ✅ 评审管理（CDCP/PDCP/TR4/ADCP）

### Sprint 2 — 迭代与看板
- ✅ Sprint 管理（创建/开始/完成/关闭）
- ✅ 任务看板（拖拽式状态流转）
- ✅ 燃尽图（ECharts 可视化）
- ✅ 通知系统（实时推送）

### Sprint 3 — 测试与文档
- ✅ **测试用例管理**（用例库、CRUD）
- ✅ **缺陷管理**（Bug 追踪、状态流转）
- ✅ **文档管理**（树形目录、Markdown）
- ✅ **质量仪表盘**（ECharts 统计图表）

### Sprint 4 — 规划中
- [ ] 甘特图视图
- [ ] 移动端 APP
- [ ] 钉钉/飞书集成
- [ ] API 开放平台

## 🔧 技术栈

| 层级 | 技术选型 |
|------|---------|
| 前端 | Vue 3 + Vite + TypeScript + Element Plus + ECharts |
| 后端 | Spring Boot 3 + Java 17 |
| 数据库 | PostgreSQL / H2（开发）|
| 缓存 | Redis |
| 安全 | Spring Security + JWT |
| 部署 | Docker Compose |

## 🚀 快速启动
```bash
# 后端
cd src/backend
mvn spring-boot:run

# 前端
cd src/frontend
npm install && npm run dev
```

**访问** http://localhost:3000 | 账号：`admin` / `admin123`

## 🔐 内置角色

| 角色 | 代码 | 说明 |
|------|------|------|
| 超级管理员 | ADMIN | 全系统权限 |
| 产品经理 | PM | 产品规划、需求管理 |
| 项目经理 | PGM | 项目管理、进度控制 |
| 开发负责人 | TL | 技术方案、代码评审 |
| 开发者 | DEV | 任务开发 |
| 测试经理 | QAM | 测试策略、质量把控 |
| 测试人员 | QA | 测试执行、缺陷报告 |
| 访客 | VIEWER | 只读访问 |

## 📁 项目结构

```
Linglo/
├── docs/                    # 项目文档（竞品分析、解决方案）
├── src/
│   ├── backend/            # Spring Boot 后端
│   │   └── src/main/java/com/ipd/platform/
│   │       ├── controller/ # REST API
│   │       ├── service/    # 业务逻辑
│   │       ├── repository/ # 数据访问
│   │       ├── entity/     # 实体类
│   │       ├── dto/        # 数据传输
│   │       ├── security/   # 安全认证
│   │       └── config/     # 配置
│   └── frontend/           # Vue 3 前端
│       └── src/
│           ├── views/       # 页面
│           ├── components/  # 组件
│           ├── api/         # API 调用
│           ├── stores/      # 状态管理
│           └── router/      # 路由
├── deploy/                 # Docker 部署
└── README.md
```

## 📊 开发进度
| Sprint | 内容 | 状态 |
|--------|------|------|
| Sprint 0 | 项目初始化 | ✅ |
| Sprint 1 | 基础框架 | ✅ |
| Sprint 2 | 看板/Sprint/通知 | ✅ |
| Sprint 3 | 测试/文档/质量 | ✅ |
| Sprint 4 | 甘特图/移动端/集成 | 📋 |

---

**玲珑** — 让每一家中小企业都能享受 IPD 体系带来的研发管理升级 🚀
