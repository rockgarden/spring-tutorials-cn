# [在Spring中使用枚举作为请求参数](https://www.baeldung.com/spring-enum-request-param)

1. 介绍

    在大多数典型的Web应用程序中，我们通常需要将请求参数限制为一组预定义的值。枚举是做到这一点的好方法。

    在本快速教程中，我们将演示如何在Spring MVC中使用枚举作为Web请求参数。

2. 使用枚举作为请求参数

    让我们先为我们的示例定义一个枚举：

    ```java
    public enum Modes {
        ALPHA, BETA;
    }
    ```

    然后，我们可以将此枚举用作Spring控制器中的RequestParameter：

    ```java
    @GetMapping("/mode2str")
    public String getStringToMode(@RequestParam("mode") Modes mode) {
        // ...
    }
    ```

    或者我们可以把它用作PathVariable：

    ```java
    @GetMapping("/findbymode/{mode}")
    public String findByEnum(@PathVariable("mode") Modes mode) {
        // ...
    }
    ```

    当我们提出网络请求时，例如/mode2str？mode=ALPHA，请求参数是字符串对象。Spring可以尝试使用其[StringToEnumConverterFactory](https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/convert/support/StringToEnumConverterFactory.java)类将此String对象转换为Enum对象。

    后端转换使用[Enum.valueOf](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Enum.html#valueOf(java.lang.Class,java.lang.String))方法。因此，输入名称字符串必须与声明的枚举值之一完全匹配。

    当我们提出与枚举值之一不匹配的字符串值的网络请求时，如/mode2str？模式=未知，Spring将无法将其转换为指定的枚举类型。在这种情况下，我们将获得一个转换失败的例外。

3. 自定义转换器

    在Java中，用大写字母定义枚举值被认为是良好做法，因为它们是常量。然而，我们可能希望在请求URL中支持小写字母。

    在这种情况下，我们需要创建一个自定义转换器：

    ```java
    public class StringToEnumConverter implements Converter<String, Modes> {
        @Override
        public Modes convert(String source) {
            return Modes.valueOf(source.toUpperCase());
        }
    }
    ```

    要使用我们的自定义转换器，我们需要在Spring配置中注册它：

    ```java
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverter(new StringToEnumConverter());
        }
    }
    ```

4. 异常处理

    如果我们的模式枚举没有匹配的常量，StringToEnumConverter中的Enum.valueOf方法将抛出IllegalArgumentException。根据我们的要求，我们可以以不同的方式在自定义转换器中处理此异常。

    例如，我们可以简单地让转换器为不匹配的字符串返回空值：

    ```java
    public class StringToEnumConverter implements Converter<String, Modes> {
        @Override
        public Modes convert(String source) {
            try {
                return Modes.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    ```

    然而，如果我们没有在自定义转换器中本地处理异常，Spring将向调用控制器方法抛出ConversionFailedException异常。有[几种方法](https://www.baeldung.com/exception-handling-for-rest-with-spring)可以处理这个例外。

    例如，我们可以使用全局异常处理程序类：

    ```java
    @ControllerAdvice
    public class GlobalControllerExceptionHandler {
        @ExceptionHandler(ConversionFailedException.class)
        public ResponseEntity<String> handleConflict(RuntimeException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    ```

5. 结论

    在这篇简短的文章中，我们通过一些代码示例学习了如何在Spring中使用枚举作为请求参数。

    我们还提供了一个自定义转换器示例，该转换器可以将输入字符串映射到枚举常数。

    最后，我们讨论了如何处理Spring遇到未知输入字符串时抛出的异常。
