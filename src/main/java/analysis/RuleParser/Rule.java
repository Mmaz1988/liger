package analysis.RuleParser;

import syntax.xle.Prolog2Java.GraphConstraint;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {


    private String left;
    private String right;
    private static Pattern rulePattern = Pattern.compile("(.+)==>(.+)");


    private List<GraphConstraint> annotation;

    public Rule(String rule) {
        generateRule(rule);
    }

    public Rule(String left, String right)
    {
        this.left = left;
        this.right = right;
    }


    public void generateRule(String rule)
    {
        Matcher rm = rulePattern.matcher(rule);
        if (rm.matches())
        {
            this.left = rm.group(1);
            this.right = rm.group(2);
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

            if (right.charAt(i) == '\\')
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
}

