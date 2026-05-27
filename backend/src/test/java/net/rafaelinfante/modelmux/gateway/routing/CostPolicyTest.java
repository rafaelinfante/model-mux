package net.rafaelinfante.modelmux.gateway.routing;

import static org.assertj.core.api.Assertions.assertThat;

import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import org.junit.jupiter.api.Test;

class CostPolicyTest {

    private final CostPolicy policy = new CostPolicy(new TokenEstimator());

    @Test
    void shortPlainPromptIsCheap() {
        assertThat(policy.tierFor(ChatRequest.of("what time is it"))).isEqualTo(ModelTier.CHEAP);
    }

    @Test
    void reasoningKeywordIsPremium() {
        assertThat(policy.tierFor(ChatRequest.of("Please design the architecture for this")))
                .isEqualTo(ModelTier.PREMIUM);
    }

    @Test
    void codeBlockIsPremium() {
        assertThat(policy.tierFor(ChatRequest.of("```java\nclass A {}\n```"))).isEqualTo(ModelTier.PREMIUM);
    }
}
