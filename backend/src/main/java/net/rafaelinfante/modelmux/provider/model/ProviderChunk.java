package net.rafaelinfante.modelmux.provider.model;

/** One streamed token delta. The terminal chunk carries the final {@link TokenUsage}. */
public record ProviderChunk(String delta, boolean last, TokenUsage usage) {

    public static ProviderChunk delta(String delta) {
        return new ProviderChunk(delta, false, null);
    }

    public static ProviderChunk last(TokenUsage usage) {
        return new ProviderChunk("", true, usage);
    }
}
