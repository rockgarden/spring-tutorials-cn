# [Java 中的 hashCode()指南](https://www.baeldung.com/java-hashcode)

1. 概述

   散列是计算机科学的一个基本概念。
   在 Java 中，一些最流行的集合背后都有高效的散列算法，如 HashMap 和 HashSet。
   在本教程中，我们将重点讨论 hashCode()如何工作，它在集合中的作用以及如何正确实现它。

2. 在数据结构中使用 hashCode()

   在某些情况下，对集合的最简单的操作可能是低效的。
   为了说明问题，这引发了一个线性搜索，这对巨大的列表来说是非常无效的：

   ```java
   List<String> words = Arrays.asList("Welcome", "to", "Baeldung");
   if (words.contains("Baeldung")) {
       System.out.println("Baeldung is in the list");
   }
   ```

   Java 提供了一些数据结构来专门处理这个问题。例如，几个 Map 接口的实现都是[哈希表](https://www.baeldung.com/cs/hash-tables)。
   当使用哈希表时，这些集合使用 hashCode()方法计算出给定键的哈希值。然后它们在内部使用这个值来存储数据，这样访问操作就更有效率了。

3. 了解 hashCode()如何工作

   简单来说，hashCode() 返回一个整数值，该值由哈希算法生成。

   根据 equals() 方法判断为相等的对象，必须返回相同的哈希码；而不同的对象则不需要返回不同的哈希码。

   hashCode() 的通用约定如下：

   1. 在 Java 应用程序的一次执行过程中，只要对象用于 equals 比较的信息没有被修改，那么对该对象多次调用 hashCode() 必须始终返回相同的值。不过，该值在应用程序的不同次执行之间不需要保持一致。
   2. 如果两个对象根据 equals(Object) 方法判断为相等，那么对这两个对象分别调用 hashCode() 方法必须产生相同的整数值。
   3. 如果两个对象根据 [equals(Object)](<https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Objects.html#equals(java.lang.Object,java.lang.Object)>) 方法判断为不相等，那么对这两个对象调用 hashCode() 方法所得到的整数结果可以相同，也可以不同。但开发者应当意识到：为不相等的对象生成不同的哈希码，有助于提升哈希表（如 HashMap、HashSet）的性能。

   “在合理可行的范围内，Object 类定义的 hashCode() 方法会为不同的对象返回不同的整数。（这通常是通过将对象的内部地址转换为整数来实现的，但 Java™ 编程语言并不要求必须采用这种实现方式。）”

4. 一个 Naive 的 hashCode()实现

   一个完全遵守上述契约的 Naive 的 hashCode()实现实际上是非常简单的。
   为了证明这一点，我们将定义一个覆盖该方法默认实现的用户类样本：

   ![User.java](./src/main/java/com/baeldung/hashcode/naive/User.java)

   用户类为 equals()和 hashCode()提供了自定义的实现，完全遵守了各自的契约。此外，让 hashCode()返回任何固定值也没有什么不合法的。
   然而，这种实现将哈希表的功能降低到基本为零，因为每个对象都将被存储在同一个单一的桶中。
   在这种情况下，哈希表的查找是线性进行的，并没有给我们带来任何真正的优势。我们将在第 7 节中进一步讨论这个问题。

5. 改进 hashCode()的实现

   让我们改进当前的 hashCode()实现，包括用户类的所有字段，这样它就能对不相等的对象产生不同的结果：

   ```java
   @Override
   public int hashCode() {
       return (int) id * name.hashCode() * email.hashCode();
   }
   ```

   这种基本的散列算法肯定要比之前的算法好得多。这是因为它计算对象的哈希代码时，只需将 name 和 email 字段的哈希代码与 id 相乘。
   一般来说，我们可以说这是一个合理的 hashCode()实现，只要我们保持 equals()实现与之一致。

6. 标准 hashCode()的实现

   我们用来计算哈希码的哈希算法越好，哈希表的性能就越好。
   让我们来看看一个 "标准" 的实现，它使用两个质数来为计算的哈希码增加更多的唯一性：

   ```java
   @Override
   public int hashCode() {
       int hash = 7;
       hash = 31 * hash + (int) id;
       hash = 31 * hash + (name == null ? 0 : name.hashCode());
       hash = 31 * hash + (email == null ? 0 : email.hashCode());
       return hash;
   }
   ```

   虽然我们需要了解 hashCode()和 equals()方法所起的作用，但我们不必每次都从头实现它们。这是因为大多数 IDE 可以生成自定义的 hashCode()和 equals()实现。而且从 Java 7 开始，我们有一个 Objects.hash()的实用方法来进行舒适的散列：
   `Objects.hash(name, email)`

   IntelliJ IDEA 生成了以下实现：

   ```java
   @Override
   public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + ((email == null) ? 0 : email.hashCode());
       result = prime * result + (int) (id ^ (id >>> 32));
       result = prime * result + ((name == null) ? 0 : name.hashCode());
       return result;
   }
   ```

   而 Eclipse 产生了这个：

   ```java
   @Override
   public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + ((email == null) ? 0 : email.hashCode());
       result = prime * result + (int) (id ^ (id >>> 32));
       result = prime * result + ((name == null) ? 0 : name.hashCode());
       return result;
   }
   ```

   除了上述基于 IDE 的 hashCode()实现外，还可以自动生成高效的实现，例如使用[Lombok](https://projectlombok.org/features/EqualsAndHashCode)。
   在这种情况下，我们需要在 pom.xml 中添加 lombok-maven 依赖项：

   ```xml
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok-maven</artifactId>
       <version>1.18.30</version>
       <type>pom</type>
   </dependency>
   ```

   现在只需用@EqualsAndHashCode 注解用户类即可：

   ```java
   @EqualsAndHashCode
   public class User {
       // fields and methods here
   }
   ```

   同样，如果我们想让 Apache Commons Lang 的[HashCodeBuilder](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/builder/HashCodeBuilder.html)类为我们生成一个 hashCode()实现，我们在 pom 文件中加入 commons-lang 的 Maven 依赖项：

   ```xml
   <dependency>
       <groupId>commons-lang</groupId>
       <artifactId>commons-lang</artifactId>
       <version>3.14.0</version>
   </dependency>
   ```

   而 hashCode()可以像这样实现：

   ```java
   public class User {
       public int hashCode() {
           return new HashCodeBuilder(17, 37).
           append(id).
           append(name).
           append(email).
           toHashCode();
       }
   }
   ```

   一般来说，在实现 hashCode()方面没有通用的配方。我们强烈建议阅读 Joshua Bloch 的[《Effective Java》](https://www.amazon.com/Effective-Java-3rd-Joshua-Bloch/dp/0134685997)。它为实现高效的散列算法提供了一份[详尽的指南](https://es.slideshare.net/MukkamalaKamal/joshua-bloch-effect-java-chapter-3)。
   注意到这里，所有这些实现都以某种形式利用了数字 31。这是因为 31 有一个很好的属性。它的乘法可以用位移来代替，这比标准的乘法要快：

   `31 * i == (i << 5) - i`

   位移过程详解

   1. `i << 5` 是什么？

      - `<<` 是左移位运算符。
      - `i << 5` 表示将整数 `i` 的二进制表示向左移动 5 位。
      - 左移 1 位相当于乘以 2，左移 5 位就相当于乘以 \(2^5 = 32\)。
      - 所以：`i << 5` 等价于 `32 * i`

   2. 那么 `(i << 5) - i` 呢？

      - 代入上面的结果：  
        `(i << 5) - i = 32 * i - i = (32 - 1) * i = 31 * i`

      所以：`31 * i == (i << 5) - i`

   为什么这更快？

   - 在早期的 CPU 或某些硬件上，**整数乘法**比**位移 + 减法**更慢。
   - 虽然现代 JVM（如 HotSpot）通常会自动将 `31 * i` 优化成 `(i << 5) - i`，但选择 31 本身就为这种优化提供了可能。
   - 因此，使用 31 既保证了良好的哈希分布（质数特性），又具备潜在的性能优势。

7. 处理哈希碰撞

   散列表的内在行为带来了这些数据结构的一个相关方面： 即使有一个高效的散列算法，两个或更多的对象可能有相同的散列代码，即使它们不相等。因此，他们的哈希代码会指向同一个桶，即使他们有不同的哈希表键。
   这种情况通常被称为哈希碰撞，存在各种[处理方法](https://courses.cs.washington.edu/courses/cse373/18au/files/slides/lecture13.pdf)，每一种都有其优点和缺点。Java 的 HashMap 使用[单独的链式方法](https://en.wikipedia.org/wiki/Hash_table#Separate_chaining_with_linked_lists)来处理碰撞问题：
   "当两个或更多的对象指向同一个桶时，它们被简单地存储在一个链接列表中。在这种情况下，哈希表是一个链接列表的数组，每个具有相同哈希值的对象都被附加到数组中的桶索引处的链接列表。
   在最坏的情况下，几个 buckets 会有一个链接列表与之绑定，列表中的一个对象的检索将以线性方式进行。"
   哈希碰撞方法简明扼要地展示了为什么有效地实现 hashCode()是如此重要。
   Java 8 给 HashMap 的实现带来了一个有趣的[增强](http://openjdk.java.net/jeps/180)。如果一个桶的大小超过了一定的阈值，一个树形图就会取代链表。这允许实现 O(logn)查找，而不是悲观的 O(n)。

   在 Java 中，`HashMap` 内部使用一个**数组 + 链表（或红黑树）**的结构来存储键值对（`Entry` 或 `Node`）：

   - 数组的每个位置称为一个 “桶”（bucket）。
   - 当多个键的哈希值映射到同一个桶时，就会发生 哈希冲突。
   - 在 Java 8 之前，所有冲突的元素都以单向链表的形式存储在同一个桶中。

   链表查找的时间复杂度是 **O(n)**（n 是该桶中元素个数），当冲突严重（比如大量键哈希到同一个桶），性能会急剧下降。

   为了解决这个问题，Java 8 引入了“树化”（treeification）机制（JEP 180）：

   **当某个桶中的链表长度超过阈值（默认为 8），并且 HashMap 的数组长度 ≥ 64 时，链表会转换为一棵红黑树（Red-Black Tree）。**

   为什么是红黑树？

   - 红黑树是一种**自平衡的二叉搜索树**。
   - 它能保证查找、插入、删除操作的时间复杂度为 **O(log n)**。
   - 相比链表的 O(n)，在冲突严重时性能显著提升。

   关键参数

   | 参数                   | 默认值 | 说明                                                |
   | ---------------------- | ------ | --------------------------------------------------- |
   | `TREEIFY_THRESHOLD`    | 8      | 链表长度 ≥ 8 时，考虑树化                           |
   | `UNTREEIFY_THRESHOLD`  | 6      | 树中节点数 ≤ 6 时，退化回链表                       |
   | `MIN_TREEIFY_CAPACITY` | 64     | 整个 HashMap 的容量（数组长度）必须 ≥ 64 才允许树化 |

   > 注意：**即使链表长度 ≥ 8，如果数组总长度 < 64，HashMap 会优先选择扩容（resize）而不是树化**。  
   > 这是因为冲突可能只是因为桶太少，扩容后冲突自然减少，没必要建树。

   工作流程示例

   1. 向 `HashMap` 插入大量哈希值相同的键（例如，自定义类未正确实现 `hashCode()`，总是返回相同值）。
   2. 某个桶的链表长度逐渐增长。
   3. 当链表长度达到 **8**，且 `table.length >= 64`：
      - 该桶的链表被转换为**红黑树**。
      - 后续在该桶中的查找、插入、删除操作变为 **O(log n)**。
   4. 如果后续删除元素，树中节点数 ≤ 6：
      - 红黑树会**退化回链表**（节省内存，因为树节点比普通节点更复杂）。

   节点类型变化

   - 普通节点：`Node<K,V>`（用于链表）
   - 树节点：`TreeNode<K,V>`（继承自 `Node`，用于红黑树）

   实际影响

   - **正常情况下**（哈希分布均匀）：几乎不会触发树化，性能与 Java 7 相同（O(1)）。
   - **极端情况下**（恶意攻击、哈希设计不良）：避免 O(n) 性能退化，保障 O(log n) 上限。
   - 这使得 `HashMap` 对 **拒绝服务（DoS）攻击**（如哈希碰撞攻击）更具抵抗力。

   Java 8 的 `HashMap` 在以下条件下将链表升级为红黑树：**链表长度 ≥ 8 且 数组容量 ≥ 64**

   此举将最坏情况下的查找复杂度从 **O(n)** 优化为 **O(log n)**，显著提升了在哈希冲突严重场景下的性能和安全性，同时在正常场景下保持高效和低开销。

   这一设计体现了“**平时轻量，异常时稳健**”的工程思想。

8. 创建一个微不足道的应用程序

   现在我们来测试一下标准 hashCode()实现的功能。
   让我们创建一个简单的 Java 应用程序，将一些用户对象添加到 HashMap 中，并在每次调用该方法时使用 SLF4J 将一条消息记录到控制台。
   下面是示例应用程序的入口点：

   ```java
   public class Application {
       public static void main(String[] args) {
           Map<User, User> users = new HashMap<>();
           User user1 = new User(1L, "John", "john@domain.com");
           User user2 = new User(2L, "Jennifer", "jennifer@domain.com");
           User user3 = new User(3L, "Mary", "mary@domain.com");
           users.put(user1, user1);
           users.put(user2, user2);
           users.put(user3, user3);
           if (users.containsKey(user1)) {
               System.out.print("User found in the collection");
           }
       }
   }
   ```

   这就是 hashCode()的实现：

   ```java
   public class User {
       // ...
       public int hashCode() {
           int hash = 7;
           hash = 31 * hash + (int) id;
           hash = 31 * hash + (name == null ? 0 : name.hashCode());
           hash = 31 * hash + (email == null ? 0 : email.hashCode());
           logger.info("hashCode() called - Computed hash: " + hash);
           return hash;
       }
   }
   ```

   在这里，需要注意的是，每当一个对象被存储在哈希图中，并用 containsKey()方法检查时，hashCode()就会被调用，计算出的哈希代码会被打印到控制台：

   ```log
   [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: 1255477819
   [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: -282948472
   [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: -1540702691
   [main] INFO com.baeldung.entities.User - hashCode() called - Computed hash: 1255477819
   User found in the collection
   ```

9. 总结

   很明显，制作高效的 hashCode()实现往往需要混合使用一些数学概念（即素数和任意数）、逻辑和基本数学运算。
   不管怎么说，我们完全可以不借助于这些技术来有效地实现 hashCode()。我们只需要确保散列算法对不相等的对象产生不同的散列码，并且与 equals()的实现一致。
