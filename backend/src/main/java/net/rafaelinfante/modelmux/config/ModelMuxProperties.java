package net.rafaelinfante.modelmux.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderType;
import net.rafaelinfante.modelmux.token.OverBudgetAction;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "modelmux")
public record ModelMuxProperties(
        Routing routing,
        List<ProviderSpec> providers,
        Map<String, Pricing> pricing,
        Budgets budgets,
        RateLimit rateLimit,
        CacheSpec cache) {

    public record Routing(RoutingMode defaultMode, List<String> failoverOrder) {}

    public record ProviderSpec(
            String id,
            ProviderType type,
            String model,
            ModelTier tier,
            boolean enabled,
            String apiKey,
            MockBehavior mock) {}

    public record MockBehavior(long baseLatencyMs, long perTokenLatencyMicros, String persona) {}

    public record Pricing(BigDecimal inputPerMillion, BigDecimal outputPerMillion) {}

    public record Budgets(boolean enabled, BigDecimal defaultDailyUsd, OverBudgetAction overBudgetAction) {}

    public record RateLimit(boolean enabled, int requestsPerMinute) {}

    public record CacheSpec(boolean enabled, long maxEntries, Duration ttl) {}

    public Pricing pricingFor(String model) {
        return pricing.getOrDefault(model, new Pricing(BigDecimal.ZERO, BigDecimal.ZERO));
    }
}
