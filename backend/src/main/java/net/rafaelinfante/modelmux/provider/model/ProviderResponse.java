package net.rafaelinfante.modelmux.provider.model;

/** Raw result from a single provider call, before the gateway adds cost, latency and routing context. */
public record ProviderResponse(String content, TokenUsage usage, FinishReason finishReason) {}
