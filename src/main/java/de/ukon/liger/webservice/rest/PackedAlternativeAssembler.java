package de.ukon.liger.webservice.rest;

import de.ukon.liger.packing.ChoiceNode;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Packs alternative semantic graphs while retaining their source contexts. */
public final class PackedAlternativeAssembler {
    private PackedAlternativeAssembler() {
    }

    public static LinguisticStructure pack(List<LinguisticStructure> alternatives) {
        if (alternatives == null || alternatives.isEmpty()) {
            return new LinguisticStructure();
        }
        if (alternatives.size() == 1) {
            return copy(alternatives.get(0));
        }

        LinguisticStructure first = alternatives.get(0);
        LinguisticStructure packed = new LinguisticStructure();
        packed.local_id = first.local_id;
        packed.text = first.text;
        packed.constraints = packConstraints(alternatives, false);
        packed.annotation = packConstraints(alternatives, true);
        packed.cp = mergeChoiceSpaces(alternatives);

        ChoiceVar[] semanticChoices = new ChoiceVar[alternatives.size()];
        Set<ChoiceVar> daughters = new LinkedHashSet<>();
        for (int i = 0; i < alternatives.size(); i++) {
            semanticChoices[i] = new ChoiceVar("M" + (i + 1));
            daughters.add(semanticChoices[i]);
            packed.cp.choices.add(Collections.singleton(semanticChoices[i]));
        }
        packed.cp.choiceNodes.add(new ChoiceNode(
                new LinkedHashSet<>(Collections.singleton(new ChoiceVar("1"))), daughters));
        if (!packed.cp.allVariables.contains("M")) {
            packed.cp.allVariables.add("M");
        }

        return packed;
    }

    private static List<GraphConstraint> packConstraints(List<LinguisticStructure> alternatives,
                                                         boolean annotations) {
        Map<String, List<Occurrence>> occurrences = new LinkedHashMap<>();
        for (int alternative = 0; alternative < alternatives.size(); alternative++) {
            List<GraphConstraint> constraints = annotations
                    ? alternatives.get(alternative).annotation
                    : alternatives.get(alternative).constraints;
            if (constraints == null) {
                continue;
            }
            for (GraphConstraint constraint : constraints) {
                occurrences.computeIfAbsent(baseKey(constraint), ignored -> new ArrayList<>())
                        .add(new Occurrence(alternative, constraint));
            }
        }

        List<GraphConstraint> packed = new ArrayList<>();
        for (List<Occurrence> group : occurrences.values()) {
            if (isShared(group, alternatives.size())) {
                packed.add(group.get(0).constraint.copy());
                continue;
            }
            for (Occurrence occurrence : group) {
                GraphConstraint constraint = occurrence.constraint.copy();
                Set<ChoiceVar> context = constraint.getReading() == null
                        ? new LinkedHashSet<>()
                        : new LinkedHashSet<>(constraint.getReading());
                context.removeIf(choice -> "1".equals(choice.choiceID));
                context.add(new ChoiceVar("M" + (occurrence.alternative + 1)));
                constraint.setReading(context);
                packed.add(constraint);
            }
        }
        return packed;
    }

    private static boolean isShared(List<Occurrence> group, int alternativeCount) {
        if (group.size() != alternativeCount) {
            return false;
        }
        Map<Integer, String> signatures = new HashMap<>();
        for (Occurrence occurrence : group) {
            String signature = readingKey(occurrence.constraint.getReading());
            if (signatures.put(occurrence.alternative, signature) != null) {
                return false;
            }
        }
        if (signatures.size() != alternativeCount) {
            return false;
        }
        return signatures.values().stream().distinct().count() == 1;
    }

    private static String baseKey(GraphConstraint constraint) {
        return String.join("|",
                String.valueOf(constraint.getFsNode()),
                String.valueOf(constraint.getRelationLabel()),
                String.valueOf(constraint.getFsValue()),
                String.valueOf(constraint.getProj()),
                Boolean.toString(constraint.isRoot()));
    }

    private static String readingKey(Set<ChoiceVar> reading) {
        if (reading == null || reading.isEmpty()) {
            return "1";
        }
        return reading.stream().map(choice -> choice.choiceID).sorted().collect(Collectors.joining(","));
    }

    private static ChoiceSpace mergeChoiceSpaces(List<LinguisticStructure> alternatives) {
        ChoiceSpace merged = new ChoiceSpace();
        merged.choiceNodes = new ArrayList<>();
        for (LinguisticStructure alternative : alternatives) {
            if (alternative.cp == null) {
                continue;
            }
            if (alternative.cp.choiceNodes != null) {
                merged.choiceNodes.addAll(alternative.cp.choiceNodes.stream()
                        .map(de.ukon.liger.packing.ChoiceNode::copy).toList());
            }
            if (alternative.cp.choices != null) {
                for (Set<ChoiceVar> choice : alternative.cp.choices) {
                    merged.choices.add(choice.stream().map(ChoiceVar::copy)
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                }
            }
            if (alternative.cp.allVariables != null) {
                for (String variable : alternative.cp.allVariables) {
                    if (!merged.allVariables.contains(variable)) {
                        merged.allVariables.add(variable);
                    }
                }
            }
        }
        merged.rootChoice = new LinkedHashSet<>(Collections.singleton(new ChoiceVar("1")));
        return merged;
    }

    private static LinguisticStructure copy(LinguisticStructure source) {
        LinguisticStructure copy = new LinguisticStructure();
        copy.local_id = source.local_id;
        copy.text = source.text;
        copy.constraints = source.constraints == null ? new ArrayList<>() : source.constraints.stream()
                .map(GraphConstraint::copy).collect(Collectors.toCollection(ArrayList::new));
        copy.annotation = source.annotation == null ? new ArrayList<>() : source.annotation.stream()
                .map(GraphConstraint::copy).collect(Collectors.toCollection(ArrayList::new));
        copy.cp = source.cp == null ? new ChoiceSpace() : source.cp.copy();
        return copy;
    }

    private record Occurrence(int alternative, GraphConstraint constraint) {
    }
}
