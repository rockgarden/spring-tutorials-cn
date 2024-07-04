# Core Java UUID

## Java中的UUID指南

1. 概述

    本文将介绍 UUID（通用唯一标识符）代码，有时也称为 GUID（全球唯一标识符）。简而言之，它是一个 128 位长的十六进制数字，以"-"分隔：

    `e58ed763-928c-4155-bee9-fdbaaadc15f3`

    标准 UUID 代码包含 32 个十六进制数字和 4 个"-"符号，因此其长度等于 36 个字符。还有一种 Nil UUID 代码，所有位都设置为 0。

    下面，我们来看看 Java 中的 UUID 类。首先，我们将了解如何使用类本身。然后，我们将了解不同类型的 UUID 以及如何在 Java 中生成它们。

    [在 Java 中验证 UUID 字符串](https://www.baeldung.com/java-validate-uuid-string)

    了解如何使用正则表达式或 UUID 类的静态方法验证 UUID 字符串。

    [在 Java 中生成字母数字 UUID 字符串](https://www.baeldung.com/java-generate-alphanumeric-uuid)

    Java 中各种 UUID 生成方法的实用比较。

    [在 Java 中转换字节数组和 UUID](https://www.baeldung.com/java-byte-array-to-uuid)

    字节数组与 UUID 之间的快速实用转换指南。

2. UUID 类

    UUID 类只有一个构造函数，需要两个长参数，分别描述最有意义和最无意义的 64 位：

    `UUID uuid = new UUID(mostSignificant64Bits, leastSignificant64Bits);`

    直接使用构造函数的缺点是，我们必须构造 UUID 的位模式，当我们想重新创建一个 UUID 对象时，这可能是一个不错的解决方案。但在大多数情况下，我们使用 UUID 来标识某些东西，因此可以随机赋值。因此，UUID 类提供了三种静态方法供我们使用。

    首先，我们可以使用 .nameUUIDFromBytes() 方法创建第 3 版 UUIF，该方法需要一个字节数组作为参数：

    `UUID uuid = UUID.nameUUIDFromBytes(bytes);`

    其次，我们可以从先前生成的代码中解析 UUID 字符串值：

    `UUID uuid = UUID.fromString(uuidHexDigitString);`

    同样，这种方法使用一些输入来创建 UUID 代码。不过，还有一种更方便的方法，无需输入任何参数即可创建 UUID。最后，使用 .randomUUID() 方法，我们可以创建一个版本 4 的 UUID：

    `UUID uuid = UUID.randomUUID();`

3. 结构

    具体来说，让我们考虑下面的 UUID 及其下面的相应掩码：

    ```txt
    123e4567-e89b-42d3-a456-556642440000
    xxxxxxxx-xxxx-Bxxx-Axxxxx-xxxxxxxxxxx
    ```

    1. UUID 变体

        在上例中，A 表示定义 UUID 布局的变体。UUID 中的所有其他位都取决于变体字段的布局。因此，变体表示 A 的三个最显著位：

        ```txt
        MSB1    MSB2    MSB3
        0       X       X     reserved (0)
        1       0       X     current variant (2)
        1       1       0     reserved for Microsoft (6)
        1       1       1     reserved for future (7)
        ```

        以上示例中 UUID 的 A 值为 "a"，二进制为 10xx。因此，布局变量为 2。

    2. UUID 版本

        同样，B 代表版本。在示例 UUID 中，B 的值是 4，表示使用的是版本 4。

        对于 Java 中的任何 UUID 对象，我们都可以使用 .variant() 和 .version() 方法检查其变体和版本：

        ```java
        UUID uuid = UUID.randomUUID();
        int variant = uuid.variant();
        int version = uuid.version();
        ```

        此外，变体 2 UUID 有五种不同的版本：

        - Time-Based (UUIDv1)
        - DCE Security (UUIDv2)
        - Name Based (UUIDv3 and UUIDv5)
        - Random (UUIDv4)

        不过，Java 只提供了 v3 和 v4 的实现，我们也可以使用构造函数生成其他类型。

4. UUID 版本

    代码参见：UUIDGenerator.java

    1. 版本 1

        UUID 版本 1 使用当前时间戳和生成 UUID 的设备的 MAC 地址。其中，时间戳的单位为 100 纳秒（从 1582 年 10 月 15 日算起）。不过，如果担心隐私问题，我们可以使用一个 48 位的随机数来代替 MAC 地址。

        考虑到这一点，让我们以长值的形式生成最不重要和最重要的 64 位：

        get64LeastSignificantBitsForVersion1()

        以上，我们组合了两个长值，分别代表随机长值的最后 63 位和 3 位变量标志。

        接下来，我们使用时间戳创建 64 个最有意义位：

        get64MostSignificantBitsForVersion1()

        然后，我们可以将这两个值传递给 UUID 的构造函数：

        generateType1UUID()

    2. 版本 2

        接下来，版本 2 还使用了时间戳和 MAC 地址。不过，[RFC 4122](https://tools.ietf.org/html/rfc4122) 并未规定具体的生成细节，因此我们不会在本文中介绍其实现方法。

    3. 版本 3 和版本 5

        第 3 版和第 5 版 UUID 使用从唯一名称空间中提取的散列名称。此外，名称的概念并不局限于文本形式。例如，域名系统（DNS）、对象标识符（OID）、URL 等都被视为有效的名称空间。

        UUID = hash(NAMESPACE_IDENTIFIER + NAME)

        具体来说，UUIDv3 和 UUIDv5 的区别在于哈希算法-v3 使用 MD5（128 位），而 v5 使用 SHA-1（160 位），截断为 128 位。对于这两个版本，我们都会相应地替换比特来校正版本和变体。

        或者，我们也可以使用 .nameUUIDFromBytes() 方法，根据先前的命名空间和给定的名称生成类型 3 UUID：

        ```java
        byte[] nameSpaceBytes = bytesFromUUID(namespace);
        byte[] nameBytes = name.getBytes("UTF-8");
        byte[] result = joinBytes(nameSpaceBytes, nameBytes);
        UUID uuid = UUID.nameUUIDFromBytes(result);
        ```

        在这里，我们将命名空间的十六进制字符串转换为字节数组，然后将其与名称相结合，创建 UUID。

        为了简单起见，我们将不描述第 5 版的[实现](https://github.com/eugenp/tutorials/blob/eb633a5b19658f8c2afc176c4dfc5510540ed10d/core-java-modules/core-java-uuid/src/main/java/com/baeldung/uuid/UUIDGenerator.java#L77)，因为它与第 5 版类似。不过，请记住 Java 并没有实现第 5 版。

    4. 版本 4

        最后，我们已经介绍了如何生成第 4 版 UUID。我们再次调用 UUID 类提供的 randomUUID() 方法来获取 UUIDv4。

5. 总结

    在本教程中，我们了解了 UUID 的结构和现有的各种版本。首先，我们了解了如何在 Java 中创建 UUID。然后，我们更详细地介绍了一些 UUID 版本。最后，我们提供了一些手动生成 UUID 代码的代码示例。

## Relevant Articles

- [Generating Alphanumeric UUID String in Java](https://www.baeldung.com/java-generate-alphanumeric-uuid)
- [x] [Guide to UUID in Java](http://www.baeldung.com/java-uuid)
- [Validate UUID String in Java](https://www.baeldung.com/java-validate-uuid-string)
- [Generate the Same UUID From a String in Java](https://www.baeldung.com/java-generate-same-uuid-from-string)
- [Generating Time Based UUIDs](https://www.baeldung.com/java-generating-time-based-uuids)

## Code

和往常一样，我们可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-uuid/) 上获取实现的源代码。
