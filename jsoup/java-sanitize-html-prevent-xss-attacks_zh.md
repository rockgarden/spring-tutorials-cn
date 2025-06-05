# [对 HTML 代码进行清理以防止 XSS 攻击](https://www.baeldung.com/java-sanitize-html-prevent-xss-attacks)

安全

HTML    XSS

1. 简介

    跨站脚本攻击（XSS）是一种漏洞类型，攻击者可以利用它向 Web 应用注入恶意脚本。这些脚本可以在用户的浏览器中执行，导致数据泄露、会话劫持或网站篡改。

    在本教程中，我们将探讨如何在 Java 应用中对 HTML 输入进行清理，以防止 XSS 攻击。

2. 项目配置

    首先，我们需要将 OWASP 的 Java HTML 清理库添加到 `pom.xml` 文件中：

    ```xml
    <dependency>
        <groupId>com.googlecode.owasp-java-html-sanitizer</groupId>
        <artifactId>owasp-java-html-sanitizer</artifactId>
        <version>20240325.1</version>
    </dependency>
    ```

    该库提供了一个高度可配置的基于策略的清理器，可以在保护应用免受 XSS 攻击的同时处理复杂的 HTML 内容。

3. 实现基础的 OWASP HTML 清理

    添加完依赖后，我们可以定义一个工具方法来使用该库清理潜在的有害 HTML 输入。下面是一个可重用的工具类示例，它使用默认策略对 HTML 进行清理，仅允许基本格式化标签：

    ```java
    public class HtmlSanitizerUtil {
        private static final PolicyFactory POLICY = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        public static String sanitize(String htmlContent) {
            return POLICY.sanitize(htmlContent);
        }
    }
    ```

    在上面的例子中，我们通过组合两个内置的清理器 —— `Sanitizers.FORMATTING` 和 `Sanitizers.LINKS` 来配置清理策略。该策略允许基本的 HTML 格式化标签，例如 `<b>`、`<i>`、`<u>`，以及通过 `<a>` 标签实现的超链接。`sanitize()` 方法随后将该策略应用于输入字符串，并返回清理后的 HTML 内容。

    我们可以通过传入不安全的 HTML 并断言输出只包含允许的标签来验证清理器是否有效：

    ```java
    String input = "<script>alert('XSS')</script><b>Hello</b> <a href='https://example.com'>link</a>";  
    String expectedOutput = "<b>Hello</b> <a href=\"https://example.com\" rel=\"nofollow\">link</a>";

    String sanitized = HtmlSanitizerUtil.sanitize(input);
    assertEquals(expectedOutput, sanitized);
    ```

    在这个测试中，我们传入了一个包含恶意 `<script>` 标签以及有效的格式化和超链接元素的字符串。清理器移除了脚本标签，并保留了安全的标签。此外，`rel="nofollow"` 属性会被自动添加到链接中，作为额外的安全措施。

4. 使用 OWASP HtmlPolicyBuilder 实现灵活清理

    虽然内置策略提供了便利，但我们通常需要对允许的 HTML 元素和属性有更精细的控制。`HtmlPolicyBuilder` API 提供了一种流畅的方式来定义自定义策略。

    下面我们实现一个清理器，允许块级和内联格式化元素：

    ```java
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
    .allowCommonBlockElements()
    .allowCommonInlineFormattingElements()
    .toFactory();

    public static String sanitize(String html) {
        return POLICY.sanitize(html);
    }
    ```

    该实现创建了一个策略，允许常见的块级元素如 `<div>`、`<p>`、`<ul>`、`<ol>`，以及内联元素如 `<b>`、`<i>` 和 `<em>`。`sanitize()` 方法使用此策略删除任何危险的标签和属性，同时保留常见的布局和样式元素。`PolicyFactory` 实例是线程安全的，可以在多个清理操作中重复使用，而无需重新实例化。

    接下来，我们通过一个基于断言的测试来验证该实现是否符合预期：

    ```java
    String input = "<div onclick='alert(1)'><p><b>Text</b></p></div><script>alert('x')</script>";
    String expectedOutput = "<div><p><b>Text</b></p></div>";

    String sanitized = HtmlSanitizer.sanitize(input);
    assertEquals(expectedOutput, sanitized);
    ```

    在这个例子中，输入内容包含不安全的事件处理器和 `<script>` 标签。我们的自定义策略去除了危险的属性和元素，只保留了允许的结构和格式化标签。这种方法在安全性与用户格式保留之间取得了良好的平衡，适用于博客评论、内容管理系统或讨论板等场景。

5. 创建自定义策略

    在某些应用场景中，我们可能希望允许不同的 HTML 元素集合或更严格地限制某些属性。OWASP Java HTML 清理库提供了一个流畅的 API 来构建自定义策略。以下是一个更复杂的策略配置示例：

    ```java
    public class CustomHtmlSanitizer {
        private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
        .allowElements("a", "p", "div", "span", "h1", "h2", "h3")
        .allowUrlProtocols("https")
        .allowAttributes("href").onElements("a")
        .requireRelNofollowOnLinks()
        .allowAttributes("class").globally()
        .allowStyling()
        .toFactory();

        public static String sanitize(String html) {
            return POLICY.sanitize(html);
        }
    }
    ```

    在该示例中，我们构建了一个自定义的清理策略，规则如下：

    - 允许的元素：策略允许结构标签如 `<div>`、`<p>` 和标题标签（`<h1>` 到 `<h3>`），以及 `<a>` 和 `<span>`
    - 允许的 URL 协议：仅允许 HTTPS 链接，有助于防止不安全的 HTTP 链接，避免混合内容问题
    - 链接属性：`href` 属性被允许用于 `<a>` 标签，且每个链接会自动添加 `rel="nofollow"` 属性，以减少 SEO 滥用
    - 全局属性：`class` 属性可在所有元素上使用，支持 [CSS 样式](https://www.baeldung.com/spring-thymeleaf-css-js#2-adding-css)钩子
    - 内联样式：允许通过 `style` 属性使用安全的 CSS 样式，如颜色、字体粗细等非危害性声明

    这种方法让我们可以完全控制清理后内容的结构和外观，同时确保任何不安全的行为（如内联 JavaScript、事件处理器或不允许的协议）都被有效清除。

    我们可以通过一个测试用例来验证这一点：

    ```java
    String input = "<h1 class='title' style='color:red;'>Welcome</h1>"
    + "<a href='https://example.com' onclick='stealCookies()'>Click</a>"
    + "<script>alert('xss');</script>";

    String expectedOutput = 
    "<h1 class=\"title\" style=\"color:red\">Welcome</h1><a href=\"https://example.com\" rel=\"nofollow\">Click</a>";

    String sanitized = CustomHtmlSanitizer.sanitize(input);
    assertEquals(expectedOutput, sanitized);
    ```

    这种类型的自定义策略非常适合用于清理博客、论坛或 CMS 系统中的用户生成内容，在保证格式灵活性的同时不牺牲安全性。

6. 替代方案：使用 JSoup 进行 HTML 清理

    尽管 OWASP Java HTML 清理库具有高度安全性且基于策略，但另一个流行的 Java HTML 清理库是 **JSoup**。JSoup 提供了强大的 HTML 解析和清理功能，非常适合在需要解析或操作 DOM 的同时进行清理的场景。

    首先，我们需要在 `pom.xml` 中添加 JSoup 依赖：

    ```xml
    <dependency>
        <groupId>org.jsoup</groupId>
        <artifactId>jsoup</artifactId>
        <version>1.20.1</version>
    </dependency>
    ```

    添加依赖后，我们可以实现一个清理器，定义允许的 HTML 元素和属性白名单。下面是一个简单的实现示例：

    ```java
    public class JsoupHtmlSanitizer {
        public static String sanitize(String html) {
            Safelist safelist = Safelist.basic()
            .addTags("h1", "h2", "h3")
            .addAttributes("a", "target")
            .addProtocols("a", "href", "http", "https");
            
            return Jsoup.clean(html, safelist);
        }
    }
    ```

    在这个例子中，我们从 `Safelist.basic()` 开始，它允许基本的 HTML 标签如 `<b>`、`<i>`、`<u>` 和 `<a>`。然后我们扩展它，允许 `<h1>`、`<h2>` 和 `<h3>` 标题标签。

    最后，我们还允许锚点标签上的 `target` 属性，当使用 `target="_blank"` 时可以让链接在新标签页中打开，并限制链接协议为 `http` 和 `https`。

    为了验证这个实现，我们运行一个简单测试：

    ```java
    String input = "<h1 onclick='x()'>Title</h1><a href='javascript:alert(1)' target='_blank'>Click</a>";
    String expectedOutput = "<h1>Title</h1><a target=\"_blank\" rel=\"nofollow\">Click</a>";

    String sanitized = JsoupHtmlSanitizer.sanitize(input);
    assertEquals(expectedOutput, sanitized);
    ```

    与 OWASP 清理器不同，JSoup 使用的是“白名单”模型，在处理预定义 HTML 结构或在清理前需要提取或修改特定 HTML 节点时，这种方式更加直观。

    此外，JSoup 会自动为使用 `target="_blank"` 的 `<a>` 标签添加 `rel="nofollow"`，以防止反向标签劫持攻击，默认增强了安全性。

7. 总结

    在本文中，我们探讨了在 Java 应用中对 HTML 进行清理以防范 XSS 攻击的多种方法。

    当需要严格的 XSS 保护和基于策略的细粒度控制时，OWASP Java HTML 清理库是理想选择；而当涉及 HTML 解析、操作，或只需要一个更简单的基于白名单的方法时，JSoup 更加适用。
