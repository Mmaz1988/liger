package de.ukon.liger.analysis.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The files a config or single rule file stands for, per field, in the config's field order
 * (semantics, pragmatics, lexica, axioms). What the client's workspace shows as a tree.
 *
 * @param kind   "config" or "rules", as in {@link ResolvedRuleSet}
 * @param source the loaded path, as given
 * @param scope  the config's scope folder, absolute; for a single rule file, its folder
 */
public record ConfigTree(String kind, Path source, Path scope, List<Section> sections) {

    /** @param files null when the loaded file does not list the field */
    public record Section(String field, List<Path> files) {

        public boolean present() {
            return files != null;
        }
    }

    /** Whether {@code file} lies inside {@link #scope}, after resolving symlinks and "..". */
    public boolean inScope(Path file) {
        try {
            return file.toRealPath().startsWith(scope.toRealPath());
        } catch (IOException e) {
            return file.toAbsolutePath().normalize().startsWith(scope.toAbsolutePath().normalize());
        }
    }

    public Section section(String field) {
        return sections.stream().filter(s -> s.field().equals(field)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No field " + field));
    }
}
