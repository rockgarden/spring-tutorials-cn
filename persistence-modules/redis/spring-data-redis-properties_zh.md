# [Spring Data Redis的基于属性的配置](https://www.baeldung.com/spring-data-redis-properties)

1. 一览表

    Spring Boot的主要吸引力之一是它经常将第三方配置简化为仅少数属性。

    在本教程中，我们将了解Spring Boot如何简化与Redis的工作。

2. 为什么是Redis？

    Redis是最受欢迎的内存内数据结构存储之一。出于这个原因，它可以用作数据库、缓存和消息代理。

    在性能方面，它因其[快速的响应时间](https://redis.io/topics/benchmarks)而广为人知。因此，它每秒可以为数十万次操作提供服务，并且易于扩展。

    它与Spring Boot应用程序很好地搭配。例如，我们的微服务架构可以将其用作缓存。我们也可以把它用作NoSQL数据库。

3. 运行Redis

    首先，让我们使用他们的官方Docker映像创建一个Redis实例。

    `$ docker run -p 16379:6379 -d redis:6.0 redis-server --requirepass "mypass"`

    上面，我们刚刚在端口16379上启动了Redis的实例，密码是mypass。

4. 启动器

    Spring极大地支持使用Spring Data Redis将我们的Spring Boot应用程序与Redis连接起来。

    因此，接下来，让我们确保我们在pom.xml中具有spring-boot-starter-data-redis依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>2.7.11</version>    
    </dependency>
    ```

5. Lettuce

    接下来，让我们配置客户端。

    我们将使用的Java Redis客户端是[Lettuce](https://www.baeldung.com/java-redis-lettuce)，因为Spring Boot默认使用它。然而，我们也可以使用[Jedis](https://www.baeldung.com/jedis-java-redis-client-library)。

    无论哪种方式，结果都是RedisTemplate的实例：

    ```java
    @Bean
    public RedisTemplate<Long, Book> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Long, Book> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Add some specific configuration here. Key serializers, etc.
        return template;
    }
    ```

6. Properties

    当我们使用Lettuce时，我们不需要配置RedisConnectionFactory。Spring Boot为我们做了这一事。

    那么，我们剩下的就是在我们的application.properties文件（适用于Spring Boot 2.x）中指定一些属性：

    ```properties
    spring.redis.database=0
    spring.redis.host=localhost
    spring.redis.port=16379
    spring.redis.password=mypass
    spring.redis.timeout=60000
    ```

    对于Spring Boot 3.x，我们需要设置以下属性：

    ```properties
    spring.data.redis.database=0
    spring.data.redis.host=localhost
    spring.data.redis.port=16379
    spring.data.redis.password=mypass
    spring.data.redis.timeout=60000
    ```

    分别：

    - database设置连接工厂使用的数据库索引
    - host是服务器主机所在的位置
    - port表示服务器正在监听的端口
    - password是服务器的登录密码，并且
    - timeout建立连接超时

    当然，我们可以配置许多其他属性。完整的配置属性列表可在Spring Boot[文档](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data-properties)中找到。

7. Demo

    最后，让我们尝试在我们的应用程序中使用它。如果我们想象一个图书类和一个图书存储库，我们可以创建和检索图书，使用我们的[RedisTemplate](https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/RedisTemplate.html)与Redis作为我们的后端进行交互：

    ```java
    @Autowired
    private RedisTemplate<Long, Book> redisTemplate;

    public void save(Book book) {
        redisTemplate.opsForValue().set(book.getId(), book);
    }

    public Book findById(Long id) {
        return redisTemplate.opsForValue().get(id);
    }
    ```

    默认情况下，Lettuce将为我们管理序列化和反序列化，所以现在没有什么可做的了。然而，很高兴知道这也可以配置。

    另一个重要功能是RedisTemplate是线程安全的，因此它在[多线程环境](https://www.baeldung.com/java-thread-safety)中可以正常工作。

8. 结论

    在本文中，我们配置了Spring Boot通过Lettuce与Redis对话。我们通过启动器、单个@Bean配置和少数属性实现了它。

    总结一下，我们使用RedisTemplate让Redis充当一个简单的后端。
