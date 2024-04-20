# [比较Java中的长值](https://www.baeldung.com/java-compare-long-values)

1. 概述

    在这个简短的教程中，我们将讨论比较两个 Long 实例的不同方法。我们将强调使用引用比较操作符 (==) 时出现的问题。

    Test Code: CompareLongUnitTest.java

2. 使用引用比较的问题

    Long 是基元类型 long 的[包装类](/java-wrapper-classes-zh.md)。由于它们是对象而不是基元值，我们需要使用 .equals() 而不是引用比较操作符 (==) 来比较 Long 实例的内容。

    在某些情况下，我们可能会认为 == 是可行的，但事实并非如此。想想看，我们可以对低位数字使用 ==：

    givenLongValuesLessThan128_whenUsingReferenceComparater_thenSuccess()

    但对于较大的数字则不行。如果数值超出 -128 至 127 的范围，我们就会遇到问题，出现完全不同的意外结果：

    givenLongValuesGreaterOrEqualsThan128_whenUsingReferenceComparater_thenFails()

    这是因为 Java 为 -128 和 127 之间的 Long 实例维护了一个[恒定池](https://docs.oracle.com/javase/specs/jls/se7/html/jls-5.html#jls-5.1.7)。

    在一般情况下，具有相同基元值的两个[盒状实例](https://www.baeldung.com/java-primitives-vs-objects)不会产生相同的对象引用。

3. 使用 .equals()

    解决方案之一是使用 .equals()。这将评估两个对象的内容（而不是引用）：

    givenLongValuesGreaterOrEqualsThan128_whenUsingEquals_thenSuccess()

4. 对象.equals()

    使用 equals() 的问题在于，我们需要小心谨慎，不要在空引用上调用它。

    幸运的是，我们可以使用一个空安全实用方法--[Objects.equals()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Objects.html#equals(java.lang.Object,java.lang.Object))。

    让我们看看它在实际中是如何工作的：

    givenNullReference_whenUsingObjectsEquals_thenNoException()

    正如我们所看到的，如果我们要比较的 Longs 中有一个为空，我们就不需要费心了。

    在引擎盖下，Objects.equals() 首先使用 == 操作符进行比较，如果比较失败，则使用标准的 equals()。

5. 长值开箱

    1. 使用 .longValue() 方法

        接下来，让我们以安全的方式使用"=="比较运算符。Number 类有一个 .longValue() 方法，可以对原始长值进行解包：

        givenLongValuesGreaterOrEqualsThan128_whenUsingComparisonOperator_andLongValue_thenSuccess()

    2. 向原始值转换

        解压缩 Long 的另一种方法是将对象转换为基元类型。因此，我们先提取基元值，然后再使用比较操作符：

        givenLongValuesGreaterOrEqualsThan128_whenUsingCasting_thenSuccess()

        请注意，在使用 .longValue() 方法或使用转换时，我们应该检查对象是否为空。如果对象为空，我们可能会遇到 NullPointerException 异常。

6. 总结

    在这个简短的教程中，我们探讨了如何比较 Long 对象的不同选项。我们还分析了比较对象或内容引用时的不同之处。
