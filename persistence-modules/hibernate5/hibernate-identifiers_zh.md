# [Hibernate/JPA中的标识符概述](https://www.baeldung.com/hibernate-identifiers)

1. 一览表

    Hibernate中的标识符表示实体的主键。这意味着这些值是唯一的，因此它们可以识别特定实体，它们不是空的，也不会被修改。

    Hibernate提供了几种定义标识符的不同方法。在本文中，我们将回顾使用库映射实体ID的每种方法。

2. 简单的标识符

    定义标识符的最直接方法是使用@Id注释。

    简单id使用@Id映射到以下类型之一的单个属性：Java原始和原始包装类型、字符串、日期、BigDecimal和BigInteger。

    让我们看看用类型为长的主键定义实体的快速示例：

    ```java
    @Entity
    public class Student {

        @Id
        private long studentId;
        
        // standard constructor, getters, setters
    }
    ```

3. 生成的标识符

    如果我们想要自动生成主键值，我们可以添加@GeneratedValue注释。

    这可以使用四种生成类型：自动、标识、序列和表。

    如果我们没有明确指定值，生成类型将默认为AUTO。

    1. 自动生成

        如果我们使用默认生成类型，持久性提供程序将根据主键属性的类型确定值。这种类型可以是数字或UUID。

        对于数值，生成基于序列或表生成器，而UUID值将使用UUIDGenerator。

        让我们首先使用AUTO生成策略映射一个实体主密钥：

        ```java
        @Entity
        public class Student {

            @Id
            @GeneratedValue
            private long studentId;

            // ...
        }
        ```

        在这种情况下，主键值在数据库级别将是唯一的。

        现在我们将看看在Hibernate 5中引入的UUIDGenerator。

        为了使用此功能，我们只需要用@GeneratedValue注释声明UUID类型的id：

        ```java
        @Entity
        public class Course {

            @Id
            @GeneratedValue
            private UUID courseId;

            // ...
        }
        ```

        Hibernate将生成一个ID，形式为“8dd5f315-9788-4d00-87bb-10eed9eff566”。

    2. 身份生成

        这种类型的生成依赖于IdentityGenerator，该生成器期望数据库中的身份列生成值。这意味着它们是自动增加的。

        要使用此生成类型，我们只需要设置策略参数：

        ```java
        @Entity
        public class Student {

            @Id
            @GeneratedValue (strategy = GenerationType.IDENTITY)
            private long studentId;

            // ...
        }
        ```

        需要注意的一点是，IDENTITY生成会禁用批处理更新。

    3. 序列生成

        要使用基于序列的id，Hibernate提供了SequenceStyleGenerator类。

        如果我们的数据库支持序列，此生成器会使用序列。如果不支持它们，它会切换到表格生成。

        为了自定义序列名称，我们可以使用带有SequenceStyleGenerator策略的@GenericGenerator注释：

        ```java
        @Entity
        public class User {
            @Id
            @GeneratedValue(generator = "sequence-generator")
            @GenericGenerator(
            name = "sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                @Parameter(name = "sequence_name", value = "user_sequence"),
                @Parameter(name = "initial_value", value = "4"),
                @Parameter(name = "increment_size", value = "1")
                }
            )
            private long userId;

            // ...
        }
        ```

        在本例中，我们还为序列设置了初始值，这意味着主键生成将从4开始。

        SEQUENCE是Hibernate文档推荐的生成类型。

        生成的值是每个序列唯一的。如果我们不指定序列名称，Hibernate将为不同类型重复使用相同的hibernate_sequence。

    4. 表生成

        TableGenerator使用一个底层数据库表，该表包含标识符生成值的段。

        让我们使用@TableGenerator注释自定义表名：

        ```java
        @Entity
        public class Department {
            @Id
            @GeneratedValue(strategy = GenerationType.TABLE,
            generator = "table-generator")
            @TableGenerator(name = "table-generator",
            table = "dep_ids",
            pkColumnName = "seq_id",
            valueColumnName = "seq_value")
            private long depId;

            // ...
        }
        ```

        在本例中，我们可以看到，我们还可以自定义其他属性，如pkColumnName和valueColumnName。

        然而，这种方法的缺点是它的扩展性不好，可能会对性能产生负面影响。

        总之，这四种生成类型将产生相似的值，但使用不同的数据库机制。

    5. 自定义生成器

        比方说，我们不想使用任何开箱即用的策略。为了做到这一点，我们可以通过实现IdentifierGenerator接口来定义我们的自定义生成器。

        我们将创建一个生成器，该生成器构建包含字符串前缀和数字的标识符：

        ```java
        public class MyGenerator
        implements IdentifierGenerator, Configurable {

            private String prefix;

            @Override
            public Serializable generate(
            SharedSessionContractImplementor session, Object obj) 
            throws HibernateException {

                String query = String.format("select %s from %s", 
                    session.getEntityPersister(obj.getClass().getName(), obj)
                    .getIdentifierPropertyName(),
                    obj.getClass().getSimpleName());

                Stream ids = session.createQuery(query).stream();

                Long max = ids.map(o -> o.replace(prefix + "-", ""))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0L);

                return prefix + "-" + (max + 1);
            }

            @Override
            public void configure(Type type, Properties properties, 
            ServiceRegistry serviceRegistry) throws MappingException {
                prefix = properties.getProperty("prefix");
            }
        }
        ```

        在本例中，我们覆盖了IdentifierGenerator接口中的generate（）方法。

        首先，我们想从prefix-XX形式的现有主键中找到最高数字。然后，我们将1添加到找到的最大数字中，并附加前缀属性以获取新生成的id值。

        我们的类还实现了可配置接口，以便我们可以在configure()方法中设置前缀属性值。

        接下来，让我们将此自定义生成器添加到实体中。

        为此，我们可以使用@GenericGenerator注释，其中包含我们生成器类的完整类名称的策略参数：

        ```java
        @Entity
        public class Product {

            @Id
            @GeneratedValue(generator = "prod-generator")
            @GenericGenerator(name = "prod-generator", 
            parameters = @Parameter(name = "prefix", value = "prod"), 
            strategy = "com.baeldung.hibernate.pojo.generator.MyGenerator")
            private String prodId;

            // ...
        }
        ```

        此外，请注意，我们将前缀参数设置为“prod”。

        让我们来看看一个快速的JUnit测试，以便更清楚地了解生成的id值：

        ```java
        @Test
        public void whenSaveCustomGeneratedId_thenOk() {
            Product product = new Product();
            session.save(product);
            Product product2 = new Product();
            session.save(product2);

            assertThat(product2.getProdId()).isEqualTo("prod-2");
        }
        ```

        在这里，使用“prod”前缀生成的第一个值是“prod-1”，后面是“prod-2”。

4. 复合标识符

    除了我们迄今为止看到的简单标识符外，Hibernate还允许我们定义复合标识符。

    复合ID由具有一个或多个持久属性的主键类表示。

    主键类必须满足几个条件：

    - 应该使用@EmbeddedId或@IdClass注释来定义它。
    - 它应该是公开的，可序列化，并有一个公开的无arg构造函数。
    - 最后，它应该实现equals（）和hashCode（）方法。

    类的属性可以是基本、复合或ManyToOne，同时避免集合和OneToOne属性。

    1. @EmbeddedId

        现在让我们来看看如何使用@EmbeddedId来定义ID。

        首先，我们需要一个用@Embeddable注释的主键类：

        ```java
        @Embeddable
        public class OrderEntryPK implements Serializable {

            private long orderId;
            private long productId;

            // standard constructor, getters, setters
            // equals() and hashCode() 
        }
        ```

        接下来，我们可以使用@EmbeddedId将OrderEntryPK类型的id添加到实体中：

        ```java
        @Entity
        public class OrderEntry {

            @EmbeddedId
            private OrderEntryPK entryId;

            // ...
        }
        ```

        让我们看看如何使用这种类型的复合ID来设置实体的主密钥：

        ```java
        @Test
        public void whenSaveCompositeIdEntity_thenOk() {
            OrderEntryPK entryPK = new OrderEntryPK();
            entryPK.setOrderId(1L);
            entryPK.setProductId(30L);

            OrderEntry entry = new OrderEntry();
            entry.setEntryId(entryPK);
            session.save(entry);

            assertThat(entry.getEntryId().getOrderId()).isEqualTo(1L);
        }
        ```

        在这里，OrderEntry对象有一个由两个属性组成的OrderEntryPK主ID：orderId和productId。

    2. @IdClass

        @IdClass注释与@EmbeddedId相似。与@IdClass的区别在于，属性是在主实体类中定义的，每个属性都使用@Id。主键类看起来和以前一样。

        让我们用@IdClass重写OrderEntry示例：

        ```java
        @Entity
        @IdClass(OrderEntryPK.class)
        public class OrderEntry {
            @Id
            private long orderId;
            @Id
            private long productId;

            // ...
        }
        ```

        然后，我们可以直接在OrderEntry对象上设置id值：

        ```java
        @Test
        public void whenSaveIdClassEntity_thenOk() {
            OrderEntry entry = new OrderEntry();
            entry.setOrderId(1L);
            entry.setProductId(30L);
            session.save(entry);

            assertThat(entry.getOrderId()).isEqualTo(1L);
        }
        ```

        请注意，对于这两种类型的复合ID，主键类也可以包含@ManyToOne属性：

        ```java
        @Embeddable
        public class OrderEntryPK implements Serializable {

            private long orderId;
            private long productId;
            @ManyToOne
            private User user;

            // ...
        }

        @Entity
        @IdClass(OrderEntryPK.class)
        public class OrderEntryIdClass {
            @Id
            private long orderId;
            @Id
            private long productId;
            @ManyToOne
            private User user;

            // ...
        }
        ```

        Hibernate还允许定义由@ManyToOne关联与@Id注释组合组成的主键。在这种情况下，实体类也应满足主键类的条件。

        然而，这种方法的缺点是实体对象和标识符之间没有分离。

5. 派生标识符

    派生标识符(Derived Identifiers)使用@MapsId注释从实体的关联中获取。

    首先，让我们创建一个UserProfile实体，该实体从与用户实体的一对一关联中获取其id：

    ```java
    @Entity
    public class UserProfile {

        @Id
        private long profileId;
        
        @OneToOne
        @MapsId
        private User user;

        // ...
    }
    ```

    接下来，让我们验证UserProfile实例的ID是否与其关联的用户实例相同：

    ```java
    @Test
    public void whenSaveDerivedIdEntity_thenOk() {
        User user = new User();
        session.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(user);
        session.save(profile);

        assertThat(profile.getProfileId()).isEqualTo(user.getUserId());
    }
    ```

6. 结论

    在本文中，我们看到了在Hibernate中定义标识符的多种方式。
