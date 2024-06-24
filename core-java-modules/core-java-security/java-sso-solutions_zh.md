# [Java应用程序的单点登录解决方案](https://www.baeldung.com/java-sso-solutions)

1. 概述

    使用多个应用程序的组织用户经常需要在多个系统间进行身份验证。因此，用户必须记住多个账户和密码。单点登录（[SSO](https://www.baeldung.com/cs/sso-guide)）技术可以解决这个问题。单点登录为一组系统提供单一登录凭证。

    在本教程中，我们将简要解释什么是单点登录，然后介绍 Java 应用程序的七种不同的单点登录解决方案。

2. 单点登录

    实施单点登录解决方案可以使用以下两种协议中的任何一种：

    - SAML 2.0
    - 开放身份连接

    [SAML 2.0](http://docs.oasis-open.org/security/saml/Post2.0/sstc-saml-tech-overview-2.0.html)（安全断言标记语言）简化了用户身份验证。它允许用户只需在身份提供商处注册和认证，即可访问多种服务。它基于 XML。OpenID Connect（OIDC）是 SAML 2.0 的后续版本。同时，它也是用于身份验证的 OAuth 2.0 协议的扩展。与 SAML 2.0 相比，OIDC 的配置更简单。

3. Keycloak

    [Keycloak](https://www.keycloak.org/) 是一个开源身份和访问管理系统（[IAM](https://www.baeldung.com/cs/iam-security),identity and access management）。它提供 SSO、用户联盟、细粒度授权、[社交登录](https://en.wikipedia.org/wiki/Social_login)、双因素身份验证（[2FA](https://en.wikipedia.org/wiki/Multi-factor_authentication)）等功能。此外，它还支持 OpenID Connect、OAuth 2.0 和 SAML。它与第三方工具集成良好。例如，它与 Spring Boot 应用程序[集成](https://www.baeldung.com/spring-boot-keycloak)得非常好。最新版本可在[这里](https://www.keycloak.org/downloads)找到。此外，它还为管理员和开发人员提供了一个友好的管理控制台，用于配置和管理 Keycloak。源代码可在 [GitHub](https://github.com/keycloak/keycloak) 上获取。

4. WSO2 身份服务器

    [WSO2 Identity Server](https://wso2.com/identity-server/)是WSO2开发的开源IAM系统。它提供 SSO、2FA、身份联盟、社交登录等功能。它还支持几乎所有流行的身份标准。此外，它还提供一个管理控制台，并公开了与其他应用程序集成的 API。不过，它主要是用 Java 编写的，源代码可在 [GitHub](https://github.com/wso2/product-is) 上获取。

5. Gluu

    [Gluu](https://gluu.org/) 是一款开源的云原生 IAM 解决方案，具有多种访问管理功能。它提供强身份验证、移动身份验证、2FA 和身份中介功能。此外，它还支持 OpenID Connect、SAML 2.0、FIDO 和用户管理访问等开放网络标准。它是用 Python 语言编写的。此外，Gluu 服务器的自动部署和配置脚本可在 [GitHub](https://github.com/GluuFederation/community-edition-setup) 上获取。

6. Apereo CAS

    [Apereo CAS](https://www.apereo.org/projects/cas) 是一个开源的企业级 SSO 系统。它也是中央认证服务（CAS）项目的一部分。与之前的解决方案类似，它支持多种协议，如 SAML、OAuth 2.0、OpenID Connect 等。此外，它还能与 uPortal、BlueSocket、TikiWiki、Mule、Liferay、Moodle 等集成。它构建于 Spring Boot 和 Spring Cloud 之上。源代码可在 [GitHub](https://github.com/apereo/cas) 上获取。

7. Spring Security OAuth2

    我们可以使用 [Spring Security OAuth](https://www.baeldung.com/sso-spring-security-oauth2-legacy) 项目来实现 SSO 解决方案。它支持 OAuth 提供者和 OAuth 消费者。此外，我们还可以使用软令牌和 Spring Security 实现 [2FA](https://www.baeldung.com/spring-security-two-factor-authentication-with-soft-token) 功能。

8. 开放访问管理

    [OpenAM](https://www.openidentityplatform.org/openam) 是一个开放访问管理解决方案，包括身份验证、授权、SSO 和身份提供程序。它支持跨域单点登录（CDSSO）、SAML 2.0、OAuth 2.0 和 OpenID Connect。最新版本和源代码可在[这里](https://github.com/OpenIdentityPlatform/OpenAM/)找到。

9. Authelia

    [Authelia](https://www.authelia.com/) 是一款开源身份验证和授权服务器，可提供 SSO 和 2FA 功能。它利用与 [FIDO2](https://en.wikipedia.org/wiki/FIDO2_Project) [Webauthn](https://en.wikipedia.org/wiki/WebAuthn) 兼容的安全密钥提供多种基于硬件的 2FA。此外，它还支持由 Google Authenticator 等应用程序生成的基于时间的一次性密码。Authelia 服务器使用 Go 语言编写，其全部源代码可在 [GitHub](https://github.com/authelia/authelia) 上获取。

10. 总结

    如今，许多组织都在使用 SSO。在本文中，我们对 Java 生态系统中的 SSO 解决方案做了一个非常高层次的介绍。其中一些解决方案提供了完整的 IAM，而另一些则只提供了 SSO 服务器和身份验证方法。

    我刚刚发布了新的 Learn Spring Security 课程，其中包括侧重于 Spring Security 中新 OAuth2 协议栈的全部资料。
