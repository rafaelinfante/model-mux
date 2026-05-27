package net.rafaelinfante.modelmux.provider;

/**
 * Transient provider failure (timeout, 429, 5xx). These are the only failures Resilience4j retries and
 * counts toward opening a provider's circuit breaker; a model declining or refusing is a normal result,
 * not one of these.
 */
public class TransientProviderException extends ProviderException {

    public TransientProviderException(String message) {
        super(message);
    }

    public TransientProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
