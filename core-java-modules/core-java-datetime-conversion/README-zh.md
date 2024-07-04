# Java日期/时间转换烹饪书和示例

本模块包含有关 Java 日期和时间对象之间转换的文章。

- 在 LocalDate 和 XMLGregorianCalendar 之间转换](https://www.baeldung.com/java-localdate-to-xmlgregoriancalendar)
- 在 Java 中将时间转换为毫秒](https://www.baeldung.com/java-time-milliseconds)
- [将日期转换为 LocalDate 或 LocalDateTime 并返回](#将日期转换为localdate或localdatetime并返回)
- 在 java.time.Instant 和 java.sql.Timestamp 之间转换](https://www.baeldung.com/java-time-instant-to-java-sql-timestamp)
- 在 LocalDateTime 和 ZonedDateTime 之间转换](https://www.baeldung.com/java-localdatetime-zoneddatetime)
- 在 Java 中将 12 小时时间转换为 24 小时时间](https://www.baeldung.com/java-convert-time-format)
- 将纪元时间转换为 LocalDate 和 LocalDateTime](https://www.baeldung.com/java-convert-epoch-localdate)

## 将日期转换为LocalDate或LocalDateTime并返回

1. 概述

    从 Java 8 开始，我们有了新的日期 API：java.time。

    不过，有时我们仍需要在新旧 API 之间执行转换，并使用这两种 API 中的日期表示。

2. 将 java.util.Date 转换为 java.time.LocalDate

    代码：DateToLocalDateConverter.java

    让我们从将旧的日期表示转换为新的日期表示开始。

    在这里，我们可以利用 Java 8 中添加到 java.util.Date 中的新 toInstant() 方法。

    在转换 Instant 对象时，我们需要使用 ZoneId，因为 Instant 对象与时区无关--只是时间轴上的点。

    Instant 对象的 atZone(ZoneId zone) API 返回一个 ZonedDateTime，因此我们只需使用 toLocalDate() 方法从中提取 LocalDate。

    首先，我们使用系统默认的 ZoneId：

    convertToLocalDateViaInstant(Date dateToConvert)

    还有一个类似的解决方案，但创建 Instant 对象的方法不同--使用 ofEpochMilli() 方法：

    convertToLocalDateViaMilisecond(Date dateToConvert)

    在继续之前，让我们快速了解一下旧的 java.sql.Date 类，以及如何将其转换为 LocalDate。

    从 Java 8 开始，我们可以在 java.sql.Date 上找到一个额外的 toLocalDate() 方法，它也为我们提供了一种将其转换为 java.time.LocalDate 的简便方法。

    在这种情况下，我们无需担心时区问题：

    convertToLocalDateViaSqlDate(Date dateToConvert)

    同样，我们也可以将旧的 Date 对象转换为 LocalDateTime 对象。让我们接下来看看。

3. 将 java.util.Date 转换为 java.time.LocalDateTime

    要获取 LocalDateTime 实例，我们同样可以使用中间的 ZonedDateTime，然后使用 toLocalDateTime() API。

    Code: DateToLocalDateTimeConverter.java

    与之前一样，我们可以使用两种可能的解决方案从 java.util.Date 获取一个 Instant 对象：

    convertToLocalDateTimeViaInstant(Date dateToConvert)

    convertToLocalDateTimeViaMilisecond(Date dateToConvert)

    请注意，对于 1582 年 10 月 10 日之前的日期，必须将 Calendar 设置为公历，并调用 [setGregorianChange()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/GregorianCalendar.html#setGregorianChange(java.util.Date)) 方法：

    ```java
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.setGregorianChange(new Date(Long.MIN_VALUE));
    Date dateToConvert = calendar.getTime();
    ```

    从 Java 8 开始，我们还可以使用 java.sql.Timestamp 获取 LocalDateTime：

    convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert)

4. 将 java.time.LocalDate 转换为 java.util.Date

    既然我们已经很好地理解了如何从旧数据表示转换到新数据表示，那么让我们来看看如何从另一个方向进行转换。

    Code: LocalDateToDateConverter.java

    我们将讨论将 LocalDate 转换为 Date 的两种可能方法。

    第一种，我们使用 java.sql.Date 对象中提供的新 valueOf(LocalDate date) 方法，该方法将 LocalDate 作为参数：

    convertToDateViaSqlDate(LocalDate dateToConvert)

    正如我们所见，它既简单又直观。它使用本地时区进行转换（所有转换都在引擎盖下完成，因此无需担心）。

    在另一个 Java 8 示例中，我们使用一个 Instant 对象，并将其传递给 java.util.Date 对象的 from(Instant instant) 方法：

    convertToDateViaInstant(LocalDate dateToConvert)

    请注意，我们在这里使用了 Instant 对象，而且在进行转换时还需要考虑时区。

    接下来，让我们使用非常类似的解决方案将 LocalDateTime 转换为 Date 对象。

5. 将 java.time.LocalDateTime 转换为 java.util.Date

    Code: LocalDateTimeToDateConverter.java

    从 LocalDateTime 获取 java.util.Date 的最简单方法是使用 Java 8 中提供的 java.sql.Timestamp 扩展：

    convertToDateViaSqlTimestamp(LocalDateTime dateToConvert)

    当然，另一种解决方案是使用即时对象，我们可以从 ZonedDateTime 获取即时对象：

    convertToDateViaInstant(LocalDateTime dateToConvert)

6. Java 9 新增功能

    在 Java 9 中，有一些新方法可简化 java.util.Date 与 java.time.LocalDate 或 java.time.LocalDateTime 之间的转换。

    LocalDate.ofInstant(Instant instant, ZoneId zone) 和 LocalDateTime.ofInstant(Instant instant, ZoneId zone) 提供了方便的快捷方式：

    DateToLocalDateConverter.java\convertToLocalDate(Date dateToConvert)

    DateToLocalDateTimeConverter.java\convertToLocalDateTime(Date dateToConvert)

7. 结论

    在本文中，我们介绍了将旧的 java.util.Date 转换为新的 java.time.LocalDate 和 java.time.LocalDateTime 以及反向转换的可能方法。

## Code

本文的完整实现可在 [GitHub](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-datetime-conversion) 上获取。
