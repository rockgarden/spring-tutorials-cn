# [Java中的访问修饰符](https://www.baeldung.com/java-access-modifiers)

1. 概述

    在本教程中，我们将讨论Java中的访问修饰符，它用于设置对类、变量、方法和构造函数的访问级别(classes, variables, methods, and constructors.)。

    简单地说，有四个访问修饰符：public、private、protected和default（无关键字）。

    在我们开始之前，让我们注意到一个顶层的类只能使用公共或默认的访问修饰符。在成员层，我们可以使用所有四个。

2. 缺省

    当我们没有明确使用任何关键字时，Java将为一个给定的类、方法或属性设置默认访问。默认访问修改器也被称为包-私有(package-private)，这意味着所有成员在同一包内是可见的，但不能从其他包中访问：

    ```java
    package com.baeldung.accessmodifiers;

    public class SuperPublic {
        static void defaultMethod() {
            ...
        }
    }
    ```

    defaultMethod()可以在同一个包的另一个类中访问：

    ```java
    package com.baeldung.accessmodifiers;

    public class Public {
        public Public() {
            SuperPublic.defaultMethod(); // Available in the same package.
        }
    }
    ```

    然而，在其他包中是不可用的。

3. 公开

    如果我们给一个类、方法或属性添加public关键字，那么我们就会让它对整个世界可用，也就是说，所有包中的所有其他类都能使用它。这是限制性最小的访问修改器：

    ```java
    package com.baeldung.accessmodifiers;

    public class SuperPublic {
        public static void publicMethod() {
            ...
        }
    }
    ```

    publicMethod()在另一个包中可用：

    ```java
    package com.baeldung.accessmodifiers.another;

    import com.baeldung.accessmodifiers.SuperPublic;

    public class AnotherPublic {
        public AnotherPublic() {
            SuperPublic.publicMethod(); // Available everywhere. Let's note different package.
        }
    }
    ```

4. 私有

    任何带有private关键字的方法、属性或构造函数都只能从同一个类中访问。这是限制性最强的访问修饰语，是封装概念的核心。所有的数据都将从外界隐藏起来：

    ```java
    package com.baeldung.accessmodifiers;

    public class SuperPublic {
        static private void privateMethod() {
            ...
        }
        
        private void anotherPrivateMethod() {
            privateMethod(); // available in the same class only.
        }
    }
    ```

5. 受保护的

    在公有和私有访问级别之间，有一个受保护的访问修改器。

    如果我们用protected关键字声明一个方法、属性或构造函数，我们可以从同一个包中访问这个成员（和包-私有访问级别一样），此外还可以从其类的所有子类中访问，即使它们位于其他包中：

    ```java
    package com.baeldung.accessmodifiers;

    public class SuperPublic {
        static protected void protectedMethod() {
            ...
        }
    }
    ```

    protectedMethod()在子类中是可用的（不管是什么包）：

    ```java
    package com.baeldung.accessmodifiers.another;

    import com.baeldung.accessmodifiers.SuperPublic;

    public class AnotherSubClass extends SuperPublic {
        public AnotherSubClass() {
            SuperPublic.protectedMethod(); // Available in subclass. Let's note different package.
        }
    }
    ```

6. 比较

    下表总结了可用的访问修改器。我们可以看到，一个类，无论使用何种访问修饰符，总是可以访问其成员：

    | Modifier  | Class | Package | Subclass | World |
    |-----------|-------|---------|----------|-------|
    | public    | Y     | Y       | Y        | Y     |
    | protected | Y     | Y       | Y        | N     |
    | default   | Y     | Y       | N        | N     |
    | private   | Y     | N       | N        | N     |

7. 总结

    在这篇短文中，我们讨论了Java中的访问修改器。

    对任何给定的成员使用最严格的访问级别以防止滥用是一个好的做法。我们应该始终使用私有访问修饰符，除非有很好的理由不这样做。

    只有当一个成员是API的一部分时，才应该使用公共访问级别。
