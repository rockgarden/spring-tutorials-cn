# [Spring中的@Lookup注解](ttps://www.baeldung.com/spring-lookup)

1. 简介

    在这个快速教程中，我们将通过@Lookup注解来看看Spring的方法级依赖注入支持。

2. 为什么是@Lookup？

    用@Lookup注解的方法告诉Spring，当我们调用该方法时，将返回该方法的返回类型的实例。

    从本质上讲，Spring将覆盖我们注释的方法，并将我们方法的返回类型和参数作为BeanFactory#getBean的参数。

    @Lookup在以下方面非常有用：

    - 将一个原型范围(prototype-scoped)的Bean注入到一个单例Bean中（类似于Provider）
      - ? 解决单例Bean用了私有全局变量
    - 程序性地注入依赖关系

    还请注意，@Lookup是XML元素查找方法的Java等价物。

3. 使用@Lookup

    1. 将原型范围的Bean注入到一个Singleton Bean中

        如果我们碰巧决定要有一个原型Spring Bean(目的是每次都获取最新的实例)，那么我们几乎立刻就会面临这样的问题：我们的单例Spring Bean将如何访问这些原型Spring Bean？

        现在，Provider当然是一种方法，尽管@Lookup在某些方面更具有通用性。

        首先，让我们创建一个原型Bean，以后我们将把它注入单例Bean中：

        ```java
        @Component
        @Scope("prototype")
        public class SchoolNotification {
            // ... prototype-scoped state
        }
        ```

        而如果我们创建一个使用@Lookup的单例bean：默认services都是单例

        ```java
        @Component
        public class StudentServices {

            // ... member variables, etc.

            @Lookup
            public SchoolNotification getNotification() {
                return null;
            }

            // ... getters and setters
        }
        ```

        使用@Lookup，我们可以通过我们的单例Bean获得SchoolNotification的一个实例：

        ```java
        @Test
        public void whenLookupMethodCalled_thenNewInstanceReturned() {
            // ... initialize context
            StudentServices first = this.context.getBean(StudentServices.class);
            StudentServices second = this.context.getBean(StudentServices.class);
            
            assertEquals(first, second); 
            assertNotEquals(first.getNotification(), second.getNotification()); 
        }
        ```

        请注意，在StudentServices中，我们将getNotification方法留作了存根。

        > Srping通过 CGLIB（动态代理）类库实现 getNotification()

        这是因为Spring通过调用beanFactory.getBean(StudentNotification.class)重写了该方法，所以我们可以让它留空。

    2. 程序性地注入依赖关系

        更为强大的是，@Lookup允许我们以程序方式注入依赖关系，这是我们无法用Provider做到的。

        让我们用一些状态来增强StudentNotification：

        ```java
        @Component
        @Scope("prototype")
        public class SchoolNotification {
            @Autowired Grader grader;

            private String name;
            private Collection<Integer> marks;

            public SchoolNotification(String name) {
                // ... set fields
            }

            // ... getters and setters

            public String addMark(Integer mark) {
                this.marks.add(mark);
                return this.grader.grade(this.marks);
            }
        }
        ```

        现在，它依赖于一些Spring上下文，也依赖于我们将以程序方式提供的额外上下文。

        然后，我们可以为StudentServices添加一个方法，该方法接收学生数据并将其持久化：

        methodinjections/StudentServices.java

        在运行时，Spring将以同样的方式实现该方法，并有一些额外的技巧。

        首先，请注意，它可以调用复杂的构造函数以及注入其他Spring Bean，允许我们将SchoolNotification处理得更像一个Spring感知的方法。

        它通过调用beanFactory.getBean(SchoolNotification.class, name)来实现getSchoolNotification。

        其次，我们有时可以让@Lookup-annotated方法变成抽象的，就像上面的例子。

        使用抽象的方法比存根好看一些，但我们只能在不对周围的Bean进行组件扫描或@Bean管理时使用它：

        ```java
        @Test
        public void whenAbstractGetterMethodInjects_thenNewInstanceReturned() {
            // ... initialize context

            StudentServices services = context.getBean(StudentServices.class);    
            assertEquals("PASS", services.appendMark("Alex", 89));
            assertEquals("FAIL", services.appendMark("Bethany", 78));
            assertEquals("PASS", services.appendMark("Claire", 96));
        }
        ```

        通过这样的设置，我们可以向SchoolNotification添加Spring的依赖性以及方法的依赖性。

4. 局限性

    尽管@Lookup具有多功能性，但也有一些明显的限制：

    - 当周围的类（如Student）被组件扫描时，@Lookup标注的方法，如getNotification，必须是具体的。这是因为组件扫描跳过了抽象Bean。
    - 当周围的类是@Bean管理的时候，@Lookup-annotated方法就完全不起作用了。

    在这种情况下，如果我们需要将原型Bean注入到一个单例中，我们可以将Provider作为一个替代方案。

5. 总结

    在这篇文章中，我们学习了如何以及何时使用Spring的@Lookup注解，包括如何使用它将原型范围的Bean注入到单例Bean中，以及如何使用它来程序化地注入依赖关系。
