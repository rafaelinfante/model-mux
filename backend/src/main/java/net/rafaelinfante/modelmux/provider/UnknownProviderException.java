package net.rafaelinfante.modelmux.provider;

public class UnknownProviderException extends RuntimeException {

    public UnknownProviderException(String providerId) {
        super("No provider registered with id '" + providerId + "'");
    }
}
