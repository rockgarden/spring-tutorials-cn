# [Selenium WebDriver 中的隐式等待与显式等待](https://www.baeldung.com/selenium-implicit-explicit-wait)  

测试

Selenium

1. 概述

    Web 应用测试的一个挑战是处理网页的动态特性。网页加载需要时间，某些元素可能在一段时间后才会出现。因此，Selenium 提供了“等待机制”，帮助我们在继续执行测试前，等待某个元素出现、消失或可点击。

    本文将探讨 Selenium 中不同类型的等待方式，比较**隐式等待（Implicit Wait）** 与 **显式等待（Explicit Wait）**，并介绍在 Selenium 测试中使用等待的最佳实践。

2. Selenium 中的等待类型

    Selenium 提供了多种等待机制，用于等待元素的出现、消失或可交互状态。这些机制主要分为三类：

    - **隐式等待（Implicit Wait）**
    - **显式等待（Explicit Wait）**
    - **流畅等待（Fluent Wait）**

    为了演示，我们先定义一些页面元素的选择器常量：

    ```java
    private static final By LOCATOR_ABOUT = By.xpath("//a[starts-with(., 'About')]");
    private static final By LOCATOR_ABOUT_BAELDUNG = By.xpath("//h3[normalize-space()='About Baeldung']");
    private static final By LOCATOR_ABOUT_HEADER = By.xpath("//h1");
    ```

    1. 隐式等待（Implicit Wait）

        隐式等待是一种**全局设置**，适用于整个 Selenium 脚本中的所有元素。它会在指定时间内轮询 DOM，等待元素出现。如果超时仍未找到元素，则抛出异常。

        默认的隐式等待时间为 0 秒。我们可以在 WebDriver 初始化后设置一次，之后无法更改：

        ```java
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        ```

        设置了隐式等待后，测试中无需再手动添加等待指令。

        以下测试会导航到 `www.baeldung.com`，点击“About”菜单并进入子页面。由于设置了 10 秒的隐式等待，测试无需显式等待即可通过：

        ```java
        void givenPage_whenNavigatingWithImplicitWait_ThenOK() {
            final String expected = "About Baeldung";
            driver.navigate().to("https://www.baeldung.com/");

            driver.findElement(LOCATOR_ABOUT).click();
            driver.findElement(LOCATOR_ABOUT_BAELDUNG).click();

            final String actual = driver.findElement(LOCATOR_ABOUT_HEADER).getText();
            assertEquals(expected, actual);
        }
        ```

        > ⚠️ 注意：如果不设置隐式等待，该测试很可能会失败。

        **特点总结：**

        - 全局生效
        - 只能等待“元素是否存在”
        - 一旦设置，对所有 `findElement` 调用都生效

    2. 显式等待（Explicit Wait）

        显式等待更加灵活，允许我们**等待某个特定条件成立**后再继续执行。

        我们使用 `ExpectedConditions` 类来定义条件，例如元素是否可见、是否可点击等。如果在指定时间内条件未满足，将抛出 `TimeoutException`。

        WebDriver 检查条件的轮询频率固定为每 500 毫秒一次。显式等待不是全局的，可以为不同元素设置不同的条件和超时时间。

        我们将上面的测试改写为使用显式等待：

        首先创建一个 `WebDriverWait` 实例，设置超时时间为 10 秒：

        ```java
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ```

        然后在操作元素前，使用 `wait.until()` 等待元素可见：

        ```java
        void givenPage_whenNavigatingWithExplicitWait_thenOK() {
            final String expected = "About Baeldung";
            driver.navigate().to("https://www.baeldung.com/");

            driver.findElement(LOCATOR_ABOUT).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(LOCATOR_ABOUT_BAELDUNG));

            driver.findElement(LOCATOR_ABOUT_BAELDUNG).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(LOCATOR_ABOUT_HEADER));

            final String actual = driver.findElement(LOCATOR_ABOUT_HEADER).getText();
            assertEquals(expected, actual);
        }
        ```

        虽然需要手动管理等待，但这种方式更精确，能显著提升测试性能。

        [ExpectedConditions](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/ui/ExpectedConditions.html) 提供了丰富的条件判断方法，例如：

        - `elementToBeClickable()`：元素可点击
        - `invisibilityOf()`：元素不可见
        - `presenceOfElementLocated()`：元素存在于 DOM 中
        - `textToBePresentInElement()`：元素包含指定文本
        - `visibilityOf()`：元素可见

    3. 流畅等待（Fluent Wait）

        流畅等待是显式等待的一种更精细的版本，允许我们自定义**轮询频率**和**忽略特定异常**。

        我们可以通过 `FluentWait` 设置超时时间和轮询间隔：

        ```java
        Wait<WebDriver> wait = new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(10))
            .pollingEvery(Duration.ofMillis(500)) // 每500ms检查一次
            .ignoring(NoSuchElementException.class); // 可忽略某些异常
        ```

        测试逻辑与显式等待相同：

        ```java
        void givenPage_whenNavigatingWithFluentWait_thenOK() {
            final String expected = "About Baeldung";
            driver.navigate().to("https://www.baeldung.com/");

            driver.findElement(LOCATOR_ABOUT).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(LOCATOR_ABOUT_BAELDUNG));

            driver.findElement(LOCATOR_ABOUT_BAELDUNG).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(LOCATOR_ABOUT_HEADER));

            final String actual = driver.findElement(LOCATOR_ABOUT_HEADER).getText();
            assertEquals(expected, actual);
        }
        ```

        > ✅ 优势：可自定义轮询频率、可忽略异常、更灵活

3. 隐式等待 vs 显式等待

    | 特性 | 隐式等待 | 显式等待 |
    |------|--------|--------|
    | **超时设置** | 全局设置，影响所有查找 | 针对特定条件设置 |
    | **等待条件** | 仅等待元素是否存在 | 可等待多种条件（可见、可点击等） |
    | **作用范围** | 全局 | 局部（针对特定元素或操作） |
    | **抛出异常** | `NoSuchElementException` | `TimeoutException` |

    > 📌 **关键区别**：隐式等待只关心“元素是否存在”，而显式等待可以等待“元素是否可见”、“是否可点击”等更具体的条件。

    ⚠️ **重要警告（来自 Selenium 官方[文档](https://www.selenium.dev/documentation/webdriver/waits/#:~:text=Warning%3A%20Do%20not%20mix%20implicit,to%20occur%20after%2020%20seconds.)）**：
    > **不要混合使用隐式等待和显式等待！** 否则可能导致不可预测的等待时间。例如，设置 10 秒隐式等待 + 15 秒显式等待，实际等待时间可能达到 20 秒。

4. 最佳实践

    使用等待时应遵循以下最佳实践：

    1. **始终使用等待**：等待元素加载是自动化测试的关键步骤。
    2. **优先使用显式等待而非隐式等待**：隐式等待会让测试在元素找不到时仍然等待完整超时时间，降低反馈效率。显式和流畅等待更精准。
    3. **必要时使用流畅等待**：当你需要以特定频率重复检查某个条件时，流畅等待是更好的选择。
    4. **设置合理的等待时间**：太短会导致误报（false negative），太长会增加测试总时长。
    5. **使用 `ExpectedConditions`**：它提供了丰富的预定义条件，确保测试逻辑清晰且高效。

5. StaleElementReferenceException（元素已过期异常）

    [StaleElementReferenceException](https://www.selenium.dev/documentation/webdriver/troubleshooting/errors/#stale-element-reference-exception) 是一个常见问题，发生在**之前定位到的元素在 DOM 中已不存在或已更新**时。

    例如，在等待某个条件时，页面发生了刷新或动态更新，导致元素“过期”。

    解决方案：重试机制

    我们可以捕获异常并重新定位元素：

    ```java
    boolean stale = true;
    int retries = 0;
    while (stale && retries < 5) {
        try {
            element.click();
            stale = false;
        } catch (StaleElementReferenceException ex) {
            retries++;
            // 重新查找元素
            element = driver.findElement(locator);
        }
    }
    if (stale) {
        throw new RuntimeException("元素在重试5次后仍然过期");
    }
    ```

    更优方案：封装 WebDriver 和 WebElement

    为了避免在每个测试中重复处理，可以封装 `WebDriver` 和 `WebElement`，在内部自动处理 `StaleElementReferenceException`。

    这样，测试代码无需关心异常处理，所有可能抛出该异常的方法（如 `click()`, `getText()`）都可以被自动重试。

6. 总结

    在本教程中，我们学习了等待机制是编写高效、稳定 Selenium 测试的关键部分。

    合理使用等待可以：

    - 避免因加载延迟导致的测试失败
    - 确保在操作前元素已准备好
    - 提高测试的可靠性和稳定性

    - **显式等待** 比隐式等待提供更强的控制力。
    - **流畅等待** 在需要精细控制轮询频率时尤为有用。
    - **避免混合使用隐式和显式等待**，以免产生不可预测的行为。
    - 对于 `StaleElementReferenceException`，可通过重试机制或封装 WebDriver 来优雅处理。

    遵循这些最佳实践，可以显著提升自动化测试的效率和可靠性。
