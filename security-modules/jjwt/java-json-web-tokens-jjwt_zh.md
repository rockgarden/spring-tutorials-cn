
# [使用 JSON Web 标记 (JWT) 强化 Java 身份验证](https://www.baeldung.com/java-json-web-tokens-jjwt)

准备在您的 Java 应用程序中构建安全身份验证，或正在为此苦恼？不清楚使用令牌（特别是 JSON Web 令牌）的好处，或者不知道如何部署令牌？我很高兴能在本教程中为您解答这些问题以及更多问题！

在我们深入了解 JSON 网络令牌（[JWTs](https://en.wikipedia.org/wiki/JSON_Web_Token)）和 [JJWT](https://github.com/jwtk/jjwt) 库（由 Stormpath 的首席技术官莱斯-哈兹伍德创建，并由一个[贡献者社区](https://github.com/jwtk/jjwt/graphs/contributors)负责维护）之前，让我们先来了解一些基础知识。

1. 身份验证与令牌验证

    应用程序用来确认用户身份的一系列协议就是身份验证。传统上，应用程序通过会话 cookie 来持久化身份。这种模式依赖于服务器端会话 ID 的存储，这就迫使开发人员创建会话存储，这种存储要么是唯一的、服务器特定的，要么是作为完全独立的会话存储层实现的。

    令牌身份验证就是为了解决服务器端会话 ID 没有也无法解决的问题而开发的。与传统身份验证一样，用户需要提供可验证的凭证，但现在他们收到的不是会话 ID，而是一组令牌。初始凭证可以是标准的用户名/密码对、API密钥，甚至是来自其他服务的令牌（Stormpath的API密钥验证功能就是一个例子）。

    1. 为什么要使用令牌？

        很简单，使用令牌代替会话 ID 可以降低服务器负载，简化权限管理，并为支持分布式或基于云的基础设施提供更好的工具。使用 JWT，这主要是通过这些类型的令牌的无状态特性来实现的。

        令牌的应用非常广泛，包括跨站请求伪造（[CSRF](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html)）保护方案、[OAuth 2.0](https://tools.ietf.org/html/rfc6749) 交互、会话 ID 和（cookie 中的）验证表示。在大多数情况下，标准并不指定令牌的特定格式。下面是一个 HTML 表单中典型的 [Spring Security CSRF](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/csrf/CsrfToken.html) 令牌示例：

        `<input name="_csrf" type="hidden" value="f3f42ea9-3104-4d13-84c0-7bcb68202f16"/>`

        如果我们试图在没有正确 CSRF 标记的情况下发布该表单，就会得到错误响应，这就是标记的作用。上述示例是一个 "哑(dumb)" 令牌。因此，我们无法从令牌本身获得任何内在含义。这也是 JWT 大显身手的地方。

2. JWT 包含什么？

    JWT（发音为 "jots"）是经过编码、加密签名（有时是加密的）的 URL 安全字符串，我们可以在各种应用中将其用作标记。下面是一个使用 JWT 作为 CSRF 标记的示例：

    ```html
    <input name="_csrf" type="hidden" 
    value="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJlNjc4ZjIzMzQ3ZTM0MTBkYjdlNjg3Njc4MjNiMmQ3MCIsImlhdCI6MTQ2NjYzMzMxNywibmJmIjoxNDY2NjMzMzE3LCJleHAiOjE0NjY2MzY5MTd9.rgx_o8VQGuDa2AqCHSgVOD5G68Ld_YYM7N7THmvLIKc"/>
    ```

    在这个例子中，我们可以看到令牌比之前的例子要长得多。就像我们之前看到的那样，如果我们在没有标记的情况下提交表单，就会得到错误响应。

    那么，为什么要使用 JWT？

    上述令牌是经过加密签名的，因此可以进行验证，从而证明它没有被篡改。此外，JWT 还包含各种附加信息。

    让我们来看看 JWT 的构造，以便更好地理解我们如何从中榨取这些好处。我们可以看到有三个不同的部分，中间用句号（.）隔开：

    |标题 |eyJhbGciOiJIUzI1NiJ9 |
    |-|-|
    |有效载荷 |eyJqdGkiOiJlNjc4ZjIzMzQ3ZTM0MTBkYjdlNjg3Njc4MjNiMmQ3MCIsImlhdC I6MTQ2NjYzMzMxNywibmJmIjoxNDY2NjMzMzE3LCJleHAiOjE0NjY2MzY5MTd9|
    |签名 |rgx_o8VQGuDa2AqCHSgVOD5G68Ld_YYM7N7THmvLIKc|

    每个部分都是 [base64](https://en.wikipedia.org/wiki/Base64) URL 编码。这确保了我们可以在 URL 中安全地使用它（稍后详述）。让我们逐一仔细查看每个部分。

    1. 标题

        如果我们使用 base64 来解码标头，就会得到下面的 JSON 字符串：

        `{"alg":"HS256"}`

        这表明 JWT 是用 [SHA-256](https://en.wikipedia.org/wiki/SHA-2) 进行 [HMAC](https://en.wikipedia.org/wiki/Hash-based_message_authentication_code) 签名的。

    2. 有效载荷

        如果我们对有效载荷进行解码，就会得到下面的 JSON 字符串（格式清晰）：

        ```json
        {
        "jti": "e678f23347e3410db7e68767823b2d70",
        "iat": 1466633317,
        "nbf": 1466633317,
        "exp": 1466636917
        }
        ```

        在有效载荷中，我们可以看到有许多带值的键。这些键被称为 "权利要求(claims)"，JWT 规范将其中七个指定为 "registered" 权利要求：

        | iss | Issuer     |
        |-----|------------|
        | sub | Subject    |
        | aud | Audience   |
        | exp | Expiration |
        | nbf | Not Before |
        | iat | Issued At  |
        | jti | JWT ID     |

        在创建 JWT 时，我们可以根据需要添加任何自定义声明。上面的列表只代表在使用的密钥和预期类型中保留的声明。我们的 CSRF 有一个 JWT ID、一个 Issued At（签发时间）、一个 Not Before（非签发时间） 和一个 Expiration time（过期时间）。过期时间正好是签发时间之后的一分钟。

    3. 签名

        最后，我们将把报文头和有效载荷（中间为.）放在一起，通过指定算法（本例中为使用 SHA-256 的 HMAC）和已知密文来创建签名部分。请注意，密文始终是一个字节数组，其长度应与所使用的算法相符。下面，我们将使用一个转换成字节数组的随机 base64 编码字符串（以保证可读性）。

        伪代码看起来是这样的：

        ```html
        computeHMACSHA256(
            header + "." + payload, 
            base64DecodeToByteArray("4pE8z3PBoHjnV1AhvGk+e8h2p+ShZpOnpr8cwHmMh1w=")
        )
        ```

        只要我们知道这个secret，我们就可以自己生成签名，并将我们的结果与 JWT 的签名部分进行比较，以验证它没有被篡改。从技术上讲，经过加密签名的 JWT 被称为 [JWS](https://tools.ietf.org/html/rfc7515)。JWT 也可以加密，这样就是 [JWE](https://tools.ietf.org/html/rfc7516)。在实际应用中，我们使用 JWT 来描述 JWE 和 JWS。

        这又回到了使用 JWT 作为 CSRF 令牌的好处。我们可以验证签名，并使用 JWT 中编码的信息来确认其有效性。JWT 的字符串表示需要与服务器范围内存储的信息相匹配，我们可以通过检查过期声明来确保它没有过期。这样，服务器就无需维护额外的状态了。

3. 设置 JJWT 教程

    JJWT (<https://github.com/jwtk/jjwt>) 是一个 Java 库，提供端到端的 JSON 网络令牌创建和验证。该库永久免费且开源（Apache 许可证，2.0 版），其设计采用了以构建器为中心的界面，隐藏了大部分复杂性。

    使用 JJWT 的主要操作包括构建和解析 JWT。首先，我们将了解这些操作。然后，我们将了解 JJWT 的一些扩展功能。最后，我们将看到 JWT 作为 CSRF 令牌在 Spring Security、Spring Boot 应用程序中的应用。

    Spring Boot 的一大优点是[构建和启动应用程序](https://www.baeldung.com/spring-boot-run-maven-vs-executable-jar)非常简单。要运行 JJWT Fun 应用程序，我们只需执行以下操作：

    `mvn clean spring-boot:run`

    本示例应用程序暴露了十个端点（我们使用 httpie 与应用程序交互；可在[此处](https://github.com/jkbrzt/httpie)找到）：

    `http localhost:8080`

    ```txt
    Available commands (assumes httpie - https://github.com/jkbrzt/httpie):

    http http://localhost:8080/
    This usage message

    http http://localhost:8080/static-builder
    build JWT from hardcoded claims

    http POST http://localhost:8080/dynamic-builder-general claim-1=value-1 ... [claim-n=value-n]
    build JWT from passed in claims (using general claims map)

    http POST http://localhost:8080/dynamic-builder-specific claim-1=value-1 ... [claim-n=value-n]
    build JWT from passed in claims (using specific claims methods)

    http POST http://localhost:8080/dynamic-builder-compress claim-1=value-1 ... [claim-n=value-n]
    build DEFLATE compressed JWT from passed in claims

    http http://localhost:8080/parser?jwt=<jwt>
    Parse passed in JWT

    http http://localhost:8080/parser-enforce?jwt=<jwt>
    Parse passed in JWT enforcing the 'iss' registered claim and the 'hasMotorcycle' custom claim

    http http://localhost:8080/get-secrets
    Show the signing keys currently in use.

    http http://localhost:8080/refresh-secrets
    Generate new signing keys and show them.

    http POST http://localhost:8080/set-secrets 
        HS256=base64-encoded-value HS384=base64-encoded-value HS512=base64-encoded-value
    Explicitly set secrets to use in the application.
    ```

    在接下来的章节中，我们将逐一检查这些端点以及处理程序中包含的 JJWT 代码。

4. 使用 JJWT 创建 JWT

    由于 JJWT 具有[流畅的接口](https://en.wikipedia.org/wiki/Fluent_interface)，因此创建 JWT 基本上只需三步：

    - 定义令牌的内部声明，如发行者、主题、有效期和 ID
    - 对 JWT 进行加密签名（使其成为 JWS）
    - 根据 [JWT 压缩序列化](https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)规则，将 JWT 压缩成 URL 安全的字符串

    最终的 JWT 将是一个由三个部分组成的 base64 编码字符串，使用指定的签名算法和提供的密钥进行签名。到此为止，我们就可以与另一方共享令牌了。

    下面是 JJWT 的运行示例：

    ```java
    String jws = Jwts.builder()
    .setIssuer("Stormpath")
    .setSubject("msilverman")
    .claim("name", "Micah Silverman")
    .claim("scope", "admins")
    // Fri Jun 24 2016 15:33:42 GMT-0400 (EDT)
    .setIssuedAt(Date.from(Instant.ofEpochSecond(1466796822L)))
    // Sat Jun 24 2116 15:33:42 GMT-0400 (EDT)
    .setExpiration(Date.from(Instant.ofEpochSecond(4622470422L)))
    .signWith(
        SignatureAlgorithm.HS256,
        TextCodec.BASE64.decode("Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=")
    )
    .compact();
    ```

    这与代码项目中的 StaticJWTController.fixedBuilder 方法中的代码非常相似。

    说到这里，有必要谈谈与 JWT 和签名相关的一些反模式。如果我们以前看过 JWT 示例，就很可能遇到过这些签名反模式情况：

    所有 HS 类型签名算法都使用字节数组。将字符串转换为字节数组便于人类读取。

    ```java
    // 1.
    .signWith(
        SignatureAlgorithm.HS256,
    "secret".getBytes("UTF-8")    
    )
    // 2.
    .signWith(
        SignatureAlgorithm.HS256,
        "Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=".getBytes("UTF-8")
    )
    // 3.
    .signWith(
        SignatureAlgorithm.HS512,
        TextCodec.BASE64.decode("Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=")
    )
    ```

    上述反模式 1 就说明了这一点。这是有问题的，因为秘密太短，而且不是字节数组的原生形式，因此会被削弱。因此，为了保持可读性，我们可以对字节数组进行 base64 编码。

    然而，反模式 2 将 base64 编码的字符串直接转换为字节数组。因此，我们应该将 base64 字符串解码回原始字节数组。

    上面的第 3 种模式就证明了这一点。那么，为什么这也是一种反模式呢？这里有一个微妙的原因。请注意，签名算法是 HS512。字节数组并不是 HS512 所能支持的最大长度，因此它比该算法所能支持的加密更弱。

    示例代码中包含了一个名为 SecretService 的类，它可以确保我们使用的秘密强度符合给定算法的要求。在应用程序启动时，我们会为每种 HS 算法创建一组新的秘密。有一些端点可以刷新秘密，也可以显式设置秘密。

    如果我们的项目如上所述运行，我们可以执行以下操作，使下面的 JWT 示例与我们项目的响应相匹配：

    ```bash
    http POST localhost:8080/set-secrets \
    HS256="Yn2kjibddFAWtnPJ2AFlL8WXmohJMCvigQggaEypa5E=" \
    HS384="VW96zL+tYlrJLNCQ0j6QPTp+d1q75n/Wa8LVvpWyG8pPZOP6AA5X7XOIlI90sDwx" \
    HS512="cd+Pr1js+w2qfT2BoCD+tPcYp9LbjpmhSMEJqUob1mcxZ7+Wmik4AYdjX+DlDjmE4yporzQ9tm7v3z/j+QbdYg=="
    ```

    然后，我们就可以访问 /static-builder 端点了：

    `http <http://localhost:8080/static-builder>`

    这样就生成了如下所示的 JWT：

    ```txt
    eyJhbGciOiJIUzI1NiJ9.
    eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIiwibmFtZSI6Ik1pY2FoIFNpbHZlcm1hbiIsInNjb3BlIjoiYWRtaW5zIiwiaWF0IjoxNDY2Nzk2ODIyLCJleHAiOjQ2MjI0NzA0MjJ9.
    kP0i_RvTAmI8mgpIkDFhRX3XthSdP-eqqFKGcU92ZIQ
    ```

    接下来，我们将点击

    ```bash
    http http://localhost:8080/parser?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIiwibmFtZSI6Ik1pY2FoIFNpbHZlcm1hbiIsInNjb3BlIjoiYWRtaW5zIiwiaWF0IjoxNDY2Nzk2ODIyLCJleHAiOjQ2MjI0NzA0MjJ9.kP0i_RvTAmI8mgpIkDFhRX3XthSdP-eqqFKGcU92ZIQ
    ```

    响应中包含了我们创建 JWT 时包含的所有声明：

    ```log
    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    ...
    {
        "jws": {
            "body": {
                "exp": 4622470422,
                "iat": 1466796822,
                "iss": "Stormpath",
                "name": "Micah Silverman",
                "scope": "admins",
                "sub": "msilverman"
            },
            "header": {
                "alg": "HS256"
            },
            "signature": "kP0i_RvTAmI8mgpIkDFhRX3XthSdP-eqqFKGcU92ZIQ"
        },
        "status": "SUCCESS"
    }
    ```

    这是解析操作，我们将在下一节详细介绍。

    然后，我们将点击一个接收声明作为参数的端点，并为我们创建一个自定义 JWT：

    `http -v POST localhost:8080/dynamic-builder-general iss=Stormpath sub=msilverman hasMotorcycle:=true`

    > 注意：“hasMotorcycle”: “true” 申请与其他申请之间有一个微妙的区别：httpie 默认假定 JSON 参数为字符串。要使用 httpie 提交原始 JSON，我们可以使用 `:=` 形式，而不是 `=`。

    下面是输出结果：

    ```log
    POST /dynamic-builder-general HTTP/1.1
    Accept: application/json
    ...
    {
        "hasMotorcycle": true,
        "iss": "Stormpath",
        "sub": "msilverman"
    }

    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    ...
    {
        "jwt": 
        "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIiwiaGFzTW90b3JjeWNsZSI6dHJ1ZX0.OnyDs-zoL3-rw1GaSl_KzZzHK9GoiNocu-YwZ_nQNZU",
        "status": "SUCCESS"
    }
    ```

    让我们看看支持该端点的代码：

    main/io.jsonwebtoken.jjwtfun.controller/DynamicJWTController.java:dynamicBuilderGeneric()

    第 2 行确保传入的 JSON 会自动转换为 `Java Map<String,Object>`，这对 JJWT 来说非常方便，因为第 5 行的方法只需使用该 Map 并一次性设置所有请求。

    虽然这段代码简洁明了，但我们还需要一些更具体的东西来确保传递的索赔是有效的。如果我们已经知道映射中的索赔是有效的，那么使用 .`setClaims(Map<String, Object> claims)` 方法就很方便了。因此，Java 的类型安全性在 JJWT 库中得到了体现。

    对于 JWT 规范中定义的每一种注册声称，JJWT 中都有一个相应的 Java 方法，该方法采用规范正确的类型。

    让我们点击示例中的另一个端点，看看会发生什么：

    `http -v POST localhost:8080/dynamic-builder-specific iss=Stormpath sub:=5 hasMotorcycle:=true`

    请注意，我们为 "sub" 输入了整数 5。下面是输出结果：

    ```log
    POST /dynamic-builder-specific HTTP/1.1
    Accept: application/json
    ...
    {
        "hasMotorcycle": true,
        "iss": "Stormpath",
        "sub": 5
    }

    HTTP/1.1 400 Bad Request
    Connection: close
    Content-Type: application/json;charset=UTF-8
    ...
    {
        "exceptionType": "java.lang.ClassCastException",
        "message": "java.lang.Integer cannot be cast to java.lang.String",
        "status": "ERROR"
    }
    ```

    正如我们所看到的，我们得到了一个错误响应，因为代码正在强制执行 Registered Claims 的类型。在这种情况下，sub 必须是字符串。下面是支持该端点的代码：

    ```java
    @RequestMapping(value = "/dynamic-builder-specific", method = POST)
    public JwtResponse dynamicBuilderSpecific(@RequestBody Map<String, Object> claims) 
    throws UnsupportedEncodingException {
        JwtBuilder builder = Jwts.builder();
        
        claims.forEach((key, value) -> {
            switch (key) {
                case "iss":
                    builder.setIssuer((String) value);
                    break;
                case "sub":
                    builder.setSubject((String) value);
                    break;
                case "aud":
                    builder.setAudience((String) value);
                    break;
                case "exp":
                    builder.setExpiration(Date.from(
                        Instant.ofEpochSecond(Long.parseLong(value.toString()))
                    ));
                    break;
                case "nbf":
                    builder.setNotBefore(Date.from(
                        Instant.ofEpochSecond(Long.parseLong(value.toString()))
                    ));
                    break;
                case "iat":
                    builder.setIssuedAt(Date.from(
                        Instant.ofEpochSecond(Long.parseLong(value.toString()))
                    ));
                    break;
                case "jti":
                    builder.setId((String) value);
                    break;
                default:
                    builder.claim(key, value);
            }
        });
    
        builder.signWith(SignatureAlgorithm.HS256, secretService.getHS256SecretBytes());

        return new JwtResponse(builder.compact());
    }
    ```

    和之前一样，该方法接受一个索赔 `Map<String, Object>` 作为参数。不过，这次我们调用的是每个已注册权利要求的特定方法，该方法会强制执行类型。

    其中一项改进是使错误信息更加具体。现在，我们只知道其中一个索赔的类型不正确。我们不知道哪个索赔出错了，也不知道它应该是什么。因此，这里有一种方法可以让我们得到更具体的错误信息。它还能解决当前代码中的一个错误：

    main/io.jsonwebtoken.jjwtfun.controller/DynamicJWTController.java:ensureType()

    ```java
    private void ensureType(String registeredClaim, Object value, Class expectedType) {
        boolean isCorrectType =
            expectedType.isInstance(value) ||
            expectedType == Long.class && value instanceof Integer;

        if (!isCorrectType) {
            String msg = "Expected type: " + expectedType.getCanonicalName() + 
            " for registered claim: '" + registeredClaim + "', but got value: " + 
                value + " of type: " + value.getClass().getCanonicalName();
            throw new JwtException(msg);
        }
    }
    ```

    此外，第 3 行还会检查传入的值是否属于预期类型。如果不是，它将抛出一个带有特定错误的 JwtException。接下来，我们将进行与前面相同的调用，看看它的实际效果：

    `http -v POST localhost:8080/dynamic-builder-specific iss=Stormpath sub:=5 hasMotorcycle:=true`

    ```log
    POST /dynamic-builder-specific HTTP/1.1
    Accept: application/json
    ...
    User-Agent: HTTPie/0.9.3

    {
        "hasMotorcycle": true,
        "iss": "Stormpath",
        "sub": 5
    }

    HTTP/1.1 400 Bad Request
    Connection: close
    Content-Type: application/json;charset=UTF-8
    ...
    {
        "exceptionType": "io.jsonwebtoken.JwtException",
        "message": 
        "Expected type: java.lang.String for registered claim: 'sub', but got value: 5 of type: java.lang.Integer",
        "status": "ERROR"
    }
    ```

    现在我们有了一个非常具体的错误信息，告诉我们出错的是子索赔。

    最后，让我们回到代码中的错误。这个问题与 JJWT 库无关。问题在于 Spring Boot 内置的 JSON 到 Java 对象映射器太聪明了。

    如果有接受 Java 对象的方法，JSON 映射器会自动将小于或等于 2,147,483,647 的传入数字转换为 Java 整数。同样，它也会自动将传入的大于 2,147,483,647 的数字转换为 Java Long。对于 JWT 的 iat、nbf 和 exp 声明，我们希望无论映射对象是整数还是长，我们的 ensureType 测试都能通过。这就是为什么我们在确定传入的值是否为正确类型时要使用附加子句的原因：

    ```java
    boolean isCorrectType =
        expectedType.isInstance(value) ||
        expectedType == Long.class && value instanceof Integer;
    ```

    如果我们期待的是 Long，但值是 Integer 的实例，我们仍然认为它是正确的类型。现在，我们已经了解了这种验证的原理，可以将它集成到我们的 dynamicBuilderSpecific 方法中：

    main/io.jsonwebtoken.jjwtfun.controller/DynamicJWTController.java:dynamicBuilderSpecific()

    > 注意：在本节的所有示例代码中，JWT 都使用 SHA-256 算法的 HMAC 进行签名。这样做是为了使示例简单明了。不过，JJWT 库支持 12 种不同的签名算法，我们可以在自己的代码中加以利用。

5. 使用 JJWT 解析 JWT

    前面我们看到，我们的代码示例中有一个用于解析 JWT 的端点。当我们点击这个端点时

    ```bash
    http http://localhost:8080/parser?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIiwibmFtZSI6Ik1pY2FoIFNpbHZlcm1hbiIsInNjb3BlIjoiYWRtaW5zIiwiaWF0IjoxNDY2Nzk2ODIyLCJleHAiOjQ2MjI0NzA0MjJ9.kP0i_RvTAmI8mgpIkDFhRX3XthSdP-eqqFKGcU92ZIQ
    ```

    时，我们会得到这样的响应：

    ```console
    HTTP/1.1 200 OK
    Content-Type: application/json;charset=UTF-8
    ...
    {
        "claims": {
            "body": {
                "exp": 4622470422,
                "iat": 1466796822,
                "iss": "Stormpath",
                "name": "Micah Silverman",
                "scope": "admins",
                "sub": "msilverman"
            },
            "header": {
                "alg": "HS256"
            },
            "signature": "kP0i_RvTAmI8mgpIkDFhRX3XthSdP-eqqFKGcU92ZIQ"
        },
        "status": "SUCCESS"
    }
    ```

    StaticJWTController 类的解析器方法如下所示：

    ```java
    @RequestMapping(value = "/parser", method = GET)
    public JwtResponse parser(@RequestParam String jwt) throws UnsupportedEncodingException {
        Jws<Claims> jws = Jwts.parser()
            .setSigningKeyResolver(secretService.getSigningKeyResolver())
            .parseClaimsJws(jwt);

        return new JwtResponse(jws);
    }
    ```

    第 4 行表明，我们希望传入的字符串是已签名的 JWT（JWS）。在解析 JWT 时，我们还使用了与签署 JWT 相同的密文。第 5 行解析 JWT 中的声明。在内部，它会验证签名，如果签名无效，就会抛出异常。

    请注意，在这种情况下，我们传递的是签名密钥解析器，而不是密钥本身。这是 JJWT 最强大的功能之一。此外，JWT 的标头指明了用于签名的算法。但是，在信任 JWT 之前，我们需要验证它。这似乎是一个陷阱。让我们看看 SecretService.getSigningKeyResolver 方法：

    ```java
    private SigningKeyResolver signingKeyResolver = new SigningKeyResolverAdapter() {
        @Override
        public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
            return TextCodec.BASE64.decode(secrets.get(header.getAlgorithm()));
        }
    };
    ```

    通过访问 JwsHeader，我们可以检查算法，并返回用于签署 JWT 的密文的适当字节数组。现在，使用这个字节数组作为密钥，JJWT 将验证 JWT 没有被篡改。

    如果我们删除传入的 JWT 的最后一个字符（这是签名的一部分），就会得到这样的响应：

    ```console
    HTTP/1.1 400 Bad Request
    Connection: close
    Content-Type: application/json;charset=UTF-8
    Date: Mon, 27 Jun 2016 13:19:08 GMT
    Server: Apache-Coyote/1.1
    Transfer-Encoding: chunked

    {
        "exceptionType": "io.jsonwebtoken.SignatureException",
        "message": 
        "JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.",
        "status": "ERROR"
    }
    ```

6. JWT 实践： Spring 安全 CSRF 标记

    虽然这篇文章的重点不是 Spring Security，但我们还是要深入探讨一下，以展示 JJWT 库在现实世界中的一些用法。

    [跨站请求伪造](https://en.wikipedia.org/wiki/Cross-site_request_forgery)是一种安全漏洞，即恶意网站欺骗我们向已建立信任的网站提交请求。常见的解决方案之一是实施同步令牌模式。这种方法会在网络表单中插入一个令牌，应用服务器会根据其存储库检查传入的令牌，以确认其正确性。如果令牌缺失或无效，服务器将作出错误响应。

    Spring Security 内置了同步令牌模式。更妙的是，如果我们使用 Spring Boot 和 Thymeleaf 模板，同步器令牌会自动为我们插入。

    默认情况下，Spring Security 使用的令牌是 "dumb" 令牌。它只是一串字母和数字。这种方法很好，也很有效。在本节中，我们将使用 JWT 作为令牌来增强基本功能。除了验证提交的令牌是否是我们所期望的令牌外，我们还将验证 JWT，以进一步证明令牌未被篡改且未过期。

    首先，我们将使用 Java 配置来配置 Spring Security。默认情况下，所有路径都需要验证，所有 POST 端点都需要 CSRF 标记。我们将稍稍放宽这一要求，以便我们目前构建的系统仍能正常运行：

    ```java
    @Configuration
    public class WebSecurityConfig {

        private String[] ignoreCsrfAntMatchers = {
            "/dynamic-builder-compress",
            "/dynamic-builder-general",
            "/dynamic-builder-specific",
            "/set-secrets"
        };

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf()
                    .ignoringAntMatchers(ignoreCsrfAntMatchers)
                .and().authorizeRequests()
                    .antMatchers("/**")
                    .permitAll();
            return http.build();
        }
    }
    ```

    我们要做两件事。首先，我们表示在向 REST API 端点发布时不需要 CSRF 标记（第 15 行）。其次，我们表示所有路径都应允许未经验证的访问（第 17 - 18 行）。

    让我们来确认 Spring Security 是否按照我们预期的方式运行。启动应用程序，在浏览器中输入以下网址：

    <http://localhost:8080/jwt-csrf-form>

    下面是该视图的 Thymeleaf 模板：

    ```xml
    <!DOCTYPE html>
    <html lang="en" xmlns:th="http://www.thymeleaf.org">
        <head>
            <!--/*/ <th:block th:include="fragments/head :: head"/> /*/-->
        </head>
        <body>
            <div class="container-fluid">
                <div class="row">
                    <div class="box col-md-6 col-md-offset-3">
                        <p/>
                        <form method="post" th:action="@{/jwt-csrf-form}">
                            <input type="submit" class="btn btn-primary" value="Click Me!"/>
                        </form>
                    </div>
                </div>
            </div>
        </body>
    </html>
    ```

    这是一个非常基本的表单，提交后将会 POST 到同一个端点。此外，我们会注意到表单中没有明确提及 CSRF 标记。如果我们查看源代码，会看到如下内容

    `<input type="hidden" name="_csrf" value="5f375db2-4f40-4e72-9907-a290507cb25e" />`

    这就是我们需要的所有确认信息，即 Spring Security 正在运行，Thymeleaf 模板正在自动插入 CSRF 令牌。

    为了使值成为 JWT，我们将启用自定义 CsrfTokenRepository。下面是 Spring Security 配置的变化：

    ```java
    @Configuration
    public class WebSecurityConfig {

        @Autowired
        CsrfTokenRepository jwtCsrfTokenRepository;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf()
                    .csrfTokenRepository(jwtCsrfTokenRepository)
                    .ignoringAntMatchers(ignoreCsrfAntMatchers)
                .and().authorizeRequests()
                    .antMatchers("/**")
                    .permitAll();
            return http.build();
        }
    }
    ```

    然后，为了将其连接起来，我们需要一个配置，该配置将公开一个能返回自定义令牌存储库的 Bean。这是配置：

    ```java
    @Configuration
    public class CSRFConfig {

        @Autowired
        SecretService secretService;

        @Bean
        @ConditionalOnMissingBean
        public CsrfTokenRepository jwtCsrfTokenRepository() {
            return new JWTCsrfTokenRepository(secretService.getHS256SecretBytes());
        }
    }
    ```

    这是我们的自定义存储库（重要部分）：

    main/io.jsonwebtoken.jjwtfun.config/JWTCsrfTokenRepository.java

    生成令牌方法会创建一个 JWT，在创建 30 秒后过期。有了这些管道，我们就可以再次启动应用程序，查看 /jwt-csrf-form 的源代码。

    现在，隐藏字段看起来是这样的

    ```txt
    <input type="hidden" name="_csrf" 
    value="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxZjIyMDdiNTVjOWM0MjU0YjZlMjY4MjQwYjIwNzZkMSIsImlhdCI6MTQ2NzA3MDQwMCwibmJmIjoxNDY3MDcwNDAwLCJleHAiOjE0NjcwNzA0MzB9.2kYLO0iMWUheAncXAzm0UdQC1xUC5I6RI_ShJ_74e5o" />
    ```

    好极了！现在，我们的 CSRF 令牌是一个 JWT，这并不难。

    然而，这只是谜题的一半。默认情况下，Spring Security 只会保存 CSRF 令牌，并确认在网络表单中提交的令牌与保存的令牌是否匹配。我们希望扩展该功能，以验证 JWT 并确保它没有过期。为此，我们将添加一个过滤器。下面是我们的 Spring Security 配置：

    ```java
    @Configuration
    public class WebSecurityConfig {

        ...

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .addFilterAfter(new JwtCsrfValidatorFilter(), CsrfFilter.class)
                .csrf()
                    .csrfTokenRepository(jwtCsrfTokenRepository)
                    .ignoringAntMatchers(ignoreCsrfAntMatchers)
                .and().authorizeRequests()
                    .antMatchers("/**")
                    .permitAll();
            return http.build();
        }

        ...
    }
    ```

    在第 9 行，我们添加了一个过滤器，并将其置于默认 CsrfFilter 之后的过滤器链中。当我们的过滤器被触发时，JWT 标记（作为一个整体）已经被 Spring Security 确认为保存的正确值。

    下面是 JwtCsrfValidatorFilter（它是私有的，因为它是 Spring Security 配置的内部类）：

    ```java
    private class JwtCsrfValidatorFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
        HttpServletRequest request, 
        HttpServletResponse response, 
        FilterChain filterChain) throws ServletException, IOException {
            // NOTE: A real implementation should have a nonce cache so the token cannot be reused
            CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

            if (
                // only care if it's a POST
                "POST".equals(request.getMethod()) &&
                // ignore if the request path is in our list
                Arrays.binarySearch(ignoreCsrfAntMatchers, request.getServletPath()) < 0 &&
                // make sure we have a token
                token != null
            ) {
                // CsrfFilter already made sure the token matched. 
                // Here, we'll make sure it's not expired
                try {
                    Jwts.parser()
                        .setSigningKey(secret.getBytes("UTF-8"))
                        .parseClaimsJws(token.getToken());
                } catch (JwtException e) {
                    // most likely an ExpiredJwtException, but this will handle any
                    request.setAttribute("exception", e);
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    RequestDispatcher dispatcher = request.getRequestDispatcher("expired-jwt");
                    dispatcher.forward(request, response);
                }
            }

            filterChain.doFilter(request, response);
        }
    }
    ```

    请看第 23 行。我们像以前一样解析 JWT。在这种情况下，如果出现异常，请求将被转发到 expired-jwt 模板。如果 JWT 验证通过，则继续正常处理。

    这就结束了使用 JWT 令牌存储库和验证器覆盖默认 Spring Security CSRF 令牌行为的循环。

    如果我们启动应用程序，浏览到 /jwt-csrf-form，等待 30 多秒，然后点击按钮，就会看到如下内容：

    ![jwt_expired](pic/jwt_expired.webp)

7. JJWT 扩展功能

    在结束我们的 JJWT 之旅之前，我们先来谈谈 JJWT 规范之外的一些扩展功能。

    1. 强制要求

        作为解析过程的一部分，JJWT 允许我们指定所需的声明以及这些声明应具有的值。如果我们的 JWT 中必须包含某些信息才能被视为有效，那么这就非常方便了。它避免了手动验证声明的大量分支逻辑。下面是为示例项目的 /parser-enforce 端点提供服务的方法：

        ```java
        @RequestMapping(value = "/parser-enforce", method = GET)
        public JwtResponse parserEnforce(@RequestParam String jwt) 
        throws UnsupportedEncodingException {
            Jws<Claims> jws = Jwts.parser()
                .requireIssuer("Stormpath")
                .require("hasMotorcycle", true)
                .setSigningKeyResolver(secretService.getSigningKeyResolver()).build()
                .parseClaimsJws(jwt);

            return new JwtResponse(jws);
        }
        ```

        第 5 行和第 6 行向我们展示了注册请求和自定义请求的语法。在这个示例中，如果不存在 iss 索赔，或者没有 Stormpath 值，JWT 就是无效的。如果自定义 hasMotorcycle 索赔不存在或值为 true，JWT 也将无效。

        让我们先创建一个符合快乐路径的 JWT：

        ```txt
        http -v POST localhost:8080/dynamic-builder-specific \
        iss=Stormpath hasMotorcycle:=true sub=msilverman


        POST /dynamic-builder-specific HTTP/1.1
        Accept: application/json
        ...
        {
            "hasMotorcycle": true,
            "iss": "Stormpath",
            "sub": "msilverman"
        }

        HTTP/1.1 200 OK
        Cache-Control: no-cache, no-store, max-age=0, must-revalidate
        Content-Type: application/json;charset=UTF-8
        ...
        {
            "jwt": 
            "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJoYXNNb3RvcmN5Y2xlIjp0cnVlLCJzdWIiOiJtc2lsdmVybWFuIn0.qrH-U6TLSVlHkZdYuqPRDtgKNr1RilFYQJtJbcgwhR0",
            "status": "SUCCESS"
        }
        ```

        现在让我们验证一下 JWT：

        ```txt
        http -v localhost:8080/parser-enforce?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJoYXNNb3RvcmN5Y2xlIjp0cnVlLCJzdWIiOiJtc2lsdmVybWFuIn0.qrH-U6TLSVlHkZdYuqPRDtgKNr1RilFYQJtJbcgwhR0


        GET /parser-enforce?jwt=http 
        -v localhost:8080/parser-enforce?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJoYXNNb3RvcmN5Y2xlIjp0cnVlLCJzdWIiOiJtc2lsdmVybWFuIn0.qrH-U6TLSVlHkZdYuqPRDtgKNr1RilFYQJtJbcgwhR0 HTTP/1.1
        Accept: */*
        ...
        HTTP/1.1 200 OK
        Cache-Control: no-cache, no-store, max-age=0, must-revalidate
        Content-Type: application/json;charset=UTF-8
        ...
        {
            "jws": {
                "body": {
                    "hasMotorcycle": true,
                    "iss": "Stormpath",
                    "sub": "msilverman"
                },
                "header": {
                    "alg": "HS256"
                },
                "signature": "qrH-U6TLSVlHkZdYuqPRDtgKNr1RilFYQJtJbcgwhR0"
            },
            "status": "SUCCESS"
        }
        ```

        到目前为止，一切顺利。现在，这一次我们不写 hasMotorcycle：

        `http -v POST localhost:8080/dynamic-builder-specific iss=Stormpath sub=msilverman`

        然后，如果我们尝试验证 JWT：

        ```txt
        http -v localhost:8080/parser-enforce?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIn0.YMONlFM1tNgttUYukDRsi9gKIocxdGAOLaJBymaQAWc
        ```

        我们将得到：

        ```txt
        GET /parser-enforce?jwt=http -v localhost:8080/parser-enforce?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJzdWIiOiJtc2lsdmVybWFuIn0.YMONlFM1tNgttUYukDRsi9gKIocxdGAOLaJBymaQAWc HTTP/1.1
        Accept: */*
        ...
        HTTP/1.1 400 Bad Request
        Cache-Control: no-cache, no-store, max-age=0, must-revalidate
        Connection: close
        Content-Type: application/json;charset=UTF-8
        ...
        {
            "exceptionType": "io.jsonwebtoken.MissingClaimException",
            "message": 
            "Expected hasMotorcycle claim to be: true, but was not present in the JWT claims.",
            "status": "ERROR"
        }
        ```

        这表明我们的 "hasMotorcycle" 请求是预期的，但却丢失了。

        再举一个例子：

        `http -v POST localhost:8080/dynamic-builder-specific iss=Stormpath hasMotorcycle:=false sub=msilverman`

        这一次，所需的声明是存在的，但它的值是错误的。让我们看看输出结果：

        ```txt
        http -v localhost:8080/parser-enforce?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJoYXNNb3RvcmN5Y2xlIjpmYWxzZSwic3ViIjoibXNpbHZlcm1hbiJ9.8LBq2f0eINB34AzhVEgsln_KDo-IyeM8kc-dTzSCr0c


        GET /parser-enforce?jwt=http 
        -v localhost:8080/parser-enforce?jwt=eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJoYXNNb3RvcmN5Y2xlIjpmYWxzZSwic3ViIjoibXNpbHZlcm1hbiJ9.8LBq2f0eINB34AzhVEgsln_KDo-IyeM8kc-dTzSCr0c HTTP/1.1
        Accept: */*
        ...
        HTTP/1.1 400 Bad Request
        Cache-Control: no-cache, no-store, max-age=0, must-revalidate
        Connection: close
        Content-Type: application/json;charset=UTF-8
        ...
        {
            "exceptionType": "io.jsonwebtoken.IncorrectClaimException",
            "message": "Expected hasMotorcycle claim to be: true, but was: false.",
            "status": "ERROR"
        }
        ```

        这表明我们的 hasMotorcycle 索赔是存在的，但却有一个意外值。

        当我们在 JWT 中执行权利要求时，MissingClaimException 和 IncorrectClaimException 是我们的朋友，也是 JJWT 库才有的功能。

    2. JWT 压缩

        如果我们在 JWT 上有很多声明，它就会变得很大；事实上，大到在某些浏览器中可能无法在 GET url 中显示。

        让我们制作一个大的 JWT：

        ```bash
        http -v POST localhost:8080/dynamic-builder-specific \
        iss=Stormpath hasMotorcycle:=true sub=msilverman the=quick brown=fox jumped=over lazy=dog \
        somewhere=over rainbow=way up=high and=the dreams=you dreamed=of
        ```

        这是生成的 JWT：

        ```txt
        eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTdG9ybXBhdGgiLCJoYXNNb3RvcmN5Y2xlIjp0cnVlLCJzdWIiOiJtc2lsdmVybWFuIiwidGhlIjoicXVpY2siLCJicm93biI6ImZveCIsImp1bXBlZCI6Im92ZXIiLCJsYXp5IjoiZG9nIiwic29tZXdoZXJlIjoib3ZlciIsInJhaW5ib3ciOiJ3YXkiLCJ1cCI6ImhpZ2giLCJhbmQiOiJ0aGUiLCJkcmVhbXMiOiJ5b3UiLCJkcmVhbWVkIjoib2YifQ.AHNJxSTiDw_bWNXcuh-LtPLvSjJqwDvOOUcmkk7CyZA
        ```

        这可真够大的！现在，让我们用同样的要求访问一个稍有不同的端点：

        ```bash
        http -v POST localhost:8080/dynamic-builder-compress \
        iss=Stormpath hasMotorcycle:=true sub=msilverman the=quick brown=fox jumped=over lazy=dog \
        somewhere=over rainbow=way up=high and=the dreams=you dreamed=of
        ```

        这次我们会得到：

        ```txt
        eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNpEzkESwjAIBdC7sO4JegdXnoC2tIk2oZLEGB3v7s84jjse_AFe5FOikc5ZLRycHQ3kOJ0Untu8C43ZigyUyoRYSH6_iwWOyGWHKd2Kn6_QZFojvOoDupRwyAIq4vDOzwYtugFJg1QnJv-5sY-TVjQqN7gcKJ3f-j8c-6J-baDFhEN_uGn58XtnpfcHAAD__w.3_wc-2skFBbInk0YAQ96yGWwr8r1xVdbHn-uGPTFuFE
        ```

        短了 62 个字符。下面是生成 JWT 方法的代码：

        ```java
        @RequestMapping(value = "/dynamic-builder-compress", method = POST)
        public JwtResponse dynamicBuildercompress(@RequestBody Map<String, Object> claims) 
        throws UnsupportedEncodingException {
            String jws =  Jwts.builder()
                .setClaims(claims)
                .compressWith(new DeflateCompressionAlgorithm())
                .signWith(
                    SignatureAlgorithm.HS256,
                    secretService.getHS256SecretBytes()
                )
                .compact();
            return new JwtResponse(jws);
        }
        ```

        注意第 6 行，我们指定了要使用的压缩算法。这就是全部内容。

        如何解析压缩后的 JWT？JJWT 库会自动检测压缩，并使用相同的算法进行解压缩：

        ```txt
        GET /parser?jwt=eyJhbGciOiJIUzI1NiIsImNhbGciOiJERUYifQ.eNpEzkESwjAIBdC7sO4JegdXnoC2tIk2oZLEGB3v7s84jjse_AFe5FOikc5ZLRycHQ3kOJ0Untu8C43ZigyUyoRYSH6_iwWOyGWHKd2Kn6_QZFojvOoDupRwyAIq4vDOzwYtugFJg1QnJv-5sY-TVjQqN7gcKJ3f-j8c-6J-baDFhEN_uGn58XtnpfcHAAD__w.3_wc-2skFBbInk0YAQ96yGWwr8r1xVdbHn-uGPTFuFE HTTP/1.1
        Accept: */*
        ...
        HTTP/1.1 200 OK
        Cache-Control: no-cache, no-store, max-age=0, must-revalidate
        Content-Type: application/json;charset=UTF-8
        ...
        {
            "claims": {
                "body": {
                    "and": "the",
                    "brown": "fox",
                    "dreamed": "of",
                    "dreams": "you",
                    "hasMotorcycle": true,
                    "iss": "Stormpath",
                    "jumped": "over",
                    "lazy": "dog",
                    "rainbow": "way",
                    "somewhere": "over",
                    "sub": "msilverman",
                    "the": "quick",
                    "up": "high"
                },
                "header": {
                    "alg": "HS256",
                    "calg": "DEF"
                },
                "signature": "3_wc-2skFBbInk0YAQ96yGWwr8r1xVdbHn-uGPTFuFE"
            },
            "status": "SUCCESS"
        }
        ```

        请注意标头中的 calg 声明。它会自动编码到 JWT 中，并为解析器提供有关解压缩时使用哪种算法的提示。

        > 注意：JWE 规范确实支持压缩。在即将发布的 JJWT 库中，我们将支持 JWE 和压缩 JWE。我们还将继续支持其他类型的 JWT 的压缩，即使它没有被指定。

8. Java 开发人员的令牌工具

    虽然本文的核心重点不是 Spring Boot 或 Spring Security，但使用这两种技术可以轻松演示本文讨论的所有功能。我们应该能够构建、启动服务器，并开始使用我们讨论过的各种端点。只需点击

    `http http://localhost:8080`

    1. JJWT

        JJWT 是一款易于使用的工具，可供开发人员在 Java 中创建和验证 JWT。JJWT 完全免费且开源（Apache 许可证，2.0 版），因此每个人都能看到它的功能和使用方法。请随时报告任何问题，提出改进建议，甚至提交一些代码！

    2. jsonwebtoken.io 和 java.jsonwebtoken.io

        [jsonwebtoken.io](http://jsonwebtoken.io/) 是我们为轻松解码 JWT 而创建的开发者工具。jsonwebtoken.io 由 [nJWT](https://github.com/jwtk/njwt) 提供支持，nJWT 是面向 Node.js 开发人员的最简洁的免费开源（Apache 许可，2.0 版）JWT 库。我们还可以在该网站上看到为各种语言生成的代码。该网站本身是开源的，可在[此处](https://github.com/stormpath/jsonwebtoken.io)找到。

        [java.jsonwebtoken.io](http://java.jsonwebtoken.io/) 专门用于 JJWT 库。我们可以在右上角的方框中更改标头和有效载荷，在左上角的方框中查看 JJWT 生成的 JWT，在下方的方框中查看生成器和解析器 Java 代码的示例。网站本身是开源的，可[在此](https://github.com/stormpath/JJWTsite)处找到。

    3. JWT 检查器

        [JWT Inspector](https://www.jwtinspector.io/) 是一个开源的 Chrome 浏览器扩展，允许开发人员直接在浏览器中检查和调试 JWT。JWT Inspector 会发现我们网站上的 JWT（cookie、本地/会话存储和标头中），并使它们可以通过我们的导航栏和 DevTools 面板轻松访问。

9. JWT This Down！

    JWT 为普通令牌增添了一些智能。在 JWT 中加密签名和验证、建立过期时间以及编码其他信息的能力为真正的无状态会话管理奠定了基础。这对扩展应用程序的能力有很大影响。

    在Stormpath，我们将JWT用于OAuth2令牌、CSRF令牌和微服务之间的断言等用途。

    一旦开始使用 JWT，您可能再也不会使用过去的哑令牌了。
