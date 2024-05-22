# 在Kubernetes中运行Cron工作

1. 简介

    在本教程中，我们将了解如何在Kubernetes中运行Cron作业。

2. 什么是Cron作业？

    就背景而言，cron job指的是任何按计划重复执行的任务。Unix和大多数相关的操作系统提供一些cron job功能。

    cron作业的一个典型用例是在重复的基础上自动执行重要任务。例如：

    - 清理磁盘空间
    - 备份文件或目录
    - 生成指标或报告
    Cron作业按时间表运行。使用一个[标准的符号](https://www.baeldung.com/cron-expressions)，我们可以定义一个广泛的时间表来执行一个作业：

    - 每天晚上10:00
    - 每个月的第一天，早上6：00
    - 每天上午8:00和下午6:00
    - 每周二上午7:00
3. 在Kubernetes中定义Cron工作

    从1.21版本开始，Kubernetes为cron作业提供了一流的支持。首先，让我们看看如何定义一个cron作业，以及如何设置其时间表。

    1. 定义Cron作业

        Kubernetes中的Cron作业与其他工作负载（如部署或守护程序集）类似。事实上，定义Cron作业的YAML看起来非常相似：

        ```yaml
        apiVersion: batch/v1
        kind: CronJob
        metadata:
        name: cleanup-job
        spec:
        schedule: "0 2 * * *"
        concurrencyPolicy: Allow
        suspend: false
        successfulJobsHistoryLimit: 10
        failedJobsHistoryLimit: 3
        startingDeadlineSeconds: 60
        jobTemplate:
            spec:
            template:
                spec:
                containers:
                - name: cleanup-job
                    image: busybox:1.28
                    imagePullPolicy: IfNotPresent
                    command:
                    - /bin/rm
                    - -f
                    - /tmp/*
                restartPolicy: OnFailure
        ```

        上面的YAML定义了一个cron工作，它将在每天凌晨2:00运行，并清理临时目录中的文件。

        如前所述，cron作业的YAML与Kubernetes中的其他工作负载几乎相同。事实上，配置的jobTemplate部分与部署、复制集和其他类型的工作负载是相同的。

        主要区别在于，cron工作规范包含额外的字段，用于定义cron工作执行行为：

        - schedule： 必需的字段，使用标准的cron语法指定cron工作时间表。
        - concurrencyPolicy： 可选的字段，指定如何处理并发作业。默认值是 "允许"，这意味着一个以上的作业可以同时运行。其他可能的值是Forbid（不允许）或Replace（新作业将取代任何正在运行的作业）。
        - suspend： 可选的字段，指定一个作业的未来执行是否应该被跳过。默认值为false。
        - successfulJobsHistoryLimit: 可选字段，指定在历史中跟踪多少次成功执行。默认值是3。
        - failedJobsHistoryLimit：可选字段，指定在历史中追踪多少次失败的执行。默认值为1。
        - startingDeadlineSeconds（起始期限）： 可选字段，指定在作业被视为失败之前允许错过其预定的开始时间的秒数。默认情况是不执行任何这样的期限。

        请注意，其中只有一个字段，即日程表，是必须的。我们将在后面仔细研究这个字段。

    2. 管理Cron作业

        现在我们已经看到了如何定义cron工作，让我们看看如何在Kubernetes中管理它们。

        首先，我们假设我们会把cron job的YAML定义放到一个名为cronjob.yaml的文件中。然后我们可以使用kubelet命令创建cron job：

        `kubelet create -f /path/to/cronjob.yaml`

        此外，我们可以用以下命令列出所有的cron job：

        ```bash
        kubectl get cronjob
        NAME          SCHEDULE    SUSPEND   ACTIVE   LAST SCHEDULE   AGE
        cleanup-job   * 2 * * *   False     0        15s             32s
        ```

        我们还可以使用describe命令来查看特定cron job的细节，包括运行历史：

        ```bash
        kubectl describe cronjob hello
        Name:                          cleanup-job
        Namespace:                     default
        Labels:                        <none>
        Annotations:                   <none>
        Schedule:                      * 2 * * *
        Concurrency Policy:            Allow
        Suspend:                       False
        Successful Job History Limit:  3
        Failed Job History Limit:      1
        Starting Deadline Seconds:     <unset>
        Selector:                      <unset>
        Parallelism:                   <unset>
        Completions:                   <unset>
        Pod Template:
        Labels:  <none>
        Containers:
        hello:
            Image:      busybox:1.28
            Port:       <none>
            Host Port:  <none>
            Command:
            /bin/rm
            -f
            /tmp/*
            Environment:     <none>
            Mounts:          <none>
        Volumes:           <none>
        Last Schedule Time:  Mon, 30 May 2022 02:00:00 -0600
        Active Jobs:         <none>
        Events:
        Type    Reason            Age                   From                Message
        ----    ------            ----                  ----                -------
        Normal  SuccessfulCreate  16m                   cronjob-controller  Created job cleanup-job-27565242
        Normal  SawCompletedJob   16m                   cronjob-controller  Saw completed job: cleanup-job-27565242, status: Complete
        ```

        最后，当我们不再需要一个cron job时，我们可以用下面的命令来删除它：

        `kubectl delete cronjob cleanup-job`

    3. Cron计划语法

        cron工作计划的语法包含五个参数，用空格隔开。每个参数可以是星号，也可以是数字。

        这些参数的顺序与传统的Unix cron语法相同。这些字段，从左到右，有以下含义和可能的值：

        - Minute (0 – 59)
        - Hour (0 – 23)
        - Day of Month (1 – 31)
        - Month (1 – 12)
        - Day of Week (0 – 6)
        请注意，对于月日参数，有些系统把0当作星期天，有些系统把它当作星期一。

        除了上面确定的可能值外，任何参数也可以是星号，意味着它适用于该字段的所有可能值。

        让我们看一些例子。首先，我们可以安排一个作业在每天上午8:00运行：

        `0 8 * * *`

        让我们看看计划参数，它将在每周二下午5点运行一个作业：

        `0 17 * * 2`

        最后，让我们看看如何在每个月的第15天，每隔一小时的30分钟运行一项工作：

        `30 0,2,4,6,8,10,12,14,16,18,20,22 15 * *`

        请注意，上述时间表也可以用跳过的语法来简化：

        `30 0-23/2 15 * *`

    4. 特殊的Cron作业条目

        除了标准的时间表语法外，cron作业还可以使用一些特殊的标识符来指定其时间表：

        - @yearly / @annually - 在每年的1月1日午夜运行一个任务
        - @monthly - 在每个月的第一天的午夜运行一个任务
        - @weekly - 在每周的周日午夜运行一项工作
        - @daily / @midnight - 在每天的午夜运行一项工作
        - @hourly - 在每天的每个小时开始时运行一项工作
    5. Cron任务的时间区间

        默认情况下，所有Kubernetes cron作业都在控制管理器的时区运行。在某些情况下，可以使用变量CRON_TZ或TZ来指定一个特定的时区。然而，这并不被官方支持。这些变量被认为是内部实施细节，因此，可能会在没有警告的情况下发生变化。

        从Kubernetes 1.24版本开始，可以将时区指定为cron job规格的一部分：

        ```yml
        spec:
        schedule: "0 2 * * *"
        timeZone: "GMT"
        ```

        timeZone字段可以是任何[有效的时区标识符](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)。

        由于这个功能仍然是试验性的，我们必须在使用它之前先启用CronJobTimeZone功能门。

4. 总结

    Cron作业是一种重复执行重要系统任务的好方法。

    在这篇文章中，我们研究了如何在Kubernetes集群中利用Cron作业。首先，我们看到了定义cron job所需的YAML，以及如何使用kubectl命令来管理其生命周期。最后，我们看了定义其时间表的各种方法以及如何处理时区。  

## 相关文章

- [ ] [Running Cron Jobs in Kubernetes](https://www.baeldung.com/ops/kubernetes-run-cron-jobs)
