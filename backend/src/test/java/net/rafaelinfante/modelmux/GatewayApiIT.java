package net.rafaelinfante.modelmux;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.rafaelinfante.modelmux.gateway.GatewayService;
import net.rafaelinfante.modelmux.gateway.StreamEvent;
import net.rafaelinfante.modelmux.gateway.compare.CompareArm;
import net.rafaelinfante.modelmux.gateway.compare.CompareResponse;
import net.rafaelinfante.modelmux.gateway.routing.RoutingMode;
import net.rafaelinfante.modelmux.provider.model.ChatRequest;
import net.rafaelinfante.modelmux.skills.healthdigest.HealthDigestRequest;
import net.rafaelinfante.modelmux.skills.healthdigest.HealthDigestResponse;
import net.rafaelinfante.modelmux.skills.prsummary.PrSummaryRequest;
import net.rafaelinfante.modelmux.skills.prsummary.PrSummaryResponse;
import net.rafaelinfante.modelmux.skills.teststub.TestStubRequest;
import net.rafaelinfante.modelmux.skills.teststub.TestStubResponse;
import net.rafaelinfante.modelmux.web.dto.ChatApiRequest;
import net.rafaelinfante.modelmux.web.dto.ChatApiResponse;
import net.rafaelinfante.modelmux.web.dto.CompareApiRequest;
import net.rafaelinfante.modelmux.web.dto.ProviderInfo;
import net.rafaelinfante.modelmux.web.dto.UsageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

class GatewayApiIT extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate rest;
    @Autowired private GatewayService gateway;

    @Test
    void explicitRoutingHitsTheNamedProvider() {
        ChatApiResponse response = chat("hello gateway", RoutingMode.EXPLICIT, "mock-fast", false);

        assertThat(response.provider()).isEqualTo("mock-fast");
        assertThat(response.content()).isNotBlank();
        assertThat(response.totalTokens()).isPositive();
        assertThat(response.correlationId()).isNotBlank();
    }

    @Test
    void costRoutingPicksTierFromComplexity() {
        assertThat(chat("what time is it", RoutingMode.COST, null, false).tier()).isEqualTo("CHEAP");
        assertThat(chat("design the architecture and analyze the trade-offs", RoutingMode.COST, null, false).tier())
                .isEqualTo("PREMIUM");
    }

    @Test
    void identicalRequestsAreServedFromCache() {
        ChatApiRequest request =
                new ChatApiRequest("a uniquely cached prompt 98765", null, null, null, RoutingMode.EXPLICIT, "mock-fast", false);

        assertThat(post(request).cacheHit()).isFalse();
        assertThat(post(request).cacheHit()).isTrue();
    }

    @Test
    void forcedFailoverFallsThroughToTheNextProvider() {
        ChatApiResponse response = chat("trigger a failover", RoutingMode.FAILOVER, null, true);

        assertThat(response.failedOver()).isTrue();
        assertThat(response.provider()).isEqualTo("mock-fast");
    }

    @Test
    void compareReturnsEveryArm() {
        ResponseEntity<CompareResponse> response =
                rest.postForEntity(
                        "/api/compare",
                        new CompareApiRequest("compare these providers", null, null, null, null),
                        CompareResponse.class);

        assertThat(response.getBody().arms()).hasSize(2).allMatch(CompareArm::ok);
    }

    @Test
    void streamingEndsWithUsageMetadata() {
        List<StreamEvent> events =
                gateway.stream(ChatRequest.of("stream a handful of words"), RoutingMode.EXPLICIT, "mock-fast")
                        .collectList()
                        .block();

        assertThat(events).anyMatch(e -> "delta".equals(e.type()));
        StreamEvent last = events.get(events.size() - 1);
        assertThat(last.type()).isEqualTo("done");
        assertThat(last.meta().usage().totalTokens()).isPositive();
    }

    @Test
    void providersEndpointListsTheMocks() {
        ProviderInfo[] providers = rest.getForObject("/api/providers", ProviderInfo[].class);
        assertThat(providers).extracting(ProviderInfo::id).contains("mock-fast", "mock-smart");
    }

    @Test
    void usageAccumulatesAndReportsBudget() {
        post(new ChatApiRequest("a request to count", null, null, null, RoutingMode.EXPLICIT, "mock-fast", false));

        UsageResponse usage = rest.getForObject("/api/usage", UsageResponse.class);
        assertThat(usage.requests()).isPositive();
        assertThat(usage.budget()).isNotNull();
    }

    @Test
    void blankPromptIsRejected() {
        ResponseEntity<String> response =
                rest.postForEntity(
                        "/api/chat",
                        new ChatApiRequest("", null, null, null, RoutingMode.COST, null, false),
                        String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void unknownExplicitProviderIsNotFound() {
        ResponseEntity<String> response =
                rest.postForEntity(
                        "/api/chat",
                        new ChatApiRequest("hi", null, null, null, RoutingMode.EXPLICIT, "does-not-exist", false),
                        String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void prSummarySkillReturnsReviewAndDeterministicStyleCheck() {
        PrSummaryResponse response =
                rest.postForObject(
                        "/api/skills/pr-summary",
                        new PrSummaryRequest("diff --git a/A.java b/A.java\n+    int x;", "class A{int x;}"),
                        PrSummaryResponse.class);

        assertThat(response.review().summary()).isNotBlank();
        assertThat(response.review().risks()).isNotEmpty();
        assertThat(response.styleCheck().checked()).isTrue();
        assertThat(response.styleCheck().compliant()).isFalse();
    }

    @Test
    void testStubSkillReturnsJUnitSkeleton() {
        TestStubResponse response =
                rest.postForObject(
                        "/api/skills/test-stub",
                        new TestStubRequest("public class Calculator { public int add(int a, int b) { return a + b; } }"),
                        TestStubResponse.class);

        assertThat(response.testSource()).contains("@Test");
    }

    @Test
    void healthDigestSkillReturnsProse() {
        HealthDigestResponse response =
                rest.postForObject(
                        "/api/skills/health-digest",
                        new HealthDigestRequest("errorRate=0.02 p99=120ms cpu=40%"),
                        HealthDigestResponse.class);

        assertThat(response.digest()).isNotBlank();
    }

    private ChatApiResponse chat(String prompt, RoutingMode mode, String provider, boolean forceFailover) {
        return post(new ChatApiRequest(prompt, null, null, null, mode, provider, forceFailover));
    }

    private ChatApiResponse post(ChatApiRequest request) {
        ResponseEntity<ChatApiResponse> response = rest.postForEntity("/api/chat", request, ChatApiResponse.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return response.getBody();
    }
}
