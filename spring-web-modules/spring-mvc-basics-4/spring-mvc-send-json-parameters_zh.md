# [带有Spring MVC的JSON参数](https://www.baeldung.com/spring-mvc-send-json-parameters)

1. 一览表

    在这个简短的教程中，我们将仔细研究如何在Spring MVC中使用JSON参数。

    首先，我们将从JSON参数的一点背景开始。然后，我们将进入兔子洞，看看如何在POST和GET请求中发送JSON参数。

2. Spring MVC中的JSON参数

    使用JSON发送或接收数据是Web开发人员的常见做法。JSON字符串的分层结构提供了一种更紧凑、更人类可读的表示HTTP请求参数的方式。

    默认情况下，Spring MVC为字符串等简单数据类型提供开箱即用的数据绑定。为此，它使用引擎盖下的[内置属性编辑器列表](https://docs.spring.io/spring/docs/current/javadoc-api/org/springframework/beans/propertyeditors/package-summary.html)。

    然而，在现实世界的项目中，我们可能想要绑定更复杂的数据类型。例如，将JSON参数映射到模型对象中可能很方便。

3. 在POST中发送JSON数据

    Spring提供了一种通过POST请求发送JSON数据的直接方式。[内置的@RequestBody注释](https://www.baeldung.com/spring-request-response-body#@requestbody)可以自动将封装在请求正文中的JSON数据反序列化到特定的模型对象中。

    一般来说，我们不必自己解析请求主体。我们可以利用[杰克逊图书馆](https://www.baeldung.com/jackson)为我们做所有繁重的工作。

    现在，让我们看看如何在Spring MVC中通过POST请求发送JSON数据。

    首先，我们需要创建一个模型对象来表示传递的JSON数据。例如，考虑产品类别：

    ```java
    public class Product {
        private int id;
        private String name;
        private double price;
        // default constructor + getters + setters
    }
    ```

    其次，让我们定义一个接受POST请求的Spring处理程序方法：

    ```java
    @PostMapping("/create")
    @ResponseBody
    public Product createProduct(@RequestBody Product product) {
        // custom logic
        return product;
    }
    ```

    正如我们所看到的，用@RequestBody注释产品参数足以绑定客户端发送的JSON数据。

    现在，我们可以使用[cURL](https://www.baeldung.com/curl-rest)测试我们的POST请求：

    ```bash
    curl -i \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    -X POST --data \
        '{"id": 1,"name": "Asus Zenbook","price": 800}' "http://localhost:8080/spring-mvc-basics-4/products/create"
    ```

4. 在GET中发送JSON参数

    Spring MVC提供[@RequestParam](https://www.baeldung.com/spring-request-param)从GET请求中提取查询参数。然而，与@RequestBody不同，@RequestParam注释仅支持int和String等简单数据类型。

    因此，要发送JSON，我们需要将JSON参数定义为一个简单的字符串。

    这里的大问题是：我们如何将我们的JSON参数（即字符串）转换为Product类的对象？

    答案很简单！Jackson库提供的[ObjectMapper类](https://www.baeldung.com/jackson-object-mapper-tutorial)提供了一种将JSON字符串转换为Java对象的灵活方法。

    现在，让我们看看如何在Spring MVC中通过GET请求发送JSON参数。首先，我们需要在控制器中创建另一个处理程序方法来处理GET请求：

    ```java
    @GetMapping("/get")
    @ResponseBody
    public Product getProduct(@RequestParam String product) throws JsonMappingException, JsonProcessingException {
        Product prod = objectMapper.readValue(product, Product.class);
        return prod;
    }
    ```

    如上所示，readValue()方法允许将JSON参数产品直接反序列化到Product类的实例中。

    请注意，我们将JSON查询参数定义为字符串对象。现在，如果我们想像使用@RequestBody时那样传递一个产品对象呢？

    为了回答这个问题，Spring通过[自定义属性编辑器](https://www.baeldung.com/spring-mvc-custom-property-editor)提供了一个简洁灵活的解决方案。

    首先，我们需要创建一个自定义属性编辑器，以封装将作为字符串给出的JSON参数转换为产品对象的逻辑：

    ```java
    public class ProductEditor extends PropertyEditorSupport {

        private ObjectMapper objectMapper;

        public ProductEditor(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public void setAsText(String text) throws IllegalArgumentException {
            if (StringUtils.isEmpty(text)) {
                setValue(null);
            } else {
                Product prod = new Product();
                try {
                    prod = objectMapper.readValue(text, Product.class);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException(e);
                }
                setValue(prod);
            }
        }

    }
    ```

    接下来，让我们将JSON参数绑定到Product类的对象上：

    ```java
    @GetMapping("/get2")
    @ResponseBody
    public Product get2Product(@RequestParam Product product) {
        // custom logic
        return product;
    }
    ```

    最后，我们需要添加拼图中最后一个缺失的碎片。让我们在我们的Spring控制器中注册ProductEditor：

    ```java
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Product.class, new ProductEditor(objectMapper));
    }
    ```

    请记住，我们需要对JSON参数进行URL编码，以确保安全传输。

    所以，而不是：

    `GET /spring-mvc-basics-4/products/get2?product={"id": 1,"name": "Asus Zenbook","price": 800}`

    我们需要发送：

    `GET /spring-mvc-basics-4/products/get2?product=%7B%22id%22%3A%201%2C%22name%22%3A%20%22Asus%20Zenbook%22%2C%22price%22%3A%20800%7D`

5. 结论

    总之，我们看到了如何在Spring MVC中使用JSON。一路上，我们展示了如何在POST和GET请求中发送JSON参数。
