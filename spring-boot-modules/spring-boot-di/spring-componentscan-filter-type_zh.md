# [Spring@ComponentScan–过滤器类型](https://www.baeldung.com/spring-componentscan-filter-type)

在之前的教程中，我们学习了Spring组件扫描的基础知识。

在本文中，我们将看到@ComponentScan注释提供的不同类型的过滤器选项。

默认情况下，用@Component、@Repository、@Service、@Controller注释的类注册为Spring bean。用@Component注释的自定义注释注释的类也是如此。我们可以通过使用@ComponentScan注释的includeFilters和excludeFilters参数来扩展此行为。

有五种类型的[ComponentScan.Filter](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/context/annotation/ComponentScan.Filter.html)过滤器。筛选器：

- ANNOTATION
- ASSIGNABLE_TYPE
- ASPECTJ
- REGEX
- CUSTOM

我们将在下一节中详细介绍这些内容。

我们应该注意，所有这些过滤器都可以在扫描中包含或排除类。为了简化示例，我们只包含类。

## FilterType.ANNOTATION

ANNOTATION过滤器类型包括或排除用给定注释标记的组件扫描中的类。

例如，我们有一个@Animal注释：componentscan.filter.annotation.Animal.java

现在，让我们定义一个Elephant类，它使用@Animal: componentscan.filter.annotation.Elephant.java

最后，让我们使用FilterType。通知Spring扫描@Animal注释类：

```java
@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION,
        classes = Animal.class))
public class ComponentScanAnnotationFilterApp { }
```

正如我们所看到的，扫描仪可以很好地拾取我们的大象：filter.annotation.ComponentScanAnnotationFilterAppTest.java。

## FilterType.ASSIGNABLE_TYPE

ASSIGNABLE_TYPE在组件扫描期间过滤所有扩展类或实现指定类型接口的类。

首先，让我们声明Animal接口：

`public interface Animal { }`

再一次，让我们声明我们的Elephant类，这次实现了Animal接口：

`public class Elephant implements Animal { }`

让我们宣布我们的Cat类也实现了Animal：

`public class Cat implements Animal { }`

现在，让我们使用ASSIGNABLE_TYPE指导Spring扫描Animal实现类：

```java
@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = Animal.class))
public class ComponentScanAssignableTypeFilterApp { }
```

我们将看到猫和大象都被扫描：

```java
@Test
public void whenAssignableTypeFilterIsUsed_thenComponentScanShouldRegisterBean() {
    ApplicationContext applicationContext =
      new AnnotationConfigApplicationContext(ComponentScanAssignableTypeFilterApp.class);
    List<String> beans = Arrays.stream(applicationContext.getBeanDefinitionNames())
      .filter(bean -> !bean.contains("org.springframework")
        && !bean.contains("componentScanAssignableTypeFilterApp"))
      .collect(Collectors.toList());
    assertThat(beans.size(), equalTo(2));
    assertThat(beans.contains("cat"), equalTo(true));
    assertThat(beans.contains("elephant"), equalTo(true));
}
```

## FilterType.REGEX

REGEX过滤器检查类名是否匹配给定的正则表达式模式。过滤器类型。REGEX检查简单类名和完全限定类名。

声明大象、猫、狮子类。这次没有实现任何接口或用任何注释进行注释：

`public class Elephant { }`
`public class Cat { }`
`public class Loin { }`

让我们使用FilterType。REGEX，指示Spring扫描与REGEX.*[nt]匹配的类。我们的正则表达式计算包含nt的所有内容：

```java
@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
        pattern = ".*[nt]"))
public class ComponentScanRegexFilterApp { }
```

这次在我们的测试中，我们将看到Spring扫描的是大象，而不是狮子：

```java
@Test
public void whenRegexFilterIsUsed_thenComponentScanShouldRegisterBeanMatchingRegex() {
    ApplicationContext applicationContext =
      new AnnotationConfigApplicationContext(ComponentScanRegexFilterApp.class);
    List<String> beans = Arrays.stream(applicationContext.getBeanDefinitionNames())
      .filter(bean -> !bean.contains("org.springframework")
        && !bean.contains("componentScanRegexFilterApp"))
      .collect(Collectors.toList());
    assertThat(beans.size(), equalTo(1));
    assertThat(beans.contains("elephant"), equalTo(true));
}
```

## FilterType.ASPECTJ

当我们想使用表达式挑选类的复杂子集时，我们需要使用FilterType ASPECTJ。

对于这个用例，我们可以重用与前一节相同的三个类。

让我们使用FilterType。ASPECTJ将指示Spring扫描与我们的ASPECTJ表达式匹配的类：

```java
@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.ASPECTJ,
  pattern = "com.baeldung.componentscan.filter.aspectj.* "
  + "&& !(com.baeldung.componentscan.filter.aspectj.L* "
  + "|| com.baeldung.componentscan.filter.aspectj.C*)"))
public class ComponentScanAspectJFilterApp { }
```

虽然有点复杂，但我们这里的逻辑需要在类名中既不以“L”也不以“C”开头的bean，所以这又给我们留下了Elephants：

```java
@Test
public void whenAspectJFilterIsUsed_thenComponentScanShouldRegisterBeanMatchingAspectJCreteria() {
    ApplicationContext applicationContext =
      new AnnotationConfigApplicationContext(ComponentScanAspectJFilterApp.class);
    List<String> beans = Arrays.stream(applicationContext.getBeanDefinitionNames())
      .filter(bean -> !bean.contains("org.springframework")
        && !bean.contains("componentScanAspectJFilterApp"))
      .collect(Collectors.toList());
    assertThat(beans.size(), equalTo(1));
    assertThat(beans.get(0), equalTo("elephant"));
}
```

## FilterType.CUSTOM

如果上述过滤器类型都不符合我们的要求，那么我们还可以创建自定义过滤器类型。例如，假设我们只想扫描名称为五个字符或更短的类。

要创建自定义过滤器，我们需要实现org.springframework.core.type.filter.TypeFilter：参见 componentscan.filter.custom.ComponentScanCustomFilter.java。

让我们使用FilterType.CUSTOM，它使用我们的自定义过滤器ComponentScanCustomFilter将Spring传递给扫描类：

```java
@Configuration
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.CUSTOM,
  classes = ComponentScanCustomFilter.class))
public class ComponentScanCustomFilterApp { }
```

现在是时候看看我们的自定义过滤器ComponentScanCustomFilter的测试用例了：参见 componentscan.filter.custom.ComponentScanCustomFilterAppTest.java。

> **重要**：当 Cat/Elephant 等 Spring POJO 加上 @Component 后将强制扫描，导致 ComponentScanCustomFilter 不起作用。
