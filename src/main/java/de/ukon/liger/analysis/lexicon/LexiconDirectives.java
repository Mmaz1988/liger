package de.ukon.liger.analysis.lexicon;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code --lexicon(path)} rule-file directive: adds a lexicon for lex/2 on top of the
 * globally active lexica, for the rules it appears with only. It must stand at the start of
 * a line, so a mention inside a comment is not a directive.
 */
public final class LexiconDirectives {

    private static final Pattern DIRECTIVE = Pattern.compile("(?m)^[ \\t]*--lexicon\\(([^)\\n]*)\\)[ \\t]*");

    public record Extracted(String ruleText, List<Path> lexica) {}

    private LexiconDirectives() {}

    /**
     * Removes the directives from {@code ruleText}, leaving their lines empty so rule line
     * numbers do not shift. Relative paths are resolved against {@code baseDir}.
     */
    public static Extracted extract(String ruleText, Path baseDir) {
        List<Path> lexica = new ArrayList<>();
        Matcher matcher = DIRECTIVE.matcher(ruleText);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            lexica.add(resolve(matcher.group(1), baseDir));
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return new Extracted(sb.toString(), lexica);
    }

    /**
     * Rewrites relative directive paths to absolute ones, resolved against the rule file's
     * directory. Rule text travels to LiGER as a plain string (ruleString), which has no
     * location of its own to resolve against.
     */
    public static String absolutize(String ruleText, Path baseDir) {
        Matcher matcher = DIRECTIVE.matcher(ruleText);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            Path path = resolve(matcher.group(1), baseDir);
            matcher.appendReplacement(sb, Matcher.quoteReplacement("--lexicon(" + path + ")"));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // No normalize(): a rules folder may be a symlink, and "../dicts" has to be resolved by
    // the file system, not lexically.
    private static Path resolve(String rawPath, Path baseDir) {
        Path path = Paths.get(rawPath.strip());
        return path.isAbsolute() || baseDir == null ? path : baseDir.resolve(path).toAbsolutePath();
    }
}
