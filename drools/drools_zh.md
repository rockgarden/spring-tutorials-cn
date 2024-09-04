# [Drools 简介](https://www.baeldung.com/drools)

1. 概述

    [Drools](https://www.drools.org/) 是一种业务规则管理系统（BRMS）解决方案。它提供了一个规则引擎，用于处理事实，并根据规则和事实处理结果生成输出。业务逻辑的集中化使得快速、低成本地引入变更成为可能。

    它还通过提供以易于理解的格式编写规则的工具，在业务团队和技术团队之间架起了一座桥梁。

2. Maven 依赖项

    要开始使用 Drools，我们首先需要在 pom.xml 中添加几个依赖项：

    ```xml
    <dependency>
        <groupId>org.kie</groupId>
        <artifactId>kie-ci</artifactId>
        <version>9.44.0.Final</version>
    </dependency>
    <dependency>
        <groupId>org.drools</groupId>
        <artifactId>drools-decisiontables</artifactId>
        <version>9.44.0.Final</version>
    </dependency>
    ```

    这两个依赖项的最新版本可在 Maven 中央仓库中找到，即 kie-ci 和 drools-decisiontables。

3. Drools 基础知识

    我们将学习 Drools 的基本概念：

    - 事实(Facts) - 代表作为规则输入的数据。
    - 工作记忆(Working Memory) - 事实的存储空间，用于模式匹配，可以修改、插入和删除。
    - 规则(Rule) - 代表将事实与匹配操作关联起来的单一规则。它可以用 Drools 规则语言编写成 .drl 文件，也可以用 excel 电子表格中的决策表来编写。
    - 知识会话(Knowledge Session) - 它拥有触发规则所需的所有资源；所有事实都被插入到会话中，然后触发匹配的规则。
    - 知识库(Knowledge Base) - 代表 Drools 生态系统中的知识，它拥有关于规则所在资源的信息，还能创建知识会话。
    - 模块(Module) - 一个模块包含多个知识库，这些知识库可以包含不同的会话。

4. Java 配置

    要在给定数据上启动规则，我们需要实例化框架提供的类，其中包含有关规则文件位置和事实(Facts)的信息：

    1. KieServices

        KieServices 类提供了对所有 Kie 构建和运行设施的访问。它提供了多个工厂、服务和实用方法。因此，让我们先来获取一个 KieServices 实例：

        `KieServices kieServices = KieServices.Factory.get();`

        使用 KieServices，我们将创建 KieFileSystem、KieBuilder 和 KieContainer 的新实例。

    2. KieFileSystem

        首先，我们需要设置 KieFileSystem；这是框架提供的内存文件系统。以下代码提供了一个容器，用于以编程方式定义规则文件和决策表等 Drools 资源：

        ```java
        private KieFileSystem getKieFileSystem() {
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            List<String> rules = Arrays.asList("com/baeldung/drools/rules/BackwardChaining.drl", "com/baeldung/drools/rules/SuggestApplicant.drl", "com/baeldung/drools/rules/Product_rules.drl.xls");
            for (String rule : rules) {
                kieFileSystem.write(ResourceFactory.newClassPathResource(rule));
            }
            return kieFileSystem;
        }
        ```

        这里我们从 classpath 读取文件，在 Maven 项目中，classpath 通常是 /src/main/resources。

    3. KieBuilder

        现在，将 KieFileSystem 的内容传递给 KieBuilder 并使用 kb.buildAll() 生成所有已定义的规则。这一步会编译规则并检查它们是否有语法错误：

        ```java
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        ```

    4. KieRepository

        框架会自动将 KieModule（编译生成）添加到 KieRepository 中：

        `KieRepository kieRepository = kieServices.getRepository();`

    5. KieContainer

        现在可以使用该 KieModule 的 ReleaseId 创建一个新的 KieContainer。在这种情况下，Kie 会分配一个默认 ReleaseId：

        ```java
        ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
        KieContainer kieContainer 
        = kieServices.newKieContainer(krDefaultReleaseId);
        ```

    6. KieSession

        现在我们可以从 KieContainer 获取 KieSession。我们的应用程序与 KieSession 进行交互，KieSession 存储并执行运行时数据：

        `KieSession kieSession = kieContainer.newKieSession();`

        下面是完整的配置：

        ```java
        public KieSession getKieSession() {
            KieBuilder kb = kieServices.newKieBuilder(getKieFileSystem());
            kb.buildAll();

            KieRepository kieRepository = kieServices.getRepository();
            ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
            KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);

            return kieContainer.newKieSession();
        }
        ```

5. 执行规则

    设置完成后，我们来看看创建规则的几个选项。

    我们将通过一个例子来探讨规则的实施，这个例子是根据申请人目前的薪水和工作年限对其进行分类。

    1. Drools 规则文件（.drl）

        简而言之，Drools 规则文件包含所有业务规则。

        规则包含一个 When-Then 结构，这里的 When 部分列出了要检查的条件，Then 部分列出了条件满足时要采取的操作：

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

        在 KieSession 中插入 Applicant 和 SuggestedRole 事实即可触发此规则：

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

        它对申请人实例进行了两个条件测试，然后根据两个条件的满足情况在 SuggestedRole 对象中设置角色字段。

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

        在本例中，我们使用了几个 Drools 提供的关键字。让我们来了解一下它们的用途：

        - package - 这是我们在 kmodule.xml 中指定的包名，规则文件位于此包中
        - import - 这与Java的导入语句类似，在这里我们需要指定要插入到KnowledgeSession中的类。
        - global - 用于定义会话的全局变量；可用于传递输入参数或获取输出参数，以汇总会话信息
        - dialect - 方言(dialect)指定了条件部分或操作部分的表达式所使用的语法。默认方言为 Java。Drools 还支持 mvel 方言；这是一种基于 Java 应用程序的表达式语言。它支持field和method/getter 访问
        - rule - 这定义了一个带有规则名称的规则块
        - when - 此项指定了规则条件，在本例中，需要检查的条件是申请人的经验年限（experienceInYears）超过 10 年，且当前薪资（currentSalary）在一定范围内。
        - then - 当满足 when 块中的条件时，该块将执行操作。在本例中，申请人的角色被设置为经理

    2. 决策表

        决策表提供了在预先格式化的 Excel 电子表格中定义规则的功能。Drools 提供的决策表的优势在于，即使是非技术人员也能很容易理解。

        此外，当有类似的规则，但数值不同时，它也很有用，在这种情况下，在 Excel 表格中添加新行比在 .drl 文件中编写新规则更容易。让我们以根据产品类型给产品贴标签为例，看看决策表的结构是怎样的：

        ![Screen-Shot-2017-04-26-at-12.26.59-PM-2](/pic/Screen-Shot-2017-04-26-at-12.26.59-PM-2.png)

        决策表分为不同的部分，最上面的部分就像标题部分，我们在这里指定规则集（即规则文件所在的包）、导入（要导入的 Java 类）和注释（关于规则目的的注释）。

        定义规则的中心部分称为 RuleTable，它将应用于同一域对象的规则分组。

        下一行是列类型 CONDITION 和 ACTION。在这些列中，我们可以访问某一行中提到的域对象的属性，并在随后的行中访问它们的值。

        触发规则的机制与我们在 .drl 文件中看到的类似。

        我们可以通过执行测试来验证应用这些规则的结果：

        ```java
        @Test
        public void whenProductTypeElectronic_ThenLabelBarcode() {
            Product product = new Product("Microwave", "Electronic");
            product = productService.applyLabelToProduct(product);

            assertEquals("BarCode", product.getLabel());
        }
        ```

6. 结论

    在这篇短文中，我们探讨了如何在应用程序中使用 Drools 作为业务规则引擎。我们还看到了可以用 Drools 规则语言以及易于理解的电子表格语言编写规则的多种方法。
