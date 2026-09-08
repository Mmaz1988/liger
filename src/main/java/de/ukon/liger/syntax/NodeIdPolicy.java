package de.ukon.liger.syntax;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defines which string values are graph node identifiers.
 *
 * Values are deliberately not considered node identifiers merely because they
 * contain letters and digits. For example, x1 remains an ordinary value until
 * the x namespace is explicitly configured.
 */
public final class NodeIdPolicy {

    // Typed IDs always carry a numeric allocation component; ordinary values
    // such as the predicate name "dog" must not become graph references.
    private static final Pattern NODE_ID = Pattern.compile("([A-Za-z])(\\d[A-Za-z0-9_]*)");
    private static final Pattern LEGACY_NUMERIC_ID = Pattern.compile("-?\\d+");

    private final Map<String, Namespace> namespaces;
    private final boolean legacyNumericIds;

    public NodeIdPolicy(Map<String, Namespace> namespaces) {
        this(namespaces, false);
    }

    public NodeIdPolicy(Map<String, Namespace> namespaces, boolean legacyNumericIds) {
        if (namespaces == null || namespaces.isEmpty()) {
            throw new IllegalArgumentException("At least one node namespace must be configured");
        }

        Map<String, Namespace> configured = new LinkedHashMap<>();
        for (Map.Entry<String, Namespace> entry : namespaces.entrySet()) {
            String prefix = Objects.requireNonNull(entry.getKey(), "Namespace prefix must not be null");
            Namespace namespace = Objects.requireNonNull(entry.getValue(), "Namespace must not be null");
            if (!prefix.matches("[A-Za-z]")) {
                throw new IllegalArgumentException("Namespace prefixes must be a single letter: " + prefix);
            }
            configured.put(prefix, namespace);
        }
        this.namespaces = Map.copyOf(configured);
        this.legacyNumericIds = legacyNumericIds;
    }

    public static NodeIdPolicy defaults() {
        Map<String, Namespace> namespaces = new LinkedHashMap<>();
        namespaces.put("f", new Namespace("f-structure", "input"));
        namespaces.put("c", new Namespace("c-structure", "cnode"));
        namespaces.put("d", new Namespace("drt", "dnode"));
        namespaces.put("g", new Namespace("glue", "gnode"));
        namespaces.put("a", new Namespace("annotation", "annotation"));
        return new NodeIdPolicy(namespaces);
    }

    /**
     * Keeps old numeric graph payloads readable while producers migrate to
     * typed IDs. New values should still use a configured namespace.
     */
    public static NodeIdPolicy legacyCompatibleDefaults() {
        NodeIdPolicy typedDefaults = defaults();
        return new NodeIdPolicy(typedDefaults.namespaces, true);
    }

    public boolean isNodeId(String value) {
        return namespaceOf(value).isPresent();
    }

    public boolean isNodeReference(String value) {
        return isNodeId(value) || (legacyNumericIds && isLegacyNumericId(value));
    }

    public Optional<Namespace> namespaceOf(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = NODE_ID.matcher(value);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        return Optional.ofNullable(namespaces.get(matcher.group(1)));
    }

    public Optional<String> familyOf(String value) {
        return namespaceOf(value).map(Namespace::family);
    }

    public Optional<String> defaultNodeTypeOf(String value) {
        return namespaceOf(value).map(Namespace::clientNodeType);
    }

    public boolean isLegacyNumericId(String value) {
        return value != null && LEGACY_NUMERIC_ID.matcher(value).matches();
    }

    public Map<String, Namespace> namespaces() {
        return namespaces;
    }

    public record Namespace(String family, String clientNodeType) {
        public Namespace {
            if (family == null || family.isBlank()) {
                throw new IllegalArgumentException("Namespace family must not be blank");
            }
            if (clientNodeType == null || clientNodeType.isBlank()) {
                throw new IllegalArgumentException("Namespace client node type must not be blank");
            }
        }
    }
}
