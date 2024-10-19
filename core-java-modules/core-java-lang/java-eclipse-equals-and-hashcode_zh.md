# [用Eclipse生成equals()和hashCode()](https://www.baeldung.com/java-eclipse-equals-and-hashcode)

1. 简介

    在这篇文章中，我们将探讨如何使用 Eclipse IDE 生成 equals() 和 hashCode() 方法。我们将说明Eclipse的代码自动生成功能是多么强大和方便，同时也强调对代码进行勤奋的测试仍然是必要的。

2. 规则

    Java中的equals()是用来检查2个对象是否相等的。测试的一个好方法是确保对象是对称的、反身的和传递的。也就是说，对于三个非空对象a、b和c。

    - 对称的 Symmetric - a.equals(b)当且仅当b.equals(a)。
    - 反射的 Reflexive - a.equals(a)
    - 传递性 Transitive - 如果a.equals(b)和b.equals(c)，那么a.equals(c)

    hashCode()必须遵守一条规则。

    - 2个被equals()的对象必须有相同的hashCode()值。

3. 带有原语的类

    让我们考虑一个只由原始成员变量组成的Java类。

    equalshashcode.entities/PrimitiveClass.java

    我们使用Eclipse IDE，用'Source->Generate hashCode() and equals()'来生成equals()和hashCode()。

    我们可以通过选择'Select All'来确保所有的成员变量都包括在内。

    请注意，在插入点下面列出的选项：影响生成的代码的风格。在这里，我们不选择任何一个选项，选择'OK'，hashCode()、equals(Object obj)方法就被添加到我们的类中。

    生成的hashCode()方法以一个质数（31）的声明开始，对原始对象进行各种操作，并根据对象的状态返回其结果。

    equals()首先检查两个对象是否是同一个实例（==），如果是则返回真。

    接下来，它检查比较对象是否为非空，两个对象是否属于同一类别，如果不是则返回false。

    最后，equals()检查每个成员变量是否相等，如果其中任何一个不相等，则返回false。

    所以我们可以编写简单的测试。

    参见 PrimitiveClassUnitTest.java

4. 带有集合和泛型的类

    现在，让我们考虑一个带有集合和泛型的更复杂的Java类。

    equalshashcode.entities/ComplexClass.java

    我们再次使用Eclipse'Source->Generate hashCode() and equals()'。注意hashCode()使用instanceOf来比较类对象，因为我们在对话框的Eclipse选项中选择了'使用'instanceof'来比较类型'。

    生成的hashCode()方法依赖于AbstractList.hashCode()和AbstractSet.hashCode()的核心Java方法。这些方法遍历一个集合，将每个项目的hashCode()值相加，并返回一个结果。

    同样，生成的equals()方法使用AbstractList.equals()和AbstractSet.equals()，它们通过比较集合的字段来比较它们是否相等。

    我们可以通过测试一些例子来验证其健壮性。

    参见 ComplexClassUnitTest.java

5. 继承

    让我们考虑一下使用继承的Java类。

    ```java
    public abstract class Shape {
        public abstract double area();
        public abstract double perimeter();
    }
    ```

    如果我们在 Square 类（extends Shape）上尝试 "Source->Generate hashCode() and equals() "，Eclipse 警告我们 ‘the superclass ‘Rectangle' does not redeclare equals() and hashCode() : the resulting code may not function correctly'。

    同样地，当我们试图在矩形类上生成hashCode()和equals()时，我们得到了一个关于超类'Shape'的警告。

    尽管有警告，Eclipse还是会允许我们继续前进。在 Rectangle 的情况下，它扩展了一个抽象的 Shape 类，它不能实现 hashCode() 或 equals() ，因为它没有具体的成员变量。对于这种情况，我们可以不理会 Eclipse。

    然而，Square 类从 Rectangle 继承了宽度和长度成员变量，以及它自己的颜色变量。在Square中创建hashCode()和equals()，而不先对Rectangle做同样的事情，意味着在equals()/hashCode()中只使用颜色。

    一个简单的测试告诉我们，如果只是宽度不同，Square的equals()/hashCode()是不够的，因为宽度不包括在equals()/hashCode()的计算中。

    让我们通过使用Eclipse为矩形类生成equals()/hashCode()来解决这个问题。

    我们必须在Square类中重新生成equals()/hashCode()，所以Rectangle的equals()/hashCode()被调用。在这次生成的代码中，我们选择了Eclipse对话框中的所有选项，所以我们看到了注释、instanceOf比较和if块。

    重新运行上面的测试，我们现在通过了，因为Square的hashCode()/equals()被正确计算。

    > With Java 8’s "default method" feature, any abstract class without direct or inherited field should be converted into an interface. However, this change may not be appropriate in libraries or other applications where the class is intended to be used as an API. 将 Shape 改为接口.
