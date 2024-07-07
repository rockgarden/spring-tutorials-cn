# Java Bean Validation Examples

本模块包含关于Bean验证的文章。

## Java Bean验证基础知识

1. 概述

    在这个快速教程中，我们将介绍用标准框架验证Java Bean的基础知识--用标准的JSR-380框架及其规范的Jakarta Bean Validation 3.0，它建立在Java EE 7中引入的Bean Validation API的功能之上。

    在大多数应用程序中，验证用户输入是一个超级常见的需求。而Java Bean Validation框架已经成为处理这类逻辑的事实标准。

    进一步阅读：

    [Spring Boot中的验证](https://www.baeldung.com/spring-boot-bean-validation)

    了解如何使用Hibernate Validator（Bean Validation框架的参考实现）来验证Spring Boot中的域对象。

    [使用Bean Validation 2.0的方法约束](https://www.baeldung.com/javax-validation-method-constraints)

    介绍使用Bean Validation 2.0的方法约束。

2. JSR 380

    JSR 380是用于Bean验证的Java API规范，是Jakarta EE和JavaSE的一部分。它使用@NotNull、@Min和@Max等注解，确保Bean的属性符合特定标准。

    这个版本需要Java 17或更高版本，因为使用了Spring Boot 3.x，它带来了Hibernate-Validator 8.0.0，它还支持Java 9及以上版本引入的新特性，如流和Optional的改进、模块、私有接口方法等。

    关于规范的全部信息，请继续阅读[JSR 380](https://jcp.org/en/jsr/detail?id=380)。

3. 依赖关系

    在最新版本的spring-boot-starter-validation中，除了其他依赖关系，hibernate-validator的横向依赖关系也将可用。

    如果你想只添加验证的依赖，你可以简单地在pom.xml中添加hibernate-validator。

    ```xml
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.0.Final</version>
    </dependency>
    ```

    一个简短的说明：hibernate-validator与Hibernate的持久性方面完全分开。所以，把它作为一个依赖项加入，我们就没有把这些持久性方面的内容加入到项目中。

4. 使用验证注解

    在这里，我们将使用一个User Bean，并为它添加一些简单的验证：

    javaxval.beanvalidation/User.java

    本例中使用的所有注解都是标准的JSR注解：

    - @NotNull验证了注释的属性值不是空的。
    - @AssertTrue验证了注释的属性值为真。
    - @Size验证了注释的属性值的大小在属性min和max之间；可以应用于String、Collection、Map和array属性。
    - @Min验证了被注释的属性的值不小于value属性。
    - @Max验证了被注释的属性的值不大于属性值。
    - @Email验证了注释的属性是一个有效的电子邮件地址。

    一些注释接受额外的属性，但消息属性对所有这些注释都是通用的。这是在各自属性的值验证失败时通常会呈现的消息。

    还有一些额外的注解，可以在JSR中找到：

    - @NotEmpty验证了该属性不是空的或空的；可以应用于String、Collection、Map或Array值。
    - @NotBlank 只能应用于文本值，并验证该属性不是空的或空白的。
    - @Positive和@PositiveOrZero适用于数值，验证它们是严格意义上的正数，或包括0的正数。
    - @Negative和@NegativeOrZero适用于数值，并验证它们是严格的负数，或包括0的负数。
    - @Past 和 @PastOrPresent 验证一个日期值是过去的或过去的，包括现在的；可以应用于日期类型，包括那些在Java 8中添加的。
    - @Future 和 @FutureOrPresent 验证一个日期值是在未来，或者在包括现在在内的未来。

    验证注解也可以应用于集合的元素：

    `List<@NotBlank String> preferences;`

    在这种情况下，任何添加到偏好列表中的值都将被验证。

    另外，该规范支持Java 8中新的Optional类型：

    ```java
    private LocalDate dateOfBirth;
    public Optional<@Past LocalDate> getDateOfBirth() {
        return Optional.of(dateOfBirth);
    }
    ```

    在这里，验证框架将自动解开LocalDate的值并验证它。

5. 编程式验证

    一些框架--比如Spring--有简单的方法来触发验证过程，只需使用注解。这主要是为了让我们不必与程序化验证的API进行交互。

    现在，让我们走手动路线，以编程方式设置东西：

    ```java
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();
    ```

    要验证一个bean，我们首先需要一个Validator对象，它是用ValidatorFactory构建的。

    1. 定义Bean

        我们现在要设置这个无效的用户--名字值为空：

        ```java
        User user = new User();
        user.setWorking(true);
        user.setAboutMe("Its all about me!");
        user.setAge(50);
        ```

    2. 验证Bean

        现在我们有了一个验证器，我们可以通过传递给验证方法来验证我们的Bean。

        任何违反用户对象中定义的约束的行为将作为一个Set返回：

        `Set<ConstraintViolation<User>> violations = validator.validate(user);`

        通过对违规行为进行迭代，我们可以使用getMessage方法获得所有的违规信息：

        ```java
        for (ConstraintViolation<User> violation : violations) {
            log.error(violation.getMessage()); 
        }
        ```

        在我们的例子中（ifNameIsNull_nameValidationFails），该集合将包含一个单独的ConstraintViolation，其消息是 "Name cannot be null"。

6. 结语

    这篇文章着重介绍了一个简单的通过标准Java验证API的过程。我们使用javax.validation注解和API展示了bean验证的基础知识。

    像往常一样，本文中概念的实现和所有代码片段都可以在GitHub上找到。

## Relevant Articles

- [x] [Java Bean Validation Basics](https://www.baeldung.com/javax-validation)
- [Validating Container Elements with Bean Validation 2.0](https://www.baeldung.com/bean-validation-container-elements)
- [Validations for Enum Types](https://www.baeldung.com/javax-validations-enums)
- [Javax BigDecimal Validation](https://www.baeldung.com/javax-bigdecimal-validation)
- [Grouping Javax Validation Constraints](https://www.baeldung.com/javax-validation-groups)
- [Constraint Composition with Bean Validation](https://www.baeldung.com/java-bean-validation-constraint-composition)
- [Using @NotNull on a Method Parameter](https://www.baeldung.com/java-notnull-method-parameter)
- More articles: [[next -->]](../javaxval-2)
