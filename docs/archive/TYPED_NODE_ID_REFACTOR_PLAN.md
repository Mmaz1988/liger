# Typed Node ID Refactor Plan

## Purpose

Replace ambiguous numeric graph node IDs with typed string IDs while preserving
ordinary attribute values and external graph compatibility.

Initial namespaces:

| Prefix | Family | LiGER client type |
| --- | --- | --- |
| `f` | f-structure | `input` |
| `c` | c-structure | `cnode` |
| `d` | DRT/DRS graph nodes | `dnode` |
| `g` | glue graph nodes | `gnode` |

Examples: `f1`, `c1`, `d1`, `g1`.

`x1` is **not** a node by default. Therefore `NAME(d1, x1)` remains an
attribute-value pair, not an edge. Node recognition must use a configurable
namespace policy, never the lexical fact that a value contains letters and
digits.

## Baseline

- LiGER baseline: `6bed219` (`graph aware functional uncertainty`)
- LFGxDRT baseline at project start: `ed116ca` (`missing files`)
- LiGER worktree contains pre-existing untracked files and generated artifacts.
- LFGxDRT worktree contains pre-existing untracked planning/test artifacts.
- Do not remove or reset those files during this refactor.

## Working Rules

- Process one repository section at a time.
- Complete and verify each section before starting the next one.
- Keep internal collection/index keys as integers where they represent list or
  constraint positions rather than graph node IDs.
- Preserve explicit semantic node types only as optional metadata where the
  public family is `dnode`.
- Maintain a compatibility boundary for legacy numeric IDs during migration.
- After each major phase, record the commit hash and test result in the
  progress section at the end of this file.

## Cross-Repository Contract

- [x] Define the canonical ID grammar, initially `[fcdg][A-Za-z0-9_]*` only
      where the configured prefix is valid; preferably require a numeric suffix
      for the first migration (`f\\d+`, `c\\d+`, `d\\d+`, `g\\d+`).
- [x] Define valid prefixes as configuration, not hard-coded behavior.
- [x] Define prefix-to-family and prefix-to-default-client-type mappings.
- [x] Define whether configuration is server-wide, parser-specific, or
      request-specific. Initial recommendation: server-wide defaults with an
      injectable policy for parser/tests.
- [ ] Define legacy numeric input behavior: accept and classify only at input
      boundaries; do not emit new numeric IDs.
- [ ] Define explicit `NODE_TYPE` precedence: explicit constraint overrides the
      prefix-derived default.
- [x] Define DRT output contract: all DRT nodes use the `d` namespace and are
      exposed to LiGER as `dnode`; internal DRT subtypes may remain internal.
- [x] Define literal/value behavior: `x1`, `i1`, `42`, `val:foo`, and source
      indices remain values unless their prefixes are explicitly enabled.
- [x] Add contract examples covering `NAME(d1,x1)`, `IN(d2,d1)`, and
      `SRC(d2,i4)`.

## LiGER

### LiGER A: Namespace Policy

- [x] Add a small central node-ID policy/model, likely under
      `de.ukon.liger.syntax` or `de.ukon.liger.utilities`.
- [x] Implement `isNodeId(String)`.
- [x] Implement prefix/family lookup.
- [x] Implement default client node-type lookup.
- [x] Support configured valid prefixes and reject/ignore unconfigured ones.
- [x] Add unit tests for valid IDs, invalid IDs, `x1` as a value, legacy numeric
      IDs, and mixed prefixes.
- [ ] Decide whether to expose a `NodeId` value object or keep the public
      `GraphConstraint` API string-based. Recommendation: keep
      `GraphConstraint` string-based and centralize parsing in the policy.

### LiGER B: Core Graph Model

- [ ] Confirm `GraphConstraint.nodeIdentifier` and `fsValue` remain strings.
- [ ] Add or revise constructors so typed string IDs are the preferred path.
- [ ] Audit `GraphConstraint.toString()` and `toPrologString()` so typed IDs
      are not mistaken for literals or numeric variables.
- [ ] Review `projection` versus `proj` semantics before deleting anything.
- [ ] Preserve JSON read compatibility for legacy `proj` and numeric IDs.
- [ ] Add JSON round-trip tests with `f1`, `c1`, `d1`, and literal `x1`.

### LiGER C: XLE Conversion

Primary file: `src/main/java/de/ukon/liger/syntax/xle/prolog2java/FsProlog2Java.java`.

- [x] Keep raw XLE Prolog parsing numeric internally where required by the
      Prolog grammar.
- [x] Convert f-structure node IDs to `f` IDs at the graph conversion boundary.
- [x] Convert c-structure node IDs to `c` IDs.
- [x] Replace the current `"0" + mother`, `"0" + left`, `"0" + right`, and
      related constructions.
- [x] Ensure all c-structure references use the same namespace, including
      `left`, `right`, `phi`, `cproj`, and terminal references where they are
      graph nodes.
- [ ] Update regexes that currently require `\\d+` if they process converted
      graph IDs rather than raw Prolog variables.
- [x] Keep `int(...)` span values and source positions as literal values.
- [x] Add parser tests asserting both source and target IDs.


- [ ] Refactor `FsPath` to compare graph IDs as strings.
- [ ] Remove graph-identity uses of `Integer.parseInt(getFsNode/getFsValue())`.
- [ ] Retain integer parsing only for actual numeric suffixes, spans, or
      internal indices.
- [ ] Audit `Fstructure.builtCstructureTree` and related graph traversal.
- [ ] Audit `GlueSemantics` synthetic IDs and decide whether they use `g` or
      `d` according to their graph role.
- [ ] Ensure generated meaning-constructor indices are not interpreted as node
      references merely because they begin with `i`.
- [ ] Add mixed f/c/g/d graph traversal tests.

### LiGER E: Query and Rule Systems

- [ ] Keep integer keys in `QueryParser` result maps when they are constraint
      indexes.
- [ ] Update query node validation so typed constants are accepted according to
      the namespace policy.
- [ ] Ensure `id(#a)` and `id(%a)` return typed string IDs unchanged.
- [x] Define comparison behavior for typed IDs: strip the prefix, require equal
      typed prefixes, and compare numeric suffixes; retain numeric-only legacy
      comparisons at compatibility boundaries.
      comparison only within the same prefix; reject mixed-family ordering.
- [ ] Update `ValueResolver` ID parsing and tests.
- [ ] Audit `FsPath`, `QueryExpression`, `Node`, `AttributeExpression`, and
      uncertainty evaluation for numeric graph assumptions.
- [ ] Ensure rule-created fresh node IDs use the configured namespace.
- [ ] Preserve integer rule indexes and annotation indexes.
- [ ] Add tests for queries involving `f1`, `c1`, `d1`, and literal `x1`.

### LiGER F: Web/API Graph Conversion

Primary files:

- `LigerWebGraph.java`
- `LigerController.java`
- `LigerGraphTranslator.java`
- `LinguisticStructureMerger.java`

- [x] Replace `isIntegerValue(...)` with namespace-policy node detection.
- [x] Create edges only when the target is a recognized node ID.
- [x] Keep values such as `x1`, `i1`, and `42` in AVPs unless configured as
      node IDs.
- [x] Derive default node type from the recognized prefix.
- [x] Make explicit `NODE_TYPE` constraints override derived types.
- [ ] Ensure merged syntax/DRT graphs do not collide across namespaces.
- [x] Preserve graph IDs in JSON and Cytoscape payloads.
- [x] Update uploaded graph handling and graph-only translation.
- [ ] Add API tests for `NAME(d1,x1)` and `IN(d2,d1)`.

### LiGER G: `proj` and Serialization Cleanup

- [ ] Inventory all remaining `getProj/setProj` consumers.
- [ ] Replace projection-based node-family inference with namespace policy where
      appropriate.
- [ ] Decide whether `proj` remains needed for XLE projection semantics, which
      are distinct from node family.
- [ ] Do not remove `proj` merely because node prefixes exist; remove it only
      after Prolog output, graph rendering, and merge behavior are covered.
- [ ] Update `GraphConstraint.equals/hashCode/toJson/parseJson` consistently.

### LiGER H: Compatibility and Verification

- [ ] Add a legacy numeric-ID input adapter or compatibility policy.
- [ ] Decide whether legacy IDs are normalized to `f`/`c`/`d` based on source
      context or preserved as opaque IDs.
- [ ] Update existing fixtures and tests incrementally.
- [ ] Run `./mvnw test`.
- [ ] Run `./mvnw -Dtest=DemoApplicationTests test`.
- [ ] Exercise CLI XLE parsing, rewrite rules, semantics extraction, JSON upload,
      graph translation, and merge endpoints.
- [ ] Check both CLI and `-web` paths.

## xleplusglue-client

### Client A: Contract Types and Normalization

- [ ] Confirm graph component IDs, `source`, and `target` are treated as strings.
- [ ] Update model comments/types if they still imply numeric IDs.
- [ ] Ensure graph normalization preserves `f1`, `c1`, `d1`, and `g1` exactly.
- [ ] Ensure fallback IDs cannot collide with backend IDs.

### Client B: Rendering

- [ ] Add a `node[node_type="dnode"]` style in the graph visualizer.
- [ ] Decide whether `dnode` uses one uniform style or a neutral style distinct
      from `input`, `cnode`, and `gnode`.
- [ ] Update subgraph rendering styles with the same `dnode` selector.
- [ ] Keep edge rendering based on `edge_type`, not numeric IDs.

### Client C: Highlighting and Rule Display

- [ ] Remove numeric-only target checks in `graph-inspector.component.ts`.
- [ ] Treat a target as a graph node if it exists in the graph or is recognized
      by the graph payload, not merely if it matches `\\d+`.
- [ ] Preserve `#` query notation while stripping it only for graph lookup.
- [ ] Ensure rule fact display renders `#f1`, `#c1`, and `#d1` correctly.
- [ ] Verify `NAME(d1,x1)` highlights only `d1` unless `x1` is an actual node.
- [ ] Add component tests for typed IDs and literal `x1` values.

### Client D: Verification

- [ ] Run the client unit test suite.
- [ ] Test graph rendering with one f/c/d/g node each.
- [ ] Test uploaded and merged graph payloads.
- [ ] Test query highlights and applied-rule highlights.
- [ ] Test layout preservation after graph updates with typed IDs.

## LFGxDRT

### LFGxDRT A: Internal/Public ID Separation

- [ ] Keep internal DRS semantics and subtype distinctions intact.
- [ ] Define public LiGER IDs separately from display names such as `x1`.
- [ ] Assign all exported DRT graph nodes IDs in the `d` namespace.
- [ ] Preserve `x1` as a label/value, not as a node ID.
- [ ] Decide deterministic allocation for DRS states, referents, and conditions.

### LFGxDRT B: Graph Compiler

Primary file: `src/main/java/de/ukon/lfgxdrt/liger_graph/LigerGraphCompiler.java`.

- [ ] Change public node IDs from unprefixed IDs, `s0`, `val:...`, and scoped
      condition IDs to the DRT namespace, e.g. `d1`, `d2`, `d3`.
- [ ] Keep explicit internal node subtype information available until JSON
      serialization, if needed for DRT-local rendering.
- [ ] Ensure referent labels remain `x1`, `x2`, etc.
- [ ] Ensure `NAME(dN,x1)` is emitted as a literal target.
- [ ] Ensure graph relations between DRT nodes use `d` targets.
- [ ] Ensure source indexes such as `i4` remain literals.
- [ ] Ensure all generated IDs are unique within a graph.

### LFGxDRT C: JSON Export

Primary file: `src/main/java/de/ukon/lfgxdrt/liger_graph/LigerGraph.java`.

- [x] Remove the `00` numeric normalization used by `prefixSemanticId(...)`.
- [x] Serialize original typed IDs directly.
- [x] Emit `NODE_TYPE: dnode` for all public DRT nodes, or emit the equivalent
      contract expected by LiGER.
- [x] Keep `NAME` targets unprefixed when they are display/value strings.
- [x] Keep `SRC` targets as literal source-index values.
- [x] Update JSON tests to assert `d` IDs and `dnode` types.
- [x] Add regression coverage explicitly proving `x1` is not an edge target.

### LFGxDRT D: Verification

- [x] Run the LFGxDRT test suite.
- [x] Update `LigerGraphJsonPrefixTest` for typed DRT IDs.
- [ ] Add a test with a DRS state, referent, condition, `NAME`, `IN`, and `SRC`.
- [ ] Verify the JSON can be consumed by LiGER without additional ID rewriting.
- [ ] Verify existing SVG/DRS rendering remains unchanged where it does not use
      the LiGER graph export.

## Recommended Execution Order

- [ ] Cross-repository contract and namespace policy tests.
- [ ] LFGxDRT public graph ID/export changes.
- [ ] LiGER policy and compatibility adapter.
- [ ] LiGER XLE conversion.
- [ ] LiGER traversal, query, rules, and semantics.
- [ ] LiGER web graph conversion and merge behavior.
- [ ] Client rendering and highlighting.
- [ ] End-to-end regression tests.
- [ ] Remove obsolete numeric detection and only then evaluate `proj` removal.

## Commit Checkpoints

Use small commits, preferably one logical phase per commit:

1. Contract and policy tests
2. LFGxDRT typed graph IDs
3. LiGER typed-ID policy and compatibility handling
4. LiGER XLE conversion
5. LiGER traversal/query/rule updates
6. LiGER web/API conversion
7. Client updates
8. Fixture cleanup and final regression verification

After each commit, record:

- repository
- commit hash
- tests run
- known failures or follow-up work

## Progress Log

| Date | Repository/phase | Commit | Verification | Notes |
| --- | --- | --- | --- | --- |
| 2026-07-21 | Planning | - | Repository inspection complete | Worktrees contain pre-existing untracked files; none modified |
| 2026-07-21 | LiGER A: namespace policy | working tree | `./mvnw -Dtest=NodeIdPolicyTest test`; `./mvnw test` (134 tests, 0 failures, 2 skipped) | Added `NodeIdPolicy` and focused tests; no existing graph behavior changed |
| 2026-07-21 | LiGER F: web graph integration | working tree | `./mvnw -Dtest=LigerGraphTranslatorTest,NodeIdPolicyTest test`; `./mvnw test` (134 tests, 0 failures, 2 skipped) | Typed IDs now create edges and derive node types; numeric IDs remain compatibility-only |
| 2026-07-21 | LFGxDRT B/C: public graph IDs | working tree (`LFGxDRT`) | `mvn test` (2 tests, 0 failures); no wrapper present | LiGER JSON export now emits `dN` IDs and `dnode`; internal DRT IDs remain unchanged |
| 2026-07-21 | LiGER C: XLE typed conversion | working tree | `./mvnw -Dtest=FsProlog2JavaTest test` (1 test passed); focused query run still has `QueryParserTest.testQueryParser17` failing | f/c IDs are emitted; glue targets use `g`; legacy numeric uncertainty constants need follow-up |
| 2026-07-21 | LiGER E/D: typed-ID consumer compatibility | working tree | `./mvnw test`: 137 tests, 0 failures, 0 errors, 2 skipped | Preserved zero-padded legacy IDs exactly, updated synthetic MC assertions to `gN`, and updated legacy constant queries to typed `*f0`/`*c11` references |
| 2026-07-21 | LiGER E: ID comparisons | working tree | 14 focused tests passed, including `ValueResolverTest` | `ValueResolver.compareIds` already strips prefixes through `IdValue`, enforces same-prefix typed comparisons, and compares numeric suffixes; added regression coverage |
