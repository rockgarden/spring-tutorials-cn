# [如何使用Spring Data访问EntityManager](https://www.baeldung.com/spring-data-entitymanager)

1. 一览表

    在处理Spring Data应用程序时，我们通常不需要直接访问EntityManager。然而，有时我们可能想要访问它，比如创建自定义查询或分离实体。

    在本快速教程中，我们将学习如何通过扩展Spring数据存储库来访问EntityManager。

2. 使用Spring数据访问实体管理器

    我们可以通过创建一个扩展自定义存储库来获取实体管理器，例如，一个内置的JpaRepository。

    首先，我们将为要存储在数据库中的用户定义一个示例实体：

    ```java
    @Entity
    public class User {
        @Id
        @GeneratedValue
        private Long id;
        private String name;
        private String email;
        // ...
    }
    ```

    我们无法直接访问JpaRepository中的实体管理器，因此我们需要创建自己的实体管理器。

    让我们用自定义查找方法创建一个：

    ```java
    public interface CustomUserRepository {
        User customFindMethod(Long id);
    }
    ```

    使用@PeristenceContext，我们可以在实现类中注入EntityManager：

    ```java
    public class CustomUserRepositoryImpl implements CustomUserRepository {

        @PersistenceContext
        private EntityManager entityManager;

        @Override
        public User customFindMethod(Long id) {
            return (User) entityManager.createQuery("FROM User u WHERE u.id = :id")
            .setParameter("id", id)
            .getSingleResult();
        }
    }
    ```

    同样，我们可以使用@PersistenceUnit注释，在这种情况下，我们将访问EntityManagerFactory，并从中访问EntityManager。

    最后，我们将创建一个同时扩展JpaRepository和CustomRepository的存储库：

    ```java
    @Repository
    public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
    }
    ```

    此外，我们可以制作一个Spring Boot应用程序，并测试以检查所有内容是否都绑定并按预期工作：

    ```java
    @SpringBootTest(classes = CustomRepositoryApplication.class)
    class CustomRepositoryUnitTest {

        @Autowired
        private UserRepository userRepository;

        @Test
        public void givenCustomRepository_whenInvokeCustomFindMethod_thenEntityIsFound() {
            User user = new User();
            user.setEmail("foo@gmail.com");
            user.setName("userName");

            User persistedUser = userRepository.save(user);

            assertEquals(persistedUser, userRepository.customFindMethod(user.getId()));
        }
    }
    ```

3. 结论

    在本文中，我们查看了在Spring Data应用程序中访问EntityManager的快速示例。我们可以在自定义存储库中访问实体管理器，并且仍然通过扩展其功能来使用我们的Spring数据存储库。
