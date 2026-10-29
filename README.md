<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">RuoYi-AI-WMS</h1>
<h4 align="center">基于 SpringBoot + Vue 的多租户进销存管理系统（内置 AI 助手）</h4>
<p align="center">
	<a href="https://gitee.com/y_project/RuoYi-Vue"><img src="https://img.shields.io/badge/RuoYi--AI--WMS-v3.9.1-brightgreen.svg"></a>
	<a href="./LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
</p>

## 平台简介

RuoYi-AI-WMS 是一套基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) 框架二次开发的多租户进销存（WMS）管理系统，在若依成熟的权限体系之上，扩展了完整的采购 / 销售 / 库存业务链路，并内置 PC 端 AI 助手，支持通过自然语言对话、语音输入、截图识别等方式快速完成单据录入与查询。

* 前端：Vue 2.6 + Element UI
* 后端：Spring Boot 3.5 / Spring Security + JWT / MyBatis-Plus / Redis
* 数据库：MySQL 8.x
* 多租户 SaaS 架构，租户间数据隔离
* AI 能力基于阿里云 DashScope（通义千问系列模型）

## 核心特性

### 进销存业务

- **商品管理**：商品档案、分类、规格、条码管理
- **采购管理**：采购订单、采购入库、供应商管理与比价
- **销售管理**：销售订单、销售出库、客户管理
- **库存管理**：实时库存、库存流水、盘点、库存预警
- **单据打印**：出/入库单据 PDF 打印，打印模板可配置
- **多租户**：租户注册、租户级数据隔离与独立配置

### AI 助手（PC 端）

- **对话式建单**：一句话创建采购/销售单据，AI 自动补全缺失信息、逐项确认
- **语音录入**：语音转文字，动口即可录入商品明细
- **截图识别**：粘贴/上传单据截图，AI 视觉模型自动提取商品明细填充表单
- **智能查询**：自然语言查询库存、订单状态等业务数据

## 内置功能（若依基础模块）

1. 用户管理、部门管理、岗位管理
2. 菜单管理、角色管理（按钮级权限、数据权限）
3. 字典管理、参数管理、通知公告
4. 操作日志、登录日志、在线用户
5. 定时任务调度、代码生成、系统接口（Swagger）
6. 服务监控、缓存监控、连接池监视

## 快速开始

### 环境要求

- JDK 17
- MySQL 8.x
- Redis
- Node.js 14+（前端构建）

### 后端

```bash
# 1. 创建数据库 ry-vue（utf8mb4），执行 sql/ 目录下的初始化脚本
# 2. 修改 ruoyi-admin/src/main/resources/application-druid.yml 中的数据库连接
#    修改 application.yml 中的 Redis 配置
# 3. 启动
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 或在 IDEA 中直接运行 RuoYiApplication
```

后端默认运行在 `http://localhost:8080`。

### 前端

```bash
cd ruoyi-ui
npm install
npm run dev
```

访问 `http://localhost:8081`（开发模式，已配置代理转发至后端 8080）。

### 默认账号

- 管理员：`admin / admin123`
- 体验账号：登录页点击「体验一下」一键进入

## AI 能力配置

在 `ruoyi-admin/src/main/resources/application.yml` 的 `ai.service` 段配置阿里云 DashScope：

```yaml
ai:
  service:
    qwen:
      api-key: 你的 DashScope API Key   # https://dashscope.console.aliyun.com
      model: qwen3.6-flash              # 对话模型
    vision:
      model: qwen3.8-omni-flash         # 截图识别视觉模型
```

各租户也可在「企业设置」页面配置自己的 API Key 与模型，租户未配置时回退使用全局配置。

## 目录结构

```
RuoYi-AI-WMS
├── ruoyi-admin      # 主应用（启动入口、Web 控制器）
├── ruoyi-wms        # WMS 业务（进销存、AI 助手、打印）
├── ruoyi-system     # 系统模块（用户/角色/菜单等）
├── ruoyi-framework  # 框架核心（安全、多租户、拦截器）
├── ruoyi-common     # 通用工具
├── ruoyi-generator  # 代码生成
├── ruoyi-quartz     # 定时任务
├── ruoyi-ui         # 前端（Vue2 + Element UI）
└── sql              # 数据库脚本
```

## 致谢

- [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)：本项目的基础框架
- [DashScope](https://dashscope.console.aliyun.com)：AI 能力支持

## License

[MIT](./LICENSE)
