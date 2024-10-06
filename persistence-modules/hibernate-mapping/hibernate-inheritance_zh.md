# [Hibernate继承映射](https://www.baeldung.com/hibernate-inheritance)

1. 一览表

    关系数据库没有直接的方法将类层次结构映射到数据库表上。

    为了解决这个问题，JPA规范提供了几种策略：

    - MappedSuperclass – 父类，不能是实体
    - 单表——来自具有共同祖先的不同类的实体被放置在单个表中。
    - 连接表-每个类都有其表，查询子类实体需要连接表。
    - 每个类的表-类的所有属性都在其表中，因此不需要连接。

    每种策略都会产生不同的数据库结构。

    实体继承意味着在查询超类时，我们可以使用多态查询来检索所有子类实体。

    由于Hibernate是JPA的实现，它包含上述所有功能以及一些与继承相关的Hibernate特定功能。

    在接下来的章节中，我们将更详细地介绍可用的策略。

2. MappedSuperclass

    使用MappedSuperclass策略，继承只在类中明显，而不在实体模型中明显。

    让我们从创建一个将代表父类的Person类开始：

    ```java
    @MappedSuperclass
    public class Person {

        @Id
        private long personId;
        private String name;

        // constructor, getters, setters
    }
    ```

    请注意，该类不再有@Entity注释，因为它不会自行在数据库中持久化。

    接下来，让我们添加一个员工子类：

    ```java
    @Entity
    public class MyEmployee extends Person {
        private String company;
        // constructor, getters, setters
    }
    ```

    在数据库中，这将对应一个MyEmployee表，其中包含子类的声明字段和继承字段的三列。

    如果我们使用这种策略，祖先就不能包含与其他实体的关联。

3. Single Table

    单表策略为每个类层次结构创建一个表。如果我们没有明确指定策略，JPA也会默认选择此策略。

    我们可以通过将@Inheritance注释添加到超类来定义我们想要使用的策略：

    ```java
    @Entity
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    public class MyProduct {
        @Id
        private long productId;
        private String name;

        // constructor, getters, setters
    }
    ```

    实体的标识符也在超类中定义。

    然后我们可以添加子类实体：

    ```java
    @Entity
    public class Book extends MyProduct {
        private String author;
    }

    @Entity
    public class Pen extends MyProduct {
        private String color;
    }
    ```

    1. 判别价值

        由于所有实体的记录都将在同一表中，因此Hibernate需要一种方法来区分它们。

        默认情况下，这是通过一个名为DTYPE的判别器列完成的，该列将实体的名称作为值。

        要自定义判别器列，我们可以使用@DiscriminatorColumn注释：

        ```java
        @Entity(name="products")
        @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
        @DiscriminatorColumn(name="product_type",
        discriminatorType = DiscriminatorType.INTEGER)
        public class MyProduct {
            // ...
        }
        ```

        在这里，我们选择用一个名为product_type的整数列来区分MyProduct子类实体。

        接下来，我们需要告诉Hibernate每个子类记录对product_type列的值：

        ```java
        @Entity
        @DiscriminatorValue("1")
        public class Book extends MyProduct {
            // ...
        }

        @Entity
        @DiscriminatorValue("2")
        public class Pen extends MyProduct {
            // ...
        }
        ```

        Hibernate添加了注释可以采取的另外两个预定义值——空值和非空值：

        - @DiscriminatorValue(“null”) 表示任何没有判别器值的行都将使用此注释映射到实体类；这可以应用于层次结构的根类。
        - @@DiscriminatorValue(“not null”) - 任何具有判别器值与实体定义关联的行都将映射到具有此注释的类。
        我们也可以使用特定于Hibernate的@DiscriminatorFormula注释来确定微分值，而不是列：

        ```java
        @Entity
        @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
        @DiscriminatorFormula("case when author is not null then 1 else 2 end")
        public class MyProduct { ... }
        ```

        这种策略具有多态查询性能的优势，因为在查询父实体时只需要访问一个表。

        另一方面，这也意味着我们不能再在子类实体属性上使用NOT NULL约束。

4. Joined Table

    使用此策略，层次结构中的每个类都映射到其表。唯一反复出现在所有表中的列是标识符，该标识符将在需要时用于连接它们。

    让我们创建一个使用此策略的超级类：

    ```java
    @Entity
    @Inheritance(strategy = InheritanceType.JOINED)
    public class Animal {
        @Id
        private long animalId;
        private String species;

        // constructor, getters, setters 
    }
    ```

    然后我们可以简单地定义一个子类：

    ```java
    @Entity
    public class Pet extends Animal {
        private String name;

        // constructor, getters, setters
    }
    ```

    两个表都将有一个animalId标识符列。

    宠物实体的主密钥对其父实体的主密钥也有一个外来密钥约束。

    为了自定义此列，我们可以添加@PrimaryKeyJoinColumn注释：

    ```java
    @Entity
    @PrimaryKeyJoinColumn(name = "petId")
    public class Pet extends Animal {
        // ...
    }
    ```

    这种继承映射方法的缺点是，检索实体需要表之间的连接，这可能会导致大量记录的性能降低。

    查询父类时，连接次数更高，因为它将与每个相关子类连接——因此，我们想要检索记录的层次结构越高，性能就越有可能受到影响。

5. Table per Class

    每类表策略将每个实体映射到其表，该表包含实体的所有属性，包括继承的属性。

    生成的模式与使用@MappedSuperclass的模式相似。但每个类的表确实会为父类定义实体，因此允许关联和多态查询。

    要使用此策略，我们只需要将@Inheritance注释添加到基类中：

    ```java
    @Entity
    @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
    public class Vehicle {
        @Id
        private long vehicleId;

        private String manufacturer;

        // standard constructor, getters, setters
    }
    ```

    然后我们可以以标准方式创建子类。

    这与仅仅在没有继承的情况下映射每个实体没有太大区别。在查询基类时，区别是显而易见的，基类也会通过在后台使用UNION语句来返回所有子类记录。

    在选择这种策略时，使用UNION也可能导致性能不佳。另一个问题是，我们不能再使用身份密钥生成。

6. Polymorphic查询

    如前所述，查询基类也会检索所有子类实体。

    让我们通过JUnit测试来了解这种行为：

    ```java
    @Test
    public void givenSubclasses_whenQuerySingleTableSuperclass_thenOk() {
        Book book = new Book(1, "1984", "George Orwell");
        session.save(book);
        Pen pen = new Pen(2, "my pen", "blue");
        session.save(pen);

        assertThat(session.createQuery("from MyProduct")
        .getResultList()).size()).isEqualTo(2);
    }
    ```

    在本例中，我们创建了两个Book和Pen对象，然后查询了它们的超类MyProduct，以验证我们将检索两个对象。

    Hibernate还可以查询非实体但由实体类扩展或实现的接口或基类。

    让我们看看使用我们的@MappedSuperclass示例的JUnit测试：

    ```java
    @Test
    public void givenSubclasses_whenQueryMappedSuperclass_thenOk() {
        MyEmployee emp = new MyEmployee(1, "john", "baeldung");
        session.save(emp);

        assertThat(session.createQuery(
        "from com.baeldung.hibernate.pojo.inheritance.Person")
        .getResultList())
        .hasSize(1);
    }
    ```

    请注意，这也适用于任何超类或接口，无论它是否是@MappedSuperclass。与通常的HQL查询的区别在于，我们必须使用完全限定的名称，因为它们不是Hibernate管理的实体。

    如果我们不希望子类被这种类型的查询返回，我们只需要在其定义中添加Hibernate @Polymorphism注释，类型为EXPLICIT：

    ```java
    @Entity
    @Polymorphism(type = PolymorphismType.EXPLICIT)
    public class Bag implements Item { ...}
    ```

    在这种情况下，在查询项目时，由于Bag类是显式的，它将抛出一个异常，表明项目实体无法解析。由于这一限制，我们必须定义另一个具有@Polymorphism（类型=多态性类型.IMPLICIT）的类。当我们查询项目时，它将搜索实现该接口的两个对象中的记录。

7. 结论

    在本文中，我们展示了在Hibernate中映射继承的不同策略。
