# [使用Jasypt的Spring Boot配置](https://www.baeldung.com/spring-boot-jasypt)

1. 介绍

    Jasypt（Java简化加密）Spring Boot提供用于在引导应用程序中加密属性源的实用程序。

    在本文中，我们将讨论如何添加[jasypt-spring-boot](https://github.com/ulisesbocchio/jasypt-spring-boot)的支持并使用它。

    有关使用Jasypt作为加密框架的更多信息，请查看我们的Jasypt简介。

2. 为什么是Jasypt？

    每当我们需要在配置文件中存储敏感信息时——这意味着我们基本上使这些信息变得脆弱；这包括任何类型的敏感信息，如凭据，但肯定不止这些。

    通过使用Jasypt，我们可以为属性文件属性提供加密，我们的应用程序将完成解密和检索原始值的工作。

3. 将JASYPT与Spring Boot一起使用的方法

    让我们来讨论一下将Jasypt与Spring Boot一起使用的不同方法。

    1. 使用jasypt-spring-boot-starter

        我们需要为我们的项目添加单个依赖项：

        ```xml
        <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>2.0.0</version>
        </dependency>
        ```

        Maven Central有最新版本的jasypt-spring-boot-starter。

        现在让我们用密钥“password”对文本“Password@1”进行加密，并将其添加到加密属性中：

        `encrypted.property=ENC(uTSqb9grs1+vUv3iN8lItC0kl65lMG+8)`

        让我们定义一个配置类AppConfigForJasyptStarter——将encrypted.properties文件指定为属性源：

        ```java
        @Configuration
        @PropertySource("encrypted.properties")
        public class AppConfigForJasyptStarter {
        }
        ```

        现在，我们将编写一个服务bean PropertyServiceForJasyptStarter，从crypted.properties中检索值。解密的值可以使用@Value注释或环境类的getProperty()方法检索：

        ```java
        @Service
        public class PropertyServiceForJasyptStarter {

            @Value("${encrypted.property}")
            private String property;

            public String getProperty() {
                return property;
            }

            public String getPasswordUsingEnvironment(Environment environment) {
                return environment.getProperty("encrypted.property");
            }
        }
        ```

        最后，使用上述服务类并设置我们用于加密的密钥，我们可以轻松检索解密的密码并在我们的应用程序中使用：

        ```java
        @Test
        public void whenDecryptedPasswordNeeded_GetFromService() {
            System.setProperty("jasypt.encryptor.password", "password");
            PropertyServiceForJasyptStarter service = appCtx
            .getBean(PropertyServiceForJasyptStarter.class);

            assertEquals("Password@1", service.getProperty());
        
            Environment environment = appCtx.getBean(Environment.class);
        
            assertEquals(
            "Password@1", 
            service.getPasswordUsingEnvironment(environment));
        }
        ```

    2. 使用jasypt-spring-boot

        对于不使用@SpringBootApplication或@EnableAutoConfiguration的项目，我们可以直接使用jasypt-spring-boot依赖项：

        ```xml
        <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>2.0.0</version>
        </dependency>
        ```

        同样，让我们用密钥“密码”加密文本“Password@2”，并将其添加到encryptedv2.properties中：

        `encryptedv2.property=ENC(dQWokHUXXFe+OqXRZYWu22BpXoRZ0Drt)`

        让我们为jasypt-spring-boot依赖性设置一个新的配置类。

        在这里，我们需要添加注释@EncryptablePropertySource：

        ```java
        @Configuration
        @EncryptablePropertySource("encryptedv2.properties")
        public class AppConfigForJasyptSimple {
        }
        ```

        此外，定义了一个新的PropertyServiceForJasyptSimple bean来返回encryptedv2.properties：

        ```java
        @Service
        public class PropertyServiceForJasyptSimple {

            @Value("${encryptedv2.property}")
            private String property;

            public String getProperty() {
                return property;
            }
        }
        ```

        最后，使用上述服务类并设置我们用于加密的密钥，我们可以轻松检索encryptedv2.property：

        ```java
        @Test
        public void whenDecryptedPasswordNeeded_GetFromService() {
            System.setProperty("jasypt.encryptor.password", "password");
            PropertyServiceForJasyptSimple service = appCtx
            .getBean(PropertyServiceForJasyptSimple.class);
        
            assertEquals("Password@2", service.getProperty());
        }
        ```

    3. 使用自定义JASYPT加密器

        第3.1节和第3.2节中定义的加密器是使用默认配置值构建的。

        然而，让我们去定义我们自己的Jasypt加密器，并尝试用于我们的应用程序。

        自定义加密器bean将看起来像：

        ```java
        @Bean(name = "encryptorBean")
        public StringEncryptor stringEncryptor() {
            PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPassword("password");
            config.setAlgorithm("PBEWithMD5AndDES");
            config.setKeyObtentionIterations("1000");
            config.setPoolSize("1");
            config.setProviderName("SunJCE");
            config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
            config.setStringOutputType("base64");
            encryptor.setConfig(config);
            return encryptor;
        }
        ```

        此外，我们可以修改SimpleStringPBEConfig的所有属性。

        此外，我们需要将属性“jasypt.encryptor.bean”添加到我们的application.properties中，以便Spring Boot知道应该使用哪个自定义加密器。

        例如，我们在应用程序中添加了使用密钥“password”加密的自定义文本“Password@3”。属性：

        ```properties
        jasypt.encryptor.bean=encryptorBean
        encryptedv3.property=ENC(askygdq8PHapYFnlX6WsTwZZOxWInq+i)
        ```

        一旦我们设置了它，我们就可以轻松地从Spring's Environment获取加密的v3.property：

        ```java
        @Test
        public void whenConfiguredExcryptorUsed_ReturnCustomEncryptor() {
            Environment environment = appCtx.getBean(Environment.class);
            assertEquals(
            "Password@3", 
            environment.getProperty("encryptedv3.property"));
        }
        ```

4. 结论

    通过使用Jasypt，我们可以为应用程序处理的数据提供额外的安全性。

    它使我们能够更多地关注应用程序的核心，如果需要，也可以用于提供自定义加密。
