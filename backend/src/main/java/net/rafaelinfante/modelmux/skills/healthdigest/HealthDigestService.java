package net.rafaelinfante.modelmux.skills.healthdigest;

import net.rafaelinfante.modelmux.gateway.GatewayResult;
import net.rafaelinfante.modelmux.gateway.GatewayService;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.skills.ProviderMeta;
import org.springframework.stereotype.Service;

/** Turns a sample of system metrics into a plain-English daily health summary. */
@Service
public class HealthDigestService {

    private static final String SYSTEM =
            "You write concise operations summaries for engineers. Plain English, no jargon, lead with what matters.";

    private final GatewayService gateway;

    public HealthDigestService(GatewayService gateway) {
        this.gateway = gateway;
    }

    public HealthDigestResponse digest(String metrics, String clientId) {
        String prompt =
                """
                Write a short plain-English operations summary of the following system metrics for a daily \
                health report. Call out anything notable (errors, latency, saturation) and end with whether \
                action is needed.

                %s"""
                        .formatted(metrics);

        GatewayResult result =
                gateway.complete(
                        new ChatRequest(prompt, SYSTEM, 400, 0.3, clientId, false), RoutingMode.COST, null);
        return new HealthDigestResponse(result.content().strip(), ProviderMeta.from(result));
    }
}
