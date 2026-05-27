package net.rafaelinfante.modelmux.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "budget",
        uniqueConstraints = @UniqueConstraint(name = "uq_budget_client_window", columnNames = {"client_id", "window_date"}))
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false, length = 120)
    private String clientId;

    @Column(name = "window_date", nullable = false)
    private LocalDate windowDate;

    @Column(name = "daily_limit_usd", nullable = false, precision = 12, scale = 6)
    private BigDecimal dailyLimitUsd;

    @Column(name = "spent_usd", nullable = false, precision = 12, scale = 6)
    private BigDecimal spentUsd;

    @Version
    private long version;

    protected Budget() {}

    public Budget(String clientId, LocalDate windowDate, BigDecimal dailyLimitUsd) {
        this.clientId = clientId;
        this.windowDate = windowDate;
        this.dailyLimitUsd = dailyLimitUsd;
        this.spentUsd = BigDecimal.ZERO;
    }

    public BigDecimal getDailyLimitUsd() {
        return dailyLimitUsd;
    }

    public BigDecimal getSpentUsd() {
        return spentUsd;
    }

    public BigDecimal remaining() {
        return dailyLimitUsd.subtract(spentUsd).max(BigDecimal.ZERO);
    }
}
