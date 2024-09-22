# [Spring MVC中的自定义数据Binder](https://www.baeldung.com/spring-mvc-custom-data-binder)

1. 一览表

    本文将展示我们如何使用Spring的数据绑定机制，通过将自动原语应用于对象转换，使我们的代码更加清晰和可读。

    默认情况下，Spring只知道如何转换简单类型。换句话说，一旦我们向控制器Int、String或Boolean类型的数据提交数据，它将自动绑定到适当的Java类型。

    但在现实世界的项目中，这还不够，因为我们可能需要绑定更复杂的对象类型。

2. 绑定单个对象以请求参数

    让我们从简单开始，首先绑定一个简单的类型；我们必须提供`Converter<S，T>`接口的自定义实现，其中S是我们转换的类型，T是我们转换的类型：

    ```java
    @Component
    public class StringToLocalDateTimeConverter
    implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            return LocalDateTime.parse(
            source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
    ```

    现在我们可以在控制器中使用以下语法：

    ```java
    @GetMapping("/findbydate/{date}")
    public GenericEntity findByDate(@PathVariable("date") LocalDateTime date) {
        return ...;
    }
    ```

    1. 使用枚举作为请求参数

        接下来，我们将看看如何使用枚举作为请求参数。

        在这里，我们有一个简单的枚举模式：

        ```java
        public enum Modes {
            ALPHA, BETA;
        }
        ```

        我们将构建一个字符串到枚举转换器，如下所示：

        ```java
        public class StringToEnumConverter implements Converter<String, Modes> {
            @Override
            public Modes convert(String from) {
                return Modes.valueOf(from);
            }
        }
        ```

        然后，我们需要注册我们的转换器：

        ```java
        @Configuration
        public class WebConfig implements WebMvcConfigurer {
            @Override
            public void addFormatters(FormatterRegistry registry) {
                registry.addConverter(new StringToEnumConverter());
            }
        }
        ```

        现在我们可以将我们的列数用作请求参数：

        ```java
        @GetMapping
        public ResponseEntity<Object> getStringToMode(@RequestParam("mode") Modes mode) {
            // ...
        }
        ```

        或者作为路径可變性：

        ```java
        @GetMapping("/entity/findbymode/{mode}")
        public GenericEntity findByEnum(@PathVariable("mode") Modes mode) {
            // ...
        }
        ```

3. 绑定对象的层次结构

    有时我们需要转换对象层次结构的整个树，有一个更集中的绑定而不是一组单独的转换器是有意义的。

    在本例中，我们有AbstractEntity我们的基类：

    ```java
    public abstract class AbstractEntity {
        long id;
        public AbstractEntity(long id){
            this.id = id;
        }
    }
    ```

    以及Foo和Bar的子类：

    ```java
    public class Foo extends AbstractEntity {
        private String name;
        // standard constructors, getters, setters
    }

    public class Bar extends AbstractEntity {
        private int value;
        // standard constructors, getters, setters
    }
    ```

    在这种情况下，我们可以实现`ConverterFactory<S，R>`，其中S将是我们转换的类型，R是定义我们可以转换为的类范围的基础类型：

    ```java
    public class StringToAbstractEntityConverterFactory 
    implements ConverterFactory<String, AbstractEntity>{

        @Override
        public <T extends AbstractEntity> Converter<String, T> getConverter(Class<T> targetClass) {
            return new StringToAbstractEntityConverter<>(targetClass);
        }

        private static class StringToAbstractEntityConverter<T extends AbstractEntity>
        implements Converter<String, T> {

            private Class<T> targetClass;

            public StringToAbstractEntityConverter(Class<T> targetClass) {
                this.targetClass = targetClass;
            }

            @Override
            public T convert(String source) {
                long id = Long.parseLong(source);
                if(this.targetClass == Foo.class) {
                    return (T) new Foo(id);
                }
                else if(this.targetClass == Bar.class) {
                    return (T) new Bar(id);
                } else {
                    return null;
                }
            }
        }
    }
    ```

    正如我们所看到的，唯一必须实现的方法是getConverter()，它返回所需类型的转换器。然后将转换过程委托给此转换器。

    然后，我们需要注册我们的ConverterFactory：

    ```java
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverterFactory(new StringToAbstractEntityConverterFactory());
        }
    }
    ```

    最后，我们可以在控制器中按照我们喜欢的使用它：

    ```java
    @RestController
    @RequestMapping("/string-to-abstract")
    public class AbstractEntityController {

        @GetMapping("/foo/{foo}")
        public ResponseEntity<Object> getStringToFoo(@PathVariable Foo foo) {
            return ResponseEntity.ok(foo);
        }
        
        @GetMapping("/bar/{bar}")
        public ResponseEntity<Object> getStringToBar(@PathVariable Bar bar) {
            return ResponseEntity.ok(bar);
        }
    }
    ```

4. 绑定域对象

    在某些情况下，我们想要将数据绑定到对象，但它要么以非直接的方式（例如，来自Session、Header或Cookie变量），要么甚至存储在数据源中。在这些情况下，我们需要使用不同的解决方案。

    1. 自定义参数解析器

        首先，我们将为这些参数定义一个注释：

        ```java
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.PARAMETER)
        public @interface Version {
        }
        ```

        然后，我们将实现自定义[HandlerMethodArgumentResolver](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/method/support/HandlerMethodArgumentResolver.html)：

        ```java
        public class HeaderVersionArgumentResolver
        implements HandlerMethodArgumentResolver {

            @Override
            public boolean supportsParameter(MethodParameter methodParameter) {
                return methodParameter.getParameterAnnotation(Version.class) != null;
            }

            @Override
            public Object resolveArgument(
            MethodParameter methodParameter, 
            ModelAndViewContainer modelAndViewContainer, 
            NativeWebRequest nativeWebRequest, 
            WebDataBinderFactory webDataBinderFactory) throws Exception {
        
                HttpServletRequest request 
                = (HttpServletRequest) nativeWebRequest.getNativeRequest();

                return request.getHeader("Version");
            }
        }
        ```

        最后一件事是让Spring知道在哪里搜索它们：

        ```java
        @Configuration
        public class WebConfig implements WebMvcConfigurer {

            //...

            @Override
            public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> argumentResolvers) {
                argumentResolvers.add(new HeaderVersionArgumentResolver());
            }
        }
        ```

        就是这样。现在我们可以在控制器中使用它：

        ```java
        @GetMapping("/entity/{id}")
        public ResponseEntity findByVersion(
        @PathVariable Long id, @Version String version) {
            return ...;
        }
        ```

        正如我们所看到的，HandlerMethodArgumentResolver的resolveArgument()方法返回一个对象。换句话说，我们可以返回任何对象，而不仅仅是字符串。

5. 结论

    因此，我们摆脱了许多常规转换，让Spring为我们做大部分事情。最后，让我们总结一下：

    - 对于单个简单类型到对象的转换，我们应该使用转换器实现
    - 为了封装一系列对象的转换逻辑，我们可以尝试ConverterFactory实现
    - 对于任何间接的数据，或者需要应用额外的逻辑来检索相关数据，最好使用HandlerMethodArgumentResolver
