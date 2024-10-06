# [定义JPA实体](https://www.baeldung.com/jpa-entities)

1. 介绍

    在本教程中，我们将学习实体的基础知识，以及在JPA中定义和自定义实体的各种注释。

2. 实体

    JPA中的实体只不过是POJO，代表可以在数据库中持久化的数据。实体表示存储在数据库中的表。实体的每个实例代表表中的一行。

    1. 实体注释

        假设我们有一个名为Student的POJO，它代表学生的数据，我们想将其存储在数据库中：

        ```java
        public class Student {
            // fields, getters and setters
        }
        ```

        为此，我们应该定义一个实体，以便JPA了解它。

        因此，让我们使用@Entity注释来定义它。我们必须在类级别指定此注释。我们还必须确保实体有一个无arg构造函数和一个主键：

        ```java
        @Entity
        public class Student {
            // fields, getters and setters
        }
        ```

        实体名称默认为类名称。我们可以使用名称元素来更改它的名称：

        ```java
        @Entity(name="student")
        public class Student {
            // fields, getters and setters
        }
        ```

        由于各种JPA实现将尝试将我们的实体子类化以提供其功能，因此实体类不得声明为最终类。

    2. Id注释

        每个JPA实体都必须有一个唯一标识它的主密钥。@Id 注释定义了主键。我们可以以不同的方式生成标识符，这些标识符由@GeneratedValue注释指定。

        我们可以从具有战略元素的四种id生成策略中进行选择。值可以是自动、表格、序列或身份：

        ```java
        @Entity
        public class Student {
            @Id
            @GeneratedValue(strategy=GenerationType.AUTO)
            private Long id;

            private String name;
            
            // getters and setters
        }
        ```

        如果我们指定GenerationType.AUTO，JPA提供商将使用任何它想要生成标识符的策略。

        如果我们对实体的字段进行注释，JPA提供商将使用这些字段来获取和设置实体的状态。除了字段访问外，我们还可以进行属性访问或混合访问，这使我们能够在同一实体中同时使用字段和属性访问。

    3. 表格注释

        在大多数情况下，数据库中的表名和实体名是不一样的。

        在这些情况下，我们可以使用@Table注释来指定表名：

        ```java
        @Entity
        @Table(name="STUDENT")
        public class Student {
            // fields, getters and setters 
        }
        ```

        我们还可以使用模式元素来提及模式：

        ```java
        @Entity
        @Table(name="STUDENT", schema="SCHOOL")
        public class Student {
            // fields, getters and setters
        }
        ```

        模式名称有助于区分一组表和另一组表。

        如果我们不使用@Table注释，表的名称将是实体的名称。

    4. 列注释

        就像@Table注释一样，我们可以使用@Column注释来提及表格中列的详细信息。

        @Column注释有许多元素，如名称、长度、可空和唯一：

        ```java
        @Entity
        @Table(name="STUDENT")
        public class Student {
            @Id
            @GeneratedValue(strategy=GenerationType.AUTO)
            private Long id;

            @Column(name="STUDENT_NAME", length=50, nullable=false, unique=false)
            private String name;
            
            // other fields, getters and setters
        }
        ```

        名称元素指定了表格中列的名称。长度元素指定其长度。可废元素指定列是否可空，唯一元素指定列是否唯一。

        如果我们不指定此注释，表格中的列名将是字段的名称。

    5. 瞬态注释

        有时，我们可能想让一个字段不持久。我们可以使用@Transient注释来做到这一点。它指定该字段不会被持久化。

        例如，我们可以从出生日期计算学生的年龄。

        因此，让我们用@Transient注释注释字段年龄：

        ```java
        @Entity
        @Table(name="STUDENT")
        public class Student {
            @Id
            @GeneratedValue(strategy=GenerationType.AUTO)
            private Long id;

            @Column(name="STUDENT_NAME", length=50, nullable=false)
            private String name;
            
            @Transient
            private Integer age;
            
            // other fields, getters and setters
        }
        ```

        因此，字段年龄不会在表格中持续存在。

    6. 时间注释

        在某些情况下，我们可能不得不在表中保存时间值。

        为此，我们有@Temporal注释：

        ```java
        @Entity
        @Table(name="STUDENT")
        public class Student {
            @Id
            @GeneratedValue(strategy=GenerationType.AUTO)
            private Long id;

            @Column(name="STUDENT_NAME", length=50, nullable=false, unique=false)
            private String name;
            
            @Transient
            private Integer age;
            
            @Temporal(TemporalType.DATE)
            private Date birthDate;
            
            // other fields, getters and setters
        }
        ```

        然而，通过JPA 3.1，我们也支持java.time.LocalDate、java.time.LocalTime、java.time.LocalDateTime、java.time.OffsetTime和java.time.OffsetDateTime。

    7. 枚举注释

        有时，我们可能想要坚持Java枚举类型。

        我们可以使用@Enumerated注释来指定枚举是否应按名称或序数（默认）持久化：

        ```java
        public enum Gender {
            MALE,
            FEMALE
        }

        @Entity
        @Table(name="STUDENT")
        public class Student {
            @Id
            @GeneratedValue(strategy=GenerationType.AUTO)
            private Long id;

            @Column(name="STUDENT_NAME", length=50, nullable=false, unique=false)
            private String name;
            
            @Transient
            private Integer age;
            
            @Temporal(TemporalType.DATE)
            private Date birthDate;
            
            @Enumerated(EnumType.STRING)
            private Gender gender;
            
            // other fields, getters and setters
        }
        ```

        如果我们要通过枚举的序数来保持性别，我们根本不需要指定@Enumerated注释。

        然而，为了通过枚举名称保持性别，我们使用EnumType.STRING配置了注释。

3. 结论

    在这篇文章中，我们了解了什么是JPA实体以及如何创建它们。我们还了解了可用于进一步自定义实体的不同注释。
