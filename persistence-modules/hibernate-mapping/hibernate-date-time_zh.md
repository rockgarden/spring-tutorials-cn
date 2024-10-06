# [Hibernate-Mapping日期和时间](https://www.baeldung.com/hibernate-date-time)

1. 一览表

    在本教程中，我们将展示如何在Hibernate中映射时间列值，包括来自java.sql、java.util和java.time软件包的类。

2. 项目设置

    为了演示时间类型的映射，我们需要H2数据库和最新版本的休眠核心库：

    ```xml
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core.orm</artifactId>
        <version>6.4.2.Final</version>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.1.214</version>
    </dependency>
    ```

    有关休眠核心库的当前版本，请前往Maven Central存储库。

3. 时区设置

    在处理日期时，最好为JDBC驱动程序设置一个特定的时区。这样，我们的应用程序将独立于系统的当前时区。

    例如，我们将按会话进行设置：

    ```java
    session = HibernateUtil.getSessionFactory().withOptions()
    .jdbcTimeZone(TimeZone.getTimeZone("UTC"))
    .openSession();
    ```

    另一种方法是在用于构建会话工厂的Hibernate属性文件中设置hibernate.jdbc.time_zone属性。通过这种方式，我们可以为整个应用程序指定一次时区。

4. 映射java.sql类型

    java.sql软件包包含与SQL标准定义的类型一致的JDBC类型：

    - 日期对应于DATE SQL类型，它只是一个没有时间的日期。
    - 时间对应于TIME SQL类型，这是以小时、分钟和秒为单位的一天中的时间。
    - 时间戳包含有关日期和时间的信息，精度高达纳秒，并对应于TIMESTAMP SQL类型。

    这些类型与SQL一致，因此它们的映射相对简单。

    我们可以使用@Basic或@Column注释：

    ```java
    @Entity
    public class TemporalValues {

        @Basic
        private java.sql.Date sqlDate;

        @Basic
        private java.sql.Time sqlTime;

        @Basic
        private java.sql.Timestamp sqlTimestamp;

    }
    ```

    然后我们可以设置相应的值：

    ```java
    temporalValues.setSqlDate(java.sql.Date.valueOf("2017-11-15"));
    temporalValues.setSqlTime(java.sql.Time.valueOf("15:30:14"));
    temporalValues.setSqlTimestamp(
    java.sql.Timestamp.valueOf("2017-11-15 15:30:14.332"));
    ```

    请注意，为实体字段选择java.sql类型可能并不总是一个好的选择。这些类是JDBC特定的，包含许多弃用的功能。

5. 映射java.util.Date类型

    类型java.util.Date包含日期和时间信息，精度高达毫秒。但它与任何SQL类型都没有直接关系。

    这就是为什么我们需要另一个注释来指定所需的SQL类型：

    ```java
    @Basic
    @Temporal(TemporalType.DATE)
    private java.util.Date utilDate;

    @Basic
    @Temporal(TemporalType.TIME)
    private java.util.Date utilTime;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date utilTimestamp;
    ```

    @Temporal注释具有类型TemporalType的单个参数值。它可以是日期、时间或时间戳，这取决于我们想要用于映射的底层SQL类型。

    然后我们可以设置相应的字段：

    ```java
    temporalValues.setUtilDate(
    new SimpleDateFormat("yyyy-MM-dd").parse("2017-11-15"));
    temporalValues.setUtilTime(
    new SimpleDateFormat("HH:mm:ss").parse("15:30:14"));
    temporalValues.setUtilTimestamp(
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        .parse("2017-11-15 15:30:14.332"));
    ```

    正如我们所看到的，java.util.Date类型（毫秒精度）不够精确，无法处理时间戳值（纳秒精度）。

    因此，当我们从数据库中检索实体时，我们会毫不奇怪地在这个字段中找到一个java.sql.Timestamp实例，即使我们最初坚持使用java.util.Date：

    ```java
    temporalValues = session.get(TemporalValues.class, 
    temporalValues.getId());
    assertThat(temporalValues.getUtilTimestamp())
    .isEqualTo(java.sql.Timestamp.valueOf("2017-11-15 15:30:14.332"));
    ```

    这对于我们的代码来说应该没问题，因为时间戳延长了日期。

6. 映射java.util.日历类型

    与java.util.Date一样，java.util.Calendar类型可以映射到不同的SQL类型，因此我们必须使用@Temporal指定它们。

    唯一的区别是Hibernate不支持将日历映射到时间：

    ```java
    @Basic
    @Temporal(TemporalType.DATE)
    private java.util.Calendar calendarDate;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Calendar calendarTimestamp;
    拷贝
    以下是我们如何设置字段的值：

    Calendar calendarDate = Calendar.getInstance(
    TimeZone.getTimeZone("UTC"));
    calendarDate.set(Calendar.YEAR, 2017);
    calendarDate.set(Calendar.MONTH, 10);
    calendarDate.set(Calendar.DAY_OF_MONTH, 15);
    temporalValues.setCalendarDate(calendarDate);
    ```

7. 映射java.time类型

    自Java 8以来，新的Java日期和时间API可用于处理时间值。此API修复了java.util.Date和java.util.Calendar类的许多问题。

    来自java.time软件包的类型直接映射到相应的SQL类型。

    因此，无需明确指定@Temporal注释：

    - LocalDate映射到DATE。
    - LocalTime和OffsetTime映射到TIME。
    - Instant、LocalDateTime、OffsetDateTime和ZonedDateTime映射到TIMESTAMP。

    这意味着我们只能用@Basic（或@Column）注释标记这些字段：

    ```java
    @Basic
    private java.time.LocalDate localDate;

    @Basic
    private java.time.LocalTime localTimeField;

    @Basic
    private java.time.OffsetTime offsetTime;

    @Basic
    private java.time.Instant instant;

    @Basic
    private java.time.LocalDateTime localDateTime;

    @Basic
    private java.time.OffsetDateTime offsetDateTime;

    @Basic
    private java.time.ZonedDateTime zonedDateTime;
    ```

    java.time软件包中的每个时间类都有一个静态解析（）方法，用于使用适当的格式解析提供的字符串值。

    因此，以下是我们如何设置实体字段的值：

    ```java
    temporalValues.setLocalDate(LocalDate.parse("2017-11-15"));

    temporalValues.setLocalTime(LocalTime.parse("15:30:18"));
    temporalValues.setOffsetTime(OffsetTime.parse("08:22:12+01:00"));

    temporalValues.setInstant(Instant.parse("2017-11-15T08:22:12Z"));
    temporalValues.setLocalDateTime(
    LocalDateTime.parse("2017-11-15T08:22:12"));
    temporalValues.setOffsetDateTime(
    OffsetDateTime.parse("2017-11-15T08:22:12+01:00"));
    temporalValues.setZonedDateTime(
    ZonedDateTime.parse("2017-11-15T08:22:12+01:00[Europe/Paris]"));
    ```

8. 结论

    在本文中，我们展示了如何在Hibernate中映射不同类型的时间值。
