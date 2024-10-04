# [使用Spring MVC上传文件](https://www.baeldung.com/spring-file-upload)

1. 一览表

    在之前的教程中，我们介绍了表单处理的基础知识，并探索了Spring MVC中的[表单标签库](https://www.baeldung.com/spring-mvc-form-tags)。

    在本教程中，我们专注于Spring为Web应用程序中的多部分（文件上传）支持提供的内容。

    Spring允许我们使用可插拔的MultipartResolver对象启用这种多部分支持。该框架提供了一个MultipartResolver实现，用于Servlet 3.0多部分请求解析。

    配置MultipartResolver后，我们将了解如何上传单个文件和多个文件。

    我们还将接触Spring Boot。

2. 使用Servlet 3.0的配置

    为了使用Servlet 3.0多部分解析，我们需要配置应用程序的几部分。

    首先，我们需要在DispatcherServletregistration中设置一个MultipartConfigElement：

    ```java
    public class MainWebAppInitializer implements WebApplicationInitializer {

        private static final String TMP_FOLDER = "/tmp"; 
        private static final int MAX_UPLOAD_SIZE = 5 * 1024 * 1024; 
        
        @Override
        public void onStartup(ServletContext sc) throws ServletException {
            
            ServletRegistration.Dynamic appServlet = sc.addServlet("mvc", new DispatcherServlet(new GenericWebApplicationContext()));

            appServlet.setLoadOnStartup(1);
            
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(TMP_FOLDER, 
            MAX_UPLOAD_SIZE, MAX_UPLOAD_SIZE * 2L, MAX_UPLOAD_SIZE / 2);
            
            appServlet.setMultipartConfig(multipartConfigElement);
        }
    }
    ```

    在MultipartConfigElement对象中，我们配置了存储位置、最大单个文件大小、最大请求大小（在单个请求中包含多个文件的情况下）以及文件上传进度刷新到存储位置的大小。

    完成此操作后，我们可以将StandardServletMultipartResolver添加到我们的Spring配置中：

    ```java
    @Bean
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }
    ```

3. 上传文件

    为了上传我们的文件，我们可以构建一个简单的表单，其中我们使用`type='file'`的HTML输入标签。

    无论我们选择了什么上传处理配置，我们都需要将表单的编码属性设置为`multipart/form-data`。

    这让浏览器知道如何对表单进行编码：

    ```xml
    <form:form method="POST" action="/spring-mvc-xml/uploadFile" enctype="multipart/form-data">
        <table>
            <tr>
                <td><form:label path="file">Select a file to upload</form:label></td>
                <td><input type="file" name="file" /></td>
            </tr>
            <tr>
                <td><input type="submit" value="Submit" /></td>
            </tr>
        </table>
    </form>
    ```

    为了存储上传的文件，我们可以使用MultipartFile变量。

    我们可以从控制器方法中的请求参数中检索此变量：

    ```java
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public String submit(@RequestParam("file") MultipartFile file, ModelMap modelMap) {
        modelMap.addAttribute("file", file);
        return "fileUploadView";
    }
    ```

    MultipartFile类提供对上传文件详细信息的访问，包括文件名、文件类型等。

    我们可以使用一个简单的HTML页面来显示此信息：

    ```jsp
    <h2>Submitted File</h2>
    <table>
        <tr>
            <td>OriginalFileName:</td>
            <td>${file.originalFilename}</td>
        </tr>
        <tr>
            <td>Type:</td>
            <td>${file.contentType}</td>
        </tr>
    </table>
    ```

    当项目在本地运行时，可以在 <http://localhost:8080/spring-mvc-java/fileUpload> 上访问表单示例。

4. 上传多个文件

    要在单个请求中上传多个文件，我们在表单中放置多个输入文件字段：

    ```jsp
    <form:form method="POST" action="/spring-mvc-java/uploadMultiFile" enctype="multipart/form-data">
        <table>
            <tr>
                <td>Select a file to upload</td>
                <td><input type="file" name="files" /></td>
            </tr>
            <tr>
                <td>Select a file to upload</td>
                <td><input type="file" name="files" /></td>
            </tr>
            <tr>
                <td>Select a file to upload</td>
                <td><input type="file" name="files" /></td>
            </tr>
            <tr>
                <td><input type="submit" value="Submit" /></td>
            </tr>
        </table>
    </form:form>
    ```

    我们需要注意每个输入字段具有相同的名称，以便它可以作为MultipartFile的数组访问：

    ```java
    @RequestMapping(value = "/uploadMultiFile", method = RequestMethod.POST)
    public String submit(@RequestParam("files") MultipartFile[] files, ModelMap modelMap) {
        modelMap.addAttribute("files", files);
        return "fileUploadView";
    }
    ```

    现在，我们可以迭代该数组来显示文件信息：

    ```jsp
    <%@ taglib prefix="c" uri="<http://java.sun.com/jsp/jstl/core>" %>
    <html>
        <head>
            <title>Spring MVC File Upload</title>
        </head>
        <body>
            <h2>Submitted Files</h2>
            <table>
                <c:forEach items="${files}" var="file">
                    <tr>
                        <td>OriginalFileName:</td>
                        <td>${file.originalFilename}</td>
                    </tr>
                    <tr>
                        <td>Type:</td>
                        <td>${file.contentType}</td>
                    </tr>
                </c:forEach>
            </table>
        </body>
    </html>
    ```

5. 上传带有附加表单数据的文件

    我们还可以将其他信息与正在上传的文件一起发送到服务器。

    我们必须在表格中包含必填字段：

    ```jsp
    <form:form method="POST"
    action="/spring-mvc-java/uploadFileWithAddtionalData"
    enctype="multipart/form-data">
        <table>
            <tr>
                <td>Name</td>
                <td><input type="text" name="name" /></td>
            </tr>
            <tr>
                <td>Email</td>
                <td><input type="text" name="email" /></td>
            </tr>
            <tr>
                <td>Select a file to upload</td>
                <td><input type="file" name="file" /></td>
            </tr>
            <tr>
                <td><input type="submit" value="Submit" /></td>
            </tr>
        </table>
    </form:form>
    ```

    在控制器中，我们可以使用@RequestParam注释获取所有表单数据：

    ```java
    @PostMapping("/uploadFileWithAddtionalData")
    public String submit(
    @RequestParam MultipartFile file, @RequestParam String name,
    @RequestParam String email, ModelMap modelMap) {
        modelMap.addAttribute("name", name);
        modelMap.addAttribute("email", email);
        modelMap.addAttribute("file", file);
        return "fileUploadView";
    }
    ```

    与前几节类似，我们可以使用带有JSTL标签的HTML页面来显示信息。

    我们还可以封装模型类中的所有表单字段，并在控制器中使用@ModelAttribute注释。当文件有很多额外的字段时，这会很有帮助。

    让我们来看看代码：

    ```java
    public class FormDataWithFile {
        private String name;
        private String email;
        private MultipartFile file;
        // standard getters and setters
    }

    @PostMapping("/uploadFileModelAttribute")
    public String submit(@ModelAttribute FormDataWithFile formDataWithFile, ModelMap modelMap) {
        modelMap.addAttribute("formDataWithFile", formDataWithFile);
        return "fileUploadView";
    }
    ```

6. Spring启动文件上传

    如果我们使用Spring Boot，到目前为止我们看到的一切都仍然适用。

    然而，Spring Boot使配置和启动一切变得更加容易，只需一点麻烦。

    特别是，没有必要配置任何servlet，因为Boot将为我们注册和配置它，前提是我们在依赖项中包含Web模块：

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    ```

    我们可以在Maven Central上找到最新版本的spring-boot-starter-web。

    如果我们想要控制最大文件上传大小，我们可以编辑我们的应用程序。属性：

    ```properties
    spring.servlet.multipart.max-file-size=128KB
    spring.servlet.multipart.max-request-size=128KB
    ```

    我们还可以控制是否启用文件上传以及文件上传的位置：

    ```properties
    spring.servlet.multipart.enabled=true
    spring.servlet.multipart.location=${java.io.tmpdir}
    ```

    请注意，我们使用${java.io.tmpdir}来定义上传位置，以便我们可以将临时位置用于不同的操作系统。

7. 结论

    在本文中，我们研究了如何在Spring中配置多部分支持。使用此方法，我们可以支持在Web应用程序中上传文件。
