package de.ukon.liger.analysis.QueryParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TemplateExpander {

    public static List<List<String>> expandQuery(String query, TemplateRegistry registry) {
        List<List<String>> seeds = new ArrayList<>();
        seeds.add(QueryParser.tokenizeQuery(query));

        List<List<String>> expanded = new ArrayList<>();
        for (List<String> seed : seeds) {
            expandTokens(seed, registry, new ArrayList<>(), expanded);
        }
        return expanded;
    }

    public static List<String> expandPathQuery(String query, TemplateRegistry registry) {
        List<String> expanded = new ArrayList<>();
        expandPathSegments(splitPath(query), registry, 0, new ArrayList<>(), expanded);
        return expanded;
    }

    private static void expandTokens(List<String> tokens, TemplateRegistry registry,
                                     List<String> prefix, List<List<String>> out) {
        if (tokens.isEmpty()) {
            out.add(new ArrayList<>(prefix));
            return;
        }

        String token = tokens.get(0);
        List<String> rest = tokens.subList(1, tokens.size());

        TemplateInvocation invocation = TemplateInvocation.parse(token);
        if (invocation == null) {
            prefix.add(token);
            expandTokens(rest, registry, prefix, out);
            prefix.remove(prefix.size() - 1);
            return;
        }

        QueryTemplate template = registry.getTemplate(invocation.name());
        if (template == null) {
            throw new IllegalArgumentException("Unknown template: " + invocation.name());
        }

        if (template.getParameters().size() != invocation.arguments().size()) {
            throw new IllegalArgumentException("Template arity mismatch for " + invocation.name());
        }

        Map<String, String> substitution = new HashMap<>();
        for (int i = 0; i < template.getParameters().size(); i++) {
            substitution.put(template.getParameters().get(i), invocation.arguments().get(i));
        }

        for (List<String> alternative : template.getAlternatives()) {
            List<String> instantiated = instantiate(alternative, substitution);
            List<String> nextTokens = new ArrayList<>(instantiated);
            nextTokens.addAll(rest);
            expandTokens(nextTokens, registry, new ArrayList<>(prefix), out);
        }
    }

    private static void expandPathSegments(List<String> segments, TemplateRegistry registry, int index,
                                           List<String> prefix, List<String> out) {
        if (index >= segments.size()) {
            out.add(String.join(">", prefix));
            return;
        }

        String segment = segments.get(index).trim();
        TemplateInvocation invocation = TemplateInvocation.parse(segment);
        if (invocation == null) {
            prefix.add(segment);
            expandPathSegments(segments, registry, index + 1, prefix, out);
            prefix.remove(prefix.size() - 1);
            return;
        }

        QueryTemplate template = registry.getTemplate(invocation.name());
        if (template == null) {
            throw new IllegalArgumentException("Unknown template: " + invocation.name());
        }

        if (!template.getParameters().isEmpty()) {
            throw new IllegalArgumentException("Path templates do not accept arguments: " + invocation.name());
        }

        for (List<String> alternative : template.getAlternatives()) {
            if (alternative.size() != 1) {
                throw new IllegalArgumentException("Path templates must expand to a single label: " + invocation.name());
            }
            prefix.add(alternative.get(0) + invocation.suffix());
            expandPathSegments(segments, registry, index + 1, prefix, out);
            prefix.remove(prefix.size() - 1);
        }
    }

    private static List<String> splitPath(String query) {
        List<String> segments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inQuote = false;
        char quoteChar = 0;

        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
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

            if (c == '>' && depth == 0) {
                segments.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            segments.add(current.toString());
        }

        return segments;
    }

    private static List<String> instantiate(List<String> tokens, Map<String, String> substitution) {
        List<String> out = new ArrayList<>();
        for (String token : tokens) {
            out.add(substitution.getOrDefault(token, token));
        }
        return out;
    }

    private record TemplateInvocation(String name, List<String> arguments, String suffix) {

        static TemplateInvocation parse(String token) {
            if (token == null || !token.startsWith("@")) {
                return null;
            }

            String body = token.substring(1).trim();
            int open = body.indexOf('(');
            if (open < 0) {
                String name = body;
                String suffix = "";
                if (body.endsWith("*")) {
                    name = body.substring(0, body.length() - 1);
                    suffix = "*";
                }
                return new TemplateInvocation(name, List.of(), suffix);
            }

            int close = body.lastIndexOf(')');
            if (close < open) {
                throw new IllegalArgumentException("Invalid template invocation: " + token);
            }

            String name = body.substring(0, open).trim();
            String argString = body.substring(open + 1, close).trim();
            if (argString.isEmpty()) {
                return new TemplateInvocation(name, List.of(), "");
            }

            List<String> args = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int depth = 0;
            boolean inQuote = false;
            char quoteChar = 0;

            for (int i = 0; i < argString.length(); i++) {
                char c = argString.charAt(i);
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
                if (c == ',' && depth == 0) {
                    args.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }

            if (!current.isEmpty()) {
                args.add(current.toString().trim());
            }

            return new TemplateInvocation(name, args, "");
        }
    }
}
