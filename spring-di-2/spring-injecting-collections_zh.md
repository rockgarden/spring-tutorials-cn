# [Spring注入集合](https://www.baeldung.com/spring-injecting-collections)

1. 简介

    在本教程中，我们将展示如何使用Spring框架注入Java集合。

    简单地说，我们将演示List、Map、Set集合接口的例子。

2. 使用@Autowired的列表

    让我们来创建一个Bean的例子：

    ```java
    public class CollectionsBean {

        @Autowired
        private List<String> nameList;

        public void printNameList() {
            System.out.println(nameList);
        }
    }
    ```

    在这里，我们声明了nameList属性来保存一个字符串值的列表。

    在这个例子中，我们对nameList使用字段注入。因此，我们放置了@Autowired注解。

    要了解更多关于依赖注入或实现它的不同方法，请查看本[指南](/Intro-zh.md)。

    之后，我们在配置设置类中注册CollectionsBean：

    ```java
    @Configuration
    public class CollectionConfig {

        @Bean
        public CollectionsBean getCollectionsBean() {
            return new CollectionsBean();
        }

        @Bean
        public List<String> nameList() {
            return Arrays.asList("John", "Adam", "Harry");
        }
    }
    ```

    除了注册CollectionsBean，我们还通过显式初始化和返回单独的@Bean配置来注入一个新的列表。

    现在，我们可以测试结果了：

    ```java
    ApplicationContext context = new AnnotationConfigApplicationContext(CollectionConfig.class);
    CollectionsBean collectionsBean = context.getBean(
    CollectionsBean.class);
    collectionsBean.printNameList();
    ```

    printNameList()方法的输出：`[John, Adam, Harry]`

3. 使用构造函数注入的集合

    为了用Set集合设置同样的例子，我们来修改CollectionsBean类：

    ```java
    public class CollectionsBean {

        private Set<String> nameSet;

        public CollectionsBean(Set<String> strings) {
            this.nameSet = strings;
        }

        public void printNameSet() {
            System.out.println(nameSet);
        }
    }
    ```

    这一次，我们想使用构造函数注入来初始化nameSet属性。这也需要对配置类进行修改：

    ```java
    @Bean
    public CollectionsBean getCollectionsBean() {
        return new CollectionsBean(new HashSet<>(Arrays.asList("John", "Adam", "Harry")));
    }
    ```

4. 注入设置器的Map

    按照同样的逻辑，让我们添加nameMap字段来演示地图注入：

    ```java
    public class CollectionsBean {

        private Map<Integer, String> nameMap;

        @Autowired
        public void setNameMap(Map<Integer, String> nameMap) {
            this.nameMap = nameMap;
        }

        public void printNameMap() {
            System.out.println(nameMap);
        }
    }
    ```

    这次我们有一个setter方法，以便使用setter依赖性注入。我们还需要在配置类中添加Map的初始化代码：

    ```java
    @Bean
    public Map<Integer, String> nameMap(){
        Map<Integer, String>  nameMap = new HashMap<>();
        nameMap.put(1, "John");
        nameMap.put(2, "Adam");
        nameMap.put(3, "Harry");
        return nameMap;
    }
    ```

    调用printNameMap()方法后的结果： `{1=John, 2=Adam, 3=Harry}`

5. 注入Bean引用

    让我们看一个例子，我们把Bean引用作为集合的元素注入。

    首先，让我们来创建Bean：

    ```java
    public class BaeldungBean {
        private String name;
        // constructor
    }
    ```

    并将BaeldungBean的List作为一个属性添加到CollectionsBean类中：

    ```java
    public class CollectionsBean {

        @Autowired(required = false)
        private List<BaeldungBean> beanList;

        public void printBeanList() {
            System.out.println(beanList);
        }
    }
    ```

    接下来，我们为每个BaeldungBean元素添加Java配置工厂方法：

    ```java
    @Configuration
    public class CollectionConfig {

        @Bean
        public BaeldungBean getElement() {
            return new BaeldungBean("John");
        }

        @Bean
        public BaeldungBean getAnotherElement() {
            return new BaeldungBean("Adam");
        }

        @Bean
        public BaeldungBean getOneMoreElement() {
            return new BaeldungBean("Harry");
        }

        // other factory methods
    }
    ```

    Spring容器将BaeldungBean类型的各个Bean注入到一个集合中。

    为了测试这一点，我们调用collectionsBean.printBeanList（）方法。输出显示了作为列表元素的Bean名称：

    `[John, Harry, Adam]`

    现在，让我们考虑一下没有BaeldungBean时的情况。如果在应用上下文中没有注册BaeldungBean，Spring会抛出一个异常，因为缺少所需的依赖关系。

    我们可以使用`@Autowired(required = false)`将依赖性标记为可选。而不是抛出一个异常，beanList不会被初始化，其值将保持为空。

    如果我们需要一个空列表而不是null，我们可以用一个新的ArrayList来初始化beanList：

    ```java
    @Autowired(required = false)
    private List<BaeldungBean> beanList = new ArrayList<>();
    ```

    1. 使用@Order对Bean进行排序

        我们可以在注入集合的时候指定Bean的顺序。

        为此，我们使用@Order注解并指定索引：

        ```java
        @Configuration
        public class CollectionConfig {

            @Bean
            @Order(2)
            public BaeldungBean getElement() {
                return new BaeldungBean("John");
            }

            @Bean
            @Order(3)
            public BaeldungBean getAnotherElement() {
                return new BaeldungBean("Adam");
            }

            @Bean
            @Order(1)
            public BaeldungBean getOneMoreElement() {
                return new BaeldungBean("Harry");
            }
        }
        ```

        Spring容器首先会注入名称为 "Harry "的Bean，因为它的顺序值最低。

        然后它将注入 "John"，最后是 "Adam" bean：`[Harry, John, Adam]`

        在本指南中了解更多关于[@Order](https://www.baeldung.com/spring-order)的信息。

    2. 使用@Qualifier来选择Bean

        我们可以使用@Qualifier来选择要注入到符合@Qualifier名称的特定集合中的bean。

        下面是我们如何将其用于注入点：

        ```java
        @Autowired
        @Qualifier("CollectionsBean")
        private List<BaeldungBean> beanList;
        ```

        然后，我们用同样的@Qualifier标记我们想注入List的bean：

        ```java
        @Configuration
        public class CollectionConfig {

            @Bean
            @Qualifier("CollectionsBean")
            public BaeldungBean getElement() {
                return new BaeldungBean("John");
            }

            @Bean
            public BaeldungBean getAnotherElement() {
                return new BaeldungBean("Adam");
            }

            @Bean
            public BaeldungBean getOneMoreElement() {
                return new BaeldungBean("Harry");
            }

            // other factory methods
        }
        ```

        在这个例子中，我们指定名字为 "John" 的Bean将被注入到名为 "CollectionsBean" 的列表中。我们在这里测试的结果：

        ```java
        ApplicationContext context = new AnnotationConfigApplicationContext(CollectionConfig.class);
        CollectionsBean collectionsBean = context.getBean(CollectionsBean.class);
        collectionsBean.printBeanList();
        ```

        从输出中，我们看到我们的集合只有一个元素：`[John]`

6. 设置一个空列表作为默认值

    我们可以通过使用Collections.emptyList()静态方法，将注入的List属性的默认值设置为一个空列表：

    ```java
    public class CollectionsBean {

        @Value("${names.list:}#{T(java.util.Collections).emptyList()}")
        private List<String> nameListWithDefaultValue;
        
        public void printNameListWithDefaults() {
            System.out.println(nameListWithDefaultValue);
        }
    }
    ```

    如果我们在 "names.list" 键没有通过属性文件初始化的情况下运行这个：

    `CollectionsBean.printNameListWithDefaults();`

    我们会得到一个空列表作为输出：`[ ]`

7. 总结

    通过本指南，我们学习了如何使用Spring框架注入不同类型的Java集合。

    我们还研究了引用类型的注入，以及如何在集合中选择或排列它们。
