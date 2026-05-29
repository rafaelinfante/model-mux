package net.rafaelinfante.modelmux.skills.healthdigest;

import jakarta.validation.constraints.NotBlank;

public record HealthDigestRequest(@NotBlank String metrics) {}
