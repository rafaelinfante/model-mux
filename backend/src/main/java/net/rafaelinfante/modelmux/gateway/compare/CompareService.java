package net.rafaelinfante.modelmux.gateway.compare;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.rafaelinfante.modelmux.gateway.GatewayResult;
import net.rafaelinfante.modelmux.gateway.ResilientInvoker;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.observability.CorrelationId;
import net.rafaelinfante.modelmux.observability.GatewayMetrics;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.ProviderRegistry;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import net.rafaelinfante.modelmux.provider.model.ProviderType;
import net.rafaelinfante.modelmux.token.BudgetService;
import net.rafaelinfante.modelmux.token.CostCalculator;
import net.rafaelinfante.modelmux.token.UsageService;
import org.springframework.stereotype.Service;

/**
 * Sends one prompt to several providers at once and returns their answers side by side. Each provider
 * runs on its own virtual thread, so the wall-clock cost is the slowest provider, not their sum — the
 * Java 21 payoff for a fan-out of mostly-waiting I/O.
 */
@Service
public class CompareService {

    private static final int MAX_ARMS = 4;

    private final ProviderRegistry registry;
    private final ResilientInvoker invoker;
    private final CostCalculator costCalculator;
    private final BudgetService budgetService;
    private final UsageService usageService;
    private final GatewayMetrics metrics;

    public CompareService(
            ProviderRegistry registry,
            ResilientInvoker invoker,
            CostCalculator costCalculator,
            BudgetService budgetService,
            UsageService usageService,
            GatewayMetrics metrics) {
        this.registry = registry;
        this.invoker = invoker;
        this.costCalculator = costCalculator;
        this.budgetService = budgetService;
        this.usageService = usageService;
        this.metrics = metrics;
    }

    public CompareResponse compare(List<String> providerIds, ChatRequest request) {
        List<LlmProvider> providers = resolve(providerIds);
        String correlationId = CorrelationId.current();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<CompareArm>> futures =
                    providers.stream()
                            .map(p -> CompletableFuture.supplyAsync(() -> callArm(p, request, correlationId), executor))
                            .toList();
            List<CompareArm> arms = futures.stream().map(CompletableFuture::join).toList();
            return new CompareResponse(request.prompt(), arms);
        }
    }

    private CompareArm callArm(LlmProvider provider, ChatRequest request, String correlationId) {
        try {
            long start = System.nanoTime();
            ProviderResponse response = invoker.invoke(provider, request, false);
            long latencyMs = (System.nanoTime() - start) / 1_000_000;
            BigDecimal cost = costCalculator.costOf(provider.model(), response.usage());
            GatewayResult result =
                    new GatewayResult(
                            correlationId,
                            request.clientId(),
                            RoutingMode.EXPLICIT,
                            provider.id(),
                            provider.type(),
                            provider.model(),
                            provider.tier(),
                            response.content(),
                            response.usage(),
                            cost,
                            latencyMs,
                            response.finishReason(),
                            false,
                            false);
            usageService.record(result);
            metrics.record(result);
            budgetService.charge(request.clientId(), cost);
            return CompareArm.ok(provider, response, cost, latencyMs);
        } catch (Exception e) {
            return CompareArm.failed(provider, e.getMessage());
        }
    }

    private List<LlmProvider> resolve(List<String> providerIds) {
        if (providerIds != null && !providerIds.isEmpty()) {
            return providerIds.stream().map(registry::require).toList();
        }
        List<LlmProvider> real = registry.all().stream().filter(p -> p.type() != ProviderType.MOCK).toList();
        List<LlmProvider> set = real.size() >= 2 ? real : registry.all();
        return set.stream().limit(MAX_ARMS).toList();
    }
}
