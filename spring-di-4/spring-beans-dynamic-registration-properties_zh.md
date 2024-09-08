# [根据属性动态注册 Spring Bean](https://www.baeldung.com/spring-beans-dynamic-registration-properties)

1. 概述

    在本教程中，我们将探讨如何根据自定义属性动态注册 [Bean](https://www.baeldung.com/spring-bean)。我们将探索 BeanDefinitionRegistryPostProcessor 接口，以及如何使用它将 Bean 添加到应用程序上下文中。

2. 代码设置

    让我们从创建一个简单的 Spring Boot 应用程序开始。

    首先，我们将定义一个要动态注册的 Bean。然后，我们将提供一个属性来决定如何注册 Bean。最后，我们将定义一个配置类，该类将根据我们的自定义属性注册 Bean。

    1. 依赖关系

        首先，让我们添加 Maven 依赖项：

        ```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>3.3.2</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <version>3.3.2</version>
            <scope>test</scope>
        </dependency>
        ```

        我们需要添加 spring-boot-starter 和 spring-boot-starter-test 依赖项。

    2. Bean类

        接下来，我们定义一个要根据自定义应用程序属性注册的 API 客户端：

        ```java
        public class ApiClient {
            private String name;
            private String url;
            private String key;
            // standard getters, setters and constructors
            public String getConnectionProperties() {
                return "Connecting to " + name + " at " + url;     
            }
        }
        ```

        假设我们想根据提供的属性使用此 bean 连接到不同的 API。我们不想为每个 API 创建类定义。相反，我们希望为每个 API 定义属性并动态注册 bean。

        我们不应该用 [@Component 或 @Service](https://www.baeldung.com/spring-component-repository-service) 来注解 ApiClient 类，因为我们不想使用组件扫描将其注册为 bean。

    3. 属性

        让我们添加一个属性来确定 Bean 应注册哪些 API。我们将在 application.yml 文件中定义该属性：

        ```yml
        api:
        clients:
            - name: example  
            url: https://api.example.com
            key: 12345
            - name: anotherexample
            url: https://api.anotherexample.com
            key: 67890
        ```

        在这里，我们定义了两个客户端及其各自的属性。我们将在注册 Bean 时使用这些属性。

3. 动态注册 Bean

    Spring 提供了一种使用 BeanDefinitionRegistryPostProcessor 接口动态注册 Bean 的方法。该接口允许我们在注释的 Bean 定义注册完成后添加或修改 Bean 定义。由于它发生在 Bean 实例化之前，因此 Bean 是在应用程序上下文完全初始化之前注册的。

    1. BeanDefinitionRegistryPostProcessor

        让我们定义一个配置类，它将根据自定义属性注册 ApiClient Bean：

        ```java
        public class ApiClientConfiguration implements BeanDefinitionRegistryPostProcessor {
            private static final String API_CLIENT_BEAN_NAME = "apiClient_";
            List<ApiClient> clients;

            public ApiClientConfiguration(Environment environment) {
                Binder binder = Binder.get(environment);
                List<HashMap> properties = binder.bind("api.clients", Bindable.listOf(HashMap.class)).get();
                clients = properties.stream().map(client -> new ApiClient(String.valueOf(client.get("name")),
                        String.valueOf(client.get("url")), String.valueOf(client.get("key")))).toList();
            }    

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

                clients.forEach(client -> {
                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ApiClient.class);
                    builder.addPropertyValue("name", client.getName());
                    builder.addPropertyValue("url", client.getUrl());
                    builder.addPropertyValue("key", client.getkey());
                    registry.registerBeanDefinition(API_CLIENT_BEAN_NAME + client.getName(), builder.getBeanDefinition());
                });
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            }
        }
        ```

        在这里，我们实现了 BeanDefinitionRegistryPostProcessor 接口。我们覆盖 postProcessBeanDefinitionRegistry 方法，该方法负责根据自定义属性注册 Bean。

        首先，我们定义一个常量 API_CLIENT_BEAN_NAME，它将用作 Bean 名称的前缀。在构造函数中，我们使用 Binder API 从环境对象中读取属性。然后，我们使用这些属性创建 ApiClient 对象。

        在执行 postProcessBeanDefinitionRegistry() 方法时，我们会遍历属性，并使用 BeanDefinitionRegistry 对象注册 ApiClient Bean。

        我们使用 BeanDefinitionBuilder 创建 Bean。它要求我们定义 Bean 类。然后，我们可以使用字段名逐一设置 Bean 属性。

        请注意，我们为每个 Bean 注册了一个唯一的名称 - API_CLIENT_BEAN_NAME + client.getName()。这将有助于我们从上下文中读取我们选择的 Bean。

    2. 主应用程序类

        最后，我们需要定义主应用程序类，并用 @SpringBootApplication 对其进行注解：

        ```java
        @SpringBootApplication
        public class RegistryPostProcessorApplication {

            public static void main(String[] args) {
                SpringApplication.run(RegistryPostProcessorApplication.class, args);
            }

            @Bean
            public ApiClientConfiguration apiClientConfiguration(ConfigurableEnvironment environment) {
                return new ApiClientConfiguration(environment);
            }
        }
        ```

        在此，我们定义 ApiClientConfiguration Bean，并将 ConfigurableEnvironment 对象传递给构造函数。这将有助于我们读取 ApiClientConfiguration 类中的属性。

4. 测试

    现在，Bean 已经注册完成，让我们来测试它们是否具有连接到 API 的正确属性。为了测试，我们将编写一个简单的测试类：

    ```java
    @SpringBootTest
    class ApiClientConfigurationTest {
        @Autowired
        private ApplicationContext context;

        @Test
        void givenBeansRegistered_whenConnect_thenConnected() {
            ApiClient exampleClient = (ApiClient) context.getBean("apiClient_example");
            Assertions.assertEquals("Connecting to example at https://api.example.com", exampleClient.getConnectionProperties());
            
            ApiClient anotherExampleClient = (ApiClient) context.getBean("apiClient_anotherexample");
            Assertions.assertEquals("Connecting to anotherexample at https://api.anotherexample.com", anotherExampleClient.getConnectionProperties());
        }
    }
    ```

    在这里，我们使用 @SpringBootTest 注解加载应用程序上下文。然后，我们使用 ApplicationContext 对象，使用 getBean() 方法从上下文中获取 Bean。getBean() 方法将唯一的 Bean 名称作为参数，并从上下文中返回 Bean。

    测试将检查 Bean 是否已正确注册并设置了正确的连接属性。

5. 总结

    在本教程中，我们探讨了如何使用 BeanDefinitionRegistryPostProcessor 接口根据自定义属性动态注册 Spring Bean。我们还编写了一个简单的测试用例，展示了如何从上下文中检索并使用 Bean。
