package net.rafaelinfante.modelmux.provider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.rafaelinfante.modelmux.provider.model.ModelTier;
import net.rafaelinfante.modelmux.provider.model.ProviderType;

/** Immutable lookup of the providers wired at startup, preserving configuration order. */
public class ProviderRegistry {

    private final Map<String, LlmProvider> byId = new LinkedHashMap<>();

    public ProviderRegistry(List<LlmProvider> providers) {
        for (LlmProvider provider : providers) {
            byId.put(provider.id(), provider);
        }
    }

    public Optional<LlmProvider> find(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public LlmProvider require(String id) {
        LlmProvider provider = byId.get(id);
        if (provider == null) {
            throw new UnknownProviderException(id);
        }
        return provider;
    }

    public List<LlmProvider> all() {
        return List.copyOf(byId.values());
    }

    public List<LlmProvider> byTier(ModelTier tier) {
        return byId.values().stream().filter(p -> p.tier() == tier).toList();
    }

    public List<LlmProvider> byType(ProviderType type) {
        return byId.values().stream().filter(p -> p.type() == type).toList();
    }

    public boolean isEmpty() {
        return byId.isEmpty();
    }
}
