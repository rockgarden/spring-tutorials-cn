# [在Spring中把资源加载成一个字符串](https://www.baeldung.com/spring-load-resource-as-string)

在本教程中，我们将探讨将包含文本的资源内容作为字符串注入Spring Bean的各种方法。

我们将研究如何定位资源并读取其内容。

此外，我们还将演示如何在多个Bean中共享加载的资源。我们将通过使用与依赖性注入有关的注解来展示这一点，尽管同样可以通过使用[基于XML的注入](https://www.baeldung.com/spring-xml-injection)和在XML属性文件中声明bean来实现。

1. 使用资源

    我们可以通过使用[资源](https://www.baeldung.com/spring-classpath-file-access)接口来简化资源文件的定位。Spring使用资源加载器帮助我们找到并读取资源，该加载器根据提供的路径决定选择哪种资源实现。资源实际上是一种访问资源内容的方式，而不是内容本身。

    让我们看看[获取classpath上资源的Resource实例](https://www.baeldung.com/spring-classpath-file-access)的一些方法。

    1. 使用资源加载器

        如果我们喜欢使用懒惰加载，我们可以使用ResourceLoader这个类。

        ```java
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:resource.txt");
        ```

        我们也可以用@Autowired将ResourceLoader注入我们的Bean中。

        ```java
        @Autowired
        private ResourceLoader resourceLoader;
        ```

        2.2. 使用@Value

        我们可以用@Value将资源直接注入Spring Bean中。

        ```java
        @Value("classpath:resource.txt")
        private Resource resource;
        ```

2. 从资源到字符串的转换

    一旦我们有了对资源的访问权，我们就需要能够把它读成一个字符串。让我们创建一个带有静态方法asString的ResourceReader实用类来为我们做这个。

    首先，我们必须获得一个InputStream。

    `InputStream inputStream = resource.getInputStream();`

    我们的下一步是把这个InputStream转换为一个字符串。我们可以使用Spring自己的FileCopyUtils#copyToString方法。

    ```java
    public class ResourceReader {
        public static String asString(Resource resource) {
            try (Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        // more utility methods
    }
    ```

    还有[很多其他的实现方法](https://www.baeldung.com/convert-input-stream-to-string)，例如，使用Spring的StreamUtils类的copyToString。

    让我们再创建一个实用方法readFileToString，它将为一个路径检索资源，并调用asString方法将其转换为一个字符串。

    ```java
    public static String readFileToString(String path) {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(path);
        return asString(resource);
    }
    ```

3. 添加一个配置类

    如果每个Bean都必须单独注入资源字符串，那么就有可能出现代码重复和Bean拥有自己单独的字符串副本而导致更多内存的使用。

    我们可以通过在加载应用上下文时将资源的内容注入一个或多个Spring Bean来实现一个更整洁的解决方案。通过这种方式，我们可以从需要使用该内容的各个Bean那里隐藏读取资源的实现细节。

    ```java
    @Configuration
    public class LoadResourceConfig {
        // Bean Declarations
    }
    ```

    1. 使用一个持有资源字符串的Bean

        让我们在一个@Configuration类中声明Bean来持有资源内容。

        ```java
        @Bean
        public String resourceString() {
            return ResourceReader.readFileToString("resource.txt");
        }
        ```

        现在让我们通过添加@Autowired注解将注册的Bean注入到字段中。

        ```java
        public class LoadResourceAsStringIntegrationTest {
            private static final String EXPECTED_RESOURCE_VALUE = "...";  // The string value of the file content
            @Autowired
            @Qualifier("resourceString")
            private String resourceString;
            @Test
            public void givenUsingResourceStringBean_whenConvertingAResourceToAString_thenCorrect() {
                assertEquals(EXPECTED_RESOURCE_VALUE, resourceString);
            }
        }
        ```

        在这种情况下，我们使用@Qualifier注解和Bean的名字，因为我们可能需要注入相同类型的多个字段--String。

        我们应该注意到，限定符中使用的Bean名称来自配置类中创建Bean的方法名称。

4. 使用SpEL

    最后，让我们看看如何使用Spring表达式语言来描述将资源文件直接加载到我们类中的一个字段所需的代码。

    让我们使用@Value注解，将文件内容注入字段resourceStringUsingSpel中。

    ```java
    public class LoadResourceAsStringIntegrationTest {
        private static final String EXPECTED_RESOURCE_VALUE = "..."; // The string value of the file content
        @Value(
        "#{T(com.baeldung.loadresourceasstring.ResourceReader).readFileToString('classpath:resource.txt')}"
        )
        private String resourceStringUsingSpel;
        @Test
        public void givenUsingSpel_whenConvertingAResourceToAString_thenCorrect() {
            assertEquals(EXPECTED_RESOURCE_VALUE, resourceStringUsingSpel);
        }
    }
    ```

    这里我们调用了ResourceReader#readFileToString，通过使用 "classpath:" 来描述文件的位置 - 在我们的@Value注解中使用了一个 "classpath:"的预设路径。

    为了减少SpEL中的代码量，我们在类ResourceReader中创建了一个辅助方法，它使用Apache Commons FileUtils来访问所提供的路径中的文件。

    ```java
    public class ResourceReader {
        public static String readFileToString(String path) throws IOException {
            return FileUtils.readFileToString(ResourceUtils.getFile(path), StandardCharsets.UTF_8);
        }
    }
    ```

5. 总结

    在本教程中，我们已经回顾了一些将资源转换为字符串的方法。

    首先，我们看到了如何产生一个资源来访问文件，以及如何从资源读到字符串。

    接下来，我们还展示了如何隐藏资源加载实现，并通过在@Configuration中创建合格的Bean，让字符串内容在Bean之间共享，允许字符串被自动连接。

    最后，我们使用了SpEL，它提供了一个紧凑而直接的解决方案，尽管它需要一个自定义的辅助函数来阻止它变得过于复杂。
