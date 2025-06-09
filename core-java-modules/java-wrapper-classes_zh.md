# Java 中的封装类

1. 概述

    顾名思义，封装类是对 Java 基元类型进行封装的对象。

    每个 Java 基元类型都有一个相应的封装类：

    - boolean, byte, short, char, int, long, float, double
    - Boolean, Byte, Short, Character, Integer, Long, Float, Double
    这些都定义在 java.lang 包中，因此我们无需手动导入。

2. 封装类

    封装类有什么作用？这是最常见的 Java [面试问题](https://javarevisited.blogspot.com/2015/10/133-java-interview-questions-answers-from-last-5-years.html)之一。

    基本上，泛型类只与对象一起工作，不支持基元。因此，如果我们想使用它们，就必须将基元值转换为包装对象。

    例如，Java 集合框架只处理对象。早在很久以前（Java 5 之前，距今已有近 15 年的历史），那时还没有自动排序功能，例如，我们不能简单地在整数集合上调用 add(5)。

    当时，这些原始(primitive)值需要手动转换为相应的封装类并存储在集合中。

    如今，有了自动选框功能，我们可以轻松地执行 ArrayList.add(101)，但在内部，Java 会先将原始值转换为整数，然后再使用 valueOf() 方法将其存储到 ArrayList 中。

3. 基元类到封装类的转换

    现在最大的问题是：我们如何将基元值转换为相应的封装类，例如将 int 转换为 Integer 或将 char 转换为 Character？

    我们可以使用构造函数或静态工厂方法将基元值转换为封装类对象。

    不过，从 Java 9 开始，许多boxed基元（如 [Integer](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Integer.html#%3Cinit%3E(int)) 或 [Long](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Long.html#%3Cinit%3E(long))）的构造函数已被弃用。

    因此，强烈建议只在新代码中使用工厂方法。

    下面我们来看一个在 Java 中将 int 值转换为 Integer 对象的示例：

    `Integer object = new Integer(1);`

    `Integer anotherObject = Integer.valueOf(1);`

    valueOf() 方法返回一个代表指定 int 值的实例。

    该方法返回缓存值，因此效率很高。它总是缓存 -128 至 127 之间的值，但也可以缓存此范围之外的其他值。

    同样，我们也可以将布尔值转换为布尔值，将字节值转换为字节值，将 char 值转换为字符值，将 long 值转换为 Long 值，将 float 值转换为 Float 值，将 double 值转换为 Double 值。不过，如果我们要将 [String 转换为 Integer](https://javarevisited.blogspot.com/2011/08/convert-string-to-integer-to-string.html)，则需要使用 parseInt() 方法，因为 String 并不是一个封装类。

    另一方面，要将包装对象转换为基元值，我们可以使用相应的方法，如 intValue()、doubleValue() 等：

    `int val = object.intValue();`

    这里有全面的参考[资料](https://www.baeldung.com/java-primitive-conversions)。

4. 自动装箱和拆箱

    Java 5 之后，手动将原始值转换为对象可以通过自动装箱和开箱功能自动完成。

    "装箱" 是指将基元值转换为相应的封装对象。由于这种转换可以自动进行，因此称为自动装盒。

    同样，当一个封装对象被拆分成一个基元值时，这就是所谓的开箱。

    实际上，这意味着我们可以将一个基元值传递给一个期望使用包装对象的方法，或者将一个基元值赋值给一个期望使用对象的变量：

    ```java
    List<Integer> list = new ArrayList<>();
    list.add(1); // autoboxing
    Integer val = 2; // autoboxing
    ```

    在本例中，Java 会自动将原始 int 值转换为包装器。

    在内部，它使用 valueOf() 方法进行转换。例如，以下几行是等价的：

    `Integer value = 3;`

    `Integer value = Integer.valueOf(3);`

    虽然这样可以使转换更容易，代码更易读，但在某些情况下，我们不应该使用自动装箱，例如在循环内部。

    与自动开箱类似，在将对象传递给期望使用基元的方法或将其赋值给基元变量时，也会自动进行开箱：

    ```java
    Integer object = new Integer(1); 
    int val1 = getSquareValue(object); //unboxing
    int val2 = object; //unboxing

    public static int getSquareValue(int i) {
        return i*i;
    }
    ```

    基本上，如果我们编写了一个接受基元值或包装对象的方法，我们仍然可以将两种值都传递给它们。Java 会根据上下文传递正确的类型，如原始值或封装对象。

5. 总结

    在本快速教程中，我们介绍了 Java 中的封装类，以及自动装箱和拆箱机制。

## 相关文章

- [x] [Wrapper Classes in Java](https://www.baeldung.com/java-wrapper-classes)
