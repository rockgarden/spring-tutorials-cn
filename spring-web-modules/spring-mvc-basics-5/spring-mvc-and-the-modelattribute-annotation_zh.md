# [Spring MVC和@ModelAttribute注释](https://www.baeldung.com/spring-mvc-and-the-modelattribute-annotation)

1. 概述

    Spring MVC中最重要的一项注解之一是[@ModelAttribute](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html)。

    @ModelAttribute 是一个注解，它将方法参数或方法返回值绑定到命名的模型属性，并将其暴露给web视图。

    在这个教程中，我们将通过一个常见的概念——来自公司员工的表单提交，来展示这个注解的可用性和功能。

2. 深入理解@ModelAttribute**

    正如引言段落所述，我们可以将*@ModelAttribute*用作方法参数或方法级别。

    1. 作为方法级别

        当我们把注解用在方法级别时，它表示该方法的目的是添加一个或多个模型属性。这些方法支持与[@RequestMapping](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestMapping.html)方法相同的参数类型，但它们不能直接映射到请求。

        让我们看一个快速的例子来理解其工作原理：

        ```java
        @ModelAttribute
        public void addAttributes(Model model) {
            model.addAttribute("msg", "Welcome to the Netherlands!");
        }
        ```

        在上面的例子中，我们看到一个方法，它向控制器类中定义的所有模型添加名为msg的属性。

        当然，稍后在文章中我们将看到实际应用。

        通常情况下，Spring MVC会在调用任何请求处理方法之前，总是先调用这个方法。基本上，带有*@ModelAttribute的方法在调用带有@RequestMapping*注解的控制器方法之前被调用。这是因为控制器方法内的任何处理都需要在模型对象创建之后进行。

        同样重要的是，我们需要将相应的类注解为[@ControllerAdvice](http://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ControllerAdvice.html)。这样，我们就可以在响应中为每个方法添加全局的模型值。这意味着对于每一个请求，对每个方法都会有一个默认的响应值。

    2. 作为方法参数

        当我们将注解用作方法参数时，它表示从模型中检索参数。如果没有注解，应该首先实例化它，然后添加到模型中。一旦在模型中，字段应从具有匹配名称的所有请求参数中填充数据。

        在下面的代码片段中，我们将使用employee模型属性从addEmployee端点接收表单数据。Spring MVC会在调用submit方法之前在幕后完成这个操作：

        ```java
        @RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
        public String submit(@ModelAttribute("employee") Employee employee) {
            // Code that uses the employee object
            return "employeeView";
        }
        ```

        稍后，我们将看到如何使用employee对象来填充employeeView模板的完整示例。

        它将表单数据绑定到一个bean。带有*@RequestMapping注解的控制器可以有自定义类参数（这些参数被注解为@ModelAttribute*）。

        在Spring MVC中，我们称之为数据绑定，这是一种常见的机制，可以避免我们不得不单独解析每个表单字段。

3. 表单示例

    在这一部分，我们将查看概述章节中概述的示例，一个非常基础的表单，提示用户（具体来说是公司员工）输入一些个人信息（具体是姓名和id）。提交完成后，如果没有任何错误，用户期望在另一个屏幕上看到先前提交的数据。

    1. 视图

        首先，我们创建一个简单的包含id和name字段的表单：

        ```html
        <form:form method="POST" action="/spring-mvc-basics/addEmployee" 
        modelAttribute="employee">
            <form:label path="name">Name</form:label>
            <form:input path="name" />
            <form:label path="id">Id</form:label>
            <form:input path="id" />
            <input type="submit" value="Submit" />
        </form:form>
        ```

    2. 控制器

        这里是控制器类，我们将实现上述视图的逻辑：

        ```java
        @Controller
        @ControllerAdvice
        public class EmployeeController {

            private Map<Long, Employee> employeeMap = new HashMap<>();

            @RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
            public String submit(
            @ModelAttribute("employee") Employee employee,
            BindingResult result, ModelMap model) {
                if (result.hasErrors()) {
                    return "error";
                }
                model.addAttribute("name", employee.getName());
                model.addAttribute("id", employee.getId());

                employeeMap.put(employee.getId(), employee);

                return "employeeView";
            }

            @ModelAttribute
            public void addAttributes(Model model) {
                model.addAttribute("msg", "Welcome to the Netherlands!");
            }
        }
        ```

        在submit()方法中，我们将一个Employee对象绑定到我们的视图。我们可以如此简单地将表单字段映射到对象模型。在方法中，我们从表单中获取值并设置到[ModelMap](http://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/ui/ModelMap.html)中。

        最后，我们返回employeeView，这意味着我们调用相应的JSP文件作为视图代表。

        此外，还有一个addAttributes()方法。它的目的是在模型中添加全局识别的值。也就是说，对每个控制器方法的每个请求都将返回一个默认的响应值。我们还需要将特定类注解为@ControllerAdvice。

    3. 模型

        如前所述，Model对象非常简单，包含了“前端”属性所需的一切。现在让我们看看一个例子：

        ```java
        @XmlRootElement
        public class Employee {

            private long id;
            private String name;

            public Employee(long id, String name) {
                this.id = id;
                this.name = name;
            }

            // standard getters and setters removed
        }
        ```

    4. 总结

        @ControllerAdvice辅助控制器，特别是应用于所有@RequestMapping方法的@ModelAttribute方法。当然，我们的addAttributes()方法将在其他@RequestMapping方法之前首先运行。

        考虑到这一点，在submit()和addAttributes()运行之后，我们可以在从控制器类返回的视图中通过美元化的花括号对提到它们的名字，比如${name}。

    5. 结果视图

        现在让我们打印从表单接收到的内容：

        ```html
        <h3>${msg}</h3>
        Name : ${name}
        ID : ${id}
        ```

4. 结论

    在这篇文章中，我们研究了*@ModelAttribute*注解在方法参数和方法级别用法的应用。
