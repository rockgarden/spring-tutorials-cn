# [如何在Java中替换多个if语句](https://www.baeldung.com/java-replace-if-statements)

1. 概述

    决策结构是任何编程语言的重要组成部分。但是，我们在编写代码时会遇到大量嵌套的 if 语句，这使我们的代码变得更加复杂和难以维护。

    在本教程中，我们将介绍替换嵌套 if 语句的各种方法。

    让我们一起探索如何简化代码的各种方法。

2. 案例分析

    我们经常会遇到涉及大量条件的业务逻辑，而每个条件都需要不同的处理。为了便于演示，我们以计算器类为例。我们将有一个方法，它将两个数字和一个运算符作为输入，并根据运算返回结果：

    [Calculator.java](/src/main/java/com/baeldung/reducingifelse/Calculator.java)

    ```java
    public int calculate(int a, int b, String operator) {
        int result = Integer.MIN_VALUE;
        if ("add".equals(operator)) {
            result = a + b;
        } else if ("multiply".equals(operator)) {
            result = a * b;
        } else if ("divide".equals(operator)) {
            result = a / b;
        } else if ("subtract".equals(operator)) {
            result = a - b;
        }
        return result;
    }
    ```

    我们还可以使用 switch 语句实现这一功能：

    ```java
    public int calculateUsingSwitch(int a, int b, String operator) {
        switch (operator) {
        case "add":
            result = a + b;
            break;
        // other cases    
        }
        return result;
    }
    ```

    在典型的开发过程中，if 语句可能会变得更大、更复杂。此外，当存在复杂条件时，switch 语句也不太适用。

    嵌套决策结构的另一个副作用是它们变得难以管理。例如，如果我们需要添加一个新的操作符，就必须添加一个新的 if 语句并实现操作。

3. 重构

    让我们来探讨一下将上述复杂的 if 语句替换为更简单、更易于管理的代码的备选方案。

    1. 工厂类

        很多时候，我们会遇到在每个分支中都执行类似操作的决策构造。这就提供了一个提取工厂方法的机会，工厂方法返回给定类型的对象，并根据具体对象的行为执行操作。

        在我们的示例中，让我们定义一个 Operation 接口，它只有一个 apply 方法：

        ![Operation.java](/src/main/java/com/baeldung/reducingifelse/Operation.java)

        该方法将两个数字作为输入，并返回结果。让我们定义一个执行加法运算的类：

        ![Addition.java](/src/main/java/com/baeldung/reducingifelse/Addition.java)

        现在我们将实现一个工厂类，它将根据给定的操作符返回 Operation 的实例：

        ![OperatorFactory.java](/src/main/java/com/baeldung/reducingifelse/OperatorFactory.java)

        现在，在计算器类中，我们可以查询工厂以获取相关操作并应用于源数字：

        ```java
        public int calculateUsingFactory(int a, int b, String operator) {
            Operation targetOperation = OperatorFactory
            .getOperation(operator)
            .orElseThrow(() -> new IllegalArgumentException("Invalid Operator"));
            return targetOperation.apply(a, b);
        }
        ```

        在这个示例中，我们看到了如何将责任委托给由工厂类提供服务的松耦合对象。但是，嵌套的 if 语句有可能被简单地转移到工厂类，这就违背了我们的初衷。

        另外，我们也可以在 Map 中维护一个对象存储库，以便快速查询。正如我们所看到的，OperatorFactory#operationMap 就能达到我们的目的。我们还可以在运行时初始化 Map，并将其配置为用于查询。

    2. 枚举的使用

        除了使用 Map 之外，我们还可以使用枚举来标记特定的业务逻辑。之后，我们可以在嵌套的 if 语句或 switch case 语句中使用它们。或者，我们也可以将它们用作对象工厂，并让它们执行相关的业务逻辑。

        这样也可以减少嵌套 if 语句的数量，并将责任委托给各个枚举值。

        让我们看看如何实现这一点。首先，我们需要定义我们的枚举：

        ![Operator.java](/src/main/java/com/baeldung/reducingifelse/Operator.java)

        我们可以看到，这些值是不同运算符的标签，它们将被用于进一步计算。我们总是可以选择在嵌套的 if 语句或开关情况中使用这些值作为不同的条件，但让我们设计另一种方法，将逻辑委托给枚举本身。

        我们将为每个枚举值定义方法并进行计算。

        现在，我们可以使用 Operator#valueOf() 方法将字符串值转换为操作符，从而调用该方法：

        ```java
        @Test
        public void whenCalculateUsingEnumOperator_thenReturnCorrectResult() {
            Calculator calculator = new Calculator();
            int result = calculator.calculate(3, 4, Operator.valueOf("ADD"));
            assertEquals(7, result);
        }
        ```

    3. 命令模式

        在前面的讨论中，我们看到使用工厂类为给定的操作符返回正确的业务对象实例。之后，业务对象将用于在计算器中执行计算。

        我们还可以设计一个 Calculator#calculate 方法来接受一个可以在输入上执行的命令。这将是替代嵌套 if 语句的另一种方法。

        我们首先定义命令接口：

        ![Command.java](/src/main/java/com/baeldung/reducingifelse/Command.java)

        接下来，我们来实现：

        ![AddCommand.java](/src/main/java/com/baeldung/reducingifelse/AddCommand.java)

        最后，让我们在计算器中引入一个新方法，该方法接受并执行命令：

        Calculator.java\calculate(Command command)

        接下来，我们可以通过实例化 AddCommand 来调用计算，并将其发送到 Calculator#calculate 方法：

        ```java
        @Test
        public void whenCalculateUsingCommand_thenReturnCorrectResult() {
            Calculator calculator = new Calculator();
            int result = calculator.calculate(new AddCommand(3, 7));
            assertEquals(10, result);
        }
        ```

    4. 规则引擎

        当我们最终编写了大量嵌套的 if 语句时，每个条件都描述了一个业务规则，必须对该规则进行评估才能处理正确的逻辑。规则引擎可将这种复杂性从主代码中移除。规则引擎会对规则进行评估，并根据输入返回结果。

        让我们以设计一个简单的 RuleEngine 为例，通过一组规则来处理一个表达式，并返回所选规则的结果。首先，我们将定义一个 Rule 接口：

        ![Rule.java](/src/main/java/com/baeldung/reducingifelse/Rule.java)

        其次，我们来实现一个：

        ![RuleEngine.java](/src/main/java/com/baeldung/reducingifelse/RuleEngine.java)

        规则引擎接受 Expression 对象并返回结果。现在，让我们将 Expression 类设计为由两个整数对象和将要应用的运算符组成的组：

        ![Expression.java](/src/main/java/com/baeldung/reducingifelse/Expression.java)

        最后，让我们定义一个自定义的 AddRule 类，它仅在指定 ADD 操作时进行求值：

        ![AddRule.java](/src/main/java/com/baeldung/reducingifelse/AddRule.java)

        现在，我们将使用表达式调用 RuleEngine：

        ```java
        @Test
        public void whenCalculateUsingCommand_thenReturnCorrectResult() {
            Calculator calculator = new Calculator();
            int result = calculator.calculate(new AddCommand(3, 7));
            assertEquals(10, result);
        }
        ```

4. 结论

    在本教程中，我们探索了许多简化复杂代码的不同选项。我们还学习了如何使用有效的设计模式来替换嵌套的 if 语句。
