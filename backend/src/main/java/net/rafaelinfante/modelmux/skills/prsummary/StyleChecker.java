package net.rafaelinfante.modelmux.skills.prsummary;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import org.springframework.stereotype.Component;

/** Runs Google Java Format over a source file to report style compliance deterministically. */
@Component
public class StyleChecker {

    public StyleResult check(String source) {
        if (source == null || source.isBlank()) {
            return StyleResult.notChecked();
        }
        try {
            String formatted = new Formatter().formatSource(source);
            return new StyleResult(true, formatted.equals(source), formatted, null);
        } catch (FormatterException e) {
            return new StyleResult(true, false, null, "Input is not valid Java: " + e.getMessage());
        }
    }
}
