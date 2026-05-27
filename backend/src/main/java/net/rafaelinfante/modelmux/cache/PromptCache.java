package net.rafaelinfante.modelmux.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import org.springframework.stereotype.Component;

/**
 * In-memory cache of identical completions, keyed by provider plus the request that determines the
 * answer. A hit returns the previous output at zero token cost. Caching is per provider on purpose:
 * the same prompt sent to two providers is two different cache entries.
 */
@Component
public class PromptCache {

    private static final String FIELD_SEPARATOR = "";

    private final boolean enabled;
    private final Cache<String, CachedCompletion> cache;

    public PromptCache(ModelMuxProperties properties) {
        ModelMuxProperties.CacheSpec spec = properties.cache();
        this.enabled = spec.enabled();
        this.cache =
                Caffeine.newBuilder()
                        .maximumSize(spec.maxEntries())
                        .expireAfterWrite(spec.ttl())
                        .build();
    }

    public Optional<CachedCompletion> get(String key) {
        return enabled ? Optional.ofNullable(cache.getIfPresent(key)) : Optional.empty();
    }

    public void put(String key, CachedCompletion value) {
        if (enabled) {
            cache.put(key, value);
        }
    }

    public String keyFor(String providerId, ChatRequest request) {
        String canonical =
                String.join(
                        FIELD_SEPARATOR,
                        providerId,
                        request.prompt(),
                        nullToEmpty(request.system()),
                        request.temperature() == null ? "" : request.temperature().toString(),
                        request.maxTokens() == null ? "" : request.maxTokens().toString());
        return sha256(canonical);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
