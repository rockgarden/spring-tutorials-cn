# [Java堆栈快速指南](https://www.baeldung.com/java-stack)

1. 一览表

    在这篇短文中，我们将介绍java.util.Stack类，并开始研究如何使用它。

    堆栈是一种通用数据结构，它表示LIFO（最后进，先出）对象集合，允许在恒定时间内推送/弹出元素。

    对于新的实现，我们应该倾向于Deque接口及其实现。Deque定义了一组更完整、更一致的LIFO操作。然而，我们可能仍然需要处理Stack类，特别是在遗留代码中，因此了解它很重要。

2. 创建一个堆栈

    让我们从使用默认的无参数构造函数创建一个空的Stack实例开始：

    ```java
    @Test
    public void whenStackIsCreated_thenItHasSizeZero() {
        Stack<Integer> intStack = new Stack<>();
        assertEquals(0, intStack.size());
    }
    ```

    这将创建一个默认容量为10的堆栈。如果添加的元素数量超过总堆栈大小，它将自动翻倍。然而，在去除元素后，它的尺寸永远不会缩小。

3. 堆栈的同步

    Stack是Vector的直接子类；这意味着与其超类类似，它是一个同步实现。

    然而，并不总是需要同步，在这种情况下，建议使用ArrayDeque。

4. 添加到堆栈中

    让我们从使用push（）方法在堆栈顶部添加一个元素开始——该方法还返回已添加的元素：

    ```java
    @Test
    public void whenElementIsPushed_thenStackSizeIsIncreased() {
        Stack<Integer> intStack = new Stack<>();
        intStack.push(1);
        assertEquals(1, intStack.size());
    }
    ```

    使用push（）方法与使用addElement（）具有相同的效果。唯一的区别是addElement（）返回操作结果，而不是添加的元素。

    我们也可以同时添加多个元素：

    ```java
    @Test
    public void whenMultipleElementsArePushed_thenStackSizeIsIncreased() {
        Stack<Integer> intStack = new Stack<>();
        List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        boolean result = intStack.addAll(intList);
        assertTrue(result);
        assertEquals(7, intList.size());
    }
    ```

5. 从堆栈中检索

    接下来，让我们来看看如何获取和删除堆栈中的最后一个元素：

    ```java
    @Test
    public void whenElementIsPoppedFromStack_thenElementIsRemovedAndSizeChanges() {
        Stack<Integer> intStack = new Stack<>();
        intStack.push(5);

        Integer element = intStack.pop();
        
        assertEquals(Integer.valueOf(5), element);
        assertTrue(intStack.isEmpty());
    }
    ```

    我们还可以在不删除堆栈的最后一个元素的情况下获取它：

    ```java
    @Test
    public void whenElementIsPeeked_thenElementIsNotRemovedAndSizeDoesNotChange() {
        Stack<Integer> intStack = new Stack<>();
        intStack.push(5);

        Integer element = intStack.peek();

        assertEquals(Integer.valueOf(5), element);
        assertEquals(1, intStack.search(5));
        assertEquals(1, intStack.size());
    }
    ```

6. 在堆栈中搜索元素

    1. 搜索

        Stack允许我们搜索元素，并获取其与顶部的距离：

        ```java
        @Test
        public void whenElementIsOnStack_thenSearchReturnsItsDistanceFromTheTop() {
            Stack<Integer> intStack = new Stack<>();
            intStack.push(5);
            intStack.push(8);
            assertEquals(2, intStack.search(5));
        }
        ```

        结果是给定对象的索引。如果存在多个元素，则返回最接近顶部的元素的索引。堆栈顶部的项目被视为位于第1位。

        如果找不到对象，搜索（）将返回-1。

    2. 获取元素索引

        为了在堆栈上获取元素的索引，我们还可以使用indexOf（）和lastIndexOf（）方法：

        ```java
        @Test
        public void whenElementIsOnStack_thenIndexOfReturnsItsIndex() {
            Stack<Integer> intStack = new Stack<>();
            intStack.push(5);
            int indexOf = intStack.indexOf(5);
            assertEquals(0, indexOf);
        }
        ```

        lastIndexOf（）将始终找到最接近堆栈顶部的元素的索引。这与search（）的工作方式非常相似——重要的区别是它返回索引，而不是与顶部的距离：

        ```java
        @Test
        public void whenMultipleElementsAreOnStack_thenIndexOfReturnsLastElementIndex() {
            Stack<Integer> intStack = new Stack<>();
            intStack.push(5);
            intStack.push(5);
            intStack.push(5);
            int lastIndexOf = intStack.lastIndexOf(5);
            assertEquals(2, lastIndexOf);
        }
        ```

7. 从堆栈中删除元素

    除了用于删除和检索元素的pop（）操作外，我们还可以使用从Vector类继承的多个操作来删除元素。

    1. 删除指定元素

        我们可以使用removeElement（）方法来删除给定元素的首次出现：

        ```java
        @Test
        public void whenRemoveElementIsInvoked_thenElementIsRemoved() {
            Stack<Integer> intStack = new Stack<>();
            intStack.push(5);
            intStack.push(5);
            intStack.removeElement(5);
            assertEquals(1, intStack.size());
        }
        ```

        我们还可以使用removeElementAt()删除堆栈中指定索引下的元素：

        ```java
            @Test
            public void whenRemoveElementAtIsInvoked_thenElementIsRemoved() {
                Stack<Integer> intStack = new Stack<>();
                intStack.push(5);
                intStack.push(7);
                
                intStack.removeElementAt(1);
                
                assertEquals(-1, intStack.search(7));
            }
        ```

    2. 去除多个元素

        让我们快速看看如何使用removeAll（）API从堆栈中删除多个元素——它将aCollection作为参数，并从堆栈中删除所有匹配的元素：

        ```java
        @Test
        public void givenElementsOnStack_whenRemoveAllIsInvoked_thenAllElementsFromCollectionAreRemoved() {
            Stack<Integer> intStack = new Stack<>();
            List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
            intStack.addAll(intList);
            intStack.add(500);

            intStack.removeAll(intList);

            assertEquals(1, intStack.size());
            assertEquals(1, intStack.search(500));
        }
        ```

        也可以使用clear（）或removeAllElements（）方法从堆栈中删除所有元素；这两种方法的工作方式相同：

        ```java
        @Test
        public void whenRemoveAllElementsIsInvoked_thenAllElementsAreRemoved() {
            Stack<Integer> intStack = new Stack<>();
            intStack.push(5);
            intStack.push(7);

            intStack.removeAllElements();

            assertTrue(intStack.isEmpty());
        }
        ```

    3. 使用过滤器删除元素

        我们还可以使用条件从堆栈中删除元素。让我们看看如何使用removeIf（）来完成此操作，并使用过滤器表达式作为参数：

        ```java
        @Test
        public void whenRemoveIfIsInvoked_thenAllElementsSatysfyingConditionAreRemoved() {
            Stack<Integer> intStack = new Stack<>();
            List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
            intStack.addAll(intList);
            intStack.removeIf(element -> element < 6);
            assertEquals(2, intStack.size());
        }
        ```

8. 迭代堆栈

    Stack允许我们同时使用迭代器和ListIterator。主要区别在于，第一个允许我们在一个方向上遍历Stack，第二个允许我们在两个方向上都这样做：

    ```java
    @Test
    public void whenAnotherStackCreatedWhileTraversingStack_thenStacksAreEqual() {
        Stack<Integer> intStack = new Stack<>();
        List<Integer> intList = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        intStack.addAll(intList);
        ListIterator<Integer> it = intStack.listIterator();

        Stack<Integer> result = new Stack<>();
        while(it.hasNext()) {
            result.push(it.next());
        }

        assertThat(result, equalTo(intStack));
    }
    ```

    Stack返回的所有Iterator都是快速失败的。

9. Java堆栈的流应用程序接口

    Stack是一个集合，这意味着我们可以将其与Java 8 Streams API一起使用。将Stream与Stack一起使用类似于将其与任何其他集合一起使用：

    ```java
    @Test
    public void whenStackIsFiltered_allElementsNotSatisfyingFilterConditionAreDiscarded() {
        Stack<Integer> intStack = new Stack<>();
        List<Integer> inputIntList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9, 10);
        intStack.addAll(inputIntList);

        List<Integer> filtered = intStack
        .stream()
        .filter(element -> element <= 3)
        .collect(Collectors.toList());

        assertEquals(3, filtered.size());
    }
    ```

10. 摘要

    本教程是了解Java核心类的快速实用指南——Stack。

    当然，您可以在[Javadoc](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Stack.html)中探索完整的API。
