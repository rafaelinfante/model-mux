package net.rafaelinfante.modelmux.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

class MockChatModelTest {

    private final MockChatModel model = new MockChatModel("mux-mini", "concise", 0, 0, new TokenEstimator());

    @Test
    void respondsWithContentAndUsage() {
        ChatResponse response = model.call(new Prompt("how does a circuit breaker work"));

        assertThat(response.getResult().getOutput().getText()).isNotBlank();
        assertThat(response.getMetadata().getUsage().getPromptTokens()).isPositive();
        assertThat(response.getMetadata().getUsage().getCompletionTokens()).isPositive();
    }

    @Test
    void recognisesPrSummaryPromptAndReturnsJson() {
        ChatResponse response =
                model.call(new Prompt("Summarize the following git diff\ndiff --git a/A.java b/A.java"));

        assertThat(response.getResult().getOutput().getText()).contains("\"summary\"", "\"risks\"");
    }

    @Test
    void streamsMultipleChunks() {
        List<ChatResponse> chunks = model.stream(new Prompt("stream a few words please")).collectList().block();

        assertThat(chunks).isNotEmpty();
    }
}
