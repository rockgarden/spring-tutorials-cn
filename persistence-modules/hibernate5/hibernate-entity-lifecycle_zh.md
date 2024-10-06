# [休眠实体生命周期](https://www.baeldung.com/hibernate-entity-lifecycle)

1. 一览表

    每个Hibernate实体在框架内自然都有一个生命周期——它要么处于瞬态、托管状态，要么处于分离状态，要么处于删除状态。

    在概念和技术层面上了解这些状态对于能够正确使用Hibernate至关重要。

2. 助手方法

    在本教程中，我们将始终使用几种帮助方法：

    - HibernateLifecycleUtil.getManagedEntities(session) -我们将使用它从会话的内部存储中获取所有托管实体
    - DirtyDataInspector.getDirtyEntities() – 我们将使用此方法获取标记为“脏”的所有实体的列表
    - HibernateLifecycleUtil.queryCount(query) – 一种对嵌入式数据库进行count(*)查询的便捷方法

    上述所有帮助方法都是静态导入的，以提高可读性。您可以在本文末尾链接的GitHub项目中找到它们的实现。

3. 这一切都是关于持久的语境

    在进入实体生命周期的话题之前，首先，我们需要了解持久性上下文。

    简单地说，持久上下文位于客户端代码和数据存储之间。这是一个将持久数据转换为实体的暂存区域，随时可以由客户端代码读取和更改。

    从理论上讲，持久性上下文是[工作单元](https://martinfowler.com/eaaCatalog/unitOfWork.html)模式的实现。它跟踪所有加载的数据，跟踪该数据的更改，并负责最终在业务事务结束时将任何更改同步回数据库。

    JPA EntityManager 和 Hibernate 的 Session 是持久化上下文概念的一种实现。在本文中，我们将使用 Hibernate Session 来表示持久化上下文。

    Hibernate 实体生命周期状态解释了实体是如何与持久化上下文相关联的，我们接下来就会看到。

4. 管理实体

    托管实体是数据库表行的表示（尽管该行还不必存在于数据库中）。

    这由当前运行的会话管理，对它所做的每个更改都将自动跟踪并传播到数据库。

    会话要么从数据库中加载实体，要么重新附加分离的实体。我们将在第5节中讨论分离的实体。

    让我们观察一些代码来澄清。

    我们的示例应用程序定义了一个实体，即FootballPlayer类。启动时，我们将使用一些样本数据初始化数据存储：

    ```txt
    +-------------------+-------+
    | Name              |  ID   |
    +-------------------+-------+
    | Cristiano Ronaldo | 1     |
    | Lionel Messi      | 2     |
    | Gigi Buffon       | 3     |
    +-------------------+-------+
    ```

    假设我们想先更改Buffon的名字——我们想用他的全名Gianluigi Buffon而不是Gigi Buffon。

    首先，我们需要通过获得会议来开始我们的工作单元：

    `Session session = sessionFactory.openSession();`

    在服务器环境中，我们可能会通过上下文感知代理将会话注入到代码中。原则保持不变：我们需要一个会议来概括我们工作单位的业务交易。

    接下来，我们将指示我们的会话从持久存储中加载数据：

    ```java
    assertThat(getManagedEntities(session)).isEmpty();

    List<FootballPlayer> players = s.createQuery("from FootballPlayer").getResultList();

    assertThat(getManagedEntities(session)).size().isEqualTo(3);
    ```

    当我们首次获取会话时，其持久上下文存储是空的，正如我们的第一个断言语句所示。

    接下来，我们正在执行一个查询，该查询从数据库中检索数据，创建数据的实体表示，最后返回实体供我们使用。

    在内部，会话跟踪它在持久上下文存储中加载的所有实体。在我们的案例中，查询后，会的内部存储将包含3个实体。

    现在让我们更改Gigi的名字：

    ```java
    Transaction transaction = session.getTransaction();
    transaction.begin();

    FootballPlayer gigiBuffon = players.stream()
    .filter(p -> p.getId() == 3)
    .findFirst()
    .get();

    gigiBuffon.setName("Gianluigi Buffon");
    transaction.commit();

    assertThat(getDirtyEntities()).size().isEqualTo(1);
    assertThat(getDirtyEntities().get(0).getName()).isEqualTo("Gianluigi Buffon");
    ```

    1. 它是如何工作的？

        在调用事务commit（）或flush（）时，会话将从其跟踪列表中找到任何脏实体，并将状态同步到数据库。

        请注意，我们不需要调用任何方法来通知Session我们更改了实体中的某些东西——因为它是一个托管实体，所有更改都会自动传播到数据库。

        托管实体始终是一个持久实体——它必须有一个数据库标识符，即使数据库行表示尚未创建，即INSERT语句正在等待工作单元的结束。

        请参阅下面关于瞬态实体的章节。

5. 分离实体

    分离实体只是一个普通的实体 POJO，其身份值与数据库行相对应。它与托管实体的区别在于，任何持久化上下文都不再跟踪它。

    当用于加载实体的会话关闭时，或者当我们调用 Session.evict(entity) 或 Session.clear() 时，实体就会被分离。

    让我们在代码中看看：

    ```java
    FootballPlayer cr7 = session.get(FootballPlayer.class, 1L);

    assertThat(getManagedEntities(session)).size().isEqualTo(1);
    assertThat(getManagedEntities(session).get(0).getId()).isEqualTo(cr7.getId());

    session.evict(cr7);

    assertThat(getManagedEntities(session)).size().isEqualTo(0);
    ```

    我们的持久性上下文不会跟踪分离实体的变化：

    ```java
    cr7.setName("CR7");
    transaction.commit();

    assertThat(getDirtyEntities()).isEmpty();
    拷贝
    Session.merge(entity)/Session.update(entity)可以（重新）附加会话：

    FootballPlayer messi = session.get(FootballPlayer.class, 2L);

    session.evict(messi);
    messi.setName("Leo Messi");
    transaction.commit();

    assertThat(getDirtyEntities()).isEmpty();

    transaction = startTransaction(session);
    session.update(messi);
    transaction.commit();

    assertThat(getDirtyEntities()).size().isEqualTo(1);
    assertThat(getDirtyEntities().get(0).getName()).isEqualTo("Leo Messi");
    ```

    Session.merge（）和Session.update（）的参考，请参阅此处。

    1. 身份领域才是最重要的

        让我们来看看以下逻辑：

        ```java
        FootballPlayer gigi = new FootballPlayer();
        gigi.setId(3);
        gigi.setName("Gigi the Legend");
        session.update(gigi);
        ```

        在上面的例子中，我们通过其构造函数以通常的方式实例化了一个实体。我们已经用值填充了字段，并将身份设置为3，这对应于属于Gigi Buffon的持久数据的身份。调用update（）的效果与我们从另一个持久性上下文加载实体的效果完全相同。

        事实上，会话没有区分重新连接的实体的来源。

        在Web应用程序中，从HTML表单值中构建分离的实体是一个很常见的场景。

        就会话而言，分离实体只是一个普通实体，其身份值对应于持久性数据。

        请注意，上述示例只是用于演示目的。我们需要确切地知道我们在做什么。否则，如果我们只是在要更新的字段上设置值，而其余部分未触及（因此，实际上为空），我们最终可能会在整个实体中看到空值。

6. 暂存实体

    瞬态实体是指在持久化存储中没有任何表示，也不受任何会话管理的实体对象。

    瞬态实体的一个典型例子是通过构造函数实例化一个新实体。

    要使瞬态实体持久化，我们需要调用 Session.save(entity) 或 Session.saveOrUpdate(entity)：

    ```java
    FootballPlayer neymar = new FootballPlayer();
    neymar.setName("Neymar");
    session.save(neymar);

    assertThat(getManagedEntities(session)).size().isEqualTo(1);
    assertThat(neymar.getId()).isNotNull();

    int count = queryCount("select count(*) from Football_Player where name='Neymar'");

    assertThat(count).isEqualTo(0);

    transaction.commit();
    count = queryCount("select count(*) from Football_Player where name='Neymar'");

    assertThat(count).isEqualTo(1);
    ```

    一旦我们执行Session.save（实体），实体就会被分配一个身份值，并由会话管理。然而，它可能尚未在数据库中可用，因为INSERT操作可能会延迟到工作单元结束。

7. 删除的实体

    如果已调用Session.delete（实体），并且会话已将实体标记为要删除，则实体处于删除（已删除）状态。DELETE命令本身可能会在工作单元结束时发出。

    让我们在以下代码中看到它：

    ```java
    session.delete(neymar);
    assertThat(getManagedEntities(session).get(0).getStatus()).isEqualTo(Status.DELETED);
    ```

    然而，请注意，该实体一直处于持久的上下文存储中，直到工作单元结束。

8. 结论

    持久性上下文的概念是理解休眠实体生命周期的核心。我们通过查看显示每个状态的代码示例来澄清生命周期。
