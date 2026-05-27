package net.rafaelinfante.modelmux.web.dto;

import java.math.BigDecimal;
import java.util.List;
import net.rafaelinfante.modelmux.token.ProviderUsage;

public record UsageResponse(
        String window,
        long requests,
        BigDecimal costUsd,
        long tokens,
        long cacheHits,
        long failovers,
        List<ProviderUsage> perProvider,
        BudgetView budget) {}
