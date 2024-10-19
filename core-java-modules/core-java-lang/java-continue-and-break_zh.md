# [Java continue and break关键词](https://www.baeldung.com/java-continue-and-break)

1. 概述

    在这篇短文中，我们将介绍continue和break这两个Java关键字，并重点介绍如何在实践中使用它们。

    简单地说，执行这些语句会导致当前控制流的分支，并终止当前迭代中代码的执行。

2. 断裂语句

    break语句有两种形式：无标记的和有标记的。

    ![break statement](pic/Illustration-1.jpg)

    1. 无标记的break

        我们可以使用无标签语句来终止for、while或do-while循环，以及switch-case块。

        ```java
        for (int i = 0; i < 5; i++) {
            if (i == 3) {
                break;
            }
        }
        ```

        这段代码定义了一个for循环，该循环应该迭代五次。但当计数器等于3时，if条件变为真，break语句终止了循环。这导致控制流被转移到for循环结束后的语句中。

        在嵌套循环的情况下，一个没有标记的break语句只终止它所在的内循环。外循环继续执行。

        ```java
        for (int rowNum = 0; rowNum < 3; rowNum++) {
            for (int colNum = 0; colNum < 4; colNum++) {
                if (colNum == 3) {
                    break;
                }
            }
        }
        ```

        这个片段有嵌套的for循环。当colNum等于3时，如果条件评估为真，break语句导致内部for循环终止。然而，外层for循环继续迭代。

    2. 标记的中断

        我们还可以使用一个带标签的break语句来终止for、while或do-while循环。带标签的中断语句可以终止外循环。

        终止后，控制流被转移到紧随外循环结束的语句。

        ```java
        compare:
        for (int rowNum = 0; rowNum < 3; rowNum++) {
            for (int colNum = 0; colNum < 4; colNum++) {
                if (rowNum == 1 && colNum == 3) {
                    break compare;
                }
            }
        }
        ```

        在这个例子中，我们在外循环之前引入了一个标签。当rowNum等于1，colNum等于3时，if条件评估为真，break语句终止外循环。

        然后控制流被转移到外循环结束后的语句。

3. 继续语句

    继续语句也有两种形式：无标签的和有标签的。

    ![continue](pic/Illustration-2.jpg)

    1. 无标签的continue

        我们可以使用无标签语句来绕过for、while或do-while循环的当前迭代中的其他语句的执行。它跳到内循环的末端并继续循环。

        ```java
        int counter = 0;
        for (int rowNum = 0; rowNum < 3; rowNum++) {
            for (int colNum = 0; colNum < 4; colNum++) {
                if (colNum != 3) {
                    continue;
                }
                counter++;
            }
        }
        ```

        在这个片段中，只要colNum不等于3，无标记的continue语句就会跳过当前的迭代，从而绕过该迭代中变量计数器的增量。然而，外层for循环继续迭代。因此，只有当colNum等于3的时候，外循环的每一次迭代才会发生计数器的递增。

    2. 标记的继续

        我们还可以使用一个标记的继续语句，跳过外循环。跳过后，控制流被转移到外循环的末端，有效地继续外循环的迭代。

        ```java
        int counter = 0;
        compare: 
        for (int rowNum = 0; rowNum < 3; rowNum++) {
            for (int colNum = 0; colNum < 4; colNum++) {
                if (colNum == 3) {
                    counter++;
                    continue compare;
                }
            }
        }
        ```

        我们在外循环之前引入了一个标签。每当colNum等于3时，变量计数器就会被递增。标记的continue语句使外循环的迭代跳过。

        控制流被转移到外循环的末端，继续进行下一次迭代。
