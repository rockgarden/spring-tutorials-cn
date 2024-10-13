# [在Spring Data REST中处理关系](https://www.baeldung.com/spring-data-rest-relationships)

1. 一览表

    在本教程中，我们将学习如何处理Spring Data REST中实体之间的关系。

    我们将专注于Spring Data REST为存储库公开的关联资源，考虑我们可以定义的每种关系类型。

    为了避免任何额外的设置，我们将使用H2嵌入式数据库作为示例。

2. 一对一的关系

    1. 数据模型

        让我们定义两个实体类，Library和Address，使用@OneToOne注释具有一对一的关系。该协会由协会的图书馆拥有：

        ```java
        @Entity
        public class Library {

            @Id
            @GeneratedValue
            private long id;

            @Column
            private String name;

            @OneToOne
            @JoinColumn(name = "address_id")
            @RestResource(path = "libraryAddress", rel="address")
            private Address address;
            
            // standard constructor, getters, setters
        }

        @Entity
        public class Address {

            @Id
            @GeneratedValue
            private long id;

            @Column(nullable = false)
            private String location;

            @OneToOne(mappedBy = "address")
            private Library library;

            // standard constructor, getters, setters
        }
        ```

        @RestResource注释是可选的，我们可以用它来自定义端点。

        我们还必须小心为每个协会资源使用不同的名称。否则，我们将遇到aJsonMappingException，并带有消息“Detected multiple association links with same relation type! Disambiguate association.”

        关联名称默认为属性名称，我们可以使用@RestResource注释的rel属性对其进行自定义：

        ```java
        @OneToOne
        @JoinColumn(name = "secondary_address_id")
        @RestResource(path = "libraryAddress", rel="address")
        private Address secondaryAddress;
        ```

        如果我们将上面的secondaryAddress属性添加到Library类中，我们将有两个名为address的资源，从而遇到冲突。

        我们可以通过为rel属性指定不同的值来解决这个问题，或者通过省略RestResource注释，使资源名称默认为secondaryAddress。

    2. 存储库

        为了将这些实体作为资源公开，我们将通过扩展CrudRepository接口为每个实体创建两个存储库接口：

        `public interface LibraryRepository extends CrudRepository<Library, Long> {}`

        `public interface AddressRepository extends CrudRepository<Address, Long> {}`

    3. 创建资源

        首先，我们将添加一个Library实例来配合使用：

        ```bash
        curl -i -X POST -H "Content-Type:application/json"
        -d '{"name":"My Library"}' http://localhost:8080/libraries
        ```

        然后API返回JSON对象：

        ```json
        {
        "name" : "My Library",
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/libraries/1"
            },
            "library" : {
            "href" : "http://localhost:8080/libraries/1"
            },
            "address" : {
            "href" : "http://localhost:8080/libraries/1/libraryAddress"
            }
        }
        }
        ```

        请注意，如果我们在Windows上使用curl，我们必须转义表示JSON主体的字符串中的双引号字符：

        `-d "{\"name\":\"My Library\"}"`

        我们可以在响应正文中看到，关联资源已在libras/{libraryId}/address端点公开。

        在我们创建关联之前，向此端点发送GET请求将返回一个空对象。

        然而，如果我们想要添加关联，我们必须首先创建一个地址实例：

        ```bash
        curl -i -X POST -H "Content-Type:application/json" 
        -d '{"location":"Main Street nr 5"}' http://localhost:8080/addresses
        ```

        POST请求的结果是包含地址记录的JSON对象：

        ```json
        {
        "location" : "Main Street nr 5",
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/addresses/1"
            },
            "address" : {
            "href" : "http://localhost:8080/addresses/1"
            },
            "library" : {
            "href" : "http://localhost:8080/addresses/1/library"
            }
        }
        }
        ```

    4. 创建协会

        在坚持两个实例后，我们可以使用其中一个关联资源来建立关系。

        这是使用HTTP方法PUT完成的，该方法支持text/uri-list的媒体类型，以及包含要绑定关联的资源URI的主体。

        由于Library实体是协会的所有者，我们将为Library添加一个地址：

        ```bash
        curl -i -X PUT -d "http://localhost:8080/addresses/1"
        -H "Content-Type:text/uri-list" http://localhost:8080/libraries/1/libraryAddress
        ```

        如果成功，它将返回状态204。为了验证这一点，我们可以检查地址的library关联资源：

        `curl -i -X GET http://localhost:8080/addresses/1/library`

        它应该返回名为“My Library”的库JSON对象。

        要删除关联，我们可以使用DELETE方法调用端点，确保使用关系所有者的关联资源：

        `curl -i -X DELETE http://localhost:8080/libraries/1/libraryAddress`

3. 一对多关系

    我们使用@OneToMany和@ManyToOne注释定义一对多关系。我们还可以添加可选的@RestResource注释来自定义关联资源。

    1. 数据模型

        为了体现一对多关系，我们将添加一个新的Book实体，它代表与Library实体关系的“many”结束：

        ```java
        @Entity
        public class Book {

            @Id
            @GeneratedValue
            private long id;
            
            @Column(nullable=false)
            private String title;
            
            @ManyToOne
            @JoinColumn(name="library_id")
            private Library library;
            
            // standard constructor, getter, setter
        }
        ```

        然后，我们也会将关系添加到图书馆类中：

        ```java
        public class Library {
            //...
            @OneToMany(mappedBy = "library")
            private List<Book> books;
            //...
        }
        ```

    2. 存储库

        我们还需要创建一个BookRepository：

        `public interface BookRepository extends CrudRepository<Book, Long> { }`

    3. 协会资源

        为了将书籍添加到库中，我们需要首先使用/books集合资源创建一个Book实例：

        ```bash
        curl -i -X POST -d "{\"title\":\"Book1\"}"
          -H "Content-Type:application/json" http://localhost:8080/books
        ```

        以下是POST请求的回复：

        ```json
        {
        "title" : "Book1",
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/books/1"
            },
            "book" : {
            "href" : "http://localhost:8080/books/1"
            },
            "bookLibrary" : {
            "href" : "http://localhost:8080/books/1/library"
            }
        }
        }
        ```

        在响应正文中，我们可以看到关联端点/books/{bookId}/library已创建。

        现在，让我们通过向包含库资源URI的关联资源发送PUT请求，将这本书与我们在上一节中创建的库相关联：

        ```bash
        curl -i -X PUT -H "Content-Type:text/uri-list"
            -d "http://localhost:8080/libraries/1" http://localhost:8080/books/1/library
        ```

        我们可以通过在图书馆/书籍关联资源上使用GET方法来验证图书馆中的书籍：

        `curl -i -X GET http://localhost:8080/libraries/1/books`

        返回的JSON对象将包含一个书籍数组：

        ```json
        {
        "_embedded" : {
            "books" : [ {
            "title" : "Book1",
            "_links" : {
                "self" : {
                "href" : "http://localhost:8080/books/1"
                },
                "book" : {
                "href" : "http://localhost:8080/books/1"
                },
                "bookLibrary" : {
                "href" : "http://localhost:8080/books/1/library"
                }
            }
            } ]
        },
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/libraries/1/books"
            }
        }
        }
        ```

        要删除关联，我们可以在关联资源上使用DELETE方法：

        `curl -i -X DELETE http://localhost:8080/books/1/library`

4. 多对多关系

    我们使用@ManyToMany注释定义多对多关系，我们也可以添加@RestResource。

    1. 数据模型

        为了创建一个多对多关系的示例，我们将添加一个新的模型类，即Author，它与Book实体具有多对多关系：

        ```java
        @Entity
        public class Author {

            @Id
            @GeneratedValue
            private long id;

            @Column(nullable = false)
            private String name;

            @ManyToMany(cascade = CascadeType.ALL)
            @JoinTable(name = "book_author", 
            joinColumns = @JoinColumn(name = "book_id", referencedColumnName = "id"), 
            inverseJoinColumns = @JoinColumn(name = "author_id", 
            referencedColumnName = "id"))
            private List<Book> books;

            //standard constructors, getters, setters
        }
        ```

        然后，我们也会在图书课上添加协会：

        ```java
        public class Book {
            //...
            @ManyToMany(mappedBy = "books")
            private List<Author> authors;
            //...
        }
        ```

    2. 存储库

        接下来，我们将创建一个存储库界面来管理Author实体：

        `public interface AuthorRepository extends CrudRepository<Author, Long> { }`

    3. 协会资源

        与前几节一样，在建立协会之前，我们必须首先创建资源。

        我们将通过向/authors集合资源发送POST请求来创建Author实例：

        ```bash
        curl -i -X POST -H "Content-Type:application/json" 
        -d "{\"name\":\"author1\"}" http://localhost:8080/authors
        ```

        接下来，我们将在我们的数据库中添加第二个Book记录：

        ```bash
        curl -i -X POST -H "Content-Type:application/json" 
        -d "{\"title\":\"Book 2\"}" http://localhost:8080/books
        ```

        然后，我们将在Author记录上执行GET请求，以查看关联URL：

        ```json
        {
        "name" : "author1",
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/authors/1"
            },
            "author" : {
            "href" : "http://localhost:8080/authors/1"
            },
            "books" : {
            "href" : "http://localhost:8080/authors/1/books"
            }
        }
        }
        ```

        现在，我们可以使用带有PUT方法的endpointauthors/1/books在两个图书记录和作者记录之间创建关联，该方法支持text/uri-list的媒体类型，并可以接收多个URI。

        要发送多个URI，我们必须用行符将它们分开：

        ```bash
        curl -i -X PUT -H "Content-Type:text/uri-list"
        --data-binary @uris.txt http://localhost:8080/authors/1/books
        ```

        uris.txt文件包含书籍的URI，每本书都在单独的行中：

        ```txt
        http://localhost:8080/books/1
        http://localhost:8080/books/2
        ```

        为了验证两本书是否与作者相关联，我们可以向关联端点发送GET请求：

        `curl -i -X GET http://localhost:8080/authors/1/books`

        我们将收到以下回复：

        ```json
        {
        "_embedded" : {
            "books" : [ {
            "title" : "Book 1",
            "_links" : {
                "self" : {
                "href" : "http://localhost:8080/books/1"
                }
            //...
            }
            }, {
            "title" : "Book 2",
            "_links" : {
                "self" : {
                "href" : "http://localhost:8080/books/2"
                }
            //...
            }
            } ]
        },
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/authors/1/books"
            }
        }
        }
        ```

        要删除关联，我们可以使用DELETE方法向关联资源的URL发送请求，后跟{bookId}：

        `curl -i -X DELETE http://localhost:8080/authors/1/books/1`

5. 使用TestRestTemplate测试端点

    让我们创建一个测试类，该类注入TestRestTemplate实例，并定义我们将使用的常量：

    ```java
    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = SpringDataRestApplication.class, 
    webEnvironment = WebEnvironment.DEFINED_PORT)
    public class SpringDataRelationshipsTest {

        @Autowired
        private TestRestTemplate template;

        private static String BOOK_ENDPOINT = "http://localhost:8080/books/";
        private static String AUTHOR_ENDPOINT = "http://localhost:8080/authors/";
        private static String ADDRESS_ENDPOINT = "http://localhost:8080/addresses/";
        private static String LIBRARY_ENDPOINT = "http://localhost:8080/libraries/";

        private static String LIBRARY_NAME = "My Library";
        private static String AUTHOR_NAME = "George Orwell";
    }
    ```

    1. 测试一对一的关系

        我们将创建一个@Test方法，通过向集合资源发出POST请求来保存Library和Address对象。

        然后，它保存与PUT请求对关联资源的关系，并验证它是否已通过对同一资源的GET请求建立：

        ```java
        @Test
        public void whenSaveOneToOneRelationship_thenCorrect() {
            Library library = new Library(LIBRARY_NAME);
            template.postForEntity(LIBRARY_ENDPOINT, library, Library.class);

            Address address = new Address("Main street, nr 1");
            template.postForEntity(ADDRESS_ENDPOINT, address, Address.class);
            
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Content-type", "text/uri-list");
            HttpEntity<String> httpEntity 
            = new HttpEntity<>(ADDRESS_ENDPOINT + "/1", requestHeaders);
            template.exchange(LIBRARY_ENDPOINT + "/1/libraryAddress", 
            HttpMethod.PUT, httpEntity, String.class);

            ResponseEntity<Library> libraryGetResponse 
            = template.getForEntity(ADDRESS_ENDPOINT + "/1/library", Library.class);
            assertEquals("library is incorrect", 
            libraryGetResponse.getBody().getName(), LIBRARY_NAME);
        }
        ```

    2. 测试一对多关系

        现在，我们将创建一个@Test方法，该方法保存一个Library实例和两个Book实例，向每个Book对象的 /library 关联资源发送PUT请求，并验证关系是否已保存：

        ```java
        @Test
        public void whenSaveOneToManyRelationship_thenCorrect() {
            Library library = new Library(LIBRARY_NAME);
            template.postForEntity(LIBRARY_ENDPOINT, library, Library.class);

            Book book1 = new Book("Dune");
            template.postForEntity(BOOK_ENDPOINT, book1, Book.class);

            Book book2 = new Book("1984");
            template.postForEntity(BOOK_ENDPOINT, book2, Book.class);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Content-Type", "text/uri-list");    
            HttpEntity<String> bookHttpEntity 
            = new HttpEntity<>(LIBRARY_ENDPOINT + "/1", requestHeaders);
            template.exchange(BOOK_ENDPOINT + "/1/library", 
            HttpMethod.PUT, bookHttpEntity, String.class);
            template.exchange(BOOK_ENDPOINT + "/2/library", 
            HttpMethod.PUT, bookHttpEntity, String.class);

            ResponseEntity<Library> libraryGetResponse = 
            template.getForEntity(BOOK_ENDPOINT + "/1/library", Library.class);
            assertEquals("library is incorrect", 
            libraryGetResponse.getBody().getName(), LIBRARY_NAME);
        }
        ```

    3. 测试多对多关系

        为了测试Book和Author实体之间的多对多关系，我们将创建一个测试方法，保存一个Author记录和两个Book记录。

        然后，它使用两本书的URI向/books关联资源发送PUT请求，并验证关系是否已建立：

        ```java
        @Test
        public void whenSaveManyToManyRelationship_thenCorrect() {
            Author author1 = new Author(AUTHOR_NAME);
            template.postForEntity(AUTHOR_ENDPOINT, author1, Author.class);

            Book book1 = new Book("Animal Farm");
            template.postForEntity(BOOK_ENDPOINT, book1, Book.class);

            Book book2 = new Book("1984");
            template.postForEntity(BOOK_ENDPOINT, book2, Book.class);

            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Content-type", "text/uri-list");
            HttpEntity<String> httpEntity = new HttpEntity<>(
            BOOK_ENDPOINT + "/1\n" + BOOK_ENDPOINT + "/2", requestHeaders);
            template.exchange(AUTHOR_ENDPOINT + "/1/books", 
            HttpMethod.PUT, httpEntity, String.class);

            String jsonResponse = template
            .getForObject(BOOK_ENDPOINT + "/1/authors", String.class);
            JSONObject jsonObj = new JSONObject(jsonResponse).getJSONObject("_embedded");
            JSONArray jsonArray = jsonObj.getJSONArray("authors");
            assertEquals("author is incorrect", 
            jsonArray.getJSONObject(0).getString("name"), AUTHOR_NAME);
        }
        ```

6. 结论

    在本文中，我们演示了与Spring Data REST使用不同类型的关系。
