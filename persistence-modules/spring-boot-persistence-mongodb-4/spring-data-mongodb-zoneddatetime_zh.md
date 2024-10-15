# [带有Spring Data MongoDB的ZonedDateTime](https://www.baeldung.com/spring-data-mongodb-zoneddatetime)

1. 一览表

    Spring Data MongoDB模块在Spring项目中与MongoDB数据库交互时提高了可读性和可用性。

    在本教程中，我们将重点介绍在读取和写入MongoDB数据库时如何处理ZonedDateTime Java对象。

2. 设置

    要使用Spring Data MongoDB模块，我们需要添加以下依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-mongodb</artifactId>
        <version>3.4.7</version>
    </dependency>
    ```

    最新版本的图书馆可以在这里找到。

    让我们定义一个名为Action的模型类（具有ZonedDateTime属性）：

    ```java
    @Document
    public class Action {
        @Id
        private String id;

        private String description;
        private ZonedDateTime time;
        
        // constructor, getters and setters 
    }
    ```

    为了与MongoDB交互，我们还将创建一个扩展MongoRepository的接口：

    `public interface ActionRepository extends MongoRepository<Action, String> { }`

    现在，我们将定义一个测试，该测试将将Action对象插入MongoDB中，并断言它是在正确的时间存储的。在断言评估中，我们正在删除纳秒信息，因为MongoDB Date类型的精度为毫秒：

    ```java
    @Test
    public void givenSavedAction_TimeIsRetrievedCorrectly() {
        String id = "testId";
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        actionRepository.save(new Action(id, "click-action", now));
        Action savedAction = actionRepository.findById(id).get();

        Assert.assertEquals(now.withNano(0), savedAction.getTime().withNano(0)); 
    }
    ```

    开箱即用，我们在运行测试时会收到以下错误：

    ```log
    org.bson.codecs.configuration.CodecConfigurationException:
    Can't find a codec for class java.time.ZonedDateTime
    ```

    Spring Data MongoDB没有定义ZonedDateTime转换器。让我们看看如何配置它们。

3. MongoDB转换器

    我们可以通过定义一个从MongoDB读取的转换器和一个用于写入它的转换器来处理ZonedDateTime对象（在所有模型中）。

    为了读取，我们正在从Date对象转换为ZonedDateTime对象。在下一个示例中，我们使用ZoneOffset.UTC，因为日期对象不存储区域信息：

    ```java
    public class ZonedDateTimeReadConverter implements Converter<Date, ZonedDateTime> {
        @Override
        public ZonedDateTime convert(Date date) {
            return date.toInstant().atZone(ZoneOffset.UTC);
        }
    }
    ```

    然后，我们正在从ZonedDateTime对象转换为Date对象。如果需要，我们可以将区域信息添加到另一个字段中：

    ```java
    public class ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Date> {
        @Override
        public Date convert(ZonedDateTime zonedDateTime) {
            return Date.from(zonedDateTime.toInstant());
        }
    }
    ```

    由于日期对象不存储区域偏移，我们在示例中使用UTC。随着ZonedDateTimeReadConverter和ZonedDateTimeWriteConverter添加到MongoCustomConversions中，我们的测试现在将通过。

    存储对象的简单打印将如下所在：

    `Action{id='testId', description='click', time=2018-11-08T08:03:11.257Z}`

4. 结论

    在这篇简短的文章中，我们看到了如何创建MongoDB转换器来处理Java ZonedDateTime对象。
