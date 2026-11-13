package com.baeldung.langchain;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static java.time.Duration.ofSeconds;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.chain.ConversationalRetrievalChain;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;

public class ChainWithDocumentLiveTest {

    private static final Logger logger = LoggerFactory.getLogger(ChainWithDocumentLiveTest.class);

    @Test
    public void givenChainWithDocument_whenPrompted_thenValidResponse() {
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(500, 0))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        Document document = loadDocument(Paths.get("src/test/resources/example-files/simpson's_adventures.txt"));
        ingestor.ingest(document);

        ChatModel chatModel = OpenAiChatModel.builder()
            .apiKey(Constants.OPENAI_API_KEY)
            .timeout(ofSeconds(60))
            .build();

        ConversationalRetrievalChain chain = ConversationalRetrievalChain.builder()
            .chatModel(chatModel)
            .contentRetriever(EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .build())
            .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
            .build();

        String answer = chain.execute("Who is Simpson?");

        logger.info(answer);
        assertNotNull(answer);
    }

}
