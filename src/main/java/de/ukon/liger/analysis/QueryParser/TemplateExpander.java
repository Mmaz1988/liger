package de.ukon.liger.analysis.QueryParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateExpander {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TemplateExpander.class);
    private static final Pattern FS_VAR_PATTERN = Pattern.compile("([#*])(\\w+)");

    public static List<List<String>> expandQuery(String query, TemplateRegistry registry) {
        List<List<String>> seeds = new ArrayList<>();
        seeds.add(QueryParser.tokenizeQuery(query));

        List<List<String>> expanded = new ArrayList<>();
        for (List<String> seed : seeds) {
            expandTokens(seed, registry, new ArrayList<>(), expanded, collectFsNodeNames(seed), new AtomicInteger(0));
        }
        return expanded;
    }

    public static List<String> expandPathQuery(String query, TemplateRegistry registry) {
        List<String> expanded = new ArrayList<>();
        expandPathSegments(splitPath(query), registry, 0, new ArrayList<>(), expanded);
        return expanded;
    }

    private static void expandTokens(List<String> tokens, TemplateRegistry registry,
                                     List<String> prefix, List<List<String>> out,
                                     Set<String> usedVars, AtomicInteger freshVarCounter) {
        if (tokens.isEmpty()) {
            out.add(new ArrayList<>(prefix));
            return;
        }

        String token = tokens.get(0);
        List<String> rest = tokens.subList(1, tokens.size());

        TemplateInvocation invocation = TemplateInvocation.parse(token);
        if (invocation == null) {
            prefix.add(token);
            Set<String> nextUsedVars = new LinkedHashSet<>(usedVars);
            nextUsedVars.addAll(collectFsNodeNames(List.of(token)));
            expandTokens(rest, registry, prefix, out, nextUsedVars, freshVarCounter);
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
            List<String> renamedAlternative = alphaRenameTemplateBody(alternative, template.getParameters(), usedVars, freshVarCounter);
            List<String> instantiated = instantiate(renamedAlternative, template.getParameters(), substitution);
            LOGGER.info("Expanded template invocation @" + invocation.name() + " with args=" + invocation.arguments() + " -> " + instantiated);
            List<String> nextTokens = new ArrayList<>(instantiated);
            nextTokens.addAll(rest);
            Set<String> nextUsedVars = new LinkedHashSet<>(usedVars);
            nextUsedVars.addAll(collectFsNodeNames(instantiated));
            expandTokens(nextTokens, registry, new ArrayList<>(prefix), out, nextUsedVars, freshVarCounter);
        }
    }

    private static List<String> alphaRenameTemplateBody(List<String> tokens,
                                                        List<String> parameters,
                                                        Set<String> usedVars,
                                                        AtomicInteger freshVarCounter) {
        Map<String, String> renames = new LinkedHashMap<>();
        List<String> out = new ArrayList<>();
        Set<String> reservedVars = new LinkedHashSet<>(usedVars);

        for (String parameter : parameters) {
            if (parameter != null && parameter.startsWith("#") && parameter.length() > 1) {
                reservedVars.add(parameter.substring(1));
            }
        }

        for (String token : tokens) {
            TemplateInvocation invocation = TemplateInvocation.parse(token);
            if (invocation != null) {
                out.add(renameTemplateInvocation(token, parameters, reservedVars, renames, freshVarCounter, invocation));
            } else {
                out.add(renameTemplateVariables(token, parameters, reservedVars, renames, freshVarCounter));
            }
        }

        return out;
    }

    private static String renameTemplateInvocation(String originalToken,
                                                   List<String> parameters,
                                                   Set<String> usedVars,
                                                   Map<String, String> renames,
                                                   AtomicInteger freshVarCounter,
                                                   TemplateInvocation invocation) {
        if (invocation.arguments().isEmpty()) {
            return originalToken;
        }

        List<String> renamedArgs = new ArrayList<>();
        for (String arg : invocation.arguments()) {
            renamedArgs.add(renameTemplateVariables(arg, parameters, usedVars, renames, freshVarCounter));
        }

        StringBuilder out = new StringBuilder("@");
        out.append(invocation.name()).append("(");
        out.append(String.join(",", renamedArgs));
        out.append(")");
        out.append(invocation.suffix());
        return out.toString();
    }

    private static String renameTemplateVariables(String token,
                                                  List<String> parameters,
                                                  Set<String> usedVars,
                                                  Map<String, String> renames,
                                                  AtomicInteger freshVarCounter) {
        Matcher matcher = FS_VAR_PATTERN.matcher(token);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String prefix = matcher.group(1);
            String variable = matcher.group(2);

            if (parameters.contains(prefix + variable)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(prefix + variable));
                continue;
            }

            String renamed = renames.get(variable);
            if (renamed == null) {
                renamed = freshFsNodeName(usedVars, freshVarCounter);
                renames.put(variable, renamed);
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(prefix + renamed));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String freshFsNodeName(Set<String> usedVars, AtomicInteger freshVarCounter) {
        de.ukon.liger.utilities.VariableHandler variableHandler = new de.ukon.liger.utilities.VariableHandler();
        HashMap<de.ukon.liger.utilities.VariableHandler.variableType, List<String>> reserved = variableHandler.getReservedVariables();
        HashMap<de.ukon.liger.utilities.VariableHandler.variableType, List<String>> seeded = new HashMap<>();

        for (Map.Entry<de.ukon.liger.utilities.VariableHandler.variableType, List<String>> entry : reserved.entrySet()) {
            seeded.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        seeded.put(de.ukon.liger.utilities.VariableHandler.variableType.FS_NODE, new ArrayList<>(usedVars));
        variableHandler.setUsedVariables(seeded);

        String candidate;
        do {
            candidate = variableHandler.returnNewVar(de.ukon.liger.utilities.VariableHandler.variableType.FS_NODE,
                    freshVarCounter.getAndIncrement());
        } while (candidate != null && usedVars.contains(candidate));

        if (candidate == null) {
            throw new IllegalStateException("Failed to generate a fresh fs node variable");
        }

        usedVars.add(candidate);
        return candidate;
    }

    private static Set<String> collectFsNodeNames(List<String> tokens) {
        Set<String> out = new LinkedHashSet<>();
        for (String token : tokens) {
            Matcher matcher = FS_VAR_PATTERN.matcher(token);
            while (matcher.find()) {
                out.add(matcher.group(2));
            }
        }
        return out;
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

    private static List<String> instantiate(List<String> tokens, List<String> parameterOrder, Map<String, String> substitution) {
        List<String> out = new ArrayList<>();
        for (String token : tokens) {
            out.add(substituteToken(token, parameterOrder, substitution));
        }
        return out;
    }

    private static String substituteToken(String token, List<String> parameterOrder, Map<String, String> substitution) {
        String result = token;
        Map<String, String> placeholders = new LinkedHashMap<>();

        for (int i = 0; i < parameterOrder.size(); i++) {
            String parameter = parameterOrder.get(i);
            String placeholder = "__LIGER_TPL_" + i + "__";
            placeholders.put(parameter, placeholder);
            result = result.replaceAll("(?<![A-Za-z0-9_])" + Pattern.quote(parameter) + "(?![A-Za-z0-9_])",
                    Matcher.quoteReplacement(placeholder));
        }

        for (String parameter : parameterOrder) {
            String placeholder = placeholders.get(parameter);
            String value = substitution.get(parameter);
            if (placeholder != null && value != null) {
                result = result.replace(placeholder, value);
            }
        }
        return result;
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
