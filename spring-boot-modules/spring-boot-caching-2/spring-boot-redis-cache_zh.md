# [带有Redis的Spring Boot缓存](https://www.baeldung.com/spring-boot-redis-cache)

1. 一览表

    在本简短的教程中，我们将了解如何将Redis配置为Spring Boot缓存的数据存储。

2. 依赖性

    首先，让我们添加spring-boot-starter-cache和spring-boot-starter-data-redis工件：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
        <version>3.1.5</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>3.1.5</version>
    </dependency>
    ```

    这些增加了缓存支持，并带来了所有所需的依赖项。

3. 配置

    通过添加上述依赖项和@EnableCaching注释，Spring Boot将使用默认缓存配置自动配置aRedisCacheManager。然而，我们可以在缓存管理器初始化之前通过几种有用的方式修改此配置。

    首先，让我们创建一个RedisCacheConfiguration bean：

    ```java
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(60))
        .disableCachingNullValues()
        .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
    ```

    这让我们可以更好地控制默认配置。例如，我们可以设置所需的生存时间（TTL）值，并自定义飞行中缓存创建的默认序列化策略。

    为了完全控制缓存设置，让我们注册我们自己的RedisCacheManagerBuilderCustomizer bean：

    ```java
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
        .withCacheConfiguration("itemCache",
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
        .withCacheConfiguration("customerCache",
            RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));
    }
    ```

    在这里，我们使用RedisCacheManagerBuilder和RedisCacheConfiguration分别为itemCache和customerCache配置10分钟和5分钟的TTL值。这有助于进一步微调每个缓存的缓存行为，包括空值、键前缀和二进制序列化。

    值得一提的是，Redis实例的默认连接详细信息是localhost:6379。Redis配置可用于进一步调整低级连接详细信息以及主机和端口。

4. 示例

    在我们的示例中，我们有一个ItemService组件，可以从数据库中检索项目信息。实际上，这代表了一种潜在的昂贵操作，也是缓存的良好候选者。

    首先，让我们使用嵌入式Redis服务器为该组件创建集成测试：

    ```java
    @Import({ CacheConfig.class, ItemService.class})
    @ExtendWith(SpringExtension.class)
    @EnableCaching
    @ImportAutoConfiguration(classes = {
    CacheAutoConfiguration.class,
    RedisAutoConfiguration.class
    })
    class ItemServiceCachingIntegrationTest {

        @MockBean
        private ItemRepository mockItemRepository;

        @Autowired
        private ItemService itemService;

        @Autowired
        private CacheManager cacheManager;

        @Test
        void givenRedisCaching_whenFindItemById_thenItemReturnedFromCache() {
            Item anItem = new Item(AN_ID, A_DESCRIPTION);
            given(mockItemRepository.findById(AN_ID))
            .willReturn(Optional.of(anItem));

            Item itemCacheMiss = itemService.getItemForId(AN_ID);
            Item itemCacheHit = itemService.getItemForId(AN_ID);

            assertThat(itemCacheMiss).isEqualTo(anItem);
            assertThat(itemCacheHit).isEqualTo(anItem);

            verify(mockItemRepository, times(1)).findById(AN_ID);
            assertThat(itemFromCache()).isEqualTo(anItem);
        }
    }
    ```

    在这里，我们为缓存行为创建一个测试切片，并调用getItemForId两次。第一次调用应该从存储库获取项目，但第二次调用应该在不调用存储库的情况下从缓存中返回项目。

    最后，让我们使用Spring的@Cacheable注释启用缓存行为：

    ```java
    @Cacheable(value = "itemCache")
    public Item getItemForId(String id) {
        return itemRepository.findById(id)
        .orElseThrow(RuntimeException::new);
    }
    ```

    这应用了缓存逻辑，同时依赖于我们之前配置的Redis缓存基础设施。

5. 结论

    在本文中，我们看到了如何使用Redis进行Spring缓存。

    我们首先描述了如何以最小的配置自动配置Redis缓存。然后，我们研究了如何通过注册配置bean来进一步自定义缓存行为。
