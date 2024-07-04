# Java中的堆栈内存和堆空间

1. 简介

   为了以最佳方式运行应用程序，JVM 将内存分为栈内存和堆内存。每当我们声明新变量和对象、调用新方法、声明字符串或执行类似操作时，JVM 都会从[栈内存或堆空间](https://www.baeldung.com/cs/memory-stack-vs-heap)为这些操作指定内存。

   在本教程中，我们将研究这些内存模型。首先，我们将探讨它们的主要特点。然后，我们将了解它们如何存储在 RAM 中，以及在哪里使用它们。最后，我们将讨论它们之间的主要区别。

2. Java 中的堆栈内存

   Java 中的堆栈内存用于静态内存分配和线程执行。它包含特定于方法的原始值，以及从方法引用到堆中对象的引用。

   访问内存的顺序是后进先出（Last-In-First-Out LIFO）。每当我们调用一个新方法时，堆栈顶部就会创建一个新块，其中包含该方法的特定值，如原始变量和对象引用。

   方法执行完毕后，其相应的堆栈帧会被刷新，数据流回到调用方法，并为下一个方法提供可用空间。

   1. 堆栈内存的主要特征

      堆栈内存的其他一些特性包括

      - 当新方法被调用和返回时，堆栈分别增长和收缩。
      - 堆栈中的变量只有在创建它们的方法运行时才存在。
      - 当方法执行完毕时，堆栈内存会自动分配和清空。
      - 如果内存已满，Java 会抛出 java.lang.StackOverFlowError 错误。
      - 与堆内存相比，访问该内存的速度很快。
      - 该内存是线程安全的，因为每个线程都在自己的堆栈中运行。

3. Java 中的堆空间

   堆空间用于在运行时为 Java 对象和 JRE 类动态分配内存。新对象总是在堆空间中创建，这些对象的引用存储在栈内存中。

   这些对象具有全局访问权限，我们可以从应用程序的任何地方访问它们。

   我们可以将这种内存模型分解成更小的部分，称为 "generations"，它们是

   - Young Generation 年轻一代 - 所有新对象都在这里分配和老化。当它填满时，就会进行一次小规模的垃圾回收。
   - Old or Tenured Generation 旧一代或长期一代 - 这是存储长期存活对象的地方。当对象存储在年轻一代时，会设置一个对象年龄阈值，当达到该阈值时，对象就会被转移到老一代。
   - Permanent Generation 永久代 - 这包括运行时类和应用程序方法的 JVM 元数据。

   我们总是可以根据自己的需求来操纵堆内存的大小。如需了解更多信息，请访问[链接](https://www.baeldung.com/jvm-parameters)文章。

   1. Java 堆内存的主要特性

      堆空间的其他一些特性包括

      - 它通过复杂的内存管理技术进行访问，其中包括年轻一代、老一代或长期一代以及永久一代。
      - 如果堆空间已满，Java 会抛出 java.lang.OutOfMemoryError 错误。
      - 访问堆内存比访问栈内存慢
      - 与堆栈相比，该内存不会自动去分配。它需要垃圾回收器来释放未使用的对象，以保持内存的使用效率。
      - 与堆栈不同，堆不是线程安全的，需要通过适当同步代码来保护。

4. 示例

   根据我们目前所学的知识，让我们分析一个简单的 Java 代码，以评估如何在此管理内存：

   ```java
   class Person {
       int id;
       String name;
       public Person(int id, String name) {
           this.id = id;
           this.name = name;
       }
   }
   public class PersonBuilder {
       private static Person buildPerson(int id, String name) {
           return new Person(id, name);
       }
       public static void main(String[] args) {
           int id = 23;
           String name = "John";
           Person person = null;
           person = buildPerson(id, name);
       }
   }
   ```

   让我们逐步分析一下：

   1. 当我们进入 main() 方法时，会在栈内存中创建一个空间来存储该方法的基元和引用。
      - 栈内存直接存储整数 id 的基元值。
      - 栈内存中还将创建 Person 类型的引用变量 person，它将指向堆中的实际对象。
   2. 在 main() 中调用参数化构造函数 Person(int, String) 时，将在之前的堆栈上分配更多内存。这将存储
      - 栈内存中调用对象的 this 对象引用
      - 堆栈内存中的原始值 id
      - 字符串参数名的引用变量，它将指向堆内存中字符串池中的实际字符串。
   3. 方法将进一步调用 buildPerson() 静态方法，为此将在前一个方法的基础上在堆内存中进行进一步分配。这将再次以上述方式存储变量。
   4. 但是，堆内存将存储新创建的 Person 类型对象的所有实例变量。
      让我们看看下图中的分配情况：

      ![Java堆栈图](/pic/java-heap-stack-diagram.png)

5. 总结

   在结束本文之前，让我们快速总结一下堆内存和堆空间的区别：

   | Parameter               | Stack Memory                                           | Heap Space                                                                               |
   | ----------------------- | ------------------------------------------------------ | ---------------------------------------------------------------------------------------- |
   | Application             | 应用程序堆栈在线程执行过程中分段使用，每次使用一个堆栈 | 整个应用程序在运行时使用堆空间                                                           |
   | Size                    | 堆栈的大小受操作系统限制，通常比堆小                   | 堆没有大小限制                                                                           |
   | Storage                 | 仅存储原始变量和对在堆空间创建的对象的引用             | 所有新创建的对象都存储在这里                                                             |
   | Order                   | 它使用后进先出（LIFO）内存分配系统进行访问             | 这些内存通过复杂的内存管理技术进行访问，其中包括年轻一代、老一代或长期一代以及永久一代。 |
   | Life                    | 堆栈内存只存在于当前方法运行期间                       | 堆空间在应用程序运行时就存在                                                             |
   | Efficiency              | 与堆相比，分配速度更快                                 | 与栈相比，分配速度更慢                                                                   |
   | Allocation/Deallocation | 当方法被调用和返回时，内存分别被自动分配和清空         | 当创建新对象时分配堆空间，当不再引用对象时由 Gargabe Collector 去分配堆空间              |

6. 结论

   栈和堆是 Java 分配内存的两种方式。在本文中，我们了解了它们的工作原理，以及何时使用它们来开发更好的 Java 程序。

   要了解有关 Java 中内存管理的更多信息，请点击此处[阅读本文](https://www.baeldung.com/java-memory-management-interview-questions)。[本文](https://www.baeldung.com/jvm-garbage-collectors)还简要讨论了 JVM 垃圾收集器。

## 相关文章

- [x] [Stack Memory and Heap Space in Java](https://www.baeldung.com/java-stack-heap)
