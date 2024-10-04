# [Spring的快速指南@Value](https://www.baeldung.com/spring-value-annotation)

1. 一览表

    在这个快速教程中，我们将看看@Value Spring注释。

    此注释可用于将值注入Spring管理bean的字段，并且可以在字段或构造函数/方法参数级别应用。

2. 设置应用程序

    为了描述此注释的不同用途，我们需要配置一个简单的Spring应用程序配置类。

    当然，我们需要一个属性文件来定义我们想要用@Value注释注入的值。因此，我们首先需要在配置类中定义一个@PropertySource——带有属性文件名。

    让我们定义属性文件：

    ```properties
    value.from.file=Value got from the file
    priority=high
    listOfValues=A,B,C
    ```

3. 使用实例

    作为一个基本且大部分无用的例子，我们只能将注释中的“string value”注入字段：

    ```java
    @Value("string value")
    private String stringValue;
    ```

    使用@PropertySource注释使我们能够使用@Valueannotation处理属性文件中的值。

    在以下示例中，我们从分配给字段的文件中获得值：

    ```java
    @Value("${value.from.file}")
    private String valueFromFile;
    ```

    我们还可以用相同的语法从系统属性中设置值。

    让我们假设我们已经定义了一个名为systemValue的系统属性：

    ```java
    @Value("${systemValue}")
    private String systemValue;
    ```

    可以为可能未定义的属性提供默认值。在这里，将注入一些默认值：

    ```java
    @Value("${unknown.param:some default}")
    private String someDefault;
    ```

    如果相同的属性被定义为系统属性并在属性文件中，那么系统属性将被应用。

    假设我们有一个属性优先级，定义为具有系统属性的值，并在属性文件中定义为其他属性。该值将是系统属性：

    ```java
    @Value("${priority}")
    private String prioritySystemProperty;
    ```

    有时，我们需要注入一堆值。将它们定义为属性文件中单个属性的逗号分隔值或系统属性并注入数组会很方便。

    在第一部分中，我们在属性文件的listOfValues中定义了逗号分隔的值，因此数组值将是[“A”、“B”、“C”]：

    ```java
    @Value("${listOfValues}")
    private String[] valuesArray;
    ```

4. 使用SpEL的高级示例

    我们也可以使用SpEL表达式来获取值。

    如果我们有一个名为priority的系统属性，那么其值将应用于字段：

    ```java
    @Value("#{systemProperties['priority']}")
    private String spelValue;
    ```

    如果我们尚未定义系统属性，那么将分配空值。

    为了防止这种情况发生，我们可以在SpEL表达式中提供一个默认值。如果系统属性未定义，我们将获得该字段的一些默认值：

    ```java
    @Value("#{systemProperties['unknown'] ?: 'some default'}")
    private String spelSomeDefault;
    ```

    此外，我们可以使用其他豆类的字段值。假设我们有一个名为someBean的bean，其fieldsomeValue等于10。然后，10将被分配到字段：

    ```java
    @Value("#{someBean.someValue}")
    private Integer someBeanValue;
    ```

    我们可以操作属性来获取值列表，这里是字符串值A、B和C的列表：

    ```java
    @Value("#{'${listOfValues}'.split(',')}")
    private List<String> valuesList;
    ```

5. 将@Value与地图一起使用

    我们还可以使用@Value注释来注入Map属性。

    首先，我们需要在属性文件中以{key: 'value' }形式定义属性：

    `valuesMap={key1: '1', key2: '2', key3: '3'}`

    请注意，地图中的值必须以单引号表示。

    现在，我们可以从属性文件中将此值注入为Map：

    ```java
    @Value("#{${valuesMap}}")
    private Map<String, Integer> valuesMap;
    ```

    如果我们需要在地图中获取特定键的值，我们所要做的就是在表达式中添加键的名称：

    ```java
    @Value("#{${valuesMap}.key1}")
    private Integer valuesMapKey1;
    ```

    如果我们不确定Map是否包含某个键，我们应该选择一个更安全的表达式，该表达式不会抛出异常，但在找不到键时将值设置为空值：

    ```java
    @Value("#{${valuesMap}['unknownKey']}")
    private Integer unknownMapKey;
    ```

    我们还可以为可能不存在的属性或键设置默认值：

    ```java
    @Value("#{${unknownMap : {key1: '1', key2: '2'}}}")
    private Map<String, Integer> unknownMap;

    @Value("#{${valuesMap}['unknownKey'] ?: 5}")
    private Integer unknownMapKeyWithDefaultValue;
    ```

    地图条目也可以在注入前进行过滤。

    让我们假设我们只需要获取那些值大于1的条目：

    ```java
    @Value("#{${valuesMap}.?[value>'1']}")
    private Map<String, Integer> valuesMapFiltered;
    ```

    我们还可以使用@Value注释来注入所有当前系统属性：

    ```java
    @Value("#{systemProperties}")
    private Map<String, String> systemPropertiesMap;
    ```

6. 将@Value与构造函数注入一起使用

    当我们使用@Value注释时，我们不仅限于字段注入。我们也可以将其与构造函数注入一起使用。

    让我们在实践中看看这个：

    ```java
    @Component
    @PropertySource("classpath:values.properties")
    public class PriorityProvider {

        private String priority;

        @Autowired
        public PriorityProvider(@Value("${priority:normal}") String priority) {
            this.priority = priority;
        }

        // standard getter
    }
    ```

    在上述示例中，我们将优先级直接注入到PriorityProvider的构造函数中。

    请注意，如果找不到该属性，我们还提供了一个默认值。

7. 将@Value与设置器注入一起使用

    与构造函数注入类似，我们也可以将@Value与设置器注入一起使用。

    让我们来看看：

    ```java
    @Component
    @PropertySource("classpath:values.properties")
    public class CollectionProvider {

        private List<String> values = new ArrayList<>();

        @Autowired
        public void setValues(@Value("#{'${listOfValues}'.split(',')}") List<String> values) {
            this.values.addAll(values);
        }

        // standard getter
    }
    ```

    我们使用SpEL表达式将值列表注入setValues方法。

8. 将@Value与Records一起使用

    Java 14引入了[records](https://www.baeldung.com/java-record-keyword)，以促进创建不可变类。自版本6.0.6以来，Spring框架支持@Value用于记录注入：

    ```java
    @Component
    @PropertySource("classpath:values.properties")
    public record PriorityRecord(@Value("${priority:normal}") String priority) {}
    ```

    在这里，我们将值直接注入到记录的构造函数中。

9. 结论

    在本文中，我们研究了使用文件中定义的简单属性、系统属性和使用SpEL表达式计算的属性的@Value注释的各种可能性。
