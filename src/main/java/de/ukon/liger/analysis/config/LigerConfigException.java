package de.ukon.liger.analysis.config;

/**
 * A LiGER config, rule file or lexicon could not be loaded. Unlike RuleParser's
 * file constructors, which log and fall back to an empty rule list, config loading
 * fails loudly: a silently empty rule set looks exactly like rules that don't match.
 */
public class LigerConfigException extends RuntimeException {

    public LigerConfigException(String message) {
        super(message);
    }

    public LigerConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
