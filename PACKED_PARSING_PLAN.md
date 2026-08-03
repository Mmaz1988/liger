# Packed Parsing Plan

Branch: `lfg2026_pragmatic_parsing=packing`

Baseline branch: `lfg2026_pragmatic_parsing=new_semantics`

The reduced-subgraph optimization remains part of both execution paths. Packed
execution should reduce uncertainty traversal to relevant subgraphs rather than
replace that optimization.

## Status

- [x] Create a dedicated packing branch with the current reduced-subgraph optimization.
- [x] Identify initial fixtures: `testdirS11.pl`, `testdirS12.pl`, `testdirS13.pl`, and `testdirS20.pl`.
- [ ] Record the current unpacked baseline for S11, S12, and S13.
- [x] Record repeatable query baselines for direct fixture loading on S11, S12, S13, and S20.
- [x] Record initial packed fixture statistics for S11, S12, and S13.
- [x] Trace the XLE parsing entry point where explicit unpacking occurs.
- [x] Audit the initial `ChoiceSpace`, `ChoiceNode`, `ChoiceVar`, and `GraphConstraint.choiceVars` flow.
- [ ] Define packed semantics for matching, conjunction, negation, branching, and rewriting.
- [x] Add initial `ChoiceContext` compatibility semantics for root, sibling, and ancestor contexts.
- [ ] Preserve packed constraints and choice space through XLE parsing.
- [ ] Add context-aware query matching while retaining reduced uncertainty subgraphs.
- [ ] Propagate choice contexts through rule annotations and rewrite deletions.
- [x] Split multiple alternative query contexts before rule annotation application.
- [ ] Adapt meaning-constructor extraction to selected or unpacked readings.
- [ ] Preserve meaning-constructor set extraction directly from packed representations.
- [x] Add an S20 regression test for packed meaning-constructor choice contexts.
- [x] Verify that packed semantic constraints survive syntactic/semantic graph merging.
- [ ] Produce a packed merged syntax-plus-semantic graph that can be consumed
  by `RuleParser` without materializing one graph per semantic solution.
- [x] Add `PackedAlternativeAssembler` to factor shared constraints and attach
  fresh semantic choice contexts to alternative-specific constraints.
- [x] Verify that a rule annotation is restricted to the matching packed
  semantic context.
- [x] Wire packed semantic alternatives into `/merge_uploaded_structures` via
  an `alternatives` payload.
- [x] Add opt-in `packAlternatives` handling to the sequence endpoint so parsed
  alternatives can be rule-processed as one packed graph.
- [ ] Add packed/unpacked differential tests.
- [ ] Benchmark runtime, memory, graph traversals, materialized structures, and results.
- [x] Add a repeatable `PackedFixtureAuditTest` for the initial choice-space statistics.
- [x] Add direct and template-based uncertainty queries to `QueryFixtureBaselineTest`.
- [x] Add opt-in packed parser-output preservation for XLE debugging.
- [x] Add an explicit XLE-unpacked comparison fixture for S20.

## Initial Fixtures

| Fixture | Purpose |
| --- | --- |
| `testdirS11.pl` | Small packing and choice-context validation |
| `testdirS12.pl` | Complex ambiguity and nested choice behavior |
| `testdirS13.pl` | Large/stress case for packed query and rule execution |
| Other `liger_resources/testFiles/*.pl` | Unambiguous regression controls |

Initial direct `fs2Java` audit results:

| Fixture | Structures | Constraints | Choice nodes | Choice contexts |
| --- | ---: | ---: | ---: | ---: |
| `testdirS11.pl` | 1 | 464 | 1 | 3 |
| `testdirS12.pl` | 1 | 4,960 | 475 | 403 |
| `testdirS13.pl` | 1 | 2,151 | 29 | 37 |
| `testdirS20.pl` | 1 | 336 | 1 | 3 |

All listed constraints had a non-empty choice context in the initial audit.
These files therefore already provide packed Prolog input suitable for testing;
the next step is to compare direct packed loading with the current application
path that invokes XLE unpacking before query/rule execution.

Choice-context counts are not solution counts. For `testdirS11.pl`, the three
contexts include the always-active root context, so they represent two actual
alternative readings/solutions rather than three solutions.

For every fixture, record:

- XLE reading count
- Choice-space and choice-node counts
- Constraint count before and after unpacking
- Query results
- Rule annotations and rewrite results
- Meaning constructors
- Runtime and memory

## Query Baseline

`QueryFixtureBaselineTest` currently loads each Prolog fixture through
`XLEoperator.fs2Java` and records result counts and per-query elapsed time. It
is a baseline for the existing query implementation over the fixture data; an
explicit XLE-unpacked versus packed execution comparison remains outstanding.

The test covers:

- `edge=PRED`
- `#a PRED %p`
- Direct `SUBJ` uncertainty in both directions
- `@GF` template paths
- `@GF*` and `@GF+`
- A chained `@GF*` followed by `@GF` path

The current expected result counts are:

| Fixture | Common queries | Template/long-path queries |
| --- | --- | --- |
| S11 | 1, 7, 1, 1 | 3, 151, 3, 3 |
| S12 | 1, 85, 24, 24 | 42, 1010, 64, 68 |
| S13 | 1, 38, 10, 10 | 22, 667, 22, 22 |
| S20 | 1, 5, 2, 2 | 2, 175, 2, 2 |

An initial attempt to invoke XLE's `unpack-prolog-graph` directly on
`testdirS11.pl` was not usable: XLE rejected the fixture with an invalid-token
error. The fixture format is suitable for LiGER's `ReadFsProlog` parser, but it
is not currently accepted as a direct input to that XLE command. The explicit
unpacked comparison therefore remains a separate fixture-generation task for
S11-S13. S20 now provides a valid XLE packed/unpacked comparison fixture.

`S20PackedUnpackedComparisonTest` records the current semantic gap: the packed
graph returns one result set, while summing the two unpacked readings returns
twice as many results. This is expected until query matching becomes
choice-context aware.

## Packed Output Debugging

The CLI flag `-debug-packed` preserves the parser output before XLE unpacking
in:

```text
liger_resources/tmp/packed_parser_output/
```

This is a latest-run snapshot. Normal execution remains unchanged; without the
flag, LiGER continues to remove the packed `sentence1.pl` after successful
unpacking.

## Execution Model

Current path:

```text
XLE output -> XLE unpacking -> one LinguisticStructure per reading
            -> query/rule execution -> meaning constructors
```

Target path:

```text
XLE output -> packed constraints + ChoiceSpace
            -> reduced relevant subgraph
            -> context-aware query/rule execution
            -> unpack only selected/final readings when required
```

The existing LiGER choice model is the foundation:

- `ChoiceSpace`
- `ChoiceNode`
- `ChoiceVar`
- `GraphConstraint.choiceVars`
- `Fstructure.cp`
- `FsProlog2Java`

## Initial Audit Findings

- `XLEoperator.parseSentences(sentences, unpack)` explicitly invokes XLE's
  `unpack-fstructure` command when `unpack` is `true`.
- `XLEoperator.parseSingle(sentence, boolean)` already exposes an unpack flag,
  but both paths subsequently convert Prolog output through `fs2Java`.
- `ReadFsProlog` has a `ChoiceSpace` field and parses choice declarations.
- `FsProlog2Java.fs2List` attaches the parsed choice context to each
  `GraphConstraint` and registers it in `ReadFsProlog.cp.choices`.
- `LinguisticStructure` already stores both constraints and `ChoiceSpace`.
- `GraphConstraint` already serializes and deserializes `choiceVars`.
- The likely first implementation boundary is therefore between
  `XLEoperator.parseSingle(..., false)`/`fs2Java` and query execution, rather
  than an XLE-native API.
- Existing query and rule code must be checked for assumptions that every
  constraint belongs to one unpacked reading or to the default choice `1`.

## Semantic Requirements

A query result must carry both node bindings and a compatible choice context.

### Context Propagation

- A fact in the root context is active in every compatible choice.
- If a rule matches only root-context facts, an added fact belongs to the root
  context and therefore distributes automatically across all choices.
- If a variable binding depends on a choice-specific fact, the binding must
  carry that compatible choice context.
- Facts derived from such a binding must be added in that context, not in the
  root context.
- Rewrite deletion must remove a fact only in the matched context. Deleting a
  root-context fact has broader effect because it removes the fact for all
  compatible choices.
- When several query constraints are combined, their contexts must be
  intersected or compatibility-checked. A result must not combine bindings
  that belong to incompatible choices.
- Identical visible bindings from different contexts must remain distinguishable
  internally, even if a later presentation layer chooses to merge them.

Packed execution must define behavior for:

- Context compatibility and intersection
- Context subsumption
- Positive and negative constraints
- Functional uncertainty
- Annotation inheritance
- Rule branching
- Context-sensitive rewriting and deletion
- Meaning-constructor extraction

Negative queries require particular care: a fact in one reading must not
invalidate a negative constraint in an incompatible reading.

## Phases

### Phase 1: Baseline

Run the existing unpacked implementation on S11, S12, S13, and unambiguous
controls. Save result summaries and performance measurements on both branches.

### Phase 2: Packing Audit

Trace `XLEoperator`, `ReadFsProlog`, `FsProlog2Java`, and `LinguisticStructure`.
Document where packed constraints are retained and where separate readings are
created or required.

### Phase 3: Packed Input Preservation

Retain the packed constraint list and `ChoiceSpace` after XLE parsing without
changing legacy unpacked behavior. Add an explicit packed/unpacked mode.

### Phase 4: Context-Aware Queries

Attach contexts to query matches and combine only compatible matches. Preserve
the reduced uncertainty graph optimization for traversal.

### Phase 5: Context-Aware Rules

Make generated annotations inherit matched contexts. Make branching refine
contexts and make rewrite deletion context-sensitive.

### Phase 6: Semantics and Differential Testing

Compare packed and unpacked query results, rule results, rewrites, and meaning
constructors on all initial fixtures.

Meaning-constructor extraction must remain context-aware. The implementation
must distinguish:

- Constructors active in the root context and therefore shared by all readings.
- Constructors introduced only in a specific choice context.
- Constructor sets that are structurally identical but belong to different
  readings.
- Constructors that should be deduplicated for presentation without losing
  their supporting choice contexts.

For each fixture, compare packed extraction with extraction from every unpacked
reading and verify both constructor content and context coverage.

### Semantic-Graph Packing Policy

Merging a LiGER syntactic graph with a semantic graph must preserve packing
rather than force both graphs into separate unpacked readings.

Semantic alternatives should be packed when:

- They are alternative solutions for the same syntactic reading.
- Their shared constraints are identical or safely distributable.
- Their differences can be represented by additional semantic choice
  variables.
- Resource-sensitive proof behavior remains valid under the shared context.

Semantic alternatives should remain separate when:

- They cannot be represented as compatible choice contexts.
- They differ in shared constraints whose distribution would change meaning.
- Linear-resource consumption or proof dependencies differ in a way that cannot
  be contextually isolated.
- They are merely duplicate derivations of the same constructor set; those
  should be deduplicated instead of creating artificial ambiguity.

The merge operation should therefore:

1. Keep syntactic choice contexts unchanged.
2. Add a separate semantic choice dimension for genuine semantic alternatives.
3. Combine syntactic and semantic contexts only when compatible.
4. Keep root semantic facts in the root context so they distribute across
   compatible readings.
5. Unpack only at the client/output boundary when separate solutions are
   explicitly requested.

### Phase 7: Performance Evaluation

Compare:

1. Baseline unpacked execution.
2. Unpacked execution with reduced-subgraph optimization.
3. Packed execution with reduced-subgraph optimization.

Measure runtime, memory, traversal count, materialized structures, branch count,
and result equivalence.

## Success Criteria

- Packed and unpacked results are equivalent.
- Choice-sensitive annotations do not leak between readings.
- Negative queries respect reading contexts.
- Rewrite rules delete only in the intended context.
- Meaning constructors remain equivalent.
- Packed execution materializes fewer duplicate structures.
- The reduced-subgraph optimization remains active.
- Legacy unpacked mode remains available as a fallback.
