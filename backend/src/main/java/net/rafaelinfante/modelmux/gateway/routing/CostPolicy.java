package net.rafaelinfante.modelmux.gateway.routing;

import java.util.Set;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.token.TokenEstimator;
import org.springframework.stereotype.Component;

/**
 * Decides cheap vs premium from the prompt alone — long prompts, code, or words that signal a
 * reasoning-heavy task get the premium tier; everything else stays cheap. Deliberately a heuristic:
 * the point is to demonstrate cost-aware routing, not to be a classifier.
 */
@Component
public class CostPolicy {

    private static final int PREMIUM_TOKEN_THRESHOLD = 280;

    private static final Set<String> PREMIUM_SIGNALS =
            Set.of(
                    "design",
                    "architecture",
                    "refactor",
                    "optimize",
                    "prove",
                    "trade-off",
                    "tradeoff",
                    "analyze",
                    "diff --git",
                    "algorithm",
                    "concurren",
                    "security review");

    private final TokenEstimator tokenEstimator;

    public CostPolicy(TokenEstimator tokenEstimator) {
        this.tokenEstimator = tokenEstimator;
    }

    public ModelTier tierFor(ChatRequest request) {
        String prompt = request.prompt();
        String lower = prompt.toLowerCase();
        boolean heavy =
                tokenEstimator.estimate(prompt) > PREMIUM_TOKEN_THRESHOLD
                        || prompt.contains("```")
                        || PREMIUM_SIGNALS.stream().anyMatch(lower::contains);
        return heavy ? ModelTier.PREMIUM : ModelTier.CHEAP;
    }
}
