# Uncertainty Path Algorithm

This describes how `@GF*`, `@GF+`, inside-out paths, and off-path constraints are evaluated.

## Goal

An uncertainty segment enumerates all valid path candidates, then filters them.
Each surviving candidate yields a solution, and the variable after the uncertainty binds to the path endpoint.

## Path Expansion

- Parse the uncertainty payload into path atoms.
- Expand templates like `@GF` into concrete labels.
- Expand quantifiers:
  - `*` means zero or more hops.
  - `+` means one or more hops.
  - no quantifier means exactly one hop.
- Include the zero-hop candidate `{}` when `*` is used.

## Semantics Change

Historically, zero-hop candidates were filtered as if they were real path nodes.
That made `*` behave too strictly for some off-path queries, because the empty path could be rejected before any actual hop was taken.

Current behavior:

- zero-hop is a genuine fallback candidate for `*`
- off-path constraints apply to traversed hops
- the empty path itself is not treated as a hop that must satisfy the off-path clause

## Traversal Direction

- The uncertainty traversal may be regular or inside-out.
- Inside-out changes how hops are followed.
- Off-path constraints do not flip with inside-out traversal.
- Off-path constraints always inspect the reached node in the normal downward graph orientation.

## Off-Path Constraints

For a clause like `:~(->SUBJ)`:

- `->` checks the node reached by the current hop.
- That node must not have an outgoing `SUBJ` edge.
- For a path `#a -OBJ-> #b`, the constraint is checked on `#b`.
- For a longer path, every reached node must satisfy the constraint.

Examples:

- One-hop `OBJ`: the node reached after `OBJ` must not have an outgoing `SUBJ` edge.
- Two hops `OBJ > OBL`: both intermediate/reached nodes must satisfy the same filter.
- Zero-hop `*`: allowed as the empty fallback, without applying the off-path clause to the current node itself.

## Candidate Evaluation

For each candidate path:

1. Start from the current bound node.
2. Walk the path hop by hop.
3. At each hop, apply the off-path constraint to the reached node.
4. Reject the candidate if any hop violates the constraint.
5. If the path survives, bind the post-uncertainty variable to the endpoint node.

## Distinct Solutions

- Different valid endpoint nodes must remain distinct solutions.
- Do not collapse branches by choosing an arbitrary reference for a variable.
- Composition must preserve all compatible bindings across adjacent query parts.

## Practical Consequences

- S18 and S19 both allow the same core query shape to produce five solutions when the off-path constraint is applied correctly.
- A zero-hop path is a fallback candidate, not a synthetic hop.
- Paths must never escape the allowed GF chain just because the quantifier allows zero hops.
