# Evrete 规则引擎简介

1. 简介

    本文将对新的开源 Java 规则引擎 Evrete 进行初步介绍。

    从历史上看，[Evrete](https://www.evrete.org/) 是作为 Drools 规则引擎的轻量级替代品而开发的。它完全符合 [Java 规则引擎规范](https://jcp.org/en/jsr/detail?id=94)，使用经典的前向链式 RETE 算法，并针对大量数据的处理做了一些调整和改进。

    它需要 Java 8 或更高版本，零依赖性，可无缝操作 JSON 和 XML 对象，并允许将功能接口作为规则的条件和操作。

    它的大多数组件都可通过服务提供商接口进行(Service Provider Interfaces)扩展，其中一个 SPI 实现可将注释的 Java 类转化为可执行的规则集。我们今天也将试一试。

2. Maven 依赖项

    在跳转到 Java 代码之前，我们需要在项目的 pom.xml 中声明 evrete-core Maven 依赖项：

    ```xml
    <dependency>
        <groupId>org.evrete</groupId>
        <artifactId>evrete-core</artifactId>
        <version>3.0.01</version>
    </dependency>
    ```

3. 用例场景

    为了使介绍不那么抽象，让我们想象一下，我们经营着一家小企业，今天是财政年度末，我们想计算每个客户的总销售额。

    我们的领域数据模型将包括两个简单的类 - Customer 和 Invoice：

    ```java
    public class Customer {
        private double total = 0.0;
        private final String name;

        public Customer(String name) {
            this.name = name;
        }

        public void addToTotal(double amount) {
            this.total += amount;
        }
        // getters and setters
    }
    public class Invoice {
        private final Customer customer;
        private final double amount;

        public Invoice(Customer customer, double amount) {
            this.customer = customer;
            this.amount = amount;
        }
        // getters and setters
    }
    ```

    另外，该引擎支持开箱即用的 [Java 记录](https://www.baeldung.com/java-16-new-features#records-jep-395)，允许开发人员将任意类属性声明为功能接口。

    在稍后的介绍中，我们将得到一个发票和客户集合，逻辑表明我们需要两条规则来处理数据：

    - 第一条规则清除每个客户的总销售额
    - 第二条规则匹配发票和客户，并更新每个客户的总额。

    我们将再次使用流体规则生成器接口和注释 Java 类来实现这些规则。让我们从规则生成器 API 开始。

4. 规则生成器 API

    规则生成器是为规则开发特定领域语言（DSL）的核心构件。开发人员在解析 Excel 源文件、纯文本或任何其他 DSL 格式时，都会用到规则生成器。

    不过，在我们的案例中，我们主要关注的是它们将规则直接嵌入开发人员代码的能力。

    1. 规则集声明

        有了规则构建器，我们就可以使用流畅接口来声明我们的两个规则了：

        ```java
        KnowledgeService service = new KnowledgeService();
        Knowledge knowledge = service
        .newKnowledge()
        .newRule("Clear total sales")
        .forEach("$c", Customer.class)
        .execute(ctx -> {
            Customer c = ctx.get("$c");
            c.setTotal(0.0);
        })
        .newRule("Compute totals")
        .forEach(
            "$c", Customer.class,
            "$i", Invoice.class
        )
        .where("$i.customer == $c")
        .execute(ctx -> {
            Customer c = ctx.get("$c");
            Invoice i = ctx.get("$i");
            c.addToTotal(i.getAmount());
        });
        ```

        首先，我们创建了一个 KnowledgeService 实例，它本质上是一个共享执行器服务。通常，我们应该为每个应用程序创建一个 KnowledgeService 实例。

        生成的知识实例是我们两个规则的预编译版本。我们这样做的原因与我们编译源代码的原因相同--确保正确性并更快地启动代码。

        熟悉 Drools 规则引擎的人会发现，我们的规则声明在语义上等同于相同逻辑的以下 DRL 版本：

        ```drl
        rule "Clear total sales"
        when
            $c: Customer
        then
            $c.setTotal(0.0);
        end

        rule "Compute totals"
        when
            $c: Customer
            $i: Invoice(customer == $c)
        then
            $c.addToTotal($i.getAmount());
        end
        ```

    2. 模拟测试数据

        我们将在三个客户和 10 万张发票上测试我们的规则集，发票金额随机且随机分布在客户中：

        ```java
        List<Customer> customers = Arrays.asList(
        new Customer("Customer A"),
        new Customer("Customer B"),
        new Customer("Customer C")
        );

        Random random = new Random();
        Collection<Object> sessionData = new LinkedList<>(customers);
        for (int i = 0; i < 100_000; i++) {
            Customer randomCustomer = customers.get(random.nextInt(customers.size()));
            Invoice invoice = new Invoice(randomCustomer, 100 * random.nextDouble());
            sessionData.add(invoice);
        }
        ```

        现在，sessionData 变量包含了客户和发票实例的组合，我们将把它们插入到规则会话中。

    3. 规则执行

        我们现在需要做的就是将所有 100,003 个对象（100k 张发票加上三个客户）输入到一个新的会话实例中，并调用其 fire() 方法：

        ```java
        knowledge
        .newStatelessSession()
        .insert(sessionData)
        .fire();

        for(Customer c : customers) {
            System.out.printf("%s:\t$%,.2f%n", c.getName(), c.getTotal());
        }
        ```

        最后几行将打印每个客户的销售额：

        ```log
        Customer A: $1,664,730.73
        Customer B: $1,666,508.11
        Customer C: $1,672,685.10
        ```

5. 注释 Java 规则

    虽然我们前面的示例能达到预期效果，但它并不能使库符合规范，规范希望规则引擎能够：

    - “通过外部化业务或应用逻辑来促进声明式编程"。
    - “包含文档化的文件格式或工具，以编写规则，并在应用程序外部设置规则执行集"。

    简而言之，这意味着符合要求的规则引擎必须能够执行在其运行时之外编写的规则。

    Evrete 的注释 Java 规则扩展模块满足了这一要求。事实上，该模块是一个 “展示型(showcase)”DSL，完全依赖于库的核心 API。

    让我们看看它是如何工作的。

    1. 安装

        Annotated Java Rules 是 Evrete 服务提供商接口 (SPI) 的一种实现，需要额外的 evrete-dsl-java Maven 依赖项：

        ```xml
        <dependency>
            <groupId>org.evrete</groupId>
            <artifactId>evrete-dsl-java</artifactId>
            <version>3.0.01</version>
        </dependency>
        ```

    2. 规则集声明

        让我们使用注解创建相同的规则集。我们将选择纯 Java 源，而不是类和捆绑的 jar：

        ```java
        public class SalesRuleset {

            @Rule
            public void rule1(Customer $c) {
                $c.setTotal(0.0);
            }

            @Rule
            @Where("$i.customer == $c")
            public void rule2(Customer $c, Invoice $i) {
                $c.addToTotal($i.getAmount());
            }
        }
        ```

        该源文件可以使用任何名称，无需遵循 Java 命名约定。引擎会按原样编译源文件，我们需要确保：

        - 我们的源文件包含所有必要的导入
        - 第三方依赖项和域类位于引擎的类路径上

        然后，我们告诉引擎从外部位置读取我们的规则集定义：

        ```java
        KnowledgeService service = new KnowledgeService();
        URL rulesetUrl = new URL("ruleset.java"); // or file.toURI().toURL(), etc
        Knowledge knowledge = service.newKnowledge(
        "JAVA-SOURCE",
        rulesetUrl
        );
        ```

        就是这样。只要代码的其他部分保持不变，我们就会得到同样的三个客户以及他们的随机销售量。

        关于这个特殊示例的一些注意事项：

        - 我们选择从纯 Java（“JAVA-SOURCE”参数）构建规则，从而允许引擎从方法参数中推断事实名称。
        - 如果我们选择 .class 或 .jar 源，方法参数就需要 @Fact 注释。
        - 引擎会自动根据方法名称对规则进行排序。如果我们交换名称，重置规则将清除先前计算的卷。因此，我们将看到零销售量。

    3. 如何工作

        每当创建一个新会话时，引擎都会将其与一个注释规则类的新实例耦合。从本质上讲，我们可以将这些类的实例视为会话本身。

        因此，如果定义了类变量，规则方法就可以访问类变量。

        如果我们定义了条件方法或将新字段声明为方法，这些方法也可以访问类变量。

        与普通 Java 类一样，这些规则集可以扩展、重用并打包成库。

        5.4. 附加功能

        简单的示例非常适合作为入门，但会遗漏许多重要的主题。对于注释 Java 规则，这些内容包括

        - 作为类方法的条件
        - 作为类方法的任意属性声明
        - 阶段监听器、继承模型和对运行时环境的访问
        - 最重要的是，全面使用类字段--从条件到操作和字段定义

6. 结论

    在本文中，我们简要测试了一个新的 Java 规则引擎。主要收获包括

    - 其他引擎可能更擅长提供即用型 DSL 解决方案和规则库。
    - Evrete 是为开发人员构建任意 DSL 而设计的。
    - 那些习惯于用 Java 编写规则的人可能会发现 “注释 Java 规则(Annotated Java rules)” 包是一个更好的选择。

    值得一提的是本文未涉及但在库的 API 中提到的其他功能：

    - 声明任意事实属性
    - 作为 Java 谓词的条件
    - 即时更改规则条件和操作
    - 冲突解决技术
    - 向实时会话添加新规则
    - 自定义实现库的扩展接口

    官方文档位于 <https://www.evrete.org/docs/>。
