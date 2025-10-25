# [Java9 模块化指南](https://www.baeldung.com/java-modularity)

核心 Java（≥ Java 8）· 定义

1. 概述

   Java 9 引入了一种位于包之上的新抽象层级，正式称为 Java 平台模块系统（Java Platform Module System, JPMS），简称 “模块（Modules）”。

   在本教程中，我们将深入探讨这一新系统，并详细讲解其各个方面。

   我们还将构建一个简单项目，以演示本指南中所学的所有概念。

2. 什么是模块？

   在学习如何使用模块之前，我们首先需要理解模块是什么。

   模块是一组紧密相关的包和资源，外加一个名为 `module-info.java` 的模块描述文件。

   换句话说，模块是一种“包的包”抽象，使我们的代码更具可重用性。

   1. 包（Packages）

      模块内部的包与 Java 自诞生以来一直使用的包完全相同。

      创建模块时，我们仍像以往一样，将代码组织在包中。

      除了组织代码，包还用于决定哪些代码可以对外公开访问。我们将在后文详细讨论这一点。

   2. 资源（Resources）

      每个模块负责管理自己的资源，如图片、配置文件等。

      过去，我们会将所有资源放在项目根目录下，并手动管理哪些资源属于应用的哪一部分。

      使用模块后，我们可以将所需的图像、XML 文件等与需要它们的模块一起打包，从而大幅简化项目管理。

   3. 模块描述文件（Module Descriptor）

      创建模块时，需在包根目录下包含一个名为 `module-info.java` 的描述文件，用于定义模块的多个方面：

      - **名称（Name）**：模块的名称
      - **依赖（Dependencies）**：本模块所依赖的其他模块列表
      - **公开包（Public Packages）**：希望对外暴露的包列表
      - **提供的服务（Services Offered）**：可被其他模块消费的服务实现
      - **消费的服务（Services Consumed）**：本模块作为消费者所使用的服务
      - **反射权限（Reflection Permissions）**：显式允许其他类通过反射访问本模块包的私有成员

      模块命名规则与包名类似（允许使用点号，不允许连字符）。常见命名方式有两种：

      - **项目风格**：如 `my.module`
      - **反向 DNS 风格**：如 `com.baeldung.mymodule`

      本指南将采用项目风格命名。

      > 注意：**默认情况下，所有包都是模块私有的**，必须显式声明为 `exports` 才能对外公开。  
      > 同样，默认**不允许通过反射访问其他模块的私有成员**。

      后文将通过示例展示如何使用模块描述文件。

   4. 模块类型

      新模块系统中共有四种模块类型：

      | 类型                                | 说明                                                                                                                |
      | ----------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
      | **系统模块（System Modules）**      | 运行 `java --list-modules` 命令列出的模块，包括 Java SE 和 JDK 模块                                                 |
      | **应用模块（Application Modules）** | 我们通常构建的模块，其名称和定义包含在编译后的 `module-info.class` 中，并打包在 JAR 内                              |
      | **自动模块（Automatic Modules）**   | 将传统 JAR 文件放入模块路径（module path）时自动生成的模块。模块名由 JAR 文件名推导而来，并可读取路径中所有其他模块 |
      | **未命名模块（Unnamed Module）**    | 放在类路径（classpath）而非模块路径中的类或 JAR 会被自动归入此模块，用于保持对旧版 Java 代码的向后兼容              |

   5. 分发方式

      模块可通过两种方式分发：

      - **JAR 文件**
      - **“展开式”编译项目（exploded directory）**

      这与传统 Java 项目一致。

      我们可以构建包含一个“主应用”和多个库模块的多模块项目。

      但需注意：每个 JAR 文件只能包含一个模块。

      因此，在构建配置中，必须确保项目中的每个模块都打包为独立的 JAR。

3. 默认模块

   安装 Java 9 后，你会发现 JDK 的结构发生了变化——所有原有包都被迁移到了新的模块系统中。

   可通过以下命令查看这些模块：

   ```bash
   java --list-modules
   ```

   这些模块分为四大类：

   - `java` 模块：Java SE 语言规范的核心实现类（如 `java.base`, `java.sql`）
   - `javafx` 模块：JavaFX UI 库
   - `jdk` 模块：JDK 自身所需的内部工具和 API
   - `oracle` 模块：Oracle 特有的实现（仅限 Oracle JDK）

4. 模块声明

   要创建模块，需在包根目录下创建一个名为 `module-info.java` 的文件，即**模块描述文件**。

   其基本结构如下：

   ```java
   module myModuleName {
       // 所有指令均为可选
   }
   ```

   使用 `module` 关键字声明模块，并指定模块名。

   虽然空模块也能工作，但通常我们需要添加**模块指令（directives）**来定义行为。

   1. `requires`

      声明模块依赖：

      ```java
      module my.module {
          requires module.name;
      }
      ```

      这表示 `my.module` 在**编译期和运行期**都依赖 `module.name`，并可访问其导出的所有公共类型。

   2. `requires static`（静态依赖）

      用于声明**可选依赖**——仅在编译时需要，运行时可不存在。

      例如，一个仅在特定日志库存在时才启用的调试功能：

      ```java
      module my.module {
          requires static module.name; // 编译时依赖，运行时可无
      }
      ```

   3. `requires transitive`（传递依赖）

      若你的模块依赖某个库，而使用者也需要该库，则可使用传递依赖：

      ```java
      module my.module {
          requires transitive module.name;
      }
      ```

      这样，当其他模块 `requires my.module` 时，会自动获得对 `module.name` 的读取权限，无需显式声明。

   4. `exports`

      默认情况下，模块**不对外暴露任何 API**。这是模块系统的核心设计目标之一——强封装。

      要公开某个包，需使用 `exports`：

      ```java
      module my.module {
          exports com.my.package.name;
      }
      ```

      只有被 `exports` 的包中的**公共类型**才能被其他模块访问。

   5. `exports ... to`（定向导出）

      若只想让特定模块访问你的 API：

      ```java
      module my.module {
          exports com.my.package.name to com.specific.module;
      }
      ```

      这增强了安全性，避免 API 被任意模块使用。

   6. `uses`

      声明本模块**消费的服务接口**（通常是接口或抽象类）：

      ```java
      module my.module {
          uses com.example.MyService;
      }
      ```

      > 注意：`uses` 与 `requires` 不同。即使服务接口来自依赖的传递依赖，也无需显式 `requires` 该依赖，只需 `uses` 接口即可。

   7. `provides ... with`

      声明本模块**提供的服务实现**：

      ```java
      module my.module {
          provides com.example.MyService with com.example.MyServiceImpl;
      }
      ```

      这使得其他模块可通过 `ServiceLoader` 发现并使用该服务。

   8. `open module`（开放整个模块）

      为兼容依赖反射的旧库（如 Spring、JUnit），可开放整个模块供反射访问：

      ```java
      open module my.module {
          // 所有包对反射开放
      }
      ```

   9. `opens`

      仅开放特定包供反射访问：

      ```java
      module my.module {
          opens com.my.package; // 对所有模块开放反射
      }
      ```

   10. `opens ... to`

       更安全的方式：仅对指定模块开放反射：

       ```java
       module my.module {
           opens com.my.package to moduleOne, moduleTwo;
       }
       ```

5. 命令行选项

   尽管 Maven 和 Gradle 已支持模块构建，但了解命令行用法仍很有价值。

   常用选项包括：

   | 选项                                  | 说明                                        |
   | ------------------------------------- | ------------------------------------------- |
   | `--module-path`                       | 指定模块路径（类似 classpath）              |
   | `--add-reads`                         | 命令行版 `requires`                         |
   | `--add-exports`                       | 命令行版 `exports`                          |
   | `--add-opens`                         | 命令行版 `opens`                            |
   | `--add-modules`                       | 显式添加模块到根模块集                      |
   | `--list-modules`                      | 列出所有可用模块                            |
   | `--patch-module`                      | 替换或补充模块中的类                        |
   | `--illegal-access=permit\|warn\|deny` | 控制对未命名模块的非法访问（默认 `permit`） |

6. 可见性（Visibility）

   许多库（如 JUnit、Spring）依赖反射工作。

   在 Java 9+ 中，默认只能访问已导出包中的公共成员，即使调用 `setAccessible(true)` 也无法访问私有成员。

   解决方法：

   - 使用 `open`、`opens` 或 `opens...to` 授予**运行时反射权限**（注意：仅运行时有效，无法用于编译）
   - 若无法修改目标模块，可在启动时使用 `--add-opens`：

   ```bash
   java --add-opens java.base/java.lang=ALL-UNNAMED ...
   ```

   > 前提：你有权控制 JVM 启动参数。

7. 实战：构建一个模块化项目

   现在，我们将通过一个完整示例，实践所学知识。

   为简化，我们将直接使用命令行，而非 Maven/Gradle。

   1. 项目结构

      ```bash
      mkdir module-project
      cd module-project
      mkdir simple-modules
      ```

      最终结构：

      ```txt
      module-project/
      ├── simple-modules/
      │   ├── hello.modules/
      │   │   └── com/baeldung/modules/hello/
      │   └── main.app/
      │       └── com/baeldung/modules/main/
      ```

   2. 第一个模块：`hello.modules`

      创建 `hello.modules/com/baeldung/modules/hello/HelloModules.java`：

      ```java
      package com.baeldung.modules.hello;

      public class HelloModules {
          public static void doSomething() {
              System.out.println("Hello, Modules!");
          }
      }
      ```

      创建 `hello.modules/module-info.java`：

      ```java
      module hello.modules {
          exports com.baeldung.modules.hello;
      }
      ```

   3. 第二个模块：`main.app`

      创建 `main.app/module-info.java`：

      ```java
      module main.app {
          requires hello.modules;
      }
      ```

      创建 `main.app/com/baeldung/modules/main/MainApp.java`：

      ```java
      package com.baeldung.modules.main;

      import com.baeldung.modules.hello.HelloModules;

      public class MainApp {
          public static void main(String[] args) {
              HelloModules.doSomething();
          }
      }
      ```

   4. 编译模块

      创建 `compile-simple-modules.sh`：

      ```bash
      #!/usr/bin/env bash
      javac -d outDir --module-source-path simple-modules $(find simple-modules -name "*.java")
      ```

      运行后，`outDir` 目录将包含两个编译后的模块。

   5. 运行程序

      创建 `run-simple-module-app.sh`：

      ```bash
      #!/usr/bin/env bash
      java --module-path outDir -m main.app/com.baeldung.modules.main.MainApp
      ```

      输出：

      ```log
      Hello, Modules!
      ```

   6. 添加服务（Service）

      1. 在 `hello.modules` 中添加接口：

         ```java
         // HelloInterface.java
         public interface HelloInterface {
             void sayHello();
         }
         ```

      2. 修改 `HelloModules` 实现该接口：

         ```java
         public class HelloModules implements HelloInterface {
             public static void doSomething() {
                 System.out.println("Hello, Modules!");
             }
             public void sayHello() {
                 System.out.println("Hello!");
             }
         }
         ```

      3. 更新 `hello.modules/module-info.java`：

         ```java
         module hello.modules {
             exports com.baeldung.modules.hello;
             provides com.baeldung.modules.hello.HelloInterface
                 with com.baeldung.modules.hello.HelloModules;
         }
         ```

      4. 更新 `main.app/module-info.java`：

         ```java
         module main.app {
             requires hello.modules;
             uses com.baeldung.modules.hello.HelloInterface;
         }
         ```

      5. 修改 `MainApp.java` 使用服务：

         ```java
         import java.util.ServiceLoader;

         public class MainApp {
             public static void main(String[] args) {
                 HelloModules.doSomething();

                 ServiceLoader<HelloInterface> loader = ServiceLoader.load(HelloInterface.class);
                 HelloInterface service = loader.iterator().next();
                 service.sayHello();
             }
         }
         ```

      重新编译并运行，输出：

      ```log
      Hello, Modules!
      Hello!
      ```

8. 向未命名模块添加模块

   当运行旧版 Java 8 程序时，可能需要显式添加某些 JDK 模块（如 `java.xml.bind`，在 Java 9+ 中默认不包含）。

   一般来说，将命名模块添加到默认根模块集的选项是 `–add-modules <module>(,<module>)*` 其中 `<module>` 是模块名称。

   ```bash
   java --add-modules java.xml.bind ...
   ```

   要在 Maven 中使用此语法，我们可以在 maven 编译器中嵌入相同的语法：

   ```xml
   <plugin>
       <groupId>org.apache.maven.plugins</groupId>
       <artifactId>maven-compiler-plugin</artifactId>
       <configuration>
           <compilerArgs>
               <arg>--add-modules</arg>
               <arg>java.xml.bind</arg>
           </compilerArgs>
       </configuration>
   </plugin>
   ```

9. 结论

   本指南全面介绍了 Java 9 模块系统的基础知识：

   - 什么是模块及其组成部分（包、资源、描述文件）
   - JDK 中的默认模块
   - 模块描述文件的各种指令（`requires`, `exports`, `provides`, `opens` 等）
   - 命令行构建与运行模块
   - 通过完整示例实践模块化开发

   模块系统通过**强封装**和**显式依赖**，显著提升了大型 Java 应用的可维护性、安全性和性能。尽管初期学习成本较高，但对于现代 Java 开发而言，掌握模块化是迈向专业化的关键一步。

## 补充

1. 命令行选项

   到目前为止，Maven 和 Gradle 已经添加了对 Java 9 模块的支持，因此您不需要大量手动构建项目。不过，了解如何通过命令行使用模块系统仍然很有价值。

   我们将在下面的完整示例中使用命令行，以帮助我们在头脑中巩固整个系统的工作原理。

   - module-path - 我们使用 -module-path 选项指定模块路径。这是包含模块的一个或多个目录的列表。
   - add-reads - 我们可以使用 -add-reads 命令行，而不是依赖模块声明文件。
   - add-exports - 命令行替代 exports 指令。
   - add-opens - 替换模块声明文件中的 open 子句。
   - add-modules - 将模块列表添加到默认模块集中
   - list-modules - 打印所有模块及其版本字符串的列表
   - patch-module - 在模块中添加或覆盖类
   - illegal-access=permit|warn|deny - 通过显示单个全局警告来放松强封装、显示所有警告或错误失败。默认为允许。

2. 可见性

   我们应该花点时间谈谈代码的可见性。

   很多库都依赖于反射来发挥其魔力（我想到了 JUnit 和 Spring）。

   在 Java 9 中，默认情况下我们只能访问导出包中的公有类、方法和字段。即使我们使用反射来访问非公有成员并调用 setAccessible(true)，也无法访问这些成员。

   我们可以使用 open、opens 和 opens...to 选项为反射授予运行时访问权限。注意，这是运行时访问！

   我们无法针对私有类型进行编译，而且无论如何也不需要这样做。

   如果我们必须访问某个模块进行反射，而我们又不是该模块的所有者（即我们不能使用 opens...to 指令），那么可以使用命令行 -add-opens 选项，允许自己的模块在运行时对锁定模块进行反射访问。

   唯一需要注意的是，你必须能访问用于运行模块的命令行参数，这样才能做到这一点。

3. 把所有东西放在一起

   既然我们已经知道了什么是模块以及如何使用模块，那么就让我们来构建一个简单的项目来演示我们刚刚学到的所有概念。

   为了保持简单，我们不会使用 Maven 或 Gradle。相反，我们将使用命令行工具来构建模块。

   1. 设置我们的项目

      首先，我们需要设置项目结构。我们将创建几个目录来组织文件。

      首先创建项目文件夹

      ```zsh
      mkdir module-project
      cd module-project
      ```

      这是整个项目的基础，所以要在这里添加文件，如 Maven 或 Gradle 构建文件、其他源代码目录和资源。

      我们还将在此放置一个目录，用于存放所有项目特定的模块。

      接下来，我们创建一个模块目录：

      mkdir simple-modules
      我们的项目结构：见 src/simple-modules

   2. 第一个模块

      现在我们已经有了基本的结构，让我们添加第一个模块。

      在 simple-modules 目录下新建一个名为 hello.modules 的目录。

      我们可以给它起任何名字，但要遵守软件包命名规则（如用句号分隔单词等）。如果愿意，我们甚至可以使用主软件包的名称作为模块名称，但通常情况下，我们希望使用与创建该模块的 JAR 相同的名称。

      在新模块下，我们可以创建想要的包。在本例中，我们将创建一个包结构：

      com.baeldung.modules.hello

      然后，在该包中创建一个名为 HelloModules.java 的新类。我们将保持代码的简洁：

      HelloModules.java

      最后，在 hello.modules 根目录下添加模块描述符；module-info.java。

      为使示例简单明了，我们只需导出 com.baeldung.modules.hello 包的所有公共成员。

   3. 我们的第二个模块

      我们的第一个模块很棒，但它什么也做不了。

      现在我们可以创建第二个模块来使用它。

      在 simple-modules 目录下创建另一个名为 main.app 的模块目录。这次我们将从模块描述符开始：module-info.java。

      我们不需要向外界公开任何东西。相反，我们只需依赖第一个模块，这样就可以访问它导出的公有类。

      现在，我们可以创建一个使用它的应用程序。

      创建一个新的包结构：com.baeldung.modules.main。

      现在，创建一个名为 MainApp.java 的新类文件。

      这就是我们演示模块所需的全部代码。下一步是通过命令行构建并运行这段代码。

   4. 构建模块

      为了构建我们的项目，我们可以创建一个简单的 bash 脚本，并将其放在项目的根目录下。

      创建一个名为 compile-simple-modules.sh 的文件。

      该命令包括两个部分，即 javac 和 find 命令。

      find 命令只是输出 simple-modules 目录下所有 .java 文件的列表。然后，我们就可以将该列表直接输入 Java 编译器。

      与旧版本的 Java 相比，我们唯一需要做的就是提供一个 module-source-path 参数，以告知编译器它正在构建模块。

      运行此命令后，我们将得到一个包含两个已编译模块的 outDir 文件夹。

   5. 运行代码

      现在我们终于可以运行代码来验证模块是否正常工作了。

      在项目根目录下创建另一个文件：run-simple-module-app.sh。

      要运行模块，我们必须至少提供模块路径和主类。如果一切正常，您应该看到

      ```zsh
      >$ ./run-simple-module-app.sh
      Hello, Modules!
      ```

   6. 添加服务

      既然我们对如何构建模块有了基本的了解，那就把它变得复杂一些吧。

      我们将看看如何使用 provides...with 和 uses 指令。

      首先，在 hello.modules 模块中定义一个名为 HelloInterface.java 的新文件。

      为了方便起见，我们将用现有的 HelloModules.java 类实现该接口。

      这就是我们创建服务所需要做的全部工作。

      现在，我们需要告诉全世界，我们的模块提供了这项服务。

      在 module-info.java 中添加以下内容：

      `provides com.baeldung.modules.hello.HelloInterface with com.baeldung.modules.hello.HelloModules;`

      正如我们所看到的，我们声明了接口和实现接口的类。

      接下来，我们需要使用这项服务。在我们的 main.app 模块中，让我们在 module-info.java 中添加以下内容：

      `uses com.baeldung.modules.hello.HelloInterface;`

      最后，在我们的主方法中，我们可以通过 ServiceLoader 使用该服务。

      编译并运行：MacOS

      ```zsh
      % cd ../core-java-9-jigsaw
      % chmod ugo+x compile-simple-modules.sh
      % ./compile-simple-modules.sh
      % sh ./run-simple-module-app.sh
      ```

      我们使用这些指令可以更明确地说明代码的使用方式。

      我们可以将实现放在私有包中，而将接口放在公共包中。

      这样，我们的代码就更安全了，而额外的开销却很少。

      继续尝试使用其他指令，进一步了解模块及其工作原理。
