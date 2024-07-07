# Spring MyBatis

## MyBatis与Spring

1. 简介

    MyBatis是最常用的开源框架之一，用于在Java应用程序中实现SQL数据库访问。

    在这个快速教程中，我们将介绍如何将MyBatis与Spring和Spring Boot集成。

    对于那些还不熟悉这个框架的人，请务必查看我们关于[使用MyBatis的文章](https://www.baeldung.com/mybatis)。

2. 定义模型

    让我们从定义简单的POJO开始，我们将在文章中使用：

    mybatis.spring/Article.java

    以及一个等效的SQL文件-schema.sql：

    接下来，让我们创建一个data.sql文件，它只是向我们的文章表插入一条记录。

    两个SQL文件都必须包含在classpath（resources/）中。

3. Spring配置

    为了开始使用MyBatis，我们必须包含两个主要的依赖项--MyBatis和MyBatis-Spring：

    ```xml
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis</artifactId>
        <version>3.5.2</version>
    </dependency>
    <dependency>
        <groupId>org.mybatis</groupId>
        <artifactId>mybatis-spring</artifactId>
        <version>2.0.2</version>
    </dependency>
    ```

    除此以外，我们还需要基本的Spring依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>5.3.8</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-beans</artifactId>
        <version>5.3.8</version>
    </dependency>
    ```

    在我们的例子中，我们将使用H2嵌入式数据库来简化设置，并使用spring-jdbc模块的EmbeddedDatabaseBuilder类进行配置：

    ```xml
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>1.4.199</version>
    </dependency>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-jdbc</artifactId>
        <version>5.3.8</version>
    </dependency>
    ```

    1. 基于注解的配置

        Spring简化了MyBatis的配置。唯一需要的元素是javax.sql.Datasource，org.apache.ibatis.session.SqlSessionFactory，以及至少一个映射器。

        首先，让我们创建一个配置类：mybatis.spring/PersistenceConfig.java

        我们还应用了MyBatis-Spring的@MapperScan注解，该注解扫描定义的包，并自动拾取使用任何映射器注解的接口，如@Select或@Delete。

        使用@MapperScan还可以确保每一个提供的映射器都被自动注册为Bean，以后可以使用[@Autowired](https://www.baeldung.com/spring-autowire)注解。

        我们现在可以创建一个简单的ArticleMapper接口：

        ```java
        public interface ArticleMapper {
            @Select("SELECT * FROM ARTICLES WHERE id = #{id}")
            Article getArticle(@Param("id") Long id);
        }
        ```

        最后，测试我们的设置：

        ```java
        @RunWith(SpringJUnit4ClassRunner.class)
        @ContextConfiguration(classes = PersistenceConfig.class)
        public class ArticleMapperIntegrationTest {
            @Autowired
            ArticleMapper articleMapper;
            @Test
            public void whenRecordsInDatabase_shouldReturnArticleWithGivenId() {
                Article article = articleMapper.getArticle(1L);
                assertThat(article).isNotNull();
                assertThat(article.getId()).isEqualTo(1L);
                assertThat(article.getAuthor()).isEqualTo("Baeldung");
                assertThat(article.getTitle()).isEqualTo("Working with MyBatis in Spring");
            }
        }
        ```

        在上面的例子中，我们已经使用MyBatis来检索我们之前在data.sql文件中插入的唯一记录。

    2. 基于XML的配置

        如前所述，为了在Spring中使用MyBatis，我们需要数据源、SqlSessionFactory和至少一个映射器。

        让我们在beans.xml配置文件中创建所需的bean定义：

        ```xml
        <jdbc:embedded-database id="dataSource" type="H2">
            <jdbc:script location="schema.sql"/>
            <jdbc:script location="data.sql"/>
        </jdbc:embedded-database>
            
        <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
            <property name="dataSource" ref="dataSource" />
        </bean>

        <bean id="articleMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
            <property name="mapperInterface" value="com.baeldung.mybatis.ArticleMapper" />
            <property name="sqlSessionFactory" ref="sqlSessionFactory" />
        </bean>
        ```

        在这个例子中，我们还使用了spring-jdbc提供的自定义XML模式来配置我们的H2数据源。

        为了测试这个配置，我们可以重新使用之前实现的测试类。然而，我们必须调整上下文配置，我们可以通过应用注解来做到这一点：

        `@ContextConfiguration(locations = "classpath:/beans.xml")`

        运行 ArticleMapperXMLUnitTest.java，测试方法实现在 ArticleMapperCommonUnitTest.java 中。

4. Spring Boot

    Spring Boot提供了一些机制，更加简化了MyBatis与Spring的配置。

    首先，让我们把mybatis-spring-boot-starter依赖性添加到我们的pom.xml中：

    ```xml
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>2.1.0</version>
    </dependency>
    ```

    默认情况下，如果我们使用自动配置功能，Spring Boot会从我们的classpath检测到H2依赖，并为我们配置Datasource和SqlSessionFactory。此外，它还会在启动时执行schema.sql和data.sql。

    如果我们不使用嵌入式数据库，我们可以通过application.yml或application.properties文件使用配置，或者定义一个指向数据库的Datasource Bean。

    我们唯一要做的就是以与之前相同的方式定义一个映射器接口，并用MyBatis的@Mapper注解来注释它。结果，Spring Boot扫描了我们的项目，寻找该注释，并将我们的映射器注册为Bean。

    之后，我们可以通过应用spring-boot-starter-test的注解，使用之前定义的测试类来测试我们的配置。

5. 总结

    在这篇文章中，我们探索了用Spring配置MyBatis的多种方法。

    我们看了使用基于注解和XML配置的例子，并展示了MyBatis与Spring Boot的自动配置功能。

## Relevant Articles

- [x] [MyBatis with Spring](https://www.baeldung.com/spring-mybatis)

## Code

一如既往，本文中使用的完整代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/persistence-modules/spring-mybatis)上找到。
