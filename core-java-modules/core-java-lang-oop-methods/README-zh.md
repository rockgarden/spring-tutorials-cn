# Java语言核心OOP方法

本模块包含关于Java中方法的文章。

- [Java中的方法](https://www.baeldung.com/java-methods)
- [x] [Java中的方法重载和覆盖](#java中的方法重载和重写)
- [x] [Java中的equals()和hashCode()合同](https://www.baeldung.com/java-equals-hashcode-contracts)
- [x] [Java中的hashCode()指南](#java中的hashcode指南)
- [Java 中的变量返回类型](https://www.baeldung.com/java-covariant-return-type)
- [Java 中方法的签名是否包括返回类型？](https://www.baeldung.com/java-method-signature-return-type)
- [解决隐藏实用程序类公共构造函数的声纳警告](https://www.baeldung.com/java-sonar-hide-implicit-constructor)

## Java equals() 和 hashCode() 合同

1. 概述

    在本教程中，我们将介绍两个密切相关的方法：equals() 和 hashCode()。我们将重点讨论它们之间的关系、如何正确覆盖它们，以及为什么要同时覆盖这两个方法或都不覆盖。

2. equals()

    对象类定义了 equals() 和 hashCode() 方法，这意味着每个 Java 类（包括我们创建的类）都隐式定义了这两个方法：

    ```java
    class Money {
        int amount;
        String currencyCode;
    }
    ```

    ```java
    Money income = new Money(55, "USD");
    Money expenses = new Money(55, "USD");
    boolean balanced = income.equals(expenses)
    ```

    我们希望 income.equals(expenses) 返回 true，但当前形式的 Money 类不会返回 true。

    对象类中 equals() 的默认实现表示相等与对象身份相同，而收入和支出是两个不同的实例。

    1. 重写 equals()

        让我们重写 equals() 方法，这样它就不会只考虑对象身份，还会考虑两个相关属性的值：

        Money.java\equals(Object o)

    2. equals() 合约

        Java SE 定义了 equals() 方法的实现必须满足的契约。大部分标准都是常识性的。equals() 方法必须是

        - 反身(reflexive)：对象必须等于自身
        - 对称性(symmetric)：x.equals(y) 必须返回与 y.equals(x) 相同的结果
        - 传递性(transitive)：如果 x.equals(y) 和 y.equals(z)，那么 x.equals(z) 也等于 y.equals(x)
        - 一致性(consistent)：只有当 equals() 中包含的属性发生变化时，equals() 的值才会发生变化（不允许随机变化）
        我们可以在 Java SE 文档中查找对象类的确切[标准](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Object.html)。

    3. 通过继承违反 equals() 对称性

        如果 equals() 的标准是如此符合常识，那么我们怎么会违反它呢？如果我们扩展了一个重载了 equals() 的类，那么就会经常出现违反的情况。让我们考虑一下扩展了我们的 Money 类的 Voucher 类：

        WrongVoucher.java

        乍一看，凭单类及其对 equals() 的覆盖似乎是正确的。只要我们将 Money 与 Money 或 Voucher 与 Voucher 进行比较，这两个 equals() 方法的行为都是正确的。但是，如果我们比较这两个对象，会发生什么情况呢？

        ```java
        Money cash = new Money(42, "USD");
        WrongVoucher voucher = new WrongVoucher(42, "USD", "Amazon");
        voucher.equals(cash) => false // As expected.
        cash.equals(voucher) => true // That's wrong.
        ```

        这违反了 equals() 合约的对称性标准。

    4. 用组合修复 equals() 的对称性

        为了避免这个陷阱，我们应该优先考虑组合而不是继承。

        与其子类化 Money，不如创建一个带有 Money 属性的 Voucher 类：Voucher.java

        现在，equals 将按照合约要求对称工作。

3. hashCode()

    hashCode() 返回一个整数，表示类的当前实例。我们应该根据类的等价定义来计算这个值。因此，如果我们覆盖了 equals() 方法，也必须覆盖 hashCode()。

    1. hashCode() 合约

        Java SE 还为 hashCode() 方法定义了一个契约。仔细研究一下就会发现，hashCode() 和 equals() 是多么密切相关。

        hashCode() 合约中的三个标准都以某种方式提到了 equals() 方法：

        - 内部一致性(internal consistency)：只有当 equals() 中的属性发生变化时，hashCode() 的值才会发生变化
        - 等价一致性(equals consistency)：彼此相等的对象必须返回相同的 hashCode
        - 碰撞(collisions)：不相等的对象可能具有相同的 hashCode
    2. 违反 hashCode() 和 equals() 的一致性

        hashCode 方法契约的第二条标准有一个重要的后果： 如果我们覆盖 equals()，就必须同时覆盖 hashCode()。这是迄今为止对 equals() 和 hashCode() 方法合约最普遍的违反。

        让我们来看这样一个例子：Team.java

        Team 类只覆盖了 equals()，但它仍然隐式地使用了 Object 类中定义的 hashCode() 的默认实现。这样，该类的每个实例都会返回不同的 hashCode()。这违反了第二条规则。

        现在，如果我们创建两个团队对象，它们的城市都是 "纽约"，部门都是 "市场营销"，那么它们将是相同的，但它们将返回不同的 hashCode。

    3. 具有不一致 hashCode() 的 HashMap 关键字

        但是，为什么我们的 Team 类中的违约会成为问题呢？当涉及到一些基于哈希的集合时，问题就开始了。让我们尝试使用 Team 类作为 HashMap 的键：

        ```java
        Map<Team,String> leaders = new HashMap<>();
        leaders.put(new Team("New York", "development"), "Anne");
        leaders.put(new Team("Boston", "development"), "Brian");
        leaders.put(new Team("Boston", "marketing"), "Charlie");
        Team myTeam = new Team("New York", "development");
        String myTeamLeader = leaders.get(myTeam);
        ```

        我们希望 myTeamLeader 返回 "Anne"，但在当前代码中，它并没有返回 "Anne"。

        如果我们想使用 Team 类的实例作为 HashMap 的键，就必须重写 hashCode() 方法，使其遵守契约；相等的对象会返回相同的 hashCode。

        让我们来看一个实现示例：Team.java\hashCode()

        更改后，leaders.get(myTeam) 如预期一样返回 "Anne"。

4. 何时重载 equals() 和 hashCode()？

    一般情况下，我们要么同时覆盖这两个函数，要么都不覆盖。在第 3 节中，我们已经看到了忽视这一规则的不良后果。

    领域驱动设计（Domain-Driven Design）可以帮助我们决定在什么情况下不覆盖它们。对于实体类，对于具有内在标识的对象，默认实现通常是合理的。

    但是，对于值对象，我们通常更喜欢基于其属性的平等。因此，我们希望覆盖 equals() 和 hashCode()。请记住第 2 节中的 Money 类：55 美元等于 55 美元，即使它们是两个独立的实例。

5. 实现助手

    我们通常不会手工编写这些方法的实现。正如我们所看到的，这其中有很多陷阱。

    一种常见的方法是让[IDE](https://www.baeldung.com/java-eclipse-equals-and-hashcode)生成 equals() 和 hashCode() 方法。

    [Apache Commons Lang](https://www.baeldung.com/java-eclipse-equals-and-hashcode) 和 [Google Guava](https://www.baeldung.com/whats-new-in-guava-19) 都提供了帮助类来简化这两个方法的编写。

    [Project Lombok](https://www.baeldung.com/intro-to-project-lombok) 还提供了 @EqualsAndHashCode 注解。请再次注意 equals() 和 hashCode() 是如何 "结合" 在一起的，甚至有一个共同的注解。

6. 验证契约

    如果要检查我们的实现是否符合 Java SE 合约以及最佳实践，我们可以使用 EqualsVerifier 库。

    让我们添加 EqualsVerifier Maven 测试依赖项：

    `<groupId>nl.jqno.equalsverifier</groupId><artifactId>equalsverifier</artifactId>`

    现在，让我们验证 Team 类是否遵循 equals() 和 hashCode() 合约：

    TeamUnitTest.java\equalsHashCodeContracts()

    值得注意的是，EqualsVerifier 同时测试 equals() 和 hashCode() 方法。

    EqualsVerifier 比 Java SE 契约更严格。例如，它确保我们的方法不会抛出 NullPointerException。此外，它还确保两个方法或类本身都是最终的。

    需要注意的是，EqualsVerifier 的默认配置只允许不可变字段。这比 Java SE 契约允许的检查更为严格。它符合领域驱动设计的建议，即值对象不可变。

    如果我们发现某些内置约束是不必要的，我们可以在 EqualsVerifier 调用中添加 suppress(Warning.SPECIFIC_WARNING)。

7. 结论

    在本文中，我们讨论了 equals() 和 hashCode() 合约。我们应该记住

    - 如果覆盖 equals()，则始终覆盖 hashCode()
    - 为值对象覆盖 equals() 和 hashCode()
    - 注意扩展已覆盖 equals() 和 hashCode() 的类的陷阱
    - 考虑使用集成开发环境或第三方库生成 equals() 和 hashCode() 方法
    - 考虑使用 EqualsVerifier 测试我们的实现

## Java中的方法重载和重写

1. 概述

    方法重载和重载是Java编程语言的关键概念，因此，它们值得深入研究。

    在这篇文章中，我们将学习这些概念的基础知识，并看看它们在哪些情况下可以发挥作用。

2. 方法重载

    方法重载是一种强大的机制，它使我们能够定义有凝聚力的类API。为了更好地理解为什么方法重载是一个如此有价值的特性，让我们看一个简单的例子。

    假设我们写了一个naive的实用类，实现了两个数字相乘、三个数字相乘的不同方法，以此类推。

    如果我们给这些方法起了误导性或模糊的名字，比如multiply2()、multiply3()、multiply4()，那么这将是一个设计不良的类API。这里就是方法重载发挥作用的地方。

    简单地说，我们可以通过两种不同的方式实现方法重载：

    - 实现两个或多个名字相同但接受不同数量参数的方法
    - 实现两个或多个名字相同但接受不同类型参数的方法。

    1. 不同的参数数

        Multiplier类简要地展示了如何通过简单地定义两个接受不同参数数的实现来重载multiply()方法：

        methodoverloadingoverriding.util/Multiplier.java

    2. 不同类型的参数

        同样地，我们可以通过让multiply()方法接受不同类型的参数来重载它：

        methodoverloadingoverriding.util/Multiplier.java

        此外，用两种类型的方法重载来定义Multiplier类是合法的。

        然而，值得注意的是，不可能有两个仅在返回类型上不同的方法实现。

        为了理解这个原因--让我们考虑下面这个例子：

        ```java
        public int multiply(int a, int b) { 
            return a * b; 
        }
        public double multiply(int a, int b) { 
            return a * b; 
        }
        ```

        在这种情况下，由于方法调用的模糊性，代码根本无法编译--编译器不知道应该调用multiply()的哪个实现。

    3. 类型推广

        方法重载提供的一个很好的功能是所谓的类型推广(Type Promotion)，也就是拓宽原始转换。

        简单地说，当传递给重载方法的参数类型和特定的方法实现之间不匹配时，一个给定的类型被隐含地提升到另一个类型。

        为了更清楚地了解类型推广是如何工作的，请考虑以下multiply()方法的实现：

        ```java
        public double multiply(int a, long b) {
            return a * b;
        }
        public int multiply(int a, int b, int c) {
            return a * b * c;
        }
        ```

        现在，调用有两个int参数的方法将导致第二个参数被提升为long，因为在这种情况下，没有一个匹配的有两个int参数的方法实现。

        让我们看看一个快速的单元测试来演示类型推广：

        ```java
        @Test
        public void whenCalledMultiplyAndNoMatching_thenTypePromotion() {
            assertThat(multiplier.multiply(10, 10)).isEqualTo(100.0);
        }
        ```

        相反，如果我们用一个匹配的实现来调用这个方法，类型推广就不会发生：

        ```java
        @Test
        public void whenCalledMultiplyAndMatching_thenNoTypePromotion() {
            assertThat(multiplier.multiply(10, 10, 10)).isEqualTo(1000);
        }
        ```

        下面是适用于方法重载的类型推广规则的总结：

        - byte可以被提升为short、int、long、float或double
        - short可以被提升为int、long、float或double
        - char可以被提升为int、long、float或double
        - int可以被提升为long、float或double
        - long可以被提升为float或double
        - float可以被提升为double

    4. 静态绑定

        将特定的方法调用与方法的主体联系起来的能力被称为绑定。

        在方法重载的情况下，绑定是在编译时静态进行的，因此它被称为静态绑定。

        编译器可以通过简单地检查方法的签名来有效地在编译时设置绑定。

3. 方法重写

    方法覆盖允许我们在子类中为基类中定义的方法提供细粒度的实现。

    虽然方法覆盖是一个强大的功能--考虑到这是使用继承的逻辑结果，是OOP的最大支柱之一--但何时何地利用它，应该根据每个使用情况仔细分析。

    现在让我们看看如何通过创建一个简单的、基于继承的（"is-a"）关系来使用方法重写。

    这里是基类：

    methodoverloadingoverriding.model/Vehicle.java

    这里是一个臆造的子类：

    methodoverloadingoverriding.model/Car.java

    在上面的层次结构中，我们简单地重载了accelerate()方法，以便为子类型Car提供一个更完善的实现。

    在这里，我们可以清楚地看到，如果一个应用程序使用Vehicle类的实例，那么它也可以与Car类的实例一起工作，因为accelerate()方法的两个实现具有相同的签名和相同的返回类型。

    让我们写几个单元测试来检查Vehicle和Car类：

    methodoverloadingoverriding/MethodOverridingUnitTest.java

    现在，让我们看看一些单元测试，看看没有被重写的run()和stop()方法如何为Car和Vehicle返回相等的值：

    MethodOverridingUnitTest.java: givenVehicleCarInstances_whenCalledRun_thenEqual()

    MethodOverridingUnitTest.java: givenVehicleCarInstances_whenCalledStop_thenEqual()

    在我们的案例中，我们可以访问两个类的源代码，所以我们可以清楚地看到，在一个基本的Vehicle实例上调用accelerate()方法和在Car实例上调用accelerate()方法将对相同的参数返回不同的值。

    因此，下面的测试证明了对汽车实例调用了重载方法：

    ```java
    @Test
    public void whenCalledAccelerateWithSameArgument_thenNotEqual() {
        assertThat(vehicle.accelerate(100))
        .isNotEqualTo(car.accelerate(100));
    }
    ```

    1. 类型可替代性

        OOP的一个核心原则是类型可替代性，它与Liskov替代原则（LSP）密切相关。

        简单地说，LSP指出，如果一个应用程序能与一个给定的基本类型一起工作，那么它也应该能与它的任何子类型一起工作。这样一来，类型的可替代性就得到了适当的保留。

        方法重写的最大问题是，派生类中的一些特定方法实现可能并不完全遵守LSP，因此无法保留类型可替代性。

        当然，让一个被覆盖的方法接受不同类型的参数并返回不同的类型也是有效的，但是要完全遵守这些规则：

        - 如果基类中的方法接受给定类型的参数，被覆盖的方法应该接受相同的类型或超类型（又称禁忌方法参数）。
        - 如果基类中的方法返回无效，被覆盖的方法应该返回无效。
        - 如果基类中的一个方法返回一个基元，被覆盖的方法应该返回相同的基元。
        - 如果基类中的方法返回某种类型，被覆盖的方法应该返回相同的类型或子类型（又称共变(covariant)返回类型）。
        - 如果基类中的方法抛出一个异常，被覆盖的方法必须抛出相同的异常或基类异常的一个子类型
    2. 动态绑定

        考虑到方法覆盖只能通过继承来实现，其中有一个基类和子类的层次结构，编译器不能在编译时确定调用什么方法，因为基类和子类都定义了相同的方法。

        因此，编译器需要检查对象的类型以知道应该调用什么方法。

        由于这种检查是在运行时进行的，方法重写是动态绑定的一个典型例子。

4. 结语

    在本教程中，我们学习了如何实现方法重载和方法重写，并探讨了一些它们有用的典型情况。

## Java中的hashCode()指南

1. 概述

    散列是计算机科学的一个基本概念。
    在Java中，一些最流行的集合背后都有高效的散列算法，如HashMap和HashSet。
    在本教程中，我们将重点讨论hashCode()如何工作，它在集合中的作用以及如何正确实现它。

    进一步阅读：

    [Java equals() 和 hashCode() 合同](https://www.baeldung.com/java-equals-hashcode-contracts)

    了解equals()和hashCode()需要履行的契约以及这两个方法之间的关系。

    [使用 Eclipse 生成 equals() 和 hashCode()](https://www.baeldung.com/java-eclipse-equals-and-hashcode)

    使用Eclipse IDE生成equals()和hashCode()的快速实用指南。

    [Lombok项目简介](https://www.baeldung.com/intro-to-project-lombok)

    全面而实用地介绍Project Lombok在标准Java代码中的许多有用的应用。

2. 在数据结构中使用hashCode()

    在某些情况下，对集合的最简单的操作可能是低效的。
    为了说明问题，这引发了一个线性搜索，这对巨大的列表来说是非常无效的：

    ```java
    List<String> words = Arrays.asList("Welcome", "to", "Baeldung");
    if (words.contains("Baeldung")) {
        System.out.println("Baeldung is in the list");
    }
    ```

    Java提供了一些数据结构来专门处理这个问题。例如，几个Map接口的实现都是[哈希表](https://www.baeldung.com/cs/hash-tables)。
    当使用哈希表时，这些集合使用hashCode()方法计算出给定键的哈希值。然后它们在内部使用这个值来存储数据，这样访问操作就更有效率了。
3. 了解hashCode()如何工作

    简单地说，hashCode()返回一个整数值，由散列算法生成。
    相等的对象（根据它们的equals()）必须返回相同的哈希代码。不同的对象不需要返回不同的哈希代码。

    hashCode()的一般契约规定：

    - 在一个Java应用程序的执行过程中，每当它在同一个对象上被调用一次以上，hashCode()必须始终如一地返回相同的值，前提是在对象上用于等价比较的信息没有被修改。这个值不需要在一个程序的执行过程中和同一程序的另一个执行过程中保持一致。
    - 如果两个对象根据equals(Object)方法是相等的，在两个对象上调用hashCode()方法必须产生相同的值。
    - 如果根据[equals(java.lang.Object)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Objects.html#equals(java.lang.Object,java.lang.Object))方法，两个对象是不相等的，在两个对象上调用hashCode方法不需要产生不同的整数结果。然而，开发人员应该知道，为不相等的对象产生不同的整数结果可以提高哈希表的性能。

    "在合理可行的范围内，由Object类定义的hashCode()方法确实为不同的对象返回不同的整数。(这通常是通过将对象的内部地址转换为整数来实现的，但这种实现技术不是JavaTM编程语言所要求的）"。
4. 一个Naive的hashCode()实现

    一个完全遵守上述契约的Naive的hashCode()实现实际上是非常简单的。
    为了证明这一点，我们将定义一个覆盖该方法默认实现的用户类样本：

    hashcode.naive/User.java

    用户类为equals()和hashCode()提供了自定义的实现，完全遵守了各自的契约。此外，让hashCode()返回任何固定值也没有什么不合法的。
    然而，这种实现将哈希表的功能降低到基本为零，因为每个对象都将被存储在同一个单一的桶中。
    在这种情况下，哈希表的查找是线性进行的，并没有给我们带来任何真正的优势。我们将在第7节中进一步讨论这个问题。
5. 改进hashCode()的实现

    让我们改进当前的hashCode()实现，包括用户类的所有字段，这样它就能对不相等的对象产生不同的结果：

    ```java
    @Override
    public int hashCode() {
        return (int) id * name.hashCode() * email.hashCode();
    }
    ```

    这种基本的散列算法肯定要比之前的算法好得多。这是因为它计算对象的哈希代码时，只需将name和email字段的哈希代码与id相乘。
    一般来说，我们可以说这是一个合理的hashCode()实现，只要我们保持equals()实现与之一致。
6. 标准hashCode()的实现

    我们用来计算哈希码的哈希算法越好，哈希表的性能就越好。
    让我们来看看一个 "标准 "的实现，它使用两个质数来为计算的哈希码增加更多的唯一性：

    ```java
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (int) id;
        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        hash = 31 * hash + (email == null ? 0 : email.hashCode());
        return hash;
    }
    ```

    虽然我们需要了解hashCode()和equals()方法所起的作用，但我们不必每次都从头实现它们。这是因为大多数IDE可以生成自定义的hashCode()和equals()实现。而且从Java 7开始，我们有一个Objects.hash()的实用方法来进行舒适的散列：
    `Objects.hash(name, email)`

    IntelliJ IDEA生成了以下实现：

    ```java
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    ```

    而Eclipse产生了这个：

    ```java
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    ```

    除了上述基于IDE的hashCode()实现外，还可以自动生成高效的实现，例如使用[Lombok](https://projectlombok.org/features/EqualsAndHashCode)。
    在这种情况下，我们需要在pom.xml中添加lombok-maven依赖项：

    ```xml
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-maven</artifactId>
        <version>1.16.18.0</version>
        <type>pom</type>
    </dependency>
    ```

    现在只需用@EqualsAndHashCode注解用户类即可：

    ```java
    @EqualsAndHashCode 
    public class User {
        // fields and methods here
    }
    ```

    同样，如果我们想让Apache Commons Lang的[HashCodeBuilder](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/builder/HashCodeBuilder.html)类为我们生成一个hashCode()实现，我们在pom文件中加入commons-lang的Maven依赖项：

    ```xml
    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
        <version>2.6</version>
    </dependency>
    ```

    而hashCode()可以像这样实现：

    ```java
    public class User {
        public int hashCode() {
            return new HashCodeBuilder(17, 37).
            append(id).
            append(name).
            append(email).
            toHashCode();
        }
    }
    ```

    一般来说，在实现hashCode()方面没有通用的配方。我们强烈建议阅读Joshua Bloch的[《Effective Java》](https://www.amazon.com/Effective-Java-3rd-Joshua-Bloch/dp/0134685997)。它为实现高效的散列算法提供了一份[详尽的指南](https://es.slideshare.net/MukkamalaKamal/joshua-bloch-effect-java-chapter-3)。
    注意到这里，所有这些实现都以某种形式利用了数字31。这是因为31有一个很好的属性。它的乘法可以用位移来代替，这比标准的乘法要快：

    `31 * i == (i << 5) - i`

7. 处理哈希碰撞

    散列表的内在行为带来了这些数据结构的一个相关方面： 即使有一个高效的散列算法，两个或更多的对象可能有相同的散列代码，即使它们不相等。因此，他们的哈希代码会指向同一个桶，即使他们有不同的哈希表键。
    这种情况通常被称为哈希碰撞，存在各种[处理方法](https://courses.cs.washington.edu/courses/cse373/18au/files/slides/lecture13.pdf)，每一种都有其优点和缺点。Java的HashMap使用[单独的链式方法](https://en.wikipedia.org/wiki/Hash_table#Separate_chaining_with_linked_lists)来处理碰撞问题：
    "当两个或更多的对象指向同一个桶时，它们被简单地存储在一个链接列表中。在这种情况下，哈希表是一个链接列表的数组，每个具有相同哈希值的对象都被附加到数组中的桶索引处的链接列表。
    在最坏的情况下，几个buckets会有一个链接列表与之绑定，列表中的一个对象的检索将以线性方式进行。"
    哈希碰撞方法简明扼要地展示了为什么有效地实现hashCode()是如此重要。
    Java 8给HashMap的实现带来了一个有趣的[增强](http://openjdk.java.net/jeps/180)。如果一个桶的大小超过了一定的阈值，一个树形图就会取代链表。这允许实现O(logn)查找，而不是悲观的O(n)。
8. 创建一个微不足道的应用程序

    现在我们来测试一下标准hashCode()实现的功能。
    让我们创建一个简单的Java应用程序，将一些用户对象添加到HashMap中，并在每次调用该方法时使用SLF4J将一条消息记录到控制台。
    下面是示例应用程序的入口点：

    ```java
    public class Application {
        public static void main(String[] args) {
            Map<User, User> users = new HashMap<>();
            User user1 = new User(1L, "John", "john@domain.com");
            User user2 = new User(2L, "Jennifer", "jennifer@domain.com");
            User user3 = new User(3L, "Mary", "mary@domain.com");
            users.put(user1, user1);
            users.put(user2, user2);
            users.put(user3, user3);
            if (users.containsKey(user1)) {
                System.out.print("User found in the collection");
            }
        }
    }
    ```

    这就是hashCode()的实现：

    ```java
    public class User {
        // ...
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (int) id;
            hash = 31 * hash + (name == null ? 0 : name.hashCode());
            hash = 31 * hash + (email == null ? 0 : email.hashCode());
            logger.info("hashCode() called - Computed hash: " + hash);
            return hash;
        }
    }
    ```

    在这里，需要注意的是，每当一个对象被存储在哈希图中，并用containsKey()方法检查时，hashCode()就会被调用，计算出的哈希代码会被打印到控制台：

    ```log
    [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: 1255477819
    [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: -282948472
    [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: -1540702691
    [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: 1255477819
    User found in the collection
    ```

9. 总结

    很明显，制作高效的hashCode()实现往往需要混合使用一些数学概念（即素数和任意数）、逻辑和基本数学运算。
    不管怎么说，我们完全可以不借助于这些技术来有效地实现hashCode()。我们只需要确保散列算法对不相等的对象产生不同的散列码，并且与equals()的实现一致。

## Relevant Articles

- [Methods in Java](https://www.baeldung.com/java-methods)
- [x] [Method Overloading and Overriding in Java](https://www.baeldung.com/java-method-overload-override)
- [Java equals() and hashCode() Contracts](https://www.baeldung.com/java-equals-hashcode-contracts)
- [x] [Guide to hashCode() in Java](https://www.baeldung.com/java-hashcode)
- [The Covariant Return Type in Java](https://www.baeldung.com/java-covariant-return-type)
- [Does a Method’s Signature Include the Return Type in Java?](https://www.baeldung.com/java-method-signature-return-type)
- [Solving the Hide Utility Class Public Constructor Sonar Warning](https://www.baeldung.com/java-sonar-hide-implicit-constructor)

## Code

像往常一样，本文中展示的所有代码样本都可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-oop-methods)上找到。
