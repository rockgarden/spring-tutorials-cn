# [Java 服务提供者接口](https://www.baeldung.com/java-spi)

Java+ Java 接口

1. 概述

   Java 6 引入了一项用于发现和加载特定接口实现的功能：**服务提供者接口**（Service Provider Interface, SPI）。

   这种模式广泛应用于 JDBC（java.sql.Driver）、JAXB、SLF4J、Dubbo 等框架中，是 Java 生态中实现插件化架构的经典范式。

   在本教程中，我们将介绍 Java SPI 的核心组件，并通过一个实际用例展示如何应用它。

2. Java SPI 的术语与定义

   Java SPI 定义了四个主要组件：

   1. 服务（Service）

      一组广为人知的编程接口和类，用于提供特定的应用功能或特性。

   2. 服务提供者接口（Service Provider Interface）

      一个接口或抽象类，作为服务的代理或端点。

      如果服务本身就是一个接口，那么它与服务提供者接口是相同的。

      在 Java 生态系统中，“服务”与“SPI”合在一起通常被称为 **API**。

   3. 服务提供者（Service Provider）

      SPI 的具体实现。服务提供者包含一个或多个实现（或继承）服务类型的实体类。

      服务提供者通过一个**提供者配置文件**进行配置和识别，该文件位于资源目录 `META-INF/services` 下。文件名是 SPI 的全限定类名，文件内容则是 SPI 实现类的全限定类名。

      服务提供者以扩展形式安装，通常是一个 JAR 文件，放置在应用程序类路径、Java 扩展类路径或用户自定义类路径中。

   4. ServiceLoader

      SPI 的核心是 [ServiceLoader](#serviceloader) 类。它的作用是**懒加载**地发现并加载实现类。它使用上下文类路径来定位提供者实现，并将其缓存在内部。

   5. 核心组件关系说明

      | 组件                      | 角色       | 说明                                                                                                                     |
      | ------------------------- | ---------- | ------------------------------------------------------------------------------------------------------------------------ |
      | **Service API 模块**      | 定义契约   | 包含：<br>• 服务接口（如 `QuoteManager`）<br>• SPI 接口（如 `ExchangeRateProvider`）<br>• 工具类（使用 `ServiceLoader`） |
      | **Service Provider 模块** | 实现契约   | 实现 SPI 接口，并在 `META-INF/services/` 下注册                                                                          |
      | **ServiceLoader**         | 发现与加载 | JDK 内置类，自动从类路径中加载所有注册的提供者                                                                           |

3. SPI 与 API 的区别

   在软件开发中，我们经常听到另一个与 SPI 相似的缩写——**API**（Application Programming Interface，应用程序编程接口）。

   让我们探讨它们之间的关键区别。

   **API** 是由软件库或框架提供的契约，定义了其他组件（如应用程序或模块）如何与其交互。因此，API 是为**使用**该软件的开发者设计的，用于访问其功能。例如：

   ```java
   String me = "Kai";
   String meUppercase = me.toUpperCase();
   String threeStars = meUppercase.replaceAll("[A-Z]", "*");
   ```

   在这个例子中，`toUpperCase()` 和 `replaceAll()` 方法来自 Java `String` 的 API。我们可以使用这些 API 定义的方法来实现字符串转大写、基于正则表达式的字符串替换等功能。

   而 **SPI** 则允许开发者**创建新的功能**，并将其集成到库或框架中。它是为**扩展**或**自定义实现**的开发者设计的。

   简单来说：

   - **API 提供了契约的实现**；
   - **SPI 提供了需要被实现的契约**。

   一个类比有助于快速理解两者的区别：想象一个网络浏览器，比如 Google Chrome。

   - “后退”、“前进”、“刷新”等按钮允许用户与浏览器交互并浏览网页——这些属于 **API**。
   - 而浏览器的扩展/插件机制则属于 **SPI**，因为它允许第三方开发者创建兼容的扩展，实现不同功能。

   Java 提供了许多 SPI 示例，包括：

   - [ ] [CurrencyNameProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/spi/CurrencyNameProvider.html)：为 `Currency` 类提供本地化的货币符号
   - [ ] [LocaleNameProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/spi/LocaleNameProvider.html)：为 `Locale` 类提供本地化名称
   - [ ] [TimeZoneNameProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/spi/TimeZoneNameProvider.html)：为 `TimeZone` 类提供本地化时区名称
   - [ ] [DateFormatProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/spi/DateFormatProvider.html)：为指定区域提供日期和时间格式
   - [ ] [NumberFormatProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/spi/NumberFormatProvider.html)：为 `NumberFormat` 类提供货币、整数和百分比格式
   - [ ] [Driver](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Driver.html)：从 JDBC 4.0 开始，JDBC API 支持 SPI 模式（旧版本使用 [Class.forName()](<https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Class.html#forName(java.lang.String)>) 加载驱动）
   - [ ] [PersistenceProvider](https://docs.oracle.com/javaee/7/api/javax/persistence/spi/PersistenceProvider.html)：提供 JPA API 的实现
   - [ ] [JsonProvider](https://docs.oracle.com/javaee/7/api/javax/json/spi/JsonProvider.html)：提供 JSON 处理对象
   - [ ] [JsonbProvider](https://javaee.github.io/javaee-spec/javadocs/javax/json/bind/spi/JsonbProvider.html)：提供 JSON 绑定对象
   - [ ] [Extension](https://docs.oracle.com/javaee/7/api/javax/enterprise/inject/spi/Extension.html)：为 CDI 容器提供扩展
   - [ ] [ConfigSourceProvider](https://download.eclipse.org/microprofile/microprofile-config-2.0/apidocs/org/eclipse/microprofile/config/spi/ConfigSourceProvider.html)：提供配置属性的来源

   接下来，我们将通过一个示例应用深入理解 SPI。

4. 示例：汇率查询应用

   现在我们已掌握基础知识，下面通过构建一个汇率查询应用来展示 SPI 的使用步骤。

   为清晰起见，我们需要至少三个项目：

   - `exchange-rate-api`
   - `exchange-rate-impl`
   - `exchange-rate-app`

   我们将通过 `exchange-rate-api` 模块定义服务、SPI 和 `ServiceLoader`；  
   在 `exchange-rate-impl` 模块中实现服务提供者；  
   最后在 `exchange-rate-app` 模块中整合所有内容。

   实际上，我们可以提供任意多个服务提供者模块，只要将它们放入 `exchange-rate-app` 的类路径中即可。

   1. 构建我们的 API

      首先创建一个名为 `exchange-rate-api` 的 Maven 项目（以 `api` 结尾是良好实践，但非强制）。

      然后创建一个表示汇率的模型类：

      ```java
      package com.baeldung.rate.api;

      public class Quote {
          private String currency;
          private BigDecimal ask;
          private BigDecimal bid;
          private LocalDate date;
          // ...
      }
      ```

      接着定义用于获取汇率的服务接口 `QuoteManager`：

      ```java
      package com.baeldung.rate.api;

      public interface QuoteManager {
          List<Quote> getQuotes(String baseCurrency, LocalDate date);
      }
      ```

      再创建一个 SPI 接口：

      ```java
      package com.baeldung.rate.spi;

      public interface ExchangeRateProvider {
          QuoteManager create();
      }
      ```

      最后，创建一个工具类 `ExchangeRate`，供客户端代码使用。该类通过 `ServiceLoader` 委托调用。

      首先调用静态工厂方法 `load()` 获取 `ServiceLoader` 实例：

      ```java
      ServiceLoader<ExchangeRateProvider> loader = ServiceLoader.load(ExchangeRateProvider.class);
      ```

      然后调用 `iterator()` 方法遍历所有可用实现：

      ```java
      Iterator<ExchangeRateProvider> it = loader.iterator();
      ```

      搜索结果会被缓存，如需发现新安装的实现，可调用 `ServiceLoader.reload()`：

      ```java
      loader.reload();
      ```

      以下是完整的工具类（基于 SPI 模式的汇率服务管理器。）：

      ```java
        public final class ExchangeRate {

            // 默认的汇率提供者实现类名
            private static final String DEFAULT_PROVIDER = "com.baeldung.rate.spi.YahooFinanceExchangeRateProvider";

            /**
            * 获取所有已注册的汇率提供者
            * 通过 ServiceLoader 加载所有实现 ExchangeRateProvider 接口的服务提供者
            *
            * @return 所有可用的汇率提供者列表
            */
            public static List<ExchangeRateProvider> providers() {
                List<ExchangeRateProvider> services = new ArrayList<>();
                // 创建 ServiceLoader 实例，用于加载 ExchangeRateProvider 接口的所有实现
                ServiceLoader<ExchangeRateProvider> loader = ServiceLoader.load(ExchangeRateProvider.class);
                // 遍历所有加载的服务提供者并添加到列表中
                loader.forEach(services::add);
                return services;
            }

            /**
            * 获取默认的汇率提供者
            * 使用预定义的默认提供者类名来获取相应的提供者实例
            *
            * @return 默认的汇率提供者实例
            */
            public static ExchangeRateProvider provider() {
                return provider(DEFAULT_PROVIDER);
            }

            /**
            * 根据指定的提供者名称获取对应的汇率提供者
            * 遍历所有已注册的服务提供者，找到类名匹配的实例
            *
            * @param providerName 要获取的提供者类名
            * @return 匹配的汇率提供者实例
            * @throws ProviderNotFoundException 当指定名称的提供者未找到时抛出异常
            */
            public static ExchangeRateProvider provider(String providerName) {
                // 加载 ExchangeRateProvider 接口的所有实现
                ServiceLoader<ExchangeRateProvider> loader = ServiceLoader.load(ExchangeRateProvider.class);
                Iterator<ExchangeRateProvider> it = loader.iterator();

                // 遍历所有提供者实例
                while (it.hasNext()) {
                    ExchangeRateProvider provider = it.next();
                    // 比较提供者的类名是否与指定的名称匹配
                    if (providerName.equals(provider.getClass().getName())) {
                        return provider; // 找到匹配的提供者，返回实例
                    }
                }
                // 如果没有找到匹配的提供者，抛出异常
                throw new ProviderNotFoundException("Exchange Rate provider " + providerName + " not found");
            }
        }
      ```

      > 注意：该工具类并非必须放在 `api` 项目中。客户端代码也可以直接调用 `ServiceLoader` 的方法。

   2. 构建服务提供者

      现在创建一个名为 `exchange-rate-impl` 的 Maven 项目，并在 `pom.xml` 中添加 API 依赖：

      ```xml
      <dependency>
          <groupId>com.baeldung</groupId>
          <artifactId>exchange-rate-api</artifactId>
          <version>1.0.0-SNAPSHOT</version>
      </dependency>
      ```

      然后创建 SPI 的实现类：

      ```java
      public class YahooFinanceExchangeRateProvider implements ExchangeRateProvider {
          @Override
          public QuoteManager create() {
              return new YahooQuoteManagerImpl();
          }
      }
      ```

      以及 `QuoteManager` 的具体实现：

      ```java
      public class YahooQuoteManagerImpl implements QuoteManager {
          @Override
          public List<Quote> getQuotes(String baseCurrency, LocalDate date) {
            // 构建要查询的货币对URL列表
            List<String> currencyQuery = new ArrayList<>();

            // 遍历系统中所有可用的货币
            Currency.getAvailableCurrencies().forEach(currency -> {
                // 排除基础货币本身，避免查询 USDUSD 这样的无效货币对
                if (!baseCurrency.equals(currency.getCurrencyCode())) {
                    // 构造查询URL，格式如：USDJPY=X
                    currencyQuery.add(String.format(URL_PROVIDER, baseCurrency + currency.getCurrencyCode()));
                }
            });

            // 存储获取到的汇率报价
            final List<Quote> quotes = new ArrayList<>();

            // 依次请求每个货币对的数据
            for (String url: currencyQuery) {
                // 发送HTTP GET请求获取数据
                String response = doGetRequest(url);

                // 如果请求成功且有响应数据
                if (response != null) {
                    // 将JSON响应转换为Quote对象
                    final Quote map = map(response);
                    if (map != null) {
                        quotes.add(map); // 添加到结果列表
                    }
                }
            }
            return quotes;
          }
      }
      ```

      为了让该实现能被发现，需创建提供者配置文件：

      ```txt
      META-INF/services/com.baeldung.rate.spi.ExchangeRateProvider
      ```

      文件内容为实现类的全限定名：

      ```java
      com.baeldung.rate.impl.YahooFinanceExchangeRateProvider
      ```

   3. 整合应用

      最后，创建客户端项目 `exchange-rate-app`，并在其 `pom.xml` 中添加 `exchange-rate-api` 依赖：

      ```xml
      <dependency>
          <groupId>com.baeldung</groupId>
          <artifactId>exchange-rate-api</artifactId>
          <version>1.0.0-SNAPSHOT</version>
      </dependency>
      ```

      此时，即可在应用中调用 SPI：

      ```java
      ExchangeRate.providers().forEach(provider -> ... );
      ```

   4. 运行应用

      现在构建所有模块。在 `java-spi` 项目根目录执行：

      ```bash
      mvn clean package
      ```

      然后运行应用（**不包含提供者**）：

      ```bash
      java -cp ./exchange-rate-api/target/exchange-rate-api-1.0.0-SNAPSHOT.jar:./exchange-rate-app/target/exchange-rate-app-1.0.0-SNAPSHOT.jar com.baeldung.rate.app.MainApp
      ```

      此时输出为空，因为未找到任何提供者。

      现在将提供者 JAR 加入类路径并重新运行：

      ```bash
      java -cp './exchange-rate-api/target/exchange-rate-api-1.0.0-SNAPSHOT.jar:./exchange-rate-app/target/exchange-rate-app-1.0.0-SNAPSHOT.jar:./exchange-rate-impl/target/exchange-rate-impl-1.0.0-SNAPSHOT.jar:./exchange-rate-impl/target/depends/*' com.baeldung.rate.app.MainApp
      ```

      此时将看到提供者被成功加载，并输出汇率应用的结果。

      > **注意**：在类路径中指定多个 JAR 时，需根据操作系统选择分隔符：
      >
      > - Linux/macOS 使用冒号（`:`）
      > - Windows 使用分号（`;`）

   5. Java SPI 实现模式 - 流程图

      ```mermaid
      graph TB
          subgraph "Client Application"
              A[MainApp]
          end

          subgraph "Service API Module (exchange-rate-api)"
              B[QuoteManager Interface]
              C[ExchangeRateProvider SPI]
              D[ExchangeRate Utility Class]
              E[ServiceLoader&lt;ExchangeRateProvider&gt;]
          end

          subgraph "Service Provider Module (exchange-rate-impl)"
              F[YahooFinanceExchangeRateProvider]
              G[YahooQuoteManagerImpl]
              H[META-INF/services/...]
          end

          subgraph "Classpath Discovery"
              I[Provider Configuration File]
          end

          A --> D
          D --> E
          E --> I
          I --> F
          F --> G
          C -.-> F
          B -.-> G

          style A fill:#e1f5fe
          style B fill:#f3e5f5
          style C fill:#f3e5f5
          style D fill:#f3e5f5
          style E fill:#f3e5f5
          style F fill:#e8f5e8
          style G fill:#e8f5e8
          style H fill:#e8f5e8
          style I fill:#fff3e0
      ```

      **图中关键组件说明**

      | 颜色标识  | 组件类型                    | 说明                       |
      | --------- | --------------------------- | -------------------------- |
      | 🟦 浅蓝色 | **Client Application**      | 客户端应用，使用服务的入口 |
      | 🟪 浅紫色 | **Service API Module**      | 定义契约的 API 模块        |
      | 🟩 浅绿色 | **Service Provider Module** | 具体实现的服务提供者       |
      | 🟨 浅橙色 | **Configuration File**      | 服务发现的配置文件         |

      **执行步骤详解**

      **Step 1: 客户端发起调用**

      ```txt
      🟦 MainApp --> 🟪 ExchangeRate Utility Class
      ```

      - 客户端代码调用 `ExchangeRate.providers()` 或 `ExchangeRate.provider()`
      - 通过工具类间接使用 SPI

      **Step 2: ServiceLoader 初始化**

      ```txt
      🟪 ExchangeRate --> ServiceLoader<ExchangeRateProvider>
      ```

      - `ExchangeRate` 类内部调用 `ServiceLoader.load(ExchangeRateProvider.class)`
      - 创建服务加载器实例

      **Step 3: 配置文件扫描**

      ```txt
      ServiceLoader --> 🟨 Provider Configuration File
      ```

      - `ServiceLoader` 自动扫描类路径下的 `META-INF/services/` 目录
      - 查找以 SPI 接口全限定名命名的配置文件

      **Step 4: 服务提供者发现**

      ```txt
      🟨 Configuration File --> 🟩 YahooFinanceExchangeRateProvider
      ```

      - 读取配置文件内容（如：`com.baeldung.rate.impl.YahooFinanceExchangeRateProvider`）
      - 根据类名实例化具体的服务提供者

      **Step 5: 服务实现创建**

      ```txt
      🟩 YahooFinanceExchangeRateProvider --> YahooQuoteManagerImpl
      ```

      - 调用 `create()` 方法返回具体的业务实现
      - 完成 SPI 到具体实现的绑定

      **Step 6: 依赖关系**

      ```txt
      🟪 QuoteManager Interface -.-> 🟩 YahooQuoteManagerImpl
      🟪 ExchangeRateProvider SPI -.-> 🟩 YahooFinanceExchangeRateProvider
      ```

      - 虚线表示**实现关系**（Implementation）
      - API 模块定义接口，实现模块提供具体实现

   6. 调用时序

      ```mermaid
      sequenceDiagram
          participant Client as 客户端代码
          participant ExchangeRate as ExchangeRate 工具类
          participant ServiceLoader as ServiceLoader<ExchangeRateProvider>
          participant Provider as YahooFinanceExchangeRateProvider
          participant Impl as YahooQuoteManagerImpl

          Client->>ExchangeRate: 调用 ExchangeRate.provider()
          ExchangeRate->>ServiceLoader: ServiceLoader.load(ExchangeRateProvider.class)
          ServiceLoader->>ServiceLoader: 扫描 META-INF/services/... 配置文件
          ServiceLoader->>Provider: 实例化 YahooFinanceExchangeRateProvider
          ExchangeRate->>Provider: 调用 provider.create()
          Provider->>Impl: new YahooQuoteManagerImpl()
          Impl-->>Provider: 返回 QuoteManager 实例
          Provider-->>ExchangeRate: 返回 QuoteManager
          ExchangeRate-->>Client: 返回 QuoteManager
          Client->>Impl: 调用 getQuotes(...)
      ```

5. 结论

   通过上述清晰的步骤，我们已全面了解 Java SPI 机制。它能帮助我们构建**高度可扩展**或**可替换**的模块化系统。

   尽管本例使用了 Yahoo 汇率服务来展示如何集成第三方 API，但实际生产系统**并不依赖外部 API**也能构建强大的 SPI 应用。SPI 的真正价值在于其**解耦与插件化能力**，使系统更具灵活性和可维护性。

   关键设计特点：

   - **解耦**：客户端只依赖 API 接口，不感知具体实现。
   - **可扩展**：新增服务只需：

     1. 实现 SPI 接口；
     2. 在 `META-INF/services/` 下添加配置文件；
     3. 将 JAR 放入 classpath。

   - **懒加载 + 缓存**：`ServiceLoader` 首次迭代时加载，后续复用缓存。
   - **无排序/优先级**：默认按 classpath 顺序加载，如需控制顺序需自行实现。

   应用场景示例：

   - **JDBC 4.0+**：`java.sql.Driver` 自动注册，无需 `Class.forName()`
   - **SLF4J**：绑定不同日志实现（Logback、Log4j 等）
   - **Dubbo / Spring Boot**：扩展点机制（如 `SpringFactoriesLoader` 借鉴 SPI 思想）

## [ServiceLoader](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html)

```txt
java.lang.Object
 ↳ java.util.ServiceLoader<S>
```

类型参数：  
S - 此加载器将要加载的服务类型

所有已实现的接口：

```txt
Iterable<S>

public final class ServiceLoader<S>
 extends Object
 implements Iterable<S>
```

服务实现加载机制

服务（service）是一个众所周知的接口或类，针对该服务可能存在零个、一个或多个服务提供者（service provider）。服务提供者（或简称提供者）是指实现了该知名接口或继承了该知名类的类。ServiceLoader 是一个对象，用于在应用程序选择的时机，定位并加载运行时环境中部署的服务提供者。应用程序代码仅引用服务本身，而不直接引用服务提供者，并且应具备在多个服务提供者之间进行选择的能力（基于它们通过服务接口暴露的功能），同时能够处理找不到任何服务提供者的情况。

1. 获取 ServiceLoader

   应用程序通过调用 ServiceLoader 的静态 load 方法之一，为指定的服务获取一个 ServiceLoader 实例。如果应用程序本身是一个模块，则其模块声明中必须包含一个 uses 指令，明确指定该服务；这有助于定位提供者并确保它们能够可靠地执行。此外，如果应用程序模块本身不包含该服务的定义，则其模块声明中必须包含一个 requires 指令，用于声明导出该服务的模块。强烈建议应用程序模块不要直接依赖包含服务提供者的模块。

   通过 ServiceLoader 的 iterator 方法，可以定位并实例化该服务的提供者。ServiceLoader 还定义了 stream 方法，用于获取一个提供者流，允许在不实例化提供者的情况下对其进行检查和过滤。

   例如，假设服务是 com.example.CodecFactory，这是一个定义了生成编码器和解码器方法的接口：

   ```java
   package com.example;
   public interface CodecFactory {
       Encoder getEncoder(String encodingName);
       Decoder getDecoder(String encodingName);
   }
   ```

   以下代码获取 CodecFactory 服务的 ServiceLoader，然后使用其迭代器（由增强型 for 循环自动创建）遍历所有已定位到的服务提供者实例：

   ```java
   ServiceLoader<CodecFactory> loader = ServiceLoader.load(CodecFactory.class);
   for (CodecFactory factory : loader) {
       Encoder enc = factory.getEncoder("PNG");
       if (enc != null) {
           // 使用 enc 对 PNG 文件进行编码
           break;
       }
   }
   ```

   如果上述代码位于一个模块中，那么为了引用 com.example.CodecFactory 接口，该模块声明必须 require 导出该接口的模块，并且必须声明使用该服务：

   ```java
   requires com.example.codec.core;
   uses com.example.CodecFactory;
   ```

   有时，应用程序可能希望在实例化服务提供者之前先对其进行检查，以判断其实例是否对当前用途有用。例如，一个能够生成 "PNG" 编码器的 CodecFactory 服务提供者可能被标注了 @PNG 注解。以下代码使用 ServiceLoader 的 stream 方法，返回 Provider<CodecFactory> 对象流（而非像 iterator 那样直接返回 CodecFactory 实例）：

   ```java
   ServiceLoader<CodecFactory> loader = ServiceLoader.load(CodecFactory.class);
   Set<CodecFactory> pngFactories = loader
       .stream()                                              // 注 a：返回 Provider<CodecFactory> 的流
       .filter(p -> p.type().isAnnotationPresent(PNG.class))  // 注 b：p.type() 返回 Class<CodecFactory>
       .map(Provider::get)                                    // 注 c：get() 返回 CodecFactory 实例
       .collect(Collectors.toSet());
   ```

2. 服务设计指南

   服务通常是一个单一类型，一般为接口或抽象类。虽然也可以使用具体类，但不推荐这样做。该类型的访问权限可以是任意的。服务的方法高度依赖于具体领域，因此本 API 规范无法对其形式或功能提供具体建议。但有两条通用准则：

   1. 服务应声明足够多的方法，以便服务提供者能够传达其领域特定的属性以及其他实现质量因素。这样，获取该服务 ServiceLoader 的应用程序就可以在每个服务提供者实例上调用这些方法，从而选择最适合自身需求的提供者。

   2. 服务应明确表达其提供者是直接实现该服务，还是作为某种间接机制（如“代理”或“工厂”）。当领域特定对象的实例化成本较高时，服务提供者通常采用间接机制；此时，服务应被设计为由提供者作为抽象层，按需创建“真实”的实现。例如，CodecFactory 服务通过其名称表明其提供者是编解码器的工厂，而非编解码器本身，因为某些编解码器的创建可能代价高昂或较为复杂。

3. 开发服务提供者

   服务提供者通常是一个具体类（单一类型）。接口或抽象类也是允许的，前提是它们声明了一个静态的 provider 方法（后文将讨论）。该类型必须是 public 的，且不能是内部类。

   服务提供者及其支持代码可以开发为一个模块，然后部署在应用程序模块路径上，或打包进模块化镜像中。也可以将服务提供者及其支持代码打包为 JAR 文件，部署在应用程序类路径上。将服务提供者开发为模块的优势在于，可以完全封装其实现细节，对外隐藏所有内部信息。

   获取某服务 ServiceLoader 的应用程序，无需关心该服务的提供者是以模块形式还是 JAR 文件形式部署的。应用程序通过 ServiceLoader 的 iterator 方法直接实例化服务提供者，或通过 stream 方法返回的 Provider 对象进行实例化，而无需了解服务提供者的具体位置。

4. 以模块形式部署服务提供者

   在模块中开发的服务提供者，必须在其模块声明中通过 provides 指令进行声明。该指令同时指定服务类型和服务提供者类；这有助于在其他模块（包含该服务的 uses 指令）获取该服务的 ServiceLoader 时定位到提供者。强烈建议模块不要导出包含服务提供者的包。模块不能在 provides 指令中指定另一个模块中的服务提供者。

   以模块形式部署的服务提供者无法控制其实例化时机（因为这由应用程序决定），但可以控制其实例化方式：

   - 如果服务提供者声明了一个 provider 方法，则 ServiceLoader 会调用该方法来获取服务提供者实例。provider 方法是一个名为 "provider" 的 public static 方法，无形式参数，返回类型必须可赋值给服务的接口或类。
     在这种情况下，服务提供者类本身无需实现服务接口或继承服务类。

   - 如果服务提供者未声明 provider 方法，则 ServiceLoader 会直接通过其 provider 构造器进行实例化。provider 构造器是一个无形式参数的 public 构造器。
     在这种情况下，服务提供者类必须可赋值给服务的接口或类。

   部署在应用程序模块路径上的自动模块（automatic module）中的服务提供者必须具有 provider 构造器。自动模块不支持 provider 方法。

   例如，假设一个模块声明了如下指令：

   ```java
   provides com.example.CodecFactory with com.example.impl.StandardCodecs,
           com.example.impl.ExtendedCodecsFactory;
   ```

   其中：

   - com.example.CodecFactory 是前文所述的双方法服务接口；
   - com.example.impl.StandardCodecs 是一个 public 类，实现了 CodecFactory 接口，并具有 public 无参构造器；
   - com.example.impl.ExtendedCodecsFactory 是一个 public 类，未实现 CodecFactory，但声明了一个名为 "provider" 的 public static 无参方法，返回类型为 CodecFactory。

   ServiceLoader 将通过 StandardCodecs 的构造器对其进行实例化，并通过调用 ExtendedCodecsFactory 的 provider 方法来获取其实例。要求 provider 构造器或 provider 方法为 public，有助于表明该类（即服务提供者）将被外部实体（即 ServiceLoader）实例化的意图。

5. 在类路径上部署服务提供者

   打包为类路径 JAR 文件的服务提供者，通过在资源目录 META-INF/services 下放置一个提供者配置文件（provider-configuration file）来标识。该配置文件的名称即为服务的完整二进制类名。文件内容为每行一个服务提供者的完整二进制类名列表。

   例如，假设服务提供者 com.example.impl.StandardCodecs 被打包到一个类路径 JAR 文件中，则该 JAR 文件应包含如下配置文件：

   META-INF/services/com.example.CodecFactory

   其内容为：

   ```txt
   com.example.impl.StandardCodecs # Standard codecs
   ```

   该提供者配置文件必须使用 UTF-8 编码。每行服务提供者类名前后允许存在空格和制表符，空行将被忽略。注释字符为 '#'（U+0023 NUMBER SIGN）；每行中第一个 '#' 及其之后的所有字符均被视为注释并被忽略。如果同一提供者类名在同一配置文件中重复出现，重复项将被忽略；如果同一提供者类名出现在多个配置文件中，同样会被忽略。

   提供者配置文件中列出的服务提供者类，可以位于与该配置文件相同的 JAR 文件中，也可以位于不同的 JAR 文件中。但该服务提供者类必须对最初用于定位该配置文件的类加载器可见（该类加载器不一定就是最终定位到配置文件的那个加载器）。

6. 提供者发现时机

   服务提供者采用惰性加载（lazy loading）策略，即按需加载和实例化。ServiceLoader 会维护一个已加载提供者的缓存。每次调用 iterator 方法时，返回的 Iterator 首先按实例化顺序返回之前缓存的所有元素，然后惰性地定位并实例化剩余的提供者，并逐个将其加入缓存。类似地，每次调用 stream 方法返回的 Stream 会先处理之前流操作已加载的提供者（按加载顺序），再惰性地定位剩余的提供者。可通过 reload 方法清除缓存。

7. 错误处理

   使用 ServiceLoader 的 iterator 时，如果在定位、加载或实例化服务提供者过程中发生错误，hasNext 和 next 方法将抛出 ServiceConfigurationError。处理 ServiceLoader 的 stream 时，任何导致服务提供者被定位或加载的操作都可能抛出 ServiceConfigurationError。

   在模块中加载或实例化服务提供者时，以下情况会抛出 ServiceConfigurationError：

   - 无法加载服务提供者类；
   - 服务提供者未声明 provider 方法，且要么不可赋值给服务接口/类，要么没有 provider 构造器；
   - 服务提供者声明了一个名为 "provider" 的 public static 无参方法，但其返回类型不可赋值给服务接口/类；
   - 服务提供者类文件中包含多个名为 "provider" 的 public static 无参方法；
   - 服务提供者声明了 provider 方法，但该方法返回 null 或抛出异常；
   - 服务提供者未声明 provider 方法，且其 provider 构造器抛出异常。

   读取提供者配置文件，或加载/实例化其中列出的服务提供者类时，以下情况会抛出 ServiceConfigurationError：

   - 提供者配置文件格式违反上述规范；
   - 读取提供者配置文件时发生 IOException；
   - 无法加载服务提供者类；
   - 服务提供者类不可赋值给服务接口/类，或未定义 provider 构造器，或无法被实例化。

8. 安全性

   ServiceLoader 始终在调用 iterator 或 stream 方法的调用者的安全上下文中执行，也可能受到创建 ServiceLoader 实例的调用者安全上下文的限制。受信任的系统代码通常应在特权安全上下文中调用本类的方法及其返回的迭代器方法。

9. 并发性

   此类的实例不是线程安全的，不能被多个并发线程安全使用。

10. 空值处理

    除非另有说明，向本类的任何方法传入 null 参数都将导致抛出 NullPointerException。
