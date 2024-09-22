# [Spring可选路径变量](https://www.baeldung.com/spring-optional-path-variables)

1. 一览表

    在本教程中，我们将学习如何在Spring中使路径变量成为可选变量。首先，我们将描述Spring[如何](https://www.baeldung.com/spring-requestmapping)在处理程序方法中绑定@PathVariable参数。然后，我们将展示在不同的Spring版本中使路径变量成为可选的不同方法。

    有关路径变量的快速概述，请阅读我们的Spring MVC[文章](https://www.baeldung.com/spring-mvc-interview-questions#q5describe-apathvariable)。

2. Spring如何绑定@PathVariable参数

    默认情况下，Spring将尝试在处理程序方法中将所有用@PathVariable注释的参数与URI模板中的相应变量绑定。如果Spring失败，它不会将我们的请求传递给该处理程序方法。

    例如，考虑以下getArticle方法，该方法试图（失败）使id路径变量成为可选：

    ```java
    @RequestMapping(value = {"/article/{id}"})
    public Article getArticle(@PathVariable(name = "id") Integer articleId) {
        if (articleId != null) {
            //...
        } else {
            //...
        }
    }
    ```

    在这里，getArticle方法应该向/article和/article/{id}提供请求。如果存在，Spring将尝试将articleId参数绑定到id路径变量。

    例如，向/article/123发送请求将articleId的值设置为123。

    另一方面，如果我们向/article发送请求，由于以下异常，Spring返回状态代码500：

    ```log
    org.springframework.web.bind.MissingPathVariableException:
    Missing URI template variable 'id' for method parameter of type Integer
    ```

    这是因为Spring无法为articleId参数设置值，因为id缺失。

    因此，我们需要一些方法来告诉Spring，如果没有相应的路径变量，则忽略绑定特定的@PathVariable参数，我们将在以下章节中看到。

3. 使路径变量成为可选的

    1. 使用@PathVariable的所需属性

        自Spring 4.3.3以来，@PathVariable注释定义了我们指示路径变量是否对处理程序方法是强制性的所需的布尔属性。

        例如，以下版本的getArticle使用所需的属性：

        ```java
        @RequestMapping(value = {"/article", "/article/{id}"})
        public Article getArticle(@PathVariable(required = false) Integer articleId) {
        if (articleId != null) {
            //...
        } else {
            //...
        }
        }
        ```

        由于必需的属性为false，如果id路径变量没有在请求中发送，Spring将不会投诉。也就是说，如果发送了，Spring会将articleId设置为id，否则将articleId设置为null。

        另一方面，如果要求为真，Spring会抛出一个异常，以防id缺失。

    2. 使用可选参数类型

        以下实现展示了Spring 4.1以及JDK 8的[可选类](https://www.baeldung.com/java-optional)如何提供另一种使articleId为可选的方法：

        ```java
        @RequestMapping(value = {"/article", "/article/{id}"})
        public Article getArticle(@PathVariable Optional<Integer> optionalArticleId) {
            if (optionalArticleId.isPresent()) {
                Integer articleId = optionalArticleId.get();
                //...
            } else {
                //...
            }
        }
        ```

        在这里，Spring创建了`Optional<Integer>`实例，optionalArticleId，以保持id的值。如果存在id，optionalArticleId将包装其值，否则，optionalArticleId将包装空值。然后，我们可以使用可选的 isPresent()、get()或orElse()方法来处理该值。

    3. 使用地图参数类型

        定义自Spring 3.2以来可用的可选路径变量的另一种方法是使用@PathVariable参数的映射：

        ```java
        @RequestMapping(value = {"/article", "/article/{id}"})
        public Article getArticle(@PathVariable Map<String, String> pathVarsMap) {
            String articleId = pathVarsMap.get("id");
            if (articleId != null) {
                Integer articleIdAsInt = Integer.valueOf(articleId);
                //...
            } else {
                //...
            }
        }
        ```

        在本例中，`Map<String, String>` pathVarsMap参数将URI中的所有路径变量作为键/值对收集。然后，我们可以使用get()方法获取一个特定的路径变量。

        请注意，由于Spring将路径变量的值提取为字符串，我们使用Integer.valueOf()方法将其转换为Integer。

    4. 使用两种处理方法

        如果我们使用的是传统的Spring版本，我们可以将getArticle处理程序方法拆分为两种方法。

        第一个方法将处理对/article/{id}的请求：

        ```java
        @RequestMapping(value = "/article/{id}")
        public Article getArticle(@PathVariable(name = "id") Integer articleId) {
            //...
        }
        ```

        虽然第二种方法将处理对/文章的请求：

        ```java
        @RequestMapping(value = "/article")
        public Article getDefaultArticle() {
            //...
        }
        ```

4. 结论

    总之，我们已经讨论了如何在不同的Spring版本中使路径变量成为可选的。
