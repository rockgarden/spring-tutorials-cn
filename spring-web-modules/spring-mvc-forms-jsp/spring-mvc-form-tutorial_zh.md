# [在Spring MVC中开始使用表单](https://www.baeldung.com/spring-mvc-form-tutorial)

1. 一览表

    在本文中，我们将讨论Spring表单和数据绑定到控制器。此外，我们将查看Spring MVC中的一个主要注释，即@模型属性。

2. 模型

    首先——让我们定义一个简单的实体，我们将显示并绑定到表单：

    ```java
    public class Employee {
        private String name;
        private long id;
        private String contactNumber;
        // standard getters and setters
    }
    ```

    这将是我们的表单支持对象。

3. 观点

    接下来——让我们定义实际表单，当然还有包含它的HTML文件。我们将使用一个创建/注册新员工的页面：

    ```jsp
    <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
    <html>
        <head>
        </head>
        <body>
            <h3>Welcome, Enter The Employee Details</h3>
            <form:form method="POST" 
            action="/spring-mvc-xml/addEmployee" modelAttribute="employee">
                <table>
                    <tr>
                        <td><form:label path="name">Name</form:label></td>
                        <td><form:input path="name"/></td>
                    </tr>
                    <tr>
                        <td><form:label path="id">Id</form:label></td>
                        <td><form:input path="id"/></td>
                    </tr>
                    <tr>
                        <td><form:label path="contactNumber">
                        Contact Number</form:label></td>
                        <td><form:input path="contactNumber"/></td>
                    </tr>
                    <tr>
                        <td><input type="submit" value="Submit"/></td>
                    </tr>
                </table>
            </form:form>
        </body>
    </html>
    ```

    首先，请注意，我们正在JSP页面中包含一个标签库——表单taglib——以帮助定义我们的表单。

    接下来——`<form:form>`标签在这里发挥着重要作用；它与常规的HTLM`<form>`标签非常相似，但modelAttribute属性是指定支持此表单的模型对象名称的键：

    ```jsp
    <form:form method="POST" 
    action="/SpringMVCFormExample/addEmployee" modelAttribute="employee">
    ```

    这将对应于稍后在控制器中的@ModelAttribute。

    接下来-每个输入字段都使用Spring Form taglib中的另一个有用标签-form:前缀。每个字段都指定了路径属性——这必须对应于模型属性的获取器/设置器（在本例中，是员工类）。加载页面时，输入字段由Spring填充，Spring调用绑定到输入字段的每个字段的获取器。提交表单时，会调用设置器将表单的值保存到对象中。

    最后——当提交表格时，控制器中的POST处理程序被调用，表格会自动绑定到我们传递的员工参数。

4. 控制者

    现在，让我们来看看将处理后端的控制器：

    ```java
    @Controller
    public class EmployeeController {

        @RequestMapping(value = "/employee", method = RequestMethod.GET)
        public ModelAndView showForm() {
            return new ModelAndView("employeeHome", "employee", new Employee());
        }

        @RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
        public String submit(@Valid @ModelAttribute("employee")Employee employee, 
        BindingResult result, ModelMap model) {
            if (result.hasErrors()) {
                return "error";
            }
            model.addAttribute("name", employee.getName());
            model.addAttribute("contactNumber", employee.getContactNumber());
            model.addAttribute("id", employee.getId());
            return "employeeView";
        }
    }
    ```

    控制器定义了两个简单的操作——用于在表单中显示数据的GET和用于通过表单提交创建操作的POST。

    另请注意，如果名为“员工”的对象没有添加到模型中，当我们尝试访问JSP时，Spring会抱怨，因为JSP将被设置为将表单绑定到“员工”模型属性：

    ```log
    java.lang.IllegalStateException:
        Neither BindingResult nor plain target object
        for bean name 'employee' available as request attribute
        at o.s.w.s.s.BindStatus.<init>(BindStatus.java:141)
    ```

    要访问我们的表单支持对象，我们需要通过@ModelAttribute注释注入它。

    方法参数上的@ModelAttribute表示将从模型中检索该参数。如果模型中不存在，则参数将首先被实例化，然后添加到模型中。

5. 处理绑定错误

    默认情况下，当请求绑定期间出现错误时，Spring MVC会抛出异常。这通常不是我们想要的，相反，我们应该向用户展示这些错误。我们将通过将一个作为参数添加到我们的控制器方法来使用BindingResult：

    ```java
    public String submit(
    @Valid @ModelAttribute("employee") Employee employee,
    BindingResult result,
    ModelMap model)
    ```

    BindingResult参数需要放在我们的表单支持对象之后——这是方法参数顺序很重要的罕见情况之一。否则，我们将遇到以下例外情况：

    ```log
    java.lang.IllegalStateException:
    Errors/BindingResult argument declared without preceding model attribute.
        Check your handler method signature!
    ```

    现在——不再抛出异常；相反，将在传递给提交方法的BindingResult上注册错误。此时，我们可以通过多种方式处理这些错误——例如，操作可以取消：

    ```java
    @RequestMapping(value = "/addEmployee", method = RequestMethod.POST)
    public String submit(@Valid @ModelAttribute("employee")Employee employee,
    BindingResult result,  ModelMap model) {
        if (result.hasErrors()) {
            return "error";
        }
        //Do Something
        return "employeeView";
    }
    ```

    请注意，如果结果包含错误，我们会向用户返回另一个视图，以便正确显示这些错误。让我们来看看那个视图——error.jsp：

    ```jsp
    <html>
        <head>
        </head>
        <body>
            <h3>Please enter the correct details</h3>
            <table>
                <tr>
                    <td><a href="employee">Retry</a></td>
                </tr>
            </table>
        </body>
    </html>
    ```

6. 展示一名员工

    最后，除了创建新员工外，我们还可以简单地显示一名员工——以下是快速查看代码：

    ```jsp
    <body>
        <h2>Submitted Employee Information</h2>
        <table>
            <tr>
                <td>Name :</td>
                <td>${name}</td>
            </tr>
            <tr>
                <td>ID :</td>
                <td>${id}</td>
            </tr>
            <tr>
                <td>Contact Number :</td>
                <td>${contactNumber}</td>
            </tr>
        </table>
    </body>
    ```

    JSP页面只是使用EL表达式来显示模型中员工对象的属性值。

7. 测试应用程序

    简单的应用程序可以部署——例如在Tomcat服务器中——并在本地访问：

    <http://localhost:8080/spring-mvc-xml/employee>

    这是包含主表单的视图——在提交操作之前：

    - Spring MVC 表格示例-提交

    提交后，数据将显示：

    - Spring MVC表单示例-查看

    就是这样——使用Spring MVC进行验证的简单表单的工作示例。
