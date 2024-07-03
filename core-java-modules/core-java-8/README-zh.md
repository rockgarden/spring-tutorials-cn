# Java8核心

本模块包含有关 Java 8 核心功能的文章

- Java 8 中的新功能](https://www.baeldung.com/java-8-new-features)
- Java 8 中的策略设计模式](https://www.baeldung.com/java-strategy-pattern)
- Java 8 Comparator.comparing() 指南](https://www.baeldung.com/java-8-comparator-comparing)
- Java 8 forEach 指南](https://www.baeldung.com/foreach-java)
- Java 中的 Spliterator 简介](https://www.baeldung.com/java-spliterator)
- 使用 Java 查找数组中的最小值/最大值](https://www.baeldung.com/java-array-min-max)
- Java 8 中的国际化和本地化](https://www.baeldung.com/java-8-localization)
- Java 中的广义目标类型推断](https://www.baeldung.com/java-generalized-target-type-inference)
- Java 中的单子](https://www.baeldung.com/java-monads)
- [[更多-->]](/core-java-modules/core-java-8-2)

## Java8的新功能

1. 概述

    在本教程中，我们将快速了解 Java 8 中一些最有趣的新功能。

    我们将讨论接口默认方法和静态方法、方法引用和可选方法。

    我们已经介绍了 Java 8 中的一些功能--[流 API](https://www.baeldung.com/java-8-streams-introduction)、[lambda 表达式和函数式接口](https://www.baeldung.com/java-8-lambda-expressions-tips)--因为它们是值得单独介绍的综合主题。

2. 接口默认方法和静态方法

    在 Java 8 之前，接口只能拥有公共抽象方法。如果不强制所有实现类创建新方法的实现，就无法为现有接口添加新功能，也无法创建具有实现的接口方法。

    从 Java 8 开始，接口可以拥有静态方法和默认方法，尽管这些方法是在接口中声明的，但却具有已定义的行为。

    1. 静态方法

        请看接口的这个方法（我们称其为接口 Vehicle）：

        ```java
        static String producer() {
            return "N&F Vehicles";
        }
        ```

        static producer() 方法只能在接口内部使用。实现类不能重载该方法。

        要在接口外调用该方法，应使用静态方法调用的标准方法：

        `String producer = Vehicle.producer();`
    2. 默认方法

        默认方法使用新的 default 关键字声明。这些方法可通过实现类的实例访问，并可被重载。

        让我们为 Vehicle 接口添加一个默认方法，该方法也将调用该接口的静态方法：

        ```java
        default String getOverview() {
            return "ATV made by " + producer();
        }
        ```

        假设该接口由类 VehicleImpl 实现。

        为执行默认方法，应创建该类的实例：

        ```java
        Vehicle vehicle = new VehicleImpl();
        String overview = vehicle.getOverview();
        ```

3. 方法引用

    对于只调用现有方法的 lambda 表达式来说，方法引用是一种更简短、更易读的替代方法。方法引用有四种变体。

    1. 静态方法引用

        静态方法引用的语法为 ContainingClass::methodName。

        在 Stream API 的帮助下，我们将尝试统计 `List<String>` 中的所有空字符串：

        `boolean isReal = list.stream().anyMatch(u -> User.isRealUser(u));`
        让我们仔细看看 anyMatch() 方法中的 lambda 表达式。它只是调用了 User 类的静态方法 isRealUser(User user)。

        因此，可以用静态方法的引用来代替它：

        `boolean isReal = list.stream().anyMatch(User::isRealUser);`
        这样的代码看起来信息量更大。

    2. 实例方法引用

        对实例方法的引用使用包含 Instance::methodName 的语法。

        下面的代码调用了 User 类型的方法 isLegalName(String string)，该方法用于验证输入参数：

        ```java
        User user = new User();
        boolean isLegalName = list.stream().anyMatch(user::isLegalName);
        ```

    3. 引用特定类型对象的实例方法

        这种引用方法的语法为 ContainingType::methodName。

        下面我们来看一个示例：

        `long count = list.stream().filter(String::isEmpty).count();`
    4. 构造函数引用

        对构造函数的引用使用的语法是 ClassName::new。

        由于 Java 中的构造函数是一种特殊的方法，因此也可以使用方法引用来引用构造函数，并将 new 作为方法名称：

        `Stream<User> stream = list.stream().map(User::new);`
4. 可选`<T>`

    在 Java 8 之前，开发人员必须仔细验证所引用的值，因为有可能抛出 NullPointerException (NPE)。所有这些检查都需要一段相当恼人且容易出错的模板代码。

    Java 8 `Optional<T>` 类可以帮助处理可能出现 NPE 的情况。该类是 T 类型对象的容器。如果该值不是空值，它可以返回该对象的值。当容器中的值为空时，它允许执行一些预定义的操作，而不是抛出 NPE。

    1. 创建`Optional<T>`

        可选类的实例可以借助其静态方法创建。

        让我们来看看如何返回一个空的 Optional：

        `Optional<String> optional = Optional.empty();`
        接下来，我们返回一个包含非空值的 Optional：

        ```java
        String str = "value";
        Optional<String> optional = Optional.of(str);
        ```

        最后，下面是如何返回一个包含特定值的可选项，或者在参数为空的情况下返回一个空的可选项：

        `Optional<String> optional = Optional.ofNullable(getString());`
    2. 可选项`<T>`用法

        假设我们希望得到一个 `List<String>`，如果参数为空，我们希望用一个 `ArrayList<String>` 的新实例来代替它。

        使用 Java 8 之前的代码，我们需要这样做

        ```java
        List<String> list = getList();
        List<String> listOpt = list != null ? list : new ArrayList<>();
        ```

        在 Java 8 中，同样的功能可以用更短的代码实现：

        `List<String> listOpt = getList().orElseGet(() -> new ArrayList<>());`
        当我们需要用老方法访问某个对象的字段时，模板代码就更多了。

        假设我们有一个 User 类型的对象，它有一个 Address 类型的字段和一个 String 类型的街道字段，如果街道字段存在，我们需要返回街道字段的值，如果街道字段为空，则返回默认值：

        ```java
        User user = getUser();
        if (user != null) {
            Address address = user.getAddress();
            if (address != null) {
                String street = address.getStreet();
                if (street != null) {
                    return street;
                }
            }
        }
        return "not specified";
        ```

        使用 Optional 可以简化这一过程：

        ```java
        Optional<User> user = Optional.ofNullable(getUser());
        String result = user
        .map(User::getAddress)
        .map(Address::getStreet)
        .orElse("not specified");
        ```

        在本例中，我们使用 map() 方法将调用 getAdress() 的结果转换为 `Optional<Address>`，将 getStreet() 的结果转换为 `Optional<String>`。如果其中任何一个方法返回空值，map() 方法将返回一个空的 Optional。

        现在假设我们的获取器返回 `Optional<T>`。

        在这种情况下，我们应该使用 flatMap() 方法，而不是 map()：

        ```java
        Optional<OptionalUser> optionalUser = Optional.ofNullable(getOptionalUser());
        String result = optionalUser
        .flatMap(OptionalUser::getAddress)
        .flatMap(OptionalAddress::getStreet)
        .orElse("not specified");
        ```

        可选项的另一个用例是用另一个异常改变 NPE。

        因此，与之前一样，让我们尝试用 Java 8 之前的风格来做这件事：

        ```java
        String value = null;
        String result = "";
        try {
            result = value.toUpperCase();
        } catch (NullPointerException exception) {
            throw new CustomException();
        }
        ```

        如果我们使用 `Optional<String>`，答案会更易读、更简单：

        ```java
        String value = null;
        Optional<String> valueOpt = Optional.ofNullable(value);
        String result = valueOpt.orElseThrow(CustomException::new).toUpperCase();
        ```

        请注意，如何在应用程序中使用 Optional 以及用于何种目的是一个严肃而有争议的设计决策，对其所有利弊的解释不在本文讨论范围之内。不过，有很多有趣的文章专门讨论这个问题。[这篇文章](http://blog.joda.org/2014/11/optional-in-java-se-8.html)和[这篇文章](http://blog.joda.org/2015/08/java-se-8-optional-pragmatic-approach.html)可能会对深入研究有所帮助。

5. 结论

    在本文中，我们简要讨论了 Java 8 中一些有趣的新功能。

    当然，Java 8 JDK 的许多包和类中还有许多其他新增功能和改进。

    但是，本文介绍的信息是探索和学习其中一些新功能的良好起点。

## Code

最后，本文的所有源代码均可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-8) 上获取。
