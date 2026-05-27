package net.rafaelinfante.modelmux.provider;

import java.time.Duration;
import java.util.List;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

/**
 * A deterministic Spring AI {@link ChatModel} so the gateway runs end-to-end with no provider keys.
 * It recognises the three skill prompts and returns realistic, parseable output for each; everything
 * else gets a short persona-flavoured answer. Latency is simulated so the playground's streaming and
 * compare views feel real and the virtual-thread fan-out has something to overlap. Responses are
 * canned on purpose — with real keys the actual model does the work and this class is never used.
 */
public class MockChatModel implements ChatModel {

    private final String model;
    private final String persona;
    private final long baseLatencyMs;
    private final long perTokenLatencyMicros;
    private final TokenEstimator tokenEstimator;

    public MockChatModel(
            String model,
            String persona,
            long baseLatencyMs,
            long perTokenLatencyMicros,
            TokenEstimator tokenEstimator) {
        this.model = model;
        this.persona = persona;
        this.baseLatencyMs = baseLatencyMs;
        this.perTokenLatencyMicros = perTokenLatencyMicros;
        this.tokenEstimator = tokenEstimator;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        String content = generate(userText(prompt));
        sleep(baseLatencyMs + (perTokenLatencyMicros * tokenEstimator.estimate(content)) / 1000);
        return response(content, tokenEstimator.estimate(prompt.getContents()), tokenEstimator.estimate(content));
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        String content = generate(userText(prompt));
        int promptTokens = tokenEstimator.estimate(prompt.getContents());
        int completionTokens = tokenEstimator.estimate(content);
        List<String> parts = chunks(content);
        long perChunk = Math.max(8, baseLatencyMs / Math.max(1, parts.size()));
        Flux<ChatResponse> body = Flux.fromIterable(parts)
                .delayElements(Duration.ofMillis(perChunk))
                .map(part -> new ChatResponse(List.of(new Generation(new AssistantMessage(part)))));
        return Flux.concat(body, Flux.just(response("", promptTokens, completionTokens)));
    }

    private String userText(Prompt prompt) {
        return prompt.getUserMessage() != null ? prompt.getUserMessage().getText() : prompt.getContents();
    }

    private ChatResponse response(String content, int promptTokens, int completionTokens) {
        Generation generation =
                new Generation(
                        new AssistantMessage(content),
                        ChatGenerationMetadata.builder().finishReason("stop").build());
        ChatResponseMetadata metadata =
                ChatResponseMetadata.builder()
                        .model(model)
                        .usage(new DefaultUsage(promptTokens, completionTokens))
                        .build();
        return new ChatResponse(List.of(generation), metadata);
    }

    private String generate(String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("diff --git") || (lower.contains("diff") && lower.contains("summar"))) {
            return prSummary(prompt);
        }
        if (lower.contains("junit") || lower.contains("test stubs") || lower.contains("class under test")) {
            return testStub(prompt);
        }
        if (lower.contains("operations summary") || (lower.contains("metric") && lower.contains("health"))) {
            return healthDigest();
        }
        return general(prompt);
    }

    private String general(String prompt) {
        String topic = firstWords(prompt, 12);
        if ("concise".equalsIgnoreCase(persona)) {
            return "Short answer on \"" + topic + "\": the key trade-off is correctness first, then cost. "
                    + "Pick the simplest option that satisfies the constraint and measure before optimising.";
        }
        return "On \"" + topic + "\": there are a few angles worth weighing. Start from the constraint that "
                + "actually binds (latency, cost, or correctness), make the smallest change that satisfies it, "
                + "and keep the design reversible. Add instrumentation early so the next decision is driven by "
                + "data rather than guesswork, and prefer boring, well-understood building blocks over novelty.";
    }

    private String prSummary(String prompt) {
        boolean touchesAuth = prompt.toLowerCase().contains("auth") || prompt.toLowerCase().contains("token");
        String securityRisk =
                touchesAuth
                        ? "{\"severity\":\"HIGH\",\"area\":\"security\",\"detail\":\"Auth-related code changed; confirm tokens are never logged and scopes are re-checked server-side.\"},"
                        : "";
        return "{"
                + "\"summary\":\"Refactors the changed module and adjusts control flow. The diff is cohesive and "
                + "scoped, but a couple of edge paths and error handling need a second look before merge.\","
                + "\"risks\":["
                + securityRisk
                + "{\"severity\":\"MEDIUM\",\"area\":\"error-handling\",\"detail\":\"A new branch can throw on null input; guard it or document the precondition.\"},"
                + "{\"severity\":\"LOW\",\"area\":\"readability\",\"detail\":\"One method now does two things; consider splitting for clarity.\"}"
                + "],"
                + "\"testGaps\":["
                + "\"No test covers the early-return path introduced in this change.\","
                + "\"Add a regression test for the boundary value mentioned in the diff.\""
                + "]"
                + "}";
    }

    private String testStub(String prompt) {
        String className = classNameFrom(prompt);
        return """
                import static org.assertj.core.api.Assertions.assertThat;
                import static org.assertj.core.api.Assertions.assertThatThrownBy;

                import org.junit.jupiter.api.DisplayName;
                import org.junit.jupiter.api.Nested;
                import org.junit.jupiter.api.Test;

                class %sTest {

                    @Nested
                    @DisplayName("happy path")
                    class HappyPath {

                        @Test
                        @DisplayName("returns the expected result for valid input")
                        void returnsExpectedResult() {
                            // arrange a valid instance, act, then assert on the observable outcome
                        }
                    }

                    @Nested
                    @DisplayName("edge cases")
                    class EdgeCases {

                        @Test
                        @DisplayName("rejects null arguments")
                        void rejectsNull() {
                            // assertThatThrownBy(() -> ...).isInstanceOf(NullPointerException.class);
                        }

                        @Test
                        @DisplayName("handles empty and boundary values")
                        void handlesBoundaries() {
                            // cover empty collections, zero, and max values
                        }
                    }
                }
                """
                .formatted(className);
    }

    private String healthDigest() {
        return "Overall the system looked healthy over the window. Request throughput held steady and the error "
                + "rate stayed within normal bounds, with a brief latency bump mid-window that recovered on its own. "
                + "One provider's circuit breaker opened twice under load and failed over cleanly, so no requests "
                + "were dropped. Nothing needs attention right now; keep an eye on the latency spikes if they recur.";
    }

    private List<String> chunks(String content) {
        if (content.isBlank()) {
            return List.of();
        }
        return List.of(content.split("(?<=\\s)"));
    }

    private static String firstWords(String text, int count) {
        String[] words = text.strip().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(count, words.length); i++) {
            sb.append(words[i]).append(' ');
        }
        return sb.toString().strip();
    }

    private static String classNameFrom(String prompt) {
        var matcher = java.util.regex.Pattern.compile("\\b(?:class|record|interface|enum)\\s+([A-Z][A-Za-z0-9_]*)")
                .matcher(prompt);
        return matcher.find() ? matcher.group(1) : "ClassUnderTest";
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(Math.max(0, millis));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
