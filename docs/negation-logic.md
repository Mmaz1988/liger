# Negation Logic

## Current behavior

LiGER negation is currently implemented as a scoped nested-parser check.

When the query contains `-(...)`:

1. The negated subquery is parsed separately with a fresh `QueryParser`.
2. The outer candidate solution is copied and used as a seed binding for the nested parse.
3. The nested parser evaluates the inner query against that seeded state.
4. The outer candidate survives only if the nested parser produces no truthy solution for the same shared query variables.

In practice this behaves like a scoped `NOT EXISTS` over the current candidate solution.

## What was tried

Several intermediate designs were explored before landing on the current one:

1. Resetting only the top-level result map after negation.
2. Explicit start/end negation markers inside the token stream.
3. A negation frame stack that snapshots and restores parser state.
4. Grounding the negated query by replacing bound variables with starred references.
5. Comparing nested-parser output only by parser-wide success.

## Why those approaches failed

1. Inner expressions can mutate nested binding state, so resetting only `solution` was not enough.
2. Start/end marker parsing was brittle in the iterator-driven query parser and was easy to apply too early or too late.
3. The frame-stack approach duplicated state-management logic and was hard to keep aligned with existing expression evaluation.
4. Grounding alone was too coarse and caused mismatches for template-heavy queries.
5. Parser-wide success is too weak a signal for negation. Negation has to inspect individual surviving solutions.

## Remaining caveat

The nested parser still relies on copied bindings, but the copy is shallow at some levels. That is usually sufficient for the current tests, but it is the first thing to revisit if a new merged graph shows leakage between inner and outer state.

## Relevant files

- `src/main/java/de/ukon/liger/analysis/QueryParser/NegationExpression.java`
- `src/main/java/de/ukon/liger/analysis/QueryParser/QueryNegation.java`
- `src/test/java/de/ukon/liger/test/QueryParserNegationTest.java`
