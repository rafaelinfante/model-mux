package net.rafaelinfante.modelmux.gateway;

import java.math.BigDecimal;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;

/** One server-sent event in a streamed completion: many {@code delta}s, then a single {@code done}. */
public record StreamEvent(String type, String content, Meta meta) {

    public record Meta(
            String providerId,
            String model,
            String tier,
            TokenUsage usage,
            BigDecimal costUsd,
            long latencyMs) {}

    public static StreamEvent delta(String content) {
        return new StreamEvent("delta", content, null);
    }

    public static StreamEvent done(Meta meta) {
        return new StreamEvent("done", null, meta);
    }
}
