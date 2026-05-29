package net.rafaelinfante.modelmux.skills.prsummary;

import java.util.List;

/** The LLM half of a PR review: a prose summary plus structured risks and test gaps. */
public record RiskReport(String summary, List<Risk> risks, List<String> testGaps) {}
