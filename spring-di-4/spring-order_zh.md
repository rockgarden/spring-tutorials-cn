# [@Order in Spring](http://www.baeldung.com/spring-order)

1. 概述

    在本教程中，我们将学习Spring的[@Order](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/annotation/Order.html)注解。@Order注解定义了被注解的组件或bean的排序顺序。

    它有一个可选的值参数，决定了组件的顺序；默认值是Ordered.LOWEST_PRECEDENCE。这标志着该组件在所有其他排序的组件中具有最低的优先级。

    同样地，Ordered.HIGHEST_PRECEDENCE的值可以用来覆盖组件中的最高优先级。

2. 何时使用@Order

    在Spring 4.0之前，@Order注解只用于AspectJ的执行顺序。这意味着最高顺序的建议将首先运行。

    从Spring 4.0开始，它支持对注入到集合中的组件进行排序。因此，Spring会根据顺序值来注入相同类型的自动连接的Bean。

    让我们通过一个简单的例子来探讨一下。

3. 如何使用@Order

    首先，让我们用相关的接口和类来设置我们的项目。

    1. 接口的创建

        让我们创建Rating接口，决定产品的等级。

        ```java
        public interface Rating {
            int getRating();
        }
        ```

    2. 组件的创建

        最后，让我们创建三个组件，定义一些产品的评级。

        ```java
        @Component
        @Order(1)
        public class Excellent implements Rating {
            @Override
            public int getRating() {
                return 1;
            }
        }

        @Component
        @Order(2)
        public class Good implements Rating {
            @Override
            public int getRating() {
                return 2;
            }
        }

        @Component
        @Order(Ordered.LOWEST_PRECEDENCE)
        public class Average implements Rating {
            @Override
            public int getRating() {
                return 3;
            }
        }
        ```

        请注意，Average类的优先级是最低的，因为它的值被覆盖了。

4. 测试我们的例子

    到目前为止，我们已经创建了所有需要的组件和接口来测试@Order注解。现在，让我们来测试一下，以确认它是否如预期那样工作。

    ```java
    public class RatingRetrieverUnitTest {
        
        @Autowired
        private List<Rating> ratings;
        
        @Test
        public void givenOrder_whenInjected_thenByOrderValue() {
            assertThat(ratings.get(0).getRating(), is(equalTo(1)));
            assertThat(ratings.get(1).getRating(), is(equalTo(2)));
            assertThat(ratings.get(2).getRating(), is(equalTo(3)));
        }
    }
    ```

5. 总结

    在这篇短文中，我们已经了解了 @Order 注解。我们可以在各种用例中找到 @Order 的应用--在这些用例中，自动连线组件的排序非常重要。其中一个例子就是 Spring 的请求过滤器。

    由于 @Order 对注入优先级的影响，它似乎也会影响单例启动顺序。但与此相反，依赖关系和 @DependsOn 声明决定了单例启动顺序。
