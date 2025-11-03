# [初始化向量（IV）在加密中的使用](https://www.baeldung.com/java-encryption-iv)

安全 · 安全算法

1. 概述
   在本教程中，我们将讨论如何在加密算法中使用初始化向量（Initialization Vector，简称 [IV](https://en.wikipedia.org/wiki/Initialization_vector)），并探讨使用 IV 时的最佳实践。

   本文假定读者已具备密码学的基础知识。

   我们的所有示例均使用 AES 算法的不同工作模式。

2. 加密算法
   任何加密算法都会接收一些数据（明文）和一个密钥，生成加密后的数据（密文）；同时也能接收生成的密文和相同的密钥，还原出原始明文。

   例如，分组密码（block cipher）算法通过对固定长度的数据块进行加密和解密来提供安全性。我们使用不同的加密模式（mode）将算法反复应用于整个数据，并决定使用何种类型的 IV。

   对于分组密码，我们处理的是固定大小的数据块。如果明文长度小于分组大小，我们需要进行填充（padding）。某些工作模式不需要填充，因为它们将分组密码当作流密码（stream cipher）来使用。

3. 初始化向量（IV）
   在密码学算法中，我们使用 IV 作为初始状态，将其加入到密码算法中，以隐藏密文中可能存在的模式。这有助于避免每次加密时都必须重新生成密钥。

   1. IV 的特性
      在大多数加密模式中，我们需要使用一个唯一的序列（即 IV）。同一个 IV 绝不能与同一个密钥重复使用。这确保了即便使用相同密钥对同一段明文多次加密，也能产生不同的密文。

      让我们看看 IV 在不同加密模式下应具备的一些特性：

      - 必须是非重复的（non-repeating）
      - 根据加密模式的不同，还需要具备随机性（random）
      - 无需保密（need not be secret）
      - 必须是一个密码学意义上的随机数（cryptographic nonce）
      - AES 的 IV 始终为 128 位（16 字节），与其密钥长度无关

   2. IV 的生成
      我们可以直接从 Cipher 类中获取 IV：

      ```java
      byte[] iv = cipher.getIV();
      ```

      如果我们不确定默认实现，也可以自己编写方法来生成 IV。如果我们没有显式提供 IV，Cipher.getIV() 方法会隐式地获取一个合适的 IV。只要符合上述特性，我们可以采用任意方法生成 IV。

      首先，我们使用 SecureRandom 类生成一个随机 IV：

      ```java
      public static IvParameterSpec getIVSecureRandom(String algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException {
          SecureRandom random = SecureRandom.getInstanceStrong();
          byte[] iv = new byte[Cipher.getInstance(algorithm).getBlockSize()];
          random.nextBytes(iv);
          return new IvParameterSpec(iv);
      }
      ```

      其次，我们也可以从 Cipher 类中获取其内部生成的参数：

      ```java
      public static IvParameterSpec getIVInternal(Cipher cipher) throws InvalidParameterSpecException {
          AlgorithmParameters params = cipher.getParameters();
          byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
          return new IvParameterSpec(iv);
      }
      ```

      我们可以使用上述任一方法生成一个随机且不可预测的 IV。然而，对于某些模式（如 GCM），IV 需与计数器（counter）配合使用。在这种情况下，通常使用前 12 字节作为 IV，后 4 字节作为计数器：

      ```java
      public static byte[] getRandomIVWithSize(int size) {
          byte[] nonce = new byte[size];
          new SecureRandom().nextBytes(nonce);
          return nonce;
      }
      ```

      此时，我们必须确保计数器不重复，同时 IV 也必须是唯一的。

      最后，虽然不推荐，但我们也可以使用硬编码（hardcoded）的 IV。

4. 在不同加密模式中使用 IV
   我们知道，加密的核心作用是隐藏明文，使攻击者无法猜测其内容。因此，我们使用不同的密码工作模式来掩盖密文中的模式。

   ECB、CBC、OFB、CFB、CTR、CTS 和 XTS 等模式提供机密性（confidentiality），但无法抵御篡改和修改。我们可以添加消息认证码（MAC）或数字签名来检测篡改。此外，还有一些“认证加密”（Authenticated Encryption, AE）模式，将加密与认证结合在一起，例如 CCM、GCM、CWC、EAX、IAPM 和 OCB。

   1. 电子密码本模式（ECB）
      ECB（[Electronic Codebook](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Electronic_codebook_(ECB))）模式对每个数据块分别使用密钥进行加密。相同的明文块始终会产生相同的密文块，因此无法有效隐藏模式。因此，ECB 模式不应用于实际的加密协议。解密过程同样容易受到重放攻击（replay attacks）的影响。

      使用 ECB 模式加密数据的代码如下：

      ```java
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, key);
      ciphertext = cipher.doFinal(data);
      ```

      使用 ECB 模式解密数据的代码如下：

      ```java
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, key);
      plaintext = cipher.doFinal(cipherText);
      ```

      如上所示，ECB 模式未使用 IV，因此相同的明文会生成相同的密文，使其容易遭受攻击。尽管 ECB 模式安全性最弱，但它仍是许多加密提供程序的默认模式，因此我们必须特别注意显式指定加密模式。

   2. 密码块链接模式（CBC）
      CBC（[Cipher Block Chaining](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_block_chaining_(CBC))）模式使用 IV 来防止相同的明文生成相同的密文。我们必须确保 IV 是真正随机或唯一的，否则将重现 ECB 模式的漏洞。

      我们使用 getIVSecureRandom 方法获取一个随机 IV：

      ```java
      IvParameterSpec iv = CryptoUtils.getIVSecureRandom("AES");
      ```

      首先，使用该 IV 在 CBC 模式下加密数据：

      ```java
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, key, iv);
      ```

      接着，在解密时，需使用相同的 IvParameterSpec 对象传递 IV：

      ```java
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
      ```

   3. 密文反馈模式（CFB）
      CFB（[Cipher Feedback](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_feedback_(CFB))）模式是最基本的流加密模式之一，属于自同步流密码（self-synchronizing stream cipher）。与 CBC 模式不同，CFB 模式无需填充。在 CFB 模式中，IV 用于生成密钥流（keystream）。如果对不同消息重复使用相同的 IV，密文中可能会暴露出相似性。与 CBC 类似，IV 也必须是随机的；如果 IV 可预测，则会丧失机密性。

      我们为 CFB 模式生成一个随机 IV：

      ```java
      IvParameterSpec iv = CryptoUtils.getIVSecureRandom("AES/CFB/NoPadding");
      ```

      极端情况下，如果使用全零 IV，在 CFB-8 模式下，某些密钥可能会导致对全零明文不进行任何加密——即密文等于明文。这种情况对约 1/256 的密钥成立。

      对于 CBC 和 CFB 模式，重复使用 IV 会泄露两条消息中相同数据块的信息。

   4. 计数器模式（CTR）与输出反馈模式（OFB）
      CTR（[Counter](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Counter_(CTR))）模式和 OFB（[Output Feedback](https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Output_feedback_(OFB))）模式将分组密码转换为同步流密码（synchronous stream cipher）。这两种模式都会生成密钥流块。此时，我们使用特定的 IV 初始化密码算法。通常分配前 12 字节作为 IV，后 4 字节作为计数器，这样最多可加密 2^32 个数据块。

      我们创建一个 IV：

      ```java
      IvParameterSpec ivSpec = CryptoUtils.getIVSecureRandom("AES");
      ```

      在 CTR 模式中，初始密钥流依赖于 IV 和密钥。如果重复使用 IV，会导致密钥流重用，从而破坏安全性。

      如果 IV 不唯一，计数器可能无法为对应重复计数器块的数据提供预期的机密性。不过，其他未重复的数据块不会受到影响。

   5. Galois/计数器模式（GCM）
      GCM（[Galois/Counter Mode](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf)）是一种认证加密附加数据（AEAD）模式，它将计数器模式加密与认证机制相结合，不仅保护明文，还可保护附加认证数据（AAD, Additional Authenticated Data）。

      然而，GCM 的安全性依赖于 IV 的唯一性。我们在 GCM 中使用 nonce（一次性随机数）作为 IV。哪怕只重复使用一次 IV，整个实现就可能面临安全攻击。

      由于 GCM 使用 AES 进行加密，其 IV（或称计数器）为 16 字节。通常，我们使用前 12 字节作为 IV，后 4 字节作为计数器（nonce）。

      在 GCM 模式下创建 IV 时，需使用 GCMParameterSpec。示例如下：

      ```java
      byte[] iv = CryptoUtils.getRandomIVWithSize(12);
      ```

      首先，获取 Cipher 实例并使用 IV 初始化：

      ```java
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
      ```

      解密时，同样使用该 IV 初始化 Cipher：

      ```java
      cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
      ```

      在 GCM 模式下，IV 也必须唯一，否则攻击者可能恢复出明文。

   6. IV 使用总结
      下表总结了不同加密模式对 IV 的要求：

        | 模式       | 初始化向量（IV）                         |
        |------------|----------------------------------------|
        | ECB        | 无 IV                                   |
        | CBC/CFB    | 随机且不可预测的 IV                      |
        | OFB/CTR    | 唯一的 IV（计数器）                      |
        | GCM        | 唯一的 IV（12 字节）和计数器（4 字节）    |

      如上所述，使用相同密钥重复使用 IV 会导致安全性丧失。如有可能，应优先选择更高级的模式（如 GCM）。此外，某些模式（如 CCM）在标准 Java 加密扩展（JCE）中不可用，此时可使用 [Bouncy Castle](https://github.com/bcgit/bc-java) 等第三方库实现。

5. 结论
   本文展示了如何在不同加密模式中使用 IV，并讨论了使用 IV 时可能遇到的问题及最佳实践。
