# [Java equals() 和 hashCode() 合同](https://www.baeldung.com/java-equals-hashcode-contracts)

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

    Project Lombok 还提供了 @EqualsAndHashCode 注解。请再次注意 equals() 和 hashCode() 是如何 "结合" 在一起的，甚至有一个共同的注解。

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
