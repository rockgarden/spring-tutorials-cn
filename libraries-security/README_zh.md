# Security

环境准备：设置无限强度管辖政策文件

标准Java安装在加密功能强度方面受到限制，这是因为政策禁止使用大小超过特定值的密钥，例如AES的密钥大小为128。

为了克服这个限制，我们需要配置无限制权限策略文件。

为了做到这一点，我们首先需要为 JVM 安装 [Java Cryptography Extension（JCE）Unlimited Strength Jurisdiction Policy Files](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) （安装说明包含在下载中）。然后，我们需要将下载的 jar 压缩文件解压缩到我们选择的目录中，该目录包含两个jar文件：

local_policy.jar
US_export_policy.jar

最后，我们需要查找 {JAVA_HOME}/lib/security 文件夹，并用这里提取的文件替换现有的策略文件。

> 将 JCE JAR 文件复制到以下目录:
`<java-home>/lib/security           [Unix]`
`<java-home>\lib\security           [Windows]`

注意，在Java9中，我们不再需要下载策略文件包，设置加密。保单属性设置为无限制就足够了：

`Security.setProperty("crypto.policy", "unlimited");`

完成后，我们需要检查配置是否正常工作：

```java
int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
System.out.println("Max Key Size for AES : " + maxKeySize);
```

结果：`Max Key Size for AES : 2147483647`

根据getMaxAllowedKeyLength()方法返回的最大密钥大小，我们可以放心地说，无限强度策略文件已正确安装。

如果返回值等于128，则需要确保已将文件安装到运行代码的JVM中。
