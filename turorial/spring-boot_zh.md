# [学习Spring Boot](https://www.baeldung.com/spring-boot)

Last updated: February 15, 2024

[Spring Boot](https://spring.io/projects/spring-boot)是Spring平台的一个有主见的、容易上手的补充--对于以最小的努力创建独立的、生产级的应用程序非常有用。

在这个系列中，我们将首先介绍Spring Boot的基础知识。读者将学习如何入门，Spring Boot与Spring有什么不同，如何定制和测试应用程序。

然后，我们将介绍一些选定的高级主题，如持久性、DevOps工具，以及其他一些对Spring Boot的入门有帮助的主题。

## Spring Boot - 基础知识

- [Spring Boot教程--Bootstrap一个简单的应用程序](/spring-boot-modules/spring-boot-bootstrap/spring-boot-start_zh.md)
- [Spring和Spring Boot之间的比较](/spring-boot-modules/spring-vs-spring-boot_zh.md)
  - [从Spring迁移到Spring Boot](/spring-boot-modules/spring-boot-migration_zh.md)
- [Spring Boot注解](/spring-boot-modules/spring-boot-annotations/spring-boot-annotations_zh.md)
- [Spring Boot启动器介绍](/spring-boot-modules/spring-boot-artifacts-2/spring-boot-starters_zh.md)
- [推荐的Spring Boot项目的包结构](spring-boot-package-structure_zh.md)
- [Spring Boot执行器](../spring-reactive-modules/spring-reactive-3/spring-boot-actuators_zh.md)
- [配置一个Spring Boot Web应用程序](/spring-boot-modules](https://www.baeldung.com/spring-boot-bean-validation)/spring-boot-mvc-4/spring-boot-application-configuration_zh.md)
- [使用Spring Boot加载初始数据的快速指南](../persistence-modules/spring-boot-persistence/README-zh.md#用spring-boot加载初始数据的快速指南)
- [Spring Boot 中的验证](/spring-boot-modules/spring-boot-validation/spring-boot-bean-validation_zh.md)
- [将应用程序从 Spring Boot 2 迁移到 Spring Boot 3](/spring-boot-modules/spring-boot-3/spring-boot-3-migration_zh.md)

## Spring Boot - 属性

- [使用Spring和Spring Boot的属性](/spring-boot-modules/spring-boot-properties/README-zh.md#spring和spring-boot的属性)
- [Spring Boot中的@ConfigurationProperties指南](/spring-boot-modules/spring-boot-properties/README-zh.md)
- [使用Spring Boot的自动属性扩展](/spring-boot-modules/spring-boot-property-exp/README-zh.md)
- [在Spring Boot中使用application.yml与application.properties](/spring-boot-modules/spring-boot-properties-3/README-zh.md#在spring-boot中使用applicationyml与applicationproperties)
- [在 Spring Boot 中将 YAML 转换为对象列表](/spring-boot-modules/spring-boot-properties-2/README-zh.md#在spring-boot中将yaml转为对象列表)
- [在 Spring Boot 属性文件中使用环境变量](/spring-boot-modules/spring-boot-properties-4/spring-boot-properties-env-variables_zh.md)

Spring Boot - 自定义

- [如何定义Spring Boot过滤器？](/spring-boot-modules/spring-boot-basic-customization/spring-boot-add-filter_zh.md)
- [如何改变Spring Boot中的默认端口](/spring-boot-modules/spring-boot-basic-customization/spring-boot-change-port_zh.md)
- [Spring Boot改变上下文路径](/spring-boot-modules/spring-boot-context-path_zh.md)
- [Spring Boot - 自定义白标错误页面](/spring-boot-modules/spring-boot-basic-customization/spring-boot-custom-error-page_zh.md)
- [在Spring Boot中使用自定义横幅](/spring-boot-modules/spring-boot-basic-customization/spring-boot-custom-banners_zh.md)
- [Spring Boot：自定义Jackson ObjectMapper](/spring-boot-modules/spring-boot-data/spring-boot-customize-jackson-objectmapper_zh.md)

Spring Boot - 测试

- [在Spring Boot中测试](/spring-boot-modules/spring-boot-simple/spring-boot-testing_zh.md)
- [探索Spring Boot的TestRestTemplate](/spring-web-modules/spring-resttemplate/spring-boot-testresttemplate_zh.md)
- [Spring Boot中的@RestClientTest快速指南](/spring-boot-modules/spring-boot-client/restclienttest-in-spring-boot_zh.md)
- [Spring Boot中的日志记录](/spring-boot-modules/spring-boot-simple/spring-boot-logging_zh.md)
- [在 Spring Boot 测试中使用 @Autowired 和 @InjectMocks](/testing-modules/spring-mockito/spring-test-autowired-injectmocks_zh.md)
- [在 Spring Boot 中设置测试时的日志级别](/spring-boot-modules/spring-boot-testing-2/spring-boot-testing-log-level_zh.md)
- [在 Spring Boot 测试中排除自动配置类](/spring-boot-modules/spring-boot-testing/spring-boot-exclude-auto-configuration-test_zh.md)
- [Spring Boot 集成测试的 Spring 安全性](/spring-boot-modules/spring-boot-security/spring-security-integration-tests_zh.md)

Spring Boot - 引擎盖下

- [用Spring Boot创建一个自定义启动器](/spring-boot-modules/spring-boot-custom-starter/spring-boot-custom-starter_zh.md)
- [用Spring Boot创建一个自定义的自动配置程序](/spring-boot-modules/spring-boot-autoconfiguration/spring-boot-custom-auto-configuration_zh.md)
- [在Spring Boot中显示自动配置报告](/spring-boot-modules/spring-boot-autoconfiguration/spring-boot-auto-configuration-report_zh.md)
- [如何获得所有Spring管理的Bean？](/spring-boot-modules/spring-boot-di/spring-show-all-beans_zh.md)
- [Spring Boot安全自动配置](/spring-boot-modules/spring-boot-security/spring-boot-security-autoconfiguration_zh.md)
- [Spring组件扫描](/spring-boot-modules/spring-boot-di/spring-component-scanning_zh.md)

Spring Boot - 持久性

- [使用Spring Boot加载初始数据的快速指南](/persistence-modules/spring-boot-persistence/spring-boot-data-sql-and-schema-sql_zh.md)
- [带有多个SQL导入文件的Spring Boot](/persistence-modules/spring-boot-persistence/spring-boot-sql-import-files_zh.md)
- ~~从Spring Boot显示Hibernate/JPA的SQL语句~~
- [使用H2数据库的Spring Boot](/persistence-modules/spring-boot-persistence-h2/spring-boot-h2-database_zh.md)
- [在 Spring Boot 中配置和使用多个数据源](/persistence-modules/spring-data-jdbc/spring-boot-configure-multiple-datasources_zh.md)
- ~~禁用 Spring 数据自动配置~~
- [使用 Spring Boot 配置 Hikari 连接池](/persistence-modules/spring-boot-persistence-5/spring-boot-hikari_zh.md)

Spring Boot - DevOps工具

- [用Minikube运行Spring Boot应用程序](/spring-cloud-modules/spring-cloud-kubernetes/spring-boot-minikube_zh.md)
- [构建Spring Boot应用的Docker化](/spring-cloud-modules/spring-cloud-docker/dockerizing-spring-boot-application_zh.md)
- [用Spring Boot创建Docker镜像](/docker-modules/docker-spring-boot/spring-boot-docker-images_zh.md)
- [将Spring Boot WAR部署到Tomcat服务器上](/spring-boot-modules/spring-boot-deployment/spring-boot-war-tomcat-deploy_zh.md)
- [指南：Spring Boot管理](/spring-boot-modules/spring-boot-admin/spring-boot-admin_zh.md)
- [Spring-Boot开发工具概述](/spring-boot-modules/spring-boot-devtools_zh.md)
- [Spring Boot CLI简介](/spring-boot-modules/spring-boot-cli/spring-boot-cli_zh.md)
- [Spring Boot应用程序作为一种服务](/spring-boot-modules/spring-boot-app-as-a-service_zh.md)
- [Spring Boot Gradle 插件](/spring-boot-modules/spring-boot-gradle-plugin_zh.md)
- [将Spring Boot应用部署到Azure](/azure/spring-boot-azure_zh.md)
- [在 Spring Boot 应用程序中设置 OpenTelemetry](/spring-cloud-modules/spring-cloud-open-telemetry/spring-boot-opentelemetry-setup_zh.md)
- [使用 Spring Boot 实现可观察性](/spring-boot-modules/spring-boot-3-observation/spring-boot-3-observability_zh.md)
- [使用 Bucket4j 对 Spring API 进行速率限制](/spring-boot-modules/spring-boot-libraries/spring-bucket4j_zh.md)

Spring Boot - 与其他库集成

- [在Spring Boot中使用Keycloak的快速指南](/spring-boot-modules/spring-boot-keycloak/spring-boot-keycloak_zh.md)
- [使用Spring Boot的Mustache指南](/mustache/spring-boot-mustache_zh.md)
- [GraphQL和Spring Boot入门](/spring-boot-modules/spring-boot-graphql/spring-graphql_zh.md)
- [使用Spring Boot的Apache Camel](/messaging-modules/spring-apache-camel/apache-camel-spring-boot_zh.md)
- [在Spring Boot应用中使用Spring Data的DynamoDB](/persistence-modules/spring-data-dynamodb/spring-data-dynamodb_zh.md)
- [使用Jasypt的Spring Boot配置](/spring-boot-modules/spring-boot-jasypt/spring-boot-jasypt_zh.md)
