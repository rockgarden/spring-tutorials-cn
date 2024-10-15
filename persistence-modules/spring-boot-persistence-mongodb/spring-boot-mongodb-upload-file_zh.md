# [使用MongoDB和Spring Boot上传和检索文件](https://www.baeldung.com/spring-boot-mongodb-upload-file)

1. 一览表

    在本教程中，我们将讨论如何使用MongoDB和Spring Boot上传和检索文件。

    我们将使用MongoDB BSON来执行小文件，使用GridFS来执行大文件。

2. Maven配置

    首先，我们将将spring-boot-starter-data-mongodb依赖项添加到我们的pom.xml中：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb</artifactId>
    </dependency>
    ```

    此外，我们需要spring-boot-starter-web和spring-boot-starter-thymeleaf依赖项来显示我们应用程序的用户界面。

    在本教程中，我们使用Spring Boot版本2.x。

3. Sring Boot属性

    接下来，我们将配置必要的Spring Boot属性。

    让我们从MongoDB属性开始：

    ```properites
    spring.data.mongodb.host=localhost
    spring.data.mongodb.port=27017
    spring.data.mongodb.database=springboot-mongo
    ```

    我们还将设置Servlet Multipart属性，以允许上传大文件：

    ```properties
    spring.servlet.multipart.max-file-size=256MB
    spring.servlet.multipart.max-request-size=256MB
    spring.servlet.multipart.enabled=true
    ```

4. 上传小文件

    现在，我们将讨论如何使用MongoDB BSON上传和检索小文件（大小<16MB）。

    在这里，我们有一个简单的文档类——照片。我们将把图像文件存储在BSON二进制文件中：

    ```java
    @Document(collection = "photos")
    public class Photo {
        @Id
        private String id;
        private String title;
        private Binary image;
    }
    ```

    我们将有一个简单的PhotoRepository：

    `public interface PhotoRepository extends MongoRepository<Photo, String> { }`

    现在，对于PhotoService，我们只有两种方法：

    - addPhoto（）——将照片上传到MongoDB
    - getPhoto（）——检索具有给定id的照片

    ```java
    @Service
    public class PhotoService {

        @Autowired
        private PhotoRepository photoRepo;

        public String addPhoto(String title, MultipartFile file) throws IOException { 
            Photo photo = new Photo(title); 
            photo.setImage(
            new Binary(BsonBinarySubType.BINARY, file.getBytes())); 
            photo = photoRepo.insert(photo); return photo.getId(); 
        }

        public Photo getPhoto(String id) { 
            return photoRepo.findById(id).get(); 
        }
    }
    ```

5. 上传大文件

    现在，我们将使用GridFS上传和检索大文件。

    首先，我们将定义一个简单的DTO-视频-来表示一个大文件：

    ```java
    public class Video {
        private String title;
        private InputStream stream;
    }
    ```

    与PhotoService类似，我们将有一个具有两种方法的视频服务——addVideo（）和getVideo（）：

    ```java
    @Service
    public class VideoService {

        @Autowired
        private GridFsTemplate gridFsTemplate;

        @Autowired
        private GridFsOperations operations;

        public String addVideo(String title, MultipartFile file) throws IOException { 
            DBObject metaData = new BasicDBObject(); 
            metaData.put("type", "video"); 
            metaData.put("title", title); 
            ObjectId id = gridFsTemplate.store(
            file.getInputStream(), file.getName(), file.getContentType(), metaData); 
            return id.toString(); 
        }

        public Video getVideo(String id) throws IllegalStateException, IOException { 
            GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id))); 
            Video video = new Video(); 
            video.setTitle(file.getMetadata().get("title").toString()); 
            video.setStream(operations.getResource(file).getInputStream());
            return video; 
        }
    }
    ```

6. 控制器

    现在，让我们来看看控制器——PhotoController和VideoController。

    1. 照片控制器

        首先，我们有PhotoController，它将使用我们的PhotoService来添加/获取照片。

        我们将定义addPhoto（）方法来上传和创建新照片：

        ```java
        @PostMapping("/photos/add")
        public String addPhoto(@RequestParam("title") String title, 
        @RequestParam("image") MultipartFile image, Model model) 
        throws IOException {
            String id = photoService.addPhoto(title, image);
            return "redirect:/photos/" + id;
        }
        ```

        我们还有getPhoto（）来检索具有给定ID的照片：

        ```java
        @GetMapping("/photos/{id}")
        public String getPhoto(@PathVariable String id, Model model) {
            Photo photo = photoService.getPhoto(id);
            model.addAttribute("title", photo.getTitle());
            model.addAttribute("image",
            Base64.getEncoder().encodeToString(photo.getImage().getData()));
            return "photos";
        }
        ```

        请注意，由于我们以字节[]的形式返回图像数据，我们将将其转换为Base64字符串，以在前端显示。

    2. 视频控制器

        接下来，我们来看看我们的视频控制器。

        这将有一个类似的方法，addVideo（），将视频上传到我们的MongoDB：

        ```java
        @PostMapping("/videos/add")
        public String addVideo(@RequestParam("title") String title, 
        @RequestParam("file") MultipartFile file, Model model) throws IOException {
            String id = videoService.addVideo(title, file);
            return "redirect:/videos/" + id;
        }
        ```

        在这里，我们有getVideo（）来检索具有给定ID的视频：

        ```java
        @GetMapping("/videos/{id}")
        public String getVideo(@PathVariable String id, Model model) throws Exception {
            Video video = videoService.getVideo(id);
            model.addAttribute("title", video.getTitle());
            model.addAttribute("url", "/videos/stream/" + id);
            return "videos";
        }
        ```

        我们还可以添加一个streamVideo（）方法，该方法将从视频输入流创建流URL：

        ```java
        @GetMapping("/videos/stream/{id}")
        public void streamVideo(@PathVariable String id, HttpServletResponse response) throws Exception {
            Video video = videoService.getVideo(id);
            FileCopyUtils.copy(video.getStream(), response.getOutputStream());        
        }
        ```

7. 前端

    最后，让我们看看我们的前端。

    让我们从uploadPhoto.html开始，它提供了上传图像的简单形式：

    ```jsp
    <html>
    <body>
    <h1>Upload new Photo</h1>
    <form method="POST" action="/photos/add" enctype="multipart/form-data">
        Title:<input type="text" name="title" />
        Image:<input type="file" name="image" accept="image/*" />
        <input type="submit" value="Upload" />
    </form>
    </body>
    </html>
    ```

    接下来，我们将添加photos.html视图来显示我们的照片：

    ```jsp
    <html>
    <body>
        <h1>View Photo</h1>
        Title: <span th:text="${title}">name</span>
        <img alt="sample" th:src="*{'data:image/png;base64,'+image}" />
    </body>
    </html>
    ```

    同样，我们有uploadVideo.html来上传视频：

    ```jsp
    <html>
    <body>
    <h1>Upload new Video</h1>
    <form method="POST" action="/videos/add" enctype="multipart/form-data">
        Title:<input type="text" name="title" />
        Video:<input type="file" name="file" accept="video/*" />
        <input type="submit" value="Upload" />
    </form>
    </body>
    </html>
    ```

    和videos.html来显示视频：

    ```jsp
    <html>
    <body>
        <h1>View Video</h1>
        Title: <span th:text="${title}">title</span>
        <video width="400" controls>
            <source th:src="${url}" />
        </video>
    </body>
    </html>
    ```

8. 结论

    在本文中，我们学习了如何使用MongoDB和Spring Boot上传和检索文件。我们同时使用BSON和GridFS来上传和检索文件。
