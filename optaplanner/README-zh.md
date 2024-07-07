# OptaPlanner

本模块包含关于OptaPlanner的文章。

## OptaPlanner指南

1. OptaPlanner简介

    在本教程中，我们将了解一个名为[OptaPlanner](https://www.optaplanner.org/)的Java约束条件满足求解器。

    OptaPlanner使用一套算法解决规划问题，设置最少。

    尽管对算法的理解可能会提供有用的细节，但由于框架为我们完成了艰苦的工作。

2. Maven依赖性

    首先，我们要为OptaPlanner添加一个Maven依赖项：

    ```xml
    <dependency>
        <groupId>org.optaplanner</groupId>
        <artifactId>optaplanner-core</artifactId>
        <version>8.24.0.Final</version>
    </dependency>
    ```

    我们从Maven Central资源库中找到OptaPlanner的最新版本。

3. Problem/Solution Class

    要解决一个问题，我们当然需要一个具体的问题作为例子。

    由于在平衡教室、时间和教师等资源方面存在困难，讲座时间安排就是一个合适的例子。

    1. 课程表（CourseSchedule）

        CourseSchedule包含了我们的问题变量和规划实体的组合，因此它是一个解决方案类。因此，我们使用多个注释来配置它。

        让我们分别仔细看一下：

        ```java
        @PlanningSolution
        public class CourseSchedule {
            @ValueRangeProvider(id = "availableRooms")
            @ProblemFactCollectionProperty
            private List<Integer> roomList;
            @ValueRangeProvider(id = "availablePeriods")
            @ProblemFactCollectionProperty
            private List<Integer> periodList;
            @ProblemFactCollectionProperty
            private List<Lecture> lectureList;
            @PlanningScore
            private HardSoftScore score;
            ...
        }
        ```

        PlanningSolution注解告诉OptaPlanner，这个类包含了包含一个解决方案的数据。

        OptaPlanner希望有这些最小的组件：规划实体(planning entity)、问题事实(problem facts)和分数(score)。

    2. 讲座（Problem Facts）

        Lecture，一个POJO，看起来像：optaplanner/Lecture.java

        我们使用Lecture类作为规划实体，所以我们在CourseSchedule的getter上添加另一个注释：

        ```java
        @PlanningEntityCollectionProperty
        public List<Lecture> getLectureList() {
            return lectureList;
        }
        ```

        Lecture类中，我们的规划实体包含被设置的约束。

        PlanningVariable注解和valueRangeProviderRef注解将约束条件与问题事实联系起来。

        这些约束值以后将在所有规划实体中进行评分。

    3. 问题事实（Problem Facts）

        roomNumber和period这两个变量作为约束条件的作用类似于彼此。

        OptaPlanner将解决方案作为使用这些变量的逻辑结果来评分。

        CourseSchedule类中，我们为这两个getter方法添加注解：

        ```java
        @ValueRangeProvider(id = "availableRooms")
        @ProblemFactCollectionProperty
        public List<Integer> getRoomList() {
            return roomList;
        }
        @ValueRangeProvider(id = "availablePeriods")
        @ProblemFactCollectionProperty
        public List<Integer> getPeriodList() {
            return periodList;
        }
        ```

        这些列表是用于讲座领域的所有可能的值。

        OptaPlanner将它们填充到整个搜索空间的所有解决方案中。

        最后，它为每个解决方案设置一个分数，所以我们需要一个字段来存储这个分数：

        ```java
        @PlanningScore
        public HardSoftScore getScore() {
            return score;
        }
        ```

        没有分数，OptaPlanner就不能找到最佳解决方案，因此前面强调了重要性。

4. 计分（Scoring）

    与我们到目前为止所看到的不同，评分类需要更多的自定义代码。

    这是因为分数计算器是针对问题和领域模型的。

    1. 自定义Java

        我们用一个简单的分数计算来解决这个问题（虽然看起来不是这样）： ScoreCalculator.java

        如果我们仔细看代码，重要的部分会变得更加清晰。我们在循环中计算分数，因为`List<Lecture>`包含房间和时期的特定非唯一组合。

        HashSet被用来保存一个唯一的键（字符串），这样我们就可以惩罚同一房间和时期的重复讲座了。

        结果是，我们收到了唯一的房间和时段的集合。

5. 测试

    我们配置了我们的解决方案、求解器和问题类。让我们来测试它吧

    1. 设置我们的测试

        首先，我们做一些设置：

        ```java
        SolverFactory<CourseSchedule> solverFactory = SolverFactory.create(new SolverConfig() 
                                                            .withSolutionClass(CourseSchedule.class)
                                                            .withEntityClasses(Lecture.class)
                                                            .withEasyScoreCalculatorClass(ScoreCalculator.class)
                                                            .withTerminationSpentLimit(Duration.ofSeconds(1))); 
        solver = solverFactory.buildSolver();
        unsolvedCourseSchedule = new CourseSchedule();
        ```

        其次，我们将数据填充到规划实体集合和问题事实列表对象中。

    2. 测试执行和验证

        最后，我们通过调用solve来测试它。

        ```java
        CourseSchedule solvedCourseSchedule = solver.solve(unsolvedCourseSchedule);
        assertNotNull(solvedCourseSchedule.getScore());
        assertEquals(-4, solvedCourseSchedule.getScore().getHardScore());
        ```

        我们检查solvedCourseSchedule是否有一个分数，它告诉我们有一个 "optimal" 解决方案。

        作为奖励，我们在CourseSchedule类中创建了一个打印方法，它将显示我们的优化解决方案：

        ```java
        public void printCourseSchedule() {
            lectureList.stream()
            .map(c -> "Lecture in Room "
                + c.getRoomNumber().toString() 
                + " during Period " + c.getPeriod().toString())
            .forEach(k -> logger.info(k));
        }
        ```

        这个方法显示：

        ```log
        Lecture in Room 1 during Period 1
        Lecture in Room 2 during Period 1
        Lecture in Room 1 during Period 2
        Lecture in Room 2 during Period 2
        Lecture in Room 1 during Period 3
        Lecture in Room 2 during Period 3
        Lecture in Room 1 during Period 1
        Lecture in Room 1 during Period 1
        Lecture in Room 1 during Period 1
        Lecture in Room 1 during Period 1
        ```

        注意最后三个条目是如何重复的。发生这种情况是因为我们的问题没有最佳解决方案。我们选择了三个时段，两个教室和十个讲座。

        由于这些固定的资源，只有六个可能的讲座。至少这个答案告诉用户，没有足够的房间或时段来容纳所有的讲座。

6. 额外功能

    我们创建的OptaPlanner的例子是一个简单的例子，然而，该框架为更多不同的用例增加了功能。我们可能想实现或改变我们的优化算法，然后指定框架来使用它。

    由于最近Java的多线程能力的改进，OptaPlanner也让开发者有能力使用多线程的多种实现，如fork和join，增量求解和多租户。

    更多信息请参考[文档](https://docs.optaplanner.org/7.9.0.Final/optaplanner-docs/html_single/index.html#multithreadedSolving)。

7. 总结

    OptaPlanner框架为开发者提供了一个强大的工具来解决约束条件的满足问题，如调度和资源分配。

    OptaPlanner提供了最小的JVM资源使用量，以及与Jakarta EE的集成。作者将继续支持该框架，Red Hat也将其作为其商业规则管理套件的一部分。

## 相关文章

- [ ] [A Guide to OptaPlanner](https://www.baeldung.com/opta-planner)

## Code

像往常一样，代码可以在[Github](https://github.com/eugenp/tutorials/tree/master/optaplanner)上找到。
