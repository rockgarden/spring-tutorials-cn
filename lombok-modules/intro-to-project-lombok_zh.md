# [Project Lombok 入门指南](https://www.baeldung.com/intro-to-project-lombok)

Java+ · Lombok · 参考文档  

1. 概述

    Java 是一门优秀的语言，但在日常开发中，为了满足框架规范或完成常见任务，我们常常需要编写大量冗余代码。这些代码对业务本身并无实质价值，而 **Lombok** 正是为解决这一问题、提升开发效率而生。

    Lombok 通过在构建过程中注入字节码，根据我们在源码中添加的注解，自动生成 `.class` 文件中的相应代码。

    我们可以将 Project Lombok 库集成到构建工具（如 Maven）中，自动为类生成 getter/setter、日志变量等代码。

    本教程将演示如何在 Maven 项目中使用 Lombok，并体验其核心功能。

2. Maven 项目配置

    在任何构建系统中集成 Lombok 都非常简单。官方[项目页面](https://projectlombok.org/features/index.html)提供了详细说明。只需在 Maven 依赖中添加如下配置：

    ```xml
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.36</version>
        <scope>provided</scope>
    </dependency>
    ```

    由于 Lombok 是纯构建期依赖（非运行时依赖），因此不会强制使用我们 `.jar` 包的用户也依赖 Lombok。

3. 局部变量类型推断

    自 Lombok 1.16.20 起，支持使用 `var` 根据初始化表达式推断局部变量类型；Lombok 1.18.22 起支持使用 `val` 推断 final 局部变量类型（即 `val` 会被替换为 `final var`）。注意：该功能**不能用于字段(fields)**。

    示例：

    ```java
    public String lombokTypeInferred() {
        val list = new ArrayList<String>();
        list.add("Hello, Lombok!");
        val listElem = list.get(0);
        return listElem.toLowerCase();
    }
    ```

    Lombok 会自动将 `list` 生成为 `final ArrayList<String>` 类型变量。

4. Getter/Setter 与构造函数

    在 Java 中，通过公共 getter/setter 方法封装对象属性是常见做法，许多框架（如 Java Bean）也依赖此模式（含无参构造函数和属性的 get/set 方法）。

    虽然 IDE 可自动生成这些代码，但它们仍需存在于源码中，并在新增或重命名字段时手动维护。

    设想一个作为 JPA 实体的类：

    ```java
    @Entity
    public class User implements Serializable {
        private @Id Long id; // 持久化时由数据库设置

        private String firstName;
        private String lastName;
        private int age;

        public User() {}

        public User(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }

        // getter 和 setter：约30行冗余代码
    }
    ```

    这是一个非常简单的类，但如果加上 getter/setter，代码量将远超实际业务信息（“用户有姓名和年龄”）。

    使用 Lombok 重构后：

    ```java
    @Entity
    @Getter @Setter @NoArgsConstructor
    public class User implements Serializable {
        private @Id Long id;

        private String firstName;
        private String lastName;
        private int age;

        public User(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }
    }
    ```

    通过 `@Getter` 和 `@Setter` 注解，Lombok 为所有字段自动生成访问器；`@NoArgsConstructor` 生成无参构造函数。

    注意：以上就是完整代码！相比之前省略 getter/setter 的版本，这里没有省略任何内容。对于仅含三个属性的类，代码节省效果显著！

    新增属性时，注解默认作用于所有字段。若需调整特定字段的访问级别（如希望 `id` 字段的 setter 仅包内或子类可见），可单独为字段添加细粒度注解：

    ```java
    private @Id @Setter(AccessLevel.PROTECTED) Long id;
    ```

5. 懒加载 Getter

    应用常需执行耗时操作（如从文件或数据库读取静态数据），并将结果缓存以供后续使用。更优的做法是“按需加载”——仅在首次调用 getter 时执行操作。这称为**懒加载（Lazy Loading）**。

    Lombok 通过 `@Getter(lazy = true)` 实现懒加载：

    ```java
    public class GetterLazy {
        @Getter(lazy = true)
        private final Map<String, Long> transactions = getTransactions();

        private Map<String, Long> getTransactions() {
            final Map<String, Long> cache = new HashMap<>();
            List<String> txnRows = readTxnListFromFile();
            txnRows.forEach(s -> {
                String[] txnIdValueTuple = s.split(DELIMETER);
                cache.put(txnIdValueTuple[0], Long.parseLong(txnIdValueTuple[1]));
            });
            return cache;
        }
    }
    ```

    编译后，Lombok 会生成线程安全的懒加载 getter：

    ```java
    public class GetterLazy {
        private final AtomicReference<Object> transactions = new AtomicReference();

        public Map<String, Long> getTransactions() {
            Object value = this.transactions.get();
            if (value == null) {
                synchronized(this.transactions) {
                    value = this.transactions.get();
                    if (value == null) {
                        Map<String, Long> actualValue = this.readTxnsFromFile();
                        value = actualValue == null ? this.transactions : actualValue;
                        this.transactions.set(value);
                    }
                }
            }
            return (Map)((Map)(value == this.transactions ? null : value));
        }
    }
    ```

    **注意**：Lombok 使用 `AtomicReference` 确保线程安全。建议通过 `getTransactions()` 方法访问字段，而非直接操作 `transactions` 字段。若在类中使用 `@ToString` 等注解，它们也会调用 getter 而非直接访问字段。

6. 不可变对象的单字段修改（With 模式）

    使用 `@With` 注解可为不可变对象生成“克隆并修改单字段”的方法。例如，为 `User` 类的 `age` 字段添加 `@With`：

    ```java
    @AllArgsConstructor
    public class User implements Serializable {
        private @Id Long id;
        private final String firstName;
        private final String lastName;
        @With private final int age;
    }
    ```

    即可通过 `withAge(int newAge)` 方法克隆对象并修改年龄：

    ```java
    User user = new User("John", "Smith", 40);
    User user_updated = user.withAge(41);
    ```

7. 值类/DTO

    在定义“值对象”或“数据传输对象（DTO）”时，我们常希望其为不可变结构（创建后不再修改）。例如，表示登录结果的类：

    ```java
    public class LoginResult {
        private final Instant loginTs;
        private final String authToken;
        private final Duration tokenValidity;
        private final URL tokenRefreshUrl;

        // 构造函数（需检查 null）
        // 只读访问器（不一定是 get*() 形式）
    }
    ```

    使用 Lombok 可大幅简化：

    ```java
    @RequiredArgsConstructor
    @Accessors(fluent = true) @Getter
    public class LoginResult {
        private final @NonNull Instant loginTs;
        private final @NonNull String authToken;
        private final @NonNull Duration tokenValidity;
        private final @NonNull URL tokenRefreshUrl;
    }
    ```

    - `@RequiredArgsConstructor`：为所有 final 字段生成构造函数。
    - `@NonNull`：构造函数自动检查 null，抛出 `NullPointerException`。
    - `@Accessors(fluent = true)`：getter 方法名与字段名相同（如 `authToken()` 而非 `getAuthToken()`）。若字段非 final，setter 也支持链式调用：

    ```java
    return new LoginResult()
    .loginTs(Instant.now())
    .authToken("asdasd")
    // ...
    ```

8. 核心 Java 模板代码

    `toString()`、`equals()` 和 `hashCode()` 方法的生成与维护同样繁琐。Lombok 提供：

    - [@ToString](https://projectlombok.org/features/ToString.html)：生成包含所有字段的 `toString()` 方法。
    - [@EqualsAndHashCode](https://projectlombok.org/features/EqualsAndHashCode.html)：根据字段生成[语义正确](http://www.artima.com/lejava/articles/equality.html)的 `equals()` 和 `hashCode()`。

    二者均支持配置选项。例如，若类参与继承体系，可设置 `callSuper = true` 以包含父类结果。

    1. 示例

        假设 `User` 实体包含事件列表：

        ```java
        @OneToMany(mappedBy = "user")
        private List<UserEvent> events;
        ```

        使用 `@ToString(exclude = {"events"})` 可避免打印整个列表（也防止循环引用）：

        ```java
        @ToString(exclude = {"events"})
        public class User { ... }
        ```

        对于 `LoginResult`，若希望仅根据 `authToken` 判断相等性：

        ```java
        @EqualsAndHashCode(of = {"authToken"})
        public class LoginResult { ... }
        ```

        若喜欢“一站式”注解，可尝试 `@Data`（组合 `@ToString`、`@EqualsAndHashCode`、`@Getter`、`@Setter`、`@RequiredArgsConstructor`）或 `@Value`（用于不可变类）。

    2. （不）在 JPA 实体中使用 @EqualsAndHashCode

        是否应在 JPA 实体中使用 Lombok 生成的 `equals()` 和 `hashCode()` 是开发者常争论的话题。默认实现包含所有非 final 字段，即使使用 `onlyExplicitlyIncluded` 限定主键，仍可能导致问题（详见 Thorben Janssen 的[博客](https://thorben-janssen.com/lombok-hibernate-how-to-avoid-common-pitfalls)）。**建议避免在 JPA 实体中使用 Lombok 生成这两个方法。**

9. 构建者模式（Builder Pattern）

    考虑一个 REST API 客户端配置类：

    ```java
    public class ApiClientConfiguration {
        private String host;
        private int port;
        private boolean useHttps;
        private long connectTimeout;
        private long readTimeout;
        private String username;
        private String password;

        // 无参构造？全参构造？
        // getter... setter?
    }
    ```

    理想情况下，配置对象应不可变（避免 setter），但手写长参数构造函数是反模式。Lombok 的 `@Builder` 可生成流畅的构建器：

    ```java
    @Builder
    public class ApiClientConfiguration {
        // ... 其他字段不变
    }
    ```

    使用方式：

    ```java
    ApiClientConfiguration config =
    ApiClientConfiguration.builder()
        .host("api.server.com")
        .port(443)
        .useHttps(true)
        .connectTimeout(15_000L)
        .readTimeout(5_000L)
        .username("myusername")
        .password("secret")
        .build();
    ```

10. 摆脱受检异常负担

    许多 Java API 抛出受检异常，迫使客户端代码捕获或声明抛出。我们常将其包装为运行时异常：

    ```java
    @SneakyThrows
    public String resourceAsString() {
        try (InputStream is = this.getClass().getResourceAsStream("sure_in_my_jar.txt")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            return br.lines().collect(Collectors.joining("\n"));
        }
    }
    ```

    [@SneakyThrows](https://projectlombok.org/features/SneakyThrows.html) 会自动将受检异常转换为运行时异常，避免冗余的 `try-catch`。

11. 确保资源释放

    Java 7 的 try-with-resources 要求资源实现 `AutoCloseable`。Lombok 的 [@Cleanup](https://projectlombok.org/features/Cleanup.html) 更灵活——可作用于任何局部变量，自动调用 `close()` 方法（无需实现特定接口）：

    ```java
    @Cleanup InputStream is = this.getClass().getResourceAsStream("res.txt");
    ```

    若释放方法名非 `close()`，可自定义：

    ```java
    @Cleanup("dispose") JFrame mainFrame = new JFrame("Main Window");
    ```

12. 自动注入日志器

    我们常手动创建日志器实例（如 SLF4J）：

    ```java
    public class ApiClientConfiguration {
        private static Logger LOG = LoggerFactory.getLogger(ApiClientConfiguration.class);
        // LOG.debug(), LOG.info(), ...
    }
    ```

    Lombok 通过注解简化此过程：

    ```java
    @Slf4j // 或 @Log、@CommonsLog、@Log4j、@Log4j2、@XSlf4j
    public class ApiClientConfiguration {
        // log.debug(), log.info(), ...
    }
    ```

    支持多种日志[框架](https://projectlombok.org/features/log)，且可自定义实例名、主题等。

13. 编写线程安全方法

    Java 的 synchronized 关键字可能导致死锁（其他代码可能同步同一实例）。Lombok 的 [@Synchronized](https://projectlombok.org/features/Synchronized.html) 使用自动生成的私有锁字段，更安全：

    ```java
    @Synchronized
    public void putValueInCache(String key, Object value) {
        // 线程安全代码
    }
    ```

    **注意**：在 Java 21+ 使用[虚拟线程](https://www.baeldung.com/java-virtual-thread-vs-thread)时，应改用 `@Locked`、`@Locked.Read`、`@Locked.Write` 获取 `ReentrantLock`。

14. 对象组合自动化

    Java 无语言级“组合优于继承”支持。Lombok 的 [@Delegate](https://projectlombok.org/features/experimental/Delegate.html) 可模拟 Traits/Mixins。例如：

    - 定义接口 `HasContactInformation`。
    - 创建适配器类 `ContactInformationSupport` 实现接口。
    - 在 `User` 和 `Customer` 中通过 `@Delegate` 组合适配器。

    接口：

    ```java
    public interface HasContactInformation {
        String getFirstName(); void setFirstName(String firstName);
        String getFullName();
        String getLastName(); void setLastName(String lastName);
        String getPhoneNr(); void setPhoneNr(String phoneNr);
    }
    ```

    适配器：

    ```java
    @Data
    public class ContactInformationSupport implements HasContactInformation {
        private String firstName;
        private String lastName;
        private String phoneNr;

        @Override
        public String getFullName() {
            return getFirstName() + " " + getLastName();
        }
    }
    ```

    组合到 `User`：

    ```java
    public class User implements HasContactInformation {
        @Delegate(types = {HasContactInformation.class})
        private final ContactInformationSupport contactInformation = new ContactInformationSupport();
        // User 自动实现所有联系信息方法
    }
    ```

15. 字段名常量

    在反射、序列化或动态查询中，我们常需字符串常量表示字段名。手动管理易出错。Lombok 的 `@FieldNameConstants` 自动生成字段名常量：

    ```java
    @Getter
    @FieldNameConstants
    public class Person {
        private final String firstName;
        private final String lastName;
        private final int age;

        public Person(String firstName, String lastName, int age) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.age = age;
        }
    }
    ```

    生成嵌套类 `Fields`：

    ```java
    public static final class Fields {
        public static final String firstName = "firstName";
        public static final String lastName = "lastName";
        public static final String age = "age";
    }
    ```

    使用常量避免硬编码：

    ```java
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Person> query = cb.createQuery(Person.class);
    Root<Person> root = query.from(Person.class);
    query.select(root).where(cb.equal(root.get(Person.Fields.lastName), "Doe"));
    ```

    可通过 `innerTypeName` 自定义嵌套类名：

    ```java
    @FieldNameConstants(innerTypeName = "FieldConstants")
    ```

16. 能否回退 Lombok？

    **完全不必担心！**

    若未来想移除 Lombok，可使用其自带的 `delombok` 工具——将注解代码转换为等效的完整 Java 源码，直接替换原文件即可。此过程可集成到[构建流程](https://projectlombok.org/features/delombok.html)中。

17. 结论

    本文未涵盖 Lombok 所有功能，更多细节请参考[官方功能概览](https://projectlombok.org/features/all)。大多数功能支持自定义配置，内置配置系统可进一步优化体验。

    现在，是时候让 Lombok 加入你的 Java 开发工具箱，大幅提升生产力了！
