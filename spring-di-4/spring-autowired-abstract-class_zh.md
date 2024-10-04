# [在抽象类中使用@Autowired](https://www.baeldung.com/spring-autowired-abstract-class)

1. 介绍

    在本快速教程中，我们将解释如何在[抽象类](https://www.baeldung.com/java-abstract-class)中使用@Autowired 注释。

    我们将@Autowired应用于抽象类，并专注于我们应该考虑的要点。

2. Setter注射

    我们可以在设置器方法上使用@Autowired：

    ```java
    public abstract class BallService {

        private LogRepository logRepository;

        @Autowired
        public final void setLogRepository(LogRepository logRepository) {
            this.logRepository = logRepository;
        }
    }
    ```

    当我们在设置器方法上使用@Autowired时，我们应该使用[最终关键字](https://www.baeldung.com/java-final)，以便子类无法覆盖设置器方法。否则，注释将无法像我们预期的那样工作。

3. 构造器注入

    我们不能在抽象类的构造函数上使用@Autowired。

    Spring不会在抽象类的构造函数上评估@Autowired注释。子类应该为超级构造函数提供必要的参数。

    相反，我们应该在子类的构造函数上使用@Autowired：

    ```java
    public abstract class BallService {

        private RuleRepository ruleRepository;

        public BallService(RuleRepository ruleRepository) {
            this.ruleRepository = ruleRepository;
        }
    }

    @Component
    public class BasketballService extends BallService {

        @Autowired
        public BasketballService(RuleRepository ruleRepository) {
            super(ruleRepository);
        }
    }
    ```

4. 小抄

    让我们总结一下一些需要记住的规则。

    首先，抽象类不是组件扫描的，因为如果没有具体的子类，它就无法实例化。

    其次，在抽象类中可以进行设置器注入，但如果我们不为设置器方法使用最终关键字，则存在风险。如果子类覆盖了设置器方法，应用程序可能不稳定。

    第三，由于Spring不支持抽象类中的构造函数注入，我们通常应该让具体子类提供构造函数参数。这意味着我们需要在具体子类中依赖构造函数注入。

    最后，对所需依赖项使用构造函数注入，对可选依赖项使用设置器注入是一个很好的[经验法则](https://docs.spring.io/spring/docs/5.1.x/spring-framework-reference/core.html#beans-factory-collaborators)。然而，正如我们在抽象类的一些细微差别中看到的那样，构造函数注入在这里通常更有利。

    因此，我们可以说，一个具体的子类决定了其抽象父类如何获得其依赖关系。只要弹簧将子类连接起来，弹簧就会进行注入。

5. 结论

    在本文中，我们练习了在抽象类中使用@Autowired，并解释了几个重要的要点。
