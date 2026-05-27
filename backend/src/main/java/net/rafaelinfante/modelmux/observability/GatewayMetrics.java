package net.rafaelinfante.modelmux.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.util.concurrent.TimeUnit;
import net.rafaelinfante.modelmux.gateway.GatewayResult;
import org.springframework.stereotype.Component;

/** Per-provider counters and timers exported to Prometheus via the actuator endpoint. */
@Component
public class GatewayMetrics {

    private final MeterRegistry registry;

    public GatewayMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void record(GatewayResult result) {
        Tags tags =
                Tags.of(
                        "provider", result.providerId(),
                        "tier", result.tier().name(),
                        "mode", result.routeMode().name());
        registry.counter("modelmux.requests", tags).increment();
        registry.counter("modelmux.tokens", tags).increment(result.usage().totalTokens());
        registry.counter("modelmux.cost.usd", tags).increment(result.costUsd().doubleValue());
        registry.timer("modelmux.latency", tags).record(result.latencyMs(), TimeUnit.MILLISECONDS);
        if (result.cacheHit()) {
            registry.counter("modelmux.cache.hits").increment();
        }
        if (result.failedOver()) {
            registry.counter("modelmux.failovers", "provider", result.providerId()).increment();
        }
    }

    public void recordProviderFailure(String providerId) {
        registry.counter("modelmux.provider.failures", "provider", providerId).increment();
    }
}
