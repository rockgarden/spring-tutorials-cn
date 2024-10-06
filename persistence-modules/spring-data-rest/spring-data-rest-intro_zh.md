# [Spring Data REST简介](https://www.baeldung.com/spring-data-rest-intro)

1. 一览表

    本文将解释Spring Data REST的基础知识，并展示如何使用它来构建一个简单的REST API。

    一般来说，Spring Data REST建立在Spring Data项目之上，可以轻松构建连接到Spring Data存储库的超媒体驱动REST网络服务——所有这些都使用HAL作为驱动超媒体类型。

    它消除了通常与此类任务相关的大量手动工作，并使Web应用程序的基本CRUD功能的实现变得非常简单。

2. Maven附属机构

    我们的简单应用程序需要以下Maven依赖项：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-rest</artifactId></dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
    </dependency>
    ```

    我们决定在这个例子中使用Spring Boot，但经典的Spring也可以正常工作。我们还选择使用H2嵌入式数据库，以避免任何额外的设置，但该示例可以应用于任何数据库。

3. 编写申请

    我们将从编写一个域对象开始，以代表我们网站的用户：

    ```java
    @Entity
    public class WebsiteUser {

        @Id
        @GeneratedValue(strategy = GenerationType.AUTO)
        private long id;

        private String name;
        private String email;

        // standard getters and setters
    }
    ```

    每个用户都有一个名字和电子邮件，以及一个自动生成的ID。现在我们可以编写一个简单的存储库：

    ```java
    @RepositoryRestResource(collectionResourceRel = "users", path = "users")
    public interface UserRepository extends PagingAndSortingRepository<WebsiteUser, Long> {
        List<WebsiteUser> findByName(@Param("name") String name);
    }
    ```

    这是一个界面，允许您使用WebsiteUser对象执行各种操作。我们还定义了一个自定义查询，该查询将根据给定的名称提供用户列表。

    @RepositoryRestResource注释是可选的，用于自定义REST端点。如果我们决定省略它，Spring将自动在“/websiteUsers”而不是“/users”中创建一个端点。

    最后，我们将编写一个标准的Spring Boot主类来初始化应用程序：

    ```java
    @SpringBootApplication
    public class SpringDataRestApplication {
        public static void main(String[] args) {
            SpringApplication.run(SpringDataRestApplication.class, args);
        }
    }
    ```

    就是这样！我们现在有一个功能齐全的REST API。让我们来看看它的行动。

4. 访问REST API

    如果我们运行应用程序并在浏览器中访问 <http://localhost:8080/> ，我们将收到以下JSON：

    ```json
    {
    "_links" : {
        "users" : {
        "href" : "http://localhost:8080/users{?page,size,sort}",
        "templated" : true
        },
        "profile" : {
        "href" : "http://localhost:8080/profile"
        }
    }
    }
    ```

    如您所见，有一个“/users”端点可用，它已经有“?page“，”?size””和“?sort”选项。

    还有一个标准的“/profile”端点，它提供应用程序元数据。需要注意的是，响应的结构遵循REST架构风格的约束。具体来说，它提供了一个统一的界面和自我描述性信息。这意味着每条消息都包含足够的信息来描述如何处理消息。

    我们的应用程序中还没有用户，所以去 <http://localhost:8080/users> 只会显示一个空的用户列表。让我们使用curl来添加用户。

    ```bash
    $ curl -i -X POST -H "Content-Type:application/json" -d '{  "name" : "Test", \ 
    "email" : "test@test.com" }' http://localhost:8080/users
    {
    "name" : "test",
    "email" : "test@test.com",
    "_links" : {
        "self" : {
        "href" : "http://localhost:8080/users/1"
        },
        "websiteUser" : {
        "href" : "http://localhost:8080/users/1"
        }
    }
    }
    ```

    让我们也看看响应标题：

    ```log
    HTTP/1.1 201 Created
    Server: Apache-Coyote/1.1
    Location: http://localhost:8080/users/1
    Content-Type: application/hal+json;charset=UTF-8
    Transfer-Encoding: chunked
    ```

    您会注意到返回的内容类型是“application/hal+json”。HAL是一种简单的格式，它提供了一种一致和简单的方法来在API中的资源之间进行超链接。标题还自动包含Locationheader，这是我们可用于访问新创建用户的地址。

    我们现在可以通过 <http://localhost:8080/users/1> 访问此用户

    ```json
    {
    "name" : "test",
    "email" : "test@test.com",
    "_links" : {
            "self" : {
            "href" : "http://localhost:8080/users/1"
            },
            "websiteUser" : {
            "href" : "http://localhost:8080/users/1"
            }
        }
    }
    ```

    您还可以使用curl或任何其他REST客户端发出PUT、PATCH和DELETE请求。同样重要的是要注意，Spring Data REST自动遵循HATEOAS的原则。HATEOAS是REST架构风格的限制之一，这意味着应该使用超文本来通过API找到你的路。

    最后，让我们尝试访问我们之前编写的自定义查询，并找到所有名为“test”的用户。这是通过访问 <http://localhost:8080/users/search/findByName?name=test>? 来完成的。

    ```json
    {
    "_embedded" : {
        "users" : [ {
        "name" : "test",
        "email" : "test@test.com",
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/users/1"
            },
            "websiteUser" : {
            "href" : "http://localhost:8080/users/1"
            }
        }
        } ]
    },
    "_links" : {
        "self" : {
        "href" : "http://localhost:8080/users/search/findByName?name=test"
        }
    }
    }
    ```

5. 结论

    本教程演示了使用Spring Data REST创建简单REST API的基础知识。
