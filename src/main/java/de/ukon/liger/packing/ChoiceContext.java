package de.ukon.liger.packing;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Operations over the choice context attached to a graph constraint. */
public final class ChoiceContext {
    private ChoiceContext() {
    }

    public static boolean isRoot(Set<ChoiceVar> context) {
        return context == null || context.isEmpty()
                || context.stream().allMatch(choice -> "1".equals(choice.choiceID));
    }

    /**
     * Returns whether two contexts can describe the same reading. Alternative
     * matches are kept as separate results; this method is only for conjunction.
     */
    public static boolean compatible(ChoiceSpace choiceSpace,
                                     Set<ChoiceVar> left,
                                     Set<ChoiceVar> right) {
        if (isRoot(left) || isRoot(right) || choiceSpace == null
                || choiceSpace.choiceNodes == null) {
            return true;
        }

        Map<String, Set<String>> ancestors = ancestors(choiceSpace);
        Set<Set<String>> alternatives = alternativeGroups(choiceSpace);
        for (ChoiceVar leftChoice : left) {
            Set<String> leftLineage = lineage(leftChoice.choiceID, ancestors);
            for (ChoiceVar rightChoice : right) {
                Set<String> rightLineage = lineage(rightChoice.choiceID, ancestors);
                for (Set<String> group : alternatives) {
                    Set<String> leftOptions = intersection(group, leftLineage);
                    Set<String> rightOptions = intersection(group, rightLineage);
                    if (!leftOptions.isEmpty() && !rightOptions.isEmpty()
                            && !leftOptions.equals(rightOptions)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static Map<String, Set<String>> ancestors(ChoiceSpace choiceSpace) {
        Map<String, Set<String>> ancestors = new HashMap<>();
        boolean changed;
        do {
            changed = false;
            for (ChoiceNode node : choiceSpace.choiceNodes) {
                Set<String> mothers = choiceIds(node.choiceNode);
                for (ChoiceVar daughter : node.daughterNodes) {
                    Set<String> lineage = ancestors.computeIfAbsent(
                            daughter.choiceID, ignored -> new HashSet<>());
                    if (lineage.addAll(mothers)) {
                        changed = true;
                    }
                    for (String mother : mothers) {
                        Set<String> motherAncestors = ancestors.get(mother);
                        if (motherAncestors != null && lineage.addAll(motherAncestors)) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
        return ancestors;
    }

    private static Set<Set<String>> alternativeGroups(ChoiceSpace choiceSpace) {
        Set<Set<String>> groups = new HashSet<>();
        for (ChoiceNode node : choiceSpace.choiceNodes) {
            Set<String> daughters = new HashSet<>();
            for (ChoiceVar daughter : node.daughterNodes) {
                daughters.add(daughter.choiceID);
            }
            if (daughters.size() > 1) {
                groups.add(daughters);
            }
        }
        return groups;
    }

    private static Set<String> lineage(String choiceID, Map<String, Set<String>> ancestors) {
        Set<String> lineage = new HashSet<>();
        lineage.add(choiceID);
        lineage.addAll(ancestors.getOrDefault(choiceID, Collections.emptySet()));
        return lineage;
    }

    private static Set<String> choiceIds(Set<Object> choices) {
        Set<String> ids = new HashSet<>();
        for (Object choice : choices) {
            if (choice instanceof ChoiceVar variable) {
                ids.add(variable.choiceID);
            } else if (choice instanceof Set<?> nested) {
                for (Object item : nested) {
                    if (item instanceof ChoiceVar variable) {
                        ids.add(variable.choiceID);
                    }
                }
            }
        }
        return ids;
    }

    private static Set<String> intersection(Set<String> left, Set<String> right) {
        Set<String> intersection = new HashSet<>(left);
        intersection.retainAll(right);
        return intersection;
    }
}
