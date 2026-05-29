package net.rafaelinfante.modelmux.skills;

import java.math.BigDecimal;
import net.rafaelinfante.modelmux.gateway.GatewayResult;

/** Which provider served a skill request, and what it cost — so skills are as observable as raw chat. */
public record ProviderMeta(
        String provider, String model, String tier, BigDecimal costUsd, long latencyMs, boolean cacheHit) {

    public static ProviderMeta from(GatewayResult result) {
        return new ProviderMeta(
                result.providerId(),
                result.model(),
                result.tier().name(),
                result.costUsd(),
                result.latencyMs(),
                result.cacheHit());
    }
}
