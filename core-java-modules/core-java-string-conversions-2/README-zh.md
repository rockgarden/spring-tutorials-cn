# Java字符串转换

本模块包含关于字符串from/to另一类型的转换的文章。

## 字符串转换

1. 概述

    在这篇快速文章中，我们将探讨String对象到Java中支持的不同数据类型的一些简单转换。
2. 将字符串转换为int或Integer

    如果我们需要将String转换为原始的int或Integer封装类型，我们可以使用parseInt()或valueOf()API来获得相应的int或Integer返回值：

    ```java
    @Test
    public void whenConvertedToInt_thenCorrect() {
        String beforeConvStr = "1";
        int afterConvInt = 1;
        assertEquals(Integer.parseInt(beforeConvStr), afterConvInt);
    }
    @Test
    public void whenConvertedToInteger_thenCorrect() {
        String beforeConvStr = "12";
        Integer afterConvInteger = 12;
        assertEquals(Integer.valueOf(beforeConvStr).equals(afterConvInteger), true);
    }
    ```

3. 将字符串转换为long或Long

    如果我们需要将一个String转换为原始的long或Long封装类型，我们可以分别使用parseLong()或valueOf()：

    ```java
    @Test
    public void whenConvertedTolong_thenCorrect() {
        String beforeConvStr = "12345";
        long afterConvLongPrimitive = 12345;
        assertEquals(Long.parseLong(beforeConvStr), afterConvLongPrimitive);
    }
    @Test
    public void whenConvertedToLong_thenCorrect() {
        String beforeConvStr = "14567";
        Long afterConvLong = 14567l;
        assertEquals(Long.valueOf(beforeConvStr).equals(afterConvLong), true);
    }
    ```

4. 将字符串转换为double或Double

    如果我们需要将一个String转换为原始的double或Double封装类型，我们可以分别使用parseDouble()或valueOf()：

    ```java
    @Test
    public void whenConvertedTodouble_thenCorrect() {
        String beforeConvStr = "1.4";
        double afterConvDoublePrimitive = 1.4;
        assertEquals(Double.parseDouble(beforeConvStr), afterConvDoublePrimitive, 0.0);
    }
    @Test
    public void whenConvertedToDouble_thenCorrect() {
        String beforeConvStr = "145.67";
        double afterConvDouble = 145.67d;
        assertEquals(Double.valueOf(beforeConvStr).equals(afterConvDouble), true);
    }
    ```

5. 将字符串转换为ByteArray

    为了将字符串转换为字节数组，getBytes()使用平台的默认字符集将字符串编码为一串字节，将结果存储到一个新的字节数组中。
    当传递的字符串不能使用默认的字符集进行编码时，getBytes()的行为没有被明确说明。根据java[文档](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/String.html)，当需要对编码过程进行更多控制时，应该使用[java.nio.charset.CharsetEncoder](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/charset/CharsetEncoder.html)类：

    ```java
    @Test
    public void whenConvertedToByteArr_thenCorrect() {
        String beforeConvStr = "abc";
        byte[] afterConvByteArr = new byte[] { 'a', 'b', 'c' };
        assertEquals(Arrays.equals(beforeConvStr.getBytes(), afterConvByteArr), true);
    }
    ```

6. 将字符串转换为CharArray

    为了将一个字符串转换为一个CharArray实例，我们可以简单地使用toCharArray()：

    ```java
    @Test
    public void whenConvertedToCharArr_thenCorrect() {
        String beforeConvStr = "hello";
        char[] afterConvCharArr = { 'h', 'e', 'l', 'l', 'o' };
        assertEquals(Arrays.equals(beforeConvStr.toCharArray(), afterConvCharArr), true);
    }
    ```

7. 将字符串转换为布尔型或布尔型

    要将一个String实例转换为原始布尔型或布尔型封装类型，我们可以分别使用parseBoolean()或valueOf()API：

    ```java
    @Test
    public void whenConvertedToboolean_thenCorrect() {
        String beforeConvStr = "true";
        boolean afterConvBooleanPrimitive = true;
        assertEquals(Boolean.parseBoolean(beforeConvStr), afterConvBooleanPrimitive);
    }

    @Test
    public void whenConvertedToBoolean_thenCorrect() {
        String beforeConvStr = "true";
        Boolean afterConvBoolean = true;
        assertEquals(Boolean.valueOf(beforeConvStr), afterConvBoolean);
    }
    ```

8. 将字符串转换为Date或LocalDateTime

    Java 6提供了用于表示日期的java.util.Date数据类型。Java 8为日期和时间引入了新的API，以解决旧的java.util.Date和java.util.Calendar的缺点。
    你可以阅读这篇[文章](https://www.baeldung.com/java-8-date-time-intro)了解更多细节。
    1. 将字符串转换为java.util.Date

        为了将String对象转换为Date对象，我们需要首先通过传递描述日期和时间格式的模式来构造一个SimpleDateFormat对象。
        例如，模式的可能值是 "MM-dd-yyy" 或 "yyyy-MM-dd"。接下来，我们需要调用传递字符串的parse方法。
        作为参数传递的String应该与pattern的格式相同。否则，在运行时将会抛出一个ParseException：

        ```java
        @Test
        public void whenConvertedToDate_thenCorrect() throws ParseException {
            String beforeConvStr = "15/10/2013";
            int afterConvCalendarDay = 15;
            int afterConvCalendarMonth = 9;
            int afterConvCalendarYear = 2013;
            SimpleDateFormat formatter = new SimpleDateFormat("dd/M/yyyy");
            Date afterConvDate = formatter.parse(beforeConvStr);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(afterConvDate);
            assertEquals(calendar.get(Calendar.DAY_OF_MONTH), afterConvCalendarDay);
            assertEquals(calendar.get(Calendar.MONTH), afterConvCalendarMonth);
            assertEquals(calendar.get(Calendar.YEAR), afterConvCalendarYear);
        }
        ```

    2. 将字符串转换为java.time.LocalDateTime

        LocalDateTime是一个不可变的日期时间对象，它代表一个时间，通常被看作是年-月-日-时-分-秒。
        为了将String对象转换为LocalDateTime对象，我们可以简单地使用parse API：

        ```java
        @Test
        public void whenConvertedToLocalDateTime_thenCorrect() {
            String str = "2007-12-03T10:15:30";
            int afterConvCalendarDay = 03;
            Month afterConvCalendarMonth = Month.DECEMBER;
            int afterConvCalendarYear = 2007;
            LocalDateTime afterConvDate 
            = new UseLocalDateTime().getLocalDateTimeUsingParseMethod(str);
            assertEquals(afterConvDate.getDayOfMonth(), afterConvCalendarDay);
            assertEquals(afterConvDate.getMonth(), afterConvCalendarMonth);
            assertEquals(afterConvDate.getYear(), afterConvCalendarYear);
        }
        ```

        根据[java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_LOCAL_DATE_TIME)，该字符串必须代表一个有效的时间。否则，在运行时将抛出一个ParseException。
        例如'2011-12-03'代表一个有效的字符串格式，其中4位数字为年份，2位数字为月份，2位数字为月份的日期。

9. 总结

    在这个快速教程中，我们已经涵盖了将字符串对象转换为java中支持的不同数据类型的不同实用方法。

## Relevant Articles

- [x] [Java String Conversions](https://www.baeldung.com/java-string-conversions)
- [Convert String to Byte Array and Reverse in Java](https://www.baeldung.com/java-string-to-byte-array)
- [Convert Character Array to String in Java](https://www.baeldung.com/java-char-array-to-string)
- [Converting String to BigDecimal in Java](https://www.baeldung.com/java-string-to-bigdecimal)
- [Converting String to BigInteger in Java](https://www.baeldung.com/java-string-to-biginteger)
- [Convert a String to Camel Case](https://www.baeldung.com/java-string-to-camel-case)
- [Convert a ByteBuffer to String in Java](https://www.baeldung.com/java-bytebuffer-to-string)
- [Convert String to Float and Back in Java](https://www.baeldung.com/java-string-to-float)
- [Difference Between parseInt() and valueOf() in Java](https://www.baeldung.com/java-integer-parseint-vs-valueof)
- [Integer.toString() vs String.valueOf() in Java](https://www.baeldung.com/java-tostring-valueof)
- More articles: [[<-- prev]](../core-java-string-conversions/README.md)

## Code

本文的完整源代码和所有代码片段都可以在GitHub上找到。
