# [使用AngularJS和Spring MVC进行表单验证](https://www.baeldung.com/validation-angularjs-spring-mvc)

1. 一览表

    验证从未像我们预期的那样简单。当然，验证用户输入到应用程序中的值对于维护我们数据的完整性非常重要。

    在Web应用程序的上下文中，数据输入通常使用HTML表单完成，需要客户端和服务器端验证。

    在本教程中，我们将研究使用AngularJS实现表单输入的客户端验证和使用Spring MVC框架实现服务器端验证。

2. Maven附属机构

    首先，让我们添加以下依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-webmvc</artifactId>
        <version>4.3.7.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.13.0</version>
    </dependency>
    ```

    最新版本的spring-webmvc、hibernate-validator和jackson-databind可以从Maven Central下载。

3. 使用Spring MVC进行验证

    应用程序永远不应该仅仅依赖客户端验证，因为这很容易被规避。为了防止错误或恶意值被保存或导致应用程序逻辑执行不当，在服务器端验证输入值也很重要。

    Spring MVC通过使用JSR 349 Bean Validation规范注释来支持服务器端验证。在本例中，我们将使用规范的参考实现，即休眠验证器。

    1. 数据模型

        让我们创建一个用户类，该类的属性用适当的验证注释进行注释：

        ```java
        public class User {

            @NotNull
            @Email
            private String email;

            @NotNull
            @Size(min = 4, max = 15)
            private String password;

            @NotBlank
            private String name;

            @Min(18)
            @Digits(integer = 2, fraction = 0)
            private int age;

            // standard constructor, getters, setters
        }
        ```

        上面使用的注释属于JSR 349规范，但@Email和@NotBlank除外，它们特定于hibernate-validator库。

    2. SpringMVC控制器

        让我们创建一个定义/user端点的控制器类，该类将用于将新的用户对象保存到列表中。

        为了启用通过请求参数接收的用户对象的验证，声明之前必须有@Valid注释，验证错误将保存在BindingResult实例中。

        为了确定对象是否包含无效值，我们可以使用BindingResult的hasErrors()方法。

        如果hasErrors()返回true，我们可以返回一个JSON数组，其中包含与未通过的验证相关的错误消息。否则，我们将把对象添加到列表中：

        ```java
        @PostMapping(value = "/user")
        @ResponseBody
        public ResponseEntity<Object> saveUser(@Valid User user, 
        BindingResult result, Model model) {
            if (result.hasErrors()) {
                List<String> errors = result.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());
                return new ResponseEntity<>(errors, HttpStatus.OK);
            } else {
                if (users.stream().anyMatch(it -> user.getEmail().equals(it.getEmail()))) {
                    return new ResponseEntity<>(
                    Collections.singletonList("Email already exists!"), 
                    HttpStatus.CONFLICT);
                } else {
                    users.add(user);
                    return new ResponseEntity<>(HttpStatus.CREATED);
                }
            }
        }
        ```

        如您所见，服务器端验证增加了能够执行客户端无法执行的其他检查的优势。

        在我们的案例中，我们可以验证具有相同电子邮件的用户是否已经存在——如果是这样，则返回状态为409 CONFLICT。

        我们还需要定义我们的用户列表，并用几个值初始化它：

        ```java
        private List<User> users = Arrays.asList(
        new User("ana@yahoo.com", "pass", "Ana", 20),
        new User("bob@yahoo.com", "pass", "Bob", 30),
        new User("john@yahoo.com", "pass", "John", 40),
        new User("mary@yahoo.com", "pass", "Mary", 30));
        ```

        让我们也添加一个映射，以检索用户列表作为JSON对象：

        ```java
        @GetMapping(value = "/users")
        @ResponseBody
        public List<User> getUsers() {
            return users;
        }
        ```

        我们在Spring MVC控制器中需要的最后一个项目是返回应用程序主页的映射：

        ```java
        @GetMapping("/userPage")
        public String getUserProfilePage() {
            return "user";
        }
        ```

        我们将在AngularJS部分更详细地查看user.html页面。

    3. SpringMVC配置

        让我们为我们的应用程序添加一个基本的MVC配置：

        ```java
        @Configuration
        @EnableWebMvc
        @ComponentScan(basePackages = "com.baeldung.springmvcforms")
        class ApplicationConfiguration implements WebMvcConfigurer {

            @Override
            public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer) {
                configurer.enable();
            }

            @Bean
            public InternalResourceViewResolver htmlViewResolver() {
                InternalResourceViewResolver bean = new InternalResourceViewResolver();
                bean.setPrefix("/WEB-INF/html/");
                bean.setSuffix(".html");
                return bean;
            }
        }
        ```

    4. 初始化应用程序

        让我们创建一个实现WebApplicationInitializer接口的类来运行我们的应用程序：

        ```java
        public class WebInitializer implements WebApplicationInitializer {

            public void onStartup(ServletContext container) throws ServletException {
                AnnotationConfigWebApplicationContext ctx
                = new AnnotationConfigWebApplicationContext();
                ctx.register(ApplicationConfiguration.class);
                ctx.setServletContext(container);
                container.addListener(new ContextLoaderListener(ctx));

                ServletRegistration.Dynamic servlet 
                = container.addServlet("dispatcher", new DispatcherServlet(ctx));
                servlet.setLoadOnStartup(1);
                servlet.addMapping("/");
            }
        }
        ```

    5. 使用Curl测试Spring Mvc验证

        在我们实现AngularJS客户端部分之前，我们可以使用cURL测试我们的API，命令：

        ```bash
        curl -i -X POST -H "Accept:application/json"
        "localhost:8080/spring-mvc-forms/user?email=aaa&password=12&age=12"
        ```

        响应是一个包含默认错误消息的数组：

        ```json
        [
            "not a well-formed email address",
            "size must be between 4 and 15",
            "may not be empty",
            "must be greater than or equal to 18"
        ]
        ```

4. AngularJS验证

    客户端验证有助于创造更好的用户体验，因为它为用户提供了有关如何成功提交有效数据的信息，并使他们能够继续与应用程序交互。

    AngularJS库非常支持在表单字段上添加验证要求、处理错误消息以及为有效和无效表单设置样式。

    首先，让我们创建一个注入ngMessages模块的AngularJS模块，该模块用于验证消息：

    `var app = angular.module('app', ['ngMessages']);`

    接下来，让我们创建一个AngularJS服务和控制器，该服务和控制器将消耗上一节中构建的API。

    1. AngularJS服务

        我们的服务将有两个调用MVC控制器方法的方法——一种用于保存用户，一种用于检索用户列表：

        ```js
        app.service('UserService',['$http', function ($http) {

            this.saveUser = function saveUser(user){
                return $http({
                method: 'POST',
                url: 'user',
                params: {email:user.email, password:user.password, 
                    name:user.name, age:user.age},
                headers: 'Accept:application/json'
                });
            }
        
            this.getUsers = function getUsers(){
                return $http({
                method: 'GET',
                url: 'users',
                headers:'Accept:application/json'
                }).then( function(response){
                return response.data;
                } );
            }

        }]);
        ```

    2. AngularJS控制器

        UserCtrl控制器注入UserService，调用服务方法并处理响应和错误消息：

        ```js
        app.controller('UserCtrl', ['$scope','UserService', function ($scope,UserService) {

        $scope.submitted = false;

        $scope.getUsers = function() {
            UserService.getUsers().then(function(data) {
                $scope.users = data;
                });
            }

            $scope.saveUser = function() {
            $scope.submitted = true;
            if ($scope.userForm.$valid) {
                    UserService.saveUser($scope.user)
                    .then (function success(response) {
                        $scope.message = 'User added!';
                        $scope.errorMessage = '';
                        $scope.getUsers();
                        $scope.user = null;
                        $scope.submitted = false;
                    },
                    function error(response) {
                        if (response.status == 409) {
                            $scope.errorMessage = response.data.message;
                    }
                    else {
                            $scope.errorMessage = 'Error adding user!';
                    }
                        $scope.message = '';
                    });
            }
            }

        $scope.getUsers();
        }]);
        ```

        我们可以在上面的例子中看到，只有当userForm的$valid属性为真时，才会调用服务方法。尽管如此，在这种情况下，对重复的电子邮件进行额外的检查，这只能在服务器上完成，并在error()函数中单独处理。

        此外，请注意，有一个已提交的变量定义，该变量将告诉我们表格是否已提交。

        最初，此变量为false，在调用saveUser()方法时，它变为true。如果我们不希望在用户提交表格之前显示验证消息，我们可以使用提交的变量来防止这种情况。

    3. 使用AngularJS验证的表单

        为了使用AngularJS库和我们的AngularJS模块，我们需要将脚本添加到ouruser.html页面：

        ```jsp
        <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.6/angular.min.js">
        </script>
        <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.0/angular-messages.js">
        </script>
        <script src="js/app.js"></script>
        ```

        然后，我们可以通过设置ng-app和ng-controller属性来使用我们的模块和控制器：

        `<body ng-app="app" ng-controller="UserCtrl">`

        让我们创建我们的HTML表单：

        ```jsp
        <form name="userForm" method="POST" novalidate
        ng-class="{'form-error':submitted}" ng-submit="saveUser()" >
        ...
        </form>
        ```

        请注意，我们必须在表单上设置novalidate属性，以防止默认的HTML5验证，并将其替换为我们自己的验证。

        如果提交的变量具有true值，ng-class属性会动态地将表单错误CSS类添加到表单中。

        ng-submit属性定义了AngularJS控制器函数，该函数在提交表单时将被调用。使用ng-submit而不是ng-click的优点是，它还可以响应使用ENTER键提交表格。

        现在让我们为用户属性添加四个输入字段：

        ```jsp
        <label class="form-label">Email:</label>
        <input type="email" name="email" required ng-model="user.email" class="form-input"/>

        <label class="form-label">Password:</label>
        <input type="password" name="password" required ng-model="user.password"
        ng-minlength="4" ng-maxlength="15" class="form-input"/>

        <label class="form-label">Name:</label>
        <input type="text" name="name" ng-model="user.name" ng-trim="true"
        required class="form-input" />

        <label class="form-label">Age:</label>
        <input type="number" name="age" ng-model="user.age" ng-min="18"
        class="form-input" required/>
        ```

        每个输入字段都通过ng-model属性绑定到用户变量的属性。

        为了设置验证规则，我们使用HTML5所需属性和几个AngularJS特定属性：ng-minglength、ng-maxlength、ng-min和ng-trim。

        对于电子邮件字段，我们还使用带有电子邮件值的类型属性进行客户端电子邮件验证。

        为了添加与每个字段对应的错误消息，AngularJS提供了ng-messages指令，该指令循环输入的$errors对象，并根据每个验证规则显示消息。

        让我们在输入定义之后添加电子邮件字段的指令：

        ```jsp
        <div ng-messages="userForm.email.$error"
        ng-show="submitted && userForm.email.$invalid" class="error-messages">
            <p ng-message="email">Invalid email!</p>
            <p ng-message="required">Email is required!</p>
        </div>
        ```

        可以为其他输入字段添加类似的错误消息。

        我们可以使用带有布尔表达式的ng-show属性来控制电子邮件字段的指令何时显示。在我们的示例中，当字段具有无效值时，我们会显示指令，这意味着$invalid属性为真，提交的变量也为真。

        一个字段一次只会显示一条错误消息。

        我们还可以在输入字段后添加一个勾号（由HEX代码字符✓表示），如果字段有效，则取决于$valid属性：

        `<div class="check" ng-show="userForm.email.$valid">✓</div>`

        AngularJS验证还支持使用CSS类（如ng-valid和ng-invalid）或更具体的类（如ng-invalid-required和ng-invalid-minlength）进行样式。

        让我们为表单的表单错误类中的无效输入添加CSS属性border-color:red：

        ```css
        .form-error input.ng-invalid {
            border-color:red;
        }
        ```

        我们还可以使用CSS类以红色显示错误消息：

        ```css
        .error-messages {
            color:red;
        }
        ```

        将所有内容放在一起后运行，构建AngularJS表单验证示例。
5. 结论

    在本教程中，我们展示了如何使用AngularJS和Spring MVC结合客户端和服务器端验证。
