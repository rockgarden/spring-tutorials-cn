# [Spring Data REST 验证器指南](https://www.baeldung.com/spring-data-rest-validators)

1. 一览表

    本文介绍了Spring Data REST验证器的基本介绍。

    简单地说，使用Spring Data REST，我们只需通过REST API向数据库添加一个新条目，但我们当然还需要确保数据在实际持久化之前有效。

    本文继续一篇现有[文章](https://www.baeldung.com/spring-data-rest-intro)，我们将重复使用我们在那里建立的现有项目。

    而且，如果您想首先开始使用Spring Data REST——这里有一个很好的方法：

2. 使用验证器

    从Spring 3开始，该框架具有验证器接口——可用于验证对象。

    1. 积极性

        在上一篇文章中，我们定义了具有两个属性的实体——名称和电子邮件。

        因此，要创建新资源，我们只需要运行：

        ```bash
        curl -i -X POST -H "Content-Type:application/json" -d 
        '{ "name" : "Test", "email" : "test@test.com" }' 
        http://localhost:8080/users
        ```

        此POST请求将把提供的JSON对象保存到我们的数据库中，操作将返回：

        ```json
        {
        "name" : "Test",
        "email" : "test@test.com",
        "_links" : {
            "self" : {
                "href" : "http://localhost:8080/users/1"
            },
            "websiteUser" : {
                "href" : "http://localhost:8080/users/1"
            }
        }
        }
        ```

        由于我们提供了有效的数据，所以预计会有积极的结果。但是，如果我们删除属性名，或者只是将值设置为空字符串，会发生什么？

        为了测试第一个场景，我们将从之前运行修改后的命令，我们将空字符串设置为属性名称的值：

        ```bash
        curl -i -X POST -H "Content-Type:application/json" -d 
        '{ "name" : "", "email" : "Baggins" }' http://localhost:8080/users
        ```

        使用该命令，我们将得到以下响应：

        ```json
        {
        "name" : "",
        "email" : "Baggins",
        "_links" : {
            "self" : {
                "href" : "http://localhost:8080/users/1"
            },
            "websiteUser" : {
                "href" : "http://localhost:8080/users/1"
            }
        }
        }
        ```

        对于第二种情况，我们将从请求中删除属性名称：

        ```bash
        curl -i -X POST -H "Content-Type:application/json" -d 
        '{ "email" : "Baggins" }' http://localhost:8080/users
        ```

        对于该命令，我们将得到以下响应：

        ```json
        {
        "name" : null,
        "email" : "Baggins",
        "_links" : {
            "self" : {
                "href" : "http://localhost:8080/users/2"
            },
            "websiteUser" : {
                "href" : "http://localhost:8080/users/2"
            }
        }
        }
        ```

        正如我们所看到的，这两个请求都没问题，我们可以通过201状态代码和API链接来确认。

        这种行为是不可接受的，因为我们想避免将部分数据插入数据库。

    2. 春季数据REST事件

        在每次调用Spring Data REST API时，Spring Data REST导出器都会生成此处列出的各种事件：

        - BeforeCreateEvent
        - AfterCreateEvent
        - BeforeSaveEvent
        - AfterSaveEvent
        - BeforeLinkSaveEvent
        - AfterLinkSaveEvent
        - BeforeDeleteEvent
        - AfterDeleteEvent

        由于所有事件都以类似的方式处理，我们只会显示如何处理beforeCreateEvent，这是在新对象保存到数据库之前生成的。

    3. 定义验证器

        要创建我们自己的验证器，我们需要使用支持和验证方法实现org.springframework.validation.Validator接口。

        支持检查验证器是否支持提供的请求，而验证方法则验证请求中提供的数据。

        让我们定义一个WebsiteUserValidator类：

        ```java
        public class WebsiteUserValidator implements Validator {

            @Override
            public boolean supports(Class<?> clazz) {
                return WebsiteUser.class.equals(clazz);
            }

            @Override
            public void validate(Object obj, Errors errors) {
                WebsiteUser user = (WebsiteUser) obj;
                if (checkInputString(user.getName())) {
                    errors.rejectValue("name", "name.empty");
                }
        
                if (checkInputString(user.getEmail())) {
                    errors.rejectValue("email", "email.empty");
                }
            }

            private boolean checkInputString(String input) {
                return (input == null || input.trim().length() == 0);
            }
        }
        ```

        错误对象是一个特殊类，旨在包含验证方法中提供的所有错误。在本文的后面，我们将展示如何使用错误对象中包含的提供消息。
        要添加新的错误消息，我们必须调用errors.rejectValue（nameOfField，errorMessage）。

        在我们定义了验证器后，我们需要将其映射到请求被接受后生成的特定事件。

        例如，在我们的案例中，beforeCreateEvent是因为我们想要将一个新对象插入到我们的数据库中。但由于我们想在请求中验证对象，我们需要先定义我们的验证器。

        这可以通过三种方式完成：

        - 添加名为“beforeCreateWebsiteUserValidator”的组件注释。Spring Boot将识别prefixbeforeCreate，该prefix决定了我们要捕获的事件，它还将从组件名称中识别WebsiteUser类。

            ```java
            @Component("beforeCreateWebsiteUserValidator")
            public class WebsiteUserValidator implements Validator {
                ...
            }
            ```

        - 使用@Bean注释在应用程序上下文中创建Bean：

            ```java
            @Bean
            public WebsiteUserValidator beforeCreateWebsiteUserValidator() {
                return new WebsiteUserValidator();
            }
            ```

        - 手动注册：

            ```java
            @SpringBootApplication
            public class SpringDataRestApplication implements RepositoryRestConfigurer {
                public static void main(String[] args) {
                    SpringApplication.run(SpringDataRestApplication.class, args);
                }

                @Override
                public void configureValidatingRepositoryEventListener(
                ValidatingRepositoryEventListener v) {
                    v.addValidator("beforeCreate", new WebsiteUserValidator());
                }
            }
            ```

        - 在这种情况下，您不需要在WebsiteUserValidator类上进行任何注释。

    4. 事件发现错误

        目前，[Spring Data REST中存在一个错误](https://jira.spring.io/browse/DATAREST-524)——这会影响事件的发现。

        如果我们调用生成beforeCreate事件的POST请求，我们的应用程序将不会调用验证器，因为由于这个错误，事件将不会被发现。

        这个问题的简单解决方法是将所有事件插入到Spring Data REST ValidatingRepositoryEventListener类中：

        ```java
        @Configuration
        public class ValidatorEventRegister implements InitializingBean {

            @Autowired
            ValidatingRepositoryEventListener validatingRepositoryEventListener;

            @Autowired
            private Map<String, Validator> validators;

            @Override
            public void afterPropertiesSet() throws Exception {
                List<String> events = Arrays.asList("beforeCreate");
                for (Map.Entry<String, Validator> entry : validators.entrySet()) {
                    events.stream()
                    .filter(p -> entry.getKey().startsWith(p))
                    .findFirst()
                    .ifPresent(
                        p -> validatingRepositoryEventListener
                    .addValidator(p, entry.getValue()));
                }
            }
        }
        ```

3. 测试

    在第2.1节中，我们表明，如果没有验证器，我们可以将没有名称属性的对象添加到我们的数据库中，这不是预期的行为，因为我们不检查数据完整性。

    如果我们想在没有名称属性的情况下添加相同的对象，但提供了验证器，我们将收到以下错误：

    ```java
    curl -i -X POST -H "Content-Type:application/json" -d 
        '{ "email" : "test@test.com" }' http://localhost:8080/users
    ```

    ```json
    {  
        "timestamp":1472510818701,
        "status":406,
        "error":"Not Acceptable",
        "exception":"org.springframework.data.rest.core.
            RepositoryConstraintViolationException",
        "message":"Validation failed",
        "path":"/users"
    }
    ```

    正如我们所看到的，检测到请求中丢失的数据，并且没有将对象保存到数据库中。我们的请求被退回，并附上500个HTTP代码和内部错误的消息。

    错误消息没有说明我们请求中的问题。如果我们想让它更具信息性，我们必须修改响应对象。

    在“Spring异常处理”一[文中](https://www.baeldung.com/exception-handling-for-rest-with-spring)，我们展示了如何处理框架生成的异常，因此在这一点上，这绝对是一本好书。

    由于我们的应用程序生成了RepositoryConstraintViolationException异常，我们将为此特定异常创建一个处理程序，该处理程序将修改响应消息。

    这是我们的RestResponseEntityExceptionHandler类：

    ```java
    @ControllerAdvice
    public class RestResponseEntityExceptionHandler extends
    ResponseEntityExceptionHandler {

        @ExceptionHandler({ RepositoryConstraintViolationException.class })
        public ResponseEntity<Object> handleAccessDeniedException(
        Exception ex, WebRequest request) {
            RepositoryConstraintViolationException nevEx = 
                (RepositoryConstraintViolationException) ex;

            String errors = nevEx.getErrors().getAllErrors().stream()
                .map(p -> p.toString()).collect(Collectors.joining("\n"));
            
            return new ResponseEntity<Object>(errors, new HttpHeaders(),
                HttpStatus.PARTIAL_CONTENT);
        }
    }
    ```

    使用此自定义处理程序，我们的返回对象将包含有关所有检测到的错误的信息。

4. 结论

    在本文中，我们表明，验证器对于每个Spring Data REST API都是必不可少的，它为数据插入提供了额外的安全层。

    我们还说明了使用注释创建新验证器是多么简单。
