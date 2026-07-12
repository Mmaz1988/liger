package de.ukon.liger.analysis.QueryParser;

import java.util.ArrayList;
import java.util.List;

public class HierarchyParser {

    public HierarchyRegistry parse(String input) {
        HierarchyRegistry registry = new HierarchyRegistry();

        if (input == null || input.isBlank()) {
            return registry;
        }

        for (String definition : splitTopLevel(input, '.')) {
            String trimmed = definition.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int assignmentIndex = trimmed.indexOf("::=");
            if (assignmentIndex < 0) {
                throw new IllegalArgumentException("Invalid hierarchy definition: " + trimmed);
            }

            String name = trimmed.substring(0, assignmentIndex).trim();
            String body = trimmed.substring(assignmentIndex + 3).trim();
            List<String> order = new ArrayList<>();

            for (String entry : body.split(">")) {
                String label = entry.trim();
                if (!label.isEmpty()) {
                    order.add(label);
                }
            }

            registry.addHierarchy(name, order);
        }

        return registry;
    }

    private List<String> splitTopLevel(String input, char delimiter) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (inQuote) {
                current.append(c);
                if (c == quoteChar) {
                    inQuote = false;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                inQuote = true;
                quoteChar = c;
                current.append(c);
                continue;
            }

            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
            }

            if (c == delimiter && depth == 0) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            parts.add(current.toString());
        }

        return parts;
    }
}
