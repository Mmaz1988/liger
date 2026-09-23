package de.ukon.liger.analysis.config;

import java.nio.file.Path;
import java.util.List;

/**
 * A parsed LiGER config (*.liger.yaml). Paths are resolved against the config's directory.
 * A rule or axiom field is null when the config leaves it out; nothing is inherited from
 * anywhere else, so a config gets exactly what it lists.
 *
 * @param scope      the folder the config's files are meant to live in (the "scope" field,
 *                   default the config's own folder). A boundary, not a base: paths still
 *                   resolve against the config's folder. Files outside it are allowed and
 *                   reported.
 * @param semantics  rule files applied before semantic composition (GSWB), in order
 * @param pragmatics post-processing rule files (presuppositions, pronouns), in order
 * @param lexica     dictionary files available to lex/2 in every rule set
 * @param axioms     TPTP axiom files loaded into the axioms editor, in order
 */
public record LigerConfig(Path source, Path scope, List<Path> semantics, List<Path> pragmatics,
                          List<Path> lexica, List<Path> axioms) {
}
