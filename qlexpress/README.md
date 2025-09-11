# 日志安全分析规则引擎

你现在拥有了一个企业级、可扩展、支持脚本热更新的日志安全分析规则引擎项目。

要求：

- 纯JAVA（Spring Boot + QLExpress）实现脚本化规则引擎
- 支持热加载规则（30秒自动刷新）
- 内置安全沙箱，防止脚本注入
- 自定义函数扩展（IP黑名单、关键词检测）
- REST API 友好，便于集成
- MySQL 持久化规则与告警

## 如何运行？

1. 创建数据库

    ```sql
    -- 执行 schema.sql 和 data.sql
    ```

2. 修改数据库密码

    在 `application.yml` 中替换：

    ```yaml
    spring:
    datasource:
        password: your_actual_mysql_password
    ```

3. 启动项目

    ```bash
    mvn spring-boot:run
    ```

    或直接在 IDE 中运行 `QlexpressDemoApplication.java`

4. 测试接口

查看激活规则

```http
GET http://localhost:8080/api/rules/active
```

测试日志分析（暴力破解）

```http
POST http://localhost:8080/api/rules/analyze
Content-Type: application/json

{
  "userId": "hacker007",
  "action": "LOGIN",
  "ip": "192.168.1.100",
  "status": "FAILED",
  "userAgent": "Mozilla/5.0 (sqlmap)",
  "resource": "/login",
  "timestamp": 1717027200000,
  "hour": 3,
  "failCount": 5
}
```

✅ 响应示例：

```json
{
  "id": null,
  "ruleName": "暴力登录检测",
  "alertType": "TRIGGERED",
  "logEvent": "{\"userId\":\"hacker007\", ...}",
  "triggeredAt": "2024-06-01T03:00:00",
  "handled": false
}
```

## TODO

- 增加规则编辑页面（Vue + Element Plus）
- 增加告警通知（邮件/钉钉/Webhook）
- 增加 Prometheus 指标暴露
- 增加规则执行耗时监控
- 使用 Redis 缓存活跃规则提升性能

AI生成提示词： **前端管理页面模板** 或 **Dockerfile + docker-compose.yml**
