package com.baeldung.langchain;

import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;

public class ChatWithMemoryLiveTest {

    private static final Logger logger = LoggerFactory.getLogger(ChatWithMemoryLiveTest.class);

    @Test
    public void givenMemory_whenPrompted_thenValidResponse() {
        ChatModel model = OpenAiChatModel.builder()
            .apiKey(Constants.OPENAI_API_KEY)
            .modelName(GPT_4_O_MINI)
            .build();
        ChatMemory chatMemory = TokenWindowChatMemory.withMaxTokens(300, new OpenAiTokenCountEstimator(GPT_4_O_MINI));

        chatMemory.add(userMessage("Hello, my name is Kumar"));
        AiMessage answer = model.chat(chatMemory.messages())
            .aiMessage();
        logger.info(answer.text());
        chatMemory.add(answer);
        assertNotNull(answer.text());

        chatMemory.add(userMessage("What is my name?"));
        AiMessage answerWithName = model.chat(chatMemory.messages())
            .aiMessage();
        logger.info(answerWithName.text());
        chatMemory.add(answerWithName);
        assertThat(answerWithName.text()).contains("Kumar");
    }

}
