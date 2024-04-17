# [使用 ElasticSearch 进行全文搜索的快速介绍](https://www.baeldung.com/elasticsearch-full-text-search-rest-api)

1. 概述

    全文搜索针对文档进行查询和语言搜索。它包括单个或多个单词或短语，并返回符合搜索条件的文档。

    ElasticSearch 是一个基于 Apache Lucene 的搜索引擎，Lucene 是一个免费的开源信息检索软件库。它提供了一个分布式全文搜索引擎，具有 HTTP Web 界面和无模式 JSON 文档。

    本文将介绍 ElasticSearch REST API，并演示仅使用 HTTP 请求进行的基本操作。

2. 安装

    要在计算机上安装 ElasticSearch，请参考[官方安装指南](https://www.elastic.co/guide/en/elasticsearch/reference/current/install-elasticsearch.html)。

    RESTfull API 运行于 9200 端口。让我们使用下面的 curl 命令来测试它是否正常运行：

    `curl -XGET 'http://localhost:9200/'`

    如果观察到以下响应，说明实例运行正常：

    ```json
    {
    "name": "NaIlQWU",
    "cluster_name": "elasticsearch",
    "cluster_uuid": "enkBkWqqQrS0vp_NXmjQMQ",
    "version": {
        "number": "5.1.2",
        "build_hash": "c8c4c16",
        "build_date": "2017-01-11T20:18:39.146Z",
        "build_snapshot": false,
        "lucene_version": "6.3.0"
    },
    "tagline": "You Know, for Search"
    }
    ```

3. 索引文档

    ElasticSearch 以文档为导向。它存储文档并编制索引。索引创建或更新文档。建立索引后，你可以搜索、排序和过滤完整的文档，而不是列数据行。这是一种根本不同的数据思维方式，也是 ElasticSearch 可以执行复杂的全文搜索的原因之一。

    文档表示为 JSON 对象。大多数编程语言都支持 JSON 序列化，它已成为 NoSQL 运动使用的标准格式。它简单、简洁、易读。

    我们将使用以下随机条目进行全文搜索：

    ```json
    {
    "title": "He went",
    "random_text": "He went such dare good fact. The small own seven saved man age."
    }

    {
    "title": "He oppose",
    "random_text": 
        "He oppose at thrown desire of no. \
        Announcing impression unaffected day his are unreserved indulgence."
    }

    {
    "title": "Repulsive questions",
    "random_text": "Repulsive questions contented him few extensive supported."
    }

    {
    "title": "Old education",
    "random_text": "Old education him departure any arranging one prevailed."
    }
    ```

    在为文档建立索引之前，我们需要决定将其存储在哪里。可以有多个索引，而索引又包含多个类型。这些类型包含多个文档，每个文档有多个字段。

    我们将使用以下模式来存储文档：

    - text： 索引名称。
    - article： 类型名称。
    - id： 该示例文本条目的 ID。

    要添加文档，我们将运行以下命令：

    ```bash
    curl -XPUT 'localhost:9200/text/article/1?pretty'
    -H 'Content-Type: application/json' -d '
    {
    "title": "He went",
    "random_text": 
        "He went such dare good fact. The small own seven saved man age."
    }'
    ```

    这里我们使用的是 id=1，我们可以使用相同的命令添加其他条目，并递增 id。

4. 检索文档

    添加完所有文档后，我们可以使用下面的命令查看集群中有多少文档：

    ```bash
    curl -XGET 'http://localhost:9200/_count?pretty' -d '
    {
    "query": {
        "match_all": {}
    }
    }'
    ```

    此外，我们还可以通过以下命令使用文档 ID 获取文档：

    `curl -XGET 'localhost:9200/text/article/1?pretty'`

    我们应该能从弹性搜索中得到以下答案：

    ```json
    {
    "_index": "text",
    "_type": "article",
    "_id": "1",
    "_version": 1,
    "found": true,
    "_source": {
        "title": "He went",
        "random_text": 
        "He went such dare good fact. The small own seven saved man age."
    }
    }
    ```

    我们可以看到，这个答案与使用 ID 1 添加的条目相对应。

5. 查询文档

    好了，让我们用下面的命令执行全文检索：

    ```bash
    curl -XGET 'localhost:9200/text/article/_search?pretty' 
    -H 'Content-Type: application/json' -d '
    {
    "query": {
        "match": {
        "random_text": "him departure"
        }
    }
    }'
    ```

    结果如下：

    ```json
    {
    "took": 32,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
    },
    "hits": {
        "total": 2,
        "max_score": 1.4513469,
        "hits": [
        {
            "_index": "text",
            "_type": "article",
            "_id": "4",
            "_score": 1.4513469,
            "_source": {
            "title": "Old education",
            "random_text": "Old education him departure any arranging one prevailed."
            }
        },
        {
            "_index": "text",
            "_type": "article",
            "_id": "3",
            "_score": 0.28582606,
            "_source": {
            "title": "Repulsive questions",
            "random_text": "Repulsive questions contented him few extensive supported."
            }
        }
        ]
    }
    }
    ```

    正如我们所看到的，我们正在搜索 "him departure"，我们得到了两个得分不同的结果。第一个结果很明显，因为文本中包含了已执行的搜索，我们可以看到得分是 1.4513469。

    第二个结果是因为目标文档中包含了 "him" 这个词。

    默认情况下，ElasticSearch 会根据相关性得分对匹配结果进行排序，即根据每个文档与查询的匹配程度进行排序。请注意，第二个结果的得分相对于第一个结果来说较小，这表明相关性较低。

6. 模糊搜索

    模糊匹配将 "fuzzily" 相似的两个词视为同一个词。首先，我们需要定义模糊的含义。

    Elasticsearch 支持的最大编辑距离是 2，用模糊度参数指定。 模糊度参数可以设置为 "AUTO"，这样会产生以下最大编辑距离：

    - 0 用于一个或两个字符的字符串
    - 1 用于三个、四个或五个字符的字符串
    - 2 用于超过五个字符的字符串

    您可能会发现，编辑距离为 2 的结果似乎并不相关。

    最大模糊度为 1 时，可能会得到更好的结果和性能。 距离指的是[列文士坦距离](https://en.wikipedia.org/wiki/Levenshtein_distance)，它是衡量两个序列之间差异的字符串度量。非正式地讲，两个单词之间的莱文斯坦距离是单字符编辑的最小数量。

    好了，让我们进行模糊搜索：

    ```bash
    curl -XGET 'localhost:9200/text/article/_search?pretty' -H 'Content-Type: application/json' -d' 
    { 
    "query": 
    { 
        "match": 
        { 
        "random_text": 
        {
            "query": "him departure",
            "fuzziness": "2"
        }
        } 
    } 
    }'
    ```

    结果如下：

    ```json
    {
    "took": 88,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
    },
    "hits": {
        "total": 4,
        "max_score": 1.5834423,
        "hits": [
        {
            "_index": "text",
            "_type": "article",
            "_id": "4",
            "_score": 1.4513469,
            "_source": {
            "title": "Old education",
            "random_text": "Old education him departure any arranging one prevailed."
            }
        },
        {
            "_index": "text",
            "_type": "article",
            "_id": "2",
            "_score": 0.41093433,
            "_source": {
            "title": "He oppose",
            "random_text":
                "He oppose at thrown desire of no. 
                \ Announcing impression unaffected day his are unreserved indulgence."
            }
        },
        {
            "_index": "text",
            "_type": "article",
            "_id": "3",
            "_score": 0.2876821,
            "_source": {
            "title": "Repulsive questions",
            "random_text": "Repulsive questions contented him few extensive supported."
            }
        },
        {
            "_index": "text",
            "_type": "article",
            "_id": "1",
            "_score": 0.0,
            "_source": {
            "title": "He went",
            "random_text": "He went such dare good fact. The small own seven saved man age."
            }
        }
        ]
    }
    }'
    ```

    正如我们所看到的，模糊性为我们提供了更多的结果。

    我们需要谨慎使用模糊性，因为它往往会检索到看起来不相关的结果。

7. 总结

    在这篇快速教程中，我们主要介绍了通过 REST API 直接为全文搜索编制文档索引和查询 Elasticsearch。

    当然，在需要的时候，我们也会为多种编程语言提供 API，但 API 仍然非常方便，而且与语言无关。
