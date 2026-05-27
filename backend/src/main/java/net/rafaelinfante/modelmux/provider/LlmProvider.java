package net.rafaelinfante.modelmux.provider;

import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderChunk;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import net.rafaelinfante.modelmux.provider.model.ProviderType;
import reactor.core.publisher.Flux;

/** One LLM behind the gateway. Implementations wrap a Spring AI {@code ChatModel}. */
public interface LlmProvider {

    String id();

    String displayName();

    ProviderType type();

    String model();

    ModelTier tier();

    /** Blocking completion. Throws {@link TransientProviderException} for retryable failures. */
    ProviderResponse complete(ChatRequest request);

    /** Streamed token deltas. Usage is accounted by the gateway once the stream completes. */
    Flux<ProviderChunk> stream(ChatRequest request);
}
