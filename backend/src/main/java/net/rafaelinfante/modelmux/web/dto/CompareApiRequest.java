package net.rafaelinfante.modelmux.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;

public record CompareApiRequest(
        @NotBlank String prompt,
        String system,
        @Positive Integer maxTokens,
        @DecimalMin("0.0") @DecimalMax("2.0") Double temperature,
        List<String> providers) {

    public ChatRequest toChatRequest(String clientId) {
        return new ChatRequest(prompt, system, maxTokens, temperature, clientId, false);
    }
}
