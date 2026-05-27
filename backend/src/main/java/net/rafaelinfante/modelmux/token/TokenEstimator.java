package net.rafaelinfante.modelmux.token;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.stereotype.Component;

/**
 * Pre-request token estimate. Tokenizers differ per provider; this is a single cl100k_base estimate
 * used for budget pre-flight and for the mock providers' synthetic usage. Real providers always
 * override it with the exact {@code usage} they report back.
 */
@Component
public class TokenEstimator {

    private final Encoding encoding =
            Encodings.newLazyEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    public int estimate(String text) {
        return text == null || text.isEmpty() ? 0 : encoding.countTokens(text);
    }
}
