package net.rafaelinfante.modelmux.skills.teststub;

import net.rafaelinfante.modelmux.gateway.GatewayResult;
import net.rafaelinfante.modelmux.gateway.GatewayService;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.skills.ProviderMeta;
import org.springframework.stereotype.Service;

/** Drafts a JUnit 5 test skeleton for a class — method stubs and the edge cases worth covering. */
@Service
public class TestStubService {

    private static final String SYSTEM =
            "You write thorough JUnit 5 test skeletons. Cover the happy path and edge cases; leave bodies empty.";

    private final GatewayService gateway;

    public TestStubService(GatewayService gateway) {
        this.gateway = gateway;
    }

    public TestStubResponse generate(String javaSource, String clientId) {
        String prompt =
                """
                Generate JUnit 5 test stubs for the following Java class. Return only the Java source of a \
                test class: methods grouped with @Nested and @DisplayName, the edge cases worth covering \
                noted as comments, and empty bodies.

                ```java
                %s
                ```"""
                        .formatted(javaSource);

        GatewayResult result =
                gateway.complete(
                        new ChatRequest(prompt, SYSTEM, 900, 0.2, clientId, false), RoutingMode.COST, null);
        return new TestStubResponse(stripCodeFences(result.content()), ProviderMeta.from(result));
    }

    private static String stripCodeFences(String content) {
        String trimmed = content.strip();
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            trimmed = firstLineBreak < 0 ? trimmed : trimmed.substring(firstLineBreak + 1);
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.strip();
    }
}
