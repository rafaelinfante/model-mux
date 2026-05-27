package net.rafaelinfante.modelmux.gateway;

import java.math.BigDecimal;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.FinishReason;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderType;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;

/** The gateway's full answer for one request: the model output plus everything we measured around it. */
public record GatewayResult(
        String correlationId,
        String clientId,
        RoutingMode routeMode,
        String providerId,
        ProviderType providerType,
        String model,
        ModelTier tier,
        String content,
        TokenUsage usage,
        BigDecimal costUsd,
        long latencyMs,
        FinishReason finishReason,
        boolean cacheHit,
        boolean failedOver) {}
