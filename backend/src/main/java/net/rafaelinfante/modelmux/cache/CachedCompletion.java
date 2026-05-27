package net.rafaelinfante.modelmux.cache;

import net.rafaelinfante.modelmux.provider.model.FinishReason;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;

public record CachedCompletion(String content, TokenUsage usage, FinishReason finishReason) {}
