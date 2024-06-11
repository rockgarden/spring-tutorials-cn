# [推荐的Spring Boot项目的包结构](https://www.baeldung.com/spring-boot-package-structure)

1. 概述

    在构建一个新的Spring Boot项目时，我们可以高度灵活地组织我们的类。

    不过，还是有一些建议需要我们牢记。

2. 没有默认包

    鉴于 @ComponentScan、@EntityScan、@ConfigurationPropertiesScan 和 @SpringBootApplication 等 Spring Boot 注解使用包来定义扫描位置，建议我们避免使用默认包，也就是说，我们应该始终在我们的类中声明包。

3. 主类

    @SpringBootApplication注解会触发对当前包及其子包的组件扫描。因此，一个可靠的方法是让项目的主类驻留在基础包中。

    这是可以配置的，我们仍然可以通过手动指定基础包来将其定位在其他地方。然而，在大多数情况下，这个选项肯定更简单。

    甚至更多，一个基于JPA的项目需要在主类上有一些额外的注解：

    ```java
    @SpringBootApplication(scanBasePackages = "example.baeldung.com")
    @EnableJpaRepositories("example.baeldung.com")
    @EntityScan("example.baeldung.com")
    ```

    另外，要注意可能需要额外的配置。

4. 设计

    包结构的设计是独立于Spring Boot的。因此，它应该由我们项目的要求来施加。

    一种流行的策略是按功能打包，它能增强模块化，并能在子包内实现包的隐私可见性。

    让我们以[PetClinic](https://github.com/spring-projects/spring-petclinic)项目为例。这个项目是由Spring开发人员建立的，以说明他们对普通Spring Boot项目应如何结构的看法。

    它是以逐个包的方式来组织的。因此，我们有主包，org.springframework.samples.petclinic，以及5个子包：

    ```java
    org.springframework.samples.petclinic.model
    org.springframework.samples.petclinic.owner
    org.springframework.samples.petclinic.system
    org.springframework.samples.petclinic.vet
    org.springframework.samples.petclinic.visit
    ```

    它们中的每一个都代表了应用程序的一个领域或一个特征，将高度耦合的类分组在里面，并实现了高度的内聚性。

5. 总结

    在这篇小文章中，我们看了一些在构建Spring Boot项目时需要注意的建议--并了解了如何设计包结构。
