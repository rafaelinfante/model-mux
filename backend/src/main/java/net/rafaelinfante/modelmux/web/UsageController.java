package net.rafaelinfante.modelmux.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import net.rafaelinfante.modelmux.token.BudgetService;
import net.rafaelinfante.modelmux.token.UsageService;
import net.rafaelinfante.modelmux.token.UsageTotals;
import net.rafaelinfante.modelmux.web.dto.BudgetView;
import net.rafaelinfante.modelmux.web.dto.UsageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
public class UsageController {

    private final UsageService usageService;
    private final BudgetService budgetService;

    public UsageController(UsageService usageService, BudgetService budgetService) {
        this.usageService = usageService;
        this.budgetService = budgetService;
    }

    @GetMapping
    public UsageResponse usage(
            @RequestParam(name = "hours", defaultValue = "24") int hours, HttpServletRequest http) {
        Instant since = Instant.now().minus(Duration.ofHours(hours));
        UsageTotals totals = usageService.totalsSince(since);
        BudgetView budget = BudgetView.from(budgetService.peek(ClientIdResolver.resolve(http)));
        return new UsageResponse(
                "last " + hours + "h",
                totals.requests(),
                totals.cost(),
                totals.tokens(),
                totals.cacheHits(),
                totals.failovers(),
                usageService.perProviderSince(since),
                budget);
    }
}
