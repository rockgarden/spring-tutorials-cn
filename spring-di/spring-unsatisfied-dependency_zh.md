# [Unsatisfied Dependency in Spring](https://www.baeldung.com/spring-unsatisfied-dependency)

1. 概述

    在这个快速教程中，我们将解释Spring的UnsatisfiedDependencyException，它的原因以及如何避免它。

2. 不满意的依赖性Exception的原因

    顾名思义，当某些Bean或属性的依赖性没有得到满足时，就会抛出UnsatisfiedDependencyException。

    这可能发生在Spring应用程序试图连接一个Bean而无法解决其中一个强制性的依赖关系时。

3. 应用实例

    假设我们有一个服务类PurchaseDeptService，它依赖于InventoryRepository：

    ```java
    @Service
    public class PurchaseDeptService {
        public PurchaseDeptService(InventoryRepository repository) {
            this.repository = repository;
        }
    }

    public interface InventoryRepository {
    }

    @Repository
    public class ShoeRepository implements InventoryRepository {
    }

    @SpringBootApplication
    public class SpringDependenciesExampleApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringDependenciesExampleApplication.class, args);
        }
    }
    ```

    现在，我们假设所有这些类都位于同一个名为com.baeldung.dependency.exception.app的包中。

    当我们运行这个Spring Boot应用程序时，一切正常。

    让我们看看如果我们跳过一个配置步骤，会遇到什么样的问题。

4. 组件注解缺失

    现在让我们从ShoeRepository类中删除@Repository注解：

    `public class ShoeRepository implements InventoryRepository {}`

    当我们再次启动我们的应用程序时，我们会看到以下错误信息： `UnsatisfiedDependencyException: Error creating bean with name ‘purchaseDeptService': Unsatisfied dependency expressed through constructor parameter 0`

    Spring没有被指示连接一个ShoeRepository Bean并将其添加到应用程序上下文中，所以它无法注入它并抛出了这个异常。

    在ShoeRepository上添加@Repository注解可以解决这个问题。

5. 包未被扫描

    现在让我们把我们的ShoeRepository（连同InventoryRepository）放入一个单独的包，名为com.baeldung.dependency.exception.repository。

    再一次，当我们运行我们的应用程序时，它抛出了UnsatisfiedDependencyException。

    为了解决这个问题，我们可以在父包上配置包扫描，并确保所有相关的类都包括在内：

    ```java
    @SpringBootApplication
    @ComponentScan(basePackages = {"com.baeldung.dependency.exception"})
    public class SpringDependenciesExampleApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringDependenciesExampleApplication.class, args);
        }
    }
    ```

6. 非唯一性的依赖解决

    假设我们添加另一个InventoryRepository的实现--DressRepository：

    ```java
    @Repository
    public class DressRepository implements InventoryRepository {
    }
    ```

    现在当我们运行我们的应用程序时，它将再次抛出UnsatisfiedDependencyException。

    然而，这次的情况有所不同。正如它所发生的，当有一个以上的bean满足它的时候，依赖性就不能被解决。

    为了解决这个问题，我们可能要添加@Qualifier来区分存储库：

    ```java
    @Qualifier("dresses")
    @Repository
    public class DressRepository implements InventoryRepository {
    }

    @Qualifier("shoes")
    @Repository
    public class ShoeRepository implements InventoryRepository {
    }
    ```

    另外，我们要给PurchaseDeptService构造函数依赖性添加一个限定词：

    ```java
    public PurchaseDeptService(@Qualifier("dresses") InventoryRepository repository) {
        this.repository = repository;
    }
    ```

    这将使DressRepository成为唯一可行的选择，Spring将把它注入PurchaseDeptService中。

7. 结语

    在这篇文章中，我们看到了遇到[UnsatisfiedDependencyException](https://www.baeldung.com/spring-beancreationexception)的几种最常见的情况，然后我们学会了如何解决这些问题。

    我们也有一个关于Spring BeanCreationException的更一般的教程。
