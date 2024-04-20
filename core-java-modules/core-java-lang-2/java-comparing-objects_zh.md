# [比较Java中的对象](https://www.baeldung.com/java-comparing-objects)

1. 简介

    比较对象是面向对象编程语言的一项基本功能。

    在本教程中，我们将探讨 Java 语言中允许我们比较对象的一些特性。我们还将了解外部库中的此类功能。

2. == 和 != 操作符

    让我们从 == 和 != 操作符开始，它们可以分别判断两个 Java 对象是否相同。

    1. 基元类型

        对于基元类型，相同意味着值相等：

        `assertThat(1 == 1).isTrue();`

        得益于自动开箱功能，在比较基元值与其封装类型对应值时，这也同样有效：

        ```java
        Integer a = new Integer(1);
        assertThat(1 == a).isTrue();
        ```

        如果两个整数的值不同，则 == 运算符将返回 false，而 != 运算符将返回 true。

    2. 对象

        参见：EqualityOperatorUnitTest.java

        假设我们要比较两个具有相同值的 Integer 封装类型：

        givenTwoIntegersWithSameValues_whenEqualityOperators_thenNotConsideredSame()

        通过比较两个对象，这些对象的值并不是 1，而是它们在[堆栈中的内存地址](https://www.baeldung.com/java-stack-heap)不同，因为这两个对象都是使用 new 操作符创建的。如果我们将 a 赋值给 b，就会得到不同的结果：

        givenTwoIntegersWithSameReference_whenEqualityOperators_thenConsideredSame()

        现在让我们看看使用 Integer#valueOf 工厂方法会发生什么：

        givenTwoIntegersFromValueOfWithSameValues_whenEqualityOperators_thenConsideredSame()

        在这种情况下，它们被认为是相同的。这是因为 valueOf() 方法将 Integer 保存在缓存中，以避免创建过多具有相同值的封装对象。因此，该方法在两次调用中返回的是同一个 Integer 实例。

        Java 对字符串也是如此：

        `assertThat("Hello!" == "Hello!").isTrue();`

        但是，如果它们是使用 new 操作符创建的，那么它们就不一样了。

        最后，两个 null 引用被视为相同，而任何非 null 对象都被视为不同于 null：

        ```java
        assertThat(null == null).isTrue();
        assertThat("Hello!" == null).isFalse();
        ```

        当然，相等运算符的行为可能会有限制。如果我们想比较映射到不同地址的两个对象，但又想根据它们的内部状态将其视为相等，该怎么办？我们将在接下来的章节中了解如何实现这一点。

3. 对象#equals方法

    现在，让我们用 equals() 方法来谈谈更广泛的相等概念。

    该方法定义在对象类中，因此每个 Java 对象都继承了它。默认情况下，该方法的实现是比较对象的内存地址，因此其工作原理与 == 运算符相同。不过，我们可以重写该方法，以定义对象的相等含义。

    首先，让我们看看它对现有对象（如 Integer）的作用：

    givenTwoIntegersWithSameValues_whenEquals_thenNotConsideredSame()

    当两个对象相同时，该方法仍然返回 true。

    我们应该注意到，我们可以传递一个空对象作为方法的参数，但不能作为我们调用方法的对象。

    我们还可以将 equals() 方法用于我们自己的对象。假设我们有一个 PersonWithoutEquals 类。

    我们可以重载该类的 equals() 方法，这样就可以根据两个人的内部细节对其进行比较：

    参见：PersonWithEquals.java equals(Object o)

    如需了解更多信息，请查看我们有关此主题的[文章](https://www.baeldung.com/java-equals-hashcode-contracts)。

4. Objects#equals 静态方法

    现在我们来看看 [Objects#equals](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Objects.html#equals(java.lang.Object,java.lang.Object)) 静态方法。前面我们提到，我们不能使用 null 作为第一个对象的值，否则会抛出 NullPointerException。

    Objects 辅助类的 equals() 方法解决了这个问题。它接受两个参数并对它们进行比较，同时也处理空值。

    让我们再次比较一下 Person 对象：

    ```java
    Person joe = new Person("Joe", "Portman");
    Person joeAgain = new Person("Joe", "Portman");
    Person natalie = new Person("Natalie", "Portman");

    assertThat(Objects.equals(joe, joeAgain)).isTrue();
    assertThat(Objects.equals(joe, natalie)).isFalse();
    ```

    正如我们解释过的，该方法处理空值。因此，如果两个参数都为空，它将返回 true；如果只有一个参数为空，它将返回 false。

    这确实很方便。比方说，我们想在 PersonWithEquals 类中添加一个可选的出生日期。

    然后，我们必须更新 equals() 方法，但要进行空处理。我们可以在 equals() 方法中添加以下条件：

    `birthDate == null ? that.birthDate == null : birthDate.equals(that.birthDate);`

    但是，如果我们在类中添加了太多 nullable 字段，就会变得非常混乱。在我们的 equals() 实现中使用 Objects#equals 方法要简洁得多，也提高了可读性：

    `Objects.equals(birthDate, that.birthDate);`

5. 可比较接口

    比较逻辑还可用于按特定顺序排列对象。通过 [Comparable](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Comparable.html) 接口，我们可以确定一个对象是大于、等于还是小于另一个对象，从而定义对象之间的排序。

    Comparable 接口是通用的，只有一个方法 compareTo()，它接收一个通用类型的参数并返回一个 int。如果该值小于参数值，返回值为负数；如果两者相等，返回值为 0，否则为正数。

    比方说，在我们的 Person 类中，我们想按姓氏比较 Person 对象：

    ```java
    public class Person implements Comparable<Person> {
        //...
        @Override
        public int compareTo(Person o) {
            return this.lastName.compareTo(o.lastName);
        }
    }
    ```

    如果调用的 Person 的姓氏大于 this，compareTo() 方法将返回负数 int；如果姓氏相同，则返回 0；否则返回正数。

    如需了解更多信息，请参阅我们有关此主题的[文章](https://www.baeldung.com/java-comparator-comparable)。

6. 比较器接口

    [Comparator接口](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Comparator.html)是一个通用接口，它有一个比较方法，该方法接收两个通用类型的参数并返回一个整数。我们在前面的 Comparable 接口中已经看到了这种模式。

    Comparator 与之类似，但它与类的定义是分开的。因此，我们可以为一个类定义任意多个 Comparator，但只能提供一个 Comparable 实现。

    假设我们有一个在表格视图中显示人物的网页，我们希望为用户提供按名字而不是姓氏排序的可能性。如果我们还想保留当前的实现，Comparable 就无法实现这一功能，但我们可以实现自己的比较器。

    让我们创建一个 Person 比较器，它将只按名字进行比较：

    `Comparator<Person> compareByFirstNames = Comparator.comparing(Person::getFirstName);`

    现在，让我们使用该比较器对人员列表进行排序：

    ```java
    Person joe = new Person("Joe", "Portman");
    Person allan = new Person("Allan", "Dale");
    List<Person> people = new ArrayList<>();
    people.add(joe);
    people.add(allan);
    people.sort(compareByFirstNames);
    assertThat(people).containsExactly(allan, joe);
    ```

    在我们的 compareTo() 实现中，还可以使用比较器接口上的其他方法：

    PersonWithEqualsAndComparableUsingComparator.java compareTo()

    在本例中，我们首先比较姓，然后比较名。接下来，我们比较出生日期，但由于它们是空值，我们必须说明如何处理。为此，我们给出了第二个参数，说明应按照自然顺序比较，空值放在最后。

7. Apache 共享资源

    让我们来看看 [Apache Commons](https://www.baeldung.com/java-commons-lang-3) 库。首先，让我们导入 Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
        <version>3.12.0</version>
    </dependency>
    ```

    参见：ApacheCommonsObjectUtilsUnitTest.java

    1. ObjectUtils#notEqual 方法

        首先，让我们来谈谈 ObjectUtils#notEqual 方法。它接受两个对象参数，根据它们自己的 equals() 方法实现来确定它们是否不相等。它还可以处理空值。

        让我们重用字符串示例：

        givenTwoStringsWithSameValues_whenApacheCommonsEqualityMethods_thenEqualsTrueNotEqualsFalse()

        需要指出的是，ObjectUtils 有一个 equals() 方法。不过，自 Java 7 出现 Objects#equals 后，该方法已被弃用。

    2. ObjectUtils#compare 方法

        现在，让我们使用 ObjectUtils#compare 方法来比较对象的顺序。这是一个通用方法，它接收两个通用类型的可比较参数，并返回一个整数。

        让我们再次使用字符串进行比较：

        givenTwoStringsWithConsecutiveValues_whenApacheCommonsCompare_thenNegative()

        默认情况下，该方法处理空值时会将其视为较大值。该方法还提供了一个重载版本，通过布尔参数反转该行为，将空值视为较小的值。

8. Guava

    让我们来看看 [Guava](https://guava.dev/)。首先，让我们导入依赖关系：

    ```xml
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>31.0.1-jre</version>
    </dependency>
    ```

    参见：GuavaUnitTest.java

    1. 对象#equal 方法

        与 Apache Commons 库类似，Google 也为我们提供了一种确定两个对象是否相等的方法，即 Objects#equal。虽然它们的实现不同，但返回的结果相同：

        ObjectsEqualMethod givenTwoStringsWithSameValues_whenObjectsEqualMethods_thenTrue()

        虽然该方法未被标记为已废弃，但 JavaDoc 中说，由于 Java 7 提供了 Objects#equals 方法，因此应将其视为已废弃方法。

    2. 比较方法

        Guava 库没有提供比较两个对象的方法（我们将在下一节了解如何实现这一目标），但它提供了比较基元值的方法。让我们使用 Ints 辅助类，看看它的 compare() 方法是如何工作的：

        `assertThat(Ints.compare(1, 2)).isNegative();`

        像往常一样，如果第一个参数小于、等于或大于第二个参数，它将返回一个可能为负、零或正的整数。除字节外，所有基元类型都有类似的方法。

    3. 比较链类

        最后，Guava 库提供了 ComparisonChain 类，它允许我们通过比较链来比较两个对象。我们可以通过名字和姓氏轻松比较两个 Person 对象：

        ComparisonChainClass givenTwoPersonWithEquals_whenComparisonChainByLastNameThenFirstName_thenSortedJoeFirstAndNatalieSecond()

        底层比较是通过 compareTo() 方法实现的，因此传递给 compare() 方法的参数必须是基元参数或可比较参数。

9. 结论

    在本文中，我们学习了在 Java 中比较对象的不同方法。我们研究了相同、相等和排序之间的区别。我们还了解了 Apache Commons 和 Guava 库中的相应功能。
