# [Java中的比较器和可比性](https://www.baeldung.com/java-comparator-comparable)

1. 简介

    在Java中，比较是很容易的，直到它们不那么容易。

    当使用自定义类型时，或者试图比较不能直接比较的对象时，我们需要使用一个比较策略。我们可以通过使用比较器或可比较接口来建立一个比较策略。

2. 设置实例

    让我们用一个足球队的例子来说明，我们想按照球员的排名来排队。

    我们将从创建一个简单的球员类开始。

    ```java
    public class Player {
        private int ranking;
        private String name;
        private int age;
        // constructor, getters, setters  
    }
    ```

    接下来，我们将创建一个PlayerSorter类来创建我们的集合，并尝试使用Collections.sort对其进行排序。

    comparable/PlayerSorter.java

    正如所料，这导致了一个编译时错误。

    ```log
    The method sort(List<T>) in the type Collections 
    is not applicable for the arguments (ArrayList<Player>)
    ```

    现在让我们试着理解我们在这里做错了什么。

3. 可比

    顾名思义，Comparable是一个接口，定义了一个对象与其他相同类型的对象进行比较的策略。这被称为该类的 "自然排序"。

    为了能够进行排序，我们必须通过实现可比较接口将我们的播放器对象定义为可比较对象。

    `public class Player implements Comparable<Player>`

    comparable/Player.java

    排序顺序是由compareTo()方法的返回值决定的。Integer.compare(x, y)如果x小于y，返回-1，如果它们相等，返回0，否则返回1。

    该方法返回一个数字，表明被比较的对象是否小于、等于或大于作为参数传递的对象。

    现在当我们运行我们的PlayerSorter时，我们可以看到我们的球员按照他们的排名进行排序。

    ```log
    Before Sorting : [John, Roger, Steven]
    After Sorting : [Steven, John, Roger]
    ```

    现在我们对使用Comparable的自然排序有了清楚的了解，让我们看看如何以比直接实现接口更灵活的方式使用其他类型的排序。

4. 比较器

    比较器接口定义了一个有两个参数的compare(arg1, arg2)方法，这两个参数代表被比较的对象，其工作原理与Comparable.compareTo()方法类似。

    1. 创建比较器

        为了创建一个比较器，我们必须实现比较器接口。

        对于我们的第一个例子，我们将创建一个比较器来使用Player的排名属性来对球员进行排序。

        comparator/PlayerRankingComparator.java

        同样地，我们可以创建一个比较器来使用播放器的年龄属性来对球员进行排序。

        comparator/PlayerAgeComparator.java

    2. 行动中的比较器

        为了演示这个概念，让我们修改我们的PlayerSorter，为Collections.sort方法引入第二个参数，这实际上是我们想要使用的比较器的实例。

        使用这种方法，我们可以覆盖自然排序。

        ```java
        // comparator/PlayerRankingSorter.java
        PlayerRankingComparator playerComparator = new PlayerRankingComparator();
        Collections.sort(footballTeam, playerComparator);
        ```

        现在让我们运行我们的 PlayerRankingSorter 来看看结果。

        ```log
        Before Sorting : [John, Roger, Steven]
        After Sorting by age : [Roger, John, Steven]
        ```

        如果我们想要一个不同的排序顺序，我们只需要改变我们使用的比较器。

        ```java
        // comparator/PlayerAgeSorter.java
        PlayerAgeComparator playerComparator = new PlayerAgeComparator();
        Collections.sort(footballTeam, playerComparator);
        ```

        现在当我们运行我们的PlayerAgeSorter时，我们可以看到不同的按年龄排序的顺序。

        ```log
        Before Sorting : [John, Roger, Steven]
        After Sorting by age : [Roger, John, Steven]
        ```

    3. Java 8的比较器

        Java 8提供了通过使用lambda表达式和comparing()静态工厂方法来定义比较器的新方法。

        参见 Java8ComparatorUnitTest.java

        让我们看看如何使用lambda表达式来创建比较器的一个快速例子。

        ```java
        Comparator byRanking = 
        (Player player1, Player player2) -> Integer.compare(player1.getRanking(), player2.getRanking());
        ```

        Comparator.comparing方法接收一个计算将用于比较项目的属性的方法，并返回一个匹配的比较器实例。

        ```java
        Comparator<Player> byRanking = Comparator
        .comparing(Player::getRanking);
        Comparator<Player> byAge = Comparator
        .comparing(Player::getAge);
        ```

5. 比较器与可比较接口

    对于定义默认排序，或者换句话说，如果它是比较对象的主要方式，那么使用Comparable接口是个不错的选择。

    那么如果我们已经有了Comparable，为什么还要使用 Comparator 呢？

    有几个原因。

    - 有时我们无法修改我们想要排序的对象的类的源代码，因此不可能使用Comparable
    - 使用Comparator可以让我们避免在我们的领域类中添加额外的代码
    - 我们可以定义多种不同的比较策略，这在使用Comparable是不可能的

6. 避开减法技巧

    在本教程的过程中，我们已经使用Integer.compare()方法来比较两个整数。然而，有人可能会说，我们应该用这个聪明的 one-liner 方法来代替。

    `Comparator<Player> comparator = (p1, p2) -> p1.getRanking() - p2.getRanking();`

    虽然它比其他解决方案要简洁得多，但在Java中它可能是整数溢出的受害者。

    Comparator/AvoidingSubtractionUnitTest.java

    由于-1远远小于Integer.MAX_VALUE，在排序后的集合中，"Roger"应该排在 "John"之前。然而，由于整数溢出，"Integer.MAX_VALUE - (-1)"将小于零。所以根据比较器/可比较契约，Integer.MAX_VALUE小于-1，这显然是不正确的。

    因此，尽管我们预期，在排序的集合中 "John"排在 "Roger"之前。

    > 运行结果: Roger,John 正常？

7. 总结

    在这篇文章中，我们探讨了Comparable和Comparator接口，并讨论了它们之间的区别。

    要了解更多关于排序的高级话题，请查看我们的其他文章，如[Java 8 Comparator.comparing](https://www.baeldung.com/java-8-comparator-comparing)，以及[Java 8与Lambdas的比较](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lambdas)。
