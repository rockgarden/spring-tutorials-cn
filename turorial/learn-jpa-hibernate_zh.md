# [学习 JPA 和 Hibernate 系列](https://www.baeldung.com/learn-jpa-hibernate)

最后更新 2023 年 10 月 19 日

JPASeries Hibernate
对象关系映射（ORM）是将 Java 对象转换为数据库表格的过程。换句话说，这使我们能够在不使用任何 SQL 的情况下与关系数据库进行交互。Java Persistence API（JPA）是定义如何在 Java 应用程序中持久化数据的规范。JPA 的主要重点是 ORM 层。

Hibernate 是当今最流行的 Java ORM 框架之一。它的首次发布距今已有近二十年的历史，至今仍拥有良好的社区支持和定期发布。此外，Hibernate 还是 JPA 规范的标准实现，并具有一些 Hibernate 特有的额外功能。让我们来看看 JPA 和 Hibernate 的一些核心功能。

定义实体

- [定义 JPA 实体](/persistence-modules/java-jpa/jpa-entities_zh.md)
- [Hibernate 实体生命周期](/persistence-modules/hibernate5/hibernate-entity-lifecycle_zh.md)
- [JPA实体生命周期事件](/persistence-modules/spring-data-jpa-annotations/jpa-entity-lifecycle-events_zh.md)
- [JPA中的默认列值](/persistence-modules/java-jpa-2/jpa-default-column-values_zh.md)
- [JPA @basic 注解](/persistence-modules/java-jpa/jpa-basic-annotation_zh.md)
- [用JPA将实体类名映射到SQL表名](/persistence-modules/java-jpa-2/jpa-entity-table-names_zh.md)
- [@Size、@Length和@Column(length=value)之间的区别](/persistence-modules/hibernate-mapping/jpa-size-length-column-differences_zh.md)
- [JPA 实体平等](/persistence-modules/java-jpa-3/jpa-entity-equality_zh.md)
- [JPA 的 @Embedded 和 @Embeddable](/persistence-modules/spring-data-jpa-annotations/jpa-embedded-embeddable_zh.md)
- [JPA 属性转换器](/persistence-modules/hibernate-jpa/jpa-attribute-converters_zh.md)
- [Hibernate @NotNull 与 @Column(nullable = false)](/persistence-modules/spring-boot-persistence-h2/hibernate-notnull-vs-nullable_zh.md)
- [在JPA中定义唯一约束](/persistence-modules/java-jpa-3/jpa-unique-constraints_zh.md)
- [JPA实体和可序列化接口](/persistence-modules/hibernate-jpa-2/jpa-entities-serializable_zh.md)
- [Hibernate - 日期和时间映射](/persistence-modules/hibernate-mapping/hibernate-date-time_zh.md)
- [将 Java 记录与 JPA 结合使用](/spring-boot-modules/spring-boot-3/spring-jpa-java-records_zh.md)
- [在 JPA 中持久化枚举](/persistence-modules/java-jpa/jpa-persisting-enums-in-jpa_zh.md)

实体关系

- [JPA 中的一对一关系](/persistence-modules/hibernate-jpa/jpa-one-to-one_zh.md)
- [JPA 中的多对多关系](/persistence-modules/spring-jpa-2/jpa-many-to-many_zh.md)
- [@JoinColumn 注解详解](/persistence-modules/hibernate-annotations/jpa-join-column_zh.md)
- [@JoinColumn 和 mappedBy 的区别](/persistence-modules/hibernate-annotations/jpa-joincolumn-vs-mappedby_zh.md)
- [在 JPA 中将单个实体映射到多个表](/persistence-modules/java-jpa-2/jpa-mapping-single-entity-to-multiple-tables_zh.md)
- [JPA/Hibernate级联类型概述](/persistence-modules/jpa-hibernate-cascade-type/jpa-cascade-types_zh.md)
- [Hibernate @WhereJoinTable 注解](/persistence-modules/hibernate-annotations/hibernate-wherejointable_zh.md)
- [Hibernate继承映射](/persistence-modules/hibernate-mapping/hibernate-inheritance_zh.md)
- [Hibernate一对多注解教程](/persistence-modules/hibernate-annotations/hibernate-one-to-many_zh.md)
- [了解 JPA/Hibernate 关联](/persistence-modules/hibernate-mapping-2/jpa-hibernate-associations_zh.md)

标识符

- [Hibernate/JPA 中的标识符概述](/persistence-modules/hibernate5/hibernate-identifiers_zh.md)
- [JPA 中的复合主键](/persistence-modules/java-jpa/jpa-composite-primary-keys_zh.md)
- [JPA 何时设置主键](/persistence-modules/java-jpa-2/jpa-strategies-when-set-primary-key_zh.md)
- [用 Hibernate 生成 UUID 作为主键](/persistence-modules/hibernate-mapping-2/java-hibernate-uuid-primary-key_zh.md)
- [用JPA返回自动生成的Id](/persistence-modules/java-jpa-3/jpa-get-auto-generated-id_zh.md)

读取操作

- [JPA查询类型](/persistence-modules/java-jpa-2/jpa-queries_zh.md)
- [JPA 查询参数用法](/persistence-modules/java-jpa-2/jpa-query-parameters_zh.md)
- [在不相关实体间构建 JPA 查询](/persistence-modules/java-jpa-2/jpa-query-unrelated-entities_zh.md)
- [在JPA中使用懒元素集合](/persistence-modules/spring-data-jpa-enterprise-2/java-jpa-lazy-collections_zh.md)
- [JPA连接类型](/persistence-modules/spring-data-jpa-query/jpa-join-types_zh.md)
- [Hibernate中的FetchMode](/persistence-modules/hibernate-mapping/hibernate-fetchmode_zh.md)
- [Hibernate命名查询](/persistence-modules/hibernate-queries/hibernate-named-query_zh.md)
- [JPA中的乐观锁定](/persistence-modules/hibernate-jpa/jpa-optimistic-locking_zh.md)
- [JPA中的悲观锁定](/persistence-modules/hibernate-jpa/jpa-pessimistic-locking_zh.md)

查询标准

- [结合 JPA AND/OR 标准谓词](/persistence-modules/java-jpa-2/jpa-and-or-criteria-predicates_zh.md)
- [标准 API - IN 表达式示例](/persistence-modules/hibernate-jpa/jpa-criteria-api-in-expressions_zh.md)
- [JPA 标准查询](/persistence-modules/hibernate-queries/hibernate-criteria-queries_zh.md)

分页和排序

- [使用 JPA 排序](/persistence-modules/spring-jpa/jpa-sort_zh.md)
- [JPA分页](/persistence-modules/spring-jpa/jpa-pagination_zh.md)
- [Hibernate 分页](/persistence-modules/spring-data-jpa-query-2/hibernate-pagination_zh.md)

查询结果

- [SqlResultSetMapping 指南](/persistence-modules/java-jpa/jpa-sql-resultset-mapping_zh.md)
- [使用聚合函数自定义 JPA 查询结果](/persistence-modules/spring-data-jpa-simple/jpa-queries-custom-result-with-aggregation-functions_zh.md)

写操作

- [JPA中的INSERT语句](/persistence-modules/spring-data-jpa-crud/jpa-insert_zh.md)
- [使用 Hibernate/JPA 进行批量插入/更新](/persistence-modules/spring-data-jpa-crud/jpa-hibernate-batch-insert-update_zh.md)
- [用Hibernate删除对象](/persistence-modules/spring-hibernate-5/delete-with-hibernate_zh.md)
- [Hibernate：保存、持久化、更新、合并、保存或更新](/persistence-modules/hibernate-enterprise/hibernate-save-persist-update-merge-saveorupdate_zh.md)
