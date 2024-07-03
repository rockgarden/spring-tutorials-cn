# [OpenNLP 介绍](https://www.baeldung.com/apache-open-nlp)

1. 概述

    Apache OpenNLP是一个开源的自然语言处理Java库。

    它有一个用于命名实体识别、句子检测、POS标记和标记化等使用案例的API。

    在本教程中，我们将看看如何在不同的用例中使用这个API。

2. Maven设置

    首先，我们需要在pom.xml中添加主要依赖项：

    ```xml
    <dependency>
        <groupId>org.apache.opennlp</groupId>
        <artifactId>opennlp-tools</artifactId>
        <version>1.8.4</version>
    </dependency>
    ```

    最新的稳定版本可以在Maven中心找到。

    有些用例需要经过训练的模型。你可以在[这里](http://opennlp.sourceforge.net/models-1.5/)下载预定义的模型，以及关于这些模型的[详细信息](http://opennlp.apache.org/models.html)。

3. 句子检测

    让我们先来了解一下什么是句子。

    句子检测是指识别一个句子的开始和结束，这通常取决于手头的语言。这也被称为 "句子边界歧义"（Sentence Boundary Disambiguation SBD）。

    在某些情况下，由于句号字符的模糊性，句子检测是相当具有挑战性的。句号通常表示一个句子的结束，但也可以出现在电子邮件地址、缩写、小数点和其他很多地方。

    与大多数NLP任务一样，对于句子检测，我们需要一个训练有素的模型作为输入，我们希望这个模型位于/resources文件夹中。

    为了实现句子检测，我们加载模型并将其传递给SentenceDetectorME的一个实例。然后，我们简单地将一个文本传入sentDetect()方法，在句子边界处将其分割：

    `SentenceDetectionUnitTest.givenEnglishModel_whenDetect_thenSentencesAreDetected()`

    注意：后缀 "ME" 在Apache OpenNLP的许多类名中使用，代表一种基于 "最大熵(Maximum Entropy)" 的算法。

4. 符号化 Tokenizing

    现在，我们可以将一个文本语料库划分为句子，我们可以开始更详细地分析一个句子。

    标记化的目的是将一个句子分成更小的部分，称为标记。通常情况下，这些标记是单词、数字或标点符号。

    在OpenNLP中，有三种类型的标记化工具。

    1. 使用TokenizerME

        在这种情况下，我们首先需要加载模型。我们可以从[这里](http://opennlp.sourceforge.net/models-1.5/)下载模型文件，把它放在/resources文件夹中，然后从那里加载它。

        接下来，我们将使用加载的模型创建一个TokenizerME的实例，并使用tokenize()方法对任何字符串进行标记化处理：

        `TokenizerUnitTest.givenEnglishModel_whenTokenize_thenTokensAreDetected()`

        正如我们所看到的，标记器已经将所有单词和句号字符识别为独立的标记。这个标记器也可以用于自定义训练的模型。

    2. 白空间标记器 WhitespaceTokenizer

        顾名思义，这个标记器只是用空白字符作为分隔符将句子分割成标记：

        TokenizerUnitTest.givenWhitespaceTokenizer_whenTokenize_thenTokensAreDetected()

        我们可以看到，该句子已经被空格分割，因此我们得到了 "Resource."(末尾有句号）作为一个标记，而不是 "Resource" 一词和句号的两个不同标记。

    3. 简单标记器 SimpleTokenizer

        这个标记器比WhitespaceTokenizer更复杂一点，它将句子分成单词、数字和标点符号。它是默认行为，不需要任何模型：

        TokenizerUnitTest.givenSimpleTokenizer_whenTokenize_thenTokensAreDetected()

5. 命名实体(Named Entity)识别

    现在我们已经了解了标记化，让我们来看看基于成功标记化的第一个用例：命名实体识别（named entity recognition NER）。

    命名实体识别的目标是在给定的文本中找到像人、地点、组织和其他命名事物这样的命名实体。

    OpenNLP为人名、日期和时间、地点和组织使用预定义的模型。我们需要使用TokenNameFinderModel加载模型，并将其传递给NameFinderME的一个实例。然后我们可以使用find()方法在给定的文本中找到命名的实体：

    `NamedEntityRecognitionUnitTest.givenEnglishPersonModel_whenNER_thenPersonsAreDetected()`

    正如我们在断言中所看到的，结果是一个Span对象的列表，其中包含构成文本中命名实体的标记的开始和结束索引。

6. 语音部分(Part-of-Speech)标记

    另一个需要标记列表作为输入的用例是部分语音标记。

    语音部分标签（part-of-speech POS）可以识别一个词的类型。OpenNLP对不同的语音部分使用以下标签：

    - NN - 名词，单数或总数
    - DT - 定语从句
    - VB - 动词，基本形式
    - VBD - 动词，过去式
    - VBZ - 动词，第三人称单数现在时
    - IN - 介词或从属连词
    - NNP - 专有名词，单数
    - TO - "to" 这个词
    - JJ - 形容词

    这些都是Penn Tree Bank中定义的相同标签。完整的列表请参考这个[列表](https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html)。

    与NER的例子类似，我们加载适当的模型，然后在一组标记上使用POSTaggerME及其方法tag()来标记该句子：

    POSTaggerUnitTest.givenPOSModel_whenPOSTagging_thenPOSAreDetected()

    tag()方法将标记映射为POS标记的列表。本例中的结果是：

    - "John" - NNP (专有名词)
    - "has" - VBZ (动词)
    - "a" - DT (定语)
    - "sister" - NN(名词)
    - "named"--VBZ（动词）。
    - "Penny" - NNP (专有名词)
    - "." - 期间

7. 语义化 Lemmatization

    现在我们有了一个句子中的标记的语音部分信息，我们可以进一步分析文本了。

    词法化是将可能具有时态、性别、语气或其他信息的词的形式映射到该词的基本形式的过程，也称为 "词法"。

    Lemmatization is the process of mapping a word form that can have a tense, gender, mood or other information to the base form of the word – also called its “lemma”.

    词法分析器将一个标记和它的语料部分标签作为输入，并返回该词的词法。因此，在Lemmatization之前，句子应该通过一个标记器和POS标签器。

    Apache OpenNLP提供两种类型的词法处理：

    - Statistical 统计型--需要一个使用训练数据建立的词法分析模型来寻找一个给定单词的词法。
    - Dictionary-based 基于字典--需要一个字典，其中包含一个词、POS标签和相应词组的所有有效组合。

    对于统计词法，我们需要训练一个模型，而对于字典词法，我们只需要一个像这样的[字典文件](https://raw.githubusercontent.com/richardwilly98/elasticsearch-opennlp-auto-tagging/master/src/main/resources/models/en-lemmatizer.dict)。

    让我们看看一个使用字典文件的代码例子：

    `LemmetizerUnitTest.givenEnglishDictionary_whenLemmatize_thenLemmasAreDetected()`

    正如我们所看到的，我们得到了每个标记的词条。"O" 表示无法确定词表，因为该词是一个专有名词。因此，我们没有 "John" 和 "Penny" 的词表。

    但我们已经确定了句子中其他词的词表：

    - has - have
    - a - a
    - sister - sister
    - named - name

8. 分块 Chunking

    语篇(Part-of-speech)信息在分块中也是必不可少的，即把句子分成有语法意义的词组，如名词组或动词组。

    与之前类似，我们对一个句子进行标记，并在调用chunk()方法之前对标记使用语料部分标记：

    ChunkerUnitTest.givenChunkerModel_whenChunk_thenChunksAreDetected()

    正如我们所看到的，我们从chunker中为每个标记得到一个输出。"B" 代表一个分块的开始，"I" 代表分块的继续，"O" 代表没有分块。

    对我们的例子的输出进行解析，我们得到6个块：

    "He" - 名词短语
    "reckons"--动词短语
    "the current account deficit" - 名词短语
    "will narrow"--动词短语
    "to"--介词短语
    "only 8 billion" - 名词短语

9. 语言检测

    除了已经讨论过的用例之外，OpenNLP还提供了一个语言检测API，可以识别某个文本的语言。

    对于语言检测，我们需要一个训练数据文件。这样的文件包含有某种语言的句子行。每一行都被标记为正确的语言，以提供给机器学习算法的输入。

    一个用于语言检测的训练数据文件样本可以在这里[下载](https://github.com/apache/opennlp/blob/master/opennlp-tools/src/test/resources/opennlp/tools/doccat/DoccatSample.txt)。

    我们可以将训练数据文件加载到LanguageDetectorSampleStream中，定义一些训练数据参数，创建一个模型，然后使用该模型来检测文本的语言：

    LanguageDetectorAndTrainingDataUnitTest.givenLanguageDictionary_whenLanguageDetect_thenLanguageIsDetected()

    结果是一个最可能的语言列表，以及一个信心分数。

    而且，通过丰富的模型，我们可以通过这种类型的检测达到非常高的准确性。

10. 总结

我们在这里探索了很多，从OpenNLP的有趣的能力。我们专注于一些有趣的功能来执行NLP任务，如词法、POS标记、标记化、句子检测、语言检测等等。