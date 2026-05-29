package net.rafaelinfante.modelmux.skills.prsummary;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StyleCheckerTest {

    private final StyleChecker checker = new StyleChecker();

    @Test
    void flagsUnformattedSourceAndFormatsIt() {
        StyleResult result = checker.check("class A{int x;}");

        assertThat(result.checked()).isTrue();
        assertThat(result.compliant()).isFalse();
        assertThat(result.formattedSource()).isNotNull();
        assertThat(checker.check(result.formattedSource()).compliant()).isTrue();
    }

    @Test
    void reportsInvalidJava() {
        StyleResult result = checker.check("this is not valid java %%%");

        assertThat(result.checked()).isTrue();
        assertThat(result.error()).isNotNull();
    }

    @Test
    void blankInputIsNotChecked() {
        assertThat(checker.check("   ").checked()).isFalse();
    }
}
