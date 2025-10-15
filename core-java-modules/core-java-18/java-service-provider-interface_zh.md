# [Java 中的互联网地址解析 SPI](https://www.baeldung.com/java-service-provider-interface)

网络编程

Java 接口

1. 引言

    在本教程中，我们将探讨 Java 的 **[JEP 418](https://openjdk.org/jeps/418)**，该提案为互联网主机和地址解析引入了一个全新的**服务提供者接口**（SPI）。

2. 互联网地址解析

    连接到计算机网络的任何设备都会被分配一个数值，即 **IP**（[Internet Protocol](https://www.baeldung.com/cs/ipv4-vs-ipv6)）。IP 地址用于在网络中唯一标识设备，并协助数据包的路由。

    IP 地址通常有两种类型：

    - **IPv4**：第四代 IP 标准，使用 32 位地址。
    - **IPv6**：为应对互联网快速扩张而推出的第六代标准，地址空间更大，使用十六进制字符。

    此外，还有一种相关地址：网络设备（如以太网端口或网卡 [NIC](https://www.baeldung.com/linux/nic-speed)）拥有 **MAC**（[Media Access Control](https://www.baeldung.com/cs/understanding-mac-addresses)）。MAC 地址全球唯一，用于唯一标识网络接口设备。

    **互联网地址解析**（Internet Address Resolution）广义上是指将高层网络地址（如域名 `baeldung` 或 URL `https://www.baeldung.com`）转换为底层网络地址（如 IP 地址或 MAC 地址）的过程。

3. Java 中的互联网地址解析

    Java 目前通过 `java.net.InetAddress` API 提供多种方式解析互联网地址。该 API 内部依赖操作系统的原生解析器进行 [DNS](https://www.baeldung.com/cs/dns-intro) 查询。

    当前 `InetAddress` API 所使用的操作系统原生地址解析涉及多个步骤：

    1. 系统级 DNS 缓存：存储常用 DNS 映射。
    2. 若本地 DNS 缓存未命中，系统解析器配置会提供 DNS 服务器信息。
    3. 操作系统向配置的 DNS 服务器发起查询，此过程可能递归多次。
    4. 若查询成功，DNS 地址会在各级服务器缓存并返回给客户端。
    5. 若未匹配，则触发迭代查询：从根服务器（Root Server）开始，获取权威名称服务器（Authoritative Name Servers, ANS）信息。ANS 存储顶级域名（如 `.com`、`.org`）的记录。
    6. 最终，若域名有效，则返回对应的 IP 地址；否则返回失败。

4. 使用 Java 的 InetAddress API

    `InetAddress` API 提供了多种 DNS 查询和解析方法，位于 `java.net` 包中。

    1. `getAllByName()` API

        该方法将主机名映射为一组 IP 地址（**正向解析**）：

        ```java
        InetAddress[] inetAddresses = InetAddress.getAllByName(host);
        Assert.assertTrue(Arrays.stream(inetAddresses)
            .map(InetAddress::getHostAddress)
            .toArray(String[]::new).length > 1);
        ```

    2. `getByName()` API

        与上述类似，但仅返回第一个匹配的 IP 地址：

        ```java
        InetAddress inetAddress = InetAddress.getByName("www.google.com");
        Assert.assertNotNull(inetAddress.getHostAddress()); // 返回 IP 地址
        ```

    3. `getByAddress()` API

        用于**反向解析**：输入 IP 地址，返回关联的主机名：

        ```java
        InetAddress inetAddress = InetAddress.getByAddress(ip);
        Assert.assertNotNull(inetAddress.getHostName()); // 例如返回 "google.com"
        ```

    4. `getCanonicalHostName()` 与 `getHostName()` API

        这两个方法也执行反向解析，尝试返回与 IP 关联的**完全限定域名**（FQDN）：

        ```java
        InetAddress inetAddress = InetAddress.getByAddress(ip);
        Assert.assertNotNull(inetAddress.getCanonicalHostName()); // 返回 FQDN
        Assert.assertNotNull(inetAddress.getHostName());
        ```

5. 服务提供者接口（SPI）

    **服务提供者接口**（SPI）是一种重要的软件设计模式，允许为特定服务插入可插拔的组件和实现。

    其核心优势在于：开发者可在不修改系统核心逻辑的前提下扩展功能，并自由切换不同实现，而不被绑定到单一方案。

    1. InetAddress 中的 SPI 组件

        JEP 418 遵循 SPI 模式，允许用自定义解析器替代默认的系统解析器。该 SPI 自 **Java 18 起可用**。系统通过服务定位器查找提供者；若未找到，则回退到默认实现。

        SPI 实现包含四个核心组件：

        1. **服务**（Service）：提供特定功能的接口和类集合。此处为“互联网地址解析”。
        2. **服务提供者接口**（SPI）：作为服务的代理接口，将操作委托给具体实现。此处为 `InetAddressResolver` 接口，定义了主机名与 IP 地址的查找操作。
        3. **服务提供者**（Service Provider）：SPI 的具体实现。`InetAddressResolverProvider` 是一个抽象类，作为自定义解析器的工厂。JVM 在启动时设置一个全局解析器，供 `InetAddress` 使用。
        4. **服务加载器**（ServiceLoader）：负责发现并加载合适的 `InetAddressResolverProvider` 实现。若加载失败，则自动回退到默认解析器。

    2. 自定义 InetAddressResolverProvider 实现

        JEP 418 新增了 `java.net.spi` 包，包含以下类：

        - `InetAddressResolverProvider`
        - `InetAddressResolver`
        - `InetAddressResolver.LookupPolicy`
        - `InetAddressResolverProvider.Configuration`

        下面我们实现一个自定义解析器，替代系统默认解析器。

        首先，创建一个工具类，从文件加载地址映射到内存（或缓存），供解析器使用。

        接着，定义 `CustomAddressResolverImpl` 类，继承 `InetAddressResolverProvider`，并实现两个必需方法：`name()` 和 `get(Configuration)`。

        ```java
        @Override
        public String name() {
            return "CustomInternetAddressResolverImpl";
        }
        ```

        `get()` 方法返回 `InetAddressResolver` 实例：

        ```java
        /**
        * 获取地址解析器实例
        * 
        * @param configuration 配置参数
        * @return 返回自定义的InetAddressResolver实现
        */
        @Override
        public InetAddressResolver get(Configuration configuration) {
            // 记录使用自定义解析器的信息
            LOGGER.info("Using Custom Address Resolver :: " + this.name());
            LOGGER.info("Registry initialised");
            // 返回匿名内部类实现的地址解析器
            return new InetAddressResolver() {
                /**
                * 根据主机名查找对应的IP地址
                * 
                * @param host 主机名
                * @param lookupPolicy 查找策略（在当前实现中未使用）
                * @return 返回InetAddress流
                * @throws UnknownHostException 当主机名无法解析时抛出异常
                */
                @Override
                public Stream<InetAddress> lookupByName(String host, LookupPolicy lookupPolicy) throws UnknownHostException {
                    // 从注册表中获取主机名对应的IP地址
                    return registry.getAddressesfromHost(host);
                }
                /**
                * 根据IP地址查找对应的主机名
                * 
                * @param addr IP地址的字节数组表示
                * @return 返回对应的主机名
                * @throws UnknownHostException 当IP地址无法解析时抛出异常
                */
                @Override
                public String lookupByAddress(byte[] addr) throws UnknownHostException {
                    // 从注册表中获取IP地址对应的主机名
                    return registry.getHostFromAddress(addr);
                }
            };
        }
        ```

        其中：

        - `lookupByName()`：根据主机名返回 IP 地址流（支持多 IP）。
        - `lookupByAddress()`：根据 IP 字节数组返回主机名。

    3. Registry 类实现

        我们使用 `HashMap<String, List<byte[]>>` 存储主机名（键）与 IP 地址列表（值，以字节数组形式）。

        **正向解析**（主机名 → IP）：

        ```java
        /**
        * 根据主机名获取对应的IP地址列表（正向解析）
        * 
        * @param host 需要解析的主机名
        * @return 包含该主机所有IP地址的Stream流
        * @throws UnknownHostException 如果注册表中不存在该主机名
        */
        public Stream<InetAddress> getAddressesfromHost(String host) throws UnknownHostException {
            // 记录正向解析操作日志
            LOGGER.info("Performing Forward Lookup for HOST : " + host);
            // 检查注册表中是否存在该主机名
            if (!registry.containsKey(host)) {
                // 如果不存在，抛出UnknownHostException异常
                throw new UnknownHostException("Missing Host information in Resolver");
            }
            // 从注册表获取该主机名对应的所有IP地址字节数组
            // 转换为Stream流，将每个byte[]转换为InetAddress对象
            // 过滤掉转换失败的null对象，返回有效的InetAddress流
            return registry.get(host)
                .stream()                           // 获取IP地址列表并创建流
                .map(add -> constructInetAddress(host, add))  // 将byte[]转换为InetAddress
                .filter(Objects::nonNull);         // 过滤掉转换失败的null值
        }
        ```

        **反向解析**（IP → 主机名）：

        ```java
        /**
        * 根据IP地址获取对应的主机名（反向解析）
        * 
        * @param arr 需要解析的IP地址字节数组 (如{1,2,3,4})
        * @return 对应的主机名字符串
        * @throws UnknownHostException 如果注册表中不存在该IP地址
        */
        public String getHostFromAddress(byte[] arr) throws UnknownHostException {
            // 记录反向解析操作日志
            LOGGER.info("Performing Reverse Lookup for Address : " + Arrays.toString(arr));
            // 遍历注册表中的所有主机名-IP映射
            for (Map.Entry<String, List<byte[]>> entry : registry.entrySet()) {
                // 检查当前主机名对应的IP列表中是否包含目标IP地址
                if (entry.getValue()
                    .stream()
                    .anyMatch(ba -> Arrays.equals(ba, arr))) {  // 使用Arrays.equals比较字节数组内容
                        // 如果找到匹配的IP地址，返回对应的主机名
                        return entry.getKey();
                }
            }
            // 如果遍历完所有条目都没找到匹配的IP，抛出异常
            throw new UnknownHostException("Address Not Found");
        }
        ```

        **服务发现**：  
        在 `resources/META-INF/services/` 目录下创建文件 `java.net.spi.InetAddressResolverProvider`，内容为自定义提供者的全限定类名：

        ```txt
        com.baeldung.inetspi.providers.CustomAddressResolverImpl
        ```

        JVM 启动时会通过 `ServiceLoader` 自动加载该实现。

6. 替代方案

    若不想实现自定义解析器，可考虑以下替代方法：

    - 使用 JNDI 及其 DNS 提供者：但无法享受 `InetAddress` 的丰富 API。
    - 通过 [Project Panama](https://www.baeldung.com/java-project-panama) 的 JNI 调用操作系统原生解析器。
    - 修改 JDK 系统属性文件（如 `jdk.net.hosts.file`），指定主机映射文件。但维护完整映射列表较为困难。

7. 结论

    本文探讨了 Java 中通过 `InetAddress` API 进行互联网地址解析的机制，并深入分析了 **JEP 418 引入的 SPI**。我们实现了自定义地址解析提供者，并讨论了其他可行的替代方案。这一 SPI 为开发者提供了更高的灵活性，使其能够根据业务需求定制 DNS 解析行为。
