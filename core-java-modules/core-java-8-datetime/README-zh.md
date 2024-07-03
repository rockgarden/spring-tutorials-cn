# Java8+日期和时间API

本模块包含有关 Java 8 中引入的日期和时间 API 的文章。

- [Introduction to the Java 8 Date/Time API](http://www.baeldung.com/java-8-date-time-intro)
- [Migrating to the New Java 8 Date Time API](http://www.baeldung.com/migrating-to-java-8-date-time-api)
- [Get the Current Date and Time in Java](https://www.baeldung.com/current-date-time-and-timestamp-in-java-8)
- [TemporalAdjuster in Java](http://www.baeldung.com/java-temporal-adjuster)
- [ZoneOffset in Java](https://www.baeldung.com/java-zone-offset)
- [Differences Between ZonedDateTime and OffsetDateTime](https://www.baeldung.com/java-zoneddatetime-offsetdatetime)
- [x] [Period and Duration in Java](#java中的周期和持续时间)
- [How to Get the Start and the End of a Day using Java](http://www.baeldung.com/java-day-start-end)
- [Set the Time Zone of a Date in Java](https://www.baeldung.com/java-set-date-time-zone)
- [x] [Comparing Dates in Java](#用java比较日期)
- [[Next -->]](/core-java-modules/core-java-datetime-java8-2)

## 用Java比较日期

1. 简介

    在本教程中，我们将重点介绍如何使用 [Java 8 Date/Time API](https://www.baeldung.com/java-8-date-time-intro) 比较日期。我们将深入探讨检查两个日期是否相等的不同方法以及如何比较日期。

    测试代码参见：Java8DateTimeApiGeneralComparisonsUnitTest.java

2. 比较日期

    在 Java 中表达日期的基本方法是 LocalDate。让我们考虑两个 LocalDate 对象实例，分别代表 2019 年 8 月 10 日和 2019 年 7 月 1 日：

    ```java
    LocalDate firstDate = LocalDate.of(2019, 8, 10);
    LocalDate secondDate = LocalDate.of(2019, 7, 1);
    ```

    我们将使用 isAfter()、isBefore() 和 isEqual() 方法以及 equals() 和 compareTo()，对两个 LocalDate 对象进行比较。

    我们使用 isAfter() 方法检查日期实例是否在另一个指定日期之后。因此，下一个 JUnit 断言将通过：

    `assertThat(firstDate.isAfter(secondDate), is(true));`

    类似地，方法 isBefore() 检查日期实例是否在另一个指定日期之前：

    `assertThat(firstDate.isBefore(secondDate), is(false));`

    isEqual() 方法检查日期是否与另一个指定日期在本地时间轴上代表相同的点：

    `assertThat(firstDate.isEqual(firstDate), is(true));`

    1. 使用可比较接口比较日期

        equals() 方法将给出与 isEqual() 相同的结果，但前提是传递的参数必须是相同类型（本例中为 LocalDate）：

        `assertThat(firstDate.equals(secondDate), is(false));`

        isEqual() 方法可用于与不同类型的对象进行比较，如 JapaneseDate、ThaiBuddhistDate 等。

        我们可以使用比较接口定义的 compareTo() 方法比较两个日期实例：

        `assertThat(firstDate.compareTo(secondDate), is(1));`

3. 比较包含时间成分的日期实例

    本节将解释如何比较两个 LocalDateTime 实例。LocalDateTime 实例包含日期和时间组件。

    与 LocalDate 类似，我们使用 isAfter()、isBefore() 和 isEqual() 方法比较两个 LocalDateTime 实例。此外，equals() 和 compareTo() 的使用方式与 LocalDate 类似。

    同样，我们也可以使用相同的方法来比较两个 ZonedDateTime 实例。让我们比较一下同一天纽约当地时间 8:00 和柏林当地时间 14:00：

    givenZonedDateTimes_whenComparing_thenAssertsPass()

    虽然这两个 ZonedDateTime 实例代表同一时刻，但它们并不代表相同的 Java 对象。它们内部有不同的 LocalDateTime 和 ZoneId 字段。

4. 其他比较

    让我们创建一个简单的实用程序类，进行稍微复杂一些的比较。

    代码参见：DateTimeComparisonUtils.java

    首先，我们将检查 LocalDateTime 和 LocalDate 实例是否在同一天：

    isSameDay(LocalDateTime timestamp, LocalDate localDateToCompare)

    其次，我们将检查两个 LocalDateTime 实例是否在同一天：

    isSameDay(LocalDateTime timestamp, LocalDateTime timestampToCompare)

    truncatedTo(TemporalUnit)方法将日期截断在给定的级别上，在我们的例子中就是一天。

    第三，我们可以实现小时级别的比较：truncatedTo(HOURS)

    最后，我们可以用类似的方法检查两个 ZonedDateTime 实例是否发生在同一小时内：

    isSameHour(ZonedDateTime zonedTimestamp, ZonedDateTime zonedTimestampToCompare)

    我们可以看到，两个 ZonedDateTime 对象实际上发生在同一小时内，即使它们的本地时间不同（分别为 8:30 和 14:00）：

    givenZonedDateTimes_whenIsSameHour_thenCompareTrue()

5. 旧 Java 日期 API 中的比较

    在 Java 8 之前，我们不得不使用 java.util.Date 和 java.util.Calendar 类来操作日期/时间信息。旧版 Java Date API 的设计有很多缺陷，如复杂和线程不安全。java.util.Date 实例代表的是 "instant in time"，而不是真正的日期。

    解决方案之一是使用 Joda Time 库。Java 8 发布后，建议迁移到 Java 8 Date/Time API。

    测试代码参见：LegacyDateComparisonUtilsUnitTest.java

    与 LocalDate 和 LocalDateTime 类似，java.util.Date 和 java.util.Calendar 对象都有 after()、before()、compareTo() 和 equals() 方法，用于比较两个日期实例。日期的比较是以毫秒为单位的时间瞬时进行的：

    givenDates__whenComparing_thenAssertsPass

    对于更复杂的比较，我们可以使用 Apache Commons Lang 库中的 DateUtils。该类包含许多处理日期和日历对象的便捷方法：

    LegacyDateComparisonUtils.java

    isSameDay(Date date, Date dateToCompare)

    isSameHour(Date date, Date dateToCompare)

    要比较来自不同 API 的日期对象，我们应首先进行适当的转换，然后再进行比较。更多详情，请参阅[将日期转换为LocalDate或LocalDateTime及返回教程](https://www.baeldung.com/java-date-to-localdate-and-localdatetime)。

6. 总结

    在本文中，我们探讨了在 Java 中比较日期实例的不同方法。

    Java 8 Date/Time 类具有丰富的 API，可用于比较有时间和时区或无时间和时区的日期。我们还了解了如何以天、小时、分钟等粒度比较日期。

## Java中的周期和持续时间

1. 概述

    在本快速教程中，我们将介绍 Java 8 中引入的两个用于处理日期的新类：Period 和 Duration。

    这两个类都可用于表示时间量或确定两个日期之间的差值。这两个类的主要区别在于，Period 使用基于日期的值，而 Duration 使用基于时间的值。

2. 周期类

    Period 类使用年、月、日单位来表示一段时间。

    测试代码：JavaPeriodUnitTest.java\whenTestPeriod_thenOk()

    通过使用 between() 方法，我们可以获得一个 Period 对象，即两个日期之间的差值。

    然后，我们可以使用 getYears()、getMonths() 和 getDays() 方法确定周期的日期单位。

    在这种情况下，可以使用 isNegative() 方法来判断 endDate 是否大于 startDate。

    如果 isNegative() 返回 false，则表示开始日期早于结束日期值。

    另一种创建 Period 对象的方法是使用专用方法根据天数、月数、周数或年数创建：of()、ofMonths()、ofYears()、ofWeeks()。

    如果只存在其中一个值，例如使用 ofDays() 方法，则其他单位的值为 0。

    在使用 ofWeeks() 方法时，参数值将用于设置天数，将其乘以 7。

    我们还可以通过解析格式为 "PnYnMnD" 的文本序列来创建 Period 对象：

    `Period.parse("P2Y3M5D");`

    使用 plusX() 和 minusX() 形式的方法可以增加或减少周期值，其中 X 代表日期单位：plusDays(50)、minusMonths(2)

3. 持续时间类

    Duration 类表示以秒或纳秒为单位的时间间隔，最适合在需要更高精度的情况下处理较短的时间量。

    测试代码：JavaDurationUnitTest.java\test2()

    我们可以使用 between() 方法以 Duration 对象的形式确定两个时刻之间的差值。

    然后，我们可以使用 getSeconds() 或 getNanoseconds() 方法来确定时间单位的值。

    或者，我们可以从两个 LocalDateTime 实例中获取一个 Duration 实例。

    isNegative() 方法可用于验证结束瞬时是否大于开始瞬时：

    我们还可以使用 ofDays()、ofHours()、ofMillis()、ofMinutes()、ofNanos() 和 ofSeconds() 方法，根据多个时间单位获得 Duration 对象。

    要根据文本序列创建 Duration 对象，该序列必须是 "PnDTnHnMn.nS "形式：

    `Duration.parse("P1DT1H10M10.5S");`

    可以使用 toDays()、toHours()、toMillis()、toMinutes() 将持续时间转换为其他时间单位。

    使用 plusX() 或 minusX() 形式的方法可以增加或减少持续时间值，其中 X 可以代表天、小时、毫秒、分钟、纳秒或秒。

    我们还可以使用 plus() 和 minus() 方法，并为其添加一个参数，指定要添加或减去的时间单位（TemporalUnit）：

    `duration.plus(60,ChronoUnit.SECONDS)`

## Code

文章中提到的所有代码片段，包括其他示例，都可以在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-8-datetime) 上找到。

## Relevant Articles

- [Introduction to the Java 8 Date/Time API](http://www.baeldung.com/java-8-date-time-intro)
- [Migrating to the New Java 8 Date Time API](http://www.baeldung.com/migrating-to-java-8-date-time-api)
- [Get the Current Date and Time in Java](https://www.baeldung.com/current-date-time-and-timestamp-in-java-8)
- [TemporalAdjuster in Java](http://www.baeldung.com/java-temporal-adjuster)
- [ZoneOffset in Java](https://www.baeldung.com/java-zone-offset)
- [Differences Between ZonedDateTime and OffsetDateTime](https://www.baeldung.com/java-zoneddatetime-offsetdatetime)
- [Period and Duration in Java](http://www.baeldung.com/java-period-duration)
- [How to Get the Start and the End of a Day using Java](http://www.baeldung.com/java-day-start-end)
- [Set the Time Zone of a Date in Java](https://www.baeldung.com/java-set-date-time-zone)
- [Comparing Dates in Java](https://www.baeldung.com/java-comparing-dates)
- [[Next -->]](/core-java-modules/core-java-datetime-java8-2)
