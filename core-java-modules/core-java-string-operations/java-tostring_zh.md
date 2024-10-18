# [Java toString()方法](https://www.baeldung.com/java-tostring)

1. 概述

    Java中的每个类都是Object类的子类，无论是直接还是间接。由于Object类包含一个toString()方法，我们可以在任何实例上调用toString()并获得其字符串表示。
    在本教程中，我们将看看toString()的默认行为，并学习如何改变其行为。

2. 默认行为

    每当我们打印一个对象引用时，它都会在内部调用toString()方法。所以，如果我们没有在类中定义toString()方法，那么就会调用Object#toString()。

    ```java
    public String toString() {
        return getClass().getName()+"@"+Integer.toHexString(hashCode());
    }
    ```

    为了看看这个方法是如何工作的，让我们创建一个客户对象，我们将在整个教程中使用：

    tostring/Customer.java

    现在，如果我们试图打印我们的Customer对象，Object#toString()将被调用，输出结果将类似于：
    `com.baeldung.tostring.Customer@6d06d69c`

3. 重写默认行为

    看一下上面的输出，我们可以看到它并没有给我们关于Customer对象的内容的很多信息。一般来说，我们对知道一个对象的哈希码不感兴趣，而是对我们对象的属性内容感兴趣。
    通过覆盖toString()方法的默认行为，我们可以使方法调用的输出更加有意义。
    现在，让我们看一下使用对象的几个不同场景，看看我们如何覆盖这个默认行为。

4. 原始类型和字符串

    我们的客户对象有字符串和原始属性。我们需要覆盖toString()方法来实现更有意义的输出：
    tostring/CustomerPrimitiveToString.java

    让我们看看现在调用toString()时得到什么：

    CustomerPrimitiveToStringUnitTest.java\givenPrimitive_whenToString_thenCustomerDetails()

5. 复杂的Java对象

    现在让我们考虑一个场景，我们的Customer对象也包含一个订单属性，该属性是Order类型的。我们的订单类同时拥有字符串和原始数据类型字段。
    所以，让我们再次覆盖toString()：
    tostring/CustomerComplexObjectToString.java

    由于订单是一个复杂的对象，如果我们只是打印我们的客户对象，而没有覆盖订单类中的toString()方法，它将把订单打印成`Order@<hashcode>`。
    为了解决这个问题，我们也在Order中覆盖toString()：
    tostring/Order.java

    现在，让我们看看当我们对包含订单属性的客户对象调用toString()方法时会发生什么：

    CustomerComplexObjectToStringUnitTest.java\givenComplex_whenToString_thenCustomerDetails()

6. 对象的数组

    接下来，让我们把我们的客户改为有一个订单数组。如果我们只是打印我们的客户对象，而不对我们的订单对象进行特殊处理，它将把订单打印成`Order;@<hashcode>`。
    为了解决这个问题，我们对订单字段使用[Arrays.toString()](https://www.baeldung.com/java-array-to-string)：
    tostring/CustomerArrayToString.java

    让我们看看调用上述toString()方法的结果：

    CustomerArrayToStringUnitTest.java\givenArray_whenToString_thenCustomerDetails()

7. 封装器、集合和StringBuffers

    当一个对象完全由[包装器](https://www.baeldung.com/java-wrapper-classes)、集合或StringBuffers组成时，不需要自定义toString()实现，因为这些对象已经用有意义的表示方法重写了toString()方法：

    tostring/CustomerWrapperCollectionToString.java

    让我们再次看看调用toString()的结果：

    CustomerWrapperCollectionToStringUnitTest.java\givenWrapperCollectionStrBuffer_whenToString_thenCustomerDetails()

8. 总结

    在这篇文章中，我们研究了创建我们自己的toString()方法的实现。
