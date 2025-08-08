# [SQL注入及其防范方法？](https://www.baeldung.com/sql-injection)

持久化  安全

定义    SQL

1. 引言
    尽管是最广为人知的漏洞之一，SQL 注入仍然位居臭名昭著的 OWASP Top 10 榜单榜首——现在属于更广泛的注入类别。
    在本教程中，我们将探讨 Java 中导致应用程序易受攻击的常见编码错误，以及如何使用 JVM 标准运行库中可用的 API 来避免这些问题。我们还将介绍使用 JPA、Hibernate 等 ORM 能获得哪些保护，以及我们仍需关注的盲点。

2. 应用程序如何变得容易受到 SQL 注入攻击？
    注入攻击之所以有效，是因为对于许多应用程序来说，执行特定计算的唯一方法是动态生成代码，然后由另一个系统或组件运行。如果在生成此代码的过程中，我们使用未经适当净化的不可信数据，就会为黑客留下可利用的漏洞。
    这种说法听起来可能有点抽象，让我们通过一个教科书式的例子来看看这种情况在实践中是如何发生的：

    ```java
    public List<AccountDTO>
    unsafeFindAccountsByCustomerId(String customerId)
    throws SQLException {
        // UNSAFE !!! DON'T DO THIS !!!
        String sql = "select "
        + "customer_id,acc_number,branch_id,balance "
        + "from Accounts where customer_id = '"
        + customerId 
        + "'";
        Connection c = dataSource.getConnection();
        ResultSet rs = c.createStatement().executeQuery(sql);
        // ...
    }
    ```

    这段代码的问题显而易见：我们将 customerId 的值未经任何验证就放入了查询中。如果我们可以确定该值仅来自可信来源，那么不会有任何问题，但我们能确定吗？
    让我们想象一下，这个函数在账户资源的 REST API 实现中使用。利用这段代码是轻而易举的：我们只需要发送一个值，当它与查询的固定部分连接时，会改变其预期行为：

    ```bash
    curl -X GET \
    '<http://localhost:8080/accounts?customerId=abc%27%20or%20%271%27=%271>' \
    ```

    假设 customerId 参数值在到达我们的函数之前未经检查，我们将收到：`abc' or '1' = '1`

    当我们把这个值与固定部分连接时，得到的最终 SQL 语句将被执行：

    ```sql
    select customer_id, acc_number,branch_id, balance
    from Accounts where customerId = 'abc' or '1' = '1'
    ```

    可能不是我们想要的结果……

    一个聪明的开发者（我们不都是吗？）现在可能会想：“这太愚蠢了！我绝不会用字符串连接来构建这样的查询。”
    别太自信……这个经典例子确实很愚蠢，但在某些情况下，我们可能仍然需要这样做：

    - 具有动态搜索条件的复杂查询：根据用户提供的条件添加 UNION 子句
    - 动态分组或排序：用作 GUI 数据表后端的 REST API

    1. 我在使用 JPA。我安全了，对吧？
        这是一个常见的误解。JPA 和其他 ORM 免除了我们手动编写 SQL 语句的麻烦，但它们并不能防止我们编写易受攻击的代码。
        让我们看看前面例子的 JPA 版本：

        ```java
        public List<AccountDTO> unsafeJpaFindAccountsByCustomerId(String customerId) {
            String jql = "from Account where customerId = '" + customerId + "'";
            TypedQuery<Account> q = em.createQuery(jql, Account.class);
            return q.getResultList()
                .stream()
                .map(this::toAccountDTO)
                .collect(Collectors.toList());
        }
        ```

        我们之前指出的问题在这里同样存在：我们使用未经验证的输入来创建 JPA 查询，因此我们在这里也暴露在同样的利用风险中。
3. 防范技术
    现在我们知道了什么是 SQL 注入，让我们看看如何保护我们的代码免受这种攻击。在这里，我们专注于 Java 和其他 JVM 语言中可用的几种非常有效的技术，但类似的概念也适用于 PHP、.Net、Ruby 等其他环境。
    对于那些寻找完整技术列表（包括特定于数据库的技术）的人，OWASP 项目维护着一份 [SQL 注入防范速查表](https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html)，这是了解更多相关内容的好地方。

    1. 参数化查询
        这种技术包括在需要插入用户提供的值时，在查询中使用带问号占位符（“?”）的预处理语句。这种方法非常有效，除非 JDBC 驱动程序的实现有 bug，否则不会被利用。
        让我们重写我们的示例函数来使用这种技术：

        ```java
        public List<AccountDTO> safeFindAccountsByCustomerId(String customerId)
        throws Exception {
            String sql = "select "
            + "customer_id, acc_number, branch_id, balance from Accounts"
            + "where customer_id = ?";
            Connection c = dataSource.getConnection();
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1, customerId);
            ResultSet rs = p.executeQuery(sql));
            // 省略 - 处理行并返回账户列表
        }
        ```

        在这里，我们使用了 Connection 实例中可用的 prepareStatement() 方法来获取一个 PreparedStatement。此接口通过扩展常规的 Statement 接口，提供了几种方法，允许我们在执行查询之前安全地插入用户提供的值。
        对于 JPA，我们有类似的功能：

        ```java
        String jql = "from Account where customerId = :customerId";
        TypedQuery<Account> q = em.createQuery(jql, Account.class)
        .setParameter("customerId", customerId);
        // 执行查询并返回映射结果（省略）
        ```

        在 Spring Boot 下运行此代码时，我们可以将属性 logging.level.sql 设置为 DEBUG，以查看实际构建的查询以执行此操作：

        ```log
        // 注意：输出已格式化以适应屏幕
        [DEBUG][SQL] select
            account0_.id as id1_0_,
            account0_.acc_number as acc_numb2_0_,
            account0_.balance as balance3_0_,
            account0_.branch_id as branch_i4_0_,
            account0_.customer_id as customer5_0_
        from accounts account0_
        where account0_.customer_id=?
        ```

        正如预期的那样，ORM 层创建了一个预处理语句，使用占位符来代替 customerId 参数。这与我们在纯 JDBC 情况下所做的相同——但少了几行代码，这很好。
        作为额外的好处，这种方法通常会导致性能更好的查询，因为大多数数据库可以缓存与预处理语句相关的查询计划。
        请注意，这种方法仅对用作值的占位符有效。例如，我们不能使用占位符来动态更改表名：

        ```java
        // 这将不起作用！！！
        PreparedStatement p = c.prepareStatement("select count(*) from ?");
        p.setString(1, tableName);
        ```

        在这里，JPA 也无能为力：

        ```java
        // 这同样不起作用！！！
        String jql = "select count(*) from :tableName";
        TypedQuery q = em.createQuery(jql,Long.class)
            .setParameter("tableName", tableName);
        return q.getSingleResult();
        ```

        在两种情况下，我们都会得到一个运行时错误。
        其主要原因在于预处理语句的本质：数据库服务器使用它们来缓存获取结果集所需的查询计划，这对于任何可能的值通常都是相同的。这对于表名和 SQL 语言中的其他结构（如 order by 子句中使用的列）来说是不成立的。

    2. JPA Criteria API
        由于显式 JQL 查询构建是 SQL 注入的主要来源，我们应该尽可能优先使用 JPA 的查询 API。
        有关此 API 的快速入门，请参考关于 Hibernate Criteria 查询的[文章](https://www.baeldung.com/hibernate-criteria-queries)。同样值得阅读的是我们关于 JPA 元模型的[文章](https://www.baeldung.com/hibernate-criteria-queries-metamodel)，它展示了如何生成元模型类，帮助我们摆脱用于列名的字符串常量——以及当它们改变时出现的运行时错误。
        让我们重写我们的 JPA 查询方法来使用 Criteria API：

        ```java
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);
        cq.select(root).where(cb.equal(root.get(Account_.customerId), customerId));
        TypedQuery<Account> q = em.createQuery(cq);
        // 执行查询并返回映射结果（省略）
        ```

        在这里，我们使用了更多的代码行来获得相同的结果，但好处是我们现在不必担心 JQL 语法。
        另一个重要的一点：尽管它很冗长，但 Criteria API 使得创建复杂的查询服务更加直接和安全。有关在实践中如何操作的完整示例，请参考 [JHipster](https://www.jhipster.tech/) 生成的应用程序所采用的方法。

    3. 用户数据净化
        数据净化是一种对用户提供的数据应用过滤器的技术，以便应用程序的其他部分可以安全地使用它。过滤器的实现可能大不相同，但通常我们可以将其分为两类：白名单和黑名单。
        黑名单，即试图识别无效模式的过滤器，通常在防止 SQL 注入的背景下价值不大——但不适用于检测！稍后会详细介绍。
        另一方面，当我们能够明确定义什么是有效输入时，白名单特别有效。
        让我们增强我们的 safeFindAccountsByCustomerId 方法，以便调用者还可以指定用于对结果集进行排序的列。由于我们知道可能的列集，我们可以使用一个简单的集合来实现白名单，并用它来净化接收到的参数：

        ```java
        private static final Set<String> VALID_COLUMNS_FOR_ORDER_BY
        = Collections.unmodifiableSet(Stream
            .of("acc_number","branch_id","balance")
            .collect(Collectors.toCollection(HashSet::new)));
        public List<AccountDTO> safeFindAccountsByCustomerId(
        String customerId,
        String orderBy) throws Exception {
            String sql = "select "
            + "customer_id,acc_number,branch_id,balance from Accounts"
            + "where customer_id = ? ";
            if (VALID_COLUMNS_FOR_ORDER_BY.contains(orderBy)) {
                sql = sql + " order by " + orderBy;
            } else {
                throw new IllegalArgumentException("想得美！");
            }
            Connection c = dataSource.getConnection();
            PreparedStatement p = c.prepareStatement(sql);
            p.setString(1,customerId);
            // ... 结果集处理省略
        }
        ```

        在这里，我们结合了预处理语句方法和一个用于净化 orderBy 参数的白名单。最终结果是一个包含最终 SQL 语句的安全字符串。在这个简单的例子中，我们使用了一个静态集合，但我们也可以使用数据库元数据函数来创建它。
        我们也可以对 JPA 使用相同的方法，同时利用 Criteria API 和元数据来避免在代码中使用字符串常量：

        ```log
        // 用于排序的有效 JPA 列的映射
        final Map<String,SingularAttribute<Account,?>> VALID_JPA_COLUMNS_FOR_ORDER_BY = Stream.of(
        new AbstractMap.SimpleEntry<>(Account_.ACC_NUMBER, Account_.accNumber),
        new AbstractMap.SimpleEntry<>(Account_.BRANCH_ID, Account_.branchId),
        new AbstractMap.SimpleEntry<>(Account_.BALANCE, Account_.balance))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        SingularAttribute<Account,?> orderByAttribute = VALID_JPA_COLUMNS_FOR_ORDER_BY.get(orderBy);
        if (orderByAttribute == null) {
            throw new IllegalArgumentException("想得美！");
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> root = cq.from(Account.class);
        cq.select(root)
        .where(cb.equal(root.get(Account_.customerId), customerId))
        .orderBy(cb.asc(root.get(orderByAttribute)));
        TypedQuery<Account> q = em.createQuery(cq);
        // 执行查询并返回映射结果（省略）
        ```

        这段代码与纯 JDBC 中的基本结构相同。首先，我们使用白名单来净化列名，然后继续创建 CriteriaQuery 从数据库中获取记录。

    4. 我们现在安全了吗？
        假设我们已经在所有地方使用了参数化查询和/或白名单。我们现在可以去找我们的经理，保证我们是安全的吗？
        嗯……别太着急。甚至不用考虑[图灵的停机问题](https://en.wikipedia.org/wiki/Halting_problem)，我们还必须考虑其他方面：
        - 存储过程：这些也容易受到 SQL 注入问题的影响；尽可能对将通过预处理语句发送到数据库的值进行净化
        - 触发器：与过程调用有同样的问题，但甚至更阴险，因为有时我们根本不知道它们的存在……
        - 不安全的直接对象引用：即使我们的应用程序没有 SQL 注入，与此漏洞类别相关的风险仍然存在——这里的关键点是攻击者可以通过不同方式欺骗应用程序，使其返回他或她本不应该有权访问的记录——OWASP 的 GitHub [仓库](https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/Insecure_Direct_Object_Reference_Prevention_Cheat_Sheet.md)上有关于此主题的优秀速查表
        简而言之，我们最好的选择是谨慎。如今，许多组织使用“红队”正是为了这个目的。让他们完成他们的工作，这正是为了发现我们代码中任何剩余的漏洞。

4. 损害控制技术
    作为一种良好的安全实践，我们应该始终实施多层防御——即所谓的纵深防御概念。主要思想是，即使我们无法找到代码中所有可能的漏洞——在处理遗留系统时的常见情况——我们也应该至少尝试限制攻击造成的损害。
    当然，这将是一个完整文章甚至一本书的主题，但让我们列举一些措施：
    - 应用最小权限原则：尽可能限制用于访问数据库的帐户的权限
    - 使用数据库特定的方法来增加额外的保护层；例如，H2 数据库有一个会话级选项，可以禁用 SQL 查询中的所有字面值
    - 使用短期有效的凭据：经常轮换应用程序的数据库凭据；实现这一点的一个好方法是使用 Spring Cloud Vault
    - 记录一切：如果应用程序存储客户数据，这是必须的；有许多解决方案可以直接集成到数据库或作为代理工作，以便在发生攻击时至少可以评估损害
    - 使用 WAF 或类似的入侵检测解决方案：这些通常是典型的黑名单示例——通常，它们带有大量已知攻击特征的数据库，并在检测到时触发可编程操作。一些还包括 JVM 内代理，通过应用一些检测技术来检测入侵——这种方法的主要优势是潜在的漏洞更容易修复，因为我们将获得完整的堆栈跟踪。

5. 结论
    在本文中，我们涵盖了 Java 应用程序中的 SQL 注入漏洞——这对任何依赖数据开展业务的组织来说都是一个非常严重的威胁——以及如何使用简单技术来防止它们。
