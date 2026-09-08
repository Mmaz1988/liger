package de.ukon.liger.analysis.QueryParser;

import java.util.ArrayList;
import java.util.List;

public class TemplateParser {

    public TemplateRegistry parse(String input) {
        TemplateRegistry registry = new TemplateRegistry();

        if (input == null || input.isBlank()) {
            return registry;
        }

        for (String definition : splitTopLevel(input, '.')) {
            String trimmed = definition.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int assignmentIndex = trimmed.indexOf(":=");
            if (assignmentIndex < 0) {
                throw new IllegalArgumentException("Invalid template definition: " + trimmed);
            }

            String head = trimmed.substring(0, assignmentIndex).trim();
            String body = trimmed.substring(assignmentIndex + 2).trim();

            TemplateHead templateHead = parseHead(head);
            List<List<String>> alternatives = new ArrayList<>();

            for (String alternative : splitTopLevel(body, '|')) {
                List<String> tokens = QueryParser.tokenizeQuery(alternative.trim());
                alternatives.add(tokens);
            }

            registry.addTemplate(new QueryTemplate(templateHead.name, templateHead.parameters, alternatives));
        }

        return registry;
    }

    private TemplateHead parseHead(String head) {
        String trimmed = head.trim();
        int open = trimmed.indexOf('(');
        if (open < 0) {
            return new TemplateHead(trimmed, List.of());
        }

        int close = trimmed.lastIndexOf(')');
        if (close < open) {
            throw new IllegalArgumentException("Invalid template head: " + head);
        }

        String name = trimmed.substring(0, open).trim();
        String args = trimmed.substring(open + 1, close).trim();
        if (args.isEmpty()) {
            return new TemplateHead(name, List.of());
        }

        List<String> parameters = new ArrayList<>();
        for (String param : splitTopLevel(args, ',')) {
            parameters.add(param.trim());
        }
        return new TemplateHead(name, parameters);
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

    private record TemplateHead(String name, List<String> parameters) {}
}
