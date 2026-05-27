package net.rafaelinfante.modelmux.provider.model;

public record TokenUsage(long promptTokens, long completionTokens, long totalTokens) {

    public static final TokenUsage ZERO = new TokenUsage(0, 0, 0);

    public static TokenUsage of(long promptTokens, long completionTokens) {
        return new TokenUsage(promptTokens, completionTokens, promptTokens + completionTokens);
    }
}
