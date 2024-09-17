# [Spring的@Controller和@RestController注解](https://www.baeldung.com/spring-controller-vs-restcontroller)

在这个简短的教程中，我们将讨论Spring MVC中@Controller和@RestController注解的区别。

我们可以对传统的Spring控制器使用第一个注解，而且它成为框架的一部分已经有很长一段时间了。

Spring 4.0引入了@RestController注解，以简化RESTful Web服务的创建。这是一个方便的注解，它结合了@Controller和@ResponseBody，这样就不需要用@ResponseBody注解来注释控制器类的每个请求处理方法。

进一步阅读：

- [Spring RequestMapping](https://www.baeldung.com/spring-requestmapping)
- [Spring的@RequestParam注解](https://www.baeldung.com/spring-request-param)

1. Spring MVC @Controller

    我们可以用@Controller注解来注释经典的控制器。这只是@Component类的一个特殊化，它允许我们通过classpath扫描自动检测实现类。

    我们通常将@Controller与@RequestMapping注解结合使用，用于请求处理方法。

    让我们来看看Spring MVC控制器的一个简单例子。

    ```java
    @Controller
    @RequestMapping("books")
    public class SimpleBookController {

        @GetMapping("/{id}", produces = "application/json")
        public @ResponseBody Book getBook(@PathVariable int id) {
            return findBookById(id);
        }

        private Book findBookById(int id) {
            // ...
        }
    }
    ```

    我们用@ResponseBody注释了请求处理方法。这个注解使得返回对象能够自动序列化到HttpResponse中。

2. Spring MVC @RestController

    @RestController是控制器的一个专门版本。它包括@Controller和@ResponseBody注解，因此，简化了控制器的实现。

    ```java
    @RestController
    @RequestMapping("books-rest")
    public class SimpleBookRestController {
        
        @GetMapping("/{id}", produces = "application/json")
        public Book getBook(@PathVariable int id) {
            return findBookById(id);
        }

        private Book findBookById(int id) {
            // ...
        }
    }
    ```

    该控制器使用了@RestController注解；因此，不需要@ResponseBody。

    控制器类的每个请求处理方法都自动将返回对象序列化为HttpResponse。
