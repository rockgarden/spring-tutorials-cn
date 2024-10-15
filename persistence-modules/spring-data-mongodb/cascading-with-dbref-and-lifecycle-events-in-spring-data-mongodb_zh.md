# [Spring Data MongoDB中的自定义级联](https://www.baeldung.com/cascading-with-dbref-and-lifecycle-events-in-spring-data-mongodb)

1. 一览表

    本教程将继续探索Spring Data MongoDB的一些核心功能——@DBRef注释和生命周期事件。

2. @DBREf

    映射框架不支持在其他文档中存储亲子关系和嵌入式文档。不过，我们能做的是——我们可以将它们单独存储，并使用DBRef来引用文档。

    当从MongoDB加载对象时，这些引用将被急切地解析，我们将返回一个映射对象，该对象看起来与存储在主文档中相同。

    让我们来看看一些代码：

    ```java
    @DBRef
    private EmailAddress emailAddress;
    ```

    电子邮件地址看起来像：

    ```java
    @Document
    public class EmailAddress {
        @Id
        private String id;
        private String value;
        // standard getters and setters
    }
    ```

    请注意，映射框架不处理级联操作。因此——例如——如果我们触发父级的保存，子元素将不会自动保存——如果我们也想保存它，我们需要明确地触发子元素的保存。

    这正是生命周期事件派上用场的地方。

3. 生命周期事件

    Spring Data MongoDB发布了一些非常有用的生命周期事件——例如onBeforeConvert、onBeforeSave、onAfterSave、onAfterLoad和onAfterConvert。

    要拦截其中一个事件，我们需要注册AbstractMappingEventListener的子类，并覆盖此处的其中一个方法。当事件被发送时，我们的监听器将被调用，并传递域对象。

    1. 基本级联保存

        让我们看看我们之前的例子——用电子邮件地址保存用户。我们现在可以监听theonBeforeConvert事件，该事件将在域对象进入转换器之前调用：

        ```java
        public class UserCascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {
            @Autowired
            private MongoOperations mongoOperations;
            @Override
            public void onBeforeConvert(BeforeConvertEvent<Object> event) { 
                Object source = event.getSource(); 
                if ((source instanceof User) && (((User) source).getEmailAddress() != null)) { 
                    mongoOperations.save(((User) source).getEmailAddress());
                }
            }
        }
        ```

        现在我们只需要将监听器注册到MongoConfig中：

        ```java
        @Bean
        public UserCascadeSaveMongoEventListener userCascadingMongoEventListener() {
            return new UserCascadeSaveMongoEventListener();
        }
        ```

        或者作为XML：

        `<bean class="org.baeldung.event.UserCascadeSaveMongoEventListener" />`

        我们已经完成了级联语义——尽管只为用户。

    2. 通用级联实现

        现在让我们通过使级联功能通用来改进之前的解决方案。让我们从定义自定义注释开始：

        ```java
        @Retention(RetentionPolicy.RUNTIME)
        @Target(ElementType.FIELD)
        public @interface CascadeSave {
            //
        }
        ```

        现在让我们在我们的自定义侦听器上工作，以通用的方式处理这些字段，而不必向任何特定实体施放：

        ```java
        public class CascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {

            @Autowired
            private MongoOperations mongoOperations;

            @Override
            public void onBeforeConvert(BeforeConvertEvent<Object> event) { 
                Object source = event.getSource(); 
                ReflectionUtils.doWithFields(source.getClass(), 
                new CascadeCallback(source, mongoOperations));
            }
        }
        ```

        因此，我们正在使用Spring的反射实用程序，并且我们正在所有符合我们标准的字段上运行回调：

        ```java
        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
            ReflectionUtils.makeAccessible(field);

            if (field.isAnnotationPresent(DBRef.class) && 
            field.isAnnotationPresent(CascadeSave.class)) {
            
                Object fieldValue = field.get(getSource());
                if (fieldValue != null) {
                    FieldCallback callback = new FieldCallback();
                    ReflectionUtils.doWithFields(fieldValue.getClass(), callback);

                    getMongoOperations().save(fieldValue);
                }
            }
        }
        ```

        如您所见，我们正在寻找同时具有DBRef注释和CascadeSave的字段。一旦我们找到这些字段，我们就会保存子实体。

        让我们来看看我们用来检查子级是否有@Id注释的FieldCallback类：

        ```java
        public class FieldCallback implements ReflectionUtils.FieldCallback {
            private boolean idFound;

            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                ReflectionUtils.makeAccessible(field);

                if (field.isAnnotationPresent(Id.class)) {
                    idFound = true;
                }
            }

            public boolean isIdFound() {
                return idFound;
            }
        }
        ```

        最后，为了使这一切一起工作，我们当然需要通过电子邮件地址字段进行正确的注释：

        ```java
        @DBRef
        @CascadeSave
        private EmailAddress emailAddress;
        ```

    3. 级联测试

        现在让我们来看看一个场景——我们用电子邮件地址保存一个用户，保存操作会自动级联到这个嵌入式实体：

        ```java
        User user = new User();
        user.setName("Brendan");
        EmailAddress emailAddress = new EmailAddress();
        emailAddress.setValue("b@gmail.com");
        user.setEmailAddress(emailAddress);
        mongoTemplate.insert(user);
        ```

        让我们来检查一下我们的数据库：

        ```json
        {
            "_id" : ObjectId("55cee9cc0badb9271768c8b9"),
            "name" : "Brendan",
            "age" : null,
            "email" : {
                "value" : "b@gmail.com"
            }
        }
        ```

4. 结论

    在本文中，我们说明了Spring Data MongoDB的一些很酷的功能——@DBRef注释、生命周期事件以及我们如何智能地处理级联。
