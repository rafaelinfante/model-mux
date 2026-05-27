package net.rafaelinfante.modelmux.gateway.routing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.config.ModelMuxProperties.Routing;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.ProviderRegistry;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderChunk;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import net.rafaelinfante.modelmux.provider.model.ProviderType;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class RoutingPlannerTest {

    private final ProviderRegistry registry =
            new ProviderRegistry(List.of(fake("cheap", ModelTier.CHEAP), fake("premium", ModelTier.PREMIUM)));
    private final RoutingPlanner planner =
            new RoutingPlanner(
                    registry,
                    new CostPolicy(new TokenEstimator()),
                    new ModelMuxProperties(
                            new Routing(RoutingMode.COST, List.of("premium", "cheap")), null, null, null, null, null));

    @Test
    void explicitReturnsOnlyTheNamedProvider() {
        assertThat(planner.plan(RoutingMode.EXPLICIT, ChatRequest.of("hi"), "cheap", false))
                .extracting(LlmProvider::id)
                .containsExactly("cheap");
    }

    @Test
    void costRoutesSimplePromptsToCheapFirst() {
        assertThat(planner.plan(RoutingMode.COST, ChatRequest.of("hello"), null, false).get(0).id())
                .isEqualTo("cheap");
    }

    @Test
    void costRoutesComplexPromptsToPremiumFirst() {
        assertThat(planner.plan(RoutingMode.COST, ChatRequest.of("design the architecture"), null, false).get(0).id())
                .isEqualTo("premium");
    }

    @Test
    void failoverFollowsConfiguredOrder() {
        assertThat(planner.plan(RoutingMode.FAILOVER, ChatRequest.of("hi"), null, false))
                .extracting(LlmProvider::id)
                .containsExactly("premium", "cheap");
    }

    @Test
    void budgetDowngradeForcesCheapFirst() {
        assertThat(planner.plan(RoutingMode.COST, ChatRequest.of("design the architecture"), null, true).get(0).id())
                .isEqualTo("cheap");
    }

    private static LlmProvider fake(String id, ModelTier tier) {
        return new LlmProvider() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String displayName() {
                return id;
            }

            @Override
            public ProviderType type() {
                return ProviderType.MOCK;
            }

            @Override
            public String model() {
                return id + "-model";
            }

            @Override
            public ModelTier tier() {
                return tier;
            }

            @Override
            public ProviderResponse complete(ChatRequest request) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Flux<ProviderChunk> stream(ChatRequest request) {
                return Flux.empty();
            }
        };
    }
}
