# Drools

## Drools简介

1. 概述

    [Drools](https://www.drools.org/)是一个业务规则管理系统（BRMS）解决方案。它提供了一个处理事实的规则引擎，并根据规则和事实的处理结果来产生输出。业务逻辑的集中化使得快速而廉价的引入变化成为可能。

    Drools提供了一个核心的业务规则引擎（BRE），一个网络创作和规则管理应用程序（Drools Workbench），在一致性等级3下对决策模型和符号（[DMN](https://www.drools.org/learn/dmn.html)）模型的完全运行支持，以及一个用于核心开发的Eclipse IDE插件。

    它还提供了一个以易于理解的格式编写规则的设施，从而弥合了业务和技术团队之间的差距。

2. Maven的依赖性

    要开始使用Drools，我们首先需要在pom.xml中添加几个依赖项：

    ```xml
    <dependency>
        <groupId>org.kie</groupId>
        <artifactId>kie-ci</artifactId>
        <version>8.40.0.Final</version>
    </dependency>
    <dependency>
        <groupId>org.drools</groupId>
        <artifactId>drools-decisiontables</artifactId>
        <version>8.40.0.Final</version>
    </dependency>
    ```

    这两个依赖的最新版本可在Maven中央仓库中以kie-ci和drools-decisiontables的形式获得。

3. Drools基础知识

    我们来看看Drools的基本概念：

    - Facts - 代表数据，作为规则的输入。
    - 工作记忆（Working Memory）- 是一个存储事实的地方，它们被用于模式匹配，可以被修改、插入和删除。
    - 规则（Rule）- 代表一个单一的规则，将Facts与匹配动作联系起来。它可以用Drools规则语言写成.drl文件，也可以用EXCEL电子表格中的决策表来写。
    - 知识会话(Knowledge Session) - 它保存了所有启动规则所需的资源；所有的事实被插入会话中，然后匹配的规则被启动。
    - 知识库(Knowledge Base) - 代表了Drools生态系统中的知识，它有关于找到规则的资源的信息，同时它也创建了知识会话。
    - 模块(Module) - 一个模块拥有多个知识库，可以容纳不同的会话。
4. Java配置

    为了在给定的数据上启动规则，我们需要实例化框架提供的类，并提供关于规则文件和事实的位置信息：

    1. KieFileSystem

        首先，我们需要设置KieFileSystem bean；这是一个由框架提供的内存文件系统。下面的代码提供了一个容器，以编程方式定义Drools资源，如规则文件，决策表：

        ```java
        public KieFileSystem kieFileSystem() throws IOException {
            KieFileSystem kieFileSystem = getKieServices().newKieFileSystem();
                for (Resource file : getRuleFiles()) {
                    kieFileSystem.write(
                    ResourceFactory.newClassPathResource(
                    RULES_PATH + file.getFilename(), "UTF-8"));
                }
                return kieFileSystem;
        }
        ```

        这里 RULES_PATH 表示文件系统中规则文件的位置。这里我们从classpath读取文件，如果是Maven项目，通常是/src/main/resources。

    2. KieContainer

        接下来，我们需要设置KieContainer，它是特定KieModule的所有KieBases的占位符。KieContainer是在其他bean的帮助下建立的，包括KieFileSystem、KieModule和KieBuilde。

        在KieBuilder上调用的buildAll()方法构建了所有的资源，并将它们与KieBase绑定。只有当它能够找到并验证所有的规则文件时，它才会成功执行：

        ```java
        ppublic KieContainer kieContainer() throws IOException {
            KieRepository kieRepository = getKieServices().getRepository();
            kieRepository.addKieModule(new KieModule() {
                public ReleaseId getReleaseId() {
                    return kieRepository.getDefaultReleaseId();
                }
            });
            KieBuilder kieBuilder = getKieServices()
            .newKieBuilder(kieFileSystem())
            .buildAll();
            return getKieServices().newKieContainer(kieRepository.getDefaultReleaseId());
        }
        ```

    3. KieSession

        通过打开一个KieSession Bean来触发规则--它可以从KieContainer中检索：

        ```java
        public KieSession kieSession() throws IOException {
            return kieContainer().newKieSession();
        }
        ```

5. 实现规则

    现在我们已经完成了设置，让我们看一下创建规则的几个选项。

    我们将通过一个例子来探讨规则的实施，即根据申请人目前的工资和他所拥有的经验年限，对其进行分类，以获得一个特定的角色。

    1. Drools规则文件（.drl）

        简单地说，Drools规则文件包含所有的业务规则。

        一个规则包括一个When-Then结构，这里的When部分列出了要检查的条件，Then部分列出了满足条件后要采取的行动：

        ```drl
        package com.baeldung.drools.rules;
        import com.baeldung.drools.model.Applicant;
        global com.baeldung.drools.model.SuggestedRole suggestedRole;
        dialect  "mvel"
        rule "Suggest Manager Role"
            when
                Applicant(experienceInYears > 10)
                Applicant(currentSalary > 1000000 && currentSalary <= 
                2500000)
            then
                suggestedRole.setRole("Manager");
        end
        ```

        这个规则可以通过在KieSession中插入Applicant和SuggestedRole事实来启动：

        ```java
        public SuggestedRole suggestARoleForApplicant(
            Applicant applicant,SuggestedRole suggestedRole){
            KieSession kieSession = kieContainer.newKieSession();
            kieSession.insert(applicant);
            kieSession.setGlobal("suggestedRole",suggestedRole);
            kieSession.fireAllRules();
            // ...
        }
        ```

        它在申请人实例上测试了两个条件，然后基于这两个条件的满足，它在SuggestedRole对象中设置了Role字段。

        这可以通过执行测试来验证：

        ```java
        @Test
        public void whenCriteriaMatching_ThenSuggestManagerRole(){
            Applicant applicant = new Applicant("David", 37, 1600000.0,11);
            SuggestedRole suggestedRole = new SuggestedRole();   
            applicantService.suggestARoleForApplicant(applicant, suggestedRole);
            assertEquals("Manager", suggestedRole.getRole());
        }
        ```

        在这个例子中，我们使用了一些Drools提供的关键字。让我们了解一下它们的用途：

        - package - 这是我们在kmodule.xml中指定的包名，规则文件位于这个包内。
        - import - 这类似于Java的import语句，在这里我们需要指定我们要插入到KnowledgeSession中的类。
        - global - 这是用来为会话定义一个全局级别的变量；它可以用来传递输入参数或获得输出参数以总结会话的信息。
        - dialect（方言） - 方言指定了在条件部分或动作部分的表达式中使用的语法。默认情况下，方言是Java。Drools也支持方言mvel；它是一种基于Java的应用程序的表达式语言。它支持字段和方法/获取器的访问。
        - rule - 这定义了一个有规则名称的规则块。
        - when - 这指定了一个规则条件，在这个例子中，被检查的条件是申请人的经验年限超过10年，并且当前工资在一定范围内。
        - then - 这个块在满足when块中的条件时执行动作。在这个例子中，申请人的角色被设置为经理。
    2. 决策表

        决策表提供了在一个预先格式化的Excel表格中定义规则的能力。Drools提供的决策表的优势在于，即使是非技术人员，也很容易理解。

        另外，当有类似的规则，但有不同的值时，它是非常有用的，在这种情况下，在Excel表格上添加新的行，比在.drl文件中写新的规则要容易得多。让我们看看决策表的结构，以根据产品类型为产品贴标签为例：

        [Screen-Shot-2017-04-26-at-12.26.59-PM-2](/pic/Screen-Shot-2017-04-26-at-12.26.59-PM-2.png)

        决策表分为不同的部分，最上面的部分就像一个标题部分，我们在这里指定RuleSet（即规则文件所在的包）、Import（要导入的Java类）和Notes（关于规则的目的的注释）。

        我们定义规则的中心部分被称为RuleTable，它将应用于同一领域对象的规则分组。

        在下一行，我们有列类型 CONDITION 和 ACTION。在这些列中，我们可以访问某一行中提到的域对象的属性，以及它们在后续行中的值。

        启动规则的机制与我们看到的.drl文件类似。

        我们可以通过执行测试来验证应用这些规则的结果：

        ```java
        @Test
        public void whenProductTypeElectronic_ThenLabelBarcode() {
            Product product = new Product("Microwave", "Electronic");
            product = productService.applyLabelToProduct(product);
            assertEquals("BarCode", product.getLabel());
        }
        ```

6. 总结

    在这篇文章中，我们已经探讨了在我们的应用程序中使用Drools作为业务规则引擎。我们也看到了多种方法，我们可以用Drools的规则语言来编写规则，也可以用容易理解的电子表格语言。

## Drools中后向链的一个例子

1. 概述

    在这篇文章中，我们将看到什么是后向链，以及我们如何在Drools中使用它。

    这篇文章是展示Drools商业规则引擎系列的一部分。

2. Maven的依赖性

    让我们从导入drools-core依赖项开始：

    ```xml
    <dependency>
        <groupId>org.drools</groupId>
        <artifactId>drools-core</artifactId>
        <version>8.40.0.Final</version>
    </dependency>
    ```

3. 前向链

    首先，在前向链中，我们从分析数据开始，并朝着一个特定的结论前进。

    应用前向链的一个例子是，一个通过检查节点之间已经知道的连接来发现新路线的系统。

4. 后向链式

    与前向链法相反，后向链法直接从结论（假设）开始，通过对一连串事实的回溯来验证它。

    当比较前向连锁和后向连锁时，前者可以被描述为 "数据驱动"（数据作为输入），而后者可以被描述为 "事件（或目标）驱动"（目标作为输入）。

    应用后向链的一个例子是验证是否有一条连接两个节点的路线。

5. Drools后向链

    Drools项目主要是作为一个前向链式系统创建的。但是，从5.2.0版本开始，它也支持后向链。

    让我们创建一个简单的应用程序，并尝试验证一个简单的假设--中国的长城是否在地球上。

    1. 数据

        让我们创建一个简单的事实库，描述事物和它的位置：

        - 地球
        - 亚洲，地球
        - 中国，亚洲
        - 中国的长城，中国
    2. 定义规则

        现在，让我们创建一个名为BackwardChaining.drl的".drl"文件，我们将把它放在/resources/com/baeldung/drools/rules/。这个文件将包含所有必要的查询和规则，将在这个例子中使用。

        主要的 belongsTo 查询，将利用后向链，可以写成：

        ```drl
        query belongsTo(String x, String y)
            Fact(x, y;)
            or
            (Fact(z, y;) and belongsTo(x, z;))
        end
        ```

        此外，让我们添加两条规则，这将使我们能够轻松地审查我们的结果：

        ```drl
        rule "Great Wall of China BELONGS TO Planet Earth"
        when
            belongsTo("Great Wall of China", "Planet Earth";)
        then
            result.setValue("Decision one taken: Great Wall of China BELONGS TO Planet Earth");
        end

        rule "print all facts"
        when
            belongsTo(element, place;)
        then
            result.addFact(element + " IS ELEMENT OF " + place);
        end
        ```

    3. 创建应用程序

        现在，我们需要一个Java类来表示事实：

        ```java
        public class Fact {
            @Position(0)
            private String element;
            @Position(1)
            private String place;
            // getters, setters, constructors, and other methods ...    
        }
        ```

        这里我们使用@Position注解来告诉应用程序，Drools将以何种顺序为这些属性提供值。

        同时，我们将创建代表结果的POJO：

        ```java
        public class Result {
            private String value;
            private List<String> facts = new ArrayList<>();
            //... getters, setters, constructors, and other methods
        }
        ```

        现在，我们可以运行这个例子了：

        ```java
        public class BackwardChainingTest {

            @Before
            public void before() {
                result = new Result();
                ksession = new DroolsBeanFactory().getKieSession();
            }

            @Test
            public void whenWallOfChinaIsGiven_ThenItBelongsToPlanetEarth() {
                ksession.setGlobal("result", result);
                ksession.insert(new Fact("Asia", "Planet Earth"));
                ksession.insert(new Fact("China", "Asia"));
                ksession.insert(new Fact("Great Wall of China", "China"));
                ksession.fireAllRules();
                assertEquals(
                result.getValue(),
                "Decision one taken: Great Wall of China BELONGS TO Planet Earth");
            }
        }
        ```

        当测试用例被执行时，它们会添加给定的事实（"亚洲属于地球"，"中国属于亚洲"，"中国的长城属于中国"）。

        之后，用BackwardChaining.drl中描述的规则处理这些事实，它提供了一个递归查询 belongsTo(String x, String y)。

        这个查询被使用后向链的规则所调用，以发现假设（"中国长城属于地球"）是真还是假。

6. 结论

    我们已经展示了后向链的概况，这是Drools的一个功能，用于检索事实列表以验证一个决定是否真实。

## Drools使用Excel文件中的规则

1. 概述

    Drools支持以电子表格的形式管理业务规则。

    在这篇文章中，我们将看到一个使用Drools来管理Excel文件的业务规则的快速例子。

2. 在Excel中定义规则

    对于我们的例子，让我们定义规则，根据客户类型和作为客户的年限来确定折扣：

    - 大于3年的个人客户获得15%的折扣
    - 少于3年的个人客户获得5%的折扣
    - 所有企业客户获得20%的折扣
    1. Excel文件

        让我们开始按照Drools要求的特定结构和关键词来创建我们的[Excel文件](https://github.com/eugenp/tutorials/blob/master/drools/src/main/resources/com/baeldung/drools/rules/Discount.drl.xls)：

        对于我们这个简单的例子，我们使用了最相关的一组关键词：

        - RuleSet - 表示决策表的开始
        - Import - 规则中使用的Java类
        - RuleTable - 表示规则集的开始
        - Name--规则的名称
        - CONDITION - 要对输入数据进行检查的条件的代码片断。一个规则应该至少包含一个条件
        - ACTION - 如果规则的条件得到满足，将采取的行动的代码片段。一条规则应该至少包含一个动作。在这个例子中，我们在客户对象上调用setDiscount。
        此外，我们在Excel文件中使用了客户类。所以，现在让我们来创建这个。

    2. 客户类

        从Excel文件中的条件和行动可以看出，我们使用Customer类的一个对象来输入数据（类型和年份）并存储结果（折扣）。

        客户类：

        ```java
        public class Customer {
            private CustomerType type;
            private int years;
            private int discount;
            // Standard getters and setters
            public enum CustomerType {
                INDIVIDUAL,
                BUSINESS;
            }
        }
        ```

3. 创建Drools规则引擎实例

    在我们执行我们所定义的规则之前，我们必须使用Drools规则引擎的实例。为此，我们必须使用Kie核心组件。

    1. KieServices

        KieServices类提供了对所有Kie构建和运行时设施的访问。它提供了几个工厂、服务和实用方法。所以，让我们首先掌握一个KieServices实例：

        `KieServices kieServices = KieServices.Factory.get();`

        使用KieServices，我们将创建KieFileSystem、KieBuilder和KieContainer的新实例。

    2. KieFileSystem

        KieFileSystem是一个虚拟文件系统。让我们将我们的 Excel 电子表格添加到其中：

        ```java
        Resource dt 
        = ResourceFactory
            .newClassPathResource("com/baeldung/drools/rules/Discount.drl.xls",
            getClass());
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(dt);
        ```

    3. KieBuilder

        现在，通过将KieFileSystem传递给KieBuilder来构建它的内容：

        ```java
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        ```

        如果成功构建，就会创建一个KieModule（任何Maven生产的jar中带有kmodule.xml的就是KieModule）。

    4. KieRepository

        框架会自动将KieModule（构建后产生的）加入KieRepository：

        `KieRepository kieRepository = kieServices.getRepository();`

    5. KieContainer

        现在可以用这个KieModule使用其ReleaseId创建一个新的KieContainer。在这种情况下，Kie分配了一个默认的ReleaseId：

        ```java
        ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
        KieContainer kieContainer 
        = kieServices.newKieContainer(krDefaultReleaseId);
        ```

    6. KieSession

        我们现在可以从KieContainer获得KieSession。我们的应用程序与KieSession进行交互，KieSession存储并执行运行时数据：

        `KieSession kieSession = kieContainer.newKieSession();`

4. 执行规则

    最后，是时候提供输入数据并启动规则了：

    ```java
    Customer customer = new Customer(CustomerType.BUSINESS, 2);
    kieSession.insert(customer);
    kieSession.fireAllRules();
    ```

5. 测试案例

    现在让我们添加一些测试案例：

    ```java
    public class DiscountExcelIntegrationTest {

        private KieSession kSession;

        @Before
        public void setup() {
            Resource dt 
            = ResourceFactory
                .newClassPathResource("com/baeldung/drools/rules/Discount.drl.xls",
                getClass());
            kSession = new DroolsBeanFactory().getKieSession(dt);
        }

        @Test
        public void 
        giveIndvidualLongStanding_whenFireRule_thenCorrectDiscount() 
            throws Exception {
            Customer customer = new Customer(CustomerType.INDIVIDUAL, 5);
            kSession.insert(customer);

            kSession.fireAllRules();

            assertEquals(customer.getDiscount(), 15);
        }

        @Test
        public void 
        giveIndvidualRecent_whenFireRule_thenCorrectDiscount() 
        throws Exception {
            Customer customer = new Customer(CustomerType.INDIVIDUAL, 1);
            kSession.insert(customer);

            kSession.fireAllRules();

            assertEquals(customer.getDiscount(), 5);
        }

        @Test
        public void 
        giveBusinessAny_whenFireRule_thenCorrectDiscount() 
            throws Exception {
            Customer customer = new Customer(CustomerType.BUSINESS, 0);
            kSession.insert(customer);

            kSession.fireAllRules();

            assertEquals(customer.getDiscount(), 20);
        }
    }
    ```

6. 疑难解答

    Drools将决策表转换为DRL。由于这个原因，处理Excel文件中的错误和错别字会很困难。通常这些错误是指DRL的内容。因此，为了排除故障，打印和分析DRL是有帮助的：

    ```java
    Resource dt 
    = ResourceFactory
        .newClassPathResource("com/baeldung/drools/rules/Discount.drl.xls",
        getClass());

    DecisionTableProviderImpl decisionTableProvider 
    = new DecisionTableProviderImpl();
    
    String drl = decisionTableProvider.loadFromResource(dt, null);
    ```

7. 总结

    在这篇文章中，我们看到了一个使用Drools来管理Excel电子表格中的业务规则的快速例子。我们已经看到了在Excel文件中定义规则的结构和最基本的关键词集。接下来，我们使用Kie组件来读取和启动规则。最后，我们编写了测试案例来验证结果。

## Relevant Articles

- [ ] [Introduction to Drools](https://www.baeldung.com/drools)
- [ ] [An Example of Backward Chaining in Drools](https://www.baeldung.com/drools-backward-chaining)
- [ ] [Drools Using Rules from Excel Files](https://www.baeldung.com/drools-excel)

## Code

像往常一样，这篇文章的完整代码可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/drools)上找到。

IDE运行环境：`<java.version>11</java.version>`。

```bash
% mvn -version
Apache Maven 3.9.0 (9b58d2bad23a66be161c4664ef21ce219c2c8584)
Maven home: /opt/homebrew/Cellar/maven/3.9.0/libexec
Java version: 11.0.17, vendor: Azul Systems, Inc., runtime: /Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
```

系统Java_home需要指向JDK11，与 `<maven.compiler.xx>11</maven.compiler.xx>` 保持一致。
