package net.rafaelinfante.modelmux;

import static org.assertj.core.api.Assertions.assertThat;

import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.web.dto.ChatApiRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
            "modelmux.budgets.default-daily-usd=0.0",
            "modelmux.budgets.over-budget-action=REJECT"
        })
class BudgetEnforcementIT extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate rest;

    @Test
    void rejectsRequestsOnceTheDailyBudgetIsExhausted() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Client-Id", "over-budget-client");
        ChatApiRequest request =
                new ChatApiRequest("anything at all", null, null, null, RoutingMode.EXPLICIT, "mock-fast", false);

        ResponseEntity<String> response =
                rest.exchange("/api/chat", HttpMethod.POST, new HttpEntity<>(request, headers), String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(429);
    }
}
