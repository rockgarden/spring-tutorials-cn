# [在 JPA 中持久化枚举](https://www.baeldung.com/jpa-persisting-enums-in-jpa)

1. 一览表

    在JPA版本2.0及以下版本中，没有将Enum值映射到数据库列的便捷方法。每个选项都有其局限性和缺点。这些问题可以通过使用JPA 2.1功能来避免。在最新版本的JPA 3.1中，在持久的列词方面，没有为现有的更改添加重要功能。

    在本教程中，我们将看看使用JPA在数据库中持久化枚举的不同可能性。我们还将描述它们的优缺点，并提供简单的代码示例。

2. 使用@Enumerated Annotation

    在2.1之前，在JPA中将枚举值映射到其数据库表示的最常见选项是使用@Enumerated注释。通过这种方式，我们可以指示JPA提供程序将枚举转换为其序数或字符串值。

    我们将在本节中探讨这两种选项。

    但让我们先创建一个简单的@Entity，我们将在本教程中使用它：

    ```java
    @Entity
    public class Article {
        @Id
        private int id;

        private String title;

        // standard constructors, getters and setters
    }
    ```

    1. 映射序数值

        如果我们将@Enumerated（EnumType.ORDINAL）注释放在枚举字段上，JPA将在数据库中保留给定实体时使用[Enum.ordinal()](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Enum.html#ordinal())值。

        让我们来介绍一下第一个枚举：

        ```java
        public enum Status {
            OPEN, REVIEW, APPROVED, REJECTED;
        }
        ```

        接下来，让我们将其添加到文章类中，并用@Enumrated（EnumType.ORDINAL）注释：

        ```java
        @Entity
        public class Article {
            @Id
            private int id;

            private String title;

            @Enumerated(EnumType.ORDINAL)
            private Status status;
        }
        ```

        现在，当坚持一个文章实体时：

        ```java
        Article article = new Article();
        article.setId(1);
        article.setTitle("ordinal title");
        article.setStatus(Status.OPEN);
        ```

        JPA将触发以下SQL语句：

        ```sql
        insert
        into
            Article
            (status, title, id)
        values
            (?, ?, ?)
        binding parameter [1] as [INTEGER] - [0]
        binding parameter [2] as [VARCHAR] - [ordinal title]
        binding parameter [3] as [INTEGER] - [1]
        ```

        当我们需要修改枚举时，这种映射会出现问题。如果我们在中间添加一个新值或重新排列枚举的顺序，我们将破坏现有的数据模型。

        此类问题可能很难发现，也存在问题，因为我们必须更新所有数据库记录。

    2. 映射字符串值

        类似地，如果我们用@Enumrated（EnumType.STRING）注释枚举字段，JPA将在存储实体时使用[Enum.name()](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Enum.html#name())值。

        让我们创建第二个枚举：

        ```java
        public enum Type {
            INTERNAL, EXTERNAL;
        }
        ```

        让我们把它添加到我们的文章类中，并用@Enumerated(EnumType.STRING)进行注释：

        ```java
        @Entity
        public class Article {
            @Id
            private int id;

            private String title;

            @Enumerated(EnumType.ORDINAL)
            private Status status;

            @Enumerated(EnumType.STRING)
            private Type type;
        }
        ```

        现在，当坚持一个文章实体时：

        ```java
        Article article = new Article();
        article.setId(2);
        article.setTitle("string title");
        article.setType(Type.EXTERNAL);
        ```

        JPA将执行以下SQL语句：

        ```sql
        insert
        into
            Article
            (status, title, type, id)
        values
            (?, ?, ?, ?)
        binding parameter [1] as [INTEGER] - [null]
        binding parameter [2] as [VARCHAR] - [string title]
        binding parameter [3] as [VARCHAR] - [EXTERNAL]
        binding parameter [4] as [INTEGER] - [2]
        ```

        使用@Enumerated（EnumType.STRING），我们可以安全地添加新的枚举值或更改枚举的顺序。然而，重命名枚举值仍然会破坏数据库数据。

        此外，尽管与@Enumrated(EnumType.ORDINAL)选项相比，这种数据表示的可读性要高得多，但它也消耗的空间比必要的要多得多。当我们需要处理大量数据时，这可能是一个重大问题。

3. 使用@PostLoad和@PrePersist注释

    我们必须处理数据库中持久枚举的另一个选项是使用标准的JPA回调方法。我们可以在@PostLoad和@PrePersist事件中来回映射我们的枚举。

    这个想法是在一个实体中有两个属性。第一个映射到数据库值，第二个是@Transient字段，包含实枚举值。然后，业务逻辑代码使用瞬态属性。

    为了更好地理解这个概念，让我们创建一个新的枚举，并在映射逻辑中使用其int值：

    ```java
    public enum Priority {
        LOW(100), MEDIUM(200), HIGH(300);

        private int priority;

        private Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public static Priority of(int priority) {
            return Stream.of(Priority.values())
            .filter(p -> p.getPriority() == priority)
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
        }
    }
    ```

    我们还添加了Priority.of（）方法，以便根据其int值轻松获取优先级实例。

    现在，要在我们的文章类中使用它，我们需要添加两个属性并实现回调方法：

    ```java
    @Entity
    public class Article {

        @Id
        private int id;

        private String title;

        @Enumerated(EnumType.ORDINAL)
        private Status status;

        @Enumerated(EnumType.STRING)
        private Type type;

        @Basic
        private int priorityValue;

        @Transient
        private Priority priority;

        @PostLoad
        void fillTransient() {
            if (priorityValue > 0) {
                this.priority = Priority.of(priorityValue);
            }
        }

        @PrePersist
        void fillPersistent() {
            if (priority != null) {
                this.priorityValue = priority.getPriority();
            }
        }
    }
    ```

    现在，当坚持一个文章实体时：

    ```java
    Article article = new Article();
    article.setId(3);
    article.setTitle("callback title");
    article.setPriority(Priority.HIGH);
    ```

    JPA将触发以下SQL查询：

    ```sql
    insert
    into
        Article
        (priorityValue, status, title, type, id)
    values
        (?, ?, ?, ?, ?)
    binding parameter [1] as [INTEGER] - [300]
    binding parameter [2] as [INTEGER] - [null]
    binding parameter [3] as [VARCHAR] - [callback title]
    binding parameter [4] as [VARCHAR] - [null]
    binding parameter [5] as [INTEGER] - [3]
    ```

    尽管与之前描述的解决方案相比，此选项在选择数据库值的表示方面提供了更大的灵活性，但它并不理想。在实体中，有两个属性代表单个枚举，这感觉不对。此外，如果我们使用这种类型的映射，我们无法在JPQL查询中使用枚举的值。

4. 使用JPA 2.1 @Converter注释

    为了克服上述解决方案的局限性，JPA 2.1版本引入了一个新的标准化API，该API可用于将实体属性转换为数据库值，反之亦然。我们需要做的就是创建一个实现jakarta.persistence.AttributeConverter的新类，并用@Converter注释它。在本例中，我们将使用JPA 3.1。命名空间从javax迁移到jakarta。

    让我们看看一个实际的例子。

    首先，我们将创建一个新的枚举：

    ```java
    public enum Category {
        SPORT("S"), MUSIC("M"), TECHNOLOGY("T");

        private String code;

        private Category(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }
    ```

    我们还需要将其添加到文章类中：

    ```java
    @Entity
    public class Article {

        @Id
        private int id;

        private String title;

        @Enumerated(EnumType.ORDINAL)
        private Status status;

        @Enumerated(EnumType.STRING)
        private Type type;

        @Basic
        private int priorityValue;

        @Transient
        private Priority priority;

        private Category category;
    }
    ```

    现在让我们创建一个新的类别转换器：

    ```java
    @Converter(autoApply = true)
    public class CategoryConverter implements AttributeConverter<Category, String> {

        @Override
        public String convertToDatabaseColumn(Category category) {
            if (category == null) {
                return null;
            }
            return category.getCode();
        }

        @Override
        public Category convertToEntityAttribute(String code) {
            if (code == null) {
                return null;
            }

            return Stream.of(Category.values())
            .filter(c -> c.getCode().equals(code))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
        }
    }
    ```

    我们已将@Converter的autoApply值设置为true，以便JPA将自动将转换逻辑应用于类别类型的所有映射属性。否则，我们将不得不将@Converter注释直接放在实体的字段上。

    现在让我们坚持一个文章实体：

    ```java
    Article article = new Article();
    article.setId(4);
    article.setTitle("converted title");
    article.setCategory(Category.MUSIC);
    ```

    然后JPA将执行以下SQL语句：

    ```sql
    insert
    into
        Article
        (category, priorityValue, status, title, type, id)
    values
        (?, ?, ?, ?, ?, ?)
    Converted value on binding : MUSIC -> M
    binding parameter [1] as [VARCHAR] - [M]
    binding parameter [2] as [INTEGER] - [0]
    binding parameter [3] as [INTEGER] - [null]
    binding parameter [4] as [VARCHAR] - [converted title]
    binding parameter [5] as [VARCHAR] - [null]
    binding parameter [6] as [INTEGER] - [4]
    ```

    正如我们所看到的，如果我们使用AttributeConverter接口，我们可以简单地设置自己的规则，将枚举转换为相应的数据库值。此外，我们可以安全地添加新的枚举值或更改现有枚举值，而不会破坏已经存在的数据。

    整体解决方案易于实施，并解决了前几节中提出的选项的所有缺点。

5. 在JPQL中使用列舉

    现在让我们看看在JPQL查询中使用枚举有多容易。

    要查找所有具有Category.SPORT类别的文章实体，我们需要执行以下语句：

    ```java
    String jpql = "select a from Article a where a.category = com.baeldung.jpa.enums.Category.SPORT";

    List<Article> articles = em.createQuery(jpql, Article.class).getResultList();
    ```

    需要注意的是，在这种情况下，我们需要使用完全限定的枚举名称。

    当然，我们不局限于静态查询。

    使用命名参数是完全合法的：

    ```java
    String jpql = "select a from Article a where a.category = :category";

    TypedQuery<Article> query = em.createQuery(jpql, Article.class);
    query.setParameter("category", Category.TECHNOLOGY);

    List<Article> articles = query.getResultList();
    ```

    这个例子提供了一种非常方便的方式来形成动态查询。

    此外，我们不需要使用完全限定的名字。

6. 结论

    在本文中，我们介绍了在数据库中持久化枚举值的各种方法。我们展示了在2.0及更低版本中使用JPA时的选项，以及JPA 2.1及更高版本中可用的新API。

    值得注意的是，这些并不是处理JPA中枚举的唯一可能性。一些数据库，如[PostgreSQL](https://www.postgresql.org/docs/current/datatype-enum.html)，提供专用列类型来存储枚举值。然而，这种解决方案超出了本文的范围。
