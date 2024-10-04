# [Wiring in Spring：@Autowired、@Resource和@Inject](https://www.baeldung.com/spring-annotations-resource-inject-autowire)

1. 一览表

    在本Spring Framework教程中，我们将演示如何使用与依赖注入相关的注释，即@Resource、@Inject和@Autowired注释。这些注释为类提供了解决依赖项的声明式方法：

    ```java
    @Autowired
    ArbitraryClass arbObject;
    ```

    与直接实例化它们相反（命令方式）：

    `ArbitraryClass arbObject = new ArbitraryClass();`

    三个注释中的两个属于Java扩展包：javax.annotation.Resource和javax.inject.Inject。@Autowired注释属于org.springframework.beans.factory.annotation软件包。

    每个注释都可以通过字段注入或设置器注入来解决依赖关系。我们将使用一个简化但实用的示例来演示三个注释之间的区别，基于每个注释的执行路径。

    这些示例将侧重于如何在集成测试期间使用三个注入注释。测试所需的依赖项可以是任意文件或任意类。

2. @资源注释

    @Resource注释是[JSR-250](https://jcp.org/en/jsr/detail?id=250)注释集合的一部分，并与雅加达EE一起打包。此注释有以下执行路径，按优先级列出：

    - 按Name匹配
    - 按Type匹配
    - 按Qualifier匹配

    这些执行路径适用于设置器和场注入。

    1. 现场注射

        我们可以通过用@Resource注释注释实例变量来通过字段注入来解决依赖关系。

        1. 按名称匹配

            我们将使用以下集成测试来演示按名称匹配字段注入：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestResourceNameType.class)
            public class FieldResourceInjectionIntegrationTest {

                @Resource(name="namedFile")
                private File defaultFile;

                @Test
                public void givenResourceAnnotation_WhenOnField_ThenDependencyValid(){
                    assertNotNull(defaultFile);
                    assertEquals("namedFile.txt", defaultFile.getName());
                }
            }
            ```

            让我们浏览一下代码。在FieldResourceInjectionTest集成测试中，在第7行，我们通过将bean名称作为属性值传递给@Resource注释，按名称解决了依赖性：

            ```java
            @Resource(name="namedFile")
            private File defaultFile;
            ```

            此配置将使用名称匹配执行路径来解析依赖项。我们必须在ApplicationContextTestResourceNameType应用程序上下文中定义beannamedFile。

            请注意，bean id和相应的参考属性值必须匹配：

            ```java
            @Configuration
            public class ApplicationContextTestResourceNameType {

                @Bean(name="namedFile")
                public File namedFile() {
                    File namedFile = new File("namedFile.txt");
                    return namedFile;
                }
            }
            ```

            如果我们未能在应用程序上下文中定义bean，将导致anorg.springframework.beans.factory.NoSuchBeanDefinitionException被抛出。我们可以通过更改ApplicationContextTestResourceNameType应用程序上下文中传递到@Bean注释中的属性值，或更改FieldResourceInjectionTest集成测试中传递到@Resource注释中的属性值来证明这一点。

        2. 按类型匹配

            为了演示按类型匹配的执行路径，我们只需删除FieldResourceInjectionTest集成测试第7行的属性值：

            ```java
            @Resource
            private File defaultFile;
            ```

            然后我们再次运行测试。

            测试仍然会通过，因为如果@Resource注释没有收到bean名称作为属性值，Spring Framework将继续进行下一个级别的优先级，即按类型匹配，以尝试解决依赖关系。

        3. 按资格赛进行匹配

            为了演示匹配限定符执行路径，将修改集成测试场景，以便在ApplicationContextTestResourceQualifier应用程序上下文中定义两个bean：

            ```java
            @Configuration
            public class ApplicationContextTestResourceQualifier {

                @Bean(name="defaultFile")
                public File defaultFile() {
                    File defaultFile = new File("defaultFile.txt");
                    return defaultFile;
                }

                @Bean(name="namedFile")
                public File namedFile() {
                    File namedFile = new File("namedFile.txt");
                    return namedFile;
                }
            }
            ```

            我们将使用QualifierResourceInjectionTest集成测试来演示匹配限定符依赖性解析。在这种情况下，需要将特定的bean依赖性注入到每个参考变量中：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestResourceQualifier.class)
            public class QualifierResourceInjectionIntegrationTest {

                @Resource
                private File dependency1;

                @Resource
                private File dependency2;

                @Test
                public void givenResourceAnnotation_WhenField_ThenDependency1Valid(){
                    assertNotNull(dependency1);
                    assertEquals("defaultFile.txt", dependency1.getName());
                }

                @Test
                public void givenResourceQualifier_WhenField_ThenDependency2Valid(){
                    assertNotNull(dependency2);
                    assertEquals("namedFile.txt", dependency2.getName());
                }
            }
            ```

            当我们运行集成测试时，将抛出org.springframework.beans.factory.NoUniqueBeanDefinitionException。会发生这种情况，因为应用程序上下文将找到文件类型的两个bean定义，并且不知道哪个bean应该解决依赖关系。

            为了解决这个问题，我们需要参考QualifierResourceInjectionTest集成测试的第7行至第10行：

            ```java
            @Resource
            private File dependency1;

            @Resource
            private File dependency2;
            ```

            我们必须添加以下代码行：

            ```java
            @Qualifier("defaultFile")

            @Qualifier("namedFile")
            ```

            因此，代码块如下所示：

            ```java
            @Resource
            @Qualifier("defaultFile")
            private File dependency1;

            @Resource
            @Qualifier("namedFile")
            private File dependency2;
            ```

            当我们再次运行集成测试时，它应该会通过。我们的测试表明，即使我们在应用程序上下文中定义了多个bean，我们也可以使用@Qualifier注释，通过允许我们将特定的依赖项注入类来清除任何混淆。

    2. Setter注射

        在字段上注入依赖项时采取的执行路径也适用于基于设置器的注入。

        1. 按名称匹配

            唯一的区别是MethodResourceInjectionTest集成测试有一个设置方法：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestResourceNameType.class)
            public class MethodResourceInjectionIntegrationTest {

                private File defaultFile;

                @Resource(name="namedFile")
                protected void setDefaultFile(File defaultFile) {
                    this.defaultFile = defaultFile;
                }

                @Test
                public void givenResourceAnnotation_WhenSetter_ThenDependencyValid(){
                    assertNotNull(defaultFile);
                    assertEquals("namedFile.txt", defaultFile.getName());
                }
            }
            ```

            我们通过注释参考变量的相应设置器方法，通过设置器注入来解决依赖关系。然后，我们将bean依赖项的名称作为属性值传递给@Resource注释：

            ```java
            private File defaultFile;

            @Resource(name="namedFile")
            protected void setDefaultFile(File defaultFile) {
                this.defaultFile = defaultFile;
            }
            ```

            在本例中，我们将重复使用namedFile bean依赖项。豆子名称和相应的属性值必须匹配。

            当我们运行集成测试时，它将通过。

            为了验证匹配名称执行路径是否解决了依赖关系，我们需要将传递给@Resource注释的属性值更改为我们选择的值，并再次运行测试。这一次，测试将失败，NoSuchBeanDefinitionException。

        2. 按类型匹配

            为了演示基于设置器的按类型匹配执行，我们将使用MethodByTypeResourceTest集成测试：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestResourceNameType.class)
            public class MethodByTypeResourceIntegrationTest {

                private File defaultFile;

                @Resource
                protected void setDefaultFile(File defaultFile) {
                    this.defaultFile = defaultFile;
                }

                @Test
                public void givenResourceAnnotation_WhenSetter_ThenValidDependency(){
                    assertNotNull(defaultFile);
                    assertEquals("namedFile.txt", defaultFile.getName());
                }
            }
            ```

            当我们运行这个测试时，它会通过。

            为了验证按类型匹配的执行路径是否解决了文件依赖关系，我们需要将默认文件变量的类类型更改为另一个类类型，如String。然后我们可以再次执行MethodByTypeResourceTest集成测试，这次将抛出NoSuchBeanDefinitionException。

            异常验证了是否确实使用了按类型匹配来解决文件依赖项。TheNoSuchBeanDefinitionException确认参考变量名称不需要与bean名称匹配。相反，依赖性解析取决于bean的类类型是否与参考变量的类类型匹配。

        3. 按资格赛进行匹配

            我们将使用MethodByQualifierResourceTest集成测试来演示匹配限定符执行路径：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestResourceQualifier.class)
            public class MethodByQualifierResourceIntegrationTest {

                private File arbDependency;
                private File anotherArbDependency;

                @Test
                public void givenResourceQualifier_WhenSetter_ThenValidDependencies(){
                assertNotNull(arbDependency);
                    assertEquals("namedFile.txt", arbDependency.getName());
                    assertNotNull(anotherArbDependency);
                    assertEquals("defaultFile.txt", anotherArbDependency.getName());
                }

                @Resource
                @Qualifier("namedFile")
                public void setArbDependency(File arbDependency) {
                    this.arbDependency = arbDependency;
                }

                @Resource
                @Qualifier("defaultFile")
                public void setAnotherArbDependency(File anotherArbDependency) {
                    this.anotherArbDependency = anotherArbDependency;
                }
            }
            ```

            我们的测试表明，即使我们在应用程序上下文中定义了特定类型的多个bean实现，我们也可以使用@Qualifier注释与@Resource注释来解决依赖关系。

            与基于字段的依赖注入类似，如果我们在应用程序上下文中定义多个bean，我们必须使用@Qualifier注释来指定使用哪个bean来解决依赖项，否则将抛出aNoUniqueBeanDefinitionException。

3. @Inject注释

    @Inject注释属于[JSR-330](https://jcp.org/en/jsr/detail?id=330)注释集合。此注释有以下执行路径，按优先级列出：

    - 按Type匹配
    - 按Qualifier匹配
    - 按Name匹配

    这些执行路径适用于设置器和场注入。为了访问@Inject注释，我们必须将javax.inject库声明为Gradle或Maven依赖项。

    对于Gradle：

    `testCompile group: 'javax.inject', name: 'javax.inject', version: '1'`

    对于Maven：

    ```xml
    <dependency>
        <groupId>javax.inject</groupId>
        <artifactId>javax.inject</artifactId>
        <version>1</version>
    </dependency>
    ```

    1. 现场注射

        1. 按类型匹配

            我们将修改集成测试示例，以使用另一种类型的依赖项，即任意依赖类。任意依赖类依赖只是一个简单的依赖，没有进一步的意义：

            ```java
            @Component
            public class ArbitraryDependency {

                private final String label = "Arbitrary Dependency";

                public String toString() {
                    return label;
                }
            }
            ```

            这是有问题的FieldInjectTest集成测试：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestInjectType.class)
            public class FieldInjectIntegrationTest {

                @Inject
                private ArbitraryDependency fieldInjectDependency;

                @Test
                public void givenInjectAnnotation_WhenOnField_ThenValidDependency(){
                    assertNotNull(fieldInjectDependency);
                    assertEquals("Arbitrary Dependency",
                    fieldInjectDependency.toString());
                }
            }
            ```

            与首先按名称解析依赖项的@Resource注释不同，@Inject注释的默认行为是按类型解析依赖项。

            这意味着，即使类引用变量名称与bean名称不同，只要bean在应用程序上下文中定义，依赖关系仍将得到解决。注意以下测试中的参考变量名称：

            ```java
            @Inject
            private ArbitraryDependency fieldInjectDependency;
            ```

            与应用程序上下文中配置的bean名称不同：

            ```java
            @Bean
            public ArbitraryDependency injectDependency() {
                ArbitraryDependency injectDependency = new ArbitraryDependency();
                return injectDependency;
            }
            ```

            当我们执行测试时，我们能够解决依赖性。

        2. 按资格赛进行匹配

            如果特定类类型有多个实现，而某个类需要特定的bean怎么办？让我们修改集成测试示例，使其需要另一个依赖项。

            在本例中，我们对ArbitraryDependency类进行子类，该类在匹配示例中使用，以创建AnotherArbitraryDependency类：

            ```java
            public class AnotherArbitraryDependency extends ArbitraryDependency {

                private final String label = "Another Arbitrary Dependency";

                public String toString() {
                    return label;
                }
            }
            ```

            每个测试用例的目标是确保我们将每个依赖项正确地注入到每个参考变量中：

            ```java
            @Inject
            private ArbitraryDependency defaultDependency;

            @Inject
            private ArbitraryDependency namedDependency;
            ```

            我们可以使用FieldQualifierInjectTest集成测试来演示限定符的匹配：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestInjectQualifier.class)
            public class FieldQualifierInjectIntegrationTest {

                @Inject
                private ArbitraryDependency defaultDependency;

                @Inject
                private ArbitraryDependency namedDependency;

                @Test
                public void givenInjectQualifier_WhenOnField_ThenDefaultFileValid(){
                    assertNotNull(defaultDependency);
                    assertEquals("Arbitrary Dependency",
                    defaultDependency.toString());
                }

                @Test
                public void givenInjectQualifier_WhenOnField_ThenNamedFileValid(){
                    assertNotNull(defaultDependency);
                    assertEquals("Another Arbitrary Dependency",
                    namedDependency.toString());
                }
            }
            ```

            如果我们在应用程序上下文中对特定类有多个实现，并且FieldQualifierInjectTest集成测试试图以下面列出的方式注入依赖项，则将抛出NoUniqueBeanDefinitionException：

            ```java
            @Inject
            private ArbitraryDependency defaultDependency;

            @Inject
            private ArbitraryDependency namedDependency;
            ```

            抛出这个异常是Spring Framework指出某个类有多个实现的方式，它对使用哪一个感到困惑。为了阐明困惑，我们可以转到FieldQualifierInjectTest集成测试的第7行和第10行：

            ```java
            @Inject
            private ArbitraryDependency defaultDependency;

            @Inject
            private ArbitraryDependency namedDependency;
            ```

            我们可以将所需的bean名称传递给@Qualifier注释，我们将其与@Inject注释一起使用。这就是代码块现在的样子：

            ```java
            @Inject
            @Qualifier("defaultFile")
            private ArbitraryDependency defaultDependency;

            @Inject
            @Qualifier("namedFile")
            private ArbitraryDependency namedDependency;
            ```

            在接收bean名称时，@Qualifier注释期望严格匹配。我们必须确保将bean名称正确传递给限定符，否则，将抛出NoUniqueBeanDefinitionException。如果我们再次运行测试，它应该会通过。

        3. 按名称匹配

            用于按名称显示匹配的FieldByNameInjectTest集成测试与按类型执行路径匹配类似。唯一的区别是，现在我们需要一种特定的豆子，而不是一种特定的类型。在本例中，我们再次对ArbitraryDependency类进行子类，以生成YetAnotherArbitraryDependency类：

            ```java
            public class YetAnotherArbitraryDependency extends ArbitraryDependency {

                private final String label = "Yet Another Arbitrary Dependency";

                public String toString() {
                    return label;
                }
            }
            ```

            为了演示按名称匹配的执行路径，我们将使用以下集成测试：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestInjectName.class)
            public class FieldByNameInjectIntegrationTest {

                @Inject
                @Named("yetAnotherFieldInjectDependency")
                private ArbitraryDependency yetAnotherFieldInjectDependency;

                @Test
                public void givenInjectQualifier_WhenSetOnField_ThenDependencyValid(){
                    assertNotNull(yetAnotherFieldInjectDependency);
                    assertEquals("Yet Another Arbitrary Dependency",
                    yetAnotherFieldInjectDependency.toString());
                }
            }
            ```

            我们列出了应用程序上下文：

            ```java
            @Configuration
            public class ApplicationContextTestInjectName {

                @Bean
                public ArbitraryDependency yetAnotherFieldInjectDependency() {
                    ArbitraryDependency yetAnotherFieldInjectDependency =
                    new YetAnotherArbitraryDependency();
                    return yetAnotherFieldInjectDependency;
                }
            }
            ```

            如果我们运行集成测试，它就会通过。

            为了验证我们是否通过名称匹配执行路径注入了依赖项，我们需要将传递到@Named注释的值，yetAnotherFieldInjectDependency更改为我们选择的另一个名称。当我们再次运行测试时，将抛出NoSuchBeanDefinitionException。

    2. Setter注射

        @Inject注释的基于设置器的注入与@Resource基于设置器的注入的方法相似。我们没有注释参考变量，而是注释相应的设置器方法。基于字段的依赖注入之后的执行路径也适用于基于设置器的注入。

4. @Autowired注释

    @Autowired注释的行为与@Inject注释相似。唯一的区别是@Autowired注释是Spring框架的一部分。此注释的执行路径与@Inject注释相同，按优先级排列：

    - 按Type匹配
    - 按Qualifier匹配
    - 按Name匹配

    这些执行路径适用于设置器和场注入。

    1. 现场注射

        1. 按类型匹配

            用于演示@Autowired匹配按类型执行路径的集成测试示例将与用于演示@Inject匹配按类型执行路径的测试相似。我们使用以下FieldAutowiredTest集成测试来演示使用@Autowired注释的按类型匹配：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestAutowiredType.class)
            public class FieldAutowiredIntegrationTest {

                @Autowired
                private ArbitraryDependency fieldDependency;

                @Test
                public void givenAutowired_WhenSetOnField_ThenDependencyResolved() {
                    assertNotNull(fieldDependency);
                    assertEquals("Arbitrary Dependency", fieldDependency.toString());
                }
            }
            ```

            我们列出了此集成测试的应用程序上下文：

            ```java
            @Configuration
            public class ApplicationContextTestAutowiredType {

                @Bean
                public ArbitraryDependency autowiredFieldDependency() {
                    ArbitraryDependency autowiredFieldDependency =
                    new ArbitraryDependency();
                    return autowiredFieldDependency;
                }
            }
            ```

            我们使用此集成测试来证明按类型匹配优先于其他执行路径。注意FieldAutowiredTest集成测试第8行的参考变量名称：

            ```java
            @Autowired
            private ArbitraryDependency fieldDependency;
            ```

            这与应用程序上下文中的bean名称不同：

            ```java
            @Bean
            public ArbitraryDependency autowiredFieldDependency() {
                ArbitraryDependency autowiredFieldDependency =
                new ArbitraryDependency();
                return autowiredFieldDependency;
            }
            ```

            当我们运行测试时，它应该会通过。

            为了确认依赖确实使用按类型匹配执行路径解决了，我们需要更改fieldDependency引用变量的类型，并再次运行集成测试。这一次，FieldAutowiredTest集成测试将失败，并抛出NoSuchBeanDefinitionException。这验证了我们使用匹配类型来解决依赖关系。

        2. 按资格赛进行匹配

            如果我们面临在应用程序上下文中定义了多个bean实现的情况：

            ```java
            @Configuration
            public class ApplicationContextTestAutowiredQualifier {

                @Bean
                public ArbitraryDependency autowiredFieldDependency() {
                    ArbitraryDependency autowiredFieldDependency =
                    new ArbitraryDependency();
                    return autowiredFieldDependency;
                }

                @Bean
                public ArbitraryDependency anotherAutowiredFieldDependency() {
                    ArbitraryDependency anotherAutowiredFieldDependency =
                    new AnotherArbitraryDependency();
                    return anotherAutowiredFieldDependency;
                }
            }
            ```

            如果我们执行以下FieldQualifierAutowiredTest集成测试，将抛出NoUniqueBeanDefinitionException：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestAutowiredQualifier.class)
            public class FieldQualifierAutowiredIntegrationTest {

                @Autowired
                private ArbitraryDependency fieldDependency1;

                @Autowired
                private ArbitraryDependency fieldDependency2;

                @Test
                public void givenAutowiredQualifier_WhenOnField_ThenDep1Valid(){
                    assertNotNull(fieldDependency1);
                    assertEquals("Arbitrary Dependency", fieldDependency1.toString());
                }

                @Test
                public void givenAutowiredQualifier_WhenOnField_ThenDep2Valid(){
                    assertNotNull(fieldDependency2);
                    assertEquals("Another Arbitrary Dependency",
                    fieldDependency2.toString());
                }
            }
            ```

            例外是由于应用程序上下文中定义的两个bean引起的歧义。Spring Framework不知道哪个bean依赖项应该自动连接到哪个引用变量。我们可以通过将@Qualifier注释添加到FieldQualifierAutowiredTest集成测试的第7行和第10行来解决这个问题：

            ```java
            @Autowired
            private FieldDependency fieldDependency1;

            @Autowired
            private FieldDependency fieldDependency2;
            ```

            因此，代码块如下所示：

            ```java
            @Autowired
            @Qualifier("autowiredFieldDependency")
            private FieldDependency fieldDependency1;

            @Autowired
            @Qualifier("anotherAutowiredFieldDependency")
            private FieldDependency fieldDependency2;
            ```

            当我们再次运行测试时，它会通过。

        3. 按名称匹配

            我们将使用相同的集成测试场景来演示使用@Autowired注释来注入字段依赖关系的名称匹配执行路径。按名称自动连接依赖项时，必须将@ComponentScan注释与应用程序上下文一起使用，ApplicationContextTestAutowiredName：

            ```java
            @Configuration
            @ComponentScan(basePackages={"com.baeldung.dependency"})
                public class ApplicationContextTestAutowiredName {
            }
            ```

            我们使用@ComponentScan注释来搜索已注释为@Component注释的Java类的软件包。例如，在应用程序上下文中，com.baeldung.dependency软件包将被扫描为已注释为@Component注释的类。在这种情况下，Spring Framework必须检测具有@Component注释的ArbitraryDependency类：

            ```java
            @Component(value="autowiredFieldDependency")
            public class ArbitraryDependency {

                private final String label = "Arbitrary Dependency";

                public String toString() {
                    return label;
                }
            }
            ```

            属性值autowiredFieldDependency传递到@Component注释中，告诉Spring Framework，ArbitraryDependency类是一个名为autowiredFieldDependency的组件。为了使@Autowired注释按名称解析依赖项，组件名称必须与FieldAutowiredNameTest集成测试中定义的字段名称相对应；请参阅第8行：

            ```java
            @RunWith(SpringJUnit4ClassRunner.class)
            @ContextConfiguration(
            loader=AnnotationConfigContextLoader.class,
            classes=ApplicationContextTestAutowiredName.class)
            public class FieldAutowiredNameIntegrationTest {

                @Autowired
                private ArbitraryDependency autowiredFieldDependency;

                @Test
                public void givenAutowired_WhenSetOnField_ThenDependencyResolved(){
                    assertNotNull(autowiredFieldDependency);
                    assertEquals("Arbitrary Dependency",
                    autowiredFieldDependency.toString());
                }
            }
            ```

            当我们运行FieldAutowiredNameTest集成测试时，它将通过。

            但我们怎么知道@Autowired注释确实调用了按名称匹配的执行路径？我们可以将引用变量autowiredFieldDependency的名称更改为我们选择的另一个名称，然后再次运行测试。

            这一次，测试将失败，并抛出NoUniqueBeanDefinitionException。类似的检查是将@Component属性值autowiredFieldDependency更改为我们选择的另一个值，并再次运行测试。ANoUniqueBeanDefinitionException也将被抛出。

            这个例外证明，如果我们使用错误的bean名称，将不会找到有效的bean。这就是我们如何知道按名称匹配的执行路径被调用。

    2. Setter注射

        @Autowired注释的基于设置器的注入与@Resource基于设置器的注入演示的方法相似。我们没有用@Inject注释注释参考变量，而是注释相应的设置器。执行路径后跟基于字段的依赖注入也适用于基于设置器的注入。

5. 应用这些注释

    这提出了应该使用哪种注释以及在什么情况下的问题。这些问题的答案取决于相关应用程序面临的设计场景，以及开发人员希望如何根据每个注释的默认执行路径利用多态性。

    1. 通过多态性对单例的应用

        如果设计是应用程序行为基于接口或抽象类的实现，并且这些行为在整个应用程序中使用，那么我们可以使用@Inject或@Autowired注释。

        这种方法的好处是，当我们升级应用程序或应用补丁来修复错误时，可以交换类，对整体应用程序行为的负面影响最小。在这种情况下，主要的默认执行路径是按类型匹配的。

    2. 通过多态性进行精细的应用程序行为配置

        如果设计使应用程序具有复杂的行为，每个行为都基于不同的接口/抽象类，并且每个实现的使用方式在应用程序中有所不同，那么我们可以使用@Resource注释。在这种情况下，主要的默认执行路径是按名称匹配的。

    3. 依赖注入应完全由雅加达EE平台处理

        如果雅加达EE平台对所有依赖项有设计授权，而不是Spring，那么在@Resource注释和@Inject注释之间进行选择。我们应该根据需要哪个默认执行路径来缩小两个注释之间的最终决定。

    4. 依赖注入应仅由Spring框架处理

        如果授权是所有依赖项由Spring框架处理，唯一的选择是@Autowired注释。

    5. 讨论总结

        下表总结了我们的讨论。

        | 场景                      | @Resource | @Inject | @Autowired |
        |-------------------------|-----------|---------|------------|
        | 通过多态性在整个应用程序中使用单子       | ✗         | ✔       | ✔          |
        | 通过多态实现细粒度的应用程序行为配置      | ✔         | ✗       | ✗          |
        | 依赖注入应仅由 Jakarta EE 平台处理 | ✔         | ✔       | ✗          |
        | 依赖注入应完全由 Spring 框架处理    | ✗         | ✗       | ✔          |

6. 结论

    在本文中，我们旨在更深入地了解每个注释的行为。了解每个注释的行为方式将有助于更好的整体应用程序设计和维护。
