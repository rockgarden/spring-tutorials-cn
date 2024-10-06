# [使用JPA返回自动生成的ID](https://www.baeldung.com/jpa-get-auto-generated-id)

1. 介绍

    在本教程中，我们将讨论如何使用JPA处理自动生成的ID。在我们研究一个实际的例子之前，我们必须了解两个关键概念，即生命周期和id生成策略。

2. 实体生命周期和ID生成

    每个实体在其生命周期内都有四种可能的状态。这些状态是新的、管理的、分离的和被移除的。我们将专注于新的和管理的州。在创建对象时，实体处于新状态。因此，EntityManager不知道这个对象。在EntityManager上调用持久方法，对象从新状态过渡到托管状态。此方法需要一个活跃的事务。

    JPA定义了ID生成的四种策略。我们可以将这四种策略分为两类：

    - ID是预先分配的，在提交前可提供给实体经理
    - 交易提交后分配ID

    有关每个id生成策略的更多详细信息，请参阅我们的文章《[JPA何时设置主密钥](https://www.baeldung.com/jpa-strategies-when-set-primary-key)》。

3. 问题陈述

    返回对象的id可能是一项繁琐的任务。我们需要了解上一节中提到的原则，以避免出现问题。根据JPA配置，服务可能会返回id等于零（或空）的对象。重点将放在服务类的实施上，以及不同的修改如何为我们提供解决方案。

    我们将创建一个Maven模块，其实现是JPA规范和Hibernate。为了简单起见，我们将使用H2内存数据库。

    让我们从创建一个域实体并将其映射到数据库表开始。在本例中，我们将创建一个具有一些基本属性的用户实体：

    ```java
    @Entity
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;
        private String username;
        private String password;

        //...
    }
    ````

    在域类之后，我们将创建一个用户服务类。这个简单的服务将引用实体管理器和将用户对象保存到数据库的方法：

    ```java
    public class UserService {
        EntityManager entityManager;

        public UserService(EntityManager entityManager) {
            this.entityManager = entityManager;
        }
    
        @Transactional
        public long saveUser(User user){
            entityManager.persist(user);
            return user.getId();
        }
    }
    ```

    这个设置是我们之前提到的一个常见的陷阱。我们可以通过测试证明saveUser方法的返回值为零：

    ```java
    @Test
    public void whenNewUserIsPersisted_thenEntityHasNoId() {
        User user = new User();
        user.setUsername("test");
        user.setPassword(UUID.randomUUID().toString());

        long index = service.saveUser(user);
    
        Assert.assertEquals(0L, index);
    }
    ```

    在接下来的章节中，我们将退后一步，了解为什么会发生这种情况，以及如何解决它。

4. 手动Transaction控制

    创建对象后，我们的用户实体处于新状态。在saveUser方法中调用持久方法后，实体状态更改为托管。我们从总结部分记得，托管对象在事务提交后获得一个ID。由于saveUser方法仍在运行，因此@Transactional注释创建的事务尚未提交。当saveUser完成执行时，我们的托管实体会获得一个ID。

    一个可能的解决方案是手动调用EntityManager上的刷新方法。另一方面，我们可以手动控制交易，并保证我们的方法正确返回id。我们可以用实体经理来做到这一点：

    ```java
    @Test
    public void whenTransactionIsControlled_thenEntityHasId() {
        User user = new User();
        user.setUsername("test");
        user.setPassword(UUID.randomUUID().toString());

        entityManager.getTransaction().begin();
        long index = service.saveUser(user);
        entityManager.getTransaction().commit();
        
        Assert.assertEquals(2L, index);
    }
    ```

5. 使用Id生成策略

    到目前为止，我们使用了第二个类别，其中id分配发生在事务提交后。预分配策略可以在事务提交前向我们提供ID，因为它们在内存中保留了少量ID。此选项并不总是可以实现的，因为并非所有数据库引擎都支持所有生成策略。将策略更改为GenerationType.SEQUENCE可以解决我们的问题。此策略使用数据库序列，而不是像GenerationType.IDENTITY中那样使用自动增量列。

    为了更改策略，我们编辑了我们的域实体类：

    ```java
    @Entity
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE)
        private long id;

        //...
    }
    ```

6. 结论

    在本文中，我们介绍了JPA中的id生成技术。首先，我们稍微回顾了id生成最重要的关键方面。然后，我们涵盖了JPA中使用的常见配置，以及它们的优缺点。
