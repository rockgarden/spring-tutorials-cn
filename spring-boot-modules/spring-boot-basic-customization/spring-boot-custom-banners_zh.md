# [在Spring Boot中使用自定义横幅](https://www.baeldung.com/spring-boot-custom-banners)

1. 一览表

    默认情况下，Spring Boot附带一个横幅，该横幅在应用程序启动后立即显示。

    在本文中，我们将学习如何创建自定义横幅并在Spring Boot应用程序中使用它。

2. 创建一个横幅

    在我们开始之前，我们需要创建自定义横幅，该横幅将在应用程序启动时显示。我们可以从头开始创建自定义横幅，或使用各种工具来为我们做到这一点。

    在这个例子中，我们使用了Baeldung的官方标志。

    然而，在某些情况下，我们可能希望使用纯文本格式的横幅，因为它相对更容易维护。

    我们在此示例中使用的纯文本自定义横幅可在此处找到。

    这里需要注意的是，ANSI字符集能够在控制台中显示彩色文本。这无法用简单的纯文本格式完成。

3. 使用自定义横幅

    由于我们已经准备好了自定义横幅，我们需要在src/main/resources目录中创建一个名为banner.txt的文件，并将横幅内容粘贴到其中。

    这里需要注意的是，banner.txt是Spring Boot使用的默认预期横幅文件名。然而，如果我们想为横幅选择任何其他位置或其他名称，我们需要在application.properties文件中设置spring.banner.location属性：

    `spring.banner.location=classpath:/path/to/banner/bannername.txt`

    我们也可以使用图像作为横幅。与banner.txt一样，Spring Boot期望横幅图像的名称为asbanner.gif。此外，我们可以在应用程序中设置不同的图像属性，如高度、宽度等。属性：

    ```properties
    spring.banner.image.location=classpath:banner.gif
    spring.banner.image.width=  //TODO
    spring.banner.image.height= //TODO
    spring.banner.image.margin= //TODO
    spring.banner.image.invert= //TODO
    ```

    然而，使用文本格式总是更好的，因为如果使用一些复杂的图像结构，应用程序启动时间将大大增加。

4. 结论

    在这篇短文中，我们展示了如何在Spring Boot应用程序中使用自定义横幅。
