package net.rafaelinfante.modelmux.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.rafaelinfante.modelmux.skills.healthdigest.HealthDigestRequest;
import net.rafaelinfante.modelmux.skills.healthdigest.HealthDigestResponse;
import net.rafaelinfante.modelmux.skills.healthdigest.HealthDigestService;
import net.rafaelinfante.modelmux.skills.prsummary.PrSummaryRequest;
import net.rafaelinfante.modelmux.skills.prsummary.PrSummaryResponse;
import net.rafaelinfante.modelmux.skills.prsummary.PrSummaryService;
import net.rafaelinfante.modelmux.skills.teststub.TestStubRequest;
import net.rafaelinfante.modelmux.skills.teststub.TestStubResponse;
import net.rafaelinfante.modelmux.skills.teststub.TestStubService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The three AI-augmented developer skills, each a thin layer over the gateway. */
@RestController
@RequestMapping("/api/skills")
public class SkillsController {

    private final PrSummaryService prSummaryService;
    private final TestStubService testStubService;
    private final HealthDigestService healthDigestService;

    public SkillsController(
            PrSummaryService prSummaryService,
            TestStubService testStubService,
            HealthDigestService healthDigestService) {
        this.prSummaryService = prSummaryService;
        this.testStubService = testStubService;
        this.healthDigestService = healthDigestService;
    }

    @PostMapping("/pr-summary")
    public PrSummaryResponse prSummary(@Valid @RequestBody PrSummaryRequest body, HttpServletRequest http) {
        return prSummaryService.summarize(body.diff(), body.javaSource(), ClientIdResolver.resolve(http));
    }

    @PostMapping("/test-stub")
    public TestStubResponse testStub(@Valid @RequestBody TestStubRequest body, HttpServletRequest http) {
        return testStubService.generate(body.javaSource(), ClientIdResolver.resolve(http));
    }

    @PostMapping("/health-digest")
    public HealthDigestResponse healthDigest(
            @Valid @RequestBody HealthDigestRequest body, HttpServletRequest http) {
        return healthDigestService.digest(body.metrics(), ClientIdResolver.resolve(http));
    }
}
