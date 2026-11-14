package com.baeldung.camellangchain4jdocling;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.model.openai.OpenAiChatModel;

public class CamelDoclingApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelDoclingApplication.class);

    public static void main(String[] args) throws Exception {
        OpenAiChatModel model = OpenAiChatModel.builder()
            .baseUrl(System.getenv().getOrDefault("LLM_BASE_URL", "http://127.0.0.1:8000/v1"))
            .apiKey(System.getenv().getOrDefault("LLM_API_KEY", "mlx"))
            .modelName(System.getenv().getOrDefault("LLM_MODEL", "mlx-community/Qwen3.8-27B-4bit"))
            .timeout(Duration.ofSeconds(Long.parseLong(System.getenv().getOrDefault("LLM_TIMEOUT_SECONDS", "300"))))
            .maxRetries(0)
            .build();

        Main main = new Main();
        AtomicReference<Throwable> conversionFailure = new AtomicReference<>();
        main.bind("chatModel", model);
        main.configure().addRoutesBuilder(new ConversionRoute(exception -> {
            if (conversionFailure.compareAndSet(null, exception)) {
                LOGGER.error("Document processing failed; stopping Camel", exception);
                CompletableFuture.runAsync(() -> {
                    try {
                        main.stop();
                    } catch (Exception stopException) {
                        LOGGER.error("Failed to stop Camel after a document processing failure", stopException);
                    }
                });
            }
        }));
        main.configure().addRoutesBuilder(new QuestionAndAnswerRoute());

        try {
            main.run(args);
        } catch (Exception exception) {
            LOGGER.error("Camel application failed to start or run", exception);
            throw exception;
        } finally {
            main.stop();
        }

        Throwable failure = conversionFailure.get();
        if (failure != null) {
            throw new IllegalStateException("Document processing failed", failure);
        }
    }
}
