package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceNode;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class LinguisticStructureMerger {

    private LinguisticStructureMerger() {
    }

    static LinguisticStructure merge(LinguisticStructure syntax, LinguisticStructure drs) {
        if (syntax == null) {
            return copy(drs);
        }
        if (drs == null) {
            return copy(syntax);
        }

        LinguisticStructure merged = new LinguisticStructure();
        merged.local_id = syntax.local_id != null ? syntax.local_id : drs.local_id;
        merged.text = syntax.text != null ? syntax.text : drs.text;
        merged.constraints = unionConstraints(syntax.constraints, drs.constraints);
        merged.annotation = concat(syntax.annotation, drs.annotation);
        merged.cp = mergeChoiceSpace(syntax.cp, drs.cp);
        return merged;
    }

    private static LinguisticStructure copy(LinguisticStructure source) {
        if (source == null) {
            return null;
        }

        LinguisticStructure copy = new LinguisticStructure();
        copy.local_id = source.local_id;
        copy.text = source.text;
        copy.constraints = source.constraints == null ? new ArrayList<>() : new ArrayList<>(source.constraints);
        copy.annotation = source.annotation == null ? new ArrayList<>() : new ArrayList<>(source.annotation);
        copy.cp = source.cp;
        return copy;
    }

    private static List<GraphConstraint> concat(List<GraphConstraint> left, List<GraphConstraint> right) {
        List<GraphConstraint> merged = new ArrayList<>();
        if (left != null) {
            merged.addAll(left);
        }
        if (right != null) {
            merged.addAll(right);
        }
        return merged;
    }

    private static List<GraphConstraint> unionConstraints(List<GraphConstraint> left, List<GraphConstraint> right) {
        Map<String, GraphConstraint> merged = new LinkedHashMap<>();
        if (left != null) {
            for (GraphConstraint constraint : left) {
                merged.putIfAbsent(constraintKey(constraint), constraint);
            }
        }
        if (right != null) {
            for (GraphConstraint constraint : right) {
                merged.putIfAbsent(constraintKey(constraint), constraint);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private static String constraintKey(GraphConstraint constraint) {
        if (constraint == null) {
            return "null";
        }
        return String.join("|",
                safe(constraint.getFsNode()),
                safe(constraint.getRelationLabel()),
                safe(constraint.getFsValue()),
                safe(constraint.getProj()),
                Boolean.toString(constraint.isRoot()),
                constraint.getReading() == null ? "" : constraint.getReading().toString());
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static ChoiceSpace mergeChoiceSpace(ChoiceSpace left, ChoiceSpace right) {
        if (left == null) {
            return right == null ? new ChoiceSpace() : right;
        }
        if (right == null) {
            return left;
        }

        ChoiceSpace merged = new ChoiceSpace();
        merged.choiceNodes = new ArrayList<>();
        if (left.choiceNodes != null) {
            merged.choiceNodes.addAll(left.choiceNodes);
        }
        if (right.choiceNodes != null) {
            merged.choiceNodes.addAll(right.choiceNodes);
        }

        Set<Set<de.ukon.liger.packing.ChoiceVar>> choices = new LinkedHashSet<>();
        if (left.choices != null) {
            choices.addAll(left.choices);
        }
        if (right.choices != null) {
            choices.addAll(right.choices);
        }
        merged.choices = choices;

        merged.rootChoice = new LinkedHashSet<>();
        if (left.rootChoice != null) {
            merged.rootChoice.addAll(left.rootChoice);
        }
        if (right.rootChoice != null) {
            merged.rootChoice.addAll(right.rootChoice);
        }
        merged.allVariables = new ArrayList<>();
        if (left.allVariables != null) {
            merged.allVariables.addAll(left.allVariables);
        }
        if (right.allVariables != null) {
            for (String variable : right.allVariables) {
                if (!merged.allVariables.contains(variable)) {
                    merged.allVariables.add(variable);
                }
            }
        }
        return merged;
    }
}
