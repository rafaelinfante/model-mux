package net.rafaelinfante.modelmux.skills.prsummary;

import net.rafaelinfante.modelmux.skills.ProviderMeta;

public record PrSummaryResponse(RiskReport review, StyleResult styleCheck, ProviderMeta meta) {}
