package net.rafaelinfante.modelmux.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.gateway.GatewayResult;
import net.rafaelinfante.modelmux.gateway.GatewayService;
import net.rafaelinfante.modelmux.gateway.StreamEvent;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.web.dto.ChatApiRequest;
import net.rafaelinfante.modelmux.web.dto.ChatApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final GatewayService gateway;
    private final RoutingMode defaultMode;

    public ChatController(GatewayService gateway, ModelMuxProperties properties) {
        this.gateway = gateway;
        this.defaultMode = properties.routing().defaultMode();
    }

    @PostMapping
    public ChatApiResponse chat(@Valid @RequestBody ChatApiRequest body, HttpServletRequest http) {
        String clientId = ClientIdResolver.resolve(http);
        GatewayResult result = gateway.complete(body.toChatRequest(clientId), mode(body), body.provider());
        return ChatApiResponse.from(result);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamEvent>> stream(@Valid @RequestBody ChatApiRequest body, HttpServletRequest http) {
        String clientId = ClientIdResolver.resolve(http);
        return gateway
                .stream(body.toChatRequest(clientId), mode(body), body.provider())
                .map(event -> ServerSentEvent.builder(event).event(event.type()).build());
    }

    private RoutingMode mode(ChatApiRequest body) {
        return body.mode() != null ? body.mode() : defaultMode;
    }
}
