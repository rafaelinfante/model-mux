package net.rafaelinfante.modelmux.provider.model;

public enum FinishReason {
    STOP,
    LENGTH,
    CONTENT_FILTER,
    TOOL_CALLS,
    ERROR,
    UNKNOWN;

    public static FinishReason fromProvider(String raw) {
        if (raw == null || raw.isBlank()) {
            return UNKNOWN;
        }
        return switch (raw.toLowerCase()) {
            case "stop", "end_turn", "complete", "completed", "eos" -> STOP;
            case "length", "max_tokens", "model_length", "token_limit" -> LENGTH;
            case "content_filter", "safety", "blocked" -> CONTENT_FILTER;
            case "tool_calls", "tool_use", "function_call" -> TOOL_CALLS;
            case "error", "failed" -> ERROR;
            default -> UNKNOWN;
        };
    }
}
