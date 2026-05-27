package net.rafaelinfante.modelmux.token;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Map;
import net.rafaelinfante.modelmux.config.ModelMuxProperties;
import net.rafaelinfante.modelmux.config.ModelMuxProperties.Pricing;
import net.rafaelinfante.modelmux.provider.model.TokenUsage;
import org.junit.jupiter.api.Test;

class CostCalculatorTest {

    @Test
    void computesCostFromPricingAndUsage() {
        Map<String, Pricing> pricing =
                Map.of("big", new Pricing(new BigDecimal("3.00"), new BigDecimal("15.00")));
        CostCalculator calculator = new CostCalculator(properties(pricing));

        BigDecimal cost = calculator.costOf("big", new TokenUsage(1000, 500, 1500));

        assertThat(cost).isEqualByComparingTo("0.010500");
    }

    @Test
    void unknownModelCostsZero() {
        CostCalculator calculator = new CostCalculator(properties(Map.of()));
        assertThat(calculator.costOf("missing", TokenUsage.of(10, 10))).isEqualByComparingTo("0");
    }

    private static ModelMuxProperties properties(Map<String, Pricing> pricing) {
        return new ModelMuxProperties(null, null, pricing, null, null, null);
    }
}
