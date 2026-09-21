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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final Set<String> FIELDS = Set.of(SEMANTICS, PRAGMATICS, LEXICA);

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
                pathList(fields, LEXICA, configFile, baseDir));
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
        try {
            return LexiconDirectives.absolutize(Files.readString(file), file.toAbsolutePath().getParent());
        } catch (IOException e) {
            throw new LigerConfigException("Failed to read rule file " + file + ": " + e, e);
        }
    }

    /** Concatenates rule files in order, each headed by a comment naming it. */
    public static String assemble(List<Path> ruleFiles) {
        StringBuilder sb = new StringBuilder();
        for (Path file : ruleFiles) {
            String text = readRuleFile(file);
            sb.append("// ===== ").append(file).append(" =====\n");
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
        if (!Files.isRegularFile(path)) {
            throw new LigerConfigException("No such rule or config file: " + path);
        }

        LigerConfig config = isConfig(path) ? load(path) : null;

        LigerConfig fallback = null;
        boolean inherits = config == null || config.pragmatics() == null || config.lexica() == null;
        if (inherits && defaultConfig != null && !isSameFile(path, defaultConfig)) {
            if (Files.isRegularFile(defaultConfig)) {
                fallback = load(defaultConfig);
            } else {
                LOGGER.warn("Default LiGER config not found at {}", defaultConfig);
            }
        }

        String semantics;
        if (config == null) {
            semantics = readRuleFile(path);
        } else {
            semantics = config.semantics() != null ? assemble(config.semantics()) : "";
        }

        List<Path> pragmaticsFiles = config != null && config.pragmatics() != null ? config.pragmatics()
                : fallback != null ? fallback.pragmatics() : null;
        String pragmatics = pragmaticsFiles != null ? assemble(pragmaticsFiles) : null;

        List<Path> lexica = config != null && config.lexica() != null ? config.lexica()
                : fallback != null && fallback.lexica() != null ? fallback.lexica()
                : LexiconRegistry.defaultPaths();

        return new ResolvedRuleSet(config != null ? ResolvedRuleSet.CONFIG : ResolvedRuleSet.RULES,
                path, semantics, pragmatics, lexica);
    }

    private static boolean isSameFile(Path a, Path b) {
        try {
            return Files.exists(b) && Files.isSameFile(a, b);
        } catch (IOException e) {
            return false;
        }
    }
}
