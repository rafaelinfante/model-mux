package net.rafaelinfante.modelmux.gateway;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.rafaelinfante.modelmux.cache.CachedCompletion;
import net.rafaelinfante.modelmux.cache.PromptCache;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.gateway.routing.RoutingPlanner;
import net.rafaelinfante.modelmux.observability.CorrelationId;
import net.rafaelinfante.modelmux.observability.GatewayMetrics;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.TransientProviderException;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.FinishReason;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;
import net.rafaelinfante.modelmux.token.BudgetExceededException;
import net.rafaelinfante.modelmux.token.BudgetService;
import net.rafaelinfante.modelmux.token.BudgetStatus;
import net.rafaelinfante.modelmux.token.CostCalculator;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import net.rafaelinfante.modelmux.token.UsageService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The orchestration seam: route, check the cache and budget, call the provider through Resilience4j,
 * account the cost, and fall through to the next candidate on a transient failure. The provider call
 * sits outside any database transaction so a slow third party never holds a row lock.
 */
@Service
public class GatewayService {

    private final RoutingPlanner routingPlanner;
    private final ResilientInvoker invoker;
    private final PromptCache promptCache;
    private final CostCalculator costCalculator;
    private final BudgetService budgetService;
    private final UsageService usageService;
    private final TokenEstimator tokenEstimator;
    private final GatewayMetrics metrics;

    public GatewayService(
            RoutingPlanner routingPlanner,
            ResilientInvoker invoker,
            PromptCache promptCache,
            CostCalculator costCalculator,
            BudgetService budgetService,
            UsageService usageService,
            TokenEstimator tokenEstimator,
            GatewayMetrics metrics) {
        this.routingPlanner = routingPlanner;
        this.invoker = invoker;
        this.promptCache = promptCache;
        this.costCalculator = costCalculator;
        this.budgetService = budgetService;
        this.usageService = usageService;
        this.tokenEstimator = tokenEstimator;
        this.metrics = metrics;
    }

    public GatewayResult complete(ChatRequest request, RoutingMode mode, String explicitProviderId) {
        String clientId = request.clientId();
        boolean downgrade = guardBudget(clientId);

        List<LlmProvider> candidates = routingPlanner.plan(mode, request, explicitProviderId, downgrade);
        String correlationId = CorrelationId.current();
        List<String> attempted = new ArrayList<>();
        RuntimeException lastTransient = null;

        for (int i = 0; i < candidates.size(); i++) {
            LlmProvider provider = candidates.get(i);
            boolean failedOver = i > 0;
            boolean forceFailure = i == 0 && request.forceProviderFailure();
            attempted.add(provider.id());

            if (!forceFailure) {
                Optional<CachedCompletion> cached =
                        promptCache.get(promptCache.keyFor(provider.id(), request));
                if (cached.isPresent()) {
                    return finish(cachedResult(provider, request, mode, correlationId, cached.get(), failedOver));
                }
            }

            try {
                long start = System.nanoTime();
                ProviderResponse response = invoker.invoke(provider, request, forceFailure);
                long latencyMs = elapsedMs(start);
                BigDecimal cost = costCalculator.costOf(provider.model(), response.usage());
                promptCache.put(
                        promptCache.keyFor(provider.id(), request),
                        new CachedCompletion(response.content(), response.usage(), response.finishReason()));
                return finish(
                        new GatewayResult(
                                correlationId,
                                clientId,
                                mode,
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
                                failedOver));
            } catch (TransientProviderException | CallNotPermittedException e) {
                metrics.recordProviderFailure(provider.id());
                lastTransient = e;
            }
        }
        throw new AllProvidersFailedException(attempted, lastTransient);
    }

    public Flux<StreamEvent> stream(ChatRequest request, RoutingMode mode, String explicitProviderId) {
        String clientId = request.clientId();
        BudgetStatus budget = budgetService.check(clientId);
        if (budget.shouldReject()) {
            return Flux.error(new BudgetExceededException(clientId, budget.limitUsd(), budget.spentUsd()));
        }
        if (request.forceProviderFailure()) {
            return Flux.error(new TransientProviderException("forced failure (demo)"));
        }

        LlmProvider provider = routingPlanner.plan(mode, request, explicitProviderId, budget.shouldDowngrade()).get(0);
        String correlationId = CorrelationId.current();
        long start = System.nanoTime();
        StringBuilder buffer = new StringBuilder();

        return provider
                .stream(request)
                .filter(chunk -> chunk.delta() != null && !chunk.delta().isEmpty())
                .map(
                        chunk -> {
                            buffer.append(chunk.delta());
                            return StreamEvent.delta(chunk.delta());
                        })
                .concatWith(
                        Mono.fromSupplier(
                                () -> completeStream(provider, request, mode, correlationId, buffer.toString(), start)));
    }

    private boolean guardBudget(String clientId) {
        BudgetStatus budget = budgetService.check(clientId);
        if (budget.shouldReject()) {
            throw new BudgetExceededException(clientId, budget.limitUsd(), budget.spentUsd());
        }
        return budget.shouldDowngrade();
    }

    private StreamEvent completeStream(
            LlmProvider provider, ChatRequest request, RoutingMode mode, String correlationId, String content, long start) {
        long latencyMs = elapsedMs(start);
        TokenUsage usage =
                TokenUsage.of(tokenEstimator.estimate(promptText(request)), tokenEstimator.estimate(content));
        BigDecimal cost = costCalculator.costOf(provider.model(), usage);
        finish(
                new GatewayResult(
                        correlationId,
                        request.clientId(),
                        mode,
                        provider.id(),
                        provider.type(),
                        provider.model(),
                        provider.tier(),
                        content,
                        usage,
                        cost,
                        latencyMs,
                        FinishReason.STOP,
                        false,
                        false));
        return StreamEvent.done(
                new StreamEvent.Meta(provider.id(), provider.model(), provider.tier().name(), usage, cost, latencyMs));
    }

    private GatewayResult cachedResult(
            LlmProvider provider,
            ChatRequest request,
            RoutingMode mode,
            String correlationId,
            CachedCompletion cached,
            boolean failedOver) {
        return new GatewayResult(
                correlationId,
                request.clientId(),
                mode,
                provider.id(),
                provider.type(),
                provider.model(),
                provider.tier(),
                cached.content(),
                cached.usage(),
                BigDecimal.ZERO,
                0L,
                cached.finishReason(),
                true,
                failedOver);
    }

    private GatewayResult finish(GatewayResult result) {
        usageService.record(result);
        metrics.record(result);
        budgetService.charge(result.clientId(), result.costUsd());
        return result;
    }

    private static String promptText(ChatRequest request) {
        return request.system() == null ? request.prompt() : request.system() + "\n" + request.prompt();
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
