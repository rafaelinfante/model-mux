package net.rafaelinfante.modelmux.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;

public record ChatApiRequest(
        @NotBlank String prompt,
        String system,
        @Positive Integer maxTokens,
        @DecimalMin("0.0") @DecimalMax("2.0") Double temperature,
        RoutingMode mode,
        String provider,
        boolean forceFailover) {

    public ChatRequest toChatRequest(String clientId) {
        return new ChatRequest(prompt, system, maxTokens, temperature, clientId, forceFailover);
    }
}
