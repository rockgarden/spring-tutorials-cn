# 在 DataX 应用 Freemarker

如何对脚本进行参数化配置管理：

## 1. 占位符替换方式（当前方式）

```json
{
  "job": {
    "parameter": {
      "username": "${db_username}",
      "password": "${db_password}"
    }
  }
}
```

## 2. 外部参数文件方式

### JSON 参数文件

```json
// job.json
{
  "job": {
    "parameter": {
      "username": "@{parameters.username}",
      "password": "@{parameters.password}"
    }
  }
}

// parameters.json
{
  "parameters": {
    "username": "admin",
    "password": "123456"
  }
}
```

### YAML 参数文件

```yaml
# config.yaml
database:
  username: ${DB_USER:default_user}
  password: ${DB_PASS:default_pass}
  url: ${DB_URL:localhost:3306}

# 在脚本中引用
{
  "job": {
    "parameter": {
      "username": "@{database.username}",
      "password": "@{database.password}"
    }
  }
}
```

## 3. 环境变量注入方式

```bash
# 设置环境变量
export DB_USERNAME=admin
export DB_PASSWORD=123456
export JDBC_URL=jdbc:mysql://localhost:3306/test

# 脚本中引用
{
  "job": {
    "parameter": {
      "username": "${env:DB_USERNAME}",
      "password": "${env:DB_PASSWORD}",
      "connection": [
        {
          "jdbcUrl": ["${env:JDBC_URL}"]
        }
      ]
    }
  }
}
```

## 4. 命令行参数方式

```bash
# DataX执行时传参
python datax.py job.json -p"-Dusername=admin -Dpassword=123456"

# 脚本中引用
{
  "job": {
    "parameter": {
      "username": "${username}",
      "password": "${password}"
    }
  }
}
```

## 5. 配置中心方式

```java
// 集成配置中心（如Nacos、Consul、Apollo）
public class ConfigCenterParameterResolver {

    public String resolveParameters(String template, String configNamespace) {
        // 从配置中心获取参数
        Map<String, String> params = configCenter.getConfig(configNamespace);

        // 替换模板中的占位符
        return PlaceholderUtils.replacePlaceholders(template, params, true);
    }
}
```

## 6. 数据库配置方式

```sql
-- 配置表
CREATE TABLE job_parameters (
    job_id VARCHAR(50),
    param_name VARCHAR(100),
    param_value TEXT,
    param_type VARCHAR(20)
);

-- 查询参数
SELECT param_name, param_value
FROM job_parameters
WHERE job_id = 'job_001';
```

## 7. 模板引擎方式

### 使用 Freemarker

```ftl
<!-- job_template.ftl -->
{
  "job": {
    "parameter": {
      "username": "${username!'default_user'}",
      "password": "${password!'default_pass'}",
      <#if tables??>
      "tables": [
        <#list tables as table>
        "${table}"<#if table_has_next>,</#if>
        </#list>
      ]
      </#if>
    }
  }
}
```

### 使用 Thymeleaf

```html
<!-- job_template.json -->
{ "job": { "parameter": { "username": "[[${username}]]", "password":
"[[${password}]]" } } }
```

## 8. 编程式构建方式

```java
public class DataxConfigBuilder {

    public static class JobBuilder {
        private Map<String, Object> parameters = new HashMap<>();

        public JobBuilder parameter(String key, Object value) {
            parameters.put(key, value);
            return this;
        }

        public String build() {
            // 动态构建JSON
            ObjectNode root = JSONUtils.createObjectNode();
            ObjectNode job = root.putObject("job");
            ObjectNode parameter = job.putObject("parameter");

            parameters.forEach((key, value) -> {
                if (value instanceof String) {
                    parameter.put(key, (String) value);
                } else if (value instanceof Number) {
                    parameter.put(key, (Number) value);
                }
                // ... 其他类型处理
            });

            return JSONUtils.toJsonString(root);
        }
    }
}
```

## 9. 混合方式（推荐）

```java
public class HybridParameterManager {

    public enum ParameterSource {
        PLACEHOLDER,      // ${param}
        ENVIRONMENT,      // ${env:PARAM}
        SYSTEM_PROPERTY,  // ${sys:param}
        DATABASE,         // ${db:param}
        CONFIG_CENTER,    // ${config:param}
        DEFAULT_VALUE     // ${param:default}
    }

    public String processTemplate(String template, ExecutionContext context) {
        // 多源参数解析
        Map<String, String> allParameters = new HashMap<>();

        // 1. 任务参数
        allParameters.putAll(context.getTaskParameters());

        // 2. 全局参数
        allParameters.putAll(context.getGlobalParameters());

        // 3. 环境变量
        System.getenv().forEach((k, v) ->
            allParameters.put("env:" + k, v));

        // 4. 系统属性
        System.getProperties().forEach((k, v) ->
            allParameters.put("sys:" + k, v.toString()));

        // 5. 数据库参数
        allParameters.putAll(loadDatabaseParameters(context.getJobId()));

        // 多层次替换
        return replaceParameters(template, allParameters);
    }
}
```

## DolphinScheduler 中的最佳实践

### 1. 保持现有占位符方式（兼容性）

```json
{
  "job": {
    "parameter": {
      "username": "${username}",
      "password": "${password}"
    }
  }
}
```

### 2. 增强参数管理

```
[全局参数] → [任务参数] → [环境变量] → [默认值]
     ↓
按优先级自动解析
```

### 3. 提供多种参数来源

```java
public class ParameterResolverChain {

    public String resolve(String template, TaskExecutionContext context) {
        // 1. 任务定义参数
        Map<String, String> params = new HashMap<>(context.getDefinedParameters());

        // 2. 全局参数覆盖
        params.putAll(context.getGlobalParameters());

        // 3. 环境变量补充
        System.getenv().forEach((k, v) -> {
            if (!params.containsKey(k)) {
                params.put(k, v);
            }
        });

        // 4. 系统属性补充
        System.getProperties().forEach((k, v) -> {
            String key = k.toString();
            if (!params.containsKey(key)) {
                params.put(key, v.toString());
            }
        });

        return PlaceholderUtils.replacePlaceholders(template, params, true);
    }
}
```

## 总结

参数化配置管理有多种方式：

1. **占位符替换**（当前方式）- 简单直观
2. **外部配置文件** - 结构化管理
3. **环境变量** - 安全性好
4. **命令行参数** - 灵活性高
5. **配置中心** - 集中管理
6. **数据库配置** - 动态更新
7. **模板引擎** - 功能强大
8. **编程构建** - 类型安全

对于 DolphinScheduler 来说，**占位符方式仍然是最合适的**，但可以：

- 增强参数来源的多样性
- 提供参数管理界面
- 支持参数验证和提示
- 保持向后兼容性

## 1. 添加依赖

```xml
<dependency>
    <groupId>org.freemarker</groupId>
    <artifactId>freemarker</artifactId>
    <version>2.3.31</version>
</dependency>
```

## 2. 创建模板引擎管理器

```java
public class DataxTemplateEngine {

    private static final Logger logger = LoggerFactory.getLogger(DataxTemplateEngine.class);

    private Configuration configuration;

    public DataxTemplateEngine() {
        initTemplateEngine();
    }

    private void initTemplateEngine() {
        configuration = new Configuration(Configuration.VERSION_2_3_31);

        // 设置模板加载路径
        try {
            // 从classpath加载模板
            configuration.setClassLoaderForTemplateLoading(
                Thread.currentThread().getContextClassLoader(),
                "datax/templates"
            );

            // 设置编码
            configuration.setDefaultEncoding("UTF-8");

            // 设置模板异常处理
            configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            // 不缓存null值
            configuration.setCacheStorage(new freemarker.cache.MruCacheStorage(0, Integer.MAX_VALUE));

        } catch (Exception e) {
            logger.error("Failed to initialize DataX template engine", e);
            throw new RuntimeException("Failed to initialize template engine", e);
        }
    }

    /**
     * 渲染DataX模板
     *
     * @param templateName 模板名称
     * @param context 模板上下文
     * @return 渲染后的JSON字符串
     */
    public String renderTemplate(String templateName, DataxTemplateContext context) {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(context, writer);
            return writer.toString();
        } catch (Exception e) {
            logger.error("Failed to render template: {}", templateName, e);
            throw new RuntimeException("Failed to render DataX template: " + templateName, e);
        }
    }

    /**
     * 验证模板是否存在
     */
    public boolean templateExists(String templateName) {
        try {
            return configuration.getTemplate(templateName) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## 3. 创建模板上下文对象

```java
public class DataxTemplateContext {

    // 基础数据库连接信息
    private String sourceType;
    private String sourceUsername;
    private String sourcePassword;
    private String sourceJdbcUrl;
    private String sourceTable;

    private String targetType;
    private String targetUsername;
    private String targetPassword;
    private String targetJdbcUrl;
    private String targetTable;

    // 任务配置
    private int channel = 3;
    private int batchSize = 1024;
    private int byteSpeed = 1048576;
    private int recordSpeed = 1000;

    // 数据处理配置
    private String whereCondition = "";
    private String splitPk = "";
    private List<String> columns = Arrays.asList("*");
    private List<String> preSql = new ArrayList<>();
    private List<String> postSql = new ArrayList<>();

    // 自定义参数
    private Map<String, Object> customParameters = new HashMap<>();

    // Getters and Setters
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getSourceUsername() { return sourceUsername; }
    public void setSourceUsername(String sourceUsername) { this.sourceUsername = sourceUsername; }

    public String getSourcePassword() { return sourcePassword; }
    public void setSourcePassword(String sourcePassword) { this.sourcePassword = sourcePassword; }

    public String getSourceJdbcUrl() { return sourceJdbcUrl; }
    public void setSourceJdbcUrl(String sourceJdbcUrl) { this.sourceJdbcUrl = sourceJdbcUrl; }

    public String getSourceTable() { return sourceTable; }
    public void setSourceTable(String sourceTable) { this.sourceTable = sourceTable; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getTargetUsername() { return targetUsername; }
    public void setTargetUsername(String targetUsername) { this.targetUsername = targetUsername; }

    public String getTargetPassword() { return targetPassword; }
    public void setTargetPassword(String targetPassword) { this.targetPassword = targetPassword; }

    public String getTargetJdbcUrl() { return targetJdbcUrl; }
    public void setTargetJdbcUrl(String targetJdbcUrl) { this.targetJdbcUrl = targetJdbcUrl; }

    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }

    public int getChannel() { return channel; }
    public void setChannel(int channel) { this.channel = channel; }

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public int getByteSpeed() { return byteSpeed; }
    public void setByteSpeed(int byteSpeed) { this.byteSpeed = byteSpeed; }

    public int getRecordSpeed() { return recordSpeed; }
    public void setRecordSpeed(int recordSpeed) { this.recordSpeed = recordSpeed; }

    public String getWhereCondition() { return whereCondition; }
    public void setWhereCondition(String whereCondition) { this.whereCondition = whereCondition; }

    public String getSplitPk() { return splitPk; }
    public void setSplitPk(String splitPk) { this.splitPk = splitPk; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<String> getPreSql() { return preSql; }
    public void setPreSql(List<String> preSql) { this.preSql = preSql; }

    public List<String> getPostSql() { return postSql; }
    public void setPostSql(List<String> postSql) { this.postSql = postSql; }

    public Map<String, Object> getCustomParameters() { return customParameters; }
    public void setCustomParameters(Map<String, Object> customParameters) {
        this.customParameters = customParameters;
    }

    // 便捷方法
    public void addCustomParameter(String key, Object value) {
        this.customParameters.put(key, value);
    }

    // 用于模板中的工具方法
    public boolean hasPreSql() {
        return preSql != null && !preSql.isEmpty();
    }

    public boolean hasPostSql() {
        return postSql != null && !postSql.isEmpty();
    }

    public boolean hasColumns() {
        return columns != null && !columns.isEmpty() &&
               !(columns.size() == 1 && "*".equals(columns.get(0)));
    }
}
```

## 4. 创建模板文件

### MySQL 到 MySQL 模板 (`mysql-to-mysql.ftl`)

```json
{
  "job": {
    "content": [
      {
        "reader": {
          "name": "mysqlreader",
          "parameter": {
            "username": "${sourceUsername}",
            "password": "${sourcePassword}",
            "connection": [
              {
                "jdbcUrl": ["${sourceJdbcUrl}"],
                "table": ["${sourceTable}"]
              }
            ]
            <#if hasColumns()>,
            "column": [
              <#list columns as col>
              "${col}"<#if col_has_next>,</#if>
              </#list>
            ]
            </#if>
            <#if splitPk?has_content>,
            "splitPk": "${splitPk}"
            </#if>
            <#if whereCondition?has_content>,
            "where": "${whereCondition}"
            </#if>
          }
        },
        "writer": {
          "name": "mysqlwriter",
          "parameter": {
            "username": "${targetUsername}",
            "password": "${targetPassword}",
            "writeMode": "replace",
            "batchSize": ${batchSize},
            "connection": [
              {
                "jdbcUrl": ["${targetJdbcUrl}"],
                "table": ["${targetTable}"]
              }
            ]
            <#if hasColumns()>,
            "column": [
              <#list columns as col>
              "${col}"<#if col_has_next>,</#if>
              </#list>
            ]
            </#if>
            <#if hasPreSql()>,
            "preSql": [
              <#list preSql as sql>
              "${sql}"<#if sql_has_next>,</#if>
              </#list>
            ]
            </#if>
            <#if hasPostSql()>,
            "postSql": [
              <#list postSql as sql>
              "${sql}"<#if sql_has_next>,</#if>
              </#list>
            ]
            </#if>
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": ${channel},
        "byte": ${byteSpeed},
        "record": ${recordSpeed}
      },
      "errorLimit": {
        "record": 0,
        "percentage": 0.02
      }
    }
  }
}
```

### HDFS 到 MySQL 模板 (`hdfs-to-mysql.ftl`)

```json
{
  "job": {
    "content": [
      {
        "reader": {
          "name": "hdfsreader",
          "parameter": {
            "path": "${sourcePath}",
            "defaultFS": "${sourceDefaultFS}",
            "fileType": "${sourceFileType!'text'}",
            "column": [
              <#list columns as col>
              {
                "index": ${col_index},
                "type": "${col_type!'string'}"
              }<#if col_has_next>,</#if>
              </#list>
            ],
            "fieldDelimiter": "${fieldDelimiter!'\\t'}"
          }
        },
        "writer": {
          "name": "mysqlwriter",
          "parameter": {
            "username": "${targetUsername}",
            "password": "${targetPassword}",
            "connection": [
              {
                "jdbcUrl": ["${targetJdbcUrl}"],
                "table": ["${targetTable}"]
              }
            ],
            "column": [
              <#list columns as col>
              "${col_name}"<#if col_has_next>,</#if>
              </#list>
            ],
            "writeMode": "replace",
            "batchSize": ${batchSize}
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": ${channel}
      }
    }
  }
}
```

## 5. 修改 DataxTaskChannel

```java
public class DataxTaskChannel implements TaskChannel {

    private static final DataxTemplateEngine templateEngine = new DataxTemplateEngine();

    @Override
    public AbstractTask createTask(TaskExecutionContext taskRequest) {
        return new DataxTask(taskRequest);
    }

    @Override
    public AbstractParameters parseParameters(ParametersNode parametersNode) {
        String rawTaskParams = parametersNode.getTaskParams();

        // 解析基础参数
        DataxParameters dataxParams = JSONUtils.parseObject(rawTaskParams, DataxParameters.class);

        if (dataxParams != null && dataxParams.getCustomConfig() == 1) {
            // 使用模板引擎处理
            String processedJson = processTemplateConfiguration(dataxParams, parametersNode);
            dataxParams.setJson(processedJson);
        }

        return dataxParams;
    }

    @Override
    public ResourceParametersHelper getResources(String parameters) {
        return JSONUtils.parseObject(parameters, DataxParameters.class).getResources();
    }

    @Override
    public void cancelApplication(boolean status) {
        // 实现取消逻辑
    }

    /**
     * 使用模板引擎处理配置
     */
    private String processTemplateConfiguration(DataxParameters dataxParams, ParametersNode parametersNode) {
        String rawJson = dataxParams.getJson();

        // 检查是否是模板引用格式
        if (isTemplateReference(rawJson)) {
            // 解析模板引用
            TemplateReference templateRef = parseTemplateReference(rawJson);

            // 构建模板上下文
            DataxTemplateContext context = buildTemplateContext(parametersNode);

            // 渲染模板
            return templateEngine.renderTemplate(templateRef.getTemplateName(), context);
        } else {
            // 传统占位符处理
            Map<String, String> paramsMap = buildParametersMap(parametersNode);
            return PlaceholderUtils.replacePlaceholders(rawJson, paramsMap, true);
        }
    }

    /**
     * 检查是否是模板引用
     * 格式: template:mysql-to-mysql 或 template:hdfs-to-mysql
     */
    private boolean isTemplateReference(String json) {
        return json != null && json.trim().startsWith("template:");
    }

    /**
     * 解析模板引用
     */
    private TemplateReference parseTemplateReference(String templateRef) {
        String[] parts = templateRef.trim().split(":");
        if (parts.length >= 2) {
            return new TemplateReference(parts[1], parts.length > 2 ? parts[2] : null);
        }
        throw new IllegalArgumentException("Invalid template reference format: " + templateRef);
    }

    /**
     * 构建模板上下文
     */
    private DataxTemplateContext buildTemplateContext(ParametersNode parametersNode) {
        DataxTemplateContext context = new DataxTemplateContext();

        // 从参数节点获取配置信息
        if (parametersNode != null && parametersNode.getDefinedParams() != null) {
            Map<String, Object> definedParams = parametersNode.getDefinedParams();

            // 映射常用参数
            setIfPresent(definedParams, "sourceUsername", context::setSourceUsername);
            setIfPresent(definedParams, "sourcePassword", context::setSourcePassword);
            setIfPresent(definedParams, "sourceJdbcUrl", context::setSourceJdbcUrl);
            setIfPresent(definedParams, "sourceTable", context::setSourceTable);

            setIfPresent(definedParams, "targetUsername", context::setTargetUsername);
            setIfPresent(definedParams, "targetPassword", context::setTargetPassword);
            setIfPresent(definedParams, "targetJdbcUrl", context::setTargetJdbcUrl);
            setIfPresent(definedParams, "targetTable", context::setTargetTable);

            // 数值参数
            setIntIfPresent(definedParams, "channel", context::setChannel);
            setIntIfPresent(definedParams, "batchSize", context::setBatchSize);
            setIntIfPresent(definedParams, "byteSpeed", context::setByteSpeed);
            setIntIfPresent(definedParams, "recordSpeed", context::setRecordSpeed);

            // 字符串参数
            setIfPresent(definedParams, "whereCondition", context::setWhereCondition);
            setIfPresent(definedParams, "splitPk", context::setSplitPk);

            // 列表参数
            setListIfPresent(definedParams, "columns", context::setColumns);
            setListIfPresent(definedParams, "preSql", context::setPreSql);
            setListIfPresent(definedParams, "postSql", context::setPostSql);

            // 其他自定义参数
            definedParams.forEach((key, value) -> {
                if (!isStandardParameter(key)) {
                    context.addCustomParameter(key, value);
                }
            });
        }

        return context;
    }

    // 辅助方法
    private void setIfPresent(Map<String, Object> params, String key, Consumer<String> setter) {
        if (params.containsKey(key) && params.get(key) != null) {
            setter.accept(params.get(key).toString());
        }
    }

    private void setIntIfPresent(Map<String, Object> params, String key, IntConsumer setter) {
        if (params.containsKey(key)) {
            try {
                Object value = params.get(key);
                if (value instanceof Number) {
                    setter.accept(((Number) value).intValue());
                } else if (value instanceof String) {
                    setter.accept(Integer.parseInt((String) value));
                }
            } catch (NumberFormatException e) {
                // 忽略无效的数字格式
            }
        }
    }

    private void setListIfPresent(Map<String, Object> params, String key, Consumer<List<String>> setter) {
        if (params.containsKey(key)) {
            Object value = params.get(key);
            if (value instanceof List) {
                List<String> stringList = new ArrayList<>();
                ((List<?>) value).forEach(item -> stringList.add(item.toString()));
                setter.accept(stringList);
            } else if (value instanceof String) {
                // 简单处理逗号分隔的字符串
                List<String> list = Arrays.asList(((String) value).split(","));
                setter.accept(list.stream().map(String::trim).collect(Collectors.toList()));
            }
        }
    }

    private boolean isStandardParameter(String key) {
        Set<String> standardParams = Set.of(
            "sourceUsername", "sourcePassword", "sourceJdbcUrl", "sourceTable",
            "targetUsername", "targetPassword", "targetJdbcUrl", "targetTable",
            "channel", "batchSize", "byteSpeed", "recordSpeed",
            "whereCondition", "splitPk", "columns", "preSql", "postSql"
        );
        return standardParams.contains(key);
    }

    /**
     * 构建传统参数映射（向后兼容）
     */
    private Map<String, String> buildParametersMap(ParametersNode parametersNode) {
        Map<String, String> paramsMap = new HashMap<>();

        if (parametersNode != null && parametersNode.getDefinedParams() != null) {
            parametersNode.getDefinedParams().forEach((key, value) -> {
                if (value != null) {
                    paramsMap.put(key, value.toString());
                }
            });
        }

        return paramsMap;
    }

    // 模板引用内部类
    private static class TemplateReference {
        private String templateName;
        private String version;

        public TemplateReference(String templateName, String version) {
            this.templateName = templateName;
            this.version = version;
        }

        public String getTemplateName() { return templateName; }
        public String getVersion() { return version; }
    }
}
```

## 6. 使用示例

### 在 DolphinScheduler 中配置任务

```json
{
  "customConfig": 1,
  "json": "template:mysql-to-mysql",
  "dsType": "MYSQL",
  "dataSource": 1,
  "dtType": "MYSQL",
  "dataTarget": 2
}
```

### 任务参数配置

```
sourceUsername: admin
sourcePassword: password123
sourceJdbcUrl: jdbc:mysql://source-db:3306/source_db
sourceTable: users
targetUsername: target_user
targetPassword: target_pass
targetJdbcUrl: jdbc:mysql://target-db:3306/target_db
targetTable: users_backup
channel: 5
batchSize: 2048
whereCondition: "status = 'active'"
splitPk: "id"
columns: ["id", "name", "email", "status"]
preSql: ["DELETE FROM users_backup WHERE status = 'inactive'"]
```

## 7. 模板管理服务

```java
public class TemplateManagementService {

    private DataxTemplateEngine templateEngine;

    public TemplateManagementService() {
        this.templateEngine = new DataxTemplateEngine();
    }

    /**
     * 获取可用模板列表
     */
    public List<String> getAvailableTemplates() {
        // 可以从文件系统或数据库获取模板列表
        return Arrays.asList(
            "mysql-to-mysql",
            "hdfs-to-mysql",
            "mysql-to-hdfs",
            "oracle-to-mysql"
        );
    }

    /**
     * 预览模板渲染结果
     */
    public String previewTemplate(String templateName, DataxTemplateContext context) {
        if (!templateEngine.templateExists(templateName)) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        return templateEngine.renderTemplate(templateName, context);
    }

    /**
     * 验证模板参数
     */
    public List<String> validateTemplateParameters(String templateName, DataxTemplateContext context) {
        List<String> errors = new ArrayList<>();

        // 根据模板类型验证必需参数
        switch (templateName) {
            case "mysql-to-mysql":
                validateMysqlToMysqlParameters(context, errors);
                break;
            case "hdfs-to-mysql":
                validateHdfsToMysqlParameters(context, errors);
                break;
        }

        return errors;
    }

    private void validateMysqlToMysqlParameters(DataxTemplateContext context, List<String> errors) {
        if (context.getSourceUsername() == null || context.getSourceUsername().isEmpty()) {
            errors.add("Source username is required");
        }
        if (context.getSourceJdbcUrl() == null || context.getSourceJdbcUrl().isEmpty()) {
            errors.add("Source JDBC URL is required");
        }
        if (context.getSourceTable() == null || context.getSourceTable().isEmpty()) {
            errors.add("Source table is required");
        }
        // ... 其他验证
    }

    private void validateHdfsToMysqlParameters(DataxTemplateContext context, List<String> errors) {
        // HDFS特定验证
    }
}
```

## 总结

这种基于模板引擎的方案优势：

1. **保持灵活性**：支持任意复杂的 JSON 结构
2. **增强可维护性**：模板与参数分离
3. **类型安全**：通过上下文对象提供类型检查
4. **向后兼容**：支持传统占位符方式
5. **可扩展性**：易于添加新的模板类型
6. **用户体验**：提供模板预览和验证功能

通过这种方式，DolphinScheduler 可以在保持 DataX 脚本灵活性的同时，提供更好的配置管理体验。
