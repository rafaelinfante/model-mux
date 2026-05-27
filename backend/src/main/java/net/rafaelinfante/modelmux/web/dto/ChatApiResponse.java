package net.rafaelinfante.modelmux.web.dto;

import java.math.BigDecimal;
import net.rafaelinfante.modelmux.gateway.GatewayResult;

public record ChatApiResponse(
        String content,
        String provider,
        String providerType,
        String model,
        String tier,
        String routeMode,
        long promptTokens,
        long completionTokens,
        long totalTokens,
        BigDecimal costUsd,
        long latencyMs,
        String finishReason,
        boolean cacheHit,
        boolean failedOver,
        String correlationId) {

    public static ChatApiResponse from(GatewayResult result) {
        return new ChatApiResponse(
                result.content(),
                result.providerId(),
                result.providerType().name(),
                result.model(),
                result.tier().name(),
                result.routeMode().name(),
                result.usage().promptTokens(),
                result.usage().completionTokens(),
                result.usage().totalTokens(),
                result.costUsd(),
                result.latencyMs(),
                result.finishReason().name(),
                result.cacheHit(),
                result.failedOver(),
                result.correlationId());
    }
}
