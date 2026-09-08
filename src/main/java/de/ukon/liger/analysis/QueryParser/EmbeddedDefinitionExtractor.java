package de.ukon.liger.analysis.QueryParser;

public final class EmbeddedDefinitionExtractor {

    private EmbeddedDefinitionExtractor() {
    }

    public static Result extract(String input) {
        TemplateRegistry templateRegistry = new TemplateRegistry();
        HierarchyRegistry hierarchyRegistry = new HierarchyRegistry();

        if (input == null || input.isBlank()) {
            return new Result("", templateRegistry, hierarchyRegistry);
        }

        StringBuilder templateDefinitions = new StringBuilder();
        StringBuilder hierarchyDefinitions = new StringBuilder();
        StringBuilder sanitized = new StringBuilder();
        StringBuilder pendingDefinition = new StringBuilder();

        enum DefinitionType {
            NONE,
            TEMPLATE,
            HIERARCHY
        }

        DefinitionType pendingType = DefinitionType.NONE;

        String[] lines = input.split("\\R", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();

            if (pendingType != DefinitionType.NONE) {
                if (!trimmed.startsWith("//") && !trimmed.isEmpty()) {
                    if (pendingDefinition.length() > 0) {
                        pendingDefinition.append(' ');
                    }
                    pendingDefinition.append(trimmed);
                }

                if (trimmed.endsWith(".")) {
                    if (pendingType == DefinitionType.TEMPLATE) {
                        templateDefinitions.append(pendingDefinition).append(' ');
                    } else if (pendingType == DefinitionType.HIERARCHY) {
                        hierarchyDefinitions.append(pendingDefinition).append(' ');
                    }
                    pendingDefinition.setLength(0);
                    pendingType = DefinitionType.NONE;
                }
            } else {
                if (isHierarchyDefinitionLine(trimmed)) {
                    pendingType = DefinitionType.HIERARCHY;
                    pendingDefinition.append(trimmed);
                    if (trimmed.endsWith(".")) {
                        hierarchyDefinitions.append(pendingDefinition).append(' ');
                        pendingDefinition.setLength(0);
                        pendingType = DefinitionType.NONE;
                    }
                } else if (isTemplateDefinitionLine(trimmed)) {
                    pendingType = DefinitionType.TEMPLATE;
                    pendingDefinition.append(trimmed);
                    if (trimmed.endsWith(".")) {
                        templateDefinitions.append(pendingDefinition).append(' ');
                        pendingDefinition.setLength(0);
                        pendingType = DefinitionType.NONE;
                    }
                } else if (!trimmed.startsWith("//")) {
                    sanitized.append(line);
                }
            }

            if (i < lines.length - 1) {
                sanitized.append('\n');
            }
        }

        if (!templateDefinitions.isEmpty()) {
            templateRegistry = new TemplateParser().parse(templateDefinitions.toString());
        }
        if (!hierarchyDefinitions.isEmpty()) {
            hierarchyRegistry = new HierarchyParser().parse(hierarchyDefinitions.toString());
        }

        return new Result(sanitized.toString().trim(), templateRegistry, hierarchyRegistry);
    }

    private static boolean isTemplateDefinitionLine(String trimmed) {
        return !trimmed.startsWith("//")
                && trimmed.contains(":=")
                && !trimmed.contains("::=")
                && !trimmed.contains("==>")
                && !trimmed.contains("=->")
                && !trimmed.contains("+->");
    }

    private static boolean isHierarchyDefinitionLine(String trimmed) {
        return !trimmed.startsWith("//") && trimmed.contains("::=");
    }

    public record Result(String query, TemplateRegistry templateRegistry, HierarchyRegistry hierarchyRegistry) {}
}
