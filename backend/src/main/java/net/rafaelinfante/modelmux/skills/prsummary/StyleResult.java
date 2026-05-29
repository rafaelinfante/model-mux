package net.rafaelinfante.modelmux.skills.prsummary;

/**
 * The deterministic half of a PR review. Google Java Format decides compliance — not the model — so
 * the formatting verdict is exact and reproducible. {@code formattedSource} is the tool's output for a
 * before/after view; {@code error} is set when the input was not parseable Java.
 */
public record StyleResult(boolean checked, boolean compliant, String formattedSource, String error) {

    public static StyleResult notChecked() {
        return new StyleResult(false, true, null, null);
    }
}
