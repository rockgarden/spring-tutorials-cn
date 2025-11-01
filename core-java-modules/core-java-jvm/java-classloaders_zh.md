# [Java中的类加载器](https://www.baeldung.com/java-classloaders)

1. 类加载器简介

    类加载器负责在运行时将 Java 类动态加载到 JVM（Java 虚拟机）中。它们也是 JRE（Java 运行时环境）的一部分。因此，有了类加载器，JVM 不需要了解底层文件或文件系统就能运行 Java 程序。

    此外，这些 Java 类不会一次性加载到内存中，而是在应用程序需要时才加载。这就是类加载器的作用所在。它们负责将类加载到内存中。

    在本教程中，我们将讨论不同类型的内置类加载器及其工作原理。然后，我们将介绍自己的自定义实现。

    [了解 Java 中的内存泄露](https://www.baeldung.com/java-memory-leaks)

    了解 Java 中的内存泄漏、如何在运行时识别内存泄漏、造成内存泄漏的原因以及防止内存泄漏的策略。

    [ClassNotFoundException与NoClassDefFoundError](https://www.baeldung.com/java-classnotfoundexception-and-noclassdeffounderror)

    了解 ClassNotFoundException 与 NoClassDefFoundError 的区别。

2. 内置类加载器的类型

    让我们从学习如何使用各种类加载器加载不同的类开始：

    classloader/PrintClassLoader.java

    执行上述方法时，将打印

    ```log
    Classloader of this class:jdk.internal.loader.ClassLoaders$AppClassLoader@73d16e93
    Classloader of DriverManager:jdk.internal.loader.ClassLoaders$PlatformClassLoader@1fb700ee
    Classloader of ArrayList:null
    ```

    我们可以看到，这里有三个不同的类加载器：应用程序、扩展和引导（显示为 null）。

    应用程序类加载器加载的是包含示例方法的类。应用程序或系统类加载器在类路径中加载我们自己的文件。

    接下来，扩展类加载器会加载 DriverManager 类。扩展类加载器加载标准 Java 核心类的扩展类。

    最后，引导类加载器加载 ArrayList 类。引导类加载器或原始类加载器是所有其他类的父类。

    不过，我们可以看到 ArrayList 在输出中显示为空。这是因为引导类加载器是用本地代码而不是 Java 编写的，所以它不会显示为 Java 类。因此，引导类加载器的行为在不同的 JVM 中会有所不同。

    现在我们来详细讨论一下这些类加载器。

    1. 引导类加载器(Bootstrap Class Loader)

        Java 类由 java.lang.ClassLoader 实例加载。然而，类加载器本身就是类。那么问题来了，谁来加载 java.lang.ClassLoader 本身呢？

        这就是引导或原始类加载器发挥作用的地方。

        它主要负责加载 JDK 内部类，通常是位于 $JAVA_HOME/jre/lib 目录下的 rt.jar 和其他核心库。此外，Bootstrap 类加载器还是所有其他 ClassLoader 实例的父类。

        如上例所示，Bootstrap 类加载器是核心 JVM 的一部分，由本地代码编写。不同的平台可能有不同的类加载器实现。

    2. 扩展类加载器(Extension Class Loader)

        扩展类加载器是引导类加载器的子类，负责加载标准核心 Java 类的扩展类，以便平台上运行的所有应用程序都能使用它们。

        扩展类加载器从 JDK 扩展目录（通常是 $JAVA_HOME/lib/ext 目录）或 java.ext.dirs 系统属性中提到的任何其他目录加载扩展类。

    3. 系统类加载器(System Class Loader)

        另一方面，系统或应用程序类加载器负责将所有应用程序级类加载到 JVM 中。它加载在 classpath 环境变量、-classpath 或 -cp 命令行选项中找到的文件。它也是扩展类加载器的子程序。

3. 类加载器如何工作？

    类加载器是 Java 运行时环境的一部分。当 JVM 请求一个类时，类加载器会尝试定位该类，并使用完全限定的类名将类定义加载到运行时中。

    java.lang.ClassLoader.loadClass() 方法负责将类定义加载到运行时。它会尝试根据全限定类名加载类。

    如果类尚未加载，它就会将请求委托给父类加载器。这个过程是递归进行的。

    最后，如果父类加载器找不到类，子类就会调用 java.net.URLClassLoader.findClass() 方法在文件系统中查找类。

    如果最后一个子类加载器也无法加载类，则会抛出 java.lang.NoClassDefFoundError 或 java.lang.ClassNotFoundException 异常。

    让我们看看抛出 ClassNotFoundException 时的输出示例：

    ```java
    java.lang.ClassNotFoundException: com.baeldung.classloader.SampleClassLoader    
        at java.net.URLClassLoader.findClass(URLClassLoader.java:381)    
        at java.lang.ClassLoader.loadClass(ClassLoader.java:424)    
        at java.lang.ClassLoader.loadClass(ClassLoader.java:357)    
        at java.lang.Class.forName0(Native Method)    
        at java.lang.Class.forName(Class.java:348)
    ```

    如果我们回顾一下从调用 java.lang.Class.forName() 开始的事件序列，我们可以看到它首先尝试通过父类加载器加载类，然后通过 java.net.URLClassLoader.findClass() 查找类本身。

    如果仍然找不到类，就会抛出 ClassNotFoundException 异常。

    现在我们来看看类加载器的三个重要特性。

    1. 委托模式

        类加载器遵循委托模式，即当请求查找类或资源时，ClassLoader实例将委托父类加载器搜索类或资源。

        比方说，我们请求将一个应用程序类加载到JVM中。系统类加载器首先将该类的加载委托给其父扩展类加载器，父扩展类加载器再将其委托给引导类加载器。

        只有当引导类加载器和扩展类加载器都无法成功加载类时，系统类加载器才会尝试自己加载类。

    2. 唯一类

        作为委托模式的结果，确保类的唯一性很容易，因为我们总是尝试向上委托。

        如果父类加载器无法找到该类，当前实例才会尝试自行加载。

    3. 可见性

        此外，子类加载器对父类加载器加载的类是可见的。

        例如，系统类加载器加载的类对扩展类加载器和引导类加载器加载的类可见，反之则不可见。

        举例说明，如果类 A 由应用程序类加载器加载，类 B 由扩展类加载器加载，那么就应用程序类加载器加载的其他类而言，A 类和 B 类都是可见的。

        而对于扩展类加载器加载的其他类来说，只有 B 类是可见的。

4. 自定义类加载器

    内置类加载器足以满足文件系统中已有文件的大多数情况。

    但是，在需要从本地硬盘或网络加载类的情况下，我们可能需要使用自定义类加载器。

    在本节中，我们将介绍自定义类加载器的一些其他用例，并演示如何创建自定义类加载器。

    1. 自定义类加载器用例

        自定义类加载器的作用不仅仅是在运行时加载类。一些用例可能包括

        - 帮助修改现有字节码，如编织代理
        - 根据用户需求动态创建类，例如在 JDBC 中，通过动态类加载在不同驱动程序实现之间进行切换。
        - 在为具有相同名称和包的类加载不同字节码时，实施类版本机制。这可以通过 URL 类加载器（通过 URL 加载 jars）或自定义类加载器来实现。

        下面是自定义类加载器可能派上用场的更具体例子。

        例如，浏览器使用自定义类加载器从网站加载可执行内容。浏览器可以使用单独的类加载器从不同网页加载小程序。用于运行小程序的小程序查看器包含一个类加载器，它可以访问远程服务器上的网站，而不是在本地文件系统中查找。

        然后，它通过 HTTP 加载原始字节码文件，并在 JVM 中将其转化为类。即使这些小程序具有相同的名称，如果由不同的类加载器加载，它们也会被视为不同的组件。

        现在我们明白了为什么要使用自定义类加载器，让我们实现一个 ClassLoader 的子类来扩展和总结 JVM 如何加载类的功能。

    2. 创建自定义类加载器

        为了便于说明，假设我们需要使用自定义类加载器从文件中加载类。

        我们需要扩展 ClassLoader 类并覆盖 findClass() 方法：

        classloader/CustomClassLoader.java

        在上例中，我们定义了一个自定义类加载器，它扩展了默认类加载器，并从指定文件中加载一个字节数组。

5. 了解 java.lang.ClassLoader

    让我们讨论一下 java.lang.ClassLoader 类中的几个基本方法，以便更清楚地了解它是如何工作的。

    1. loadClass() 方法

        `public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {}`

        该方法负责加载给定名称参数的类。name 参数是指完全限定的类名。

        Java 虚拟机会调用 loadClass() 方法来解析类引用，并将 resolve 设置为 true。不过，并不总是有必要解析一个类。如果我们只需要确定类是否存在，那么 resolve 参数就会被设置为 false。

        该方法是类加载器的入口点。

        我们可以尝试从 java.lang.ClassLoader 的源代码中了解 loadClass() 方法的内部工作原理：

        ```java
        protected Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    long t0 = System.nanoTime();
                        try {
                            if (parent != null) {
                                c = parent.loadClass(name, false);
                            } else {
                                c = findBootstrapClassOrNull(name);
                            }
                        } catch (ClassNotFoundException e) {
                            // ClassNotFoundException thrown if class not found
                            // from the non-null parent class loader
                        }
                        if (c == null) {
                            // If still not found, then invoke findClass in order
                            // to find the class.
                            c = findClass(name);
                        }
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            }
        ```

        该方法的默认实现按以下顺序搜索类：

        1. 调用 findLoadedClass(String) 方法查看类是否已加载。
        2. 调用父类加载器上的 loadClass(String) 方法。
        3. 调用 findClass(String) 方法查找类。

    2. defineClass() 方法

        `protected final Class<?> defineClass(String name, byte[] b, int off, int len) throws ClassFormatError`

        该方法负责将字节数组转换为类的实例。在使用该类之前，我们需要解析它。

        如果数据不包含有效的类，就会抛出 ClassFormatError。

        此外，我们不能覆盖此方法，因为它被标记为最终方法。

    3. findClass() 方法

        `protected Class<?> findClass(String name) throws ClassNotFoundException`

        该方法以完全限定的名称为参数查找类。我们需要在遵循委托模式加载类的自定义类加载器实现中覆盖此方法。

        此外，如果父类加载器找不到请求的类，loadClass() 会调用此方法。

        如果类加载器的父类找不到该类，默认实现会抛出 ClassNotFoundException 异常。

    4. getParent() 方法

        `public final ClassLoader getParent()`

        此方法返回父类加载器以进行委托。

        有些实现，如第 2 节中的实现，使用 null 表示引导类加载器。

    5. getResource() 方法

        `public URL getResource(String name)`

        该方法尝试查找具有给定名称的资源。

        它将首先委托给资源的父类加载器。如果父类加载器为空，则搜索虚拟机内置类加载器的路径。

        如果失败，该方法将调用 findResource(String) 查找资源。作为输入指定的资源名称可以是类路径的相对或绝对路径。

        它将返回一个用于读取资源的 URL 对象，如果找不到资源或调用者没有足够的权限返回资源，则返回空值。

        值得注意的是，Java 从类路径加载资源。

        最后，Java 中的资源加载与位置无关，因为只要环境设置为可以找到资源，代码在哪里运行并不重要。

6. 上下文类加载器

    一般来说，上下文类加载器提供了一种替代 J2SE 中引入的类加载委托方案的方法。

    就像我们以前学过的，JVM 中的类加载器遵循一个分层模型，即除了引导类加载器外，每个类加载器都有一个父类。

    不过，有时当 JVM 核心类需要动态加载应用程序开发人员提供的类或资源时，我们可能会遇到问题。

    例如，在 JNDI 中，核心功能由 rt.jar 中的引导类实现。但这些 JNDI 类可能会加载由独立供应商实现的 JNDI 提供程序（部署在应用程序 classpath 中）。这种情况要求引导类加载器（父类加载器）加载应用程序加载器（子类加载器）可见的类。

    J2SE 委托在这里不起作用，为了解决这个问题，我们需要找到其他的类加载方式。这可以通过线程上下文加载器来实现。

    java.lang.Thread 类有一个方法 getContextClassLoader()，用于返回特定线程的 ContextClassLoader。ContextClassLoader 由线程创建者在加载资源和类时提供。

    如果未设置该值，则默认使用父线程的类加载器上下文。

7. 结论

    类加载器对于执行 Java 程序至关重要。本文很好地介绍了类加载器。

    我们讨论过不同类型的类加载器，即 Bootstrap、Extensions 和 System 类加载器。Bootstrap 是所有类加载器的父类，负责加载 JDK 内部类。而 Extensions 和 System 则分别从 Java extensions 目录和 classpath 中加载类。

    我们还了解了类加载器的工作原理，并检查了一些特性，如委托、可见性和唯一性。然后，我们简要介绍了如何创建自定义类加载器。最后，我们介绍了上下文类加载器。
