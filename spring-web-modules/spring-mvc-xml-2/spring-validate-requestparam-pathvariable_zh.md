# [在Spring中验证RequestParams和PathVariables](https://www.baeldung.com/spring-validate-requestparam-pathvariable)

1. 简介

    在本教程中，我们将学习如何在Spring MVC中验证HTTP请求参数和路径变量。

    具体来说，我们将用[JSR 303注解](https://beanvalidation.org/1.0/spec/)来验证字符串和数字参数。

    要探索其他类型的验证，我们可以参考我们关于[Java Bean验证](https://www.baeldung.com/javax-validation)和[方法约束](https://www.baeldung.com/javax-validation-method-constraints)的教程，或者我们可以学习[如何创建自己的验证器](https://www.baeldung.com/spring-mvc-custom-validator)。

2. 配置

    为了使用Java验证API，我们必须添加一个JSR 303实现，例如hibernate-validator。

    ```xml
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
    </dependency>
    ```

    我们还必须通过添加@Validated注解来启用控制器中的请求参数和路径变量的验证。

    ```java
    @RestController
    @RequestMapping("/")
    @Validated
    public class Controller {
        // ...
    }
    ```

    值得注意的是，启用参数验证还需要一个**MethodValidationPostProcessor Bean**。如果我们使用的是Spring Boot应用程序，那么这个Bean是自动配置的，因为我们的classpath上有hibernate-validator依赖。

    否则，在标准的Spring应用程序中，我们必须明确添加这个Bean。

    ```java
    @EnableWebMvc
    @Configuration
    @ComponentScan("com.baeldung.spring")
    public class ClientWebConfigJava implements WebMvcConfigurer {
        @Bean
        public MethodValidationPostProcessor methodValidationPostProcessor() {
            return new MethodValidationPostProcessor();
        }
        // ...
    }
    ```

    默认情况下，Spring中路径或请求验证过程中的任何错误都会导致HTTP 500响应。在本教程中，我们将使用[ControllerAdvice](https://www.baeldung.com/exception-handling-for-rest-with-spring)的一个自定义实现，以更可读的方式处理这类错误，对任何不良请求返回HTTP 400。我们可以在GitHub上找到这个解决方案的[源代码](https://github.com/eugenp/tutorials/tree/master/spring-web-modules/spring-mvc-xml)。

3. 验证一个RequestParam

    让我们考虑一个例子，我们将一个数字的工作日作为请求参数传入控制器方法。

    ```java
    @GetMapping("/name-for-day")
    public String getNameOfDayByNumber(@RequestParam Integer dayOfWeek) {
        // ...
    }
    ```

    我们的目标是确保dayOfWeek的值在1到7之间。为了做到这一点，我们将使用@Min和@Max注解。

    ```java
    @GetMapping("/name-for-day")
    public String getNameOfDayByNumber(@RequestParam @Min(1) @Max(7) Integer dayOfWeek) {
        // ...
    }
    ```

    任何不符合这些条件的请求都会返回HTTP状态400，并带有一个默认的错误信息。

    例如，如果我们调用<http://localhost:8080/name-for-day?dayOfWeek=24>，响应信息将是。

    `getNameOfDayByNumber.dayOfWeek: must be less than or equal to 7`

    我们可以通过添加一个自定义的信息来改变默认的信息。

    `@Max(value = 1, message = “day number has to be less than or equal to 7”)`

4. 验证一个PathVariable

    就像@RequestParam一样，我们可以使用javax.validation.constraints包中的任何注解来验证一个@PathVariable。

    让我们考虑一个例子，我们验证一个字符串参数不是空白的，并且长度小于或等于10。

    ```java
    @GetMapping("/valid-name/{name}")
    public void createUsername(@PathVariable("name") @NotBlank @Size(max = 10) String username) {
        // ...
    }
    ```

    例如，任何带有超过10个字符的名字参数的请求，都会导致HTTP 400错误，并有一个消息。

    `createUser.name:size must be between 0 and 10`

    通过设置@Size注解中的消息参数，可以很容易地覆盖默认的消息。
