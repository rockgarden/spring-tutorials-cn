# [Spring Boot启动器介绍](https://www.baeldung.com/spring-boot-starters)

1. 概述

    依赖性管理是任何复杂项目的一个重要方面。而手动操作并不理想；你在这方面花的时间越多，你在项目的其他重要方面的时间就越少。

    Spring Boot启动器正是为了解决这个问题而建立的。启动器POMs是一组方便的依赖性描述符，你可以将其包含在你的应用程序中。你可以一站式获得你所需要的所有Spring和相关技术，而不必翻阅样本代码和复制粘贴大量的依赖描述符。

    我们有30多个Boot starters可用--让我们在下面的章节中看看其中的一些。

2. 网络启动器

    首先，我们来看看开发REST服务；我们可以使用Spring MVC、Tomcat和Jackson等库--对于一个应用程序来说，有很多依赖性。

    Spring Boot启动器可以帮助减少手动添加的依赖关系的数量，只需添加一个依赖关系。因此，无需手动指定依赖关系，只需添加一个启动器，如下例所示：spring-boot-starter-web

    现在我们可以创建一个REST控制器。为了简单起见，我们将不使用数据库，而专注于REST控制器：

    demo/GenericEntityController.java

    GenericEntity是一个简单的Bean，其id为Long类型，值为String类型。

    就这样--随着应用程序的运行，你可以访问 <http://localhost:8080/entity/all> ，并检查控制器是否工作。

    我们已经用最小的配置创建了一个REST应用程序。

3. 测试启动程序

    对于测试，我们通常使用以下一组库： Spring Test, JUnit, Hamcrest, 和Mockito。我们可以手动包含所有这些库，但Spring Boot启动器可以用以下方式自动包含这些库：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    ```

    请注意，你不需要指定一个工件的版本号。Spring Boot会找出要使用的版本--你所需要指定的只是spring-boot-starter-parent工件的版本。如果以后你需要升级Boot库和依赖关系，只需在一个地方升级Boot版本，它就会处理剩下的事情。

    让我们实际测试一下我们在前面的例子中创建的控制器。

    有两种方法来测试控制器：

    - 使用模拟环境
    - 使用嵌入式Servlet容器（如Tomcat或Jetty）

    在这个例子中，我们将使用一个模拟环境：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = Application.class)
    @WebAppConfiguration
    public class SpringBootApplicationIntegrationTest {
        @Autowired
        private WebApplicationContext webApplicationContext;
        private MockMvc mockMvc;

        @Before
        public void setupMockMvc() {
            mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        }

        @Test
        public void givenRequestHasBeenMade_whenMeetsAllOfGivenConditions_thenCorrect()
        throws Exception { 
            MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
            mockMvc.perform(MockMvcRequestBuilders.get("/entity/all")).
            andExpect(MockMvcResultMatchers.status().isOk()).
            andExpect(MockMvcResultMatchers.content().contentType(contentType)).
            andExpect(jsonPath("$", hasSize(4))); 
        } 
    }
    ```

    上述测试调用了/entity/all端点，并验证了JSON响应是否包含4个元素。为了使这个测试通过，我们还必须在控制器类中初始化我们的列表：

    `entityList.add(new GenericEntity(1l, "entity_1"));`

    这里重要的是，@WebAppConfiguration注解和MockMVC是spring-test模块的一部分，hasSize是Hamcrest匹配器，而@Before是JUnit注解。这些都可以通过导入一个这样的启动依赖来实现。

4. 数据JPA启动程序

    大多数Web应用程序都有某种持久性--而这往往是JPA。

    与其手动定义所有相关的依赖关系--不如用启动器来代替：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    ```

    请注意，开箱即用，我们至少对以下数据库有自动支持： H2、Derby和Hsqldb。在我们的例子中，我们将使用H2。

    现在让我们为我们的实体创建存储库：

    `public interface GenericEntityRepository extends JpaRepository<GenericEntity, Long> {}`

    是时候测试这段代码了。下面是JUnit测试：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = Application.class)
    public class SpringBootJPATest {
        @Autowired
        private GenericEntityRepository genericEntityRepository;
        @Test
        public void givenGenericEntityRepository_whenSaveAndRetreiveEntity_thenOK() {
            GenericEntity genericEntity = 
            genericEntityRepository.save(new GenericEntity("test"));
            GenericEntity foundedEntity = 
            genericEntityRepository.findOne(genericEntity.getId());
            assertNotNull(foundedEntity);
            assertEquals(genericEntity.getValue(), foundedEntity.getValue());
        }
    }
    ```

    我们没有花时间指定数据库供应商、URL连接和凭证。没有必要进行额外的配置，因为我们受益于坚实的Boot默认值；但当然，如果有必要，仍然可以对所有这些细节进行配置。

5. 邮件启动器

    企业开发中一个非常常见的任务是发送电子邮件，直接处理Java Mail API通常会很困难。

    Spring Boot启动器隐藏了这种复杂性--邮件依赖性可以通过以下方式指定：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    ```

    现在我们可以直接使用JavaMailSender了，所以我们来写一些测试。

    为了测试的目的，我们需要一个简单的SMTP服务器。在这个例子中，我们将使用Wiser。我们可以这样把它包含在我们的POM中：

    ```xml
    <dependency>
        <groupId>org.subethamail</groupId>
        <artifactId>subethasmtp</artifactId>
        <version>3.1.7</version>
        <scope>test</scope>
    </dependency>
    ```

    Wiser的最新版本可以在Maven中央仓库找到。

    以下是测试的源代码：

    ```java
    @RunWith(SpringJUnit4ClassRunner.class)
    @SpringApplicationConfiguration(classes = Application.class)
    public class SpringBootMailTest {
        @Autowired
        private JavaMailSender javaMailSender;

        private Wiser wiser;

        private String userTo = "user2@localhost";
        private String userFrom = "user1@localhost";
        private String subject = "Test subject";
        private String textMail = "Text subject mail";

        @Before
        public void setUp() throws Exception {
            final int TEST_PORT = 25;
            wiser = new Wiser(TEST_PORT);
            wiser.start();
        }

        @After
        public void tearDown() throws Exception {
            wiser.stop();
        }

        @Test
        public void givenMail_whenSendAndReceived_thenCorrect() throws Exception {
            SimpleMailMessage message = composeEmailMessage();
            javaMailSender.send(message);
            List<WiserMessage> messages = wiser.getMessages();

            assertThat(messages, hasSize(1));
            WiserMessage wiserMessage = messages.get(0);
            assertEquals(userFrom, wiserMessage.getEnvelopeSender());
            assertEquals(userTo, wiserMessage.getEnvelopeReceiver());
            assertEquals(subject, getSubject(wiserMessage));
            assertEquals(textMail, getMessage(wiserMessage));
        }

        private String getMessage(WiserMessage wiserMessage)
        throws MessagingException, IOException {
            return wiserMessage.getMimeMessage().getContent().toString().trim();
        }

        private String getSubject(WiserMessage wiserMessage) throws MessagingException {
            return wiserMessage.getMimeMessage().getSubject();
        }

        private SimpleMailMessage composeEmailMessage() {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(userTo);
            mailMessage.setReplyTo(userFrom);
            mailMessage.setFrom(userFrom);
            mailMessage.setSubject(subject);
            mailMessage.setText(textMail);
            return mailMessage;
        }
    }
    ```

    在这个测试中，@Before和@After方法负责启动和停止邮件服务器。

    请注意，我们正在为JavaMailSender Bean布线--该bean是由Spring Boot自动创建的。

    就像Boot中的其他默认值一样，JavaMailSender的邮件设置可以在application.properties中自定义：

    ```properties
    spring.mail.host=localhost
    spring.mail.port=25
    spring.mail.properties.mail.smtp.auth=false
    ```

    因此，我们将邮件服务器配置在localhost:25，并且不需要认证。

6. 总结

    在这篇文章中，我们已经给出了启动器的概述，解释了为什么我们需要它们，并提供了如何在项目中使用它们的例子。

    让我们回顾一下使用Spring Boot启动器的好处：

    - 提高POM的可管理性
    - 生产就绪、经过测试和支持的依赖性配置
    - 减少项目的整体配置时间

    实际的启动器列表可以在[这里](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-starters)找到。
