package net.rafaelinfante.modelmux.skills.prsummary;

import net.rafaelinfante.modelmux.gateway.GatewayResult;
import net.rafaelinfante.modelmux.gateway.GatewayService;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.skills.ProviderMeta;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

/**
 * The PR-review skill. The model handles judgment (summary, risks, test gaps) as structured output;
 * Google Java Format handles formatting — deterministically, alongside. Splitting the two is the
 * point: never ask the model to do what a tool does exactly.
 */
@Service
public class PrSummaryService {

    private static final String SYSTEM =
            "You are a senior engineer reviewing a pull request. Be specific, terse, and honest about risk.";

    private final GatewayService gateway;
    private final StyleChecker styleChecker;
    private final BeanOutputConverter<RiskReport> converter = new BeanOutputConverter<>(RiskReport.class);

    public PrSummaryService(GatewayService gateway, StyleChecker styleChecker) {
        this.gateway = gateway;
        this.styleChecker = styleChecker;
    }

    public PrSummaryResponse summarize(String diff, String javaSource, String clientId) {
        String prompt =
                """
                Summarize the following git diff for a reviewer in two or three sentences, then list the \
                top risks (bugs, security, missing tests) and any test gaps.

                %s

                %s"""
                        .formatted(diff, converter.getFormat());

        GatewayResult result =
                gateway.complete(
                        new ChatRequest(prompt, SYSTEM, 700, 0.2, clientId, false), RoutingMode.COST, null);
        RiskReport review = converter.convert(result.content());
        StyleResult styleCheck = styleChecker.check(javaSource);
        return new PrSummaryResponse(review, styleCheck, ProviderMeta.from(result));
    }
}
