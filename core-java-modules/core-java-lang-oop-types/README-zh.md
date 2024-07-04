# 核心Java语言OOP-类型

- [x] [Java类和对象](#java类和对象)
- [x] [Java关键字this指南](#this关键词的指南)
- [Java 中的嵌套类](https://www.baeldung.com/java-nested-classes)
- [Java 中的标记接口](https://www.baeldung.com/java-marker-interfaces)
- [x] [Java 中枚举枚举值](#在java中遍历枚举值)
- [为 Java 枚举附加值](https://www.baeldung.com/java-enum-values)
- [x] [Java 枚举指南](#java枚举指南)
- [确定对象是否属于原始类型](https://www.baeldung.com/java-object-primitive-type)
- [在 Java 中扩展枚举](https://www.baeldung.com/java-extending-enums)
- [Java 类文件命名约定](https://www.baeldung.com/java-class-file-naming)

## Java类和对象

1. 概述

    在这个快速教程中，我们将看看Java编程语言的两个基本组成部分--类classes和对象objects。它们是面向对象编程（OOP）的基本概念，我们用它来模拟现实生活中的实体。

    在OOP中，类是对象的蓝图或模板。我们用它们来描述实体的类型。

    另一方面，对象是活的实体，由类创建。它们在其字段中包含某些状态，并通过其方法呈现某些行为。

2. 类

    简单地说，一个类代表一个定义或一种类型的对象。在Java中，类可以包含字段、构造函数和方法。

    让我们看一个例子，用一个简单的Java类代表一辆汽车：objects/Car.java

    这个Java类总体上代表一辆汽车。我们可以从这个类中创建任何类型的汽车。我们用字段来保持状态，用构造函数来创建这个类的对象。

    每个Java类默认都有一个空的构造函数。如果我们没有像上面那样提供一个特定的实现，我们就使用它。下面是我们的汽车类的默认构造函数的样子：`Car(){}`

    这个构造函数简单地将对象的所有字段初始化为默认值。字符串被初始化为空，整数被初始化为零。

    现在，我们的类有一个特定的构造函数，因为我们希望我们的对象在创建时有其字段的定义：

    `Car(String type, String model) {...}`

    总而言之，我们写了一个定义汽车的类。它的属性由字段描述，字段包含了该类对象的状态，而它的行为则由方法描述。

3. 对象

    类是在编译时翻译的，而对象是在运行时从类中创建的。

    一个类的对象被称为实例，我们用构造函数创建并初始化它们：

    ```java
    Car focus = new Car("Ford", "Focus", "red");
    Car auris = new Car("Toyota", "Auris", "blue");
    Car golf = new Car("Volkswagen", "Golf", "green");
    ```

    现在，我们已经创建了不同的汽车对象，都来自一个单一的类。这就是它的意义所在，在一个地方定义蓝图，然后，在许多地方多次重复使用它。

    到目前为止，我们有三个汽车对象，它们都停在那里，因为它们的速度是零。我们可以通过调用我们的 increaseSpeed 方法来改变这种情况：

    ```java
    focus.increaseSpeed(10);
    auris.increaseSpeed(20);
    golf.increaseSpeed(30);
    ```

    现在，我们已经改变了我们的汽车的状态--它们都在以不同的速度移动。

    此外，我们可以而且应该定义对我们的类、其构造函数、字段和方法的访问控制。我们可以通过使用访问修饰符来做到这一点。

4. 访问修饰符

    在前面的例子中，我们省略了访问修饰符来简化代码。通过这样做，我们实际上使用了一个默认的包--私有修改器。该修饰符允许从同一包中的任何其他类中访问该类。

    通常情况下，我们会在构造函数中使用public修饰符，以允许从所有其他对象中访问：

    `public Car(String type, String model, String color) {...}`

    我们类中的每个字段和方法也应该用一个特定的修饰符来定义访问控制。类通常有公共修改器，但我们倾向于保持我们的字段是私有的。

    字段持有我们对象的状态，因此我们要控制对该状态的访问。我们可以保留其中的一些字段为私有，而另一些为公有。我们通过被称为getters和setters的特定方法来实现这一点。

    让我们来看看我们的具有完全指定的访问控制的类：objects/Car.java

    我们的类被标记为公共的，这意味着我们可以在任何包中使用它。另外，构造函数是公共的，这意味着我们可以在任何其他对象中从这个类中创建一个对象。

    我们的字段被标记为private，这意味着它们不能从我们的对象中直接访问，但我们通过getters和setters提供对它们的访问。

    类型type和模型model字段没有getters和setters，因为它们持有我们对象的内部数据。我们只能在初始化时通过构造函数来定义它们。

    此外，颜色可以被访问和改变，而速度只能被访问，但不能改变。我们通过专门的公共方法 increaseSpeed() 和 decreaseSpeed() 来执行速度调整。

    换句话说，我们使用访问控制来封装对象的状态。

5. 总结

    在这篇文章中，我们经历了Java语言的两个基本元素，类和对象，并展示了它们的使用方法和原因。我们还介绍了访问控制的基础知识，并演示了其用法。

    要学习Java语言的其他概念，我们建议下一步阅读继承([inheritance](https://www.baeldung.com/java-inheritance)), 超级关键字([super keyword](https://www.baeldung.com/java-super))和抽象类([abstract classes](https://www.baeldung.com/java-abstract-class))。

## this关键词的指南

1. 简介

    在本教程中，我们将看一下this Java关键字。

    在Java中，这个关键字是对当前对象的引用，其方法正在被调用。

    让我们来探讨如何以及何时使用该关键字。

2. 歧义场影射

    Disambiguating Field Shadowing

    这个关键字对于区分实例变量和局部参数很有用。最常见的原因是当我们有与实例字段同名的构造器参数时：

    ```java
    public class KeywordTest {
        private String name;
        private int age;
        public KeywordTest(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
    ```

    正如我们在这里看到的，我们在name和age实例字段上使用这个--将它们与参数区分开来。

    另一种用法是在局部范围内的参数隐藏或阴影中使用它。一个使用的例子可以在[变量和方法隐藏](https://www.baeldung.com/java-variable-method-hiding)的文章中找到。

3. 引用同一类的构造函数

    在构造函数中，我们可以使用this()来调用同一类别的不同构造函数。在这里，我们使用this()进行构造函数链，以减少代码的使用。

    最常见的用例是从参数化构造函数中调用一个默认的构造函数：

    ```java
    public KeywordTest(String name, int age) {
        this();
    }
    ```

    或者，我们可以从无参数构造函数中调用参数化构造函数并传递一些参数：

    ```java
    public KeywordTest() {
        this("John", 27);
    }
    ```

    注意，this()应该是构造函数的第一个语句，否则会发生编译错误。

4. 将this作为参数传递

    这里我们有printInstance()方法，其中定义了this关键字参数：

    ```java
    public KeywordTest() {
        printInstance(this);
    }

    public void printInstance(KeywordTest thisKeyword) {
        System.out.println(thisKeyword);
    }
    ```

    在构造函数中，我们调用了printInstance()方法。通过这个方法，我们传递一个对当前实例的引用。

5. 返回this

    我们也可以使用这个关键字来从方法中返回当前的类实例。

    为了不重复代码，这里有一个完整的实际例子，说明它是如何在[构建器设计模式](https://www.baeldung.com/creational-design-patterns)中实现的。

6. 内层类中的this关键字

    我们也用它来从内类中访问外类实例：

    ```java
    public class KeywordTest {
        private String name;
        class ThisInnerClass {
            boolean isInnerClass = true;
            public ThisInnerClass() {
                KeywordTest thisKeyword = KeywordTest.this;
                String outerString = KeywordTest.this.name;
            }
        }
    }
    ```

    在这里，在构造函数内部，我们可以通过KeywordTest.this调用获得对KeywordTest实例的引用。我们可以更深入地访问实例变量，如KeywordTest.this.name字段。

7. 总结

    在这篇文章中，我们探讨了Java中的this关键字。

## Java枚举指南

1. 概述

    在本教程中，我们将学习什么是Java枚举，它们解决什么问题，以及如何在实践中使用它们的一些设计模式。

    Java 5首次引入了枚举关键字。它表示一种特殊类型的类，总是扩展java.lang.Enum类。关于使用方法的官方文档，我们可以去看一下[文档](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Enum.html)。

    这样定义的常量使代码更具可读性，可以进行编译时检查，预先记录可接受的值的列表，并避免由于传入无效的值而导致的意外行为。

    下面是一个快速而简单的例子，这个枚举定义了披萨订单的状态；订单状态可以是ORDERED、READY或DELIVERED：

    ```java
    public enum PizzaStatus {
        ORDERED,
        READY, 
        DELIVERED; 
    }
    ```

    此外，枚举还带有许多有用的方法，如果我们使用传统的公共静态final常量的话，我们就需要写这些方法。

2. 自定义枚举方法

    现在我们对什么是枚举以及如何使用枚举有了基本的了解，我们将通过在枚举上定义一些额外的API方法来使我们之前的例子更上一层楼：

    ```java
    public class Pizza {
        private PizzaStatus status;
        public enum PizzaStatus {
            ORDERED,
            READY,
            DELIVERED;
        }

        public boolean isDeliverable() {
            if (getStatus() == PizzaStatus.READY) {
                return true;
            }
            return false;
        }
        
        // Methods that set and get the status variable.
    }
    ```

3. 使用"=="操作符对枚举类型进行比较

    由于枚举类型确保在JVM中只存在一个常量的实例，我们可以安全地使用`"=="`操作符来比较两个变量，就像我们在上面的例子中做的那样。此外，`"=="`运算符提供了编译时和运行时的安全性。

    首先，我们将在下面的片段中看一下运行时的安全性，我们将使用`"=="`操作符来比较状态。任何一个值都可以是空的，我们不会得到一个NullPointerException。相反，如果我们使用equals方法，我们会得到一个NullPointerException：

    ```java
    if(testPz.getStatus().equals(Pizza.PizzaStatus.DELIVERED)); 
    if(testPz.getStatus() == Pizza.PizzaStatus.DELIVERED); 
    ```

    至于编译时的安全性，让我们看一个例子，我们将通过使用equals方法进行比较来确定不同类型的枚举是否相等。这是因为枚举的值和getStatus方法不约而同地是一样的；然而，从逻辑上讲，比较应该是假的。我们通过使用`"=="`操作符来避免这个问题。

    编译器将把这种比较标记为不兼容错误：

    ```java
    if(testPz.getStatus().equals(TestColor.GREEN));
    if(testPz.getStatus() == TestColor.GREEN);
    ```

4. 在开关语句中使用枚举类型

    我们也可以在开关语句中使用枚举类型：

    ```java
    public int getDeliveryTimeInDays() {
        switch (status) {
            case ORDERED: return 5;
            case READY: return 2;
            case DELIVERED: return 0;
        }
        return 0;
    }
    ```

5. 枚举中的字段、方法和构造函数

    我们可以在枚举类型中定义构造函数、方法和字段，这使得它们非常强大。

    接下来，让我们扩展上面的例子，实现从一个比萨饼订单的一个阶段过渡到另一个阶段。我们将看到我们如何摆脱之前使用的if和switch语句：

    enums/Pizza.java

    下面的测试片段演示了这是如何工作的：

    PizzaUnitTest.java: givenPizaOrder_whenReady_thenDeliverable()

6. EnumSet和EnumMap

    1. EnumSet

        EnumSet是一个专门的Set实现，旨在与Enum类型一起使用。

        与HashSet相比，由于使用了内部的Bit Vector Representation，它是一个非常高效和紧凑的Enum常量集合的表示。它还为传统的基于int的 "位标志(bit flags)" 提供了一个类型安全的替代方案，使我们能够写出简洁的代码，使其更具可读性和可维护性。

        EnumSet是一个抽象类，它有两个实现，RegularEnumSet和JumboEnumSet，其中一个的选择取决于实例化时枚举中常量的数量。

        因此，只要我们想在大多数情况下处理枚举常量的集合（如子集、添加、删除和批量操作如containsAll和removeAll），使用这个集合是个好主意，如果我们只是想在所有可能的常量上进行迭代，则使用Enum.values()。

        在下面的代码片段中，我们可以看到如何使用EnumSet来创建常量的子集：

        ```java
        public class Pizza {

            private static EnumSet<PizzaStatus> undeliveredPizzaStatuses =
            EnumSet.of(PizzaStatus.ORDERED, PizzaStatus.READY);

            private PizzaStatus status;

            ...

            public static List<Pizza> getAllUndeliveredPizzas(List<Pizza> input) {
                return input.stream().filter(
                (s) -> undeliveredPizzaStatuses.contains(s.getStatus()))
                    .collect(Collectors.toList());
            }

            public void deliver() { 
                if (isDeliverable()) { 
                    PizzaDeliverySystemConfiguration.getInstance().getDeliveryStrategy()
                    .deliver(this); 
                    this.setStatus(PizzaStatus.DELIVERED); 
                } 
            }
            
            // Methods that set and get the status variable.
        }
        ```

        执行下面的测试展示了Set接口的EnumSet实现的力量：

        PizzaUnitTest.java: givenPizaOrders_whenRetrievingUnDeliveredPzs_thenCorrectlyRetrieved()

    2. EnumMap

        EnumMap是一个专门的Map实现，旨在将枚举常量作为键来使用。与其对应的HashMap相比，它是一个高效而紧凑的实现，内部表示为一个数组：

        `EnumMap<Pizza.PizzaStatus, Pizza> map;`

        让我们看一个如何在实践中使用它的例子：

        ```java
        public static EnumMap<PizzaStatus, List<Pizza>> 
        groupPizzaByStatus(List<Pizza> pizzaList) {
            EnumMap<PizzaStatus, List<Pizza>> pzByStatus = 
            new EnumMap<PizzaStatus, List<Pizza>>(PizzaStatus.class);
            
            for (Pizza pz : pizzaList) {
                PizzaStatus status = pz.getStatus();
                if (pzByStatus.containsKey(status)) {
                    pzByStatus.get(status).add(pz);
                } else {
                    List<Pizza> newPzList = new ArrayList<Pizza>();
                    newPzList.add(pz);
                    pzByStatus.put(status, newPzList);
                }
            }
            return pzByStatus;
        }
        ```

        执行下面的测试展示了Map接口的EnumMap实现的力量：

        PizzaUnitTest.java: givenPizaOrders_whenGroupByStatusCalled_thenCorrectlyGrouped()

7. 使用枚举实现设计模式

    1. 单例模式

        通常情况下，使用Singleton模式实现一个类是相当不容易的。枚举提供了一种实现单子的快速而简单的方法。

        此外，由于枚举类在外壳下实现了Serializable接口，JVM保证该类是一个单子。这与传统的实现方式不同，我们必须确保在反序列化过程中不创建新的实例。

        在下面的代码片断中，我们可以看到如何实现一个单子模式：

        enums/PizzaDeliverySystemConfiguration.java

    2. 策略模式

        传统上，策略模式是通过拥有一个由不同类实现的接口来编写的。

        添加一个新的策略意味着添加一个新的实现类。通过枚举，我们可以事半功倍地实现这一点，增加一个新的实现意味着简单地定义另一个具有某种实现的实例。

        下面的代码片段展示了如何实现策略模式：

        enums/PizzaDeliveryStrategy.java

        然后我们在Pizza类中添加以下方法：

        ```java
        public void deliver() {
            if (isDeliverable()) {
                PizzaDeliverySystemConfiguration.getInstance().getDeliveryStrategy()
                .deliver(this);
                this.setStatus(PizzaStatus.DELIVERED);
            }
        }
        ```

        PizzaUnitTest.java: givenPizaOrder_whenDelivered_thenPizzaGetsDeliveredAndStatusChanges()

8. Java 8和枚举

    我们可以用Java 8重写Pizza类，看看getAllUndeliveredPizzas()和groupPizzaByStatus()方法是如何通过使用lambdas和Stream APIs变得如此简洁的：

    ```java
    public static List<Pizza> getAllUndeliveredPizzas(List<Pizza> input) {
        return input.stream().filter(
        (s) -> !deliveredPizzaStatuses.contains(s.getStatus()))
            .collect(Collectors.toList());
    }

    public static EnumMap<PizzaStatus, List<Pizza>> 
    groupPizzaByStatus(List<Pizza> pzList) {
        EnumMap<PizzaStatus, List<Pizza>> map = pzList.stream().collect(
        Collectors.groupingBy(Pizza::getStatus,
        () -> new EnumMap<>(PizzaStatus.class), Collectors.toList()));
        return map;
    }
    ```

9. Enum的JSON表示法

    使用Jackson库，有可能对枚举类型进行JSON表示，就像它们是POJO一样。在下面的代码片段中，我们将看到如何使用Jackson注解来实现这一点：

    ```java
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum PizzaStatus {
        ORDERED (5){
            @Override
            public boolean isOrdered() {
                return true;
            }
        },
        READY (2){
            @Override
            public boolean isReady() {
                return true;
            }
        },
        DELIVERED (0){
            @Override
            public boolean isDelivered() {
                return true;
            }
        };

        private int timeToDelivery;

        public boolean isOrdered() {return false;}

        public boolean isReady() {return false;}

        public boolean isDelivered(){return false;}

        @JsonProperty("timeToDelivery")
        public int getTimeToDelivery() {
            return timeToDelivery;
        }

        private PizzaStatus (int timeToDelivery) {
            this.timeToDelivery = timeToDelivery;
        }
    }
    ```

    我们可以使用Pizza和PizzaStatus，如下所示：

    ```java
    Pizza pz = new Pizza();
    pz.setStatus(Pizza.PizzaStatus.READY);
    System.out.println(Pizza.getJsonString(pz));
    ```

    这将产生以下JSON表示的Pizza状态：

    ```json
    {
    "status" : {
        "timeToDelivery" : 2,
        "ready" : true,
        "ordered" : false,
        "delivered" : false
    },
    "deliverable" : true
    }
    ```

    关于枚举类型的JSON序列化/反序列化（包括自定义）的更多信息，我们可以参考[Jackson - Serialize Enums as JSON Objects](https://www.baeldung.com/jackson-serialize-enums)。

10. 结语

    在这篇文章中，我们探索了Java枚举，从语言基础知识到更高级和有趣的现实世界的使用案例。

## 在Java中遍历枚举值

1. 概述

    在 Java 中，枚举是一种数据类型，可帮助我们将一组预定义的常量分配给变量。

    在本快速教程中，我们将学习在 Java 中遍历枚举的不同方法。

2. 遍历枚举值

    让我们先定义一个枚举，以便创建一些简单的代码示例：

    enums.iteration/DaysOfWeekEnum.java

    枚举没有用于迭代的方法，如 forEach() 或 iterator()。相反，我们可以使用由 values() 方法返回的 Enum 值数组。

    1. 使用 for 循环进行迭代

        首先，我们可以简单地使用老式 for 循环：

        `for (DaysOfWeekEnum day : DaysOfWeekEnum.values()) {}`

    2. 使用流迭代

        我们还可以使用 java.util.Stream 对 Enum 值执行操作。

        要创建一个流，我们有两种选择。第一种是使用 Stream.of：

        `Stream.of(DaysOfWeekEnum.values());`

        第二种是使用 Arrays.stream：

        `Arrays.stream(DaysOfWeekEnum.values());`

        让我们扩展 DaysOfWeekEnum 类，创建一个使用 Stream 的示例：

        enums.iteration/DaysOfWeekEnum.java\stream()

        现在，我们将编写一个示例来打印非工作日：

        `DaysOfWeekEnum.stream().filter(d -> d.getTypeOfDay().equals("off")).forEach(System.out::println);`

    3. 使用 forEach() 进行迭代

        在 Java 8 中，forEach() 方法被添加到 Iterable 接口中。因此，所有 Java 集合类都有 forEach() 方法的实现。为了在枚举中使用这些方法，我们首先需要将枚举转换为合适的集合。我们可以使用 Arrays.asList() 生成一个 ArrayList，然后使用 forEach() 方法对其进行循环：

        `Arrays.asList(DaysOfWeekEnum.values()).forEach(day -> System.out.println(day));`

    4. 使用 EnumSet 进行遍历

        EnumSet 是一种专门的集合实现，我们可以将其用于枚举类型：

        `EnumSet.allOf(DaysOfWeekEnum.class).forEach(day -> System.out.println(day));`

    5. 使用枚举值数组列表

        我们还可以将枚举值添加到列表中。这样，我们就可以像操作其他程序一样操作 List：

        ```java
        List<DaysOfWeekEnum> days = new ArrayList<>();
        days.add(DaysOfWeekEnum.FRIDAY);
        days.remove(DaysOfWeekEnum.SATURDAY);
        ```

        我们还可以使用 Arrays.asList() 创建 ArrayList。

        但是，由于 ArrayList 由 Enum 值数组支持，它将是不可变的，因此我们无法从列表中添加或删除项目。下面代码中的移除操作将失败，并出现 UnsupportedOperationException 异常：

        ```java
        List<DaysOfWeekEnum> days = Arrays.asList(DaysOfWeekEnum.values());
        days.remove(0);
        ```

3. 结论

    在本文中，我们讨论了在 Java 中使用 forEach、Stream 和 for 循环遍历枚举的各种方法。

    如果我们需要执行并行操作，Stream 是一个不错的选择。否则，使用哪种方法并无限制。

## Relevant Articles

- [x] [Java Classes and Objects](https://www.baeldung.com/java-classes-objects)
- [x] [Guide to the this Java Keyword](https://www.baeldung.com/java-this)
- [Nested Classes in Java](https://www.baeldung.com/java-nested-classes)
- [Marker Interfaces in Java](https://www.baeldung.com/java-marker-interfaces)
- [x] [Iterating over Enum Values in Java](https://www.baeldung.com/java-enum-iteration)
- [Attaching Values to Java Enum](https://www.baeldung.com/java-enum-values)
- [x] [A Guide to Java Enums](https://www.baeldung.com/a-guide-to-java-enums)
- [Determine if an Object is of Primitive Type](https://www.baeldung.com/java-object-primitive-type)
- [Extending Enums in Java](https://www.baeldung.com/java-extending-enums)
- [Java Class File Naming Conventions](https://www.baeldung.com/java-class-file-naming)

## Code

像往常一样，完整的代码可以在[Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-oop-types)上找到。
