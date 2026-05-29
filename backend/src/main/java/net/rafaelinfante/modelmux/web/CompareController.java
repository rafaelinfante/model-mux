package net.rafaelinfante.modelmux.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.rafaelinfante.modelmux.gateway.compare.CompareResponse;
import net.rafaelinfante.modelmux.gateway.compare.CompareService;
import net.rafaelinfante.modelmux.web.dto.CompareApiRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compare")
public class CompareController {

    private final CompareService compareService;

    public CompareController(CompareService compareService) {
        this.compareService = compareService;
    }

    @PostMapping
    public CompareResponse compare(@Valid @RequestBody CompareApiRequest body, HttpServletRequest http) {
        String clientId = ClientIdResolver.resolve(http);
        return compareService.compare(body.providers(), body.toChatRequest(clientId));
    }
}
