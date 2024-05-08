# Spring Statemachine 参考文档

<https://docs.spring.io/spring-statemachine/docs/3.2.1/reference/#introduction>

<https://github.com/spring-projects/spring-statemachine>

## 前言

状态机的概念很可能比本参考文档的任何读者都要早，而且肯定比 Java 语言本身还要早。对有限自动机的描述可以追溯到 1943 年，当时沃伦-麦库洛克（Warren McCulloch）和沃尔特-皮茨（Walter Pitts）两位先生写了一篇关于有限自动机的论文。后来，乔治-H-米利（George H. Mealy）于 1955 年提出了状态机概念（被称为 "Mealy机"）。一年后，即 1956 年，爱德华-摩尔（Edward F. Moore）发表了另一篇论文，描述了所谓的 "Moore机"。如果你读过任何有关状态机的文章，那么 Mealy 和 Moore 这两个名字应该会在某个时刻出现在你的脑海中。

本参考文档包含以下部分：
引言包含对本参考文档的介绍。
使用 Spring Statemachine 介绍 Spring Statemachine(SSM) 的用法。
状态机示例（State Machine Examples）包含更详细的状态机示例。
常见问题包含常见问题。
附录包含有关所用材料和状态机的通用信息。

## 简介

Spring Statemachine (SSM) 是一个让应用程序开发人员在 Spring 应用程序中使用传统状态机概念的框架。SSM 提供以下功能：

- 易于使用的扁平（单层）状态机，适用于简单用例。
- 分层状态机结构可简化复杂的状态配置。
- 状态机区域可提供更复杂的状态配置。
- 使用触发器、转换、保护和动作。
- 类型安全配置适配器
- 状态机事件监听器
- Spring IoC 集成，将 bean 与状态机关联起来。

在继续学习之前，我们建议你先阅读附录《词汇表》和《[状态机速成班](https://docs.spring.io/spring-statemachine/docs/3.2.1/reference/#crashcourse)》，对状态机有一个大致的了解。文档的其余部分希望你熟悉状态机的概念。

### 背景知识

状态机之所以功能强大，是因为状态机的行为始终保持一致，而且相对容易调试，这是因为状态机的运行规则是在启动时就写好的。我们的想法是，你的应用程序现在处于并可能存在于有限数量的状态中。然后会发生一些事情，把你的应用程序从一个状态带到下一个状态。状态机由触发器驱动，触发器基于事件或定时器。
在应用程序之外设计高级逻辑，然后以各种不同的方式与状态机交互要容易得多。你可以通过发送事件、监听状态机的操作或请求当前状态来与状态机交互。
传统上，当开发人员发现代码库看起来像一盘意大利面条时，就会将状态机添加到现有项目中。意大利面代码看起来就像一个由 IF、ELSE 和 BREAK 子句组成的永无止境的分层结构，当事情开始变得过于复杂时，编译器可能会要求开发人员回家。

### 使用场景

当出现以下情况时，项目就适合使用状态机：

- 可以用状态来表示应用程序或其部分结构。
- 你想把复杂的逻辑拆分成较小的可管理任务。
- 应用程序已经因为（例如）异步发生的事情而出现并发问题。

您已经在尝试实现状态机，这时您可以

- 使用布尔标志或枚举来模拟情况。
- 使用只对应用程序生命周期的某些部分有意义的变量。
- 在 if-else 结构（或者更糟糕的是，多个此类结构）中循环，检查特定标志或枚举是否被设置，然后在标志和枚举的特定组合存在或不存在时进一步设置异常。

## 入门

如果你刚刚开始使用 Spring Statemachine，本节将为你提供帮助！在这里，我们将回答 "是什么？"、"怎么做？"和 "为什么？"等基本问题。我们首先温和地介绍 Spring Statemachine。然后，我们将构建第一个 Spring Statemachine 应用程序，并讨论一些核心原则。

### 系统要求

Spring Statemachine 3.2.1 是使用 JDK 8（所有工件都兼容 JDK 7）和 Spring Framework 5.3.27 构建和测试的。它的核心系统不需要 Spring Framework 之外的任何其他依赖项。

其他可选部分（如[使用分布式状态](https://docs.spring.io/spring-statemachine/docs/3.2.1/reference/#sm-distributed)）依赖于 Zookeeper，而[状态机示例](https://docs.spring.io/spring-statemachine/docs/3.2.1/reference/#statemachine-examples)依赖于 spring-shell 和 spring-boot，它们会带来框架本身之外的其他依赖。此外，可选的安全和数据访问功能也依赖于 Spring Security 和 Spring Data 模块。

### 模块

下表介绍了 Spring Statemachine 可用的模块。

| Module                             | 说明                                                                          |
|------------------------------------|-----------------------------------------------------------------------------|
| spring-statemachine-core           | Spring Statemachine 的核心系统。                                                  |
| spring-statemachine-recipes-common | 无需依赖于核心框架之外的常用配方。                                                           |
| spring-statemachine-kryo           | Spring Statemachine 的 Kryo 序列化器。                                            |
| spring-statemachine-data-common    | Spring Data 的通用支持模块                                                         |
| spring-statemachine-data-jpa       | Spring Data JPA 支持模块                                                        |
| spring-statemachine-data-redis     | Spring Data Redis 支持模块                                                      |
| spring-statemachine-data-mongodb   | Spring Data MongoDB 支持模块                                                    |
| spring-statemachine-zookeeper      | 分布式状态机的 Zookeeper 集成。                                                       |
| spring-statemachine-test           | 状态机测试支持模块                                                                   |
| spring-statemachine-cluster        | Spring Cloud Cluster 支持模块。注意 Spring Cloud Cluster 已被 Spring Integration 取代。 |
| spring-statemachine-uml            | 使用 Eclipse Papyrus 进行 UI UML 建模的支持模块。                                       |
| spring-statemachine-autoconfigure  | Spring Boot 支持模块。                                                           |
| spring-statemachine-bom            | 物料清单 pom。                                                                   |
| spring-statemachine-starter        | Spring Boot 启动器。                                                            |

### 使用 Gradle

典型的 build.gradle 文件，可通过在 <https://start.spring.io> 上选择各种设置创建的。

在正常的项目结构下，可以使用以下命令构建该项目：
`# ./gradlew clean build`

预期的 Spring Boot 打包胖 jar 将是 build/libs/demo-0.0.1-SNAPSHOT.jar。

### 使用 Maven

典型的 pom.xml 文件，可通过在 <https://start.spring.io> 上选择各种选项创建的。

在正常项目结构下，可以使用以下命令构建该项目：

`# mvn clean package`

预期的 Spring Boot 打包 fat-jar 将是 target/demo-0.0.1-SNAPSHOT.jar。

> 注意：在生产开发中不需要使用`libs-milestone`和 `libs-snapshot` 资源库。

### 开发第一个 Spring Statemachine 应用程序

你可以先创建一个实现 CommandLineRunner 的简单 Spring Boot 应用程序类。下面的示例展示了如何做到这一点：

```java
@SpringBootApplication
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

然后，您需要添加状态和事件，如下例所示：

```java
public enum States {
    SI, S1, S2
}
public enum Events {
    E1, E2
}
```

然后需要添加状态机配置，如下例所示：

```java
@Configuration
@EnableStateMachine
public class StateMachineConfig
        extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config)
            throws Exception {
        config
            .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states)
            throws Exception {
        states
            .withStates()
                .initial(States.SI)
                    .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions)
            throws Exception {
        transitions
            .withExternal()
                .source(States.SI).target(States.S1).event(Events.E1)
                .and()
            .withExternal()
                .source(States.S1).target(States.S2).event(Events.E2);
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                System.out.println("State change to " + to.getId());
            }
        };
    }
}
```

然后，您需要实现 CommandLineRunner 并自动连接 StateMachine。下面的示例展示了如何实现：

```java
@Autowired
private StateMachine<States, Events> stateMachine;

@Override
public void run(String... args) throws Exception {
    stateMachine.sendEvent(Events.E1);
    stateMachine.sendEvent(Events.E2);
}
```

根据您使用 Gradle 还是 Maven 构建应用程序，您可以分别使用 java -jar build/libs/gs-statemachine-0.1.0.jar 或 java -jar target/gs-statemachine-0.1.0.jar 运行它。
该命令的结果应该是正常的 Spring Boot 输出。不过，您还会发现以下几行：

```log
State change to SI
State change to S1
State change to S2
```

这些行表明，您构建的机器正在从一个状态移动到另一个状态。

<https://docs.spring.io/spring-statemachine/docs/3.2.1/reference/#statemachine>