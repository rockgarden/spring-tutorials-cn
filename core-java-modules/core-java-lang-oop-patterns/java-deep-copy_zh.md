# [如何在 Java 中实现对象的深拷贝](https://www.baeldung.com/java-deep-copy)  

核心 Java

Apache Commons Lang Java 接口

序列化

1. 引言

    当我们需要在 Java 中复制一个对象时，有两种方式需要考虑：**浅拷贝（shallow copy）** 和 **深拷贝（deep copy）**。

    使用**浅拷贝**时，我们仅复制字段的值，因此副本可能仍然依赖于原始对象。而使用**深拷贝**时，我们会确保对象图中的所有对象都被递归地复制，从而使副本独立于任何先前存在的、可能被修改的对象。

    在本教程中，我们将比较这两种方法，并学习四种实现深拷贝的方式。

2. Maven 配置

    我们将使用三个 Maven 依赖项：Gson、Jackson 和 Apache Commons Lang，来测试不同的深拷贝方法。

    将以下依赖添加到你的 `pom.xml` 文件中：

    ```xml
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.14.0</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.17.2</version>
    </dependency>
    ```

    Gson、Jackson 和 Apache Commons Lang 的最新版本可以在 [Maven Central](https://search.maven.org/) 上找到。

3. 模型类

    为了比较不同的对象拷贝方法，我们需要定义两个类：

    ```java
    class Address {
        private String street;
        private String city;
        private String country;

        // 标准构造函数、getter 和 setter 方法
    }
    ```

    ```java
    class User {
        private String firstName;
        private String lastName;
        private Address address;

        // 标准构造函数、getter 和 setter 方法
    }
    ```

4. 浅拷贝

    浅拷贝是指只复制对象字段的值：

    ```java
    @Test
    public void whenShallowCopying_thenObjectsShouldNotBeSame() {
        Address address = new Address("Downing St 10", "London", "England");
        User pm = new User("Prime", "Minister", address);
        
        User shallowCopy = new User(
            pm.getFirstName(), 
            pm.getLastName(), 
            pm.getAddress()
        );

        assertThat(shallowCopy).isNotSameAs(pm);
    }
    ```

    在这个例子中，`pm != shallowCopy`，说明它们是不同的对象。但问题在于：如果我们修改原始 `address` 的属性，`shallowCopy` 中的 `address` 也会受到影响。

    如果 `Address` 是不可变类，这倒不是问题，但它不是：

    ```java
    @Test
    public void whenModifyingOriginalObject_ThenCopyShouldChange() {
        Address address = new Address("Downing St 10", "London", "England");
        User pm = new User("Prime", "Minister", address);
        User shallowCopy = new User(
            pm.getFirstName(), 
            pm.getLastName(), 
            pm.getAddress()
        );

        address.setCountry("Great Britain");
        
        assertThat(shallowCopy.getAddress().getCountry())
            .isEqualTo(pm.getAddress().getCountry());
    }
    ```

    输出结果为 `true`，说明浅拷贝的对象仍然共享同一个 `Address` 实例。

5. 深拷贝

    深拷贝可以解决这个问题。其优势在于，对象图中的每个可变对象都会被递归复制。

    由于副本不依赖于之前创建的任何可变对象，因此不会像浅拷贝那样意外被修改。

    接下来我们将讨论几种实现深拷贝的方法，并验证其独立性。

    1. 构造函数拷贝（Copy Constructor）

        第一种方法是使用**拷贝构造函数**：

        ```java
        public Address(Address that) {
            this(that.getStreet(), that.getCity(), that.getCountry());
        }

        public User(User that) {
            this(that.getFirstName(), that.getLastName(), new Address(that.getAddress()));
        }
        ```

        注意：我们没有为 `String` 字段创建新的实例，因为 `String` 是不可变类，无需深拷贝。

        测试验证：

        ```java
        @Test
        public void whenModifyingOriginalObject_thenCopyShouldNotChange() {
            Address address = new Address("Downing St 10", "London", "England");
            User pm = new User("Prime", "Minister", address);
            User deepCopy = new User(pm); // 使用拷贝构造函数

            address.setCountry("Great Britain");

            assertNotEquals(
                pm.getAddress().getCountry(), 
                deepCopy.getAddress().getCountry()
            );
        }
        ```

        测试通过，说明深拷贝成功，副本不受原始对象影响。

    2. Cloneable 接口

        第二种方法是实现 `Cloneable` 接口并重写 `clone()` 方法。

        首先在 `Address` 类中实现：

        ```java
        @Override
        public Object clone() {
            try {
                return (Address) super.clone();
            } catch (CloneNotSupportedException e) {
                return new Address(this.street, this.city, this.country);
            }
        }
        ```

        然后在 `User` 类中实现：

        ```java
        @Override
        public Object clone() {
            User user = null;
            try {
                user = (User) super.clone(); // super.clone() 是浅拷贝
            } catch (CloneNotSupportedException e) {
                user = new User(this.firstName, this.lastName, this.address);
            }
            user.address = (Address) this.address.clone(); // 手动深拷贝 address
            return user;
        }
        ```

        测试验证：

        ```java
        @Test
        public void whenModifyingOriginalObject_thenCloneCopyShouldNotChange() {
            Address address = new Address("Downing St 10", "London", "England");
            User pm = new User("Prime", "Minister", address);
            User deepCopy = (User) pm.clone();

            address.setCountry("Great Britain");

            assertThat(deepCopy.getAddress().getCountry())
                .isNotEqualTo(pm.getAddress().getCountry());
        }
        ```

        测试通过，说明 `clone()` 实现了深拷贝。

        > ⚠️ 注意：`Object.clone()` 是浅拷贝，必须手动对可变字段进行深拷贝。

6. 使用外部库

    前面的方法虽然有效，但在以下场景中可能不适用：

    - 无法修改源码（如第三方类）
    - 对象图过于复杂
    - 没时间为每个类添加构造函数或实现 `clone()`

    此时，我们可以借助**序列化与反序列化**机制实现深拷贝：将对象序列化为字节流或 JSON，再反序列化为新对象，从而获得完全独立的副本。

    1. Apache Commons Lang

        Apache Commons Lang 提供了 `SerializationUtils.clone()` 方法，它通过序列化实现深拷贝。

        **前提**：所有类必须实现 `Serializable` 接口。

        ```java
        @Test
        public void whenModifyingOriginalObject_thenCommonsCloneShouldNotChange() {
            Address address = new Address("Downing St 10", "London", "England");
            User pm = new User("Prime", "Minister", address);
            User deepCopy = (User) SerializationUtils.clone(pm);

            address.setCountry("Great Britain");

            assertThat(deepCopy.getAddress().getCountry())
                .isNotEqualTo(pm.getAddress().getCountry());
        }
        ```

        > ✅ 优点：简单直接  
        > ❌ 缺点：要求所有类实现 `Serializable`，否则抛出 `SerializationException`

    2. 使用 Gson 进行 JSON 序列化

        Gson 是 Google 提供的 JSON 序列化库，无需实现 `Serializable` 接口。

        ```java
        @Test
        public void whenModifyingOriginalObject_thenGsonCloneShouldNotChange() {
            Address address = new Address("Downing St 10", "London", "England");
            User pm = new User("Prime", "Minister", address);
            Gson gson = new Gson();
            User deepCopy = gson.fromJson(gson.toJson(pm), User.class);

            address.setCountry("Great Britain");

            assertThat(deepCopy.getAddress().getCountry())
                .isNotEqualTo(pm.getAddress().getCountry());
        }
        ```

        > ✅ 优点：无需实现 `Serializable`，支持大多数 POJO  
        > ❌ 缺点：不支持 `transient` 字段的保留（会被忽略）

    3. 使用 Jackson 进行 JSON 序列化

        Jackson 是另一个流行的 JSON 库，用法类似 Gson。

        **注意**：Jackson 要求类有**默认无参构造函数**（或通过注解指定构造函数）。

        ```java
        @Test
        public void whenModifyingOriginalObject_thenJacksonCopyShouldNotChange() 
            throws IOException {
            Address address = new Address("Downing St 10", "London", "England");
            User pm = new User("Prime", "Minister", address);
            ObjectMapper objectMapper = new ObjectMapper();
            
            User deepCopy = objectMapper
                .readValue(objectMapper.writeValueAsString(pm), User.class);

            address.setCountry("Great Britain");

            assertThat(deepCopy.getAddress().getCountry())
                .isNotEqualTo(pm.getAddress().getCountry());
        }
        ```

        > ✅ 优点：性能优秀，广泛用于 Spring 生态  
        > ❌ 缺点：需要默认构造函数，对复杂泛型支持需额外配置

7. 总结

    选择哪种深拷贝方式取决于你的具体场景：

    | 方法 | 适用场景 | 优点 | 缺点 |
    |------|--------|------|------|
    | **拷贝构造函数** | 可控制源码、结构简单 | 类型安全、性能高 | 需手动编写，复杂对象图工作量大 |
    | **Cloneable 接口** | 已有继承结构 | JDK 原生支持 | 易出错，`clone()` 设计有缺陷 |
    | **Apache Commons Lang** | 所有类可序列化 | 简单易用 | 必须实现 `Serializable` |
    | **Gson / Jackson** | 第三方类、复杂结构 | 无需修改源码、通用性强 | 依赖 JSON 序列化规则，性能略低 |

    **建议**：

    - 如果你能控制类的设计，优先使用**拷贝构造函数**。
    - 如果对象图复杂且无法修改源码，推荐使用 **Gson 或 Jackson** 实现 JSON 序列化拷贝。
    - 尽量避免使用 `Cloneable`，因其设计存在争议。
