# [Spring Web 注释](https://www.baeldung.com/spring-mvc-annotations)

1. 概述

    在本教程中，我们将探索 org.springframework.web.bind.annotation 包中的 Spring Web 注释。

2. @RequestMapping

    简单地说，@RequestMapping 在 @Controller 类中标记请求处理程序方法；可以使用：

    - path或其aliases、name和value：方法映射到的 URL
    - method：兼容的 HTTP 方法
    - params：根据 HTTP 参数的有无或值过滤请求
    - headers：根据 HTTP 标头的有无或值过滤请求
    - consumes：该方法可在 HTTP 请求正文中消耗的媒体类型
    - produces：该方法可在 HTTP 响应体中生成哪些媒体类型

    下面是一个快速示例：

    ```java
    @Controller
    class VehicleController {
        @RequestMapping(value = "/vehicles/home", method = RequestMethod.GET)
        String home() {
            return "home";
        }
    }
    ```

    如果在类级别应用此注解，我们就能为 @Controller 类中的所有处理程序方法提供默认设置。唯一的例外是 URL，Spring 不会用方法级设置覆盖 URL，而是附加两个路径部分。

    例如，以下配置与上述配置效果相同：

    ```java
    @Controller
    @RequestMapping(value = "/vehicles", method = RequestMethod.GET)
    class VehicleController {
        @RequestMapping("/home")
        String home() {
            return "home";
        }
    }
    ```

    此外，@GetMapping、@PostMapping、@PutMapping、@DeleteMapping 和 @PatchMapping 是 @RequestMapping 的不同变体，其 HTTP 方法已分别设置为 GET、POST、PUT、DELETE 和 PATCH。

    这些方法自 Spring 4.3 发布后可用。

3. @RequestBody

    下面我们来看看 @RequestBody - 它将 HTTP 请求的正文映射到一个对象：

    ```java
    @PostMapping("/save")
    void saveVehicle(@RequestBody Vehicle vehicle) {
        // ...
    }
    ```

    反序列化是自动进行的，取决于请求的内容类型。

4. @PathVariable

    接下来，我们来谈谈 @PathVariable。

    该注解表示一个方法参数绑定到一个 URI 模板变量。我们可以使用 @RequestMapping 注解指定 URI 模板，并使用 @PathVariable 将方法参数绑定到模板部分之一。

    我们可以使用名称或其别名、值参数来实现这一目的：

    ```java
    @RequestMapping("/{id}")
    Vehicle getVehicle(@PathVariable("id") long id) {
        // ...
    }
    ```

    如果模板中的部件名称与方法参数名称一致，我们就不必在注解中指定它：

    ```java
    @RequestMapping("/{id}")
    Vehicle getVehicle(@PathVariable long id) {
        // ...
    }
    ```

    此外，我们还可以通过将参数 required 设为 false 来标记一个可选的路径变量：

    ```java
    @RequestMapping("/{id}")
    Vehicle getVehicle(@PathVariable(required = false) long id) {
        // ...
    }
    ```

5. @RequestParam

    我们使用 @RequestParam 来访问 HTTP 请求参数：

    ```java
    @RequestMapping
    Vehicle getVehicleByParam(@RequestParam("id") long id) {
        // ...
    }
    ```

    它的配置选项与 @PathVariable 注解相同。

    除了这些设置外，当 Spring 发现请求中没有值或值为空时，我们还可以使用 @RequestParam 指定一个注入值。为此，我们必须设置 defaultValue 参数。

    提供默认值会隐式地将 required 设为 false：

    ```java
    @RequestMapping("/buy")
    Car buyCar(@RequestParam(defaultValue = "5") int seatCount) {
        // ...
    }
    ```

    除了参数，我们还可以访问 HTTP 请求的其他部分：cookie 和头信息。我们可以分别使用注解 @CookieValue 和 @RequestHeader 访问它们。

    配置方法与 @RequestParam 相同。

6. 响应处理注解

    在接下来的章节中，我们将了解在 Spring MVC 中处理 HTTP 响应的最常用注解。

    1. @ResponseBody

        如果我们用 @ResponseBody 标记一个请求处理程序方法，Spring 就会将该方法的结果视为响应本身：

        ```java
        @ResponseBody
        @RequestMapping("/hello")
        String hello() {
            return "Hello World!";
        }
        ```

        如果我们使用此注解注解 @Controller 类，所有请求处理程序方法都将使用它。

    2. @ExceptionHandler

        使用此注解，我们可以声明一个自定义错误处理程序方法。当请求处理程序方法抛出任何指定异常时，Spring 会调用该方法。

        捕获的异常可以作为参数传递给该方法：

        ```java
        @ExceptionHandler(IllegalArgumentException.class)
        void onIllegalArgumentException(IllegalArgumentException exception) {
            // ...
        }
        ```

    3. @ResponseStatus

        如果我们使用此注解来注解请求处理程序方法，就可以指定响应所需的 HTTP 状态。我们可以使用 code 参数或其别名 value 参数声明状态代码。

        此外，我们还可以使用 reason 参数提供原因。我们还可以与 @ExceptionHandler 一起使用：

        ```java
        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        void onIllegalArgumentException(IllegalArgumentException exception) {
            // ...
        }
        ```

        有关 HTTP 响应状态的更多信息，请访问[本文](https://www.baeldung.com/spring-mvc-controller-custom-http-status-code)。

7. 其他网络注解

    有些注解并不直接管理 HTTP 请求或响应。在接下来的章节中，我们将介绍最常见的注解。

    1. 控制器

        我们可以使用 @Controller 定义 Spring MVC 控制器。有关详细信息，请访问我们的 Spring Bean 注解文章。

    2. @RestController

        @RestController 将 @Controller 和 @ResponseBody 结合在一起。

        因此，以下声明是等价的：

        ```java
        @Controller
        @ResponseBody
        class VehicleRestController {
            // ...
        }

        @RestController
        class VehicleRestController {
            // ...
        }
        ```

    3. @ModelAttribute

        使用此注解，我们可以通过提供模型键来访问 MVC @Controller 模型中的元素：

        ```java
        @PostMapping("/assemble")
        void assembleVehicle(@ModelAttribute("vehicle") Vehicle vehicleInModel) {
            // ...
        }
        ```

        与 @PathVariable 和 @RequestParam 一样，如果参数名称相同，我们就不必指定模型键：

        ```java
        @PostMapping("/assemble")
        void assembleVehicle(@ModelAttribute Vehicle vehicle) {
            // ...
        }
        ```

        此外，@ModelAttribute 还有另一个用途：如果我们用它注解一个方法，Spring 会自动将该方法的返回值添加到模型中：

        ```java
        @ModelAttribute("vehicle")
        Vehicle getVehicle() {
            // ...
        }
        ```

        和之前一样，我们不必指定模型关键字，Spring 默认使用方法的名称：

        ```java
        @ModelAttribute
        Vehicle vehicle() {
            // ...
        }
        ```

        在 Spring 调用请求处理程序方法之前，它会调用类中所有@ModelAttribute注解的方法。

        有关 @ModelAttribute 的更多信息，请参阅[本文](https://www.baeldung.com/spring-mvc-and-the-modelattribute-annotation)。

    4. @CrossOrigin

        @CrossOrigin 可以使注解的请求处理程序方法实现跨域通信：

        ```java
        @CrossOrigin
        @RequestMapping("/hello")
        String hello() {
            return "Hello World!";
        }
        ```

        如果我们用它标记一个类，它就会应用于其中的所有请求处理程序方法。

        我们可以使用此注解的参数对 CORS 行为进行微调。

        更多详情，请访问[本文](https://www.baeldung.com/spring-cors)。

8. 总结

    在本文中，我们了解了如何使用 Spring MVC 处理 HTTP 请求和响应。
