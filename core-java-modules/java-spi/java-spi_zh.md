# [Java 服务提供者接口](https://www.baeldung.com/java-spi)

Java+ Java 接口

1. 概述

    Java 6 引入了一项用于发现和加载特定接口实现的功能：**服务提供者接口**（Service Provider Interface, SPI）。

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

        SPI 的核心是 [ServiceLoader](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ServiceLoader.html) 类。它的作用是**懒加载**地发现并加载实现类。它使用上下文类路径来定位提供者实现，并将其缓存在内部。

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

    - [CurrencyNameProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/spi/CurrencyNameProvider.html)：为 `Currency` 类提供本地化的货币符号
    - [LocaleNameProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/spi/LocaleNameProvider.html)：为 `Locale` 类提供本地化名称
    - [TimeZoneNameProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/spi/TimeZoneNameProvider.html)：为 `TimeZone` 类提供本地化时区名称
    - [DateFormatProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/spi/DateFormatProvider.html)：为指定区域提供日期和时间格式
    - [NumberFormatProvider](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/text/spi/NumberFormatProvider.html)：为 `NumberFormat` 类提供货币、整数和百分比格式
    - [Driver](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Driver.html)：从 JDBC 4.0 开始，JDBC API 支持 SPI 模式（旧版本使用 [Class.forName()](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Class.html#forName(java.lang.String)) 加载驱动）
    - [PersistenceProvider](https://docs.oracle.com/javaee/7/api/javax/persistence/spi/PersistenceProvider.html)：提供 JPA API 的实现
    - [JsonProvider](https://docs.oracle.com/javaee/7/api/javax/json/spi/JsonProvider.html)：提供 JSON 处理对象
    - [JsonbProvider](https://javaee.github.io/javaee-spec/javadocs/javax/json/bind/spi/JsonbProvider.html)：提供 JSON 绑定对象
    - [Extension](https://docs.oracle.com/javaee/7/api/javax/enterprise/inject/spi/Extension.html)：为 CDI 容器提供扩展
    - [ConfigSourceProvider](https://download.eclipse.org/microprofile/microprofile-config-2.0/apidocs/org/eclipse/microprofile/config/spi/ConfigSourceProvider.html)：提供配置属性的来源

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

        以下是完整的工具类：

        ```java
        public final class ExchangeRate {

            private static final String DEFAULT_PROVIDER = "com.baeldung.rate.spi.YahooFinanceExchangeRateProvider";

            // 获取所有提供者
            public static List<ExchangeRateProvider> providers() {
                List<ExchangeRateProvider> services = new ArrayList<>();
                ServiceLoader<ExchangeRateProvider> loader = ServiceLoader.load(ExchangeRateProvider.class);
                loader.forEach(services::add);
                return services;
            }

            // 获取默认提供者
            public static ExchangeRateProvider provider() {
                return provider(DEFAULT_PROVIDER);
            }

            // 根据名称获取提供者
            public static ExchangeRateProvider provider(String providerName) {
                ServiceLoader<ExchangeRateProvider> loader = ServiceLoader.load(ExchangeRateProvider.class);
                Iterator<ExchangeRateProvider> it = loader.iterator();
                while (it.hasNext()) {
                    ExchangeRateProvider provider = it.next();
                    if (providerName.equals(provider.getClass().getName())) {
                        return provider;
                    }
                }
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
                // 从 Yahoo API 获取数据
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
        java -cp ./exchange-rate-api/target/exchange-rate-api-1.0.0-SNAPSHOT.jar:./exchange-rate-app/target/exchange-rate-app-1.0.0-SNAPSHOT.jar:./exchange-rate-impl/target/exchange-rate-impl-1.0.0-SNAPSHOT.jar:./exchange-rate-impl/target/depends/* com.baeldung.rate.app.MainApp
        ```

        此时将看到提供者被成功加载，并输出汇率应用的结果。

        > **注意**：在类路径中指定多个 JAR 时，需根据操作系统选择分隔符：
        >
        > - Linux/macOS 使用冒号（`:`）
        > - Windows 使用分号（`;`）

5. 结论

    通过上述清晰的步骤，我们已全面了解 Java SPI 机制。它能帮助我们构建**高度可扩展**或**可替换**的模块化系统。

    尽管本例使用了 Yahoo 汇率服务来展示如何集成第三方 API，但实际生产系统**并不依赖外部 API**也能构建强大的 SPI 应用。SPI 的真正价值在于其**解耦与插件化能力**，使系统更具灵活性和可维护性。
