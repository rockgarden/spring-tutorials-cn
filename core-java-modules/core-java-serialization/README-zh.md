# 核心Java序列化
  
- [Java 中的可外部化接口指南](http://www.baeldung.com/java-externalizable)
- [x] [Java序列化简介](http://www.baeldung.com/java-serialization)
- [Java 中的反序列化漏洞](https://www.baeldung.com/java-deserialization-vulnerabilities)
- [Java 中的序列化验证](https://www.baeldung.com/java-validate-serializable)
- [什么是 serialVersionUID？](https://www.baeldung.com/java-serial-version-uid)
- [Java 序列化：readObject() 与 readResolve()](https://www.baeldung.com/java-serialization-readobject-vs-readresolve)

## Java序列化简介

1. 简介

    序列化是将对象的状态转换为字节流；反序列化则相反。换句话说，序列化是将 Java 对象转换为静态字节流（序列），然后将其保存到数据库或通过网络传输。

2. 序列化和反序列化

    序列化过程与实例无关；例如，我们可以在一个平台上序列化对象，然后在另一个平台上反序列化。符合序列化条件的类需要实现一个特殊的标记接口 Serializable。

    ObjectInputStream 和 ObjectOutputStream 都是高级类，分别扩展了 java.io.InputStream 和 java.io.OutputStream。ObjectOutputStream 可以将原始类型和对象图形以字节流的形式写入 OutputStream。然后，我们可以使用 ObjectInputStream 读取这些流。

    ObjectOutputStream 中最重要的方法是

    `public final void writeObject(Object o) throws IOException;`

    该方法接收一个可序列化的对象，并将其转换为字节序列（流）。同样，ObjectInputStream 中最重要的方法是

    `public final Object readObject() throws IOException, ClassNotFoundException;`

    该方法可以读取字节流，并将其转换回 Java 对象。然后再将其转换回原始对象。

    让我们用 Person 类来说明序列化。请注意，静态字段属于类（而不是对象），不会被序列化。另外，请注意我们可以使用关键字 transient 在序列化过程中忽略类字段：

    serialization\Person.java

    下面的测试举例说明了如何将 Person 类型的对象保存到本地文件，然后再将其读取回来：

    PersonUnitTest.java\whenSerializingAndDeserializing_ThenObjectIsTheSame()

    我们使用 ObjectOutputStream 将此对象的状态保存到 FileOutputStream 文件中。我们在项目目录中创建了文件 "yourfile.txt"。然后使用 FileInputStream 加载该文件。ObjectInputStream 提取该流并将其转换为名为 p2 的新对象。

    最后，我们将测试加载对象的状态，确保其与原始对象的状态一致。

    请注意，我们必须明确地将加载的对象转换为 Person 类型。

3. Java 序列化注意事项

    Java 中的序列化有一些注意事项。

    1. 继承和组合

        当一个类实现了 java.io.Serializable 接口时，它的所有子类也都是可序列化的。相反，当一个对象引用另一个对象时，这些对象必须分别实现 Serializable 接口，否则将抛出 NotSerializableException 异常：

        ```java
        public class Person implements Serializable {
        private Address country; // 也必须是可序列化的
        }
        ```

        如果可序列化对象中的一个字段由对象数组组成，那么所有这些对象也必须是可序列化的，否则将抛出 NotSerializableException 异常。

    2. 序列化版本 UID

        JVM 会为每个可序列化类关联一个版本（长）号。我们用它来验证保存的对象和加载的对象是否具有相同的属性，从而在序列化时是否兼容。

        大多数集成开发环境都能自动生成这个编号，它基于类名、属性和相关的访问修饰符。任何更改都会导致不同的编号，并可能导致无效类异常（InvalidClassException）。

        如果可序列化类没有声明 serialVersionUID，JVM 会在运行时自动生成一个。不过，强烈建议每个类都声明其 serialVersionUID，因为生成的 serialVersionUID 取决于编译器，因此可能会导致意想不到的 InvalidClassException。

    3. Java 中的自定义序列化

        Java 指定了一种序列化对象的默认方式，但 Java 类可以覆盖这种默认行为。在尝试序列化具有某些不可序列化属性的对象时，自定义序列化尤其有用。我们可以通过在要序列化的类中提供两个方法来实现这一点：

        `private void writeObject(ObjectOutputStream out) throws IOException;`

        和

        `private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException;`

        通过这些方法，我们可以将不可序列化的属性序列化为其他可以序列化的形式：

        serialization\Employee.java

        serialization\Address.java

        我们可以运行下面的单元测试来测试这种自定义序列化：

        PersonUnitTest.java\whenCustomSerializingAndDeserializing_ThenObjectIsTheSame()

        在这段代码中，我们可以看到如何通过自定义序列化 Address 来保存一些不可序列化的属性。请注意，我们必须将不可序列化属性标记为暂态属性，以避免 NotSerializableException。

4. 总结

    在这篇简短的文章中，我们回顾了 Java 序列化，讨论了注意事项，并学习了如何进行自定义序列化。

## Code

本文使用的源代码可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-serialization) 上获取。
