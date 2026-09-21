package de.ukon.liger.analysis.config;

import java.nio.file.Path;
import java.util.List;

/**
 * What loading a config or a single rule file yields: the assembled rule text for both
 * stages and the lexica to activate.
 *
 * @param kind       "config" or "rules"
 * @param pragmatics null when neither the loaded file nor a default config supplies any
 */
public record ResolvedRuleSet(String kind, Path source, String semantics, String pragmatics, List<Path> lexica) {

    public static final String CONFIG = "config";
    public static final String RULES = "rules";
}
