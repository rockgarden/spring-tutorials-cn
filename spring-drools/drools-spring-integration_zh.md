# [Drools Spring 集成](https://www.baeldung.com/drools-spring-integration)

1. 简介

    在本快速教程中，我们将把 Drools 与 Spring 集成。如果您刚开始使用 Drools，请查看这篇介绍文章。

2. Maven 依赖项

    首先，让我们在 pom.xml 文件中添加以下依赖项：

    ```xml
    <dependency>
        <groupId>org.drools</groupId>
        <artifactId>drools-core</artifactId>
        <version>9.44.0.Final</version>
    </dependency>
    <dependency>
        <groupId>org.kie</groupId>
        <artifactId>kie-spring</artifactId>
        <version>7.74.1.Final</version>
    </dependency>
    ```

    最新版本可在此处找到 drools-core，在此处找到 kie-spring。

3. 初始数据

    现在，让我们定义将在示例中使用的数据。我们将根据行驶距离和夜间附加费标志来计算车费。

    下面是一个将用作事实的简单对象：

    ```java
    public class TaxiRide {
        private Boolean isNightSurcharge;
        private Long distanceInMile;
        // standard constructors, getters/setters
    }
    ```

    让我们定义另一个业务对象，用于表示车费：

    ```java
    public class Fare {
        private Long nightSurcharge;
        private Long rideFare;
        // standard constructors, getters/setters
    }
    ```

    现在，让我们定义一个计算出租车费用的业务规则：

    ```drl
    global com.baeldung.spring.drools.model.Fare rideFare;
    dialect  "mvel"

    rule "Calculate Taxi Fare - Scenario 1"
        when
            taxiRideInstance:TaxiRide(isNightSurcharge == false && distanceInMile < 10);
        then
            rideFare.setNightSurcharge(0);
            rideFare.setRideFare(70);
    end
    ```

    正如我们所看到的，定义了一条规则来计算给定出租车的总票价。

    该规则接受一个 TaxiRide 对象，并检查 isNightSurcharge 属性是否为 false，distanceInMile 属性值是否小于 10，然后计算出车费为 70，并将 nightSurcharge 属性设置为 0。

    计算结果将被设置为车费对象，以供进一步使用。

4. 弹簧集成

    1. Spring Bean 配置

        现在，让我们继续进行 Spring 集成。

        我们将定义一个 Spring Bean 配置类，它将负责实例化 TaxiFareCalculatorService Bean 及其依赖项：

        ```java
        @Configuration
        @ComponentScan("com.baeldung.spring.drools.service")
        public class TaxiFareConfiguration {
            private static final String drlFile = "TAXI_FARE_RULE.drl";

            @Bean
            public KieContainer kieContainer() {
                KieServices kieServices = KieServices.Factory.get();

                KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
                kieFileSystem.write(ResourceFactory.newClassPathResource(drlFile));
                KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
                kieBuilder.buildAll();
                KieModule kieModule = kieBuilder.getKieModule();

                return kieServices.newKieContainer(kieModule.getReleaseId());
            }
        }
        ```

        KieServices 是一个单例，它是获取 Kie 提供的所有服务的单点入口。我们使用 KieServices.Factory.get() 获取 KieServices。

        接下来，我们需要获取 KieContainer，它是运行规则引擎所需的所有对象的占位符。

        KieContainer 是在其他 Bean（包括 KieFileSystem、KieBuilder 和 KieModule）的帮助下创建的。

        让我们继续创建 KieModule，它是定义规则知识（即 KieBase）所需的所有资源的容器。

        `KieModule kieModule = kieBuilder.getKieModule();`

        > KieBase 是一个存储库，包含与应用程序相关的所有知识，如规则、流程、函数、类型模型等，它隐藏在 KieModule 中。KieBase 可从 KieContainer 获取。

        一旦创建了 KieModule，我们就可以继续创建 KieContainer，其中包含定义了 KieBase 的 KieModule。KieContainer 是通过模块创建的：

        `KieContainer kContainer = kieServices.newKieContainer(kieModule.getReleaseId());`

    2. Spring 服务

        让我们定义一个服务类，通过将 Fact 对象传递给引擎来处理结果，从而执行实际的业务逻辑：

        ```java
        @Service
        public class TaxiFareCalculatorService {

            @Autowired
            private KieContainer kieContainer;

            public Long calculateFare(TaxiRide taxiRide, Fare rideFare) {
                KieSession kieSession = kieContainer.newKieSession();
                kieSession.setGlobal("rideFare", rideFare);
                kieSession.insert(taxiRide);
                kieSession.fireAllRules();
                kieSession.dispose();
                return rideFare.getTotalFare();
            }
        }
        ```

        最后，使用 KieContainer 实例创建 KieSession。KieSession 实例是插入输入数据的地方。KieSession 与引擎交互，根据插入的事实处理规则中定义的实际业务逻辑。

        全局（就像全局变量）用于向引擎传递信息。我们可以使用 setGlobal(“key”, value) 设置全局；在本例中，我们将 Fare 对象设置为全局，用于存储计算出的出租车费用。

        正如我们在第 4 节中所讨论的，规则需要数据来操作。我们使用 kieSession.insert(taxiRide) 将事实插入会话；

        完成输入 Fact 的设置后，我们就可以调用 fireAllRules() 请求引擎执行业务逻辑。

        最后，我们需要调用 dispose() 方法清理会话，以避免内存泄漏。

5. 实际示例

    现在，我们可以连接一个 Spring 上下文，看看 Drools 是否按照预期运行：

    ```java
    @Test
    public void whenNightSurchargeFalseAndDistLessThan10_thenFixWithoutNightSurcharge() {
        TaxiRide taxiRide = new TaxiRide();
        taxiRide.setIsNightSurcharge(false);
        taxiRide.setDistanceInMile(9L);
        Fare rideFare = new Fare();
        Long totalCharge = taxiFareCalculatorService.calculateFare(taxiRide, rideFare);
        assertNotNull(totalCharge);
        assertEquals(Long.valueOf(70), totalCharge);
    }
    ```

6. 结论

    在本文中，我们通过一个简单的用例了解了 Drools Spring 集成。
