package net.rafaelinfante.modelmux.token;

import java.math.BigDecimal;
import java.math.RoundingMode;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.config.ModelMuxProperties.Pricing;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;
import org.springframework.stereotype.Component;

@Component
public class CostCalculator {

    private static final BigDecimal PER_MILLION = BigDecimal.valueOf(1_000_000);

    private final ModelMuxProperties properties;

    public CostCalculator(ModelMuxProperties properties) {
        this.properties = properties;
    }

    public BigDecimal costOf(String model, TokenUsage usage) {
        Pricing pricing = properties.pricingFor(model);
        BigDecimal input = pricing.inputPerMillion().multiply(BigDecimal.valueOf(usage.promptTokens()));
        BigDecimal output = pricing.outputPerMillion().multiply(BigDecimal.valueOf(usage.completionTokens()));
        return input.add(output).divide(PER_MILLION, 6, RoundingMode.HALF_UP);
    }
}
