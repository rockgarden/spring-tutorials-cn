# [Java 中的 ‘private’ 访问修饰符](https://www.baeldung.com/java-private-keyword)

核心 Java · 访问修饰符 · 定义 · Java 关键字

1. 概述

    在 Java 编程语言中，字段、构造函数、方法和类都可以使用访问修饰符进行标记。在本教程中，我们将讨论 Java 中的 `private` 访问修饰符。

2. 关键字说明

    `private` 访问修饰符非常重要，因为它支持**封装**（encapsulation）和**信息隐藏**（information hiding），而这正是面向对象编程（OOP）的核心原则。

    - **封装**负责将数据和方法捆绑在一起；
    - **信息隐藏**是封装的自然结果，它隐藏了对象的内部实现细节。

    需要牢记的第一点是：**被声明为 `private` 的元素只能在其声明的类内部访问**。

3. 字段（Fields）

    接下来，我们通过一些简单的代码示例来更好地理解这一概念。

    首先，创建一个包含两个 `private` 实例变量的 `Employee` 类：

    ```java
    public class Employee {
        private String privateId;
        private boolean manager;
        //...
    }
    ```

    在这个例子中，我们将 `privateId` 字段标记为 `private`，因为我们希望在生成 ID 时加入一些逻辑。同样，`manager` 属性也被设为 `private`，因为我们不希望外部代码直接修改该字段。

4. 构造函数（Constructors）

    现在，我们创建一个 `private` 构造函数：

    ```java
    private Employee(String id, String name, boolean managerAttribute) {
        this.name = name;
        this.privateId = id + "_ID-MANAGER";
    }
    ```

    通过将构造函数标记为 `private`，我们只能在类内部使用它。

    接下来，添加一个静态方法，作为从 `Employee` 类外部调用该私有构造函数的唯一途径：

    ```java
    public static Employee buildManager(String id, String name) {
        return new Employee(id, name, true);
    }
    ```

    现在，我们可以通过以下方式获取一个经理类型的 `Employee` 实例：

    ```java
    Employee manager = Employee.buildManager("123MAN", "Bob");
    ```

    在幕后，`buildManager` 方法调用了我们的私有构造函数。

5. 方法（Methods）

    现在，我们在类中添加一个私有方法：

    ```java
    private void setManager(boolean manager) {
        this.manager = manager;
    }
    ```

    假设出于某种原因，公司规定只有名为 “Carl” 的员工才能被提拔为经理，而其他类并不知道这一规则。我们可以创建一个公共方法，在其中加入业务逻辑，并调用上述私有方法：

    ```java
    public void elevateToManager() {
        if ("Carl".equals(this.name)) {
            setManager(true);
        }
    }
    ```

6. `private` 的实际应用

    让我们看看如何从外部使用 `Employee` 类：

    ```java
    public class ExampleClass {
        public static void main(String[] args) {
            Employee employee = new Employee("Bob", "ABC123");
            employee.setPrivateId("BCD234");
            System.out.println(employee.getPrivateId());
        }
    }
    ```

    执行 `ExampleClass` 后，控制台将输出：

    ```log
    BCD234_ID
    ```

    在此示例中，我们使用了公共构造函数和公共方法 `setPrivateId()`，因为我们**无法直接访问**私有变量 `privateId`。

    现在，如果我们尝试从 `Employee` 类外部访问私有方法、构造函数或变量，会发生什么？

    ```java
    public class ExampleClass {
        public static void main(String[] args) {
            Employee employee = new Employee("Bob", "ABC123", true); // ❌
            employee.setManager(true);                               // ❌
            employee.privateId = "ABC234";                           // ❌
        }
    }
    ```

    上述每一条非法语句都会导致**编译错误**：

    ```log
    The constructor Employee(String, String, boolean) is not visible
    The method setManager(boolean) from the type Employee is not visible
    The field Employee.privateId is not visible
    ```

7. 类（Classes）

    有一种特殊情况可以创建 `private` 类——即作为**另一个类的内部类**（inner class）。
    如果我们将一个**顶层类**（outer/top-level class）声明为 `private`，就会禁止其他类访问它，使其完全无法使用，因此 Java 不允许这样做。

    下面是一个合法的 `private` 内部类示例：

    ```java
    public class PublicOuterClass {

        public PrivateInnerClass getInnerClassInstance() {
            PrivateInnerClass myPrivateClassInstance = this.new PrivateInnerClass();
            myPrivateClassInstance.id = "ID1";
            myPrivateClassInstance.name = "Bob";
            return myPrivateClassInstance;
        }

        private class PrivateInnerClass {
            public String name;
            public String id;
        }
    }
    ```

    在此例中，我们在 `PublicOuterClass` 内部通过 `private` 修饰符定义了一个私有内部类。

    由于使用了 `private` 关键字，如果我们尝试在 `PublicOuterClass` 外部实例化 `PrivateInnerClass`，代码将无法编译，并报错：

    ```log
    PrivateInnerClass cannot be resolved to a type
    ```

8. 结论

    在本篇简明教程中，我们探讨了 Java 中的 `private` 访问修饰符。它是实现**封装**的有效方式，进而达成**信息隐藏**。
    通过合理使用 `private`，我们可以确保只向其他类暴露我们希望公开的数据和行为，从而提升代码的健壮性、可维护性和安全性。
