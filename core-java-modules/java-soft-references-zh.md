# Java中的软引用

<https://www.baeldung.com/java-soft-references>

1. 什么是软引用？

    软[引用](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ref/Reference.html)对象（或软可达对象）可由垃圾回收器根据内存需求进行清除。软引用对象没有指向它的强引用。

    当垃圾回收器被a调用时，它会开始遍历堆中的所有元素。GC 会将引用类型的对象存储在一个特殊队列中。

    在对堆中的所有对象进行检查后，垃圾回收器会通过从上述队列中删除对象来确定哪些实例应被删除。

    这些规则在不同的 JVM 实现中各不相同，但文档规定，在 JVM 抛出 OutOfMemoryError 之前，保证所有对软可达对象的软引用都会被清除。

    不过，我们并不保证软引用被清除的时间，也不保证清除一组不同对象的软引用的顺序。

    通常，JVM 实现会选择清理最近创建的引用或最近使用的引用。

    软可及对象在最后一次被引用后仍将存活一段时间。默认值是堆中每个空闲兆字节的生命周期为一秒钟。可以使用 -XX:[SoftRefLRUPolicyMSPerMB](http://www.oracle.com/technetwork/java/hotspotfaq-138619.html#gc_softrefs) 标志调整该值。

    例如，要将该值改为 2.5 秒（2500 毫秒），我们可以使用

    `-XX:SoftRefLRUPolicyMSPerMB=2500`
    与弱引用相比，软引用的生命周期更长，因为它们会一直存在，直到需要额外内存为止。

    因此，如果我们需要尽可能长时间地在内存中保留对象，软引用是更好的选择。

2. 软引用的使用案例

    软引用可用于实现对内存敏感的缓存，因为内存管理是一个非常重要的因素。

    只要软引用的引用对象是强可达的，即实际正在使用中，该引用就不会被清除。

    例如，缓存可以通过为最近使用的条目保留强引用来防止这些条目被丢弃，剩下的条目则由垃圾回收器决定是否丢弃。

3. 使用软引用

    在 Java 中，软引用由 [java.lang.ref.SoftReference](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ref/SoftReference.html) 类表示。

    我们有两种方法来初始化它。

    第一种方法是只传递一个引用：

    ```java
    StringBuilder builder = new StringBuilder();
    SoftReference<StringBuilder> reference1 = new SoftReference<>(builder);
    ```

    第二个选项意味着传递对 [java.lang.ref.ReferenceQueue](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/ref/Reference.html) 的引用以及对引用对象的引用。引用队列旨在让我们了解垃圾回收器执行的操作。当垃圾回收器决定删除该引用的引用对象时，它会将该引用对象追加到引用队列中。

    下面是使用引用队列初始化 SoftReference 的方法：

    ```java
    ReferenceQueue<StringBuilder> referenceQueue = new ReferenceQueue<>();
    SoftReference<StringBuilder> reference2
    = new SoftReference<>(builder, referenceQueue);
    ```

    作为 java.lang.ref.Reference，它包含 get 和 clear 方法，分别用于获取和重置引用：

    ```java
    StringBuilder builder1 = reference2.get();
    reference2.clear();
    StringBuilder builder2 = reference2.get(); // null
    ```

    每次使用这种引用时，我们都需要确保由 get 返回的引用存在：

    ```java
    StringBuilder builder3 = reference2.get();
    if (builder3 != null) {
        // GC hasn't removed the instance yet
    } else {
        // GC has cleared the instance
    }
    ```

4. 总结

    熟悉了软引用的概念及其用例。

    学会了如何创建软引用并以编程方式使用它。
