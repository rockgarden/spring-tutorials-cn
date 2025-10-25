# [Java Switch 语句](https://www.baeldung.com/java-switch)

核心 Java 定义 · Java 语句参考

1. 概述

    在本教程中，我们将学习 switch 语句是什么以及如何使用它。

    `switch` 语句可以替代多个嵌套的 `if-else` 结构，从而提升代码可读性。

    `switch` 语句随着 Java 版本不断演进：

    - Java 5 引入了对 `enum` 类型的支持；
    - Java 7 增加了对 `String` 类型的支持；
    - Java 12 起引入了 switch 表达式（switch expressions）（预览功能），并在后续版本中正式化。

    下文将通过代码示例展示：

    - `switch` 的基本用法；
    - `break` 语句的作用；
    - `switch` 参数与 `case` 值的要求；
    - `String` 在 `switch` 中的比较机制。

2. 基本用法示例

    假设我们有如下嵌套的 `if-else` 代码：

    ```java
    public String exampleOfIF(String animal) {
        String result;
        if (animal.equals("DOG") || animal.equals("CAT")) {
            result = "domestic animal";
        } else if (animal.equals("TIGER")) {
            result = "wild animal";
        } else {
            result = "unknown animal";
        }
        return result;
    }
    ```

    这段代码可读性差，难以维护。

    使用 `switch` 语句可显著改善：

    ```java
    public String exampleOfSwitch(String animal) {
        String result;
        switch (animal) {
            case "DOG":
                result = "domestic animal";
                break;
            case "CAT":
                result = "domestic animal";
                break;
            case "TIGER":
                result = "wild animal";
                break;
            default:
                result = "unknown animal";
                break;
        }
        return result;
    }
    ```

    - `switch` 将 `animal` 与各个 `case` 值进行比较；
    - 若无匹配项，则执行 `default` 分支；
    - `break` 用于跳出 `switch`，防止“穿透”（fall-through）。

3. `break` 语句的重要性

    虽然大多数情况下我们只希望执行一个 `case`，但 `break` 是必需的。若省略，程序会继续执行后续 `case` 的代码。

    **示例（缺少 `break`）：**

    ```java
    public void forgetBreakInSwitch(String animal) {
        switch (animal) {
            case "DOG":
                System.out.println("domestic animal");
            default:
                System.out.println("unknown animal");
        }
    }
    ```

    调用 `forgetBreakInSwitch("DOG")` 的输出为：

    ```log
    domestic animal
    unknown animal
    ```

    > 这种行为称为 **fall-through（穿透）**。虽然通常应避免，但也可有意利用它来为多个 `case` 共享同一段逻辑。

    **优化写法（合并 `case`）：**

    ```java
    switch (animal) {
        case "DOG":
        case "CAT":
            result = "domestic animal";
            break;
        case "TIGER":
            result = "wild animal";
            break;
        default:
            result = "unknown animal";
            break;
    }
    ```

4. `switch` 参数与 `case` 值的要求

    1. 支持的数据类型

        `switch` 仅支持以下类型：

        - 基本类型：`byte`、`short`、`int`、`char`
        - 对应包装类：`Byte`、`Short`、`Integer`、`Character`
        - `String`（自 **Java 7** 起支持）
        - `enum`（自 **Java 5** 起支持）

        > `switch` 表达式和所有 `case` 值必须是**相同类型**。

    2. 不允许 `null`

        - **`switch` 参数不能为 `null`**，否则抛出 `NullPointerException`：

        ```java
        @Test(expected = NullPointerException.class)
        public void whenSwitchArgumentIsNull_thenNullPointerException() {
            String animal = null;
            s.exampleOfSwitch(animal); // 抛出异常
        }
        ```

        - **`case` 标签也不能是 `null`**，否则编译失败。

    3. `case` 值必须是编译期常量

        `case` 值必须是**编译时常量表达式(Compile-Time Constants)**。例如：

        ```java
        final String dog = "DOG";  // 编译通过
        String cat = "CAT";        // 非 final，编译失败

        switch (animal) {
            case dog:  // OK
                // ...
            case cat:  // 编译错误！
                // ...
        }
        ```

    4. `String` 比较使用 `equals()`

        `switch` 对 `String` 的比较**内部使用 `equals()` 方法**，而非 `==`。

        因此，即使 `String` 是通过 `new` 创建的，也能正确匹配：

        ```java
        @Test
        public void whenCompareStrings_thenByEqual() {
            String animal = new String("DOG"); // 新对象
            assertEquals("domestic animal", s.exampleOfSwitch(animal)); // 通过
        }
        ```

5. Switch 表达式（Java 12+）

    从 **Java 12** 开始引入 **switch 表达式**（Java 14 起正式标准化），提供更简洁、安全的语法。

    1. 新语法：`->` 与无穿透

        ```java
        var result = switch (month) {
            case JANUARY, JUNE, JULY -> 3;
            case FEBRUARY, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER -> 1;
            case MARCH, MAY, APRIL, AUGUST -> 2;
            default -> 0;
        };
        ```

        - 使用 `->` 替代 `:`
        - **自动跳出**，无需 `break`
        - 支持**逗号分隔多个 case**

    2. `yield` 关键字（用于代码块）

        当需要在 `->` 右侧执行多行逻辑时，使用 `{}` 和 `yield` 返回值：

        ```java
        var result = switch (month) {
            case JANUARY, JUNE, JULY -> 3;
            case FEBRUARY, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER -> 1;
            case MARCH, MAY, APRIL, AUGUST -> {
                int monthLength = month.toString().length();
                yield monthLength * 4; // 返回值
            }
            default -> 0;
        };
        ```

    3. `return` 的限制

        - 在 **switch 语句** 中可以使用 `return`：

        ```java
        switch (month) {
            case JANUARY -> { return 3; }
            default -> { return 0; }
        }
        ```

        - 但在 **switch 表达式** 中**不能使用 `return`**（因为表达式本身返回值）：

        ```java
        // 编译错误！
        var result = switch (month) {
            case JANUARY -> { return 3; } // 不允许
            default -> 0;
        };
        ```

    4. 穷尽性检查（Exhaustiveness）

        - **switch 语句**：不要求覆盖所有情况。
        - **switch 表达式**：**必须覆盖所有可能值**（或提供 `default`）。

        有效（覆盖全部 `enum` 值）：

        ```java
        var result = switch (month) {
            case JANUARY, JUNE, JULY -> 3;
            case FEBRUARY, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER -> 1;
            case MARCH, MAY, APRIL, AUGUST -> 2;
        }; // 无 default，但所有 case 已覆盖 → 合法
        ```

        无效（未覆盖全部）：

        ```java
        var result = switch (month) {
            case JANUARY -> 3;
            case FEBRUARY -> 1;
        }; // 编译错误！缺少 default 且未穷尽
        ```

6. 结论

    本文深入探讨了 Java 中 `switch` 语句的细节。是否使用 `switch` 应基于：

    - **可读性**
    - **被比较值的类型和数量**

    `switch` 特别适合处理**有限且预定义的选项集**（如星期、月份、状态码等）。

    但若选项频繁变化，或逻辑复杂，应考虑其他设计方式，例如：

    - **多态（Polymorphism）**
    - **策略模式（Strategy Pattern）**
    - **命令模式（Command Pattern）**

    合理选择控制结构，才能写出清晰、可维护、可扩展的代码。
