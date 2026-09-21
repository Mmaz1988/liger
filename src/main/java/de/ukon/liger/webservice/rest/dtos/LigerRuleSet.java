package de.ukon.liger.webservice.rest.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * /load_rules' answer for a LiGER config or a single rule file: the rule text of both stages,
 * ready to send back as ruleString, and the lexica LiGER activated for lex/2.
 */
public class LigerRuleSet {

    /** "config" or "rules". */
    public String kind;
    /** The path that was loaded, as requested. */
    public String source;
    /** Rules applied before semantic composition. */
    public String semantics;
    /** Post-processing rules; null when neither the file nor the default config has any. */
    public String pragmatics;
    /** The now-active lexica, as real paths. */
    public List<String> lexica = new ArrayList<>();

    public LigerRuleSet() {
    }

    public LigerRuleSet(String kind, String source, String semantics, String pragmatics, List<String> lexica) {
        this.kind = kind;
        this.source = source;
        this.semantics = semantics;
        this.pragmatics = pragmatics;
        this.lexica = lexica;
    }
}
