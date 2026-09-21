package de.ukon.liger.analysis.config;

import java.nio.file.Path;
import java.util.List;

/**
 * A parsed LiGER config (*.liger.yaml). Paths are resolved against the config's directory.
 * A field is null when the config leaves it out, which is not the same as an empty list:
 * a missing pragmatics or lexica field is inherited from the default config.
 *
 * @param semantics  rule files applied before semantic composition (GSWB), in order
 * @param pragmatics post-processing rule files (presuppositions, pronouns), in order
 * @param lexica     dictionary files available to lex/2 in every rule set
 */
public record LigerConfig(Path source, List<Path> semantics, List<Path> pragmatics, List<Path> lexica) {
}
