# [探索SpringMVC的表单标签库](https://www.baeldung.com/spring-mvc-form-tags)

1. 概述
   在本系列的[第一篇](https://www.baeldung.com/spring-mvc-form-tutorial)文章，介绍了表单标签库的使用以及如何将数据绑定到控制器中。

   在这篇文章中，我们将介绍Spring MVC提供的各种标签，帮助我们创建和验证表单(create and validate forms)。
2. input标签
   我们将从输入标签开始。这个标签默认使用绑定的值和type='text'来渲染一个HTML输入标签。

   `<form:input path="name" />`

   从Spring 3.1开始，你可以使用其他HTML5特有的类型，如电子邮件、日期和其他。例如，如果我们想创建一个电子邮件字段，我们可以使用type='email'。

   `<form:input type="email" path="email" />`

   同样地，如果要创建一个日期字段，我们可以使用type='date'，这将在许多兼容HTML5的浏览器中呈现一个日期选择器。

   `<form:input type="date" path="dateOfBirth" />`

3. password标签

   这个标签使用绑定的值渲染了一个type='password'的HTML输入标签。这个HTML输入掩盖了打入字段的值。

   `<form:password path="password" />

4. textarea标签

   这个标签渲染了一个HTML文本区域。

   `<form:textarea path="notes" rows="3" cols="20" />`

   我们可以用与HTML textarea相同的方式指定行和列的数量。

5. checkbox和checkboxes标签

   checkbox标签渲染了一个type='checkbox'的HTML输入标签。Spring MVC的表单标签库为checkbox标签提供了不同的方法，应该可以满足我们所有的checkbox需求。

   `<form:checkbox path="receiveNewsletter" />`

   上面的例子生成了一个经典的单选框，有一个布尔值。如果我们将绑定的值设置为true，这个复选框将被默认选中。

   下面的例子生成了多个复选框。在这种情况下，复选框的值是在JSP页面里面硬编码的。

    ```html
    Bird watching: <form:checkbox path="hobbies" value="Bird watching"/>
    Astronomy: <form:checkbox path="hobbies" value="Astronomy"/>
    Snowboarding: <form:checkbox path="hobbies" value="Snowboarding"/>
    ```

   这里，绑定的值是数组或java.util.Collection的类型。

   `String[] hobbies;`

   checkboxes标签的目的是用来渲染多个复选框，其中的复选框值是在运行时生成的。

   `<form:checkboxes items="${favouriteLanguageItem}" path="favoriteLanguage" />`

   为了生成这些值，我们在items属性中传递一个数组、一个列表或一个包含可用选项的map。我们可以在控制器中初始化我们的值。

    ```java
    List<String> favouriteLanguageItem = new ArrayList<String>();
    favouriteLanguageItem.add("Java");
    favouriteLanguageItem.add("C++");
    favouriteLanguageItem.add("Perl");
    ```

   通常情况下，绑定的属性是一个集合，所以它可以容纳用户选择的多个值。

   `List<String> favouriteLanguage;`

6. radiobutton和radiobuttons标签

   这个标签渲染了一个type='radio'的HTML输入标签。

    ```html
    Male: <form:radiobutton path="sex" value="M"/>
    Female: <form:radiobutton path="sex" value="F"/>
    ```

   一个典型的使用模式会涉及到多个标签实例，它们的不同值被绑定到同一个属性。

   `private String sex;`

   就像checkboxes标签一样，radiobuttons标签也会渲染多个type='radio'的HTML输入标签。

   `<form:radiobuttons items="${jobItem}" path="job" />`

   在这种情况下，我们可能想把可用的选项作为一个数组、一个列表或一个包含 items 属性中可用选项的 Map 传入。

    ```java
    List<String> jobItem = new ArrayList<String>();
    jobItem.add("Full time");
    jobItem.add("Part time");
    ```

7. select标签

    这个标签渲染了一个HTML选择元素。

    `<form:select path="country" items="${countryItems}" />`

    为了生成数值，我们在 items 属性中传递一个数组、一个列表或一个包含可用选项的地图。再一次，我们可以在控制器中初始化我们的值。

    ```java
    Map<String, String> countryItems = new LinkedHashMap<String, String>();
    countryItems.put("US", "United States");
    countryItems.put("IT", "Italy");
    countryItems.put("UK", "United Kingdom");
    countryItems.put("FR", "France");
    ```

    select标签也支持使用嵌套的option和options标签。

    option标签渲染一个单一的HTML选项，而options标签则渲染一个HTML选项标签的列表。

    options标签和select标签一样，在items属性中接受一个数组、一个列表或一个包含可用选项的Map。

    ```html
    <form:select path="book">
        <form:option value="-" label="--请选择--"/>
        <form:options items="${books}" />
    </form:select>
    ```

    当我们有需要一次选择几个项目时，我们可以创建一个多重列表框。要呈现这种类型的列表，只需在选择标签中添加multiple="true"属性。

    `<form:select path="fruit" items="${fruit}" multiple="true"/>`

    这里的绑定属性是一个数组或一个java.util.Collection。

    `List<String> fruit`

8. 隐藏的标签

   此标记使用绑定值呈现type='hidden'的HTML输入标记：

   `<form:hidden path="id" value="12345" />`

9. Errors标签

   字段错误消息由与控制器关联的验证器生成。我们可以使用错误标记来呈现这些字段错误消息：

   `<form:errors path="name" cssClass="error" />`

   这将显示路径属性中指定的字段的错误。默认情况下，错误消息在span标记中呈现，路径值后面附加.errors作为id，还可以选择cssClass属性中的CSS类，该类可用于设置输出的样式：

   `<span id="name.errors" class="error">Name is required!</span>`

   要用不同的元素而不是默认的span标记来封装错误消息，我们可以在元素属性中指定首选元素：

   `<form:errors path="name" cssClass="error" element="div" />`

   这将在div元素中呈现错误消息：

   `<div id="name.errors" class="error">Name is required!</div>`

   除了能够显示特定输入元素的错误之外，我们还可以显示给定页面的整个错误列表（不考虑字段）。这是通过使用通配符*实现的：

   `<form:errors path="*" />`

## 验证器

为了显示一个给定字段的错误，我们需要定义一个验证器Validator。

```java
public class PersonValidator implements Validator {

    @Override
    public boolean supports(Class clazz) {
        return Person.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "required.name");
    }
}
```

在这种情况下，如果字段名是空的，验证器会从资源包中返回由required.name标识的错误信息。

资源包在Spring的XML配置文件中定义如下。

```xml
<bean class="org.springframework.context.support.ResourceBundleMessageSource" id="messageSource">
     <property name="basename" value="messages" />
</bean>
```

或者用纯Java的配置风格。

```java
@Bean
public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasenames("messages");
    return messageSource;
}
```

错误信息是在messages.properties文件里面定义的。

`required.name = Name is required!`

为了应用这个验证，我们需要在控制器中包含一个对验证器的引用，并在控制器方法中调用验证方法，该方法在用户提交表单时被调用。

```java
@RequestMapping(value = "/addPerson", method = RequestMethod.POST)
public String submit(
  @ModelAttribute("person") Person person, 
  BindingResult result, 
  ModelMap modelMap) {

    validator.validate(person, result);

    if (result.hasErrors()) {
        return "personForm";
    }
    
    modelMap.addAttribute("person", person);
    return "personView";
}
```

## JSR 303 Bean验证

从Spring 3开始，我们可以使用JSR 303（通过 @Valid annotation 注解）进行bean验证。要做到这一点，我们需要在classpath上有一个JSR303验证器框架。我们将使用Hibernate验证器（参考实现）。以下是我们需要在POM中包含的依赖关系。

```xml
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>7.0.5.Final</version>
</dependency>
```

> <https://hibernate.org/validator/releases/7.0/> The main change is that all the dependencies using javax. packages are now using jakarta.* packages.

为了使Spring MVC通过@Valid注解支持JSR 303验证，我们需要在Spring配置文件中启用以下内容。

`<mvc:annotation-driven/>`

或者在Java配置中使用相应的注解@EnableWebMvc。

```java
@EnableWebMvc
@Configuration
public class ClientWebConfigJava implements WebMvcConfigurer {
    // All web configuration will go here
}
```

接下来，我们需要用@Valid注解来注解我们要验证的控制器方法。

```java
@RequestMapping(value = "/addPerson", method = RequestMethod.POST)
public String submit(
  @Valid @ModelAttribute("person") Person person, 
  BindingResult result, 
  ModelMap modelMap) {
    if(result.hasErrors()) {
        return "personForm";
    }    
    modelMap.addAttribute("person", person);
    return "personView";
}
```

现在我们可以用Hibernate验证器注解来验证该实体的属性。

```java
@NotEmpty
private String password;
```

默认情况下，如果我们让密码输入字段为空，这个注解将显示 "may not be empty"。

我们可以通过在验证器示例中定义的资源包中创建一个属性来覆盖默认的错误信息。消息的关键遵循规则AnnotationName.entity.fieldname:

`NotEmpty.person.password = Password is required!`

## code

我们探讨了Spring提供的用于处理表单的各种标签。

我们还看了用于显示验证错误的标签以及显示自定义错误信息所需的配置。

学习了如何在Spring应用程序中验证请求参数和路径变量。

当项目在本地运行时，可以通过以下地址访问表单示例。

<http://localhost:8080/spring-mvc-xml/person>

### TODO

spring MVC 与 springBoot 版本不匹配 导致运行 SpringContextTest.java 报错：

```log
Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'requestMappingHandlerMapping' defined in class path resource [org/springframework/web/servlet/config/annotation/DelegatingWebMvcConfiguration.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping]: Factory method 'requestMappingHandlerMapping' threw exception; nested exception is java.lang.ClassCastException: org.springframework.web.accept.ContentNegotiationManagerFactoryBean$$EnhancerBySpringCGLIB$$6dd798c1 cannot be cast to org.springframework.web.accept.ContentNegotiationManager
```
