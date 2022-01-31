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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {


    private String left;
    private String right;
    private boolean rewrite;
    private static Pattern rulePattern = Pattern.compile("(.+)=(=|-)>(.+)");


    private List<GraphConstraint> annotation;

    public Rule(String rule) {
        generateRule(rule);
    }

    public Rule(String rule, boolean rewrite) {
        generateRule(rule);
        this.rewrite = rewrite;
    }

    public Rule(String left, String right)
    {
        this.left = left;
        this.right = right;
    }

    public Rule(String left, String right, boolean rewrite)
    {
        this.left = left;
        this.right = right;
        this.rewrite = rewrite;
    }


    public void generateRule(String rule)
    {
        Matcher rm = rulePattern.matcher(rule);
        if (rm.matches())
        {
            this.left = rm.group(1);
            this.right = rm.group(3);
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
        return   left +  "==>" +
                 right;
    }

    public boolean isRewrite() {
        return rewrite;
    }

    public void setRewrite(boolean rewrite) {
        this.rewrite = rewrite;
    }

}

