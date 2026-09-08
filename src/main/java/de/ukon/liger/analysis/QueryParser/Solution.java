package de.ukon.liger.analysis.QueryParser;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Solution extends AbstractSet<SolutionKey> {

    private Set<SolutionKey> solutionKeys;
    private boolean truthValue;

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
        return new Solution(solutionKeys, truthValue);
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
        return new Solution(merged, (left == null || left.truthValue) && (right == null || right.truthValue));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution)) return false;
        Solution solution = (Solution) o;
        return solutionKeys.equals(solution.solutionKeys);
    }

    @Override
    public int hashCode() {
        return solutionKeys.hashCode();
    }
}
