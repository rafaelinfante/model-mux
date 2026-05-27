package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;

/** Aggregate usage over a window. Used directly as a JPQL constructor-expression target. */
public record UsageTotals(Long requests, BigDecimal cost, Long tokens, Long cacheHits, Long failovers) {

    public static UsageTotals empty() {
        return new UsageTotals(0L, BigDecimal.ZERO, 0L, 0L, 0L);
    }
}
