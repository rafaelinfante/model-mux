package net.rafaelinfante.modelmux.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.config.ModelMuxProperties.CacheSpec;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.provider.model.FinishReason;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;
import org.junit.jupiter.api.Test;

class PromptCacheTest {

    @Test
    void keyIsStableAndScopedToProvider() {
        PromptCache cache = cache(true);
        ChatRequest request = ChatRequest.of("hello");

        assertThat(cache.keyFor("a", request)).isEqualTo(cache.keyFor("a", request));
        assertThat(cache.keyFor("a", request)).isNotEqualTo(cache.keyFor("b", request));
    }

    @Test
    void storesAndReturnsCompletion() {
        PromptCache cache = cache(true);
        String key = cache.keyFor("a", ChatRequest.of("x"));
        cache.put(key, new CachedCompletion("hi", TokenUsage.of(1, 1), FinishReason.STOP));

        assertThat(cache.get(key)).map(CachedCompletion::content).contains("hi");
    }

    @Test
    void disabledCacheAlwaysMisses() {
        PromptCache cache = cache(false);
        String key = cache.keyFor("a", ChatRequest.of("x"));
        cache.put(key, new CachedCompletion("hi", TokenUsage.ZERO, FinishReason.STOP));

        assertThat(cache.get(key)).isEmpty();
    }

    private static PromptCache cache(boolean enabled) {
        return new PromptCache(
                new ModelMuxProperties(
                        null, null, null, null, null, new CacheSpec(enabled, 100, Duration.ofMinutes(5))));
    }
}
