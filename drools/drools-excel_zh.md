# [Drools使用Excel文件中的规则](https://www.baeldung.com/drools-excel)

1. 概述

    Drools支持以电子表格的形式管理业务规则。

    在这篇文章中，我们将看到一个使用Drools来管理Excel文件的业务规则的快速例子。

2. 在Excel中定义规则

    对于我们的例子，让我们定义规则，根据客户类型和作为客户的年限来确定折扣：

    - 大于3年的个人客户获得15%的折扣
    - 少于3年的个人客户获得5%的折扣
    - 所有企业客户获得20%的折扣

    1. Excel文件

        让我们开始按照Drools要求的特定结构和关键词来创建我们的[Excel文件](/src/main/resources/com/baeldung/drools/rules/Discount.drl.xls)：

        ![Discount.drl.xls](/pic/Drools_Excel.webp)

        对于我们这个简单的例子，我们使用了最相关的一组关键词：

        - RuleSet - 表示决策表的开始
        - Import - 规则中使用的Java类
        - RuleTable - 表示规则集的开始
        - Name - 规则的名称
        - CONDITION - 要对输入数据进行检查的条件的代码片断。一个规则应该至少包含一个条件。
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

    ```drl
    Resource dt 
        = ResourceFactory
            .newClassPathResource("com/baeldung/drools/rules/Discount.drl.xls",
            getClass());

    DecisionTableProviderImpl decisionTableProvider 
        = new DecisionTableProviderImpl();
        
    String drl = decisionTableProvider.loadFromResource(dt, null);
    ```

## Code

IDE运行环境：`<java.version>11</java.version>`。

```bash
% mvn -version
Apache Maven 3.9.0 (9b58d2bad23a66be161c4664ef21ce219c2c8584)
Maven home: /opt/homebrew/Cellar/maven/3.9.0/libexec
Java version: 11.0.17, vendor: Azul Systems, Inc., runtime: /Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
```

系统Java_home需要指向JDK11，与 `<maven.compiler.xx>11</maven.compiler.xx>` 保持一致。
