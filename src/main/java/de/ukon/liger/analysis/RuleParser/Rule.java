/*
 * "
 *     Copyright (C) 2021 Mark-Matthias Zymla
 *
 *     This file is part of the abstract syntax annotator  (https://github.com/Mmaz1988/abstract-syntax-annotator-web/blob/master/README.md).
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * "
 */

package de.ukon.liger.analysis.RuleParser;

import de.ukon.liger.syntax.GraphConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {

    public enum RuleOperator {
        ADD("==>"),
        REWRITE("=->"),
        FORK_ADD("?=>"),
        FORK_DELETE("?->");

        private final String symbol;

        RuleOperator(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }


    private String left;
    private String right;
    private String operator = "==>";
    private boolean rewrite;
    private  boolean branch;
    private boolean questionDelete;

    private int lineNumber;
    private int ruleIndex;
    private static Pattern rulePattern = Pattern.compile("(.+?)(==>|=->|\\?=>|\\?->)(.+)");

    public Rule(String rule) {
        generateRule(rule);
    }

    public Rule(String rule, boolean rewrite) {
        generateRule(rule);
        this.rewrite = rewrite;
        if (rewrite) {
            this.operator = RuleOperator.REWRITE.getSymbol();
        }
    }

    public Rule(String rule, boolean rewrite, boolean branch) {
        generateRule(rule);
        this.rewrite = rewrite;
        this.branch = branch;
        if (rewrite) {
            this.operator = RuleOperator.REWRITE.getSymbol();
        } else if (branch) {
            this.operator = RuleOperator.FORK_ADD.getSymbol();
        }
    }

    public Rule(String left, String right)
    {
        this.left = left;
        this.right = right;
        this.operator = RuleOperator.ADD.getSymbol();
    }

    public Rule(String left, String right, boolean rewrite)
    {
        this.left = left;
        this.right = right;
        this.rewrite = rewrite;
        this.operator = rewrite ? RuleOperator.REWRITE.getSymbol() : RuleOperator.ADD.getSymbol();
    }

    public Rule(String left, String right, boolean rewrite, boolean branch)
    {
        this.left = left;
        this.right = right;
        this.rewrite = rewrite;
        this.branch = branch;
        if (rewrite) {
            this.operator = RuleOperator.REWRITE.getSymbol();
        } else if (branch) {
            this.operator = RuleOperator.FORK_ADD.getSymbol();
        } else {
            this.operator = RuleOperator.ADD.getSymbol();
        }
    }

    public Rule(String left, String right, boolean rewrite, boolean branch, boolean questionDelete)
    {
        this.left = left;
        this.right = right;
        this.rewrite = rewrite;
        this.branch = branch;
        this.questionDelete = questionDelete;
        if (questionDelete) {
            this.operator = RuleOperator.FORK_DELETE.getSymbol();
        } else if (rewrite) {
            this.operator = RuleOperator.REWRITE.getSymbol();
        } else if (branch) {
            this.operator = RuleOperator.FORK_ADD.getSymbol();
        } else {
            this.operator = RuleOperator.ADD.getSymbol();
        }
    }


    public void generateRule(String rule)
    {
        Matcher rm = rulePattern.matcher(rule);
        if (rm.matches())
        {
            this.left = rm.group(1);
            this.operator = rm.group(2);
            this.right = rm.group(3);
            this.rewrite = RuleOperator.REWRITE.getSymbol().equals(this.operator);
            this.branch = RuleOperator.FORK_ADD.getSymbol().equals(this.operator);
            this.questionDelete = RuleOperator.FORK_DELETE.getSymbol().equals(this.operator);
        }
    }





    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public List<String> splitGoal() {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < right.length(); i++)
        {
            if (right.charAt(i) == '&')
            {
                i++;
                out.add(sb.toString().trim());
                sb = new StringBuilder();
            }

            if (right.charAt(i)  == '\\')
            {
                i++;
            }

            sb.append(right.charAt(i));
        }

        out.add(sb.toString().trim());

        return out;
    }


    @Override
    public String toString() {
        return   left +  " " + operator + " " +
                 right + ".";
    }

    public boolean isRewrite() {
        return rewrite;
    }

    public void setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
        if (rewrite) {
            this.operator = RuleOperator.REWRITE.getSymbol();
        } else if (!this.branch && !this.questionDelete) {
            this.operator = RuleOperator.ADD.getSymbol();
        }
    }

    public boolean isBranch() {
        return branch;
    }

    public void setBranch(boolean branch) {
        this.branch = branch;
        if (branch) {
            this.operator = RuleOperator.FORK_ADD.getSymbol();
        } else if (!this.rewrite && !this.questionDelete) {
            this.operator = RuleOperator.ADD.getSymbol();
        }
    }

    public boolean isQuestionDelete() {
        return questionDelete;
    }

    public void setQuestionDelete(boolean questionDelete) {
        this.questionDelete = questionDelete;
        if (questionDelete) {
            this.operator = RuleOperator.FORK_DELETE.getSymbol();
        } else if (!this.rewrite && !this.branch) {
            this.operator = RuleOperator.ADD.getSymbol();
        }
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
        this.rewrite = RuleOperator.REWRITE.getSymbol().equals(operator);
        this.branch = RuleOperator.FORK_ADD.getSymbol().equals(operator);
        this.questionDelete = RuleOperator.FORK_DELETE.getSymbol().equals(operator);
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getRuleIndex() {
        return ruleIndex;
    }

    public void setRuleIndex(int ruleIndex) {
        this.ruleIndex = ruleIndex;
    }





}
