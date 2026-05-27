package net.rafaelinfante.modelmux.gateway;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.TransientProviderException;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import org.springframework.stereotype.Component;

/**
 * Wraps a single provider call in a per-provider retry and circuit breaker. The breaker is outermost
 * so it sees one outcome per attempt-group: when it is open the call short-circuits immediately and
 * the gateway moves on. {@code forceFailure} injects a transient error for the playground's failover
 * demo, exercising the real retry/breaker path rather than faking the result.
 */
@Component
public class ResilientInvoker {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public ResilientInvoker(
            CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
    }

    public ProviderResponse invoke(LlmProvider provider, ChatRequest request, boolean forceFailure) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(provider.id());
        Retry retry = retryRegistry.retry(provider.id());

        Supplier<ProviderResponse> call =
                () -> {
                    if (forceFailure) {
                        throw new TransientProviderException(
                                "forced failure for provider '" + provider.id() + "' (demo)");
                    }
                    return provider.complete(request);
                };

        return circuitBreaker.executeSupplier(Retry.decorateSupplier(retry, call));
    }
}
