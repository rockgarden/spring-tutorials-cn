# [将CSV文件读取到数组中](https://www.baeldung.com/java-csv-file-array)

Java IO

CSV Java文件阅读器

1. 概述  

    简单来说，CSV（逗号分隔值）文件包含由逗号分隔符分隔的组织信息。  

    在本教程中，我们将探讨将CSV文件读取到数组中的不同方法。  

2. 使用`java.io`中的`BufferedReader`  

    首先，我们使用`BufferedReader`中的`readLine()`逐行读取记录，然后根据逗号分隔符将每行拆分为多个标记：  

    ```java
    List<List<String>> records = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader("book.csv"))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] values = line.split(COMMA_DELIMITER);
            records.add(Arrays.asList(values));
        }
    }
    ```  

    注意：对于更复杂的CSV文件（例如包含引号或逗号作为值），此方法无法正确解析。  

3. 使用`java.util`中的`Scanner`  

    接下来，我们使用`java.util.Scanner`遍历文件内容并逐行检索记录：  

    ```java
    List<List<String>> records = new ArrayList<>();
    try (Scanner scanner = new Scanner(new File("book.csv"))) {
        while (scanner.hasNextLine()) {
            records.add(getRecordFromLine(scanner.nextLine()));
        }
    }
    ```  

    然后，我们解析这些行并将其存储在数组中：  

    ```java
    private List<String> getRecordFromLine(String line) {
        List<String> values = new ArrayList<String>();
        try (Scanner rowScanner = new Scanner(line)) {
            rowScanner.useDelimiter(COMMA_DELIMITER);
            while (rowScanner.hasNext()) {
                values.add(rowScanner.next());
            }
        }
        return values;
    }
    ```  

    与之前一样，此方法无法正确处理更复杂的CSV文件。  

4. 使用OpenCSV  

    我们可以使用OpenCSV来处理更复杂的CSV文件。  

    OpenCSV是一个第三方库，提供了处理CSV文件的API。  

    我们将使用`CSVReader`中的`readNext()`方法读取文件中的记录：  

    ```java
    List<List<String>> records = new ArrayList<List<String>>();
    try (CSVReader csvReader = new CSVReader(new FileReader("book.csv"));) {
        String[] values = null;
        while ((values = csvReader.readNext()) != null) {
            records.add(Arrays.asList(values));
        }
    }
    ```  

    要深入了解OpenCSV，请查看我们的[OpenCSV教程](https://www.baeldung.com/opencsv)。  

5. 使用`Files`工具类  

    或者，我们可以使用`Files`类来实现相同的目标。该工具类包含多个静态方法，用于操作文件和目录。让我们看看如何在实际中使用它。  

    1. 使用`Files#lines`  

        `lines()`方法是Java 8中引入的增强功能之一。它允许我们将文件的所有行作为[流读取](https://www.baeldung.com/java-8-streams)。以下是具体示例：  

        ```java
        try (Stream<String> lines = Files.lines(Paths.get(CSV_FILE))) {
            List<List<String>> records = lines.map(line -> Arrays.asList(line.split(COMMA_DELIMITER)))
            .collect(Collectors.toList());
        }
        ```  

        在这里，`Paths.get(CSV_FILE)`方法返回一个[Path](https://www.baeldung.com/java-path-vs-file)实例，表示CSV文件的路径。此外，我们使用`map()`方法将CSV文件的每一行转换为字符串列表。请注意，我们使用了[try-with-resources](https://www.baeldung.com/java-try-with-resources)来确保文件在结束时自动关闭。  

    2. 使用`Files#readAllLines`  

        同样，`Files`提供了`readAllLines()`方法作为实现相同目标的另一种选择。与`lines()`类似，此方法接受一个`Path`对象作为参数，并直接返回包含指定CSV文件每一行的列表：  

        ```java
        List<List<String>> records = Files.readAllLines(Paths.get(CSV_FILE))
        .stream()
        .map(line -> Arrays.asList(line.split(COMMA_DELIMITER)))
        .collect(Collectors.toList());
        ```  

        值得注意的是，我们使用流API将CSV文件读取到`List<List<String>>`中。这里需要提到的一个重要注意事项是，`readAllLines()`会一次性将所有内容加载到内存中，因此不要用它来读取大文件。  

    3. 使用`Files#newBufferedReader`  

        另一种选择是使用`newBufferedReader()`方法。它返回一个[BufferedReader](https://www.baeldung.com/java-buffered-reader)实例，提供了一种更高效地读取文件的方式。  

        接下来，我们通过一个示例来学习如何使用此方法：  

        ```java
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CSV_FILE))) {
            List<List<String>> records = reader.lines()
            .map(line -> Arrays.asList(line.split(COMMA_DELIMITER)))
            .collect(Collectors.toList());
        }
        ```  

        如上所示，我们使用了与之前相同的逻辑来读取CSV文件。请注意，与其他方法相比，`newBufferedReader()`是处理大文件时的最佳选择。  

6. 结论  

    在这篇简短的文章中，我们探讨了将CSV文件读取到数组中的不同方法。  
