# [Spring Data REST中的预测和摘录](https://www.baeldung.com/spring-data-rest-projections-excerpts)

1. 一览表

    在本文中，我们将探索Spring Data REST的预测和摘录概念。

    我们将学习如何使用投影来创建模型的自定义视图，以及如何使用摘录作为资源集合的默认视图。

2. 我们的域名模型

    首先，让我们从定义我们的领域模型开始：书籍和作者。

    让我们来看看图书实体类：

    ```java
    @Entity
    public class Book {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private long id;

        @Column(nullable = false)
        private String title;
        
        private String isbn;

        @ManyToMany(mappedBy = "books", fetch = FetchType.EAGER)
        private List<Author> authors;
    }
    ```

    以及作者模型：

    ```java
    @Entity
    public class Author {

        @Id
        @GeneratedValue(strategy=GenerationType.IDENTITY)
        private long id;

        @Column(nullable = false)
        private String name;

        @ManyToMany(cascade = CascadeType.ALL)
        @JoinTable(
        name = "book_author", 
        joinColumns = @JoinColumn(
            name = "book_id", referencedColumnName = "id"), 
        inverseJoinColumns = @JoinColumn(
            name = "author_id", referencedColumnName = "id"))
        private List<Book> books;
    }
    ```

    这两个实体也存在多对多的关系。

    接下来，让我们为每个模型定义标准的Spring Data REST存储库：

    `public interface BookRepository extends CrudRepository<Book, Long> {}`

    `public interface AuthorRepository extends CrudRepository<Author, Long> {}`

    现在，我们可以访问图书端点，使用 <http://localhost:8080/books/{id}> 的id获取特定图书的详细信息：

    ```json
    {
    "title" : "Animal Farm",
    "isbn" : "978-1943138425",
    "_links" : {
        "self" : {
        "href" : "http://localhost:8080/books/1"
        },
        "book" : {
        "href" : "http://localhost:8080/books/1"
        },
        "authors" : {
        "href" : "http://localhost:8080/books/1/authors"
        }
    }
    }
    ```

    请注意，由于作者模型有其存储库，作者的详细信息不是响应的一部分。然而，我们可以找到它们的链接——<http://localhost:8080/books/1/authors>。

3. 创建投影

    有时，我们只对实体属性的子集或自定义视图感兴趣。在这种情况下，我们可以利用预测。

    让我们使用Spring Data REST投影为我们的书创建一个自定义视图。

    我们将从创建一个简单的投影开始，称为CustomBook：

    ```java
    @Projection(
    name = "customBook",
    types = { Book.class })
    public interface CustomBook {
        String getTitle();
    }
    ```

    请注意，我们的投影被定义为带有@Projection注释的接口。我们可以使用名称属性来自定义投影的名称，以及类型属性来定义它所应用的对象。

    在我们的示例中，CustomBook投影将只包含一本书的标题。

    创建投影后，让我们再看看我们的图书表示：

    ```json
    {
    "title" : "Animal Farm",
    "isbn" : "978-1943138425",
    "_links" : {
        "self" : {
        "href" : "<http://localhost:8080/books/1>"
        },
        "book" : {
        "href" : "<http://localhost:8080/books/1{?projection}>",
        "templated" : true
        },
        "authors" : {
        "href" : "<http://localhost:8080/books/1/authors>"
        }
    }
    }
    ```

    太好了，我们可以看到我们投影的链接。让我们检查一下我们在`http://localhost:8080/books/1?projection=customBook`：

    ```json
    {
    "title" : "Animal Farm",
    "_links" : {
        "self" : {
        "href" : "<http://localhost:8080/books/1>"
        },
        "book" : {
        "href" : "<http://localhost:8080/books/1{?projection}>",
        "templated" : true
        },
        "authors" : {
        "href" : "<http://localhost:8080/books/1/authors>"
        }
    }
    }
    ```

    在这里，我们可以看到，我们只获得标题字段，而isbn不再出现在自定义视图中。

    一般来说，我们可以在`http://localhost:8080/books/1?projection={projection name}`。

    此外，请注意，我们需要在与模型相同的软件包中定义我们的投影。或者，我们可以使用RepositoryRestConfigurerAdapter来显式添加它：

    ```java
    @Configuration
    public class RestConfig implements RepositoryRestConfigurer {
        @Override
        public void configureRepositoryRestConfiguration(
        RepositoryRestConfiguration repositoryRestConfiguration, CorsRegistry cors) {
            repositoryRestConfiguration.getProjectionConfiguration()
            .addProjection(CustomBook.class);
        }
    }
    ```

4. 向预测添加新数据

    现在，让我们看看如何将新数据添加到我们的投影中。

    正如我们在上一节中讨论的那样，我们可以使用投影来选择要包含在视图中的属性。此外，我们还可以添加原始视图中未包含的数据。

    1. 隐藏的数据

        默认情况下，id不包含在原始资源视图中。

        为了查看结果中的id，我们可以明确地包含id字段：

        ```java
        @Projection(
        name = "customBook",
        types = { Book.class })
        public interface CustomBook {
            @Value("#{target.id}")
            long getId();

            String getTitle();
        }
        ```

        现在输出在`http://localhost:8080/books/1?projection={projection name}`将是：

        ```json
        {
        "id" : 1,
        "title" : "Animal Farm",
        "_links" : {
            ...
        }
        }
        ```

        请注意，我们还可以使用@JsonIgnore包含从原始视图中隐藏的数据。

    2. 计算数据

        我们还可以包含根据我们的资源属性计算出的新数据。

        例如，我们可以在我们的预测中包括作者计数：

        ```java
        @Projection(name = "customBook", types = { Book.class })
        public interface CustomBook {

            @Value("#{target.id}")
            long getId(); 
            
            String getTitle();
                
            @Value("#{target.getAuthors().size()}")
            int getAuthorCount();
        }
        ```

        我们可以在`http://localhost:8080/books/1?projection=customBook`：

        ```json
        {
            "id" : 1,
            "title" : "Animal Farm",
            "authorCount" : 1,
            "_links" : {
                    ...
                }
        }
        ```

    3. 轻松访问相关资源

        最后，如果我们通常需要访问相关资源——就像我们的例子中的书的作者一样，我们可以通过明确包含它来避免额外的请求：

        ```java
        @Projection(
        name = "customBook",
        types = { Book.class })
        public interface CustomBook {

            @Value("#{target.id}")
            long getId(); 
            
            String getTitle();
            
            List<Author> getAuthors();
            
            @Value("#{target.getAuthors().size()}")
            int getAuthorCount();
        }
        ```

        最终的投影输出将是：

        ```json
        {
        "id" : 1,
        "title" : "Animal Farm",
        "authors" : [ {
            "name" : "George Orwell"
        } ],
        "authorCount" : 1,
        "_links" : {
            "self" : {
            "href" : "<http://localhost:8080/books/1>"
            },
            "book" : {
            "href" : "<http://localhost:8080/books/1{?projection}>",
            "templated" : true
            },
            "authors" : {
            "href" : "<http://localhost:8080/books/1/authors>"
            }
        }
        }
        ```

        接下来，我们将看看摘录。

5. 摘录

    摘录是我们作为默认视图应用于资源集合的预测。

    让我们自定义我们的BookRepository，以自动使用customBook Projection进行集合响应。

    为了实现这一点，我们将使用@RepositoryRestResource注释的摘录投影属性：

    ```java
    @RepositoryRestResource(excerptProjection = CustomBook.class)
    public interface BookRepository extends CrudRepository<Book, Long> {}
    ```

    现在，我们可以通过调用<http://localhost:8080/books>来确保customBook是图书收藏的默认视图：

    ```json
    {
    "_embedded" : {
        "books" : [ {
        "id" : 1,
        "title" : "Animal Farm",
        "authors" : [ {
            "name" : "George Orwell"
        } ],
        "authorCount" : 1,
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/books/1"
            },
            "book" : {
            "href" : "http://localhost:8080/books/1{?projection}",
            "templated" : true
            },
            "authors" : {
            "href" : "http://localhost:8080/books/1/authors"
            }
        }
        } ]
    },
    "_links" : {
        "self" : {
        "href" : "<http://localhost:8080/books>"
        },
        "profile" : {
        "href" : "<http://localhost:8080/profile/books>"
        }
    }
    }
    ```

    同样适用于在<http://localhost:8080/authors/1/books>上查看特定作者的书籍：

    ```json
    {
    "_embedded" : {
        "books" : [ {
        "id" : 1,
        "authors" : [ {
            "name" : "George Orwell"
        } ],
        "authorCount" : 1,
        "title" : "Animal Farm",
        "_links" : {
            "self" : {
            "href" : "http://localhost:8080/books/1"
            },
            "book" : {
            "href" : "http://localhost:8080/books/1{?projection}",
            "templated" : true
            },
            "authors" : {
            "href" : "http://localhost:8080/books/1/authors"
            }
        }
        } ]
    },
    "_links" : {
        "self" : {
        "href" : "<http://localhost:8080/authors/1/books>"
        }
    }
    }
    ```

    如前所述，摘录仅自动适用于收集资源。对于单个资源，我们必须使用投影参数，如前几节所示。

    这是因为如果我们将投影应用为单个资源的默认视图，将很难知道如何从部分视图更新资源。

    最后，重要的是要记住，预测和摘录是为只读目的。

6. 结论

    我们学习了如何使用Spring Data REST投影来创建模型的自定义视图。我们还学习了如何使用摘录作为资源集合的默认视图。
