# [Spring Data Redis简介](https://www.baeldung.com/spring-data-redis-tutorial)

1. 一览表

    本教程是对Spring Data Redis的介绍，它向[Redis](http://redis.io/)（流行的内存内数据结构存储）提供了Spring Data平台的抽象。

    Redis由基于密钥库的数据结构驱动，以持久化数据，并可用作数据库、缓存、消息代理等。

    我们将能够使用Spring Data（模板等）的常见模式，同时拥有所有Spring Data项目的传统简单性。

2. Maven附属机构

    让我们从在pom.xml中声明Spring Data Redis依赖项开始：

    ```xml
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-redis</artifactId>
        <version>3.2.0</version>
    </dependency>

    <dependency>
        <groupId>redis.clients</groupId>
        <artifactId>jedis</artifactId>
        <version>5.1.2</version>
        <type>jar</type>
    </dependency>
    ```

    最新版本的spring-data-redis和jedis可以从Maven Central下载。

    或者，我们可以为Redis使用Spring Boot启动器，这将消除对单独的spring-data和jedis依赖项的需求：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>3.2.0</version>
    </dependency>
    ```

    Maven Central再次提供最新版本的信息。

3. Redis配置

    要定义应用程序客户端和Redis服务器实例之间的连接设置，我们需要使用Redis客户端。

    Java有许多Redis客户端实现可用。在本教程中，我们将使用[Jedis](https://github.com/xetorthio/jedis)——一个简单而强大的Redis客户端实现。

    框架中对XML和Java配置有很好的支持。对于本教程，我们将使用基于Java的配置。

    1. Java配置

        让我们从配置bean定义开始：

        ```java
        @Bean
        JedisConnectionFactory jedisConnectionFactory() {
            return new JedisConnectionFactory();
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate() {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(jedisConnectionFactory());
            return template;
        }
        ```

        配置非常简单。

        首先，使用Jedis客户端，我们正在定义一个连接工厂。

        然后我们使用jedisConnectionFactory定义了一个RedisTemplate。这可用于使用自定义存储库查询数据。

    2. 自定义连接属性

        请注意，上述配置中缺少通常的连接相关属性。例如，配置中缺少服务器地址和端口。原因很简单：我们正在使用默认值。

        然而，如果我们需要配置连接详细信息，我们可以随时修改jedisConnectionFactory配置：

        ```java
        @Bean
        JedisConnectionFactory jedisConnectionFactory() {
            JedisConnectionFactory jedisConFactory
            = new JedisConnectionFactory();
            jedisConFactory.setHostName("localhost");
            jedisConFactory.setPort(6379);
            return jedisConFactory;
        }
        ```

4. Redis存储库

    让我们使用一个学生实体：

    ```java
    @RedisHash("Student")
    public class Student implements Serializable {
    
        public enum Gender { 
            MALE, FEMALE
        }

        private String id;
        private String name;
        private Gender gender;
        private int grade;
        // ...
    }
    ```

    1. Spring数据存储库

        现在让我们创建StudentRepository：

        ```java
        @Repository
        public interface StudentRepository extends CrudRepository<Student, String> {}
        ```

5. 使用StudentRepository访问数据

    通过在StudentRepository中扩展CrudRepository，我们自动获得一套执行CRUD功能的完整持久方法。

    1. 保存一个新的学生对象

        让我们在数据存储中保存一个新的学生对象：

        ```java
        Student student = new Student(
        "Eng2015001", "John Doe", Student.Gender.MALE, 1);
        studentRepository.save(student);
        ```

    2. 检索现有学生对象

        我们可以通过获取学生数据来验证上一节中学生的正确插入：

        ```java
        Student retrievedStudent =
        studentRepository.findById("Eng2015001").get();
        ```

    3. 更新现有学生对象

        让我们更改上面检索到的学生姓名，并再次保存：

        ```java
        retrievedStudent.setName("Richard Watson");
        studentRepository.save(student);
        ```

        最后，我们可以再次检索学生的数据，并验证数据存储中的姓名是否已更新。

    4. 删除现有学生数据

        我们可以删除插入的学生数据：

        `studentRepository.deleteById(student.getId());`

        现在我们可以搜索学生对象，并验证结果为空。

    5. 查找所有学生数据

        我们可以插入一些学生对象：

        ```java
        Student engStudent = new Student(
        "Eng2015001", "John Doe", Student.Gender.MALE, 1);
        Student medStudent = new Student(
        "Med2015001", "Gareth Houston", Student.Gender.MALE, 2);
        studentRepository.save(engStudent);
        studentRepository.save(medStudent);
        ```

        我们也可以通过插入集合来实现这一点。为此，有一个不同的方法——saveAll（）——接受一个包含我们想要持久的多个学生对象的单个可重对象。

        为了找到所有插入的学生，我们可以使用findAll（）方法：

        ```java
        List<Student> students = new ArrayList<>();
        studentRepository.findAll().forEach(students::add);
        ```

        然后，我们可以快速检查学生列表的大小，或通过检查每个对象的属性来验证更大的粒度。

6. 结论

    在本文中，我们介绍了Spring Data Redis的基础知识。
