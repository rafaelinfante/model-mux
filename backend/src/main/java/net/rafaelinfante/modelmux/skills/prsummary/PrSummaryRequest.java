package net.rafaelinfante.modelmux.skills.prsummary;

import jakarta.validation.constraints.NotBlank;

/**
 * {@code diff} drives the LLM review; {@code javaSource} (optional) is the full source of the changed
 * file, formatted deterministically by Google Java Format.
 */
public record PrSummaryRequest(@NotBlank String diff, String javaSource) {}
