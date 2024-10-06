# [JPA实体平等](https://www.baeldung.com/jpa-entity-equality)

1. 一览表

    在本教程中，我们将看看如何处理与JPA实体对象的平等。

2. 考虑因素

    一般来说，平等只是意味着两个对象是相同的。然而，在Java中，我们可以通过覆盖[Object.equals（）和Object.hashCode（）](https://www.baeldung.com/java-equals-hashcode-contracts)方法来更改等式的定义。归根结底，Java允许我们定义平等意味着什么。但首先，我们需要考虑几件事。

    1. 集合

        Java集合将对象分组在一起。分组逻辑使用被称为散列代码的特殊值来确定对象的组。

        如果hashCode（）方法返回的值对所有实体都是相同的，这可能会导致不良行为。假设我们的实体对象有一个主键定义为id，但我们将hashCode（）方法定义为：

        ```java
        @Override
        public int hashCode() {
            return 12345;
        }
        ```

        集合在比较时无法区分不同的对象，因为它们都共享相同的散列代码。幸运的是，解决这个问题就像在生成散列代码时使用唯一密钥一样简单。例如，我们可以使用我们的id来定义hashCode（）方法：

        ```java
        @Override
        public int hashCode() {
            return id * 12345;
        }
        ```

        在这种情况下，我们使用实体的id来定义散列代码。现在，集合可以比较、分类和存储我们的实体。

    2. Transient的实体

        新创建的与[持久性上下文](https://www.baeldung.com/jpa-hibernate-persistence-context)没有关联的JPA实体对象被视为处于瞬态状态。这些对象通常没有填充其@Id成员。因此，如果equals（）或hashCode（）在计算中使用id，这意味着所有瞬态对象都将相等，因为它们的id都将为空。这种情况并不多，这是可取的。

    3. 子类

        在定义平等时，子类也是一个问题。在equals（）方法中比较类是很常见的。因此，在比较对象的平等性时，包括getClass（）方法将有助于过滤掉子类。

        让我们定义一个equals（）方法，该方法仅在对象具有同一类且具有相同id时才有效：

        ```java
        @Override
        public boolean equals(Object o) {
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            return o.id.equals(this.id);
        }
        ```

3. 定义平等

    鉴于这些考虑，在处理平等时，我们有几个选择。因此，我们采取的方法取决于我们计划如何使用我们的对象的具体情况。让我们来看看我们的选择。

    1. 没有覆盖

        默认情况下，Java根据来自对象类的所有对象提供equals（）和hashCode（）方法。因此，我们能做的最简单的事情就是什么都不做。不幸的是，这意味着在比较对象时，为了被视为平等，它们必须是相同的实例，而不是代表同一对象的两个单独的实例。

    2. 使用数据库密钥

        在大多数情况下，我们正在处理存储在数据库中的JPA实体。通常，这些实体有一个唯一值的主键。因此，该实体中具有相同主键值的任何实例都是相等的。因此，我们可以像上面对子类所做的那样覆盖equals（），也可以仅使用两者中的主键覆盖hashCode（）。

    3. 使用商业密钥

        或者，我们可以使用业务密钥来比较JPA实体。在这种情况下，对象的密钥由除主密钥以外的实体成员组成。此密钥应使JPA实体独一无二。在比较实体时，使用业务密钥可以给我们提供相同的预期结果，而不需要主密钥或数据库生成的密钥。

        假设我们知道电子邮件地址总是唯一的，即使它不是@Id字段。我们可以在hashCode（）和equals（）方法中包含电子邮件字段：

        ```java
        public class EqualByBusinessKey {

            private String email;

            @Override
            public int hashCode() {
                return java.util.Objects.hashCode(email);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (obj == null) {
                    return false;
                }
                if (obj instanceof EqualByBusinessKey) {
                    if (((EqualByBusinessKey) obj).getEmail().equals(getEmail())) {
                        return true;
                    }
                }

                return false;
            }
        }
        ```

4. 结论

    在本教程中，我们讨论了在编写JPA实体对象时处理等式的各种方法。我们还描述了我们在选择方法时应该考虑的因素。
