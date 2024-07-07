# Spring Boot Vue

本模块包含关于Spring Boot与Vue.js的文章

## 带有Spring Boot后端的Vue.js前台

1. 概述

    在本教程中，我们将讨论一个使用Vue.js前端渲染单页，同时使用Spring Boot作为后端的应用实例。

    我们还将利用Thymeleaf来向模板传递信息。

2. 2.Spring Boot设置

    应用程序的pom.xml使用spring-boot-starter-thymeleaf依赖项，与通常的spring-boot-starter-web一起进行模板渲染。

    Thymeleaf默认在templates/处寻找视图模板，我们将在src/main/resources/templates/index.html中添加一个空的index.html。我们将在下一节中更新其内容。

    最后，我们的Spring Boot控制器将在src/main/java中：

    springbootmvc.controllers/MainController.java

    这个控制器渲染了一个单一的模板，数据通过Spring Web模型对象用model.addAttribute传递给视图。

    让我们使用以下方法运行该应用程序：

    `mvn spring-boot:run`

    浏览到 <http://localhost:8080> ，查看索引页。当然，这时它将是空的。

    我们的目标是使该页面打印出这样的内容：

    ```txt
    Name of Event: FIFA 2018

    Lionel Messi
    Argentina's superstar

    Christiano Ronaldo
    Portugal top-ranked player
    ```

3. 使用Vue.Js组件渲染数据

    1. 模板的基本设置

        在模板中，让我们加载Vue.js和Bootstrap（可选）来渲染用户界面：

        ```html
        <script src="https://cdn.jsdelivr.net/npm/vue@2.5.16/dist/vue.js"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/babel-standalone/6.21.1/babel.min.js"></script>
        ```

        在这里，我们从CDN上加载Vue.js，但如果你喜欢，也可以托管它。

        我们在浏览器中加载Babel，这样我们就可以在页面中写一些符合ES6标准的代码，而不需要运行转译步骤。

        在现实世界的应用中，你可能会使用Webpack和Babel转译器等工具来构建程序，而不是使用浏览器内的Babel。

        现在让我们保存页面并使用mvn spring-boot:run命令重新启动。我们刷新浏览器，看看我们的更新；还没有什么有趣的东西。

        接下来，让我们设置一个空的div元素，我们将把我们的用户界面附在上面：

        `<div id="contents"></div>`

        接下来，我们在页面上设置一个Vue应用程序：

        ```html
        <script type="text/babel">
            var app = new Vue({
                el: '#contents'
            });
        </script>
        ```

        刚才发生了什么？这段代码在页面上创建了一个Vue应用程序。我们把它附加到带有CSS选择器#contents的元素上。

        这指的是页面上的空div元素。现在应用程序已经被设置为使用Vue.js!

    2. 在模板中显示数据

        接下来，让我们创建一个标题，显示我们从Spring控制器传来的'eventName'属性，并使用Thymeleaf的功能渲染它：

        ```html
        <div class="lead">
            <strong>Name of Event:</strong>
            <span th:text="${eventName}"></span>
        </div>
        ```

        现在让我们给Vue应用程序附加一个'data'属性，以保存我们的播放器数据数组，它是一个简单的JSON数组。

        我们的Vue应用程序现在看起来像这样：

        ```html
        <script type="text/babel">
            var app = new Vue({
                el: '#contents',
                data: {
                    players: [
                        { id: "1", 
                        name: "Lionel Messi", 
                        description: "Argentina's superstar" },
                        { id: "2", 
                        name: "Christiano Ronaldo", 
                        description: "World #1-ranked player from Portugal" }
                    ]
                }
            });
        </script>
        ```

        现在，Vue.js知道了一个叫做player的数据属性。

    3. 用Vue.js组件渲染数据

        接下来，让我们创建一个名为player-card的Vue.js组件，它只渲染一个球员。记住，在创建Vue应用程序之前要注册这个组件。

        否则，Vue将找不到它：

        ```js
        Vue.component('player-card', {
            props: ['player'],
            template: `<div class="card">
                <div class="card-body">
                    <h6 class="card-title">
                        {{ player.name }}
                    </h6>
                    <p class="card-text">
                        <div>
                            {{ player.description }}
                        </div>
                    </p>
                </div>
                </div>`
        });
        ```

        最后，让我们在应用程序对象中循环播放一组球员，为每个球员渲染一个球员卡组件：

        ```js
        <ul>
            <li style="list-style-type:none" v-for="player in players">
                <player-card
                v-bind:player="player" 
                v-bind:key="player.id">
                </player-card>
            </li>
        </ul>
        ```

        这里的逻辑是名为v-for的Vue指令，它将在玩家数据属性中循环遍历每个玩家，并为`<li>`元素中的每个玩家条目呈现一张玩家卡。

        `v-bind:player`意味着玩家卡组件将被赋予一个名为player的属性，其值将是当前正在使用的player循环变量。`v-bind:key`是使每个`＜li＞`元素唯一所必需的。

        一般来说，`player.id`是一个不错的选择，因为它已经是独一无二的了。

        现在，如果您重新加载此页面，请观察devtools中生成的HTML标记，它将类似于以下内容：

        ```js
        <ul>
            <li style="list-style-type: none;">
                <div class="card">
                    // contents
                </div>
            </li>
            <li style="list-style-type: none;">
                <div class="card">
                    // contents
                </div>
            </li>
        </ul>
        ```

        工作流程改进注意事项：每次更改代码时都必须重新启动应用程序并刷新浏览器，这很快就会变得麻烦。

        因此，为了让生活更轻松，请参阅这篇关于如何使用Spring Boot开发工具和[自动重启](/SpringBootDevTools.md)的文章。

4. 结论

    在这篇快速的文章中，我们介绍了如何使用Spring Boot作为后端，使用Vue.js作为前端来设置web应用程序。这个配方可以为更强大和可扩展的应用程序奠定基础，而这只是大多数此类应用程序的起点。

## Relevant Articles

- [x] [Vue.js Frontend with a Spring Boot Backend](https://www.baeldung.com/spring-boot-vue-js)

## Code

和往常一样，代码示例可以在GitHub上找到。
