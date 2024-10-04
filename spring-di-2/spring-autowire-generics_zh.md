# [泛型的Spring Autowiring](https://www.baeldung.com/spring-autowire-generics)

1. 概述

    在本教程中，我们将看到如何通过[泛型](https://www.baeldung.com/java-generics)注入Spring Bean。

2. 在Spring 3.2中自动连接泛型。

    Spring从3.2版本开始支持泛型的注入。

    假设我们有一个名为Vehicle的抽象类和一个名为Car的具体子类：

    - dependencyinjectiontypes.model/Vehicle.java
    - dependencyinjectiontypes.model/Car.java

    假设我们想把一个Vehicle类型的对象列表注入某个处理程序类中：

    ```java
    @Autowired
    private List<Vehicle> vehicles;
    ```

    Spring会将所有的Vehicle实例Bean自动连接到这个列表中。我们如何通过Java或XML配置来实例化这些Bean并不重要。

    我们也可以使用限定词来只获取Vehicle类型的特定Bean。然后我们创建@CarQualifier，并用@Qualifier来注解它：

    dependencyinjectiontypes.annotation/CarQualifier.java

    现在我们可以在我们的列表中使用这个注解，只获得一些特定的车辆：

    ```java
    @Autowired
    @CarQualifier
    private List<Vehicle> vehicles;
    ```

    在这种情况下，我们可以创建几个Vehicle Bean，但Spring将只把那些带有@CarQualifier的Vehicle注入到上面的列表中：

    ```java
    public class CustomConfiguration {
        @Bean
        @CarQualifier
        public Car getMercedes() {
            return new Car("E280", "Mercedes", "Diesel");
        }
    }
    ```

3. Spring 4.0中的Autowiring泛型。

    假设我们有另一个名为Motorcycle的Vehicle子类：

    dependencyinjectiontypes.model/Motorcycle.java

    现在，如果我们只想把汽车Bean注入到我们的列表中，而不是摩托车Bean，我们可以通过使用特定的子类作为类型参数来做到这一点：

    ```java
    @Autowired
    private List<Car> vehicles;
    ```

    自4.0版本以来，Spring允许我们使用通用类型作为限定符，而不需要显式注解。

    在Spring 4.0之前，上面的代码无法与Vehicle的多个子类的Bean一起工作。如果没有明确的限定符，我们会收到一个NonUniqueBeanDefinitionException。

4. ResolvableType

    泛型自动连接功能在幕后借助于ResolvableType类来工作。

    它是在Spring 4.0中引入的，用于封装Java类型，处理对超类型、接口、泛型参数的访问，并最终解析为一个类：

    ```java
    ResolvableType vehiclesType = ResolvableType.forField(getClass().getDeclaredField("vehicles"));
    System.out.println(vehiclesType);

    ResolvableType type = vehiclesType.getGeneric();
    System.out.println(type);

    Class<?> aClass = type.resolve();
    System.out.println(aClass);
    ```

    上述代码的输出将显示相应的简单和通用类型：

    ```log
    java.util.List<com.example.model.Vehicle>
    com.example.model.Vehicle
    class com.example.model.Vehicle
    ```

5. 总结

    通用类型的注入是一个强大的功能，它节省了开发人员分配显式限定符的努力，使代码更干净，更容易理解。
