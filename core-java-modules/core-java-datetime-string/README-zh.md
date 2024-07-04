# Java 日期解析和格式化 Cookbooks 和示例

本模块包含有关解析和格式化 Java 日期和时间对象的文章。

- [Check If a String Is a Valid Date in Java](https://www.baeldung.com/java-string-valid-date)
- [x] [Regex for Matching Date Pattern in Java](#在java中使用regex匹配日期模式)
- [Guide to DateTimeFormatter](https://www.baeldung.com/java-datetimeformatter)
- [Format ZonedDateTime to String](https://www.baeldung.com/java-format-zoned-datetime-string)
- [A Guide to SimpleDateFormat](https://www.baeldung.com/java-simple-date-format)
- [Display All Time Zones With GMT and UTC in Java](https://www.baeldung.com/java-time-zones)
- [Convert Between String and Timestamp](https://www.baeldung.com/java-string-to-timestamp)
- [Convert String to Date in Java](http://www.baeldung.com/java-string-to-date)
- [Format a Milliseconds Duration to HH:MM:SS](https://www.baeldung.com/java-ms-to-hhmmss)
- [Format Instant to String in Java](https://www.baeldung.com/java-instant-to-string)

## 在Java中使用Regex匹配日期模式

1. 简介

    如果使用得当，正则表达式是匹配各种模式的强大工具。

    在本文中，我们将使用 java.util.regex 包来确定给定字符串是否包含有效日期。

    有关正则表达式的介绍，请参阅[《Java正则表达式API指南》](https://www.baeldung.com/regular-expressions-java)。

2. 日期格式概述

    我们将根据国际公历定义有效日期。我们的格式将遵循一般模式： YYYY-MM-DD。

    我们还要加入闰年的概念，即包含 2 月 29 日这一天的年份。根据公历，除了能被 100 整除但包括能被 400 整除的年份外，如果年号能被 4 平均整除，我们就称这一年为闰年。

    在所有其他情况下，我们都称这一年为平年。

    有效日期示例

    - 2017-12-31
    - 2020-02-29
    - 2400-02-29

    无效日期示例

    - 2017/12/31：标记分隔符不正确
    - 2018-1-1：缺少前导零
    - 2018-04-31：四月的天数计算错误
    - 2100-02-29：今年不是闰年，因为值除以 100，所以二月仅限 28 天

3. 执行解决方案

    既然我们要使用正则表达式匹配日期，那么首先让我们勾画出一个接口 DateMatcher，它提供了一个匹配方法：

    regexp.datepattern/DateMatcher.java

    下面我们将逐步介绍该接口的实现，最后形成完整的解决方案。

    1. 匹配广泛的格式

        首先，我们将创建一个非常简单的原型来处理匹配器的格式约束：

        regexp.datepattern/FormattedDateMatcher.java

        在这里，我们指定有效日期必须由三组整数组成，中间用破折号隔开。第一组由四个整数组成，其余两组各有两个整数。

        匹配日期： 2017-12-31, 2018-01-31, 0000-00-00, 1029-99-72

        不匹配日期 2018-01、2018-01-xx、2020/02/29

    2. 匹配特定日期格式

        我们的第二个示例接受日期标记范围以及格式约束。为简单起见，我们将兴趣限制在 1900-2999 年之间。

        既然我们已经成功匹配了一般的日期格式，那么我们就需要进一步限制，以确保日期确实正确：

        `^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$`

        这里我们引入了三组需要匹配的整数范围：

        - `(19|2[0-9])[0-9]{2}`通过匹配以 19 或 2X 开头，后跟几个任意数字的数字，涵盖了有限的年份范围。
        - `0[1-9]|1[012]`匹配 01-12 范围内的月份编号
        - `0[1-9]|[12][0-9]|3[01]`匹配范围为 01-31 的日号。

        匹配日期： 1900-01-01, 2205-02-31, 2999-12-31

        不匹配日期： 1899-12-31、2018-05-35、2018-13-05、3000-01-01、2018-01-xx

    3. 匹配 2 月 29 日

        为了正确匹配闰年，我们必须首先确定何时遇到闰年，然后确保我们接受 2 月 29 日为这些年份的有效日期。

        由于限制范围内的闰年数量足够多，我们应该使用适当的可分割规则来过滤它们：

        - 如果一个数字的最后两位数组成的数字能被 4 整除，则原数字也能被 4 整除。
        - 如果数字的最后两位数是 00，则该数字能被 100 整除。

        下面是一种解法：

        `^((2000|2400|2800|(19|2[0-9])(0[48]|[2468][048]|[13579][26]))-02-29)$`

        该模式由以下部分组成：

        - `2000|2400|2800`匹配一组除数为 400 的闰年，限制范围为 1900-2999
        - `19|2[0-9](0[48]|[2468][048]|[13579][26]))`匹配除数为 4 且除数不为 100 的年份的所有白名单组合。
        - `-02-29` 匹配 February 2nd

        匹配日期： 2020-02-29, 2024-02-29, 2400-02-29

        不匹配的日期 2019-02-29, 2100-02-29, 3200-02-29, 2020/02/29

    4. 匹配二月的一般日期

        除了匹配闰年中的 2 月 29 日，我们还需要匹配所有年份中二月的所有其他日子（1 - 28）：

        `^(((19|2[0-9])[0-9]{2})-02-(0[1-9]|1[0-9]|2[0-8]))$`

        匹配日期： 2018-02-01, 2019-02-13, 2020-02-25

        非匹配日期 2000-02-30, 2400-02-62, 2018/02/28

    5. 匹配 31 天的月份

        一月、三月、五月、七月、八月、十月和十二月应匹配 1 到 31 天：

        `^(((19|2[0-9])[0-9]{2})-(0[13578]|10|12)-(0[1-9]|[12][0-9]|3[01]))$`

        匹配日期： 2018-01-31, 2021-07-31, 2022-08-31

        不匹配日期 2018-01-32, 2019-03-64, 2018/01/31

    6. 匹配 30 天的月份

        四月、六月、九月和十一月应匹配 1 到 30 天：

        `^(((19|2[0-9])[0-9]{2})-(0[469]|11)-(0[1-9]|[12][0-9]|30))$`

        匹配日期： 2018-04-30, 2019-06-30, 2020-09-30

        非匹配日期： 2018-04-31, 2019-06-31, 2018/04/30

    7. 公历日期匹配器

        现在，我们可以将上述所有模式合并到一个匹配器中，从而得到一个满足所有约束条件的完整的 GregorianDateMatcher：

        regexp.datepattern/GregorianDateMatcher.java

        我们使用交替字符"|"来匹配四个分支中的至少一个。因此，二月的有效日期要么匹配第一个分支闰年的 2 月 29 日，要么匹配第二个分支 1 至 28 日中的任意一天。其余月份的日期则与第三和第四分支匹配。

        由于我们还没有为了更好的可读性而对这种模式进行优化，因此请随意尝试它的长度。

        至此，我们已经满足了开头引入的所有约束条件。

    8. 性能说明

        解析复杂的正则表达式可能会严重影响执行流程的性能。本文的主要目的并不是学习一种高效的方法来测试字符串是否属于所有可能日期的集合。

        如果需要可靠、快速的日期验证方法，请考虑使用 Java8 提供的 LocalDate.parse()。

4. 结论

    在本文中，我们学习了如何使用正则表达式来匹配严格格式化的公历日期，并提供了格式、范围和月份长度的规则。

## Code

本文介绍的所有代码均可在 [Github](https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-datetime-string) 上获取。这是一个基于 Maven 的项目，因此很容易导入和运行。

## Relevant Articles

- [Check If a String Is a Valid Date in Java](https://www.baeldung.com/java-string-valid-date)
- [Regex for Matching Date Pattern in Java](https://www.baeldung.com/java-date-regular-expressions)
- [Guide to DateTimeFormatter](https://www.baeldung.com/java-datetimeformatter)
- [Format ZonedDateTime to String](https://www.baeldung.com/java-format-zoned-datetime-string)
- [A Guide to SimpleDateFormat](https://www.baeldung.com/java-simple-date-format)
- [Display All Time Zones With GMT and UTC in Java](https://www.baeldung.com/java-time-zones)
- [Convert Between String and Timestamp](https://www.baeldung.com/java-string-to-timestamp)
- [Convert String to Date in Java](http://www.baeldung.com/java-string-to-date)
- [Format a Milliseconds Duration to HH:MM:SS](https://www.baeldung.com/java-ms-to-hhmmss)
- [Format Instant to String in Java](https://www.baeldung.com/java-instant-to-string)
