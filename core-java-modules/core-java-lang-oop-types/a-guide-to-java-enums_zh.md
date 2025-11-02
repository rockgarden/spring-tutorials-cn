# [Java枚举指南](https://www.baeldung.com/a-guide-to-java-enums)

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
