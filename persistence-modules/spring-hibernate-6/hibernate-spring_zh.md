# [用 Spring 引导 Hibernate](https://www.baeldung.com/spring-boot-hibernate)

1. 一览表

    在本文中，我们将讨论如何使用Java和XML配置使用Spring引导Hibernate 6。

    本文聚焦于Spring MVC，介绍了如何在Spring Boot中使用Hibernate。

2. Spring整合

    使用原生Hibernate API引导SessionFactory有点复杂，需要我们相当多的代码（如果您需要这样做，请查看[官方文档](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html)）。

    幸运的是，Spring支持引导SessionFactory，因此我们只需要几行Java代码或XML配置。

3. Maven附属机构

    让我们首先将必要的依赖项添加到我们的pom.xml中：

    ```xml
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.5.2.Final</version>
    </dependency>
    ```

    spring-orm模块提供与Hibernate的Spring集成：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-orm</artifactId>
        <version>6.0.11</version>
    </dependency>
    ```

    为了简单起见，我们将使用H2作为我们的数据库：

    ```xml
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.1.214</version>
    </dependency>
    ```

    最后，我们将使用Tomcat JDBC连接池，它比Spring提供的DriverManagerDataSource更适合生产目的：

    ```xml
    <dependency>
        <groupId>org.apache.tomcat</groupId>
        <artifactId>tomcat-dbcp</artifactId>
        <version>9.0.80</version>
    </dependency>
    ```

4. 配置

    如前所述，Spring支持我们启动Hibernate SessionFactory。

    我们所要做的就是定义一些Bean和一些参数。

    有了Spring，我们有两个选项来进行这些配置：基于Java和基于XML。

    1. 使用Java配置

        要将Hibernate 6与Spring一起使用，我们必须为LocalSessionFactoryBean、DataSource、PlatformTransactionManager和一些Hibernate特定属性定义bean。

        让我们创建我们的HibernateConfig类来配置带有Spring的Hibernate 6：

        ```java
        @Configuration
        @EnableTransactionManagement
        public class HibernateConf {

            @Bean
            public LocalSessionFactoryBean sessionFactory() {
                LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
                sessionFactory.setDataSource(dataSource());
                sessionFactory.setPackagesToScan(
                {"com.baeldung.hibernate.bootstrap.model" });
                sessionFactory.setHibernateProperties(hibernateProperties());

                return sessionFactory;
            }

            @Bean
            public DataSource dataSource() {
                BasicDataSource dataSource = new BasicDataSource();
                dataSource.setDriverClassName("org.h2.Driver");
                dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
                dataSource.setUsername("sa");
                dataSource.setPassword("sa");

                return dataSource;
            }

            @Bean
            public PlatformTransactionManager hibernateTransactionManager() {
                HibernateTransactionManager transactionManager
                = new HibernateTransactionManager();
                transactionManager.setSessionFactory(sessionFactory().getObject());
                return transactionManager;
            }

            private final Properties hibernateProperties() {
                Properties hibernateProperties = new Properties();
                hibernateProperties.setProperty(
                "hibernate.hbm2ddl.auto", "create-drop");
                hibernateProperties.setProperty(
                "hibernate.dialect", "org.hibernate.dialect.H2Dialect");

                return hibernateProperties;
            }
        }
        ```

    2. 使用XML配置

        作为次要选项，我们还可以使用基于XML的配置配置Hibernate 6：

        ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <beans xmlns="...">

            <bean id="sessionFactory" 
            class="org.springframework.orm.hibernate5.LocalSessionFactoryBean">
                <property name="dataSource" 
                ref="dataSource"/>
                <property name="packagesToScan" 
                value="com.baeldung.hibernate.bootstrap.model"/>
                <property name="hibernateProperties">
                    <props>
                        <prop key="hibernate.hbm2ddl.auto">
                            create-drop
                        </prop>
                        <prop key="hibernate.dialect">
                            org.hibernate.dialect.H2Dialect
                        </prop>
                    </props>
                </property>
            </bean>

            <bean id="dataSource" 
            class="org.apache.tomcat.dbcp.dbcp2.BasicDataSource">
                <property name="driverClassName" value="org.h2.Driver"/>
                <property name="url" value="jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"/>
                <property name="username" value="sa"/>
                <property name="password" value="sa"/>
            </bean>

            <bean id="txManager" 
            class="org.springframework.orm.hibernate5.HibernateTransactionManager">
                <property name="sessionFactory" ref="sessionFactory"/>
            </bean>
        </beans>
        ```

        正如我们很容易看到的，我们正在定义与之前基于Java的配置完全相同的bean和参数。

        要将XML引导到Spring上下文中，如果应用程序配置了Java配置，我们可以使用一个简单的Java配置文件：

        ```java
        @Configuration
        @EnableTransactionManagement
        @ImportResource({"classpath:hibernate6Configuration.xml"})
        public class HibernateXMLConf {
            //
        }
        ```

        或者，如果整体配置纯为XML，我们可以简单地将XML文件提供给Spring Context。

5. 用法

    此时，Hibernate 5已完全配置为Spring，我们可以在需要时直接注入原始Hibernate SessionFactory：

    ```java
    public abstract class BarHibernateDAO {

        @Autowired
        private SessionFactory sessionFactory;

        // ...
    }
    ```

6. 支持的数据库

    不幸的是，Hibernate项目并没有提供受支持数据库的官方列表。

    话虽如此，很容易看出是否支持特定的数据库类型；我们可以查看[支持的方言列表](https://docs.jboss.org/hibernate/orm/6.1/userguide/html_single/Hibernate_User_Guide.html#database)。

7. 结论

    在这个快速教程中，我们用Hibernate 6配置了Spring——同时配置了Java和XML。
