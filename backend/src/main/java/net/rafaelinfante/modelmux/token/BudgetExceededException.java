package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;

public class BudgetExceededException extends RuntimeException {

    private final BigDecimal limitUsd;
    private final BigDecimal spentUsd;

    public BudgetExceededException(String clientId, BigDecimal limitUsd, BigDecimal spentUsd) {
        super("Daily budget exhausted for client '" + clientId + "'");
        this.limitUsd = limitUsd;
        this.spentUsd = spentUsd;
    }

    public BigDecimal getLimitUsd() {
        return limitUsd;
    }

    public BigDecimal getSpentUsd() {
        return spentUsd;
    }
}
