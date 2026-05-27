package net.rafaelinfante.modelmux.gateway.routing;

public enum RoutingMode {
    /** Caller names the provider explicitly. */
    EXPLICIT,
    /** Prompt complexity decides cheap vs premium tier. */
    COST,
    /** Try providers in order, falling through on transient failure. */
    FAILOVER
}
