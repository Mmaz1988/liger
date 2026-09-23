package de.ukon.liger.analysis.config;

import de.ukon.liger.analysis.lexicon.LexiconDirectives;
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
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Loads LiGER configs and assembles their rule files.
 *
 * <pre>
 * # paths are relative to the config file; a config lives in its grammar's liger/ folder
 * scope: .              # the folder its files belong in (default: the config's folder)
 * semantics:            # rules applied before semantic composition
 *   - semantics/degree_rules_lfgxdrt.liger
 * pragmatics:           # post-processing rules, applied to the merged syntax + DRS
 *   - pragmatics/core.liger
 *   - pragmatics/pronouns.liger
 * lexica:               # dictionaries for lex/2, available to all rules
 *   - dicts/ling_dict_eng.txt
 * axioms:               # TPTP axiom files, loaded into the client's axioms editor
 *   - axioms/degree_axioms.txt
 * </pre>
 *
 * A config gets exactly what it lists: nothing is inherited from a default config, and a
 * single rule file is a semantics stage and nothing else. Post-processing needs pragmatics
 * rules, so a rule set without them cannot be post-processed; the client says so.
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
    public static final String SCOPE = "scope";
    private static final Set<String> FIELDS = Set.of(SCOPE, SEMANTICS, PRAGMATICS, LEXICA, AXIOMS);
    public static final String CONFIG_SUFFIX = ".liger.yaml";
    /** Where a grammar keeps its LiGER config and rule files. */
    public static final String CONFIG_FOLDER = "liger";

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
                scope(fields, configFile, baseDir),
                pathList(fields, SEMANTICS, configFile, baseDir),
                pathList(fields, PRAGMATICS, configFile, baseDir),
                pathList(fields, LEXICA, configFile, baseDir),
                pathList(fields, AXIOMS, configFile, baseDir));
    }

    private static Path scope(Map<?, ?> fields, Path configFile, Path baseDir) {
        Object value = fields.get(SCOPE);
        if (value == null) {
            return baseDir;
        }
        if (!(value instanceof String raw) || raw.isBlank()) {
            throw new LigerConfigException("Field 'scope' in LiGER config " + configFile + " must be a folder path");
        }
        Path scope = baseDir.resolve(raw.strip());
        if (!Files.isDirectory(scope)) {
            throw new LigerConfigException("Scope '" + raw + "' in LiGER config " + configFile
                    + " is not a folder (resolved to " + scope + ")");
        }
        return scope;
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
     * Loads either a config or a single rule file: the assembled rule text of each stage and
     * the lexica. A field the config does not list is null (lexica: empty); a single rule
     * file has semantics only.
     */
    public static ResolvedRuleSet resolve(Path path) {
        ConfigTree tree = tree(path);

        String semantics = ResolvedRuleSet.RULES.equals(tree.kind()) ? readRuleFile(path)
                : assemble(tree.section(SEMANTICS).files());

        ConfigTree.Section pragmatics = tree.section(PRAGMATICS);
        ConfigTree.Section lexica = tree.section(LEXICA);
        ConfigTree.Section axioms = tree.section(AXIOMS);
        return new ResolvedRuleSet(tree.kind(), path, semantics,
                pragmatics.present() ? assemble(pragmatics.files()) : null,
                lexica.present() ? lexica.files() : List.of(),
                axioms.present() ? assembleAxioms(axioms.files()) : null);
    }

    /**
     * The files behind {@link #resolve}, per field and without reading them. Paths are the
     * ones {@link #assemble} names in its marker lines. Files outside the config's scope are
     * logged; the client marks them.
     */
    public static ConfigTree tree(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new LigerConfigException("No such rule or config file: " + path);
        }

        if (!isConfig(path)) {
            // A single rule file's path is made absolute like a config's files are, so a
            // client can resolve its --lexicon directives without knowing LiGER's working
            // directory; its semantics text has no marker line naming the path.
            Path file = path.toAbsolutePath();
            return new ConfigTree(ResolvedRuleSet.RULES, path, file.getParent(), List.of(
                    new ConfigTree.Section(SEMANTICS, List.of(file)),
                    new ConfigTree.Section(PRAGMATICS, null),
                    new ConfigTree.Section(LEXICA, null),
                    new ConfigTree.Section(AXIOMS, null)));
        }

        LigerConfig config = load(path);
        ConfigTree tree = new ConfigTree(ResolvedRuleSet.CONFIG, path, config.scope(), List.of(
                // A config without semantics has none, rather than no semantics stage.
                new ConfigTree.Section(SEMANTICS, config.semantics() != null ? config.semantics() : List.of()),
                new ConfigTree.Section(PRAGMATICS, config.pragmatics()),
                new ConfigTree.Section(LEXICA, config.lexica()),
                new ConfigTree.Section(AXIOMS, config.axioms())));
        for (ConfigTree.Section section : tree.sections()) {
            if (section.present()) {
                for (Path file : section.files()) {
                    if (!tree.inScope(file)) {
                        LOGGER.warn("{} lists {} under '{}', outside its scope {}", path, file, section.field(),
                                config.scope());
                    }
                }
            }
        }
        return tree;
    }

    /**
     * The LiGER configs (*.liger.yaml) of a grammar, sorted: those directly in the grammar's
     * folder and those directly in its {@code liger/} folder. Not deeper: a folder can hold
     * several grammars (grammars/dev holds a loose grammar beside a grammar folder), and one
     * grammar's config must not be offered for another.
     *
     * @param grammar the grammar's main file or its folder
     */
    public static List<Path> findConfigs(Path grammar) {
        Path root = Files.isDirectory(grammar) ? grammar : grammar.toAbsolutePath().getParent();
        if (root == null || !Files.isDirectory(root)) {
            throw new LigerConfigException("No such grammar file or directory: " + grammar);
        }
        List<Path> configs = new ArrayList<>();
        for (Path dir : List.of(root, root.resolve(CONFIG_FOLDER))) {
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(file -> Files.isRegularFile(file) && file.getFileName().toString().endsWith(CONFIG_SUFFIX))
                        .forEach(configs::add);
            } catch (IOException e) {
                throw new LigerConfigException("Failed to list LiGER configs in " + dir + ": " + e, e);
            }
        }
        configs.sort(null);
        return configs;
    }
}
