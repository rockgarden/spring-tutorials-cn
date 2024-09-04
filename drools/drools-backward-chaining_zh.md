# [Drools中后向链的一个例子](https://www.baeldung.com/drools-backward-chaining)

1. 概述

    在这篇文章中，我们将看到什么是后向链，以及我们如何在Drools中使用它。

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

    与前向链法相反，后向链法直接从结论（假设hypothesis）开始，通过对一连串事实的回溯来验证它。

    当比较前向连锁和后向连锁时，前者可以被描述为 "数据驱动(data-driven)"（数据作为输入），而后者可以被描述为 "事件（或目标）驱动(event(or goal)-driven)"（目标作为输入）。

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
