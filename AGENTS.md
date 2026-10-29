# RuoYi-AI-WMS — AI 协作者入口（AGENTS.md）

> 基于 RuoYi 的进销存 WMS（多租户 SaaS + AI 能力）。  
> 跨工具共用入口（WorkBuddy / Claude Code / Copilot 等）：**每次开发前先读本文件，全程遵循本文件规范**。  
> 结构参考 automation-hub/AGENTS.md：本文件只放高频要点与铁律，详细介绍见 `README.md` 与各模块代码注释。

## 项目速览

- 技术栈：Spring Boot 3.5.8（非 2.x）/ Java 17 / MyBatis-Plus 3.5.5 / Druid 1.2.27 / Redis / MySQL 8.2.0 / Springdoc 2.8.14。
- PC 前端 `ruoyi-ui`：**Vue 2.6.12 + Element UI 2.15.14**（不是 Vue3 / Element Plus，不要写组合式 API）。
- 移动端不在本仓库范围内（详见铁律 6）。
- 模块划分：`ruoyi-admin`（主应用 + web 控制器，WMS 控制器在 `web/controller/wms/`）｜`ruoyi-wms`（WMS 业务 Service/Domain/Mapper）｜`ruoyi-system`｜`ruoyi-framework`｜`ruoyi-common`｜`ruoyi-generator`｜`ruoyi-quartz`。
- 端口：后端 8080｜Redis 6379。
- 目录：SQL 脚本 `sql/`（按日期命名）。

## 常用命令

本机为 **macOS**（Maven 用 IDEA 内置版，无全局 mvn）：

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
MVN="/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin/mvn"

$MVN -pl ruoyi-admin -am -DskipTests clean compile   # 后端编译验证（改 Java 后必跑）
$MVN clean package -DskipTests                        # 打包（先删 ruoyi-admin/target/ruoyi-admin.jar 旧包）

cd ruoyi-ui && npm run dev                            # PC 前端
```

数据库连接：本地 `ry-vue`，连接信息读 `ruoyi-admin/src/main/resources/application-druid.yml`（root@localhost:3306）。

## 铁律（改代码前必读）

1. **改 Java 代码必须编译验证**：`mvn compile` 出 `BUILD SUCCESS` 才算完成，不许只改不编。
2. **打包前必删旧 jar**（`ruoyi-admin/target/ruoyi-admin.jar`），避免覆盖失败导致「代码没生效」的假象。
3. **表结构变更流程**：先在本地 `ry-vue` 库编写并验证 → 脚本命名 `YYYY-MM-DD-功能描述.sql` → 存入 `sql/`，头部注释写明用途/日期/执行结果，附回滚语句。**禁止在生产库直接执行未验证脚本**。
4. **前端遵循 Vue2 + Element UI 语法**，升级依赖前先确认兼容性。
5. **分支纪律**：改动只做在与本分支定位相符的范围内，不混入其他性质的修改。新建分支用 `git checkout -b <name> --no-track <base>`，避免误设上游跟踪。
6. **移动端不在本仓库范围内**：涉及移动端的需求去对应仓库做。
7. **凭证不入文档**：本文件不写明文密码；数据库密码读 `application-druid.yml`，其他本机凭证向用户索取。

## 凭证与连接

| 项                  | 位置 / 说明                                                                     |
| ------------------ | --------------------------------------------------------------------------- |
| MySQL（本地 `ry-vue`） | `ruoyi-admin/src/main/resources/application-druid.yml`（root@localhost:3306） |
| Redis              | `ruoyi-admin/src/main/resources/application.yml`                            |

> 说明：数据库密码在 `application-druid.yml` 中为明文（RuoYi 惯例，仅限本机开发库）；其他本机凭证不入文档，向用户索取。

## 验证方式

- 后端：`mvn -pl ruoyi-admin -am -DskipTests clean compile` → BUILD SUCCESS。
- 数据库变更：本地 `ry-vue` 执行无错 + 业务功能点检。
- 前端：`npm run dev` 启动后页面功能点检。
- 联调地址：`http://localhost:8080`。

## AI 协作（执行模式：自动执行）

- **修 Bug / 整理文件 / 重构 / 明确需求的新功能：直接做，完成后汇报**；不问「要修复吗」「确认吗」。
- 只有**需求不明确**或**存在多种方案**时才询问用户。
- 会话进度记录以 `.workbuddy/memory/`（本地工作日志）+ `git log` 为准。

---

