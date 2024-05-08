# [春季表达语言指南](https://www.baeldung.com/spring-expression-language)

1. 概述

    Spring 表达式语言（SpEL）是一种功能强大的表达式语言，支持在运行时查询和操作对象图。我们可以将其与 XML 或基于注解的 Spring 配置一起使用。

    该语言有几种可用的操作符：

    | Type        | Operators                                    |
    |-------------|----------------------------------------------|
    | Arithmetic  | +, -, *, /, %, ^, div, mod                   |
    | Relational  | <, >, ==, !=, <=, >=, lt, gt, eq, ne, le, ge |
    | Logical     | and, or, not, &&, \|\|, !                    |
    | Conditional | ?:                                           |
    | Regex       | matches                                      |

2. 操作符

    在这些示例中，我们将使用基于注释的配置。有关 XML 配置的更多详情，请参阅本文后面的章节。

    SpEL 表达式以 # 符号开头，并用大括号封装： `#{expression}`。

    属性可以类似的方式引用，以 $ 符号开头并用大括号封装：

    ```text
    ${property.name}
    ```

    属性占位符不能包含 SpEL 表达式，但表达式可以包含属性引用：

    `#{${someProperty} + 2}`

    在上面的示例中，假设 someProperty 的值为 2，那么生成的表达式将是 2 + 2，计算结果为 4。

    1. 算术运算符

        SpEL 支持所有基本算术运算符：

        ```java
        @Value("#{19 + 1}") // 20
        private double add; 

        @Value("#{'String1 ' + 'string2'}") // "String1 string2"
        private String addString; 

        @Value("#{20 - 1}") // 19
        private double subtract;

        @Value("#{10 * 2}") // 20
        private double multiply;

        @Value("#{36 / 2}") // 19
        private double divide;

        @Value("#{36 div 2}") // 18, the same as for / operator
        private double divideAlphabetic; 

        @Value("#{37 % 10}") // 7
        private double modulo;

        @Value("#{37 mod 10}") // 7, the same as for % operator
        private double moduloAlphabetic; 

        @Value("#{2 ^ 9}") // 512
        private double powerOf;

        @Value("#{(2 + 2) * 2 + 9}") // 17
        private double brackets;
        ```

        除法和模数运算有字母别名，div 表示 /，mod 表示 %。+ 运算符也可用于连接字符串。

    2. 关系运算符和逻辑运算符

        SpEL 还支持所有基本的关系和逻辑运算：

        ```java
        @Value("#{1 == 1}") // true
        private boolean equal;

        @Value("#{1 eq 1}") // true
        private boolean equalAlphabetic;

        @Value("#{1 != 1}") // false
        private boolean notEqual;

        @Value("#{1 ne 1}") // false
        private boolean notEqualAlphabetic;

        @Value("#{1 < 1}") // false
        private boolean lessThan;

        @Value("#{1 lt 1}") // false
        private boolean lessThanAlphabetic;

        @Value("#{1 <= 1}") // true
        private boolean lessThanOrEqual;

        @Value("#{1 le 1}") // true
        private boolean lessThanOrEqualAlphabetic;

        @Value("#{1 > 1}") // false
        private boolean greaterThan;

        @Value("#{1 gt 1}") // false
        private boolean greaterThanAlphabetic;

        @Value("#{1 >= 1}") // true
        private boolean greaterThanOrEqual;

        @Value("#{1 ge 1}") // true
        private boolean greaterThanOrEqualAlphabetic;
        ```

        所有关系运算符都有字母别名。例如，在基于 XML 的配置中，我们不能使用包含角括号（<、<=、>、>=）的运算符。相反，我们可以使用 lt（小于）、le（小于或等于）、gt（大于）或 ge（大于或等于）。

    3. 逻辑操作符

        SpEL 还支持所有基本逻辑运算：

        ```java
        @Value("#{250 > 200 && 200 < 4000}") // true
        private boolean and; 

        @Value("#{250 > 200 and 200 < 4000}") // true
        private boolean andAlphabetic;

        @Value("#{400 > 300 || 150 < 100}") // true
        private boolean or;

        @Value("#{400 > 300 or 150 < 100}") // true
        private boolean orAlphabetic;

        @Value("#{!true}") // false
        private boolean not;

        @Value("#{not true}") // false
        private boolean notAlphabetic;
        ```

        与算术运算符和关系运算符一样，所有逻辑运算符也有字母克隆。

    4. 条件操作符

        我们使用条件操作符来根据某些条件注入不同的值：

        ```java
        @Value("#{2 > 1 ? 'a' : 'b'}") // "a"
        private String ternary;
        ```

        我们使用三元操作符在表达式中执行紧凑的 if-then-else 条件逻辑。在本例中，我们试图检查是否为 true。

        三元操作符的另一个常见用法是检查某个变量是否为空，然后返回变量值或默认值：

        ```java
        @Value("#{someBean.someProperty != null ? someBean.someProperty : 'default'}")
        private String ternary;
        ```

        Elvis 运算符是 Groovy 语言中用于上述情况的三元运算符语法的一种缩写方式。SpEL 中也有这种运算符。

        这段代码等同于上面的代码：

        ```java
        @Value("#{someBean.someProperty ?: 'default'}") // Will inject provided string if someProperty is null
        private String elvis;
        ```

    5. 在 SpEL 中使用 Regex

        我们可以使用 matches 操作符检查字符串是否与给定的正则表达式匹配：

        ```java
        @Value("#{'100' matches '\\d+' }") // true
        private boolean validNumericStringResult;

        @Value("#{'100fghdjf' matches '\\d+' }") // false
        private boolean invalidNumericStringResult;

        @Value("#{'valid alphabetic string' matches '[a-zA-Z\\s]+' }") // true
        private boolean validAlphabeticStringResult;

        @Value("#{'invalid alphabetic string #$1' matches '[a-zA-Z\\s]+' }") // false
        private boolean invalidAlphabeticStringResult;

        @Value("#{someBean.someValue matches '\d+'}") // true if someValue contains only digits
        private boolean validNumericValue;
        ```

    6. 访问列表和地图对象

        借助 SpEL，我们可以访问上下文中任何Map或列表的内容。

        我们将创建一个新的 bean carPark，它将在 List 和 Map 中存储一些汽车及其司机的信息：

        main/.spring.spel.entity/CarPark.java

        现在，我们可以使用 SpEL 访问集合的值：

        ```java
            @Value("#{carPark.carsByDriver['Driver1']}") // Model1
            private Car driver1Car;
            @Value("#{carPark.carsByDriver['Driver2']}") // Model2
            private Car driver2Car;
            @Value("#{carPark.cars[0]}") // Model1
            private Car firstCarInPark;
            @Value("#{carPark.cars.size()}") // Model2
            private Integer numberOfCarsInPark;
        ```

3. 在 Spring 配置中使用

    1. 引用 Bean

        在本示例中，我们将了解如何在基于 XML 的配置中使用 SpEL。我们可以使用表达式来引用 Bean 或 Bean 字段/方法。

        例如，假设我们有以下类：

        ```java
        public class Engine {
            private int capacity;
            private int horsePower;
            private int numberOfCylinders;

        // Getters and setters
        }

        public class Car {
            private String make;
            private int model;
            private Engine engine;
            private int horsePower;

        // Getters and setters
        }
        ```

        现在，我们创建一个应用程序上下文，在其中使用表达式注入值：

        ```xml
        <bean id="engine" class="com.baeldung.spring.spel.Engine">
        <property name="capacity" value="3200"/>
        <property name="horsePower" value="250"/>
        <property name="numberOfCylinders" value="6"/>
        </bean>
        <bean id="someCar" class="com.baeldung.spring.spel.Car">
        <property name="make" value="Some make"/>
        <property name="model" value="Some model"/>
        <property name="engine" value="#{engine}"/>
        <property name="horsePower" value="#{engine.horsePower}"/>
        </bean>
        ```

        请看一下 someCar Bean。someCar 的 engine 和 horsePower 字段使用的表达式分别是对 engine Bean 和 horsePower 字段的 Bean 引用。

        要对基于注解的配置进行同样的操作，请使用 @Value("#{expression}")注解。

    2. 在配置中使用操作符

        本文第一节中的每个操作符都可用于 XML 配置和基于注解的配置。

        不过，请记住，在基于 XML 的配置中，我们不能使用角括号操作符"<"。相反，我们应该使用字母别名，如 lt（小于）或 le（小于或等于）。

        对于基于注释的配置，则没有此类限制：

        ```java
        public class SpelOperators {
            private boolean equal;
            private boolean notEqual;
            private boolean greaterThanOrEqual;
            private boolean and;
            private boolean or;
            private String addString;
            
            // Getters and setters
            @Override
            public String toString() {
                // toString which include all fields
            }
        }
        ```

        现在，我们将在应用程序上下文中添加一个 spelOperators Bean：

        ```xml
        <bean id="spelOperators" class="com.baeldung.spring.spel.SpelOperators">
        <property name="equal" value="#{1 == 1}"/>
        <property name="notEqual" value="#{1 lt 1}"/>
        <property name="greaterThanOrEqual" value="#{someCar.engine.numberOfCylinders >= 6}"/>
        <property name="and" value="#{someCar.horsePower == 250 and someCar.engine.capacity lt 4000}"/>
        <property name="or" value="#{someCar.horsePower > 300 or someCar.engine.capacity > 3000}"/>
        <property name="addString" value="#{someCar.model + ' manufactured by ' + someCar.make}"/>
        </bean>
        ```

        从上下文中读取该 Bean 后，我们就可以验证值是否已正确注入：

        ```java
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        SpelOperators spelOperators = (SpelOperators) context.getBean("spelOperators");
        ```

        在这里，我们可以看到 spelOperators Bean 的 toString 方法的输出结果：

        ```text
        [equal=true, notEqual=false, greaterThanOrEqual=true, and=true, 
        or=true, addString=Some model manufactured by Some make]
        ```

4. 以编程方式解析表达式

    有时，我们可能想在配置上下文之外解析表达式。幸运的是，使用 SpelExpressionParser 可以做到这一点。

    我们可以使用前面示例中的所有运算符，但在使用时应去掉大括号和哈希符号。也就是说，如果我们想在 Spring 配置中使用带有 + 运算符的表达式，语法是 #{1 + 1}；而在配置之外使用时，语法则是 1 + 1。

    在下面的示例中，我们将使用上一节中定义的 Car 和 Engine Bean。

    1. 使用表达式解析器

        让我们来看一个简单的示例：

        ```java
        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("'Any string'");
        String result = (String) expression.getValue();
        ```

        ExpressionParser 负责解析表达式字符串。在本例中，SpEL 解析器将简单地把字符串 "Any String" 作为表达式进行评估。不出所料，结果将是 "Any String"。

        与在配置中使用 SpEL 一样，我们可以用它来调用方法、访问属性或调用构造函数：

        ```java
        Expression expression = expressionParser.parseExpression("'Any string'.length()");
        Integer result = (Integer) expression.getValue();
        ```

        此外，我们可以调用构造函数，而不是直接对字面进行操作：

        `Expression expression = expressionParser.parseExpression("new String('Any string').length()");`

        我们还可以用同样的方法访问 String 类的 bytes 属性，从而得到字符串的 byte[] 表示形式：

        ```java
        Expression expression = expressionParser.parseExpression("'Any string'.bytes");
        byte[] result = (byte[]) expression.getValue();
        ```

        我们可以链式调用方法，就像在普通 Java 代码中一样：

        ```java
        Expression expression = expressionParser.parseExpression("'Any string'.replace(\" \", \"\").length()");
        Integer result = (Integer) expression.getValue();
        ```

        在这种情况下，结果将是 9，因为我们用空字符串替换了空白。

        如果不想对表达式结果进行转换，我们可以使用通用方法 `T getValue(Class<T> desiredResultType)`，在该方法中，我们可以提供希望返回的类的类型。

        请注意，如果返回值无法转换为 desiredResultType，则会抛出 EvaluationException：

        `Integer result = expression.getValue(Integer.class);`

        最常见的用法是提供针对特定对象实例求值的表达式字符串：

        ```java
        Car car = new Car();
        car.setMake("Good manufacturer");
        car.setModel("Model 3");
        car.setYearOfProduction(2014);

        ExpressionParser expressionParser = new SpelExpressionParser();
        Expression expression = expressionParser.parseExpression("model");

        EvaluationContext context = new StandardEvaluationContext(car);
        String result = (String) expression.getValue(context);
        ```

        在这种情况下，结果将等于汽车对象的模型字段 "Model 3" 的值。StandardEvaluationContext 类指定了将针对哪个对象对表达式进行求值。

        创建上下文对象后，就不能再更改它了。StandardEvaluationContext 的构建成本很高，而且在重复使用过程中，它会建立缓存状态，以便更快地执行后续表达式求值。由于存在缓存，因此如果根对象不发生变化，最好尽可能重复使用 StandardEvaluationContext。

        但是，如果根对象反复更改，我们可以使用下面示例中的机制：

        ```java
        Expression expression = expressionParser.parseExpression("model");
        String result = (String) expression.getValue(car);
        ```

        在这里，我们调用 getValue 方法，参数代表我们要应用 SpEL 表达式的对象。

        我们也可以像以前一样使用通用 getValue 方法：

        ```java
        Expression expression = expressionParser.parseExpression("yearOfProduction > 2005");
        boolean result = expression.getValue(car, Boolean.class);
        ```

    2. 使用 ExpressionParser 设置值

        使用解析表达式返回的 Expression 对象上的 setValue 方法，我们可以为对象设置值。SpEL 将负责类型转换。默认情况下，SpEL 使用 org.springframework.core.convert.ConversionService。我们可以创建自己的自定义类型转换器。ConversionService 是泛型感知的，因此我们可以将它与泛型一起使用。

        让我们看看在实践中是如何做到这一点的：

        ```java
        Car car = new Car();
        car.setMake("Good manufacturer");
        car.setModel("Model 3");
        car.setYearOfProduction(2014);

        CarPark carPark = new CarPark();
        carPark.getCars().add(car);

        StandardEvaluationContext context = new StandardEvaluationContext(carPark);

        ExpressionParser expressionParser = new SpelExpressionParser();
        expressionParser.parseExpression("cars[0].model").setValue(context, "Other model");
        ```

        由此生成的汽车对象的车型将从 "Model 3" 改为 "Other model"。

    3. 解析器配置

        在下面的示例中，我们将使用该类：

        ```java
        public class CarPark {
            private List<Car> cars = new ArrayList<>();
            // Getter and setter
        }
        ```

        可以通过调用带有 SpelParserConfiguration 对象的构造函数来配置 ExpressionParser。

        例如，如果我们尝试在未配置解析器的情况下将汽车对象添加到 CarPark 类的汽车数组中，我们将收到如下错误：

        `EL1025E:(pos 4): The collection has '0' elements, index '0' is invalid`

        我们可以改变解析器的行为，允许它在指定索引为空的情况下自动创建元素（autoGrowNullReferences，构造函数的第一个参数），或者自动增长数组或列表以容纳超出其初始大小的元素（autoGrowCollections，第二个参数）：

        ```java
        SpelParserConfiguration config = new SpelParserConfiguration(true, true);
        StandardEvaluationContext context = new StandardEvaluationContext(carPark);

        ExpressionParser expressionParser = new SpelExpressionParser(config);
        expressionParser.parseExpression("cars[0]").setValue(context, car);

        Car result = carPark.getCars().get(0);
        ```

        生成的汽车对象将等同于上一示例中设置为 carPark 对象汽车数组第一个元素的汽车对象。

5. 结论

    SpEL 是一种功能强大、支持良好的表达式语言，我们可以在 Spring 产品组合的所有产品中使用它。我们可以用它来配置 Spring 应用程序，或编写解析器以在任何应用程序中执行更一般的任务。
