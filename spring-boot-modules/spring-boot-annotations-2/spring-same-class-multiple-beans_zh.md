# [使用Spring注释实例化同一类的多个beans](https://www.baeldung.com/spring-same-class-multiple-beans)

1. 一览表

    Spring IoC容器创建和管理Spring beans，这是我们应用程序的核心。创建bean实例与从普通Java类创建对象相同。然而，产生几个同类的豆子可能具有挑战性。

    在本教程中，我们将学习如何在Spring框架中使用注释来创建同一类的多个bean。

2. 使用Java配置

    这是使用注释创建同一类的多个bean的最简单、最容易的方法。在这种方法中，我们将使用基于Java的配置类来配置同一类的多个bean。

    让我们考虑一个简单的例子。我们有一个Person类，它有两个类成员，名字和姓氏：

    ```java
    public class Person {
        private String firstName;
        private String lastName;

        public Person(String firstName, String secondName) {
            super();
            this.firstName = firstName;
            this.lastName = secondName;
        }

        @Override
        public String toString() {
            return "Person [firstName=" + firstName + ", secondName=" + lastName + "]";
        }
    }
    ```

    接下来，我们将构建一个名为PersonConfig的配置类，并在其中定义Person类的多个bean：

    ```java
    @Configuration
    public class PersonConfig {
        @Bean
        public Person personOne() {
            return new Person("Harold", "Finch");
        }

        @Bean
        public Person personTwo() {
            return new Person("John", "Reese");
        }
    }
    ```

    在这里，@Bean实例化了两个ID与方法名称相同的bean，并在BeanFactory（Spring容器）接口中注册它们。接下来，我们可以初始化Spring容器，并从Spring容器中请求任何豆子。这一策略也使实现依赖注入变得简单。我们可以使用自动接线将一个豆子（如personOne）直接注入另一个相同类型的豆子（如personTwo）。

    这种方法的局限性在于，我们需要在典型的基于Java的配置样式中使用新关键字手动实例化bean。因此，如果同一类的bean数量增加，我们需要先注册它们，并在配置类中创建bean。这使得它成为一种更特定于Java的方法，而不是特定于Spring的方法。

3. 使用@Component注释

    在这种方法中，我们将使用@Component注释来创建多个bean，这些bean继承了从Person类的属性。首先，我们将创建多个子类，即PersonOne和PersonTwo，扩展Person超类：

    ```java
    @Component
    public class PersonOne extends Person {

        public PersonOne() {
            super("Harold", "Finch");
        }
    }

    @Component
    public class PersonTwo extends Person {

        public PersonTwo() {
            super("John", "Reese");
        }
    }
    ```

    接下来，在PersonConfig文件中，我们将使用@ComponentScan注释来启用整个软件包的组件扫描。这使得Spring容器能够自动创建注释为@Component的任何类的bean：

    ```java
    @Configuration
    @ComponentScan("com.baeldung.multibeaninstantiation.solution2")
    public class PersonConfig {
    }
    ```

    现在我们可以使用Spring容器中的PersonOne或PersonTwo豆子。在其他地方，我们可以使用Person类bean。这种方法的问题在于，它不会创建同一类的多个实例。相反，它创建了从超类继承属性的类豆。

    因此，我们只能在继承类没有定义任何其他属性的情况下使用此解决方案。此外，继承的使用增加了代码的整体复杂性。

4. 使用BeanFactoryPostProcessor

    第三个也是最后一个方法利用[BeanFactoryPostProcessor](https://www.baeldung.com/spring-beanpostprocessor)接口的自定义实现来创建同一类的多个bean实例。

    这可以通过以下步骤来实现：

    - 创建自定义bean类并使用FactoryBean接口进行配置
    - 使用BeanFactoryPostProcessor接口实例化同一类型的多个bean

    1. 自定义Bean实现

        为了更好地理解这种方法，我们将进一步扩展同一示例。假设有一个依赖于Person类的多个实例的Human类：

        ```java
        public class Human implements InitializingBean {

            private Person personOne;

            private Person personTwo;

            @Override
            public void afterPropertiesSet() throws Exception {
                Assert.notNull(personOne, "Harold is alive!");
                Assert.notNull(personTwo, "John is alive!");
            }

            /* Setter injection */
            @Autowired
            public void setPersonOne(Person personOne) {
                this.personOne = personOne;
                this.personOne.setFirstName("Harold");
                this.personOne.setSecondName("Finch");
            }

            @Autowired
            public void setPersonTwo(Person personTwo) {
                this.personTwo = personTwo;
                this.personTwo.setFirstName("John");
                this.personTwo.setSecondName("Reese");
            }
        }
        ```

        [InitializingBean](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/InitializingBean.html)接口调用afterPropertiesSet（）方法，以检查BeanFactory是否设置了所有bean属性，并满足其他依赖项。此外，我们正在使用设置器注入来注入和初始化两个Person类bean，personOne和personTwo。接下来，我们将创建一个实现FactoryBean接口的Person类。

        FactoryBean充当在IoC容器中创建其他豆的工厂。此接口旨在创建更多实现它的bean实例。在我们的案例中，它生成了Person类类型的实例，并自动配置它：

        ```java
        @Qualifier(value = "personOne, personTwo")
        public class Person implements FactoryBean<Object> {
            private String firstName;
            private String secondName;

            public Person() {
                // initialization code (optional)
            }

            @Override
            public Class<Person> getObjectType() {
                return Person.class;
            }

            @Override
            public Object getObject() throws Exception {
                return new Person();
            }

            public boolean isSingleton() {
                return true;
            }

            // code for getters & setters
        }
        ```

        这里需要注意的第二件重要的事情是使用@Qualifier注释，其中包含类级多个人类型的名称或bean id。在这种情况下，在班级级别使用@Qualifier背后是有原因的，我们接下来将看到。

    2. 自定义BeanFactory实现

        现在，我们将使用[BeanFactoryPostProcessor](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/beans/factory/config/BeanFactoryPostProcessor.html)界面的自定义实现。

        任何实现BeanFactoryPostProcessor的类都会在创建任何Spring bean之前执行。这允许我们配置和操作bean生命周期。

        BeanFactoryPostProcessor扫描所有带有@Qualifier注释的类。此外，它从该注释中提取名称（bean id），并手动创建具有指定名称的该类类型的实例：

        ```java
        public class PersonFactoryPostProcessor implements BeanFactoryPostProcessor {

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                Map<String, Object> map = beanFactory.getBeansWithAnnotation(Qualifier.class);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    createInstances(beanFactory, entry.getKey(), entry.getValue());
                }
            }

            private void createInstances(ConfigurableListableBeanFactory beanFactory, String beanName, Object bean) {
                Qualifier qualifier = bean.getClass().getAnnotation(Qualifier.class);
                for (String name : extractNames(qualifier)) {
                    Object newBean = beanFactory.getBean(beanName);
                    beanFactory.registerSingleton(name.trim(), newBean);
                }
            }

            private String[] extractNames(Qualifier qualifier) {
                return qualifier.value().split(",");
            }
        }
        ```

        在这里，一旦Spring容器初始化，就会调用自定义BeanFactoryPostProcessor实现。

        接下来，为了保持简单，我们将使用Java配置类来初始化自定义和BeanFactory实现：

        ```java
        @Configuration
        public class PersonConfig {
            @Bean
            public PersonFactoryPostProcessor PersonFactoryPostProcessor() {
                return new PersonFactoryPostProcessor();
            }

            @Bean
            public Person person() {
                return new Person();
            }

            @Bean
            public Human human() {
                return new Human();
            }
        }
        ```

        这种方法的局限性在于它的复杂性。此外，不鼓励使用它，因为这不是在典型的Spring应用程序中配置bean的自然方式。尽管有局限性，但这种方法更特定于Spring，并具有使用注释实例化多个类似类型的bean的目的。

5. 结论

    在本文中，我们通过三种不同的方法了解了使用Spring注释实例化同一类的多个bean。前两种方法是实例化多个Springbean的简单、Java特定的方法。第三个有点棘手和复杂，但它符合使用注释创建bean的目的。
