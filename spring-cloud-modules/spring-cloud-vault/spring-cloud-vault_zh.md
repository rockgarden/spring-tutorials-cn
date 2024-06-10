# [Spring Cloud Vault 简介](https://www.baeldung.com/spring-cloud-vault)

1. 概述

    在本教程中，我们将介绍如何在 Spring Boot 应用程序中使用 Hashicorp 的 Vault 来保护敏感的配置数据。

    在此，我们假定您对 Vault 有一定的了解，并且已经安装并运行了测试设置。如果不是这种情况，请先阅读我们的 [Vault 入门教程](https://www.baeldung.com/vault)，以便熟悉其基础知识。

2. Spring 云保险库

    Spring Cloud Vault 是 Spring Cloud 堆栈中相对较新的新增功能，它允许应用程序以透明的方式访问存储在 Vault 实例中的秘密。

    一般来说，向 Vault 迁移是一个非常简单的过程：只需在项目中添加所需的库和一些额外的配置属性就可以了。无需修改代码！

    之所以能做到这一点，是因为它在当前环境中作为高优先级的 PropertySource 注册。

    因此，只要需要属性，Spring 就会使用它。例如，数据源属性、配置属性等。

3. 在 Spring Boot 项目中添加 Spring Cloud Vault

    为了在基于 Maven 的 Spring Boot 项目中包含 spring-cloud-vault 库，我们使用了相关的启动器工件，它将提取所有需要的依赖项。

    除了主启动器，我们还将包含 spring-vault-config-databases，它增加了对动态数据库凭据的支持：

    ```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-vault-config</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-vault-config-databases</artifactId>
    </dependency>
    ```

    可从 Maven Central 下载最新版本的 Spring Cloud Vault 启动程序。

    1. 基本配置

        为了正常工作，Spring Cloud Vault 需要一种方法来确定从何处联系 Vault 服务器以及如何对其进行身份验证。

        为此，我们需要在 application.yml 或 application.properties 中提供必要的信息：

        ```yml
        spring:
        cloud:
            vault:
            uri: https://localhost:8200
            ssl:
                trust-store: classpath:/vault.jks
                trust-store-password: changeit
        config:
            import: vault://
        ```

        spring.cloud.vault.uri 属性指向 Vault 的 API 地址。由于我们的测试环境使用带有自签名证书的 HTTPS，因此还需要提供包含其公钥的密钥存储。如果在导入证书时遇到 “ ”验证证书失败：x509：证书依赖传统通用名称字段，请使用 SAN "这样的错误，可以使用下面的跳过证书验证检查选项来消除错误：

        `vault read -tls-skip-verify database/creds/fakebank-accounts-ro`

        请注意，该配置没有验证数据。对于使用固定令牌的最简单情况，我们可以通过系统属性 spring.cloud.vault.token 或环境变量来传递。这种方法与标准云配置机制（如 Kubernetes 的 ConfigMaps 或 Docker secrets）结合使用效果很好。

        Spring Vault 还需要为我们要在应用程序中使用的每种秘密类型进行额外配置。下文将介绍如何为两种常见的秘密类型添加支持：键/值和数据库凭证。

4. 使用通用密文后台

    我们使用通用秘密后端来访问存储在 Vault 中作为键值对的未版本化秘密。

    假设我们的类路径中已经有了 spring-cloud-starter-vault-config 依赖项，那么我们只需在 application.yml 文件中添加几个属性即可：

    ```yml
    spring:
    cloud:
        vault:
        # other vault properties omitted ...
        generic:
            enabled: true
            application-name: fakebank
    ```

    在这种情况下，application-name 属性是可选的。如果不指定，Spring 将使用标准的 spring.application.name 代替。

    现在，我们可以像使用其他环境属性一样使用存储在 secret/fakebank 中的所有键/值对。下面的代码段展示了我们如何读取存储在此路径下的 foo 密钥的值：

    ```java
    @Autowired Environment env;
    public String getFoo() {
        return env.getProperty("foo");
    }
    ```

    正如我们所见，代码本身对 Vault 一无所知，这是好事！我们仍然可以在本地测试中使用固定属性，只需在 application.yml 中启用一个属性，就可以随意切换到 Vault。

    1. 关于 Spring 配置文件的说明

        如果当前环境中存在可用的配置文件，Spring Cloud Vault 将使用可用的配置文件名称作为后缀附加到指定的基本路径中，并在该路径中搜索键/值对。

        它还会在可配置的默认应用路径（带或不带配置文件后缀）下查找属性，因此我们可以在单个位置共享秘密。请谨慎使用此功能！

        总之，如果假冒银行应用程序的生产配置文件处于活动状态，Spring Vault 将查找存储在以下路径下的属性：

        - secret/fakebank/production (higher priority)
        - secret/fakebank
        - secret/application/production
        - secret/application (lower priority)

        在上述列表中，application 是 Spring 用作秘密默认附加位置的名称。我们可以使用 spring.cloud.vault.generic.default-context 属性修改它。

        存储在最特定路径下的属性将优先于其他属性。例如，如果上述路径下有相同的属性 foo，那么优先顺序将是

5. 使用数据库秘密后台

    数据库后台模块允许 Spring 应用程序使用 Vault 创建的动态生成的数据库凭证。Spring Vault 会在标准的 spring.datasource.username 和 spring.datasource.password 属性下注入这些凭据，以便它们能被常规数据源选中。

    请注意，在使用该后端之前，我们必须在 Vault 中创建数据库配置和角色，如前一[教程](https://www.baeldung.com/vault)所述。

    为了在 Spring 应用程序中使用 Vault 生成的数据库凭据，项目的类路径中必须有 spring-cloud-vault-config-databases 以及相应的 JDBC 驱动程序。

    我们还需要在 application.yml 中添加几个属性，以便在应用程序中使用 Vault：

    ```yml
    spring:
    cloud:
        vault:
        # ... other properties omitted
        database:
            enabled: true
            role: fakebank-accounts-rw
    ```

    这里最重要的属性是角色属性，它包含存储在 Vault 中的数据库角色名称。在启动过程中，Spring 会与 Vault 联系，要求它创建具有相应权限的新凭证。

    默认情况下，Vault 会在配置的 “生存时间 ”之后撤销与这些凭证相关的权限。

    幸运的是，Spring Vault 会自动续订与获取的凭证相关的租约。这样，只要我们的应用程序还在运行，凭证就会一直有效。

    现在，让我们来看看这种集成的实际效果。以下代码段从 Spring 管理的 DataSource 获取一个新的数据库连接：

    `Connection c = datasource.getConnection();`

    我们可以再次看到，我们的代码中没有使用 Vault 的迹象。所有集成都发生在环境级，因此我们的代码可以像往常一样轻松地进行单元测试。

6. 总结

    在本教程中，我们展示了如何使用 Spring Vault 库将 Vault 与 Spring Boot 集成。我们介绍了两种常见用例：通用键/值对和动态数据库凭证。
