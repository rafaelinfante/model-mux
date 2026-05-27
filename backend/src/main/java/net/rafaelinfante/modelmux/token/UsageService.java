package net.rafaelinfante.modelmux.token;

import java.time.Instant;
import java.util.List;
import net.rafaelinfante.modelmux.gateway.GatewayResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageService {

    private final UsageRepository repository;

    public UsageService(UsageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(GatewayResult result) {
        repository.save(new UsageRecord(result, Instant.now()));
    }

    @Transactional(readOnly = true)
    public UsageTotals totalsSince(Instant since) {
        UsageTotals totals = repository.totalsSince(since);
        return totals == null ? UsageTotals.empty() : totals;
    }

    @Transactional(readOnly = true)
    public List<ProviderUsage> perProviderSince(Instant since) {
        return repository.perProviderSince(since);
    }
}
