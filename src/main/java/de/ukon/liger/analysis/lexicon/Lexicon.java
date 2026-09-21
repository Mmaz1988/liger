package de.ukon.liger.analysis.lexicon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The dictionaries available to the rule function {@code lex(value,dictionary)}: dictionary
 * name -> label -> values, all in file order. Immutable once built.
 */
public final class Lexicon {

    public static final Lexicon EMPTY = new Lexicon(new LinkedHashMap<>());

    private final Map<String, LinkedHashMap<String, List<String>>> dictionaries;

    public Lexicon(Map<String, ? extends Map<String, List<String>>> dictionaries) {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> copy = new LinkedHashMap<>();
        dictionaries.forEach((dict, labels) -> {
            LinkedHashMap<String, List<String>> labelCopy = new LinkedHashMap<>();
            labels.forEach((label, values) -> labelCopy.put(label, List.copyOf(values)));
            copy.put(dict, labelCopy);
        });
        this.dictionaries = Collections.unmodifiableMap(copy);
    }

    /**
     * Combines lexica in order. Dictionaries of the same name are joined; a label present in
     * several lexica gets the union of their values. Earlier lexica's labels come first, so
     * they win a {@link #lookup} for a value listed under labels from several lexica.
     */
    public static Lexicon merge(List<Lexicon> lexica) {
        LinkedHashMap<String, LinkedHashMap<String, List<String>>> merged = new LinkedHashMap<>();
        for (Lexicon lexicon : lexica) {
            lexicon.dictionaries.forEach((dict, labels) -> {
                LinkedHashMap<String, List<String>> target = merged.computeIfAbsent(dict, k -> new LinkedHashMap<>());
                labels.forEach((label, values) -> {
                    List<String> union = target.computeIfAbsent(label, k -> new ArrayList<>());
                    for (String value : values) {
                        if (!union.contains(value)) {
                            union.add(value);
                        }
                    }
                });
            });
        }
        return new Lexicon(merged);
    }

    public boolean hasDictionary(String dictionary) {
        return dictionaries.containsKey(dictionary);
    }

    /** The first label of {@code dictionary} whose values contain {@code value}. */
    public Optional<String> lookup(String dictionary, String value) {
        Map<String, List<String>> labels = dictionaries.get(dictionary);
        if (labels == null) {
            return Optional.empty();
        }
        return labels.entrySet().stream()
                .filter(entry -> entry.getValue().contains(value))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Map<String, LinkedHashMap<String, List<String>>> dictionaries() {
        return dictionaries;
    }
}
