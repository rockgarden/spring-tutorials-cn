# [JPA/Hibernate级联类型概述](https://www.baeldung.com/jpa-cascade-types)

1. 一览表

    在本教程中，我们将讨论什么是JPA/Hibernate中的级联。然后，我们将涵盖各种可用的级联类型及其语义。

2. 什么是级联？

    实体关系通常取决于另一个实体的存在，例如人-地址关系。没有人，地址实体就没有任何其自身意义。当我们删除Person实体时，我们的地址实体也应该被删除。

    级联是实现这一目标的方法。当我们对目标实体执行一些操作时，相同的操作将应用于关联实体。

    1. JPA级联类型

        所有特定于JPA的级联操作都由包含条目的jakarta.persistence.CascadeType枚举表示：

        - ALL
        - PERSIST
        - MERGE
        - REMOVE
        - REFRESH
        - DETACH

    2. Hibernate级联类型

        Hibernate支持三种额外的级联类型以及JPA指定的级联类型。这些特定于Hibernate的级联类型可在org.hibernate.annotations.CascadeType中找到：

        - REPLICATE
        - SAVE_UPDATE
        - LOCK

3. 级联类型之间的区别

    1. CascadeType.ALL

        CascadeType.ALL将所有操作（包括特定于Hibernate的操作）从父实体传播到子实体。

        让我们用一个例子来看看它：

        ```java
        @Entity
        public class Person {
            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            private int id;
            private String name;
            @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
            private List<Address> addresses;
        }
        ```

        请注意，在OneToMany关联中，我们在注释中提到了级联类型。

        现在让我们看看关联的实体地址：

        ```java
        @Entity
        public class Address {
            @Id
            @GeneratedValue(strategy = GenerationType.AUTO)
            private int id;
            private String street;
            private int houseNumber;
            private String city;
            private int zipCode;
            @ManyToOne(fetch = FetchType.LAZY)
            private Person person;
        }
        ```

    2. CascadeType.PERSIST

        持久操作使瞬态实例持久化。级联类型PERSIST将持久操作从父实体传播到子实体。当我们保存人员实体时，地址实体也将被保存。

        让我们来看看持久操作的测试用例：

        ```java
        @Test
        public void whenParentSavedThenChildSaved() {
            Person person = new Person();
            Address address = new Address();
            address.setPerson(person);
            person.setAddresses(Arrays.asList(address));
            session.persist(person);
            session.flush();
            session.clear();
        }
        ```

        当我们运行上述测试用例时，我们将看到以下SQL：

        ```log
        Hibernate: insert into Person (name, id) values (?, ?)
        Hibernate: insert into Address (
            city, houseNumber, person_id, street, zipCode, id) values (?, ?, ?, ?, ?, ?)
        ```

    3. CascadeType.MERGE

        合并操作将给定对象的状态复制到具有相同标识符的持久对象上。CascadeType.MERGE将合并操作从父实体传播到子实体。

        让我们来测试一下合并操作：

        ```java
        @Test
        public void whenParentSavedThenMerged() {
            int addressId;
            Person person = buildPerson("devender");
            Address address = buildAddress(person);
            person.setAddresses(Arrays.asList(address));
            session.persist(person);
            session.flush();
            addressId = address.getId();
            session.clear();

            Address savedAddressEntity = session.find(Address.class, addressId);
            Person savedPersonEntity = savedAddressEntity.getPerson();
            savedPersonEntity.setName("devender kumar");
            savedAddressEntity.setHouseNumber(24);
            session.merge(savedPersonEntity);
            session.flush();
        }
        ```

        当我们运行测试用例时，合并操作会生成以下SQL：

        ```log
        Hibernate: select address0_.id as id1_0_0_, address0_.city as city2_0_0_, address0_.houseNumber as houseNum3_0_0_, address0_.person_id as person_i6_0_0_, address0_.street as street4_0_0_, address0_.zipCode as zipCode5_0_0_ from Address address0_ where address0_.id=?
        Hibernate: select person0_.id as id1_1_0_, person0_.name as name2_1_0_ from Person person0_ where person0_.id=?
        Hibernate: update Address set city=?, houseNumber=?, person_id=?, street=?, zipCode=? where id=?
        Hibernate: update Person set name=? where id=?
        ```

        在这里，我们可以看到合并操作首先加载地址和人员实体，然后作为CascadeType.MERGE的结果更新两者。

    4. CascadeType.REMOVE

        顾名思义，删除操作从数据库和持久上下文中删除与实体对应的行。

        CascadeType.REMOVE将删除操作从父实体传播到子实体。与JPA的CascadeType.REMOVE类似，我们有CascadeType.DELETE，它特定于Hibernate。两者之间没有区别。

        现在是时候测试CascadeType了。删除：

        ```java
        @Test
        public void whenParentRemovedThenChildRemoved() {
            int personId;
            Person person = buildPerson("devender");
            Address address = buildAddress(person);
            person.setAddresses(Arrays.asList(address));
            session.persist(person);
            session.flush();
            personId = person.getId();
            session.clear();

            Person savedPersonEntity = session.find(Person.class, personId);
            session.remove(savedPersonEntity);
            session.flush();
        }
        ```

        当我们运行测试用例时，我们将看到以下SQL：

        ```log
        Hibernate: delete from Address where id=?
        Hibernate: delete from Person where id=?
        ```

        由于CascadeType.REMOVE，与该人关联的地址也被删除了。

    5. CascadeType.DETACH

        分离操作将实体从持久上下文中删除。当我们使用CascadeType.DETACH时，子实体也将从持久上下文中删除。

        让我们看看它的行动：

        ```java
        @Test
        public void whenParentDetachedThenChildDetached() {
            Person person = buildPerson("devender");
            Address address = buildAddress(person);
            person.setAddresses(Arrays.asList(address));
            session.persist(person);
            session.flush();
            
            assertThat(session.contains(person)).isTrue();
            assertThat(session.contains(address)).isTrue();

            session.detach(person);
            assertThat(session.contains(person)).isFalse();
            assertThat(session.contains(address)).isFalse();
        }
        ```

        在这里，我们可以看到，在将人分出后，人和地址都不存在于持久的上下文中。

    6. CascadeType.LOCK

        不直观地，CascadeType.LOCK再次将实体及其关联的子实体与持久上下文重新连接。

        让我们来看看测试用例来了解CascadeType.LOCK：

        ```java
        @Test
        public void whenDetachedAndLockedThenBothReattached() {
            Person person = buildPerson("devender");
            Address address = buildAddress(person);
            person.setAddresses(Arrays.asList(address));
            session.persist(person);
            session.flush();
            
            assertThat(session.contains(person)).isTrue();
            assertThat(session.contains(address)).isTrue();

            session.detach(person);
            assertThat(session.contains(person)).isFalse();
            assertThat(session.contains(address)).isFalse();
            session.unwrap(Session.class)
            .buildLockRequest(new LockOptions(LockMode.NONE))
            .lock(person);

            assertThat(session.contains(person)).isTrue();
            assertThat(session.contains(address)).isTrue();
        }
        ```

        正如我们所看到的，在使用CascadeType.LOCK时，我们将实体人员及其关联地址附加回持久上下文。

    7. CascadeType.REFRESH

        刷新操作从数据库中重新读取给定实例的值。在某些情况下，我们可能会在数据库中持久化后更改实例，但稍后我们需要撤销这些更改。

        在这种情况下，这可能很有用。当我们将此操作与Cascade Type REFRESH一起使用时，每当父实体刷新时，子实体也会从数据库中重新加载。

        为了更好地理解，让我们看看CascadeType.REFRESH的测试用例：

        ```java
        @Test
        public void whenParentRefreshedThenChildRefreshed() {
            Person person = buildPerson("devender");
            Address address = buildAddress(person);
            person.setAddresses(Arrays.asList(address));
            session.persist(person);
            session.flush();
            person.setName("Devender Kumar");
            address.setHouseNumber(24);
            session.refresh(person);
            
            assertThat(person.getName()).isEqualTo("devender");
            assertThat(address.getHouseNumber()).isEqualTo(23);
        }
        ```

        在这里，我们对保存的实体人员和地址进行了一些更改。当我们刷新人员实体时，地址也会刷新。

    8. CascadeType.REPLICATE

        当我们有多个数据源，并且我们希望数据同步时，会使用复制操作。使用CascadeType.REPLICATE，每当在父实体上执行时，同步操作也会传播到子实体。

        现在让我们来测试CascadeType.REPLICATE：

        ```java
        @Test
        public void whenParentReplicatedThenChildReplicated() {
            Person person = buildPerson("devender");
            person.setId(2);
            Address address = buildAddress(person);
            address.setId(2);
            person.setAddresses(Arrays.asList(address));
            session.unwrap(Session.class).replicate(person, ReplicationMode.OVERWRITE);
            session.flush();
            
            assertThat(person.getId()).isEqualTo(2);
            assertThat(address.getId()).isEqualTo(2);
        }
        ```

        由于CascadeType.REPLICATE，当我们复制人员实体时，其关联地址也会用我们设置的标识符复制。

    9. CascadeType.SAVE_UPDATE

        CascadeType.SAVE_UPDATE将相同的操作传播到关联的子实体。当我们使用特定于休眠的操作时，它很有用，如保存、更新和保存或更新。

        让我们看看CascadeType.SAVE_UPDATE在运行：

        ```java
        @Test
        public void whenParentSavedThenChildSaved() {
            Person person = buildPerson("devender");
            Address address = buildAddress(person);
            person.setAddresses(Arrays.asList(address));
            session.saveOrUpdate(person);
            session.flush();
        }
        ```

        由于CascadeType.SAVE_UPDATE，当我们运行上述测试用例时，我们可以看到人员和地址都被保存了。

        以下是生成的SQL：

        ```log
        Hibernate: insert into Person (name, id) values (?, ?)
        Hibernate: insert into Address (
            city, houseNumber, person_id, street, zipCode, id) values (?, ?, ?, ?, ?, ?)
        ```

4. 结论

    在本文中，我们讨论了级联以及JPA和Hibernate中可用的不同级联类型选项。
