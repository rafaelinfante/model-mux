package net.rafaelinfante.modelmux.provider;

import java.util.ArrayList;
import java.util.List;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.FinishReason;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderChunk;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import net.rafaelinfante.modelmux.provider.model.ProviderType;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import reactor.core.publisher.Flux;

/** Wraps any Spring AI {@code ChatModel} (real or the mock) behind the gateway's provider contract. */
public class SpringAiProvider implements LlmProvider {

    private final String id;
    private final String displayName;
    private final ProviderType type;
    private final String model;
    private final ModelTier tier;
    private final ChatClient chatClient;

    public SpringAiProvider(
            String id,
            String displayName,
            ProviderType type,
            String model,
            ModelTier tier,
            ChatClient chatClient) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.model = model;
        this.tier = tier;
        this.chatClient = chatClient;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public ProviderType type() {
        return type;
    }

    @Override
    public String model() {
        return model;
    }

    @Override
    public ModelTier tier() {
        return tier;
    }

    @Override
    public ProviderResponse complete(ChatRequest request) {
        try {
            ChatResponse response = chatClient.prompt(buildPrompt(request)).call().chatResponse();
            return extract(response);
        } catch (Exception e) {
            throw classify(e);
        }
    }

    @Override
    public Flux<ProviderChunk> stream(ChatRequest request) {
        return chatClient
                .prompt(buildPrompt(request))
                .stream()
                .chatResponse()
                .map(SpringAiProvider::toChunk)
                .onErrorMap(this::classify);
    }

    private Prompt buildPrompt(ChatRequest request) {
        List<Message> messages = new ArrayList<>();
        if (request.system() != null && !request.system().isBlank()) {
            messages.add(new SystemMessage(request.system()));
        }
        messages.add(new UserMessage(request.prompt()));
        return new Prompt(messages, options(request));
    }

    private ChatOptions options(ChatRequest request) {
        var builder = ChatOptions.builder();
        if (request.temperature() != null) {
            builder = builder.temperature(request.temperature());
        }
        if (request.maxTokens() != null) {
            builder = builder.maxTokens(request.maxTokens());
        }
        return builder.build();
    }

    private static ProviderResponse extract(ChatResponse response) {
        Generation generation = response.getResult();
        String content = generation.getOutput().getText();
        String finishReason = generation.getMetadata() != null ? generation.getMetadata().getFinishReason() : null;
        Usage usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;
        return new ProviderResponse(content, toUsage(usage), FinishReason.fromProvider(finishReason));
    }

    private static ProviderChunk toChunk(ChatResponse response) {
        Generation generation = response.getResult();
        String delta = generation != null && generation.getOutput() != null ? generation.getOutput().getText() : "";
        return ProviderChunk.delta(delta == null ? "" : delta);
    }

    private static TokenUsage toUsage(Usage usage) {
        if (usage == null) {
            return TokenUsage.ZERO;
        }
        long prompt = usage.getPromptTokens() == null ? 0 : usage.getPromptTokens().longValue();
        long completion = usage.getCompletionTokens() == null ? 0 : usage.getCompletionTokens().longValue();
        long total = usage.getTotalTokens() == null ? prompt + completion : usage.getTotalTokens().longValue();
        return new TokenUsage(prompt, completion, total);
    }

    private RuntimeException classify(Throwable e) {
        if (e instanceof ProviderException providerException) {
            return providerException;
        }
        if (e instanceof TransientAiException) {
            return new TransientProviderException("provider transient error: " + e.getMessage(), e);
        }
        if (e instanceof ResourceAccessException) {
            return new TransientProviderException("provider connection error", e);
        }
        if (e instanceof HttpServerErrorException) {
            return new TransientProviderException("provider returned 5xx", e);
        }
        if (e instanceof HttpClientErrorException clientError) {
            if (clientError.getStatusCode().value() == 429) {
                return new TransientProviderException("provider rate-limited", e);
            }
            return new ProviderException("provider rejected request: " + clientError.getStatusCode(), e);
        }
        Throwable root = rootCause(e);
        if (root instanceof java.net.SocketTimeoutException
                || root instanceof java.util.concurrent.TimeoutException) {
            return new TransientProviderException("provider timeout", e);
        }
        return new ProviderException(e.getMessage() == null ? "provider error" : e.getMessage(), e);
    }

    private static Throwable rootCause(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
