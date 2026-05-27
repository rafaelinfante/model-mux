package net.rafaelinfante.modelmux.gateway;

import java.util.List;

public class AllProvidersFailedException extends RuntimeException {

    public AllProvidersFailedException(List<String> attemptedProviderIds, Throwable cause) {
        super("All providers failed after trying " + attemptedProviderIds, cause);
    }
}
