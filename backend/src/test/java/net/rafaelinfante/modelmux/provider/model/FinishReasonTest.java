package net.rafaelinfante.modelmux.provider.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FinishReasonTest {

    @Test
    void mapsKnownProviderReasons() {
        assertThat(FinishReason.fromProvider("stop")).isEqualTo(FinishReason.STOP);
        assertThat(FinishReason.fromProvider("end_turn")).isEqualTo(FinishReason.STOP);
        assertThat(FinishReason.fromProvider("max_tokens")).isEqualTo(FinishReason.LENGTH);
        assertThat(FinishReason.fromProvider("content_filter")).isEqualTo(FinishReason.CONTENT_FILTER);
    }

    @Test
    void unknownAndBlankBecomeUnknown() {
        assertThat(FinishReason.fromProvider(null)).isEqualTo(FinishReason.UNKNOWN);
        assertThat(FinishReason.fromProvider("")).isEqualTo(FinishReason.UNKNOWN);
        assertThat(FinishReason.fromProvider("something-else")).isEqualTo(FinishReason.UNKNOWN);
    }
}
