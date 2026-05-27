package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;

public record BudgetStatus(
        boolean enforced,
        boolean overBudget,
        OverBudgetAction action,
        BigDecimal limitUsd,
        BigDecimal spentUsd,
        BigDecimal remainingUsd) {

    public static BudgetStatus notEnforced() {
        return new BudgetStatus(false, false, OverBudgetAction.REJECT, null, null, null);
    }

    public boolean shouldReject() {
        return overBudget && action == OverBudgetAction.REJECT;
    }

    public boolean shouldDowngrade() {
        return overBudget && action == OverBudgetAction.DOWNGRADE;
    }
}
