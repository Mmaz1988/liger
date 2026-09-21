package de.ukon.liger.analysis.lexicon;

import de.ukon.liger.analysis.LinguisticDictionary;
import de.ukon.liger.analysis.config.LigerConfigException;
import de.ukon.liger.utilities.PathVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads lexicon files and holds the process-wide active lexica. A LiGER config's
 * {@code lexica} are a global setting: one config is active per parsing session, and every
 * rule set (semantics and pragmatics alike) sees its lexica. A rule file can add lexica of
 * its own with a {@code --lexicon(path)} directive; those are layered over the active set
 * for that RuleParser only.
 *
 * Parsed files are cached by real path and re-read when their modification time changes,
 * so edits to a lexicon take effect without reloading the config.
 */
public final class LexiconRegistry {

    public static final String DEFAULT_LEXICON = "ling_dict_eng.txt";

    private final static Logger LOGGER = LoggerFactory.getLogger(LexiconRegistry.class);

    private record Cached(FileTime modified, Lexicon lexicon) {}

    private static final Map<Path, Cached> CACHE = new ConcurrentHashMap<>();

    // null means "not set": the default lexicon is used.
    private static volatile List<Path> activePaths;

    private LexiconRegistry() {}

    /** Loads one lexicon file, from the cache when it has not changed on disk. */
    public static Lexicon load(Path file) {
        try {
            Path real = file.toRealPath();
            FileTime modified = Files.getLastModifiedTime(real);
            Cached cached = CACHE.get(real);
            if (cached != null && cached.modified().equals(modified)) {
                return cached.lexicon();
            }
            Lexicon lexicon = new Lexicon(LinguisticDictionary.text2Dict(real));
            CACHE.put(real, new Cached(modified, lexicon));
            return lexicon;
        } catch (IOException e) {
            throw new LigerConfigException("Failed to load lexicon file " + file + ": " + e, e);
        }
    }

    /** Loads and merges lexicon files in order; see {@link Lexicon#merge}. */
    public static Lexicon loadAll(List<Path> files) {
        List<Lexicon> lexica = new ArrayList<>();
        for (Path file : files) {
            lexica.add(load(file));
        }
        return Lexicon.merge(lexica);
    }

    /** Makes {@code files} the active lexica. Every file is loaded first, so a bad path changes nothing. */
    public static void setActive(List<Path> files) {
        loadAll(files);
        activePaths = List.copyOf(files);
        LOGGER.info("Active lexica: {}", activePaths);
    }

    /** Back to the default lexicon ({@code <resources>/dicts/ling_dict_eng.txt}). */
    public static void resetToDefault() {
        activePaths = null;
    }

    public static List<Path> activePaths() {
        List<Path> paths = activePaths;
        return paths != null ? paths : defaultPaths();
    }

    /** The active lexica, merged. */
    public static Lexicon active() {
        return loadAll(activePaths());
    }

    /**
     * The lexicon LiGER used before configs existed. Missing is not an error here: a LiGER
     * without a dicts folder simply has no lex/2 dictionaries.
     */
    public static List<Path> defaultPaths() {
        if (PathVariables.dictPath == null) {
            return List.of();
        }
        Path path = Paths.get(PathVariables.dictPath, DEFAULT_LEXICON);
        if (!Files.exists(path)) {
            LOGGER.warn("Default lexicon not found at {}", path);
            return List.of();
        }
        return List.of(path);
    }
}
