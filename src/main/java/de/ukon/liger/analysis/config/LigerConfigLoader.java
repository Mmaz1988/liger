package de.ukon.liger.analysis.config;

import de.ukon.liger.analysis.lexicon.LexiconDirectives;
import de.ukon.liger.analysis.lexicon.LexiconRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Loads LiGER configs and assembles their rule files.
 *
 * <pre>
 * # paths are relative to the config file
 * semantics:            # rules applied before semantic composition
 *   - degree_rules_lfgxdrt.liger
 * pragmatics:           # post-processing rules, applied to the merged syntax + DRS
 *   - pragmatics/core.liger
 *   - pragmatics/pronouns.liger
 * lexica:               # dictionaries for lex/2, available to all rules
 *   - ../dicts/ling_dict_eng.txt
 * axioms:               # TPTP axiom files, loaded into the client's axioms editor
 *   - ../../axioms/basic_axioms.txt
 * </pre>
 *
 * The rule files of a stage are concatenated in order into one rule text rather than parsed
 * one by one: templates and hierarchies are per parse, so a template defined in one file is
 * only visible to the rules of another if both are one text. A consequence is that a
 * {@code --replace(true)} in any file applies to the whole stage.
 */
public final class LigerConfigLoader {

    public static final String SEMANTICS = "semantics";
    public static final String PRAGMATICS = "pragmatics";
    public static final String LEXICA = "lexica";
    public static final String AXIOMS = "axioms";
    private static final Set<String> FIELDS = Set.of(SEMANTICS, PRAGMATICS, LEXICA, AXIOMS);
    public static final String CONFIG_SUFFIX = ".liger.yaml";

    private final static Logger LOGGER = LoggerFactory.getLogger(LigerConfigLoader.class);

    private LigerConfigLoader() {}

    public static boolean isConfig(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".yaml") || name.endsWith(".yml");
    }

    public static LigerConfig load(Path configFile) {
        Object root;
        try {
            root = new Yaml(new SafeConstructor(new LoaderOptions())).load(Files.readString(configFile));
        } catch (IOException e) {
            throw new LigerConfigException("Failed to read LiGER config " + configFile + ": " + e, e);
        } catch (YAMLException e) {
            throw new LigerConfigException("Invalid YAML in LiGER config " + configFile + ": " + e.getMessage(), e);
        }

        if (root == null) {
            root = Map.of();
        }
        if (!(root instanceof Map<?, ?> fields)) {
            throw new LigerConfigException("LiGER config " + configFile + " must be a mapping with the fields " + FIELDS);
        }
        for (Object key : fields.keySet()) {
            if (!FIELDS.contains(String.valueOf(key))) {
                throw new LigerConfigException("Unknown field '" + key + "' in LiGER config " + configFile
                        + "; expected " + FIELDS);
            }
        }

        Path baseDir = configFile.toAbsolutePath().getParent();
        return new LigerConfig(configFile,
                pathList(fields, SEMANTICS, configFile, baseDir),
                pathList(fields, PRAGMATICS, configFile, baseDir),
                pathList(fields, LEXICA, configFile, baseDir),
                pathList(fields, AXIOMS, configFile, baseDir));
    }

    // No normalize() when resolving: a rules folder may be a symlink (it is one in a local
    // liger checkout), and "../dicts" has to be resolved by the file system, not lexically.
    private static List<Path> pathList(Map<?, ?> fields, String field, Path configFile, Path baseDir) {
        if (!fields.containsKey(field)) {
            return null;
        }
        Object value = fields.get(field);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> entries)) {
            throw new LigerConfigException("Field '" + field + "' in LiGER config " + configFile + " must be a list of paths");
        }
        List<Path> paths = new ArrayList<>();
        for (Object entry : entries) {
            if (!(entry instanceof String raw) || raw.isBlank()) {
                throw new LigerConfigException("Field '" + field + "' in LiGER config " + configFile
                        + " contains a non-path entry: " + entry);
            }
            Path path = baseDir.resolve(raw.strip());
            if (!Files.isRegularFile(path)) {
                throw new LigerConfigException("File '" + raw + "' listed under '" + field + "' in LiGER config "
                        + configFile + " does not exist (resolved to " + path + ")");
            }
            paths.add(path);
        }
        return List.copyOf(paths);
    }

    /** A rule file's text, with its --lexicon directives made absolute. */
    public static String readRuleFile(Path file) {
        return LexiconDirectives.absolutize(readFile(file), file.toAbsolutePath().getParent());
    }

    private static String readFile(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new LigerConfigException("Failed to read " + file + ": " + e, e);
        }
    }

    /** Concatenates rule files in order, each headed by a comment naming it. */
    public static String assemble(List<Path> ruleFiles) {
        return concatenate(ruleFiles, "// ", LigerConfigLoader::readRuleFile);
    }

    /** The axiom files in order. Their marker lines use TPTP's comment character. */
    public static String assembleAxioms(List<Path> axiomFiles) {
        return concatenate(axiomFiles, "% ", LigerConfigLoader::readFile);
    }

    private static String concatenate(List<Path> files, String commentPrefix, Function<Path, String> read) {
        StringBuilder sb = new StringBuilder();
        for (Path file : files) {
            String text = read.apply(file);
            sb.append(commentPrefix).append("===== ").append(file).append(" =====\n");
            sb.append(text);
            if (!text.endsWith("\n")) {
                sb.append('\n');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Loads either a config or a single rule file.
     *
     * A single rule file is the semantics stage, and loading one means no config is active:
     * its pragmatics and lexica come from the default config. A config inherits pragmatics
     * or lexica from the default config only when it leaves the field out.
     *
     * @param defaultConfig may be null or missing on disk; then nothing is inherited, the
     *                      pragmatics are null and the lexica are LiGER's default lexicon
     */
    public static ResolvedRuleSet resolve(Path path, Path defaultConfig) {
        ConfigTree tree = tree(path, defaultConfig);

        String semantics = ResolvedRuleSet.RULES.equals(tree.kind()) ? readRuleFile(path)
                : assemble(tree.section(SEMANTICS).files());

        ConfigTree.Section pragmatics = tree.section(PRAGMATICS);
        ConfigTree.Section axioms = tree.section(AXIOMS);
        return new ResolvedRuleSet(tree.kind(), path, semantics,
                pragmatics.present() ? assemble(pragmatics.files()) : null,
                tree.section(LEXICA).files(),
                axioms.present() ? assembleAxioms(axioms.files()) : null);
    }

    /**
     * The files behind {@link #resolve}, per field and without reading them: which files a
     * config or single rule file stands for, and which fields came from the default config.
     * Paths are the ones {@link #assemble} names in its marker lines.
     */
    public static ConfigTree tree(Path path, Path defaultConfig) {
        if (!Files.isRegularFile(path)) {
            throw new LigerConfigException("No such rule or config file: " + path);
        }

        LigerConfig config = isConfig(path) ? load(path) : null;

        LigerConfig fallback = null;
        boolean inherits = config == null || config.pragmatics() == null || config.lexica() == null
                || config.axioms() == null;
        if (inherits && defaultConfig != null && !isSameFile(path, defaultConfig)) {
            if (Files.isRegularFile(defaultConfig)) {
                fallback = load(defaultConfig);
            } else {
                LOGGER.warn("Default LiGER config not found at {}", defaultConfig);
            }
        }

        // A config's semantics are never inherited: a config without them has none. A single
        // rule file's path is made absolute like a config's files are, so a client can resolve
        // its --lexicon directives without knowing LiGER's working directory; its semantics
        // text has no marker line naming the path.
        ConfigTree.Section semantics = config == null
                ? new ConfigTree.Section(SEMANTICS, List.of(path.toAbsolutePath()), null)
                : new ConfigTree.Section(SEMANTICS, config.semantics() != null ? config.semantics() : List.of(), null);

        ConfigTree.Section lexica = inherit(LEXICA, config, fallback, LigerConfig::lexica);
        if (!lexica.present()) {
            lexica = new ConfigTree.Section(LEXICA, LexiconRegistry.defaultPaths(), ConfigTree.BUILTIN);
        }

        return new ConfigTree(config != null ? ResolvedRuleSet.CONFIG : ResolvedRuleSet.RULES, path,
                List.of(semantics,
                        inherit(PRAGMATICS, config, fallback, LigerConfig::pragmatics),
                        lexica,
                        inherit(AXIOMS, config, fallback, LigerConfig::axioms)));
    }

    private static ConfigTree.Section inherit(String field, LigerConfig config, LigerConfig fallback,
                                              Function<LigerConfig, List<Path>> files) {
        if (config != null && files.apply(config) != null) {
            return new ConfigTree.Section(field, files.apply(config), null);
        }
        if (fallback != null && files.apply(fallback) != null) {
            return new ConfigTree.Section(field, files.apply(fallback), fallback.source().toString());
        }
        return new ConfigTree.Section(field, null, null);
    }

    /**
     * The LiGER configs (*.liger.yaml) in a grammar's directory and below, sorted. A config
     * may sit anywhere under the grammar; XLE's *.fileindexdir folders are skipped.
     *
     * @param grammar the grammar's main file or its directory
     */
    public static List<Path> findConfigs(Path grammar) {
        Path root = Files.isDirectory(grammar) ? grammar : grammar.toAbsolutePath().getParent();
        if (root == null || !Files.isDirectory(root)) {
            throw new LigerConfigException("No such grammar file or directory: " + grammar);
        }
        List<Path> configs = new ArrayList<>();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return dir.getFileName().toString().endsWith(".fileindexdir")
                            ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && file.getFileName().toString().endsWith(CONFIG_SUFFIX)) {
                        configs.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    LOGGER.warn("Skipping {} while looking for LiGER configs: {}", file, e.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new LigerConfigException("Failed to list LiGER configs under " + root + ": " + e, e);
        }
        configs.sort(null);
        return configs;
    }

    private static boolean isSameFile(Path a, Path b) {
        try {
            return Files.exists(b) && Files.isSameFile(a, b);
        } catch (IOException e) {
            return false;
        }
    }
}
