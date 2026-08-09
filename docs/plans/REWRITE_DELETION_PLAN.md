# Rewrite Deletion Plan

## Goal

Make rewrite deletion precise. A rule such as:

```text
#a bind #a =-> 0.
```

must delete only the matched `bind` edge. It must not delete every fact associated with `#a`.

For explicit exceptions, support protected LHS edges:

```text
#d bind #d & #d +acc #d =-> 0.
```

This should delete `bind`, retain `acc`, and retain every other unrelated fact.

## Current Problem

`RuleParser.addAnnotation2(...)` collects all `GraphConstraint` values returned by the LHS query and treats them as deletion targets. Query results can contain all facts associated with a bound node, not only the graph atoms written in the rule. Consequently, deletion is broader than the LHS expressions indicate.

The existing `=-> 0` deletion block also uses the broad query result set when removing facts from `constraints` and `annotation`.

## Implementation

1. Make deletion atom-scoped.
   - Determine which concrete `GraphConstraint` objects satisfy each graph atom in the LHS.
   - Delete only those constraints, rather than every constraint included in a variable binding.
   - Preserve support for conjunctions, node/value variables, quoted values, and existing query features.
   - Keep deletion consistent across both `LinguisticStructure.constraints` and `LinguisticStructure.annotation`.

2. Add protected-edge syntax for deletion rules.
   - Recognize a leading `+` on an LHS relation label, such as `+acc`.
   - Normalize the label to `acc` when constructing the query so existing `acc` facts still match.
   - Record the matching constraint as protected.
   - Exclude protected constraints from the atom-scoped deletion set.
   - Keep `+` limited to the relation-label position so existing functional-uncertainty syntax remains unaffected.

3. Preserve existing rewrite behavior outside deletion.
   - Do not change ordinary RHS addition or nonzero rewrite behavior unless required by the deletion implementation.
   - Keep `=-> 0` as deletion-only semantics.
   - Avoid broad `edgeKey` matching where it could remove distinct constraints; prefer the actual matched constraints or full constraint equality where appropriate.

## Tests

Add regression tests to `src/test/java/de/ukon/liger/test/RuleParserTest.java` covering:

- `#a bind #a =-> 0` removes only `bind`.
- Other facts on the same source node remain.
- Other facts on the target node remain.
- The same behavior applies when matched facts are annotations.
- `#d bind #d & #d +acc #d =-> 0` removes `bind` and preserves `acc`.
- Multiple protected and unprotected edges behave independently.
- Existing rewrite deletion tests continue to pass.

## Verification

Run:

```text
./mvnw -Dtest=RuleParserTest test
./mvnw test
```

## Files Likely To Change

- `src/main/java/de/ukon/liger/analysis/RuleParser/RuleParser.java`
- `src/test/java/de/ukon/liger/test/RuleParserTest.java`

`Rule.java` or `QueryParser` should only change if the existing parsing structure cannot support normalization and protected-edge metadata cleanly.
