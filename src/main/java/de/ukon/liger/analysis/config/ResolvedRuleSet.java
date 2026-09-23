package de.ukon.liger.analysis.config;

import java.nio.file.Path;
import java.util.List;

/**
 * What loading a config or a single rule file yields: the assembled rule text for both
 * stages and the lexica to activate.
 *
 * @param kind       "config" or "rules"
 * @param pragmatics null when neither the loaded file nor a default config supplies any
 * @param axioms     the concatenated axiom files, or null when there are none
 */
public record ResolvedRuleSet(String kind, Path source, String semantics, String pragmatics, List<Path> lexica,
                              String axioms) {

    public static final String CONFIG = "config";
    public static final String RULES = "rules";
}
