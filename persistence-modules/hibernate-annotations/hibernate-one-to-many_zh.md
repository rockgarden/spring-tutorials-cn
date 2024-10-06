# [Hibernate一对多注释教程](https://www.baeldung.com/hibernate-one-to-many)

1. 介绍

    这个快速的Hibernate教程将带我们了解使用JPA注释的一对多映射示例，JPA注释是XML的替代方案。

    我们还将了解什么是双向关系，它们如何产生不一致，以及所有权的想法如何提供帮助。

2. 描述

    简单地说，一对多映射意味着表中的一行映射到另一个表中的多行。

    在这个例子中，我们将实现一个购物车系统，其中每个购物车都有一个表格，每个项目都有另一个表格。一个购物车可以包含许多项目，所以在这里我们有一个一对多映射。

    这在数据库级别的工作方式是，我们在购物车表中将cart_id作为主键，并在项目中将cart_id作为外键。

    我们用代码来做的方式是使用@OneToMany。

    让我们以反映数据库中关系的方式将购物车类映射到项目对象的集合：

    ```java
    public class Cart {

        //...     
    
        @OneToMany(mappedBy="cart")
        private Set<Item> items;

        //...
    }
    ```

    我们还可以使用@ManyToOne在每个项目中添加对购物车的引用，使其成为双向关系。双向意味着我们能够从购物车中访问项目，也可以从项目中访问购物车。

    mappedBy属性是我们用来告诉Hibernate我们使用哪个变量来表示子类中的父类。

    为了开发实现一对多关联的Hibernate应用程序示例，使用以下技术和库：

    - JDK 1.8或更高版本
    - Hibernate 6
    - Maven 3或更高版本
    - H2数据库

3. 设置

    1. 数据库设置

        我们将使用Hibernate从域模型管理我们的模式。换句话说，我们不需要提供SQL语句来创建实体之间的各种表和关系。因此，让我们继续创建Hibernate示例项目。

    2. Maven附属机构

        让我们从将Hibernate和H2驱动程序依赖项添加到我们的pom.xml文件中开始。Hibernate依赖项使用JBoss日志记录，并自动添加为传递依赖项：

        ```xml
        <dependency>
            <groupId>org.hibernate.orm</groupId>
            <artifactId>hibernate-core</artifactId>
            <version>6.5.2.Final</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.224</version>
        </dependency>
        ```

        请访问Maven中央存储库，了解最新版本的Hibernate和H2依赖项。

    3. Hibernate会话工厂

        接下来，让我们为我们的数据库交互创建Hibernate SessionFactory：

        ```java
        public static SessionFactory getSessionFactory() {

            ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
            .applySettings(dbSettings())
            .build();

            Metadata metadata = new MetadataSources(serviceRegistry)
            .addAnnotatedClass(Cart.class)
            // other domain classes
            .buildMetadata();

            return metadata.buildSessionFactory();
        }

        private static Map<String, Object> dbSettings() {
            // return Hibernate settings
        }
        ```

4. Models

    映射相关配置将使用模型类中的JPA注释完成：

    ```java
    @Entity
    @Table(name="CART")
    public class Cart {

        //...

        @OneToMany(mappedBy="cart")
        private Set<Item> items;
    
        // getters and setters
    }
    ```

    请注意，@OneToMany注释用于定义Item类中的属性，该属性将用于映射themappedBy变量。这就是为什么我们在项目类中有一个名为“cart”的属性：

    ```java
    @Entity
    @Table(name="ITEMS")
    public class Item {

        //...
        @ManyToOne
        @JoinColumn(name="cart_id", nullable=false)
        private Cart cart;

        public Item() {}
        
        // getters and setters
    }
    ```

    同样重要的是要注意，@ManyToOne注释与购物车类变量相关联。@JoinColumn注释引用映射的列。

5. 在行动中

    在测试程序中，我们正在创建一个带有main（）方法的类，以获取Hibernate Session，并将模型对象保存到实现一对多关联的数据库中：

    ```java
    sessionFactory = HibernateAnnotationUtil.getSessionFactory();
    session = sessionFactory.getCurrentSession();
    LOGGER.info("Session created");

    tx = session.beginTransaction();

    session.save(cart);
    session.save(item1);
    session.save(item2);

    tx.commit();
    LOGGER.info("Cart ID={}", cart.getId());
    LOGGER.info("item1 ID={}, Foreign Key Cart ID={}", item1.getId(), item1.getCart().getId());
    LOGGER.info("item2 ID={}, Foreign Key Cart ID={}", item2.getId(), item2.getCart().getId());
    ```

    这是我们测试程序的输出：

    ```log
    Session created
    Hibernate: insert into CART values ()
    Hibernate: insert into ITEMS (cart_id)
    values (?)
    Hibernate: insert into ITEMS (cart_id)
    values (?)
    Cart ID=7
    item1 ID=11, Foreign Key Cart ID=7
    item2 ID=12, Foreign Key Cart ID=7
    Closing SessionFactory
    ```

6. @ManyToOne注释

    正如我们在第2节中看到的那样，我们可以使用@ManyToOne注释来指定多对一的关系。Amany-to-one映射意味着此实体的许多实例被映射到另一个实体的一个实例——一个购物车中的许多项目。

    @ManyToOne注释也允许我们创建双向关系。我们将在接下来的几个小节中详细介绍这一点。

    1. 不一致和所有权

        现在，如果购物车引用了项目，但项目没有反过来引用购物车，我们的关系将是单向的。这些物体也会具有自然的一致性。

        然而，就我们而言，这种关系是双向的，带来了不一致的可能性。

        让我们想象一下，开发人员想要将项目1添加到购物车1实例中，将项目2添加到购物车2实例中，但犯了一个错误，使购物车2和项目2之间的引用变得不一致：

        ```java
        Cart cart1 = new Cart();
        Cart cart2 = new Cart();

        Item item1 = new Item(cart1);
        Item item2 = new Item(cart2);
        Set<Item> itemsSet = new HashSet<Item>();
        itemsSet.add(item1);
        itemsSet.add(item2);
        cart1.setItems(itemsSet); // wrong!
        ```

        如上所示，项目2引用了购物车2，而购物车2不引用项目2，这很糟糕。

        Hibernate应该如何将项目2保存到数据库中？项目2的外键会参考cart1还是cart2？

        我们使用关系的拥有方的想法来解决这种歧义；属于拥有方的引用优先，并保存到数据库中。

    2. 作为拥有方的项目

        正如[JPA规范](http://download.oracle.com/otndocs/jcp/persistence-2.0-fr-eval-oth-JSpec/)第2.9节所述，将多对一的一方标记为拥有方是一种好的做法。

        换句话说，项目是拥有方，购物车是反向方，这正是我们之前所做的。

        那么，我们是如何实现这个的呢？

        通过在Cart类中包含mappedBy属性，我们将其标记为反面。

        与此同时，我们还用@ManyToOne注释Item.cart字段，使Item成为拥有方。

        回到我们的“不一致”示例，现在Hibernate知道项目2的引用更重要，并将项目2的引用保存到数据库。

        让我们检查一下结果：

        ```log
        item1 ID=1, Foreign Key Cart ID=1
        item2 ID=2, Foreign Key Cart ID=2
        ```

        虽然购物车在我们的片段中引用了项目2，但项目2对购物车2的引用保存在数据库中。

    3. 购物车作为拥有方

        也可以将一对多侧标记为拥有方，将多对一侧标记为反向侧。

        虽然这不是一种推荐的做法，但让我们继续尝试一下。

        下面的代码片段显示了作为拥有方的一对多方的实现：

        ```java
        public class ItemOIO {

            //  ...
            @ManyToOne
            @JoinColumn(name = "cart_id", insertable = false, updatable = false)
            private CartOIO cart;
            //..
        }

        public class CartOIO {

            //..  
            @OneToMany
            @JoinColumn(name = "cart_id") // we need to duplicate the physical information
            private Set<ItemOIO> items;
            //..
        }
        ```

        请注意，我们如何删除mappedBy元素，并将多对一@JoinColumn设置为可插入和可更新为false。

        如果我们运行相同的代码，结果将相反：

        ```log
        item1 ID=1, Foreign Key Cart ID=1
        item2 ID=2, Foreign Key Cart ID=1
        ```

        如上所示，现在项目2属于购物车。

7. 结论

    我们已经看到，使用JPA注释实现与Hibernate ORM和H2数据库的一对多关系是多么容易。

    此外，我们了解了双向关系以及如何实现拥有方的概念。
