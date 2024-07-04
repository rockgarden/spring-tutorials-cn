# Core Java Lang Syntax

## Java原语介绍

1. 概述

    Java编程语言有8种原始数据类型。

    在本教程中，我们将看看这些基元是什么，并对每个类型进行介绍。

2. 原始数据类型

    在Java中定义的八个基元是int、byte、short、long、float、double、boolean和char。这些并不被认为是对象，而是代表原始值。

    它们直接存储在堆栈中（查看这篇[文章](https://www.baeldung.com/java-initialization)以了解更多关于Java内存管理的信息）。

    我们将看一下存储大小、默认值和如何使用每种类型的例子。

    让我们从一个快速参考开始。

    类型 大小（比特） 最小 最大 示例
    | Type    | Size (bits) | Minimum | Maximum        | Example                       |
    |---------|-------------|---------|----------------|-------------------------------|
    | byte    | 8           | -2^7^     | 2^7^– 1         | byte b = 100;                 |
    | short   | 16          | -2^15^    | 2^15^– 1         | short s = 30_000;             |
    | int     | 32          | -2^31^    | 2^31^– 1         | int i = 100_000_000;          |
    | long    | 64          | -2^63^    | 2^63^– 1         | long l = 100_000_000_000_000; |
    | float   | 32          | -2^149^    | (2-2^-23^)·2^127^  | float f = 1.456f;             |
    | double  | 64          | -2^1074^   | (2-2^-52^)·2^1023^ | double f = 1.456789012345678; |
    | char    | 16          | 0       | 2^16^– 1         | char c = ‘c';                 |
    | boolean | 1           | –       | –              | boolean b = true;             |

    1. int

        我们要讨论的第一个原始数据类型是int。int类型也被称为整数，它可以容纳各种非分数的数字值。

        具体来说，Java使用32位的内存来存储它。换句话说，它可以表示从-2,147,483,648（-2^31^）到2,147,483,647（2^31^-1）的数值。

        在Java 8中，通过使用新的特殊辅助函数，可以将无符号整数值存储到4,294,967,295（2^32^-1）。

        我们可以简单地声明一个int。

        int x = 424_242;
        int y;

        没有赋值的int声明的默认值是0。

        如果该变量被定义在一个方法中，我们必须在使用它之前赋值。

        我们可以对int进行所有标准的算术运算。只是要注意，在对整数进行这些操作时，小数值会被砍掉。

    2. 字节

        byte是一种类似于int的原始数据类型，只是它只占用8位内存。这就是为什么我们称它为byte。因为内存大小太小，byte只能容纳从-128（-2^7^）到127（2^7^-1）的值。

        下面是我们如何创建byte。

        byte b = 100;
        byte empty;

        byte的默认值也是0。

    3. short

        如果我们想节省内存而byte又太小，我们可以使用介于byte和int之间的类型：short。

        在16比特的内存中，它是int的一半，是字节的两倍。它的可能取值范围是-32,768(-2^15^)到32,767(2^15^-1)。

        short是这样声明的。

        short s = 20_020;
        short s;

        与其他类型类似，其默认值为0，我们也可以对其使用所有标准的算术。

    4. long

        long是int的老大哥。它被存储在64位的内存中，所以它可以保存一个大得多的可能值。

        long的可能值在-9,223,372,036,854,775,808（-2^63^）到9,223,372,036,854,775,807（2^63^-1）之间。

        我们可以简单地声明一个。

        long l = 1_234_567_890;
        long l;

        和其他整数类型一样，默认值也是0，我们可以在long上使用所有对int有效的算术。

    5. float

        我们在Java中使用float类型来表示基本的小数。这是一个单精度的十进制数。这意味着如果我们超过了六个小数点，这个数字就会变得不那么精确，而更像是一种估计。

        在大多数情况下，我们并不关心精度的损失。但如果我们的计算需要绝对的精度（例如，金融操作、登陆月球等），我们需要使用为这项工作设计的特定类型。欲了解更多信息，请查看Java类[大十进制](https://www.baeldung.com/java-bigdecimal-biginteger)。

        这种类型就像int一样被存储在32位的内存中。然而，由于是浮动的小数点，它的范围有很大的不同。它既可以表示正数，也可以表示负数。最小的小数是1.40239846 x 10^-45^，最大的值是3.40282347 x 10^38^。

        我们声明浮点数与其他类型相同。

        float f = 3.145f;
        float f;

        而默认值是0.0而不是0。另外，注意我们在字面数字的末尾加上f的指定来定义浮点数。否则，Java会抛出一个错误，因为十进制值的默认类型是double。

        我们还可以对浮点数进行所有标准的算术运算。然而，需要注意的是，我们进行浮点运算的方式与整数运算非常不同。

    6. double

        接下来，我们看一下double。它的名字来自于它是一个双精度的十进制数字的事实。

        它被存储在64位的内存中。这意味着它代表的可能数字范围比float大得多。

        虽然，它确实受到与float一样的精度限制。其范围是4.9406564584124654 x 10^-324^到1.7976931348623157 x 10^308^。这个范围也可以是正数或负数。

        声明double与其他数字类型是一样的。

        double d = 3.134575999233847539348D;
        double d;

        和float一样，默认值也是0.0。与float类似，我们附加字母D来指定字面为双数。

    7. 布尔型

        最简单的原始数据类型是布尔型。它只能包含两个值：真或假。它的值存储在一个单一的位中。

        然而，为了方便起见，Java对该值进行填充，并将其存储在一个字节中。

        下面是我们如何声明布尔值。

        boolean b = true;
        boolean b;

        在没有值的情况下声明它，默认为false。 布尔值是控制我们程序流程的基石。我们可以对其使用布尔运算符（例如，和、或等）。

    8. char

        最后要看的原始数据类型是char。

        char也叫字符，是一个16位的整数，代表一个Unicode编码的字符。它的范围是0到65,535。在Unicode中，这代表'\u0000'到'\uffff'。

        关于所有可能的Unicode值的列表，请查看[Unicode表](https://unicode-table.com/en/)等网站。

        现在我们来声明一个char。

        char c = 'a';
        char c = 65;
        char c;

        在定义我们的变量时，我们可以使用任何字符字面，它们会自动为我们转换为Unicode的编码。一个字符的默认值是'/u0000'。

    9. Overflow溢出

        原始数据类型有大小限制。但如果我们试图存储一个大于最大值的值，会发生什么？

        我们会遇到一种叫做溢出的情况。

        当一个整数溢出时，它会滚到最小值，并从那里开始向上计数。

        浮点数的溢出是返回无穷大。

        int i = Integer.MAX_VALUE;
        int j = i + 1;
        // j will roll over to -2_147_483_648

        double d = Double.MAX_VALUE;
        double o = d + 1;
        // o will be Infinity

        下溢也是同样的问题，只是它涉及到存储一个比最小值小的值。当数字下溢时，它们会返回0.0。

    10. Autoboxing 自动排版

        每个原始数据类型也有一个完整的Java类实现，可以包裹它。例如，Integer类可以包装一个int。有时需要从原始类型转换到它的对象封装器（例如，将它们用于[泛型](https://www.baeldung.com/java-generics)）。

        幸运的是，Java可以自动为我们进行这种转换，这个过程被称为 "Autoboxing"。

        Character c = 'c';
        Integer i = 1;

3. 总结

    在这篇文章中，我们已经介绍了Java中支持的八种原始数据类型。

    这些是大多数（如果不是全部）Java程序所使用的构件，所以很值得了解它们的工作原理。

## Java的main()方法详解

1. 概述

    每个程序都需要一个地方来开始执行；说到Java程序，那就是main方法。
    我们在编写代码时习惯于写main方法，以至于我们甚至没有注意到它的细节。在这篇简短的文章中，我们将分析这个方法，并展示一些其他的编写方法。

2. 常见的签名

    最常见的main方法模板是。

    `public static void main(String[] args) { }`

    这就是我们学习的方式，这就是IDE为我们自动完成代码的方式。但这并不是这个方法的唯一形式，还有一些有效的变体我们可以使用，而不是每个开发者都会注意到这个事实。

    在我们深入研究这些方法的签名之前，让我们回顾一下普通签名中每个关键词的含义。

    - public--访问修改器，意味着全局可见
    - static--该方法可以直接从类中访问，我们不必实例化一个对象来获得一个引用并使用它
    - void - 意味着这个方法不会返回一个值
    - main - 方法的名称，这是JVM在执行Java程序时寻找的标识符。

    至于args参数，它表示方法所接收的值。这就是我们在第一次启动程序时向其传递参数的方式。

    参数args是一个字符串数组。在下面的例子中。

    `java CommonMainMethodSignature foo bar`

    我们正在执行一个名为CommonMainMethodSignature的Java程序，并传递两个参数：foo和bar。这些值可以在main方法中作为`args[0]`（以foo为值）和`args[1]`（以bar为值）被访问。

    在下一个例子中，我们要检查args来决定是加载测试还是生产参数。

    ```java
    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("test")) {
                // load test parameters
            } else if (args[0].equals("production")) {
                // load production parameters
            }
        }
    }
    ```

    记住，IDE也可以向程序传递参数，这一点总是很好。

3. 写main()方法的不同方法

    让我们看看写main方法的一些不同方式。虽然它们不是很常见，但都是有效的签名。

    请注意，这些都不是专门针对main方法的，它们可以用于任何Java方法，但它们也是main方法的有效部分。

    方括号可以放在String附近，就像常见的模板一样，也可以放在两侧的args附近。

    `public static void main(String []args) { }`

    `public static void main(String args[]) { }`

    参数可以用varargs来表示。

    `public static void main(String...args) { }`

    我们甚至可以为main()方法添加strictfp，在处理浮点值时，它用于处理器之间的兼容。

    `public strictfp static void main(String[] args) { }`

    synchronized和final也是main方法的有效关键字，但它们在这里不会产生影响。

    另一方面，final可以应用于args，以防止数组被修改。

    `public static void main(final String[] args) { }`

    为了结束这些例子，我们也可以用上述所有的关键字来写main方法（当然，在实际应用中你可能永远不会用到这些关键字）。

    final static synchronized strictfp void main(final String[] args) { }

4. 拥有一个以上的main()方法

    我们也可以在我们的应用程序中定义一个以上的main方法。

    事实上，有些人把它作为一种原始的测试技术来验证单个的类（尽管像JUnit这样的测试框架更适合这种活动）。

    为了指定JVM应该执行哪个主方法作为我们应用程序的入口，我们使用MANIFEST.MF文件。在清单中，我们可以指明主类。

    主类： mypackage.ClassWithMainMethod

    这主要是在创建可执行的.jar文件时使用。我们通过位于META-INF/MANIFEST.MF的清单文件（以UTF-8编码），指出哪个类有主方法来启动执行。

5. 总结

    本教程描述了main方法的细节以及它可以采取的一些其他形式，甚至是那些对大多数开发者来说并不常见的形式。

    请记住，尽管我们所展示的所有例子在语法上都是有效的，但它们只是起到教育作用，大多数时候我们会坚持使用常见的签名来完成我们的工作。

## 泛型基础

1. 概述

    JDK 5.0 引入了 Java 生成器，目的是减少错误并为类型添加额外的抽象层。

    本教程将快速介绍 Java 中的泛型、泛型背后的目标以及泛型如何提高代码质量。

    进一步阅读：

    [Java 中的方法引用](https://www.baeldung.com/java-method-references)

    Java 方法引用的快速实用概述。

    [使用反射从 Java 类中获取字段](https://www.baeldung.com/java-reflection-class-fields)

    了解如何使用反射获取类的字段，包括继承字段

2. 泛型的必要性

    让我们设想一个场景：我们想在 Java 中创建一个列表来存储整数。

    我们可能会尝试写如下代码：

    ```java
    List list = new LinkedList();
    list.add(new Integer(1)); 
    Integer i = list.iterator().next();
    ```

    令人惊讶的是，编译器会抱怨最后一行。它不知道返回的是什么数据类型。

    编译器会要求进行显式转换：

    `Integer i = (Integer) list.iterator.next();`

    没有任何契约可以保证 list 的返回类型是 Integer。定义的 list 可以容纳任何对象。我们只有通过检查上下文才能知道我们正在检索一个 list。在查看类型时，它只能保证是对象，因此需要显式转换以确保类型安全。

    这种类型转换很烦人--我们知道列表中的数据类型是整数。同时，这种类型转换也会使我们的代码变得杂乱无章。如果程序员在显式转换时出错，可能会导致与类型相关的运行时错误。

    如果程序员能表达他们使用特定类型的意图，而编译器能确保这些类型的正确性，那么事情就简单多了。这就是泛型的核心思想。

    让我们修改前面代码片段的第一行：

    `List<Integer> list = new LinkedList<>();`

    通过添加包含类型的菱形运算符 <>，我们将此列表的特化范围缩小到了整数类型。换句话说，我们指定了列表内部的类型。编译器可以在编译时强制执行该类型。

    在小型程序中，这似乎只是一个微不足道的补充。但在较大的程序中，这可以大大提高健壮性，并使程序更易于阅读。

3. 泛型方法

    我们在编写泛型方法时只需声明一个方法，就可以用不同类型的参数调用它们。无论我们使用哪种类型，编译器都会确保其正确性。

    以下是泛型方法的一些特性：

    - 泛型方法在方法声明的返回类型之前有一个类型参数（包围类型的菱形运算符）。
    - 类型参数可以有边界。
    - 泛型方法可以有不同的类型参数，在方法签名中用逗号分隔。
    - 泛型方法的方法体与普通方法一样。

    下面是一个定义泛型方法将数组转换为列表的示例：

    ```java
    public <T> List<T> fromArrayToList(T[] a) {   
        return Arrays.stream(a).collect(Collectors.toList());
    }
    ```

    方法签名中的 `<T>` 意味着该方法将处理泛型 T。即使方法返回 void 也需要这样做。

    如前所述，方法可以处理不止一种泛型。在这种情况下，我们必须在方法签名中添加所有的泛型类型。

    下面是我们如何修改上述方法以处理 T 和 G 类型：

    ```java
    public static <T, G> List<G> fromArrayToList(T[] a, Function<T, G> mapperFunction) {
        return Arrays.stream(a)
        .map(mapperFunction)
        .collect(Collectors.toList());
    }
    ```

    我们传递的函数可以将元素类型为 T 的数组转换为元素类型为 G 的列表。

    例如，将 Integer 转换为字符串表示：

    GenericsUnitTest.java: givenArrayOfIntegers_thanListOfStringReturnedOK()

    > 请注意，Oracle 建议使用大写字母来表示泛型，而选择更具描述性的字母来表示正式类型。在 Java 集合中，我们使用 T 表示类型，K 表示键，V 表示值。

    1. 有界泛型

        请记住，类型参数可以是有界的。Bounded 意味着 "受限(restricted)"，我们可以限制方法接受的类型。

        例如，我们可以指定一个方法接受一个类型及其所有子类（上界）或一个类型及其所有超类（下界）。

        要声明有上界的类型，我们可以在类型后使用关键字 extends，然后再加上我们要使用的上界：

        `public <T extends Number> List<T> fromArrayToList(T[] a) { ...}`

        我们在此使用关键字 extends 的意思是，如果是类，类型 T 扩展了上界；如果是接口，则实现了上界。

    2. 多重上界

        一个类型也可以有多个上界：

        `<T extends Number & Comparable>`

        如果由 T 扩展的类型之一是一个类（例如 Number），我们必须把它放在边界列表的首位。否则会导致编译错误。

4. 在泛型中使用通配符

    通配符(Wildcards)由问号表示`?`在Java中，我们使用它们来指代未知类型。通配符对泛型特别有用，可以用作参数类型。

    但首先，有一个重要的注意事项需要考虑。我们知道Object是所有Java类的超类型。但是，Object的集合不是任何集合的超类型。

    例如，`List＜Object＞`不是`List＜String＞`的超类型，将类型为`List＜Object>`的变量分配给类型为`List<String>`的变量将导致编译器错误。这是为了防止在将异构类型添加到同一集合时可能发生的冲突。

    相同的规则适用于类型及其子类型的任何集合。

    考虑这个例子：

    ```java
    public static void paintAllBuildings(List<Building> buildings) {
        buildings.forEach(Building::paint);
    }
    ```

    如果我们想象一个 "建筑物" 的子类型，例如房屋，我们就不能在房屋列表中使用此方法，尽管房屋是 "建筑物" 的子类型。

    如果我们需要将此方法用于 Building 类型及其所有子类型，那么有界通配符就能发挥神奇的作用：

    `public static void paintAllBuildings(List<? extends Building> buildings) {...}`

    现在，此方法将适用于 Building 类型及其所有子类型。这称为上界通配符(upper-bounded wildcard)，其中 Building 类型是上界。

    我们还可以指定有下限的通配符，其中未知类型必须是指定类型的超类型。下限可以使用 super 关键字指定，后面跟特定类型。例如，<? super T> 表示未知类型是 T 的超类（= T 及其所有父类）。

5. 类型擦除

    Java 中加入泛型是为了确保类型安全。为了确保泛型不会在运行时造成开销，编译器会在编译时对泛型进行一个称为类型擦除的处理。

    类型擦除会移除所有类型参数，并用它们的边界(bounds)或 Object（如果类型参数是无边界的）代替。这样，编译后的字节码就只包含正常的类、接口和方法，确保不会产生新的类型。在编译时，也会对对象类型进行适当的转换。

    这是类型擦除的一个例子：

    ```java
    public <T> List<T> genericMethod(List<T> list) {
        return list.stream().collect(Collectors.toList());
    }
    ```

    通过类型擦除，无界类型 T 被 Object 代替：

    ```java
    // for illustration
    public List<Object> withErasure(List<Object> list) {
        return list.stream().collect(Collectors.toList());
    }

    // which in practice results in
    public List withErasure(List list) {
        return list.stream().collect(Collectors.toList());
    }
    ```

    如果类型是有边界的，则在编译时将用边界替换类型：

    `public <T extends Building> void genericMethod(T t) {...}`

    并在编译后发生变化：

    `public void genericMethod(Building t) {...}`

6. 泛型与基元数据类型

    Java 中的泛型有一个限制，即类型参数不能是原始类型(primitive type)。

    例如，以下代码无法编译：

    ```java
    List<int> list = new ArrayList<>();
    list.add(17);
    ```

    要理解原始数据类型无法工作的原因，我们要记住，泛型是一种编译时特性，这意味着类型参数会被删除，所有泛型类型都以 Object 类型实现。

    让我们来看看 list 的 add 方法：

    ```java
    List<Integer> list = new ArrayList<>();
    list.add(17);
    ```

    add 方法的签名是

    `boolean add(E e);`

    并将编译为

    `boolean add(Object e);`

    因此，类型参数必须可转换为 Object。由于基元类型(primitive types)不扩展 Object，因此我们不能将其用作类型参数。

    不过，Java 为基元类型提供了盒式类型(boxed types)，并提供了自动盒式化(autoboxing)和开盒式化(unboxing)来解除盒式化：

    ```java
    Integer a = 17;
    int b = a;
    ```

    因此，如果我们想创建一个可以保存整数的 list，可以使用这个封装器：

    ```java
    List<Integer> list = new ArrayList<>();
    list.add(17);
    int first = list.get(0);
    ```

    编译后的代码相当于以下代码：

    ```java
    List list = new ArrayList<>();
    list.add(Integer.valueOf(17));
    int first = ((Integer) list.get(0)).intValue();
    ```

    未来版本的 Java 可能会允许使用原始数据类型进行泛型。[Valhalla](http://openjdk.java.net/projects/valhalla/) 项目旨在改进处理泛型的方式。我们的想法是按照 [JEP 218](http://openjdk.java.net/jeps/218) 中的描述实现泛型特化。

7. 结论

    Java 泛型是对 Java 语言的有力补充，因为它使程序员的工作更轻松、更不易出错。泛型可在编译时强制执行类型正确性，最重要的是，它可以实现泛型算法，而不会给我们的应用程序带来任何额外的开销。

## 在Java中创建对象的指南

1. 概述

    简单地说，在我们能够在JVM上使用一个对象之前，它必须被初始化。

    在本教程中，我们将研究初始化原始类型和对象的各种方法。

2. 声明与初始化

    让我们首先确保我们在同一个页面上。

    声明是定义变量的过程，包括其类型和名称。

    这里我们要声明id变量：

    `int id;`

    另一方面，初始化是指分配一个值：

    `id = 1;`

    为了演示，我们将创建一个带有名字和id属性的用户类：

    initializationguide/User.java

    接下来，我们将看到初始化的工作方式因我们要初始化的字段类型而不同。

3. 对象与基元

    Java提供了两种类型的数据表示：原始类型和引用类型。在本节中，我们将讨论两者在初始化方面的区别。

    Java有八种内置的数据类型，被称为Java原始类型；这种类型的变量直接持有其值。

    引用类型持有对对象（类的实例）的引用。不像原始类型在分配变量的内存中持有它们的值，引用不持有它们所引用的对象的值。

    相反，引用通过存储对象所在的内存地址指向一个对象。

    注意，Java不允许我们发现物理内存地址是什么。相反，我们只能用引用来指代对象。

    让我们看一个例子，它声明并初始化了一个来自用户类的引用类型：

    UserUnitTest.java whenIntializedWithNew_thenInstanceIsNotNull()

    我们可以看到，通过使用关键字new，可以将一个引用分配给一个新的对象，它负责创建新的用户对象。

4. 创建对象

    与基元不同，对象的创建要更复杂一些。这是因为我们不只是向字段添加值；相反，我们使用new关键字触发初始化。作为回报，这将调用一个构造函数并初始化内存中的对象。

    new关键字负责通过构造函数为新对象分配内存。

    构造函数通常用于初始化代表创建对象主要属性的实例变量。

    如果我们没有明确提供一个构造函数，编译器会创建一个默认的构造函数，它没有参数，只是为对象分配内存。

    一个类可以有很多构造函数，只要它们的参数列表是不同的（重载）。每一个不调用同一类中另一个构造函数的构造函数都有一个对其父级构造函数的调用，不管它是明确写的还是由编译器通过super()插入的。

    让我们给我们的User类添加一个构造函数：

    ```java
    public User(String name, int id) {
        this.name = name;
        this.id = id;
    }
    ```

    现在我们可以使用我们的构造函数来创建一个具有初始属性值的用户对象：

    `User user = new User("Alice", 1);`

5. 变量范围

    在下面的章节中，我们将看一下Java中变量可以存在的不同类型的作用域，以及这对初始化过程的影响。

    1. 实例和类变量

        实例变量和类变量不需要我们初始化它们。一旦我们声明这些变量，它们就会被赋予一个默认值。

        现在让我们试着定义一些与实例和类相关的变量，并测试它们是否有一个默认值：

        UserUnitTest.java givenUserInstance_whenValuesAreNotInitialized_thenUserNameAndIdReturnDefault()

    2. 本地变量

        本地变量在使用前必须被初始化，因为它们没有默认值，而且编译器不会让我们使用一个未初始化的值。

        例如，下面的代码会产生一个编译器错误：

        ```java
        public void print(){
            int i;
            System.out.println(i);
        }
        ```

6. 最终关键字

    应用于一个字段的final关键字意味着该字段的值在初始化后不能再被改变。通过这种方式，我们可以在Java中定义常量。

    让我们给我们的用户类添加一个常量：

    `private static final int YEAR = 2000;`

    常量必须在声明时或在构造函数中被初始化。

7. Java中的初始化器

    在Java中，初始化器是一个没有相关名称或数据类型的代码块，它被置于任何方法、构造函数或其他代码块之外。

    Java提供两种类型的初始化器：静态初始化器和实例初始化器。

    1. 实例初始化器

        我们可以用这些来初始化实例变量。

        为了演示，我们将在User类中使用一个实例初始化器为用户ID提供一个值：

        ```java
        {
            id = 0;
        }
        ```

    2. 静态初始化块

        静态初始化器，或静态块，是一个用于初始化静态字段的代码块。换句话说，它是一个简单的初始化器，标有关键字static：

        ```java
        private static String forum;
        static {
            forum = "Java";
        }
        ```

8. 初始化的顺序

    在编写初始化不同类型字段的代码时，我们必须注意初始化的顺序。

    在Java中，初始化语句的顺序是这样的：

    - 静态变量和静态初始化器的顺序
    - 实例变量和实例初始化器的顺序
    - 构造函数

9. 对象的生命周期

    现在我们已经学会了如何声明和初始化对象，让我们来发现当对象不被使用时，它们会发生什么。

    与其他语言不同的是，我们必须担心对象的销毁问题，Java通过其垃圾收集器来处理过时的对象。

    Java中的所有对象都存储在我们程序的堆内存中。事实上，堆代表了为我们的Java程序分配的一大批未使用的内存。

    另一方面，垃圾收集器是一个Java程序，它通过删除不再可及的对象来负责自动内存管理。

    对于一个Java对象来说，它必须遇到以下情况之一，才能变得不可触及：

    - 该对象不再有任何指向它的引用。
    - 所有指向该对象的引用都超出了范围。

    总之，一个对象首先是由一个类创建的，通常使用关键字new。然后，这个对象就开始了它的生活，并为我们提供了对其方法和字段的访问。

    最后，当它不再需要时，垃圾收集器就会将其销毁。

10. 创建对象的其他方法

    在这一节中，我们将简要地看一下除了new关键字以外的创建对象的方法，并学习如何应用它们，特别是反射、克隆和序列化。

    反射是一种机制，我们可以用来在运行时检查类、字段和方法。下面是一个使用反射创建我们的用户对象的例子：

    UserUnitTest.java givenUserInstance_whenInitializedWithReflection_thenInstanceIsNotNull()

    在这种情况下，我们使用反射来寻找和调用User类的构造函数。

    下一个方法，克隆，是一种创建一个对象的精确拷贝的方法。为此，我们的用户类必须实现Cloneable接口：

    `public class User implements Cloneable { //... }`

    现在我们可以使用clone()方法来创建一个新的clonedUser对象，它的属性值与用户对象相同：

    UserUnitTest.java givenUserInstance_whenCopiedWithClone_thenExactMatchIsCreated()

    我们还可以使用sun.misc.Unsafe类来为一个对象分配内存，而不调用构造函数：

    `User u = (User) unsafeInstance.allocateInstance(User.class);`

11. 总结

    在这篇文章中，我们介绍了Java中字段的初始化。然后我们研究了Java中不同的数据类型以及如何使用它们。我们还探讨了在Java中创建对象的几种方法。

## Java循环指南

1. 概述

    在这篇文章中，我们将研究Java语言的一个核心方面--使用循环重复执行一条或一组语句。

2. 循环的介绍

    在编程语言中，循环是一种促进执行一组指令的功能，直到控制的布尔表达式评估为假。

    Java提供了不同类型的循环以满足任何编程需要。每种循环都有自己的目的和合适的用例来服务。

    下面是我们在Java中可以找到的循环的类型。

    - 简单的for循环
    - 增强的for-each循环
    - While 循环
    - Do-While 循环

3. for 循环

    For 循环是一种控制结构，它允许我们通过增加和评估一个循环计数器来重复某些操作。

    关于详细的例子，请看专门的帖子。[Java For Loop](https://www.baeldung.com/java-for-loop)。

4. While 循环

    while循环是Java最基本的循环语句。它在其控制的布尔表达式为真时重复一个语句或一个语句块。

    关于详细的例子，请看专门的帖子。[Java While Loop](https://www.baeldung.com/java-while-loop)。

5. 暂时循环（Do-While Loop

    Do-while循环的工作原理与while循环相同，只是第一个条件的评估发生在循环的第一次迭代之后。

    关于详细的例子，请看专门的帖子。[Java Do-While Loop](https://www.baeldung.com/java-do-while-loop)。

6. 结语

    在这个快速教程中，我们展示了Java编程语言中可用的不同类型的循环。

    我们还看到，在合适的用例下，每个循环都有其特定的用途。我们讨论了适合于特定循环实现的情况。

    像往常一样，可以在[GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-syntax)上找到例子。

### Java For Loop

在这篇文章中，我们将看看Java语言的一个核心方面--使用for循环重复执行一条或一组语句。

1. 简单的for循环

    for 循环是一种控制结构，它允许我们通过增加和评估一个循环计数器来重复某些操作。

    在第一次迭代之前，循环计数器被初始化，然后进行条件评估，接着进行步骤定义（通常是简单的增量）。

    for循环的语法是。

    ```txt
    for (initialization; Boolean-expression; step) 
    statement;
    ```

    让我们在一个简单的例子中看到它。

    ```java
    for (int i = 0; i < 5; i++) {
        System.out.println("Simple for loop: i = " + i);
    }
    ```

    for语句中使用的初始化、布尔表达式和步骤是可选的。下面是一个无限for循环的例子。

    ```java
    for ( ; ; ) {
        // Infinite for loop
    }
    ```

    1. 带标签的for循环

        我们也可以有标记的for循环。如果我们有嵌套的for循环，这很有用，这样我们就可以从特定的for循环中break/continue下去。

        ```java
        aa: for (int i = 1; i <= 3; i++) {
            if (i == 1)
            continue;
            bb: for (int j = 1; j <= 3; j++) {
                if (i == 2 && j == 2) {
                    break aa;
                }
                System.out.println(i + " " + j);
            }
        }
        ```

2. 增强型for循环

    从Java 5开始，我们有了第二种for循环，叫做增强型for，它使我们更容易遍历数组或集合中的所有元素。

    增强型for循环的语法是。

    ```txt
    for(Type item : items)
    statement;
    ```

    由于这种循环与标准for循环相比是简化的，所以我们在初始化循环时只需要声明两件事。

    - 我们目前正在迭代的元素的句柄handle
    - 我们正在迭代的源数组/集合(array/collection)

    因此，我们可以这样说。对于 items 中的每个元素，将该元素分配给 item 变量，然后运行循环的主体。

    让我们看一下这个简单的例子。

    ```java
    int[] intArr = { 0,1,2,3,4 };
    for (int num : intArr) {
        System.out.println("Enhanced for-each loop: i = " + num);
    }
    ```

    我们可以用它来遍历各种Java数据结构。

    给定一个 `List<String>` list对象 - 我们可以对其进行迭代。

    ```java
    for (String item : list) {
        System.out.println(item);
    }
    ```

    我们同样可以对一个 `Set<String>` 集合进行迭代。

    ```java
    for (String item : set) {
        System.out.println(item);
    }
    ```

    而且，给定一个 `Map<String,Integer>` map，我们也可以迭代它。

    ```java
    for (Entry<String, Integer> entry : map.entrySet()) {
        System.out.println(
        "Key: " + entry.getKey() + 
        " - " + 
        "Value: " + entry.getValue());
    }
    ```

    1. Iterable.forEach()

        从Java 8开始，我们可以用一种稍微不同的方式来利用for-each循环。我们现在在Iterable接口中有一个专门的forEach()方法，它接受一个代表我们要执行的动作的lambda表达式。

        在内部，它只是将工作委托给了标准循环。

        ```java
        default void forEach(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            for (T t : this) {
                action.accept(t);
            }
        }
        ```

        让我们看一下这个例子。

        ```java
        List<String> names = new ArrayList<>();
        names.add("Larry");
        names.add("Steve");
        names.add("James");
        names.add("Conan");
        names.add("Ellen");
        names.forEach(name -> System.out.println(name));
        ```

3. 总结

    在这个快速教程中，我们探讨了Java的for循环。

    一如既往，我们可以在GitHub上找到一些例子。

## Relevant Articles

- [x] [Introduction to Java Primitives](https://www.baeldung.com/java-primitives)
- [x] [Java main() Method Explained](https://www.baeldung.com/java-main-method)
- [x] [The Basics of Java Generics](https://www.baeldung.com/java-generics)
- [Java Primitive Conversions](https://www.baeldung.com/java-primitive-conversions)
- [x] [A Guide to Creating Objects in Java](https://www.baeldung.com/java-initialization)
- [x] [A Guide to Java Loops](https://www.baeldung.com/java-loops)
- [Varargs in Java](https://www.baeldung.com/java-varargs)
- [Java Switch Statement](https://www.baeldung.com/java-switch)
- [Breaking Out of Nested Loops](https://www.baeldung.com/java-breaking-out-nested-loop)
- [Java Do-While Loop](https://www.baeldung.com/java-do-while-loop)
- [Java While Loop](https://www.baeldung.com/java-while-loop)
- [x] [Java For Loop](https://www.baeldung.com/java-for-loop)
- [[More -->]](/core-java-modules/core-java-lang-syntax-2)

## Code

这篇文章的完整实现可以在[Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-lang-syntax)上找到。
