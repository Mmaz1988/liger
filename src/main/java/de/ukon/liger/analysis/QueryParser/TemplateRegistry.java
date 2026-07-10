package de.ukon.liger.analysis.QueryParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TemplateRegistry {
    private final Map<String, QueryTemplate> templates = new HashMap<>();

    public void addTemplate(QueryTemplate template) {
        templates.put(template.getName(), template);
    }

    public QueryTemplate getTemplate(String name) {
        return templates.get(name);
    }

    public Map<String, QueryTemplate> getTemplates() {
        return Collections.unmodifiableMap(templates);
    }
}
