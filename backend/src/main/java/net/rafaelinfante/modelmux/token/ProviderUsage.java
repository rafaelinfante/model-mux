package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;

public record ProviderUsage(String providerId, Long requests, BigDecimal cost, Long tokens) {}
