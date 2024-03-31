# [Java属性入门](https://www.baeldung.com/java-properties)

[核心Java](https://www.baeldung.com/category/core-java)

1. 概述

    大多数 Java 应用程序都需要在某些时候使用属性，通常是为了在编译代码之外将简单的参数存储为键值对。

    因此，Java 语言拥有对属性的一流支持--java.util.Properties--一个专为处理此类配置文件而设计的实用程序类。

    这就是我们本文将重点讨论的内容。

2. 加载属性

    1. 从属性文件

        让我们从一个从属性文件加载键值对的示例开始；我们要加载两个在类路径上可用的文件：

        app.properties：

        ```properties
        version=1.0
        name=TestApp
        date=2016-11-12
        ```

        还有catalog：

        c1=files
        c2=images
        c3=videos

        请注意，虽然建议属性文件使用后缀".properties"，但这并非必要。

        现在我们可以非常简单地将它们加载到属性实例中：

        test/.java.properties/PropertiesUnitTest.java:givenPropertyValue_whenPropertiesFileLoaded_thenCorrect()

        只要文件内容符合属性文件格式要求，属性类就能正确解析文件。下面是[属性文件格式](https://en.wikipedia.org/wiki/.properties)的详细信息。

    2. 从 XML 文件加载

        除了属性文件，Properties 类还可以加载符合特定 DTD 规范的 XML 文件。

        下面是一个从 XML 文件 icons.xml 加载键值对的示例：

        test/resources/icons.xml

        现在，让我们加载它：

        test/.java.properties/PropertiesUnitTest.java:givenPropertyValue_whenXMLPropertiesFileLoaded_thenCorrect()

3. 获取属性

    我们可以使用 getProperty(String key) 和 getProperty(String key, String defaultValue) 通过键值获取值。

    如果存在键值对，这两个方法都会返回相应的值。但如果没有这样的键值对，前者将返回 null，后者将返回 defaultValue。

    示例代码：

    test/.java.properties/PropertiesUnitTest.java:givenAbsentProperty_whenPropertiesFileLoaded_thenReturnsDefault()

    请注意，虽然 Properties 类继承了 Hashtable 类的 get() 方法，但我不建议使用它来获取值。因为它的 get() 方法将返回一个只能被转换为字符串的对象值，而 getProperty() 方法已经为你正确处理了原始对象值。

    下面的代码将抛出异常：

    `float appVerFloat = (float) appProps.get("version");`

4. 设置属性

    我们可以使用 setProperty() 方法更新已存在的键值对或添加新的键值对。

    示例代码：

    test/.java.properties/PropertiesUnitTest.java:givenPropertyValue_whenPropertiesSet_thenCorrect()

    请注意，虽然 Properties 类从 Hashtable 类继承了 put() 方法和 putAll() 方法，但我不建议您使用它们，原因与 get() 方法相同：Properties 中只能使用字符串值。

    下面的代码不会如你所愿，当你使用 getProperty() 获取其值时，它将返回空值：

    `appProps.put("version", 2);`

5. 删除属性

    如果要删除键值对，可以使用 remove() 方法。

    示例代码：

    test/.java.properties/PropertiesUnitTest.java:givenPropertyValueNull_whenPropertiesRemoved_thenCorrect()

6. 存储

    1. 存储到属性文件

        Properties 类提供了一个store()方法，用于输出键值对。

        示例代码：

        ```java
        String newAppConfigPropertiesFile = rootPath + "newApp.properties";
        appProps.store(new FileWriter(newAppConfigPropertiesFile), "store to properties file");
        ```

        第二个参数用于注释。如果不想写任何注释，只需使用空值即可。

    2. 存储到 XML 文件

        属性类还提供了一个 storeToXML() 方法，用于以 XML 格式输出键值对。

        示例代码：

        ```java
        String newAppConfigXmlFile = rootPath + "newApp.xml";
        appProps.storeToXML(new FileOutputStream(newAppConfigXmlFile), "store to xml file");
        ```

        第二个参数与 store() 方法中的参数相同。

7. 其他常见操作

    Properties 类还提供了一些其他方法来操作属性。

    示例代码：

    test/.java.properties/PropertiesUnitTest.java:givenPropertiesSize_whenPropertyFileLoaded_thenCorrect()

8. 默认属性列表

    一个 Properties 对象可以包含另一个 Properties 对象作为其默认属性列表。如果在原始属性列表中找不到属性键，则会搜索默认属性列表。

    除了 "app.properties"，我们在类路径上还有另一个文件--"default.properties"：

    default.properties

    示例代码

    test/.java.properties/PropertiesUnitTest.java:givenPropertyValueAbsent_LoadsValuesFromDefaultProperties()

9. 属性和编码

    默认情况下，属性文件应采用 ISO-8859-1 （Latin-1）编码，因此一般不使用包含 ISO-8859-1 以外字符的属性。

    必要时，我们可以借助 JDK native2ascii 工具等工具或文件上的显式编码来绕过这一限制。

    对于 XML 文件，loadFromXML() 方法和 storeToXML() 方法默认使用 UTF-8 字符编码。

    不过，在读取编码不同的 XML 文件时，我们可以在 DOCTYPE 声明中指定；写入时也有足够的灵活性--我们可以在 storeToXML() API 的第三个参数中指定编码。

10. 结论

    在本文中，我们讨论了 Properties 类的基本用法，包括如何使用 Properties 加载和存储属性和 XML 格式的键值对，如何操作 Properties 对象中的键值对，如检索值、更新值、获取大小，以及如何使用 Properties 对象的默认列表。
