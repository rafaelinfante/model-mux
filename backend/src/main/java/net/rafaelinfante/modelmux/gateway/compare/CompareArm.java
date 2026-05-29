package net.rafaelinfante.modelmux.gateway.compare;

import java.math.BigDecimal;
import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.model.ProviderResponse;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;

/** One side of a compare: a provider's answer plus what it cost and how long it took, or its error. */
public record CompareArm(
        String providerId,
        String displayName,
        String model,
        String tier,
        boolean ok,
        String content,
        TokenUsage usage,
        BigDecimal costUsd,
        long latencyMs,
        String error) {

    public static CompareArm ok(LlmProvider provider, ProviderResponse response, BigDecimal cost, long latencyMs) {
        return new CompareArm(
                provider.id(),
                provider.displayName(),
                provider.model(),
                provider.tier().name(),
                true,
                response.content(),
                response.usage(),
                cost,
                latencyMs,
                null);
    }

    public static CompareArm failed(LlmProvider provider, String error) {
        return new CompareArm(
                provider.id(),
                provider.displayName(),
                provider.model(),
                provider.tier().name(),
                false,
                null,
                TokenUsage.ZERO,
                BigDecimal.ZERO,
                0L,
                error);
    }
}
