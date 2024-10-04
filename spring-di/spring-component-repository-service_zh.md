# [@Component vs @Repository和@Service in Spring](https://www.baeldung.com/spring-component-repository-service)

1. 介绍

    在这个快速教程中，我们将了解Spring Framework中@Component、@Repository和@Service注释之间的区别。

2. Spring注释

    在大多数典型的应用程序中，我们有不同的层，如数据访问、演示、服务、业务等。

    此外，在每一层中，我们都有各种各样的豆子。为了自动检测这些豆子，Spring使用classpath扫描注释。

    然后，它在ApplicationContext中注册每个bean。

    以下是其中几个注释的简要概述：

    - @Component是任何Spring管理组件的通用刻板印象。
    - @Service在服务层注释类。
    - @Repository在持久性层上注释类，该层将充当数据库存储库。

    我们将在这里专注于它们之间的差异。

3. 有什么不同？

    这些刻板印象之间的主要区别在于它们用于不同的分类。当我们为自动檢測注释类时，我们应该使用相应的刻板印象。

    现在让我们更详细地浏览一下它们。

    1. @Component

        我们可以在整个应用程序中使用@Component将bean标记为Spring的托管组件。Spring只会使用@Component来拾取和注册beans，一般不会查找@Service和@Repository。

        它们在ApplicationContext中注册，因为它们用@Component注释：

        ```java
        @Component
        public @interface Service {}

        @Component
        public @interface Repository {}
        ```

        @Service和@Repository是@Component的特殊情况。它们在技术上是相同的，但我们将它们用于不同的目的。

    2. @Repository

        @Repository的工作是捕捉特定于持久性的异常，并将其作为Spring的统一未检查异常之一重新抛出。

        为此，Spring提供了PersistenceExceptionTranslationPostProcessor，我们需要将其添加到应用程序上下文中（如果我们使用Spring Boot，则已包含）：

        `<bean class="org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor"/>`

        这个bean post processor为任何带有@Repository注释的bean添加一个顾问。

    3. @Service

        我们用@Service标记beans，以表明它们持有业务逻辑。除了在服务层中使用外，此注释没有其他特殊用途。

4. 结论

    在这篇文章中，我们了解了@Component、@Repository和@Serviceannotations之间的区别。我们分别检查了每个注释，以了解它们的使用领域。

    总之，根据他们的图层惯例选择注释总是一个好主意。
