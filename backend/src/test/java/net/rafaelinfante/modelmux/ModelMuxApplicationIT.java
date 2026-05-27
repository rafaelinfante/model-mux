package net.rafaelinfante.modelmux;

import static org.assertj.core.api.Assertions.assertThat;

import net.rafaelinfante.modelmux.provider.LlmProvider;
import net.rafaelinfante.modelmux.provider.ProviderRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ModelMuxApplicationIT extends AbstractIntegrationTest {

    @Autowired private ProviderRegistry registry;

    @Test
    void contextLoadsWithMockProviders() {
        assertThat(registry.all()).extracting(LlmProvider::id).contains("mock-fast", "mock-smart");
    }
}
