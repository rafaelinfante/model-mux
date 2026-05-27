package net.rafaelinfante.modelmux.web;

import java.util.List;
import net.rafaelinfante.modelmux.provider.ProviderRegistry;
import net.rafaelinfante.modelmux.web.dto.ProviderInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/providers")
public class ProvidersController {

    private final ProviderRegistry registry;

    public ProvidersController(ProviderRegistry registry) {
        this.registry = registry;
    }

    @GetMapping
    public List<ProviderInfo> providers() {
        return registry.all().stream().map(ProviderInfo::from).toList();
    }
}
