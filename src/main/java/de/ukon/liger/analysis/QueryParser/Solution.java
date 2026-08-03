package de.ukon.liger.analysis.QueryParser;

import de.ukon.liger.packing.ChoiceContext;
import de.ukon.liger.packing.ChoiceSpace;
import de.ukon.liger.packing.ChoiceVar;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Solution extends AbstractSet<SolutionKey> {

    private Set<SolutionKey> solutionKeys;
    private boolean truthValue;
    private Set<Set<ChoiceVar>> choiceContexts = new LinkedHashSet<>();

    public Solution() {
        this(new LinkedHashSet<>(), true);
    }

    public Solution(Set<SolutionKey> solutionKeys) {
        this(solutionKeys, true);
    }

    public Solution(Set<SolutionKey> solutionKeys, boolean truthValue) {
        this.solutionKeys = solutionKeys == null ? new LinkedHashSet<>() : new LinkedHashSet<>(solutionKeys);
        this.truthValue = truthValue;
    }

    public Set<Set<ChoiceVar>> getChoiceContexts() {
        return choiceContexts;
    }

    public void setChoiceContexts(Set<Set<ChoiceVar>> contexts) {
        choiceContexts = new LinkedHashSet<>();
        if (contexts != null) {
            for (Set<ChoiceVar> context : contexts) {
                choiceContexts.add(copyContext(context));
            }
        }
    }

    public void addChoiceContext(Set<ChoiceVar> context) {
        choiceContexts.add(copyContext(context));
    }

    public Set<SolutionKey> getSolutionKeys() {
        return solutionKeys;
    }

    public void setSolutionKeys(Set<SolutionKey> solutionKeys) {
        this.solutionKeys = solutionKeys == null ? new LinkedHashSet<>() : new LinkedHashSet<>(solutionKeys);
    }

    public boolean isTruthValue() {
        return truthValue;
    }

    public void setTruthValue(boolean truthValue) {
        this.truthValue = truthValue;
    }

    public boolean isEmpty() {
        return solutionKeys.isEmpty();
    }

    public Solution copy() {
        Solution copy = new Solution(solutionKeys, truthValue);
        copy.setChoiceContexts(choiceContexts);
        return copy;
    }

    @Override
    public Iterator<SolutionKey> iterator() {
        return solutionKeys.iterator();
    }

    @Override
    public int size() {
        return solutionKeys.size();
    }

    @Override
    public boolean add(SolutionKey solutionKey) {
        return solutionKeys.add(solutionKey);
    }

    @Override
    public boolean remove(Object o) {
        return solutionKeys.remove(o);
    }

    @Override
    public void clear() {
        solutionKeys.clear();
    }

    @Override
    public boolean contains(Object o) {
        return solutionKeys.contains(o);
    }

    public static Solution merge(Solution left, Solution right) {
        Set<SolutionKey> merged = new LinkedHashSet<>();
        if (left != null) {
            merged.addAll(left.getSolutionKeys());
        }
        if (right != null) {
            merged.addAll(right.getSolutionKeys());
        }
        Solution result = new Solution(merged, (left == null || left.truthValue) && (right == null || right.truthValue));
        result.setChoiceContexts(mergeContexts(left, right, null));
        return result;
    }

    public static Solution mergeIfCompatible(Solution left, Solution right, ChoiceSpace choiceSpace) {
        Set<Set<ChoiceVar>> contexts = mergeContexts(left, right, choiceSpace);
        if (contexts.isEmpty()) {
            return null;
        }
        Solution result = merge(left, right);
        result.setChoiceContexts(contexts);
        return result;
    }

    private static Set<Set<ChoiceVar>> mergeContexts(Solution left, Solution right,
                                                      ChoiceSpace choiceSpace) {
        Set<Set<ChoiceVar>> leftContexts = contextsOrRoot(left);
        Set<Set<ChoiceVar>> rightContexts = contextsOrRoot(right);
        Set<Set<ChoiceVar>> merged = new LinkedHashSet<>();
        for (Set<ChoiceVar> leftContext : leftContexts) {
            for (Set<ChoiceVar> rightContext : rightContexts) {
                if (choiceSpace != null && !ChoiceContext.compatible(choiceSpace, leftContext, rightContext)) {
                    continue;
                }
                Set<ChoiceVar> combined = new LinkedHashSet<>(leftContext);
                combined.addAll(rightContext);
                merged.add(combined);
            }
        }
        return merged;
    }

    private static Set<Set<ChoiceVar>> contextsOrRoot(Solution solution) {
        if (solution == null || solution.choiceContexts.isEmpty()) {
            return Collections.singleton(Collections.singleton(new ChoiceVar("1")));
        }
        return solution.choiceContexts;
    }

    private static Set<ChoiceVar> copyContext(Set<ChoiceVar> context) {
        Set<ChoiceVar> copy = new LinkedHashSet<>();
        if (context != null) {
            for (ChoiceVar choice : context) {
                copy.add(choice.copy());
            }
        }
        if (copy.isEmpty()) {
            copy.add(new ChoiceVar("1"));
        }
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution)) return false;
        Solution solution = (Solution) o;
        return solutionKeys.equals(solution.solutionKeys)
                && choiceContexts.equals(solution.choiceContexts);
    }

    @Override
    public int hashCode() {
        return 31 * solutionKeys.hashCode() + choiceContexts.hashCode();
    }
}
