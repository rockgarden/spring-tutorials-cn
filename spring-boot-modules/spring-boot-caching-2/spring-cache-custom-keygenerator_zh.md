# [Spring Cache – 创建自定义 KeyGenerator](https://www.baeldung.com/spring-cache-custom-keygenerator)

Spring+ 缓存

1. 概述

    在本篇快速教程中，我们将演示如何在 Spring Cache 中创建自定义的键生成器（KeyGenerator）。

    如需了解该模块的入门知识，请参阅[本文](https://www.baeldung.com/spring-cache-tutorial)。

2. KeyGenerator

    KeyGenerator 负责为缓存中的每个数据项生成键（key），该键将在检索数据项时用于查找。

    其默认实现是 `SimpleKeyGenerator`——它使用方法参数来生成键。这意味着，如果我们有两个方法使用相同的缓存名称和相同类型的参数集，那么极有可能发生键冲突。

    这也意味着一个方法的缓存数据可能会被另一个方法覆盖。

3. 自定义 KeyGenerator

    `KeyGenerator` 只需实现一个方法：

    ```java
    Object generate(Object object, Method method, Object... params)
    ```

    如果该方法未被正确实现或使用，可能导致缓存数据被意外覆盖。

    下面是一个实现示例：

    ```java
    public class CustomKeyGenerator implements KeyGenerator {

        public Object generate(Object target, Method method, Object... params) {
            return target.getClass().getSimpleName() + "_"
            + method.getName() + "_"
            + StringUtils.arrayToDelimitedString(params, "_");
        }
    }
    ```

    之后，我们有两种使用方式：

    第一种是在 `ApplicationConfig` 中声明一个 Bean。
    需要注意的是，该配置类必须继承 `CachingConfigurerSupport` 或实现 `CachingConfigurer` 接口：

    ```java
    @EnableCaching
    @Configuration
    public class ApplicationConfig extends CachingConfigurerSupport {

        @Bean
        public CacheManager cacheManager() {
            SimpleCacheManager cacheManager = new SimpleCacheManager();
            Cache booksCache = new ConcurrentMapCache("books");
            cacheManager.setCaches(Arrays.asList(booksCache));
            return cacheManager;
        }

        @Bean("customKeyGenerator")
        public KeyGenerator keyGenerator() {
            return new CustomKeyGenerator();
        }
    }
    ```

    第二种方式是仅在特定方法上使用它：

    ```java
    @Component
    public class BookService {

        @Cacheable(value = "books", keyGenerator = "customKeyGenerator")
        public List<Book> getBooks() {
            List<Book> books = new ArrayList<>();
            books.add(new Book("The Counterfeiters", "André Gide"));
            books.add(new Book("Peer Gynt and Hedda Gabler", "Henrik Ibsen"));
            return books;
        }
    }
    ```

4. 结论

    在本文中，我们探讨了一种实现 Spring Cache 自定义 `KeyGenerator` 的方法。
