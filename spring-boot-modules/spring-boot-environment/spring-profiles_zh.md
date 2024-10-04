# [Spring概况](https://www.baeldung.com/spring-profiles)

1. 一览表

    在本教程中，我们将专注于在春季介绍配置文件。

    配置文件是框架的核心功能——允许我们将bean映射到不同的配置文件——例如，dev、test和prod。

    然后，我们可以在不同的环境中激活不同的配置文件，只引导我们需要的Bean。

2. 在Bean上使用@Profile

    让我们从简单开始，看看我们如何让Bean属于特定配置文件。我们使用@Profile注释——我们将bean映射到该特定配置文件；注释仅使用一个（或多个）配置文件的名称。

    考虑一个基本场景：我们有一个Bean，它只应在开发期间处于活动状态，而不应在生产中部署。

    我们用dev配置文件注释该bean，它只会在开发期间出现在容器中。在生产中，开发人员根本不会活跃：

    ```java
    @Component
    @Profile("dev")
    public class DevDatasourceConfig
    ```

    作为简短的旁注，配置文件名称也可以以NOT运算符作为前缀，例如，!dev，将他们排除在个人资料之外。

    在示例中，只有当开发配置文件未激活时，组件才会被激活：

    ```java
    @Component
    @Profile("!dev")
    public class DevDatasourceConfig
    ```

3. 在XML中声明配置文件

    配置文件也可以在XML中配置。`<beans>`标签有一个配置文件属性，该属性接受适用配置文件的逗号分隔值：

    ```xml
    <beans profile="dev">
        <bean id="devDatasourceConfig"
        class="org.baeldung.profiles.DevDatasourceConfig" />
    </beans>
    ```

4. 设置配置文件

    下一步是激活和设置配置文件，以便将各自的Bean注册在容器中。

    这可以通过多种方式完成，我们将在以下章节中探讨。

    1. 通过WebApplicationInitializer接口进行编程

        在Web应用程序中，WebApplicationInitializer可用于以编程方式配置ServletContext。

        这也是以编程方式设置我们的活动配置文件的非常方便的地方：

        ```java
        @Configuration
        public class MyWebApplicationInitializer 
        implements WebApplicationInitializer {

            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
        
                servletContext.setInitParameter(
                "spring.profiles.active", "dev");
            }
        }
        ```

    2. 通过可配置环境进行编程

        我们也可以直接在环境中设置配置文件：

        ```java
        @Autowired
        private ConfigurableEnvironment env;
        ...
        env.setActiveProfiles("someProfile");
        ```

    3. web.xml中的上下文参数

        同样，我们可以使用上下文参数在Web应用程序的web.xml文件中定义活动配置文件：

        ```xml
        <context-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>/WEB-INF/app-config.xml</param-value>
        </context-param>
        <context-param>
            <param-name>spring.profiles.active</param-name>
            <param-value>dev</param-value>
        </context-param>
        ```

    4. JVM系统参数

        配置文件名称也可以通过JVM系统参数传递。这些配置文件将在应用程序启动期间被激活：

        `-Dspring.profiles.active=dev`

    5. 环境变量

        在Unix环境中，配置文件也可以通过环境变量激活：

        `export spring_profiles_active=dev`

    6. Maven简介

        Spring配置文件也可以通过指定spring.profiles.active配置属性通过Maven配置文件激活。

        在每个Maven配置文件中，我们可以设置spring.profiles.active属性：

        ```xml
        <profiles>
            <profile>
                <id>dev</id>
                <activation>
                    <activeByDefault>true</activeByDefault>
                </activation>
                <properties>
                    <spring.profiles.active>dev</spring.profiles.active>
                </properties>
            </profile>
            <profile>
                <id>prod</id>
                <properties>
                    <spring.profiles.active>prod</spring.profiles.active>
                </properties>
            </profile>
        </profiles>
        ```

        其值将用于替换application.properties中的@spring.profiles.active@占位符：

        `spring.profiles.active=@spring.profiles.active@`

        现在我们需要在pom.xml中启用资源过滤：

        ```xml
        <build>
            <resources>
                <resource>
                    <directory>src/main/resources</directory>
                    <filtering>true</filtering>
                </resource>
            </resources>
            ...
        </build>
        ```

        并附加一个-P参数来切换将应用哪个Maven配置文件：

        `mvn clean package -Pprod`

        此命令将为prod配置文件打包应用程序。当该应用程序运行时，它还会为该应用程序应用spring.profiles.active值prod。

    7. 测试中的@ActiveProfile

        测试使使用@ActiveProfile注释来启用特定配置文件来指定哪些配置文件处于活动状态变得非常容易：

        `@ActiveProfiles("dev")`

        到目前为止，我们已经研究了多种激活配置文件的方法。现在让我们看看哪个优先于另一个，如果我们使用多个，从最高优先级到最低优先级会发生什么：

        - web.xml中的上下文参数
        - Web应用程序初始化器
        - JVM系统参数
        - 环境变量
        - Maven简介

5. 默认配置文件

    任何不指定配置文件的bean都属于默认配置文件。

    Spring还提供了一种在没有其他配置文件处于活动状态时设置默认配置文件的方法——通过使用spring.profiles.default属性。

6. 获取活跃的个人资料

    Spring的活动配置文件驱动 @Profile 注释的行为，以 enabling/disabling bean。然而，我们可能也希望以编程方式访问活动配置文件列表。

    我们有两种方法可以做到这一点，使用环境或spring.profiles.active。

    1. 使用环境

        我们可以通过注入环境对象来访问活动配置文件：

        ```java
        public class ProfileManager {
            @Autowired
            private Environment environment;

            public void getActiveProfiles() {
                for (String profileName : environment.getActiveProfiles()) {
                    System.out.println("Currently active profile - " + profileName);
                }  
            }
        }
        ```

    2. 使用spring.profiles.active

        或者，我们可以通过注入属性spring.profiles.active来访问配置文件：

        ```java
        @Value("${spring.profiles.active}")
        private String activeProfile;
        ```

        在这里，我们的activeProfile变量将包含当前活跃的配置文件的名称，如果有多个，它将包含其名称，用逗号分隔。

        然而，我们应该考虑如果根本没有主动配置文件会发生什么。使用我们上面的代码，没有活动配置文件将阻止应用程序上下文的创建。由于缺少注入变量的占位符，这将导致非法参数异常。

        为了避免这种情况，我们可以定义一个默认值：

        ```java
        @Value("${spring.profiles.active:}")
        private String activeProfile;
        ```

        现在，如果没有配置文件处于活动状态，我们的activeProfile将只包含一个空字符串。

        如果我们想像上一个例子一样访问它们的列表，我们可以通过拆分activeProfile变量来完成：

        ```java
        public class ProfileManager {
            @Value("${spring.profiles.active:}")
            private String activeProfiles;

            public String getActiveProfiles() {
                for (String profileName : activeProfiles.split(",")) {
                    System.out.println("Currently active profile - " + profileName);
                }
            }
        }
        ```

7. 示例：使用配置文件分离数据源配置

    既然基础知识已经不在了，让我们来看看一个真实的例子。

    考虑一个场景，即我们必须维护开发和生产环境的数据源配置。

    让我们创建一个通用的接口DatasourceConfig，该接口需要由两个数据源实现实现：

    ```java
    public interface DatasourceConfig {
        public void setup();
    }
    ```

    以下是开发环境的配置：

    ```java
    @Component
    @Profile("dev")
    public class DevDatasourceConfig implements DatasourceConfig {
        @Override
        public void setup() {
            System.out.println("Setting up datasource for DEV environment. ");
        }
    }
    ```

    以及生产环境的配置：

    ```java
    @Component
    @Profile("production")
    public class ProductionDatasourceConfig implements DatasourceConfig {
        @Override
        public void setup() {
        System.out.println("Setting up datasource for PRODUCTION environment. ");
        }
    }
    ```

    现在让我们创建一个测试并注入我们的DatasourceConfig接口；根据活动配置文件，Spring将注入DevDatasourceConfig或ProductionDatasourceConfig bean：

    ```java
    public class SpringProfilesWithMavenPropertiesIntegrationTest {
        @Autowired
        DatasourceConfig datasourceConfig;

        public void setupDatasource() {
            datasourceConfig.setup();
        }
    }
    ```

    当dev配置文件处于活动状态时，Spring会注入DevDatasourceConfig对象，当调用setup（）方法时，输出如下：

    `Setting up datasource for DEV environment.`

8. Spring Boot中的配置文件

    Spring Boot支持迄今为止概述的所有配置文件配置，并有一些附加功能。

    1. 激活或设置配置文件

        第4节中介绍的初始化参数spring.profiles.active也可以在Spring Boot中设置为属性，以定义当前活动配置文件。这是Spring Boot将自动拾取的标准属性：

        `spring.profiles.active=dev`

        然而，从Spring Boot 2.4开始，此属性不能与spring.config.activate.on-profile一起使用，因为这可能会引发ConfigDataException（即InvalidConfigDataPropertyException或InactiveConfigDataAccessException）。

        要以编程方式设置配置文件，我们还可以使用SpringApplication类：

        `SpringApplication.setAdditionalProfiles("dev");`

        要在Spring Boot中使用Maven设置配置文件，我们可以在spring-boot-maven-plugin inpom.xml下指定配置文件名称：

        ```xml
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <profiles>
                        <profile>dev</profile>
                    </profiles>
                </configuration>
            </plugin>
            ...
        </plugins>
        ```

        并执行Spring Boot特定的Maven目标：

        `mvn spring-boot:run`

    2. 特定于配置文件的属性文件

        然而，Spring Boot带来的最重要的配置文件相关功能是特定于配置文件的属性文件。这些必须以application-{profile}.properties格式命名。

        Spring Boot将自动加载所有配置文件的application.properties文件中的属性，以及仅针对指定配置文件的配置文件特定.properties文件中的属性。

        例如，我们可以使用两个名为application-dev.properties和application-production.properties的文件为dev和production配置文件配置不同的数据源：

        在application-production.properties文件中，我们可以设置MySql数据源：

        ```properties
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        spring.datasource.url=jdbc:mysql://localhost:3306/db
        spring.datasource.username=root
        spring.datasource.password=root
        ```

        然后，我们可以在application-dev.properties文件中为dev配置文件配置相同的属性，以使用内存H2数据库：

        ```properties
        spring.datasource.driver-class-name=org.h2.Driver
        spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
        spring.datasource.username=sa
        spring.datasource.password=sa
        ```

        通过这种方式，我们可以轻松地为不同的环境提供不同的配置。

        在Spring Boot 2.4之前，可以从配置文件特定文档中激活配置文件。但情况不再是这样了；在更高版本中，在这种情况下，框架将再次抛出InvalidConfigDataPropertyException或InactiveConfigDataAccessException。

    3. 多文档文件

        为了进一步简化独立环境的属性定义，我们甚至可以将所有属性放在同一个文件中，并使用分隔符来指示配置文件。

        从2.4版本开始，除了之前支持的[YAML](https://www.baeldung.com/spring-yaml)外，Spring Boot还扩展了对属性文件的多文档文件的支持。所以现在，我们可以在sameapplication.properties中指定开发和生产属性：

        ```properties
        my.prop=used-always-in-all-profiles
        #---
        spring.config.activate.on-profile=dev
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        spring.datasource.url=jdbc:mysql://localhost:3306/db
        spring.datasource.username=root
        spring.datasource.password=root
        #---
        spring.config.activate.on-profile=production
        spring.datasource.driver-class-name=org.h2.Driver
        spring.datasource.url=jdbc:h2:mem:db;DB_CLOSE_DELAY=-1
        spring.datasource.username=sa
        spring.datasource.password=sa
        ```

        Spring Boot按自上而下的顺序读取此文件。也就是说，如果一些属性，例如my.prop，在上述示例的末尾再次出现，则将考虑最最终值。

    4. 个人资料组

        Boot 2.4中添加的另一个功能是配置文件组。顾名思义，它允许我们将相似的配置文件分组在一起。

        让我们考虑一个用例，即我们将为生产环境提供多个配置配置文件。比方说，aproddb用于数据库，prodquartz用于生产环境中的调度器。

        为了通过我们的application.properties文件一次性启用这些配置文件，我们可以指定：

        `spring.profiles.group.production=proddb,prodquartz`

        因此，激活生产配置文件也会激活proddb和prodquartz。

9. 结论

    在本文中，我们讨论了如何在bean上定义配置文件，以及如何在我们的应用程序中启用正确的配置文件。

    最后，我们用一个简单但真实的例子验证了我们对配置文件的理解。
