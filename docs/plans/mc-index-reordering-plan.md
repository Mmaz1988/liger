# LiGER MC Index Reordering Plan

## Goal
Add a LiGER-side ordering layer for GLUE meaning constructors so that:
- every MC gets a unique synthetic index `1..n`
- indices follow syntactic anchoring order as much as possible
- MCs inside the same `in_set` / same GLUE node may be numbered in any stable order
- the new index is stored in the main LiGER graph as a `GraphConstraint`
- the original f-structure / GLUE source remains traceable

## Intended Behavior
For an input like `Kim yawned`:
- MCs anchored to `Kim` should receive earlier synthetic indices than MCs anchored to `yawned`
- if multiple MCs belong to the same GLUE node, they still each get their own unique index
- the within-node order is not semantically important

## Backend Plan

### 1. Identify MC-producing nodes
Find the place where LiGER currently extracts meaning constructors from the annotated f-structure.

For each MC-bearing item:
- keep the original LiGER / f-structure reference
- determine its `in_set` membership
- determine the syntactic anchor path that dominates the GLUE node

### 2. Derive a syntactic ordering key
Use the c-structure side to determine where the MC is anchored.

Likely strategy:
- traverse from the GLUE node upward through the `cproj > g:: > GLUE` chain
- identify the c-structure node that dominates the Glue node
- use that c-structure position as the primary sort key

The order should be partial:
- MCs in different syntactic locations are ordered by anchoring position
- MCs inside the same GLUE node may be numbered arbitrarily

### 3. Assign synthetic indices
After grouping by syntactic anchor:
- sort groups by syntactic position
- enumerate all MCs continuously from `1..n`
- within a group, use first-come-first-serve or any stable local order

### 4. Emit graph constraints in the main graph
For each MC-bearing node, add a LiGER graph constraint to the main graph representing the new synthetic index.

Planned convention:
- `GraphConstraint` contains a new `INDEX` value
- that value is the synthetic enumeration
- the original source node remains available in the graph for traceability

### 5. Preserve traceability
Keep enough original data so downstream logic can recover:
- original f-structure node id
- original GLUE / `in_set` association
- new synthetic `INDEX`

### 6. Update GSWB-facing serialization
When preparing MCs for GSWB:
- serialize the new synthetic indices
- do not pass raw f-structure ids as the primary ordering
- keep a mapping back to the original LiGER node for diagnostics and traceability

## Data Model / Contract

### LiGER output
Each solution should continue to carry:
- `solutionKey`
- `graph`
- `appliedRules`
- `meaningConstructors`
- `numberOfMCsets`
- `axioms`

### New MC index data
The LiGER graph should carry the synthetic MC index using the `INDEX` value on a `GraphConstraint`.

## Tests

### Backend tests
Add/adjust tests to verify:
- `apply_rules_xle` and `parse_xle` still return per-solution payloads
- synthetic MC indices are assigned in syntax order
- MCs in the same GLUE node still each get unique indices
- `numberOfMCsets` counts brace-delimited sets, not individual MC lines

### Example regression case
Use a small sentence like:
- `Kim yawned`

Expected:
- MCs anchored to `Kim` get earlier synthetic indices
- MCs anchored to `yawned` get later synthetic indices
- original source mapping remains intact

## Risks / Notes
- The ordering is intentionally partial, not total.
- Same-GLUE-node MCs do not need semantically meaningful internal ordering.
- The main implementation challenge is finding the most reliable c-structure anchor for each Glue node.
- The graph constraint representation needs to stay compatible with the existing LiGER graph model and downstream consumers.

## Open Implementation Question
What is the best exact attachment point for the synthetic `INDEX` value in the current graph model:
- annotation-only graph constraint
- constraint in the main LiGER graph
- both, if downstream consumers need different views
