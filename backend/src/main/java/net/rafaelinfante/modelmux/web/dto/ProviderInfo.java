package net.rafaelinfante.modelmux.web.dto;

import net.rafaelinfante.modelmux.provider.LlmProvider;

public record ProviderInfo(String id, String displayName, String type, String model, String tier) {

    public static ProviderInfo from(LlmProvider provider) {
        return new ProviderInfo(
                provider.id(),
                provider.displayName(),
                provider.type().name(),
                provider.model(),
                provider.tier().name());
    }
}
