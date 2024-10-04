# [如何在Spring动态自动连接Bean](https://www.baeldung.com/spring-dynamic-autowire)

1. 介绍

    在这个简短的教程中，我们将展示如何在Spring中动态自动连接bean。

    我们将首先提出一个真实世界的用例，其中动态自动接线可能会有所帮助。除此之外，我们将展示如何在春天用两种不同的方式解决它。

2. 动态自动接线用例

    动态自动布线在我们需要动态更改Spring的bean执行逻辑的地方很有帮助。它很实用，特别是在根据一些运行时变量选择要执行的代码的地方。

    为了演示现实世界的用例，让我们创建一个控制世界不同地区的服务器的应用程序。出于这个原因，我们用两种简单方法创建了一个接口：

    ```java
    public interface RegionService {
        boolean isServerActive(int serverId);

        String getISOCountryCode();
    }
    ```

    以及两种实施：

    ```java
    @Service("GBregionService")
    public class GBRegionService implements RegionService {
        @Override
        public boolean isServerActive(int serverId) {
            return false;
        }

        @Override
        public String getISOCountryCode() {
            return "GB";
        }
    }

    @Service("USregionService")
    public class USRegionService implements RegionService {
        @Override
        public boolean isServerActive(int serverId) {
            return true;
        }

        @Override
        public String getISOCountryCode() {
            return "US";
        }
    }
    ```

    假设我们有一个网站，用户可以选择检查服务器在所选区域是否处于活动状态。因此，我们希望有一个服务类，在给定用户的输入时动态更改RegionService接口实现。毫无疑问，这是动态bean自动接线发挥作用的用例。

3. 使用BeanFactory

    BeanFactory是访问Spring bean容器的根接口。特别是，它包含获取特定豆类的有用方法。由于BeanFactory也是Spring Bean，我们可以自动接线并直接在我们的课堂上使用它：

    ```java
    @Service
    public class BeanFactoryDynamicAutowireService {
        private static final String SERVICE_NAME_SUFFIX = "regionService";
        private final BeanFactory beanFactory;

        @Autowired
        public BeanFactoryDynamicAutowireService(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        public boolean isServerActive(String isoCountryCode, int serverId) {
            RegionService service = beanFactory.getBean(getRegionServiceBeanName(isoCountryCode), 
            RegionService.class);

            return service.isServerActive(serverId);
        }

        private String getRegionServiceBeanName(String isoCountryCode) {
            return isoCountryCode + SERVICE_NAME_SUFFIX;
        }
    }
    ```

    我们使用getBean（）方法的过载版本来获取具有给定名称和所需类型的bean。

    虽然这有效，但我们真的更愿意依赖更成语的东西；即使用依赖注入的东西。

4. 使用接口

    为了通过依赖注入解决这个问题，我们将依靠Spring的一个鲜为人知的功能。

    除了标准的单字段自动布线外，Spring还使我们能够将特定接口的实现的所有bean收集到地图中：

    ```java
    @Service
    public class CustomMapFromListDynamicAutowireService {
        private final Map<String, RegionService> servicesByCountryCode;

        @Autowired
        public CustomMapFromListDynamicAutowireService(List<RegionService> regionServices) {
            servicesByCountryCode = regionServices.stream()
                    .collect(Collectors.toMap(RegionService::getISOCountryCode, Function.identity()));
        }

        public boolean isServerActive(String isoCountryCode, int serverId) {
            RegionService service = servicesByCountryCode.get(isoCountryCode);

            return service.isServerActive(serverId);
        }
    }
    ```

    我们在构造函数中创建了一张地图，该地图按其国家代码进行实现。此外，我们可以稍后在方法中使用它来获取特定实现，以检查给定服务器是否在特定区域处于活动状态。

5. 结论

    在这个快速教程中，我们看到了如何使用两种不同的方法在Spring中动态自动连接Bean。
