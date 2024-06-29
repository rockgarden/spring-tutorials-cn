# ESB

## 轻量级集成框架比较

JVM环境中提供了集成框架： Spring Integration，Mule ESB和Apache Camel，实现了企业集成模式（EIP），提供了标准化的，特定于领域的语言来集成应用程序，这些应用程序的集成应以标准化的方式建模，并支持自动测试支持。

这三个框架都有很多相似之处，并提供了一致的模型和消息传递体系结构以集成多种技术。 无论您必须使用哪种技术，都始终以相同的方式进行操作，即相同的语法，相同的API，相同的自动测试。 唯一的区别是每个端点的配置（例如，JMS需要队列名称，而JDBC需要数据库连接URL）。

IMO，这是最重要的功能。 每个框架使用不同的名称，但是想法是相同的。 例如，“骆驼路径”等效于“M流”，“骆驼组件”在Spring Integration中称为“适配器”。

此外，还存在其他一些与重量级ESB不同的相似之处。 您只需要在类路径中添加一些库即可。 因此，您可以在JVM环境中的任何地方使用每个框架。 无论您的项目是Java SE独立应用程序，还是要将其部署到Web容器（例如Tomcat），JEE应用程序服务器（例如Glassfish），OSGi容器甚至云中，都可以。 只需添加库，进行一些简单的配置，即可完成。 然后，您可以开始实施集成工作（路由，转换等）。

这三个框架都是开源的，并提供熟悉的公共功能，例如源代码，论坛，邮件列表，问题跟踪和对新功能的投票。 好的社区会编写文档，博客和教程（IMO Apache Camel拥有最引人注目的社区）。 只有已发行书籍的数量对这三者都可能更好。 可以通过不同的供应商获得商业支持：

- Spring集成：SpringSource（ <http://www.springsource.com> ）
- Mule ESB：MuleSoft（ <http://www.mulesoft.org> ）
- Apache Camel：FuseSource（ <http://fusesource.com> ）和Talend（ <http://www.talend.com> ）

IDE的支持非常好，即使视觉设计师也可以使用这三种方法来建模集成问题（并让他们生成代码）。 每个框架都适合企业使用，因为所有框架都提供必需的功能，例如错误处理，自动测试，事务，多线程，可伸缩性和监视。

1. 差异性

    如果您知道这些框架之一，那么由于它们的概念相同和许多其他相似之处，您可以轻松地学习其他框架。 接下来，让我们讨论它们的区别，以便能够决定何时使用哪一个。 两个最重要的区别是支持的技术数量和使用的DSL。 因此，下面我将特别关注这两个标准。 在所有示例中，我将使用代码片段来实现众所周知的EIP“基于内容的路由器”。 自己判断，您更喜欢哪一个。

2. Spring整合
    Spring Integration基于著名的Spring项目，并通过集成支持扩展了编程模型。 您可以像在其他Spring项目中一样使用Spring功能，例如依赖项注入，事务或安全性。

    如果您已经有一个Spring项目并且需要添加一些集成的东西，那么Spring Integration非常棒。 如果您了解Spring本身，那么几乎不需要学习Spring Integration。 尽管如此，Spring Integration仅对技术提供了非常基本的支持-只是“基本的东西”，例如文件，FTP，JMS，TCP，HTTP或Web服务。

    > Mule和Apache Camel提供了许多更多的组件！

    可以通过编写许多XML代码（没有真正的DSL）来实现集成，如下面的代码片段所示：

    ```xml
    <file:inbound-channel-adapter
                id=”incomingOrders”
                directory=”file:incomingOrders”/>
    
    <payload-type-router input-channel=”incomingOrders”>
                <mapping type=”com.kw.DvdOrder” channel=”dvdOrders” />
                <mapping type=”com.kw.VideogameOrder”
                                    channel=”videogameOrders” />
                <mapping type=”com.kw.OtherOrder” channel=”otherOrders” />
    
    </payload-type-router>
    
    <file:outbound-channel-adapter
                id=”dvdOrders”
                directory=”dvdOrders”/>
    
    <jms:outbound-channel-adapter
                id=”videogamesOrders”
                destination=”videogameOrdersQueue”
                channel=”videogamesOrders”/>
    
    <logging-channel-adapter id=”otherOrders” level=”INFO”/>
    ```

    您还可以对某些内容使用Java代码和注释，但是最后，您需要大量XML。 老实说，我不太喜欢XML声明。 它适用于配置（例如JMS连接工厂），但不适用于复杂的集成逻辑。 至少，它应该是具有更好可读性的DSL，但是更复杂的Spring Integration示例确实很难阅读。 
    此外，Eclipse的可视化设计器（称为集成图）还可以，但不如其竞争者那么直观好。 因此，如果我已经有一个现有的Spring项目，并且仅添加一些仅需要“基本技术”（例如文件，FTP，JMS或JDBC）的集成逻辑，就只能使用Spring Integration。

3. Mule ESB

    顾名思义，Mule ESB是一个完整的ESB，包括几个附加功能，而不仅仅是一个集成框架（您可以将其与基于Apache Camel的ESB Apache ServiceMix进行比较）。 尽管如此，Mule也可以用作轻量级的集成框架-只需不添加和使用EIP集成之外的任何其他功能。 作为Spring Integration，Mule仅提供XML DSL。 在我看来，至少它比Spring Integration更容易阅读。 Mule Studio提供了非常出色且直观的视觉设计师。 将以下代码片段与上面的Spring集成代码进行比较。 它比Spring Integration更像DSL。 如果集成逻辑更复杂，则这很重要。

    ```xml
    <flow name=”muleFlow”>
            <file:inbound-endpoint path=”incomingOrders”/>
            <choice>
                <when expression=”payload instanceof com.kw.DvdOrder”
                            evaluator=”groovy”>
                            <file:outbound-endpoint path=”incoming/dvdOrders”/>
                </when>
                <when expression=”payload instanceof com.kw.DvdOrder”
                            evaluator=”groovy”>
                            <jms:outbound-endpoint
                            queue=”videogameOrdersQueue”/>
                </when>
                <otherwise>
                                    <logger level=”INFO”/>
                </otherwise>
            </choice>
    </flow>
    ```

    Mule的主要优点是与重要专有接口（例如SAP，Tibco Rendevous，Oracle Siebel CRM，Paypal或IBM的CICS事务网关 ）的一些非常有趣的连接器 。 如果您的集成项目需要其中一些连接器，那么我可能会选择Mule！

    对于某些项目而言，缺点是Mule对OSGi拒绝： http : //blogs.mulesoft.org/osgi-no-thanks/

4. Apache Camel

    Apache Camel与Mule几乎相同。 它为您可能想到的几乎每种技术提供了许多组件（甚至比Mule还要多）。 如果没有可用的组件，则可以从Maven原型开始很容易地创建自己的组件！ 如果您是Spring的人：Camel也具有很棒的Spring集成。 与其他两个一样，它提供了XML DSL：

    ```xml
    <route>
            <from uri=”file:incomingOrders”/>
            <choice>
                <when>
                    <simple>${in.header.type} is ‘com.kw.DvdOrder’</simple>
                                <to uri=”file:incoming/dvdOrders”/>
                </when>
                <when>
                    <simple>${in.header.type} is ‘com.kw.VideogameOrder’
                </simple>
                                <to uri=”jms:videogameOrdersQueue”/>
                </when>
                <otherwise>
                    <to uri=”log:OtherOrders”/>
                </otherwise>
            </choice>
        </route>
    ```

    可读性优于Spring Integration，并且几乎与Mule相同。 此外，FuseSource还提供了一个很好的（但商业化的）可视化设计器Fuse IDE，它可以生成XML DSL代码。 尽管如此，无论您使用视觉设计器还是仅使用XML编辑器，它都是很多XML。 我个人不喜欢这样。

    因此，让我们向您展示另一个很棒的功能： Apache Camel还提供了Java，Groovy和Scala的DSL 。 您不必编写太多难看的XML。 就个人而言，我更喜欢使用这些流利的DSL之一而不是XML来进行集成逻辑。 我只使用XML做配置工作，例如JMS连接工厂或JDBC属性。 在这里，您可以看到使用Java DSL代码段的相同示例：

    ```java
    from(“file:incomingOrders “)
        .choice()
                    .when(body().isInstanceOf(com.kw.DvdOrder.class))
                                    .to(“file:incoming/dvdOrders”)
                    .when(body().isInstanceOf(com.kw.VideogameOrder.class))
                                    .to(“jms:videogameOrdersQueue “)
                    .otherwise()
                                    .to(“mock:OtherOrders “);
    ```

    流利的编程DSL非常易于阅读（即使在更复杂的示例中也是如此）。 此外，这些编程DSL比XML具有更好的IDE支持（代码完成，重构等）。 由于这些很棒的流利的DSL，如果我不需要Mule的某些出色的连接器来连接专有产品，我将始终使用Apache Camel。 由于它与Spring的集成非常好，因此在大多数用例中，我甚至更喜欢Apache Camel。

    我个人最喜欢的是Apache Camel，这是因为它具有出色的Java，Groovy和Scala DSL ，并结合了许多受支持的技术。 仅当我需要某些专有产品的独特连接器时，才使用Mule。 如果只需要集成“基本技术”（例如FTP或JMS），则只能在现有的Spring项目中使用Spring Integration。 尽管如此：无论您选择这些轻量级集成框架中的哪个，都可以通过轻松的工作轻松实现复杂的集成项目，这将带来很多乐趣。 切记：繁琐的ESB通常具有太多的功能，因此也有太多不必要的复杂性和工作量。 使用正确的工具完成正确的工作！
