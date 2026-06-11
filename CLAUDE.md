# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DataX-Web 是一个基于 Alibaba DataX 的分布式数据同步调度平台，提供 Web 界面管理数据同步任务。集成并二次开发了 xxl-job 实现分布式调度。

## Build & Run

```bash
# 构建（默认跳过测试）
mvn clean install -Dmaven.test.skip=true

# 构建并运行测试
mvn clean install

# 构建后产物在 build/ 目录
# 打包成 tar.gz: mvn clean install 后 datax-assembly 会生成 build/datax-web-2.1.2.tar.gz
```

运行 admin 模块：
```bash
# 需要 MySQL 数据库，先执行 doc/db/datax_web.sql 初始化
cd datax-admin
# 修改 application.yml 中的数据库连接信息
mvn spring-boot:run
```

运行 executor 模块：
```bash
cd datax-executor
# 修改 application.yml 中的 admin addresses、json path、python path 等
mvn spring-boot:run
```

## Module Architecture

```
datax-web/
├── datax-admin/         # 调度中心 (Spring Boot Web)
│   ├── controller/      # REST API 控制器
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # MyBatis-Plus 持久层
│   ├── entity/          # 数据实体
│   ├── dto/             # 数据传输对象
│   ├── tool/            # DataX JSON 构建工具
│   │   ├── datax/       # JSON 构建核心 (reader/writer)
│   │   ├── query/       # 各数据源查询工具 (MySQL/Oracle/Hive/HBase/MongoDB 等)
│   │   ├── meta/        # 元数据工具
│   │   └── database/    # 数据库连接工具
│   ├── core/            # 调度核心 (xxl-job 二次开发)
│   │   ├── scheduler/   # 调度器
│   │   ├── trigger/     # 任务触发
│   │   ├── route/       # 路由策略
│   │   ├── thread/      # 后台线程
│   │   ├── cron/        # Cron 表达式工具
│   │   ├── kill/        # 任务终止
│   │   └── handler/     # MyBatis 类型处理器
│   ├── config/          # Spring 配置 (Security/Swagger/MyBatisPlus/Web)
│   ├── filter/          # 过滤器
│   └── exception/       # 全局异常处理
│
├── datax-executor/      # 任务执行器 (Spring Boot)
│   ├── service/
│   │   ├── jobhandler/  # 任务处理器 (DataX/Shell/Python/PowerShell)
│   │   ├── command/     # 命令构建
│   │   └── logparse/    # 日志解析
│   └── util/            # 工具类 (如 Linux 系统监控)
│
├── datax-core/          # 核心库 (xxl-job 核心)
│   ├── biz/             # 业务模型 (AdminBiz/ExecutorBiz)
│   ├── executor/        # 执行器注册与运行
│   ├── handler/         # 任务处理接口与注解
│   ├── glue/            # GLUE 脚本任务支持
│   ├── thread/          # 执行器端线程
│   ├── log/             # 日志处理
│   └── enums/           # 枚举定义
│
├── datax-rpc/           # RPC 通信层
│   ├── remoting/        # 远程通信 (Netty/Netty HTTP)
│   ├── serialize/       # 序列化
│   ├── registry/        # 服务注册发现
│   └── util/            # JSON 工具
│
└── datax-assembly/      # 打包配置 (生成 tar.gz 发布包)
```

## Key Technical Stack

- **Java 8**, Spring Boot 2.1.4.RELEASE, MyBatis-Plus 3.3.1
- **MySQL 5.7+** (调度中心数据存储)
- **Swagger 2.9.2** + swagger-bootstrap-ui (API 文档)
- **JWT** (用户认证), **Spring Security** (权限控制)
- **Netty 4.1.43** (RPC 通信)
- **Vue.js** (前端，单独仓库 datax-web-ui，打包后放到 admin 的 static 目录)

## Data Source Support

通过 `tool/query/` 中的查询工具类支持多种数据源：MySQL、Oracle、PostgreSQL、SQLServer、Hive、HBase、MongoDB、ClickHouse、Phoenix、DM8 达梦数据库。

## Environment Variables

关键配置通过环境变量注入（详见 `datax-admin/application.yml`）：
- `DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD` - 数据库连接
- `APP_PORT` - 服务端口 (默认 8080)
- `MAIL_USERNAME`, `MAIL_PASSWORD` - 邮件告警
- `SWAGGER_ENABLE` - Swagger 开关

## Coding Conventions

- 使用 Lombok 简化实体代码
- MyBatis-Plus Mapper 放在 `mapper/` 包，XML 映射在 `resources/mybatis-mapper/`
- 前端静态资源直接存放于 `datax-admin/src/main/resources/static/`
- 任务 JSON 构建通过 `tool/datax/` 下的 reader/writer 策略类实现
