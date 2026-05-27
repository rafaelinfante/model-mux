package net.rafaelinfante.modelmux.token;

public enum OverBudgetAction {
    /** Reject the request with HTTP 429. */
    REJECT,
    /** Force the request onto the cheapest tier instead of rejecting. */
    DOWNGRADE
}
