package de.ukon.liger.webservice.rest;

import de.ukon.liger.syntax.GraphConstraint;
import de.ukon.liger.syntax.LinguisticStructure;
import de.ukon.liger.webservice.rest.dtos.LigerSourceSpan;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SourceIndexResolverService {
    private static final List<String> REVERSE_PATH = List.of("in_set", "GLUE", "g::", "cproj");

    public LigerSourceSpan resolve(LinguisticStructure structure, Collection<Integer> sourceIndices) {
        if (structure == null || sourceIndices == null || sourceIndices.isEmpty()) {
            return new LigerSourceSpan("", null, null);
        }

        Map<String, List<GraphConstraint>> constraintsByTarget = structure.returnFullGraph().stream()
                .filter(c -> c.getFsValue() != null)
                .collect(Collectors.groupingBy(c -> String.valueOf(c.getFsValue()), LinkedHashMap::new, Collectors.toList()));

        List<SurfaceNode> nodes = new ArrayList<>();
        for (Integer sourceIndex : sourceIndices) {
            if (sourceIndex == null) {
                continue;
            }
            String synId = "i" + sourceIndex;
            structure.returnFullGraph().stream()
                    .filter(c -> "SYN-ID".equals(c.getRelationLabel())
                            && synId.equals(normalizeIndex(c.getFsValue())))
                    .map(GraphConstraint::getFsNode)
                    .flatMap(mcNode -> resolveCNodes(mcNode, constraintsByTarget).stream())
                    .map(cNode -> surfaceNode(structure, cNode))
                    .filter(Objects::nonNull)
                    .forEach(nodes::add);
        }

        List<SurfaceNode> unique = nodes.stream().distinct()
                .sorted(Comparator.comparingInt(SurfaceNode::start).thenComparingInt(SurfaceNode::end))
                .toList();
        if (unique.isEmpty()) {
            return new LigerSourceSpan("", null, null);
        }

        String text = unique.stream().map(SurfaceNode::token).collect(Collectors.joining(" "));
        return new LigerSourceSpan(text, unique.get(0).start(), unique.get(unique.size() - 1).end());
    }

    private Set<String> resolveCNodes(String mcNode, Map<String, List<GraphConstraint>> constraintsByTarget) {
        Set<String> current = new LinkedHashSet<>();
        current.add(mcNode);
        for (String relation : REVERSE_PATH) {
            Set<String> next = new LinkedHashSet<>();
            for (String target : current) {
                for (GraphConstraint constraint : constraintsByTarget.getOrDefault(target, List.of())) {
                    if (relation.equals(constraint.getRelationLabel())) {
                        next.add(constraint.getFsNode());
                    }
                }
            }
            current = next;
            if (current.isEmpty()) {
                return Set.of();
            }
        }
        return current;
    }

    private SurfaceNode surfaceNode(LinguisticStructure structure, String node) {
        Map<String, List<GraphConstraint>> byNode = structure.returnFullGraph().stream()
                .filter(c -> node.equals(c.getFsNode()))
                .collect(Collectors.groupingBy(GraphConstraint::getRelationLabel));
        String token = value(byNode, "token");
        Integer start = integerValue(value(byNode, "start"));
        Integer end = integerValue(value(byNode, "end"));
        if (token == null || start == null || end == null) {
            return null;
        }
        return new SurfaceNode(unquote(token), start, end);
    }

    private String value(Map<String, List<GraphConstraint>> byNode, String relation) {
        List<GraphConstraint> values = byNode.getOrDefault(relation, List.of());
        return values.isEmpty() ? null : String.valueOf(values.get(0).getFsValue());
    }

    private Integer integerValue(String value) {
        if (value == null) return null;
        String normalized = value.replaceAll("[^0-9-]", "");
        return normalized.isEmpty() ? null : Integer.valueOf(normalized);
    }

    private String normalizeIndex(Object value) {
        if (value == null) return "";
        String normalized = String.valueOf(value).trim();
        return normalized.startsWith("i") ? normalized : "i" + normalized;
    }

    private String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private record SurfaceNode(String token, int start, int end) {}
}
