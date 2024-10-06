# [了解 JPA/Hibernate 关联](https://www.baeldung.com/jpa-hibernate-associations)

1. 一览表

    Java持久性API（JPA）是Java应用程序的对象关系映射（ORM）规范。此外，[Hibernate](https://www.baeldung.com/hibernate-5-spring)是JPA规范的流行实现之一。

    关联是ORM中的一个基本概念，允许我们定义实体之间的关系。在本教程中，我们将讨论JPA/Hibernate中单向和双向关联之间的区别。

2. 单向关联

    单向关联通常用于面向对象编程，以建立实体之间的关系。然而，重要的是要注意，在单向关联中，只有一个实体对另一个实体有引用。

    为了在Java中定义单向关联，我们可以使用@ManyToOne、@OneToMany、@OneToOne和@ManyToMany等注释。通过使用这些注释，我们可以在代码中的两个实体之间创建清晰且定义明确的关系。

    1. 一对多的关系

        在一对多关系中，一个实体对另一个实体的一个或多个实例具有引用。

        一个常见的例子是一个部门与其员工之间的关系。每个部门都有很多员工，但每个员工只属于一个部门。

        让我们来看看如何定义一对多单向关联：

        ```java
        @Entity
        public class Department {

            @Id
            private Long id;
        
            @OneToMany
            @JoinColumn(name = "department_id")
            private List<Employee> employees;
        }

        @Entity
        public class Employee {

            @Id
            private Long id;
        }
        ```

        在这里，部门实体引用了员工实体的列表。@OneToMany注释指定这是一个一对多关联。@JoinColumn注释指定了员工表中的外键列，引用了部门表。

    2. 多对一的关系

        在多对一关系中，一个实体的许多实例与另一个实体的一个实例相关联。

        例如，让我们考虑一下学生和学校。每个学生只能注册一所学校，但每所学校可以有多个学生。

        让我们来看看如何定义多对一的单向关联：

        ```java
        @Entity
        public class Student {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            private String name;

            @ManyToOne
            @JoinColumn(name = "school_id")
            private School school;
        }

        @Entity
        public class School {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            private String name;
        }
        ```

        在这种情况下，我们在学生和学校实体之间有一个多对一的单向关联。@ManyToOne注释指定每个学生只能注册一所学校，@JoinColumn注释指定了加入学生和学校实体的外键列名称。

    3. 一对一的关系

        在一对一关系中，一个实体的实例只与另一个实体的一个实例相关联。

        一个常见的例子是员工和停车点之间的关系。每个员工都有一个停车位，每个停车位都属于一名员工。

        让我们来看看如何定义一对一的单向关联：

        ```java
        @Entity
        public class Employee {

            @Id
            private Long id;
        
            @OneToOne
            @JoinColumn(name = "parking_spot_id")
            private ParkingSpot parkingSpot;

        }

        @Entity
        public class ParkingSpot {

            @Id
            private Long id;

        }
        ```

        在这里，员工实体引用了ParkingSpot实体。@OneToOne注释指定这是一个一对一的关联。@JoinColumn注释指定了员工表中引用ParkingSpot表的外键列

    4. 多对多关系

        在多对多关系中，一个实体的许多实例与另一个实体的许多实例相关联。

        假设我们有两个实体——书和作者。每本书可以有多位作者，每位作者可以写多本书。在JPA中，这种关系使用@ManyToMany注释表示。

        让我们来看看如何定义多对多单向关联：

        ```java
        @Entity
        public class Book {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            private String title;

            @ManyToMany
            @JoinTable(name = "book_author",
                    joinColumns = @JoinColumn(name = "book_id"),
                    inverseJoinColumns = @JoinColumn(name = "author_id"))
            private Set<Author> authors;

        }

        @Entity
        public class Author {

            @Id
            @GeneratedValue(strategy = GenerationType.IDENTITY)
            private Long id;

            private String name;
        }
        ```

        在这里，我们可以看到图书和作者实体之间的多对多单向关联。@ManyToMany注释指定每本书可以有多位作者，每位作者可以写多本书。The@JoinTable注释指定了加入表的名称和外键列，以加入图书和作者实体。

3. 双向关联

    双向关联是两个实体之间的关系，其中每个实体都对另一个实体有引用。

    为了定义双向关联，我们在@OneToMany和@ManyToMany注释中使用mappedBy属性。然而，重要的是要注意，仅仅依靠单向关联可能不够，因为双向关联提供了额外的好处。

    1. 一对多双向协会

        在一对多双向关联中，一个实体对另一个实体有引用。此外，另一个实体有对第一个实体的引用集合。

        例如，一个部门实体有一组员工实体。同时，员工实体对它所属的部门实体有参考。

        让我们来看看如何创建一对多双向关联：

        ```java
        @Entity
        public class Department {

            @OneToMany(mappedBy = "department")
            private List<Employee> employees;

        }

        @Entity
        public class Employee {

            @ManyToOne
            @JoinColumn(name = "department_id")
            private Department department;

        }
        ```

        在部门实体中，我们使用@OneToMany注释来指定部门实体和员工实体之间的关系。mappedBy属性指定了拥有关系的员工实体中属性的名称。在这种情况下，部门实体不拥有这种关系，因此我们指定了mappedBy =“department”。

        在员工实体中，我们使用@ManyToOne注释来指定员工实体和部门实体之间的关系。@JoinColumn注释指定了员工表中引用部门表的外键列的名称。

    2. 多对多双向协会

        在处理多对多双向关联时，重要的是要明白，每个参与的实体都将有对另一个实体的引用集合。

        为了说明这个概念，让我们考虑一个包含课程实体的学生实体和一个包含学生实体集合的课程实体的例子。通过建立这种双向关联，我们使两个实体能够相互了解，并使其更容易导航和管理他们的关系。

        以下是如何创建多对多双向关联的示例：

        ```java
        @Entity
        public class Student {

            @ManyToMany(mappedBy = "students")
            private List<Course> courses;

        }

        @Entity
        public class Course {

            @ManyToMany
            @JoinTable(name = "course_student",
                joinColumns = @JoinColumn(name = "course_id"),
                inverseJoinColumns = @JoinColumn(name = "student_id"))
            private List<Student> students;

        }
        ```

        在学生实体中，我们使用@ManyToMany注释来指定学生实体和课程实体之间的关系。mappedBy属性在拥有关系的课程实体中指定属性名称。在这种情况下，课程实体拥有这种关系，因此我们指定了mappedBy =“students”。

        在课程实体中，我们使用@ManyToMany注释来指定课程实体和学生实体之间的关系。@JoinTable注释指定了存储关系的连接表的名称。

4. 单向与双向关联

    面向对象编程中的单向和双向关联在两类之间关系的方向上有所不同。

    首先，单向关联只有一个方向的关系，而双向关联在两个方向上都有关系。这种差异会影响软件系统的设计和功能。例如，双向关联可以使相关类之间更容易导航，但它们也会带来更多的复杂性和潜在的错误。

    另一方面，单向关联可能更简单，错误更少，但它们可能需要更多的变通办法才能在相关类之间导航。

    总体而言，了解单向和双向关联之间的区别对于就软件系统的设计和实施做出明智的决定至关重要。

    以下是一个表格，总结了数据库中单向和双向关联之间的差异：

    |       | 单向关联                        |                                |
    |-------|-----------------------------|--------------------------------|
    | 定义    | 两个表之间的关系，其中一个表的外键引用另一个表的主键。 | 两个表之间的关系，其中两个表都有一个引用另一个表主键的外键。 |
    | 导航    | 只能单向浏览--从子表到父表。             | 双向导航--从任一表到另一表。                |
    | 性能    | 由于表结构简单，约束较少，因此一般速度较快。      | 由于额外的约束和表结构的复杂性，一般速度较慢。        |
    | 数据一致性 | 确保子表中的外键约束引用父表中的主键。         | 确保子表中的外键约束引用父表中的主键。            |
    | 灵活性   | 灵活性较差，因为更改子表可能需要更改父表模式。     | 更灵活，因为可以独立更改任一表，而不会影响另一表。      |

    识别这些变化很重要，因为它们可能会显著影响数据库系统的性能和功能。

5. 结论

    在本文中，我们看到了单向或双向关联之间的选择如何依赖于软件的具体需求。单向关联更简单，可能对许多应用程序来说足够，而双向关联提供了更多的灵活性，在更复杂的场景中可能很有用。

    然而，双向关联也会带来更多的复杂性和潜在问题，如循环依赖和内存泄漏，应谨慎使用。重要的是要仔细考虑权衡，并为每种情况选择合适的协会类型。
