package net.rafaelinfante.modelmux.observability;

import java.util.UUID;
import org.slf4j.MDC;

public final class CorrelationId {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    private CorrelationId() {}

    /** The id bound to the current request, or a fresh one for callers outside an HTTP request. */
    public static String current() {
        String existing = MDC.get(MDC_KEY);
        return existing != null ? existing : UUID.randomUUID().toString();
    }
}
