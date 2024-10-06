# [使用Hibernate删除对象](https://www.baeldung.com/delete-with-hibernate)

1. 一览表

    作为一个功能齐全的ORM框架，Hibernate负责持久对象（实体）的生命周期管理，包括读取、保存、更新和删除等CRUD操作。

    在本文中，我们探讨了使用Hibernate从数据库中删除对象的各种方式，并解释了可能出现的常见问题和陷阱。

    我们使用JPA，只退后一步，将Hibernate原生API用于那些在JPA中未标准化的功能。

2. 删除对象的不同方式

    在以下情况下，对象可能会被删除：

    - 通过使用EntityManager.remove
    - 当从其他实体实例中级联删除时
    - 当应用孤儿Removal时
    - 通过执行删除JPQL语句
    - 通过执行本地查询
    - 通过应用软删除技术（根据@Where子句中的条件过滤软删除的实体）

    在文章的其余部分，我们详细地研究了这些要点。

3. 使用实体管理器删除

    使用EntityManager删除是删除实体实例的最直接方法：

    ```java
    Foo foo = new Foo("foo");
    entityManager.persist(foo);
    flushAndClear();

    foo = entityManager.find(Foo.class, foo.getId());
    assertThat(foo, notNullValue());
    entityManager.remove(foo);
    flushAndClear();

    assertThat(entityManager.find(Foo.class, foo.getId()), nullValue());
    ```

    在本文中的示例中，我们使用一种帮助方法在需要时刷新和清除持久性上下文：

    ```java
    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
    ```

    调用EntityManager.remove方法后，提供的实例过渡到已删除状态，并在下一次刷新时从数据库中删除相关。

    请注意，如果对已删除的实例应用了PERSIST操作，则会重新存在。一个常见的错误是忽略PERSIST操作已应用于已删除的实例（通常，因为它在刷新时从另一个实例级联），因为[JPA规范](http://download.oracle.com/otndocs/jcp/persistence-2_1-fr-eval-spec/index.html)第3.2.2节规定，在这种情况下，此类实例必须再次持续存在。

    我们通过定义从Foo到Bar的@ManyToOne关联来说明这一点：

    ```java
    @Entity
    public class Foo {
        @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        private Bar bar;

        // other mappings, getters and setters
    }
    ```

    当我们删除由Foo实例引用的Bar实例时，该实例也在持久性上下文中加载，Bar实例将不会从数据库中删除：

    ```java
    Bar bar = new Bar("bar");
    Foo foo = new Foo("foo");
    foo.setBar(bar);
    entityManager.persist(foo);
    flushAndClear();

    foo = entityManager.find(Foo.class, foo.getId());
    bar = entityManager.find(Bar.class, bar.getId());
    entityManager.remove(bar);
    flushAndClear();

    bar = entityManager.find(Bar.class, bar.getId());
    assertThat(bar, notNullValue());

    foo = entityManager.find(Foo.class, foo.getId());
    foo.setBar(null);
    entityManager.remove(bar);
    flushAndClear();

    assertThat(entityManager.find(Bar.class, bar.getId()), nullValue());
    ```

    如果删除的Bar由Foo引用，PERSIST操作将从Foo级联到Bar，因为关联用cascade = CascadeType.ALL标记，并且删除是计划外的。为了验证这种情况是否正在发生，我们可能会为org.hibernate软件包启用跟踪日志级别，并搜索取消计划实体删除等条目。

4. 级联删除

    当父实体被删除时，删除可以级联到子实体：

    ```java
    Bar bar = new Bar("bar");
    Foo foo = new Foo("foo");
    foo.setBar(bar);
    entityManager.persist(foo);
    flushAndClear();

    foo = entityManager.find(Foo.class, foo.getId());
    entityManager.remove(foo);
    flushAndClear();

    assertThat(entityManager.find(Foo.class, foo.getId()), nullValue());
    assertThat(entityManager.find(Bar.class, bar.getId()), nullValue());
    ```

    这里的条被删除，因为删除是从foo级联的，因为关联被声明为从Foo到Bar的所有生命周期操作级联。

    请注意，在@ManyToMany关联中级联REMOVE操作几乎总是一个错误，因为这将触发删除可能与其他父实例关联的子实例。这也适用于CascadeType.ALL，因为它意味着所有操作都要级联，包括REMOVE操作。

5. 清除Orphans

    orphanRemoval指令声明，当关联的实体实例与父实例解除关联时，或等效地在父实体被删除时，它们将被删除。

    我们通过定义从Bar到Baz的这种关联来展示这一点：

    ```java
    @Entity
    public class Bar {
        @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
        private List<Baz> bazList = new ArrayList<>();

        // other mappings, getters and setters
    }
    ```

    然后，当Baz实例从父条实例列表中删除时，它会自动删除：

    ```java
    Bar bar = new Bar("bar");
    Baz baz = new Baz("baz");
    bar.getBazList().add(baz);
    entityManager.persist(bar);
    flushAndClear();

    bar = entityManager.find(Bar.class, bar.getId());
    baz = bar.getBazList().get(0);
    bar.getBazList().remove(baz);
    flushAndClear();

    assertThat(entityManager.find(Baz.class, baz.getId()), nullValue());
    ```

    orphanRemoval操作的语义与直接应用于受影响的子实例的REMOVE操作完全相似，这意味着REMOVE操作进一步级联到嵌套的子实例。因此，您必须确保没有其他实例引用已删除的实例（否则它们会重新存在）。

6. 使用JPQL语句删除

    Hibernate支持DML风格的删除操作：

    ```java
    Foo foo = new Foo("foo");
    entityManager.persist(foo);
    flushAndClear();

    entityManager.createQuery("delete from Foo where id = :id")
    .setParameter("id", foo.getId())
    .executeUpdate();

    assertThat(entityManager.find(Foo.class, foo.getId()), nullValue());
    ```

    需要注意的是，DML风格的JPQL语句不影响已加载到持久性上下文中的实体实例的状态或生命周期，因此建议在加载受影响的实体之前执行它们。

7. 使用本地查询删除

    有时我们需要回到本机查询，以实现Hibernate不支持或特定于数据库供应商的东西。我们还可能通过本机查询删除数据库中的数据：

    ```java
    Foo foo = new Foo("foo");
    entityManager.persist(foo);
    flushAndClear();

    entityManager.createNativeQuery("delete from FOO where ID = :id")
    .setParameter("id", foo.getId())
    .executeUpdate();

    assertThat(entityManager.find(Foo.class, foo.getId()), nullValue());
    ```

    同样的建议适用于原生查询，适用于JPA DML风格语句，即原生查询不影响在执行查询之前加载到持久性上下文中的实体实例的状态或生命周期。

8. 软删除

    出于审计目的和保存历史记录，通常不希望从数据库中删除数据。在这种情况下，我们可能会应用一种叫做软删除的技术。基本上，我们只是将一行标记为已删除，并在检索数据时将其过滤掉。

    为了避免所有读取软删除实体的查询中的where子句中出现大量冗余条件，Hibernate提供了@Where注释，该注释可以放置在实体上，其中包含自动添加到为该实体生成的SQL查询中的SQL片段。

    为了证明这一点，我们将@Where注释和一个名为DELETED的列添加到Foo实体中：

    ```java
    @Entity
    @Where(clause = "DELETED = 0")
    public class Foo {
        // other mappings

        @Column(name = "DELETED")
        private Integer deleted = 0;
        
        // getters and setters

        public void setDeleted() {
            this.deleted = 1;
        }
    }
    ```

    以下测试确认一切正常：

    ```java
    Foo foo = new Foo("foo");
    entityManager.persist(foo);
    flushAndClear();

    foo = entityManager.find(Foo.class, foo.getId());
    foo.setDeleted();
    flushAndClear();

    assertThat(entityManager.find(Foo.class, foo.getId()), nullValue());
    ```

9. 结论

    在本文中，我们研究了使用Hibernate删除数据的不同方式。我们解释了基本概念和一些最佳实践。我们还演示了如何使用Hibernate轻松实现软删除。
