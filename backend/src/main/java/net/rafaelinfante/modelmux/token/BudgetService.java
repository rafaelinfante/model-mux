package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Per-client daily spend cap. Spend is incremented with an atomic UPDATE so concurrent requests for the
 * same client cannot lose writes; the cap is checked against what is already spent, not a reservation,
 * which keeps a single request from being charged twice.
 */
@Service
public class BudgetService {

    private final BudgetRepository repository;
    private final ModelMuxProperties.Budgets config;

    public BudgetService(BudgetRepository repository, ModelMuxProperties properties) {
        this.repository = repository;
        this.config = properties.budgets();
    }

    @Transactional
    public BudgetStatus check(String clientId) {
        if (!config.enabled()) {
            return BudgetStatus.notEnforced();
        }
        return statusOf(getOrCreate(clientId));
    }

    @Transactional(readOnly = true)
    public BudgetStatus peek(String clientId) {
        if (!config.enabled()) {
            return BudgetStatus.notEnforced();
        }
        return repository
                .findByClientIdAndWindowDate(clientId, today())
                .map(this::statusOf)
                .orElseGet(
                        () ->
                                new BudgetStatus(
                                        true,
                                        false,
                                        config.overBudgetAction(),
                                        config.defaultDailyUsd(),
                                        BigDecimal.ZERO,
                                        config.defaultDailyUsd()));
    }

    private BudgetStatus statusOf(Budget budget) {
        boolean overBudget = budget.getSpentUsd().compareTo(budget.getDailyLimitUsd()) >= 0;
        return new BudgetStatus(
                true,
                overBudget,
                config.overBudgetAction(),
                budget.getDailyLimitUsd(),
                budget.getSpentUsd(),
                budget.remaining());
    }

    @Transactional
    public void charge(String clientId, BigDecimal cost) {
        if (!config.enabled() || cost.signum() <= 0) {
            return;
        }
        if (repository.addSpend(clientId, today(), cost) == 0) {
            getOrCreate(clientId);
            repository.addSpend(clientId, today(), cost);
        }
    }

    private Budget getOrCreate(String clientId) {
        return repository
                .findByClientIdAndWindowDate(clientId, today())
                .orElseGet(() -> insert(clientId));
    }

    private Budget insert(String clientId) {
        try {
            return repository.save(new Budget(clientId, today(), config.defaultDailyUsd()));
        } catch (DataIntegrityViolationException raced) {
            return repository
                    .findByClientIdAndWindowDate(clientId, today())
                    .orElseThrow(() -> raced);
        }
    }

    private static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}
