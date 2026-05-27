package net.rafaelinfante.modelmux.provider.model;

import java.util.Objects;

/**
 * Provider-agnostic chat request. {@code clientId} carries the budget/rate-limit identity; providers
 * ignore it. {@code forceProviderFailure} only ever affects mock providers — it is how the playground's
 * "force failover" button drives a deterministic failure so the breaker and fallthrough are observable.
 */
public record ChatRequest(
        String prompt,
        String system,
        Integer maxTokens,
        Double temperature,
        String clientId,
        boolean forceProviderFailure) {

    public ChatRequest {
        Objects.requireNonNull(prompt, "prompt must not be null");
    }

    public static ChatRequest of(String prompt) {
        return new ChatRequest(prompt, null, null, null, "anonymous", false);
    }

    public ChatRequest withClientId(String clientId) {
        return new ChatRequest(prompt, system, maxTokens, temperature, clientId, forceProviderFailure);
    }
}
