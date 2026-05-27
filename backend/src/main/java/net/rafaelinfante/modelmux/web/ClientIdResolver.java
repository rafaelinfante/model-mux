package net.rafaelinfante.modelmux.web;

import jakarta.servlet.http.HttpServletRequest;

/** Resolves the budget/rate-limit identity from the {@code X-Client-Id} header. */
public final class ClientIdResolver {

    public static final String HEADER = "X-Client-Id";
    private static final String ANONYMOUS = "anonymous";

    private ClientIdResolver() {}

    public static String resolve(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        return header == null || header.isBlank() ? ANONYMOUS : header.trim();
    }
}
