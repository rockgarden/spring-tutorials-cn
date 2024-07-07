# 车队路线问题

<https://www.optaplanner.org/learn/useCases/vehicleRoutingProblem.html>

车辆路由问题（Vehicle Routing Problem VRP）通过改善访问顺序，优化送货卡车、货运卡车、公共交通工具（公共汽车、出租车和飞机）或技术人员在路上的路线。与人工规划相比，这种路线优化大大减少了驾驶时间和燃料消耗。

![VRP](/pic/vehicleRoutingValueProposition.png)

OptaPlanner非常灵活，可以处理任何VRP变体。它包括了CVRP（电容式车辆路由问题 Capacitated Vehicle Routing Problem）和VRPTW（带时间窗口的车辆路由问题 Vehicle Routing Problem with Time Windows）的例子。

[车辆路由快速入门](https://github.com/kiegroup/optaplanner-quickstarts/tree/stable/use-cases/vehicle-routing)，并根据您的需求进行定制。

什么是OptaPlanner？
OptaPlanner是领先的开源Java™人工智能约束解算器，用于优化车辆路由问题、旅行推销员问题和类似的用例。它涵盖了任何类型的车队调度，如飞机、卡车、公共汽车、出租车、自行车和船舶的路由，无论车辆是运输产品还是乘客，或者司机是否提供服务。

OptaPlanner是一个轻量级、可嵌入的计划引擎。它使日常的Java™程序员能够有效地解决优化问题。它还与其他JVM语言（如Kotlin和Scala）兼容。约束条件适用于普通的领域对象，可以调用现有的代码。不需要将约束条件作为数学方程输入。在引擎下，OptaPlanner将复杂的人工智能优化算法（如Tabu Search、Simulated Annealing、Late Acceptance和 其他 metaheuristics（元启发式算法））与非常高效的得分计算和其他最先进的约束解决技术相结合(state-of-the-art constraint solving techniques)。

OptaPlanner是开放源码软件，在Apache许可证下发布。它是用100%的纯Java™编写的，可在任何JVM上运行，并可在Maven Central资源库中使用。

什么是车辆路由问题？

许多企业每天都面临着一个车辆路由问题：确定最佳订单，用车队将一些物品运送到多个地点。他们面临这个问题，以便为商店补货，为客户提供产品/服务，为设备进行维修。

扩大规模

OptaPlanner通过增量得分计算、附近选择、影子变量以及--当然--先进的构造启发式和元启发式，在车辆路由问题上进行了扩展。

与真实地图的整合

与谷歌地图或OpenStreetMap的整合是直接的：

![整合地图](/pic/integrationWithRealMaps.png)