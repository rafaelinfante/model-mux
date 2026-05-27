package net.rafaelinfante.modelmux.provider;

/** Terminal provider failure (bad request, auth, content policy) — not worth retrying or failing over. */
public class ProviderException extends RuntimeException {

    public ProviderException(String message) {
        super(message);
    }

    public ProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
