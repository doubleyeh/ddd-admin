# ddd-admin
spring boot 4 spring data jpa querydsl mutil tenant

## 功能

- [x] 登录，
- [x] 租户管理
- [x] 用户管理
- [x] 角色管理
- [x] 权限配置
- [x] 菜单配置
- [x] 在线用户查询，踢人下线

## 项目结构说明

项目采用 DDD（领域驱动设计）分层架构，并结合模块化思想对包结构进行了划分，以实现高内聚、低耦合。

```
com.mok.ddd
├── application         // 应用层: 负责业务流程编排、DTO转换
│   ├── common          // 通用应用服务 (如 BaseService)
│   └── sys             // 系统管理模块 (用户、角色、菜单等)
│       ├── dto         // - 数据传输对象 (Data Transfer Object)
│       ├── mapper      // - MapStruct 转换接口
│       └── service     // - 应用服务实现
│
├── domain              // 领域层: 核心业务逻辑和领域模型
│   ├── common          // 通用领域对象 (如 BaseEntity)
│   └── sys             // 系统管理域
│       ├── model       // - 领域模型/实体 (Entity)
│       └── repository  // - 仓库接口 (Repository Interface)
│
├── infrastructure      // 基础设施层: 提供通用技术能力
│   ├── common          // 通用基础设施组件 (如 AOP 切面)
│   ├── config          // - Spring Boot 配置类
│   ├── limiter         // - 接口限流实现
│   ├── repository      // - 仓库通用实现 (CustomRepositoryImpl)
│   ├── security        // - Spring Security 通用配置
│   ├── sys             // 系统管理模块相关基础设施
│   │   └── security    //   - CustomUserDetailsService
│   └── tenant          // - 多租户核心实现
│
├── web                 // 接口层: 暴露 RESTful API
│   ├── common          // 通用 Web 组件 (如全局异常处理)
│   └── sys             // 系统管理模块 Controller
│
└── Application.java    // Spring Boot 启动类
```

### 分层职责

*   **web**: 接口层。负责接收 HTTP 请求，参数校验，并调用 `application` 层的服务。
*   **application**: 应用层。负责编排 `domain` 层的领域服务和仓库，处理 DTO 与领域模型的转换，实现具体的业务用例。
*   **domain**: 领域层。包含项目的核心业务逻辑。定义领域实体（Entity）和仓库接口（Repository），实现纯粹的领域服务。**此层不依赖任何其他层**。
*   **infrastructure**: 基础设施层。为其他层提供技术实现，如数据库访问（JPA 实现）、缓存（Redis）、安全（Spring Security）等。

## 后续计划

1.  **日志管理**
    - [x] 文件日志配置
    - [x] 接入操作日志（AOP），记录关键业务操作。
    - [x] 接入登录日志，记录用户登录行为及IP。

2.  **数据字典**
    - [ ] 实现数据字典管理，支持动态配置系统参数。

3.  **代码生成器**
    - [ ] 开发基于 DDD 架构的代码生成插件或工具，自动生成 Controller, Service, Repository, Entity, DTO。

4.  **文件服务**
    - [ ] 封装统一文件服务接口。
    - [ ] 支持本地存储、MinIO、阿里云 OSS 等多种存储策略。

5.  **定时任务**
    - [ ] 集成 Quartz 或 Spring Task，实现任务的可视化管理（暂停、恢复、立即执行）。

6.  **系统监控**
    - [ ] 集成 Spring Boot Admin 进行应用监控。
    - [ ] 接入 Prometheus + Grafana 进行性能指标监控。

7.  **消息通知**
    - [ ] 封装邮件、短信发送服务。
    - [ ] 集成 WebSocket 实现后端主动推送。

8.  **部署与运维**
    - [ ] 编写 Dockerfile。
    - [ ] 提供 Docker Compose 一键部署脚本。