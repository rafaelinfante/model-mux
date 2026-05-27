package net.rafaelinfante.modelmux.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.rafaelinfante.modelmux.config.ModelMuxProperties.MockBehavior;
import net.rafaelinfante.modelmux.config.ModelMuxProperties.ProviderSpec;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.MockChatModel;
import net.rafaelinfante.modelmux.provider.ProviderRegistry;
import net.rafaelinfante.modelmux.provider.SpringAiProvider;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

/**
 * Builds the provider registry from configuration. Real models are constructed here, only when a
 * provider is enabled and a key is present — so the gateway boots on the mocks alone. Each model is
 * given a no-op retry template because resilience is owned by Resilience4j, not Spring AI.
 */
@Configuration
public class ProvidersConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ProvidersConfiguration.class);

    @Bean
    ProviderRegistry providerRegistry(ModelMuxProperties properties, TokenEstimator tokenEstimator) {
        List<LlmProvider> providers = new ArrayList<>();
        for (ProviderSpec spec : properties.providers()) {
            if (!spec.enabled()) {
                continue;
            }
            switch (spec.type()) {
                case MOCK -> providers.add(mockProvider(spec, tokenEstimator));
                case ANTHROPIC -> addReal(providers, spec, this::anthropicModel);
                case OPENAI -> addReal(providers, spec, this::openAiModel);
            }
        }

        if (providers.isEmpty()) {
            throw new IllegalStateException("No providers registered; enable at least one mock provider.");
        }
        log.info(
                "Registered {} provider(s): {}",
                providers.size(),
                providers.stream().map(LlmProvider::id).toList());
        return new ProviderRegistry(providers);
    }

    private void addReal(
            List<LlmProvider> providers, ProviderSpec spec, Function<ProviderSpec, ChatModel> modelFactory) {
        if (spec.apiKey() == null || spec.apiKey().isBlank()) {
            log.warn("Provider '{}' is enabled but no API key is set; skipping it.", spec.id());
            return;
        }
        providers.add(provider(spec, ChatClient.create(modelFactory.apply(spec))));
    }

    private ChatModel anthropicModel(ProviderSpec spec) {
        AnthropicApi api = AnthropicApi.builder().apiKey(spec.apiKey()).build();
        AnthropicChatOptions options = AnthropicChatOptions.builder().model(spec.model()).build();
        return AnthropicChatModel.builder()
                .anthropicApi(api)
                .defaultOptions(options)
                .retryTemplate(singleAttempt())
                .build();
    }

    private ChatModel openAiModel(ProviderSpec spec) {
        OpenAiApi api = OpenAiApi.builder().apiKey(spec.apiKey()).build();
        OpenAiChatOptions options = OpenAiChatOptions.builder().model(spec.model()).build();
        return OpenAiChatModel.builder()
                .openAiApi(api)
                .defaultOptions(options)
                .retryTemplate(singleAttempt())
                .build();
    }

    private LlmProvider mockProvider(ProviderSpec spec, TokenEstimator tokenEstimator) {
        MockBehavior behavior = spec.mock();
        MockChatModel model =
                new MockChatModel(
                        spec.model(),
                        behavior.persona(),
                        behavior.baseLatencyMs(),
                        behavior.perTokenLatencyMicros(),
                        tokenEstimator);
        return provider(spec, ChatClient.create(model));
    }

    private LlmProvider provider(ProviderSpec spec, ChatClient chatClient) {
        return new SpringAiProvider(
                spec.id(), displayName(spec), spec.type(), spec.model(), spec.tier(), chatClient);
    }

    private String displayName(ProviderSpec spec) {
        return switch (spec.type()) {
            case ANTHROPIC -> "Anthropic Claude";
            case OPENAI -> "OpenAI";
            case MOCK -> "Mock (" + spec.tier().name().toLowerCase() + ")";
        };
    }

    private static RetryTemplate singleAttempt() {
        return RetryTemplate.builder().maxAttempts(1).build();
    }
}
