# [Spring MVC自定义验证](https://www.baeldung.com/spring-mvc-custom-validator)

1. 一览表

    通常，当我们需要验证用户输入时，Spring MVC提供标准的预定义验证器。

    然而，当我们需要验证更特定类型的输入时，我们有能力创建自己的自定义验证逻辑。

    在本教程中，我们将这样做；我们将创建一个自定义验证器来验证带有电话号码字段的表单，然后我们将为多个字段显示自定义验证器。

    本教程侧重于Spring MVC。我们的文章题为“[Spring Boot中的验证](https://www.baeldung.com/spring-boot-bean-validation)”，描述了如何在Spring Boot中创建自定义验证。

2. 设置

    为了从API中受益，我们将将依赖项添加到我们的pom.xml文件中：

    ```xml
    <dependency>
        <groupId>org.hibernate.validator</groupId>
        <artifactId>hibernate-validator</artifactId>
        <version>8.0.1.Final</version>
    </dependency>
    ```

    可以在这里查看最新版本的依赖项。

    如果我们使用Spring Boot，那么我们添加spring-boot-starter-validation，这也将带来休眠验证器依赖关系。

3. 自定义验证

    创建自定义验证器需要推出我们自己的注释，并在模型中使用它来执行验证规则。

    因此，让我们创建我们的自定义验证器，该验证器检查电话号码。电话号码必须是至少八位数字，但不超过11位数字。

4. 新注释

    让我们创建一个新的@interface来定义我们的注释：

    ```java
    @Documented
    @Constraint(validatedBy = ContactNumberValidator.class)
    @Target( { ElementType.METHOD, ElementType.FIELD })
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ContactNumberConstraint {
        String message() default "Invalid phone number";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
    }
    ```

    使用@Constraint注释，我们定义了将验证我们字段的类。message()是用户界面中显示的错误消息。最后，附加代码主要是符合Spring标准的模板代码。

5. 创建验证器

    现在让我们创建一个验证器类，以强制执行我们的验证规则：

    ```java
    public class ContactNumberValidator implements 
    ConstraintValidator<ContactNumberConstraint, String> {
        @Override
        public void initialize(ContactNumberConstraint contactNumber) {
        }

        @Override
        public boolean isValid(String contactField,
        ConstraintValidatorContext cxt) {
            return contactField != null && contactField.matches("[0-9]+")
            && (contactField.length() > 8) && (contactField.length() < 14);
        }
    }
    ```

    验证类实现了ConstraintValidator接口，还必须实现isValid方法；正是在这个方法中，我们定义了验证规则。

    当然，我们在这里使用一个简单的验证规则，以展示验证器的工作原理。

    ConstraintValidator定义了验证给定对象的给定约束的逻辑。实施必须遵守以下限制：

    - 对象必须解析为非参数化类型
    - 对象的通用参数必须是无界通配符类型

6. 应用验证注释

    在我们的案例中，我们创建了一个包含一个字段的简单类来应用验证规则。在这里，我们正在设置注释字段进行验证：

    ```java
    @ContactNumberConstraint
    private String phone;
    ```

    我们定义了一个字符串字段，并用我们的自定义注释@ContactNumberConstraint进行注释。在我们的控制器中，我们创建了映射并处理了任何错误：

    ```java
    @Controller
    public class ValidatedPhoneController {
        @GetMapping("/validatePhone")
        public String loadFormPage(Model m) {
            m.addAttribute("validatedPhone", new ValidatedPhone());
            return "phoneHome";
        }
        
        @PostMapping("/addValidatePhone")
        public String submitForm(@Valid ValidatedPhone validatedPhone,
        BindingResult result, Model m) {
            if(result.hasErrors()) {
                return "phoneHome";
            }
            m.addAttribute("message", "Successfully saved phone: "
            + validatedPhone.toString());
            return "phoneHome";
        }   
    }
    ```

    我们定义了这个具有单个JSP页面的简单控制器，并使用submitForm方法来强制验证我们的电话号码。

7. 观点

    我们的观点是一个基本的JSP页面，其表单只有一个字段。当用户提交表格时，该字段将由我们的自定义验证器验证，并重定向到同一页面，并带有验证成功或失败的消息：

    ```jsp
    <form:form
    action="/${pageContext.request.contextPath}/addValidatePhone"
    modelAttribute="validatedPhone">
        <label for="phoneInput">Phone: </label>
        <form:input path="phone" id="phoneInput" />
        <form:errors path="phone" cssClass="error" />
        <input type="submit" value="Submit" />
    </form:form>
    ```

8. 测试

    现在让我们测试一下我们的控制器，看看它是否给我们提供了适当的响应和视图：

    ```java
    @Test
    public void givenPhonePageUri_whenMockMvc_thenReturnsPhonePage(){
        this.mockMvc.
        perform(get("/validatePhone")).andExpect(view().name("phoneHome"));
    }
    ```

    让我们也测试一下我们的字段是否根据用户输入进行验证：

    ```java
    @Test
    public void
    givenPhoneURIWithPostAndFormData_whenMockMVC_thenVerifyErrorResponse() {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/addValidatePhone").
        accept(MediaType.TEXT_HTML).
        param("phoneInput", "123")).
        andExpect(model().attributeHasFieldErrorCode(
            "validatedPhone","phone","ContactNumberConstraint")).
        andExpect(view().name("phoneHome")).
        andExpect(status().isOk()).
        andDo(print());
    }
    ```

    在测试中，我们为用户提供了“123”的输入，正如我们预期的那样，一切都在工作，我们在客户端看到了错误。

9. 自定义类级验证

    也可以在类级别定义自定义验证注释，以验证类的多个属性。

    此场景的常见用例是验证类的两个字段是否具有匹配值。

    1. 创建注释

        让我们添加一个名为FieldsValueMatch的新注释，该注释以后可以应用于类。注释将有两个参数，字段和字段匹配，表示要比较的字段名称：

        ```java
        @Constraint(validatedBy = FieldsValueMatchValidator.class)
        @Target({ ElementType.TYPE })
        @Retention(RetentionPolicy.RUNTIME)
        public @interface FieldsValueMatch {
            String message() default "Fields values don't match!";
            String field();
            String fieldMatch();

            @Target({ ElementType.TYPE })
            @Retention(RetentionPolicy.RUNTIME)
            @interface List {
                FieldsValueMatch[] value();
            }
        }
        ```

        我们可以看到我们的自定义注释还包含一个列表子接口，用于在类上定义多个FieldsValueMatch注释。

    2. 创建验证器

        接下来，我们需要添加包含实际验证逻辑的FieldsValueMatchValidator类：

        ```java
        public class FieldsValueMatchValidator
        implements ConstraintValidator<FieldsValueMatch, Object> {

            private String field;
            private String fieldMatch;

            public void initialize(FieldsValueMatch constraintAnnotation) {
                this.field = constraintAnnotation.field();
                this.fieldMatch = constraintAnnotation.fieldMatch();
            }

            public boolean isValid(Object value, ConstraintValidatorContext context) {
                Object fieldValue = new BeanWrapperImpl(value)
                .getPropertyValue(field);
                Object fieldMatchValue = new BeanWrapperImpl(value)
                .getPropertyValue(fieldMatch);
                
                if (fieldValue != null) {
                    return fieldValue.equals(fieldMatchValue);
                } else {
                    return fieldMatchValue == null;
                }
            }
        }
        ```

        isValid（）方法检索两个字段的值，并检查它们是否相等。

    3. 应用注释

        让我们创建一个用于用户注册所需数据的NewUserForm模型类。它将有两个电子邮件和密码属性，以及两个验证电子邮件和验证密码属性，以重新输入这两个值。

        由于我们有两个字段要检查相应的匹配字段，让我们在NewUserForm类上添加两个@FieldsValueMatch注释，一个用于电子邮件值，一个用于密码值：

        ```java
        @FieldsValueMatch.List({
            @FieldsValueMatch(
            field = "password",
            fieldMatch = "verifyPassword",
            message = "Passwords do not match!"
            ),
            @FieldsValueMatch(
            field = "email",
            fieldMatch = "verifyEmail",
            message = "Email addresses do not match!"
            )
        })
        public class NewUserForm {
            private String email;
            private String verifyEmail;
            private String password;
            private String verifyPassword;
            // standard constructor, getters, setters
        }
        ```

        为了验证Spring MVC中的模型，让我们创建一个带有/user POST映射的控制器，该控制器接收使用@Valid注释的NewUserForm对象，并验证是否存在任何验证错误：

        ```java
        @Controller
        public class NewUserController {
            @GetMapping("/user")
            public String loadFormPage(Model model) {
                model.addAttribute("newUserForm", new NewUserForm());
                return "userHome";
            }

            @PostMapping("/user")
            public String submitForm(@Valid NewUserForm newUserForm, 
            BindingResult result, Model model) {
                if (result.hasErrors()) {
                    return "userHome";
                }
                model.addAttribute("message", "Valid form");
                return "userHome";
            }
        }
        ```

    4. 测试注释

        为了验证我们的自定义类级注释，让我们编写一个JUnit测试，该测试将匹配信息发送到/user端点，然后验证响应是否包含错误：

        ```java
        public class ClassValidationMvcTest {
            private MockMvc mockMvc;

            @Before
            public void setup(){
                this.mockMvc = MockMvcBuilders
                .standaloneSetup(new NewUserController()).build();
            }
            
            @Test
            public void givenMatchingEmailPassword_whenPostNewUserForm_thenOk() 
            throws Exception {
                this.mockMvc.perform(MockMvcRequestBuilders
                .post("/user")
                .accept(MediaType.TEXT_HTML).
                .param("email", "john@yahoo.com")
                .param("verifyEmail", "john@yahoo.com")
                .param("password", "pass")
                .param("verifyPassword", "pass"))
                .andExpect(model().errorCount(0))
                .andExpect(status().isOk());
            }
        }
        ```

        然后，我们还将添加一个JUnit测试，该测试将不匹配的信息发送到/user端点，并断言结果将包含两个错误：

        ```java
        @Test
        public void givenNotMatchingEmailPassword_whenPostNewUserForm_thenOk()
        throws Exception {
            this.mockMvc.perform(MockMvcRequestBuilders
            .post("/user")
            .accept(MediaType.TEXT_HTML)
            .param("email", "<john@yahoo.com>")
            .param("verifyEmail", "<john@yahoo.commmm>")
            .param("password", "pass")
            .param("verifyPassword", "passsss"))
            .andExpect(model().errorCount(2))
            .andExpect(status().isOk());
            }
        ```

10. 摘要

    在这篇简短的文章中，我们学习了如何创建自定义验证器来验证字段或类，然后将它们连接到Spring MVC。
