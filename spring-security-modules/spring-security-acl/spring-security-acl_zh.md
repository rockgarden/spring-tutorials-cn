# [Spring 安全 ACL 简介](https://www.baeldung.com/spring-security-acl)

1. 简介

    访问控制列表（ACL）是附加到对象上的权限列表。ACL 规定了哪些身份可对给定对象进行哪些操作。

    Spring Security 访问控制列表是一个支持域对象安全的 Spring 组件。简单地说，Spring ACL 可帮助定义特定用户/角色在单个域对象上的权限，而不是在典型的每个操作级别上的全面权限。

    例如，具有管理员角色的用户可以查看（READ）和编辑（WRITE）中央公告栏上的所有消息，但普通用户只能查看消息、与消息相关但不能编辑。同时，其他具有编辑器角色的用户可以查看和编辑某些特定的信息。

    因此，不同的用户/角色对每个特定对象拥有不同的权限。在这种情况下，Spring ACL 就能完成任务。本文将探讨如何使用 Spring ACL 设置基本的权限检查。

2. 配置

    1. ACL 数据库

        要使用 Spring Security ACL，我们需要在数据库中创建四个必选表。

        第一个表是 ACL_CLASS，用于存储域对象的类名，列包括

        - ID
        - CLASS：安全域对象的类名，例如：com.baeldung.acl.persistence.entity.NoticeMessage

        其次，我们需要 ACL_SID 表，该表允许我们普遍识别系统中的任何原则或权限。该表需要

        - ID
        - SID：即用户名或角色名。SID 代表安全标识
        - PRINCIPAL：0 或 1，表示相应的 SID 是负责人（用户，如 mary、mike、jack......）或授权（角色，如 ROLE_ADMIN、ROLE_USER、ROLE_EDITOR......）。

        下一个表是 ACL_OBJECT_IDENTITY，它存储每个唯一域对象的信息：

        - ID
        - OBJECT_ID_CLASS：定义域对象类别，链接到 ACL_CLASS 表
        - OBJECT_ID_IDENTITY：域对象可根据类别存储在多个表中。因此，该字段存储目标对象主键
        - PARENT_OBJECT：指定此对象标识符在此表中的父节点
        - OWNER_SID：对象所有者的 ID，链接至 ACL_SID 表
        - ENTRIES_INHERITING：该对象的 ACL 条目是否继承自父对象（ACL 条目在 ACL_ENTRY 表中定义）。

        最后，ACL_ENTRY 存储了分配给对象标识上每个 SID 的单独权限：

        - ID
        - ACL_OBJECT_IDENTITY：指定对象标识，链接到 ACL_OBJECT_IDENTITY 表
        - ACE_ORDER：当前条目在相应对象标识的 ACL 条目列表中的顺序
        - SID：授予或拒绝权限的目标 SID，链接到 ACL_SID 表
        - MASK：整数位掩码，表示实际授予或拒绝的权限
        - GRANTING：值 1 表示授予，值 0 表示拒绝
        - AUDIT_SUCCESS（成功）和 AUDIT_FAILURE（失败）：用于审计。

    2. 依赖性

        要在项目中使用 Spring ACL，首先要定义依赖关系：

        ```xml
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-acl</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
        </dependency>
        ```

        Spring ACL 需要缓存来存储对象标识和 ACL 条目，因此我们将使用 spring-context 提供的 ConcurrentMapCache。

        在不使用 Spring Boot 的情况下，我们需要明确添加版本。这些版本可在 Maven Central 上查看：spring-security-acl、spring-security-config、spring-context-support。

    3. ACL 相关配置

        我们需要通过启用全局方法安全性来保护所有返回安全域对象或更改对象的方法：

        main/.acl.config/AclMethodSecurityConfiguration.java

        我们还可以通过将 prePostEnabled 设置为 true 来启用基于表达式的访问控制，从而使用 Spring Expression Language (SpEL)。此外，我们还需要一个支持 ACL 的表达式处理程序：

        ```java
        @Bean
        public MethodSecurityExpressionHandler defaultMethodSecurityExpressionHandler() {
            DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
            AclPermissionEvaluator permissionEvaluator = new AclPermissionEvaluator(aclService());
            expressionHandler.setPermissionEvaluator(permissionEvaluator);
            return expressionHandler;
        }
        ```

        因此，我们将 AclPermissionEvaluator 分配给 DefaultMethodSecurityExpressionHandler。评估器需要一个 MutableAclService 来从数据库加载权限设置和域对象的定义。

        为简单起见，我们使用提供的 JdbcMutableAclService：

        ```java
        @Bean
        public JdbcMutableAclService aclService() { 
            return new JdbcMutableAclService(dataSource, lookupStrategy(), aclCache()); 
        }
        ```

        正如它的名字一样，JdbcMutableAclService 使用 JDBCTemplate 来简化数据库访问。它需要一个 DataSource（用于 JDBCTemplate）、LookupStrategy（在查询数据库时提供优化的查找）和 AclCache（缓存 ACL 条目和对象标识）。

        同样，为了简单起见，我们使用提供的 BasicLookupStrategy 和 EhCacheBasedAclCache。

        ```java
        @Autowired
        DataSource dataSource;

        @Bean
        public AclAuthorizationStrategy aclAuthorizationStrategy() {
            return new AclAuthorizationStrategyImpl(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        @Bean
        public PermissionGrantingStrategy permissionGrantingStrategy() {
            return new DefaultPermissionGrantingStrategy(new ConsoleAuditLogger());
        }

        @Bean
        public SpringCacheBasedAclCache aclCache() {
            final ConcurrentMapCache aclCache = new ConcurrentMapCache("acl_cache");
            return new SpringCacheBasedAclCache(aclCache, permissionGrantingStrategy(), aclAuthorizationStrategy());
        }

        @Bean 
        public LookupStrategy lookupStrategy() { 
            return new BasicLookupStrategy(
            dataSource, 
            aclCache(), 
            aclAuthorizationStrategy(), 
            new ConsoleAuditLogger()
            ); 
        }
        ```

        在这里，AclAuthorizationStrategy 负责断定当前用户是否拥有某些对象的所有必要权限。

        它需要 PermissionGrantingStrategy 的支持，后者定义了确定是否向特定 SID 授予权限的逻辑。

3. 使用 Spring ACL 实现方法安全

    到目前为止，我们已经完成了所有必要的配置。现在，我们可以为我们的安全方法设置所需的检查规则。

    默认情况下，Spring ACL 会引用 BasePermission 类来获取所有可用权限。基本上，我们有 READ, WRITE, CREATE, DELETE 和 ADMINISTRATION 权限。

    让我们尝试定义一些安全规则：

    ```java
    @PostFilter("hasPermission(filterObject, 'READ')")
    List<NoticeMessage> findAll();
        
    @PostAuthorize("hasPermission(returnObject, 'READ')")
    NoticeMessage findById(Integer id);
        
    @PreAuthorize("hasPermission(#noticeMessage, 'WRITE')")
    NoticeMessage save(@Param("noticeMessage")NoticeMessage noticeMessage);
    ```

    执行 findAll() 方法后，将触发 @PostFilter 方法。所需规则 `hasPermission(filterObject,'READ')` 表示只返回当前用户有 READ 权限的通知消息。

    同样，在执行 findById() 方法后会触发 `@PostAuthorize`，确保只返回当前用户有 READ 权限的 NoticeMessage 对象。否则，系统将抛出 AccessDeniedException。

    另一方面，系统会在调用 save() 方法前触发 `@PreAuthorize` 注解。它将决定是否允许执行相应的方法。如果不允许，系统将抛出 AccessDeniedException。

4. 执行中

    现在，我们将使用 JUnit 测试所有这些配置。我们将使用 H2 数据库来尽可能简化配置。

    我们需要添加

    ```xml
    <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    </dependency>

    <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <scope>test</scope>
    </dependency>

    <dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
    </dependency>
    ```

    1. 场景

        在此方案中，我们将有两个用户（manager、hr）和一个用户角色（ROLE_EDITOR），因此我们的 acl_sid 将如下所示：

        ```sql
        INSERT INTO acl_sid (principal, sid) VALUES
        (1, 'manager'),
        (1, 'hr'),
        (0, 'ROLE_EDITOR');
        ```

        然后，我们需要在 acl_class 中声明 NoticeMessage 类。然后，我们需要在 acl_class 中声明 NoticeMessage 类，并在 system_message 中插入三个 NoticeMessage 类实例。

        此外，还必须在 acl_object_identity 中声明这 3 个实例的对应记录：

        ```sql
        INSERT INTO acl_class (id, class) VALUES
        (1, 'com.baeldung.acl.persistence.entity.NoticeMessage');

        INSERT INTO system_message(id,content) VALUES 
        (1,'First Level Message'),
        (2,'Second Level Message'),
        (3,'Third Level Message');

        INSERT INTO acl_object_identity 
        (object_id_class, object_id_identity, 
        parent_object, owner_sid, entries_inheriting) 
        VALUES
        (1, 1, NULL, 3, 0),
        (1, 2, NULL, 3, 0),
        (1, 3, NULL, 3, 0);
        ```

        最初，我们将第一个对象（id =1）的 READ 和 WRITE 权限授予用户管理器。同时，任何拥有 ROLE_EDITOR 的用户都将拥有所有三个对象的 READ 权限，但只拥有第三个对象（id=3）的 WRITE 权限。此外，用户 hr 只有第二个对象的 READ 权限。

        在这里，由于我们使用默认的 Spring ACL BasePermission 类进行权限检查，因此 READ 权限的掩码值将为 1，WRITE 权限的掩码值将为 2。我们在 acl_entry 中的数据将是

        ```java
        INSERT INTO acl_entry 
        (acl_object_identity, ace_order, 
        sid, mask, granting, audit_success, audit_failure) 
        VALUES
        (1, 1, 1, 1, 1, 1, 1),
        (1, 2, 1, 2, 1, 1, 1),
        (1, 3, 3, 1, 1, 1, 1),
        (2, 1, 2, 1, 1, 1, 1),
        (2, 2, 3, 1, 1, 1, 1),
        (3, 1, 3, 1, 1, 1, 1),
        (3, 2, 3, 2, 1, 1, 1);
        ```

    2. 测试用例

        首先，我们尝试调用 findAll 方法。

        根据我们的配置，该方法只返回用户有 READ 权限的 NoticeMessage。

        因此，我们希望结果列表只包含第一条信息：

        test/.acl/SpringACLIntegrationTest.java:givenUserManager_whenFindAllMessage_thenReturnFirstMessage()

        然后，我们尝试调用任何角色为 ROLE_EDITOR 的用户的相同方法。请注意，在这种情况下，这些用户拥有对所有三个对象的 READ 权限。

        因此，我们预计结果列表将包含所有三条信息：

        test/.acl/SpringACLIntegrationTest.java:givenRoleEditor_whenFindAllMessage_thenReturn3Message()

        接下来，我们将使用管理器用户，尝试通过 ID 获取第一条消息并更新其内容--这一切应该都能顺利进行：

        test/.acl/SpringACLIntegrationTest.java:givenUserManager_whenFind1stMessageByIdAndUpdateItsContent_thenOK()

        但如果任何具有 ROLE_EDITOR 角色的用户更新了第一条信息的内容，我们的系统就会抛出 AccessDeniedException：

        test/.acl/SpringACLIntegrationTest.java:givenRoleEditor_whenFind1stMessageByIdAndUpdateContent_thenFail()

        同样，hr 用户可以通过 id 找到第二条信息，但更新失败：

        test/.acl/SpringACLIntegrationTest.java:givenUsernameHr_whenFindMessageById2_thenOK()

        test/.acl/SpringACLIntegrationTest.java:givenUsernameHr_whenUpdateMessageWithId2_thenFail()

5. 结论

    本文介绍了 Spring ACL 的基本配置和用法。

    我们知道，Spring ACL 需要特定的表来管理对象、原则/权限和权限设置。与这些表的所有交互，尤其是更新操作，都必须通过 AclService 进行。我们将在以后的文章中探讨该服务的基本 CRUD 操作。

    默认情况下，我们只能使用 BasePermission 类中预定义的权限。
