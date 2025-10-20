# [Java 泛型基础](https://www.baeldung.com/java-generics)

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

   通配符(Wildcards)由问号表示`?`在 Java 中，我们使用它们来指代未知类型。通配符对泛型特别有用，可以用作参数类型。

   但首先，有一个重要的注意事项需要考虑。我们知道 Object 是所有 Java 类的超类型。但是，Object 的集合不是任何集合的超类型。

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
