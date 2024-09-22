# [在Spring中处理日期参数](https://www.baeldung.com/spring-date-parameters)

1. 介绍

    在这个简短的教程中，我们将学习如何在请求和应用程序级别在Spring REST请求中接受日期、本地日期和本地日期时间参数。

2. 问题

    让我们考虑一个具有三种方法的控制器，该方法接受日期、本地日期和本地日期时间参数：

    ```java
    @RestController
    public class DateTimeController {

        @PostMapping("/date")
        public void date(@RequestParam("date") Date date) {
            // ...
        }

        @PostMapping("/localdate")
        public void localDate(@RequestParam("localDate") LocalDate localDate) {
            // ...
        }

        @PostMapping("/localdatetime")
        public void dateTime(@RequestParam("localDateTime") LocalDateTime localDateTime) {
            // ...
        }
    }
    ```

    当向任何具有根据ISO 8601格式的参数的方法发送POST请求时，我们将得到一个异常。

    例如，当向/date端点发送“2018-10-22”时，我们会收到一个带有类似消息的不良请求错误：

    ```log
    Failed to convert value of type 'java.lang.String' to required type 'java.time.LocalDate'; 
    nested exception is org.springframework.core.convert.ConversionFailedException.
    ```

    这是因为默认情况下，Spring无法将字符串参数转换为任何日期或时间对象。

3. 在请求级别转换日期参数

    解决这个问题的方法之一是用@DateTimeFormat注释注释参数，并提供一个格式模式参数：

    ```java
    @RestController
    public class DateTimeController {

        @PostMapping("/date")
        public void date(@RequestParam("date") 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date date) {
            // ...
        }

        @PostMapping("/local-date")
        public void localDate(@RequestParam("localDate") 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate localDate) {
            // ...
        }

        @PostMapping("/local-date-time")
        public void dateTime(@RequestParam("localDateTime") 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime) {
            // ...
        }
    }
    ```

    这样，只要字符串使用ISO 8601格式格式化，字符串将被正确转换为日期对象。

    我们还可以通过在@DateTimeFormat注释中提供模式参数来使用我们自己的转换模式：

    ```java
    @PostMapping("/date")
    public void date(@RequestParam("date") 
    @DateTimeFormat(pattern = "dd.MM.yyyy") Date date) {
        // ...
    }
    ```

4. 在应用程序级别转换日期参数

    在Spring中处理日期和时间对象转换的另一种方法是提供全局配置。通过遵循官方文档，我们应该扩展WebMvcConfigurationSupport配置和itsmvcConversionService方法：

    ```java
    @Configuration
    public class DateTimeConfig extends WebMvcConfigurationSupport {

        @Bean
        @Override
        public FormattingConversionService mvcConversionService() {
            DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);

            DateTimeFormatterRegistrar dateTimeRegistrar = new DateTimeFormatterRegistrar();
            dateTimeRegistrar.setDateFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            dateTimeRegistrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            dateTimeRegistrar.registerFormatters(conversionService);

            DateFormatterRegistrar dateRegistrar = new DateFormatterRegistrar();
            dateRegistrar.setFormatter(new DateFormatter("dd.MM.yyyy"));
            dateRegistrar.registerFormatters(conversionService);

            return conversionService;
        }
    }
    ```

    首先，我们用一个假参数创建了DefaultFormattingConversionService，这意味着Spring默认不会注册任何格式化程序。

    然后，我们需要注册日期和日期时间参数的自定义格式。我们通过注册两个自定义格式注册商来做到这一点。第一个，DateTimeFormatterRegistar，将负责解析LocalDate和LocaDateTime对象。第二个，DateFormattingRegistrar，将处理Date对象。

5. 在属性文件中配置日期和时间

    Spring还为我们提供了通过应用程序属性文件设置全局日期和时间格式的选项。日期、日期-时间和时间格式有三个单独的参数：

    ```properties
    spring.mvc.format.date=yyyy-MM-dd
    spring.mvc.format.date-time=yyyy-MM-dd HH:mm:ss
    spring.mvc.format.time=HH:mm:ss
    ```

    所有这些参数都可以替换为iso值。例如，将日期-时间参数设置为：

    `spring.mvc.format.date-time=iso`

    将等于ISO-8601格式：

    `spring.mvc.format.date-time=yyyy-MM-dd HH:mm:ss`

6. 结论

    在本文中，我们学习了如何在Spring MVC请求中接受日期参数。我们讨论了如何根据要求和全球范围内做到这一点。

    我们还学会了如何创建我们自己的日期格式模式。
