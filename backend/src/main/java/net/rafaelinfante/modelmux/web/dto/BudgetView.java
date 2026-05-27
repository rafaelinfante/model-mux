package net.rafaelinfante.modelmux.web.dto;

import java.math.BigDecimal;
import net.rafaelinfante.modelmux.token.BudgetStatus;

public record BudgetView(
        boolean enforced,
        boolean overBudget,
        String action,
        BigDecimal limitUsd,
        BigDecimal spentUsd,
        BigDecimal remainingUsd) {

    public static BudgetView from(BudgetStatus status) {
        return new BudgetView(
                status.enforced(),
                status.overBudget(),
                status.action() == null ? null : status.action().name(),
                status.limitUsd(),
                status.spentUsd(),
                status.remainingUsd());
    }
}
