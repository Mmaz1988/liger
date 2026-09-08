package de.ukon.liger.webservice.rest.dtos;

import de.ukon.liger.analysis.RuleParser.Rule;

import java.util.Objects;

public class LigerRule {
    public String rule;
    public int index;
    public int lineNumber;

    public LigerRule(String rule, int index, int lineNumber) {
        this.rule = rule;
        this.index = index;
        this.lineNumber = lineNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LigerRule rule = (LigerRule) o;
        return rule == rule &&
                index == rule.index;

    }

    @Override
    public int hashCode() {
        return Objects.hash(rule,index);
    }

}
