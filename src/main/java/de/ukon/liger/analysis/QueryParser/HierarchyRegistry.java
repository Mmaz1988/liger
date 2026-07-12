package de.ukon.liger.analysis.QueryParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HierarchyRegistry {

    private final Map<String, List<String>> hierarchies = new HashMap<>();

    public void addHierarchy(String name, List<String> order) {
        hierarchies.put(name, new ArrayList<>(order));
    }

    public List<String> getHierarchy(String name) {
        return hierarchies.getOrDefault(name, Collections.emptyList());
    }

    public boolean isSuperior(String name, String superior, String inferior) {
        List<String> order = getHierarchy(name);
        int superiorIndex = order.indexOf(superior);
        int inferiorIndex = order.indexOf(inferior);

        return superiorIndex >= 0 && inferiorIndex >= 0 && superiorIndex < inferiorIndex;
    }

    public boolean contains(String name) {
        return hierarchies.containsKey(name);
    }

    public List<String[]> getSuperiorPairs(String name) {
        List<String> order = getHierarchy(name);
        if (order.size() < 2) {
            return Collections.emptyList();
        }

        List<String[]> pairs = new ArrayList<>();
        for (int i = 0; i < order.size() - 1; i++) {
            for (int j = i + 1; j < order.size(); j++) {
                pairs.add(new String[] {order.get(i), order.get(j)});
            }
        }
        return pairs;
    }

    public boolean isValidHierarchy(String name) {
        return contains(name) && getHierarchy(name).stream().allMatch(Objects::nonNull);
    }
}
