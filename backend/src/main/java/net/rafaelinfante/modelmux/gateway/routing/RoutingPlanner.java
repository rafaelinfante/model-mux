package net.rafaelinfante.modelmux.gateway.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.ProviderRegistry;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import org.springframework.stereotype.Component;

/**
 * Turns a routing mode into an ordered list of providers to try. The first entry is the primary; the
 * rest are fallbacks the gateway only reaches if the primary fails transiently. Explicit routing has
 * no fallbacks by design — the caller asked for one provider.
 */
@Component
public class RoutingPlanner {

    private final ProviderRegistry registry;
    private final CostPolicy costPolicy;
    private final List<String> failoverOrder;

    public RoutingPlanner(ProviderRegistry registry, CostPolicy costPolicy, ModelMuxProperties properties) {
        this.registry = registry;
        this.costPolicy = costPolicy;
        this.failoverOrder = properties.routing().failoverOrder();
    }

    public List<LlmProvider> plan(
            RoutingMode mode, ChatRequest request, String explicitProviderId, boolean forceCheapTier) {
        if (forceCheapTier) {
            return preferTier(ModelTier.CHEAP);
        }
        return switch (mode) {
            case EXPLICIT -> List.of(registry.require(requireId(explicitProviderId)));
            case COST -> preferTier(costPolicy.tierFor(request));
            case FAILOVER -> failoverChain();
        };
    }

    private String requireId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Explicit routing requires a 'provider' id");
        }
        return id;
    }

    private List<LlmProvider> preferTier(ModelTier tier) {
        List<LlmProvider> chosen = registry.byTier(tier);
        List<LlmProvider> rest = registry.all().stream().filter(p -> p.tier() != tier).toList();
        return concat(chosen, rest);
    }

    private List<LlmProvider> failoverChain() {
        List<LlmProvider> ordered =
                failoverOrder.stream().map(registry::find).flatMap(Optional::stream).toList();
        List<LlmProvider> remaining =
                registry.all().stream()
                        .filter(p -> ordered.stream().noneMatch(o -> o.id().equals(p.id())))
                        .toList();
        List<LlmProvider> chain = concat(ordered, remaining);
        if (chain.isEmpty()) {
            throw new IllegalStateException("No providers available for failover");
        }
        return chain;
    }

    private static List<LlmProvider> concat(List<LlmProvider> first, List<LlmProvider> second) {
        List<LlmProvider> combined = new ArrayList<>(first);
        combined.addAll(second);
        return combined;
    }
}
