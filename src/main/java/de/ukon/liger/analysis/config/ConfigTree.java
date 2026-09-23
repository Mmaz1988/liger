package de.ukon.liger.analysis.config;

import java.nio.file.Path;
import java.util.List;

/**
 * The files a config or single rule file stands for, per field, in the config's field order
 * (semantics, pragmatics, lexica, axioms). What the client's workspace shows as a tree.
 *
 * @param kind   "config" or "rules", as in {@link ResolvedRuleSet}
 * @param source the loaded path, as given
 */
public record ConfigTree(String kind, Path source, List<Section> sections) {

    /** inheritedFrom of lexica that fall back to LiGER's default lexicon rather than a config. */
    public static final String BUILTIN = "builtin";

    /**
     * @param files         null when neither the loaded file nor the default config has the field
     * @param inheritedFrom the default config's path when the field comes from it,
     *                      {@link #BUILTIN} for LiGER's default lexicon, null when it is the
     *                      loaded file's own
     */
    public record Section(String field, List<Path> files, String inheritedFrom) {

        public boolean present() {
            return files != null;
        }
    }

    public Section section(String field) {
        return sections.stream().filter(s -> s.field().equals(field)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No field " + field));
    }
}
