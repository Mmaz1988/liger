package de.ukon.liger.semantics;

import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceNode;
import de.ukon.liger.packing.ChoiceVar;
import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.syntax.xle.Fstructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Builds a sequence graph without extracting semantics from its constituents first. */
public final class SequenceGraphAssembler {

    private static final Pattern NUMERIC_ID = Pattern.compile("(.+?)(\\d+)");
    private static final Pattern SYN_ID = Pattern.compile("i(\\d+)");

    private SequenceGraphAssembler() {
    }

    public static Fstructure assemble(List<? extends LinguisticStructure> structures) {
        List<Part> parts = new ArrayList<>();
        for (int i = 0; structures != null && i < structures.size(); i++) {
            LinguisticStructure structure = structures.get(i);
            parts.add(new Part(null, structure == null ? null : structure.local_id,
                    structure == null ? null : structure.local_id, null, structure));
        }
        return assembleDetailed(parts).structure();
    }

    public static AssemblyResult assembleDetailed(List<Part> parts) {
        List<? extends LinguisticStructure> structures = parts == null
                ? Collections.emptyList()
                : parts.stream().map(Part::structure).toList();
        if (structures == null || structures.isEmpty()) {
            throw new IllegalArgumentException("At least one structure is required");
        }

        ChoiceSpace choiceSpace = new ChoiceSpace();
        choiceSpace.choiceNodes = new ArrayList<>();
        choiceSpace.rootChoice = new LinkedHashSet<>();
        choiceSpace.choices = new LinkedHashSet<>();
        choiceSpace.allVariables = new ArrayList<>();
        List<String> texts = new ArrayList<>();

        for (int i = 0; i < structures.size(); i++) {
            LinguisticStructure structure = structures.get(i);
            if (structure == null) {
                continue;
            }
            texts.add(structure.text == null ? "" : structure.text);
            ChoiceSpace sourceChoiceSpace = structure.cp == null ? new ChoiceSpace() : structure.cp;
            mergeChoiceSpace(choiceSpace, sourceChoiceSpace, i);
        }

        RebasedSequence sequence = rebaseSequence(structures);
        List<GraphConstraint> constraints = sequence.constraints();
        List<GraphConstraint> annotations = sequence.annotations();
        List<String> roots = sequence.roots();
        for (int i = 1; i < roots.size(); i++) {
            String previousRoot = roots.get(i - 1);
            String currentRoot = roots.get(i);
            if (previousRoot == null || currentRoot == null) {
                continue;
            }
            GraphConstraint next = new GraphConstraint(
                    new LinkedHashSet<>(choiceSpace.rootChoice),
                    previousRoot,
                    "NEXT",
                    currentRoot,
                    "c",
                    false);
            constraints.add(next);
        }

        String text = String.join("\n", texts);
        Fstructure result = new Fstructure("sequence", text, constraints, choiceSpace);
        result.annotation = annotations;
        List<PartProvenance> provenance = new ArrayList<>();
        LinkedHashMap<String, String> rebasedIds = new LinkedHashMap<>();
        int presentIndex = 0;
        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            if (part.structure() == null) {
                continue;
            }
            Map<String, String> idMap = sequence.idMaps().get(presentIndex);
            String root = sequence.roots().size() > presentIndex ? sequence.roots().get(presentIndex) : null;
            LinkedHashMap<String, String> orderedMap = new LinkedHashMap<>(idMap);
            for (Map.Entry<String, String> entry : orderedMap.entrySet()) {
                rebasedIds.put(i + ":" + entry.getKey(), entry.getValue());
            }
            PartProvenance source = new PartProvenance(i, part.sentenceId(), part.syntaxVariantId(),
                    part.solutionKey(), part.side(), root, orderedMap);
            provenance.add(source);
            addProvenance(annotations, choiceSpace.rootChoice, root, source);
            presentIndex++;
        }
        return new AssemblyResult(result, provenance, rebasedIds);
    }

    private record RebasedSequence(List<GraphConstraint> constraints,
                                   List<GraphConstraint> annotations,
                                   List<String> roots,
                                   List<Map<String, String>> idMaps) {
    }

    public record Part(String sentenceId, String syntaxVariantId, String solutionKey,
                       String side, LinguisticStructure structure) {}

    public record PartProvenance(int sourceIndex, String sentenceId, String syntaxVariantId,
                                 String solutionKey, String side, String rootId,
                                 LinkedHashMap<String, String> rebasedIds) {}

    public record AssemblyResult(Fstructure structure, List<PartProvenance> provenance,
                                 LinkedHashMap<String, String> rebasedIds) {}

    private static RebasedSequence rebaseSequence(
            List<? extends LinguisticStructure> structures) {
        List<GraphConstraint> constraints = new ArrayList<>();
        List<GraphConstraint> annotations = new ArrayList<>();
        List<String> roots = new ArrayList<>();
        List<Map<String, String>> idMaps = new ArrayList<>();
        int synNext = 0;
        Map<String, Integer> nodeOffsets = new HashMap<>();
        Set<String> usedNodeIds = new LinkedHashSet<>();
        for (int structureIndex = 0; structureIndex < structures.size(); structureIndex++) {
            LinguisticStructure structure = structures.get(structureIndex);
            if (structure == null) {
                continue;
            }
            List<GraphConstraint> originalConstraints = structure.constraints == null
                    ? Collections.emptyList()
                    : structure.constraints;
            List<GraphConstraint> originalAnnotations = structure.annotation == null
                    ? Collections.emptyList()
                    : structure.annotation;
            List<GraphConstraint> originalAll = new ArrayList<>(originalConstraints);
            originalAll.addAll(originalAnnotations);
            int constraintMin = minIndexedId(originalConstraints);
            int annotationMin = minIndexedId(originalAnnotations);
            int sentenceMin = constraintMin < 0
                    ? annotationMin
                    : annotationMin < 0 ? constraintMin : Math.min(constraintMin, annotationMin);
            int sentenceMax = Math.max(maxIndexedId(originalConstraints), maxIndexedId(originalAnnotations));
            CopyResult copyResult = copyAndRebase(originalAll, nodeOffsets, usedNodeIds, structureIndex);
            List<GraphConstraint> copied = copyResult.constraints();
            for (GraphConstraint constraint : copied) {
                constraint.setReading(rebaseReading(constraint.getReading(), structureIndex));
            }
            idMaps.add(copyResult.idMap());
            int synShift = sentenceMax < 0 || synNext == 0 ? 0 : synNext - sentenceMin;
            for (int i = 0; i < copied.size(); i++) {
                GraphConstraint constraint = copied.get(i);
                if (isIndexedRelation(constraint.getRelationLabel())) {
                    Matcher matcher = SYN_ID.matcher(String.valueOf(constraint.getFsValue()));
                    if (matcher.matches()) {
                        int value = Integer.parseInt(matcher.group(1)) + synShift;
                        constraint.setFsValue("i" + value);
                    }
                }
            }

            int constraintCount = originalConstraints.size();
            List<GraphConstraint> copiedConstraints = copied.subList(0, constraintCount);
            String root = copiedConstraints.stream()
                    .filter(GraphConstraint::isRoot)
                    .map(GraphConstraint::getFsNode)
                    .findFirst()
                    .orElseGet(() -> copiedConstraints.stream()
                            .filter(c -> "c".equals(c.getProj()))
                            .map(GraphConstraint::getFsNode)
                            .findFirst()
                            .orElseGet(() -> copiedConstraints.stream()
                                    .map(GraphConstraint::getFsNode)
                                    .filter(Objects::nonNull)
                                    .findFirst().orElse(null)));
            roots.add(root);

            constraints.addAll(copiedConstraints);
            annotations.addAll(copied.subList(constraintCount, copied.size()));
            if (sentenceMax >= 0) {
                synNext = Math.max(synNext, synShift + sentenceMax + 1);
            }
        }
        return new RebasedSequence(constraints, annotations, roots, idMaps);
    }

    private record CopyResult(List<GraphConstraint> constraints, Map<String, String> idMap) {}

    private static CopyResult copyAndRebase(List<GraphConstraint> source,
                                            Map<String, Integer> offsets,
                                            Set<String> usedNodeIds,
                                            int partIndex) {
        Set<String> localNodeIds = source.stream().map(GraphConstraint::getFsNode)
                .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Integer> localMax = new HashMap<>();
        Map<String, Integer> localMin = new HashMap<>();
        for (GraphConstraint constraint : source) {
            recordNumericId(constraint.getFsNode(), localMin, localMax);
            recordNumericId(String.valueOf(constraint.getFsValue()), localMin, localMax);
        }
        Map<String, Integer> applied = new HashMap<>();
        for (String namespace : localMax.keySet()) {
            int nextAvailable = offsets.getOrDefault(namespace, 0);
            int shift = nextAvailable == 0
                    ? 0
                    : nextAvailable - localMin.get(namespace);
            applied.put(namespace, shift);
            offsets.put(namespace, Math.max(nextAvailable,
                    shift + localMax.get(namespace) + 1));
        }

        List<GraphConstraint> result = new ArrayList<>();
        LinkedHashMap<String, String> idMap = new LinkedHashMap<>();
        for (String id : localNodeIds) {
            String rebased = rebaseReference(id, applied);
            if (usedNodeIds.contains(rebased)) {
                rebased = "s" + (partIndex + 1) + "_" + rebased;
            }
            usedNodeIds.add(rebased);
            idMap.put(id, rebased);
        }
        for (GraphConstraint original : source) {
            GraphConstraint copy = original.copy();
            copy.setFsNode(idMap.getOrDefault(copy.getFsNode(), copy.getFsNode()));
            if (!"SYN-ID".equals(copy.getRelationLabel())) {
                Object originalValue = copy.getFsValue();
                copy.setFsValue(originalValue == null
                        ? null
                        : idMap.getOrDefault(String.valueOf(originalValue), String.valueOf(originalValue)));
            }
            result.add(copy);
        }
        return new CopyResult(result, idMap);
    }

    private static Set<ChoiceVar> rebaseReading(Set<ChoiceVar> reading, int partIndex) {
        if (reading == null) {
            return new LinkedHashSet<>();
        }
        return reading.stream().map(choice -> {
            ChoiceVar copy = choice.copy();
            if (!"1".equals(copy.choiceID)) {
                copy.choiceID = "s" + (partIndex + 1) + "_" + copy.choiceID;
            }
            return copy;
        }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static void recordNumericId(String value,
                                        Map<String, Integer> minValues,
                                        Map<String, Integer> maxValues) {
        if (value == null) {
            return;
        }
        Matcher matcher = NUMERIC_ID.matcher(value);
        if (matcher.matches()) {
            int numericId = Integer.parseInt(matcher.group(2));
            minValues.merge(matcher.group(1), numericId, Math::min);
            maxValues.merge(matcher.group(1), numericId, Math::max);
        }
    }

    private static String rebaseReference(String value, Map<String, Integer> offsets) {
        if (value == null) {
            return null;
        }
        Matcher matcher = NUMERIC_ID.matcher(value);
        if (!matcher.matches()) {
            return value;
        }
        int offset = offsets.getOrDefault(matcher.group(1), 0);
        return matcher.group(1) + (Integer.parseInt(matcher.group(2)) + offset);
    }

    private static int maxIndexedId(List<GraphConstraint> constraints) {
        int max = 0;
        for (GraphConstraint constraint : constraints) {
            if (!isIndexedRelation(constraint.getRelationLabel())) {
                continue;
            }
            Matcher matcher = SYN_ID.matcher(String.valueOf(constraint.getFsValue()));
            if (matcher.matches()) {
                max = Math.max(max, Integer.parseInt(matcher.group(1)));
            }
        }
        return max;
    }

    private static int minIndexedId(List<GraphConstraint> constraints) {
        int min = Integer.MAX_VALUE;
        for (GraphConstraint constraint : constraints) {
            if (!isIndexedRelation(constraint.getRelationLabel())) {
                continue;
            }
            Matcher matcher = SYN_ID.matcher(String.valueOf(constraint.getFsValue()));
            if (matcher.matches()) {
                min = Math.min(min, Integer.parseInt(matcher.group(1)));
            }
        }
        return min == Integer.MAX_VALUE ? -1 : min;
    }

    private static boolean isIndexedRelation(String relation) {
        return "SYN-ID".equals(relation) || "SRC".equals(relation);
    }

    private static void mergeChoiceSpace(ChoiceSpace target, ChoiceSpace source, int partIndex) {
        if (source == null) {
            return;
        }
        java.util.function.Function<ChoiceVar, ChoiceVar> rebase = choice -> {
            ChoiceVar copy = choice.copy();
            if (!"1".equals(copy.choiceID)) {
                copy.choiceID = "s" + (partIndex + 1) + "_" + copy.choiceID;
            }
            return copy;
        };
        if (source.rootChoice != null) {
            source.rootChoice.stream().map(rebase).forEach(target.rootChoice::add);
        }
        if (source.choiceNodes != null) {
            source.choiceNodes.stream().map(node -> rebaseChoiceNode(node, rebase))
                    .forEach(target.choiceNodes::add);
        }
        if (source.choices != null) {
            source.choices.stream()
                    .map(set -> set.stream().map(rebase).collect(Collectors.toCollection(LinkedHashSet::new)))
                    .forEach(target.choices::add);
        }
        if (source.allVariables != null) {
            source.allVariables.stream().map(variable -> "s" + (partIndex + 1) + "_" + variable)
                    .filter(variable -> !target.allVariables.contains(variable))
                    .forEach(target.allVariables::add);
        }
    }

    private static ChoiceNode rebaseChoiceNode(ChoiceNode source,
                                                java.util.function.Function<ChoiceVar, ChoiceVar> rebase) {
        Set<Object> mothers = rebaseChoiceSet(source.choiceNode, rebase);
        Set<ChoiceVar> daughters = (source.daughterNodes == null
                ? java.util.stream.Stream.<ChoiceVar>empty() : source.daughterNodes.stream()).map(rebase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ChoiceNode(mothers, daughters);
    }

    private static Set<Object> rebaseChoiceSet(java.util.Collection<?> source,
                                                java.util.function.Function<ChoiceVar, ChoiceVar> rebase) {
        Set<Object> result = new LinkedHashSet<>();
        if (source == null) {
            return result;
        }
        for (Object value : source) {
            if (value instanceof ChoiceVar choice) {
                result.add(rebase.apply(choice));
            } else if (value instanceof java.util.Collection<?> nested) {
                result.add(rebaseChoiceSet(nested, rebase));
            } else {
                result.add(value);
            }
        }
        return result;
    }

    private static void addProvenance(List<GraphConstraint> annotations, Set<ChoiceVar> reading,
                                      String root, PartProvenance provenance) {
        if (root == null) {
            return;
        }
        addMetadata(annotations, reading, root, "SENTENCE-ID", provenance.sentenceId());
        addMetadata(annotations, reading, root, "SYNTAX-VARIANT-ID", provenance.syntaxVariantId());
        addMetadata(annotations, reading, root, "SOLUTION-KEY", provenance.solutionKey());
    }

    private static void addMetadata(List<GraphConstraint> annotations, Set<ChoiceVar> reading,
                                    String root, String label, String value) {
        if (value != null && !value.isBlank()) {
            annotations.add(new GraphConstraint(reading.stream().map(ChoiceVar::copy)
                    .collect(Collectors.toCollection(LinkedHashSet::new)), root, label, value, "c", false));
        }
    }
}
