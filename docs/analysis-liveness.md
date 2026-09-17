# Example: Live Variable Analysis

## The problem

A variable is **live** at a program point if its current value might still be read before
it is overwritten on at least one subsequent execution path. If a variable is *dead* —
never read before being overwritten — then any register holding it can be safely reused.

Compilers use this to allocate registers efficiently. Static analysis tools use it to
detect dead assignments (a write to a variable that is never subsequently read is
suspicious: either the write is dead code, or the variable was meant to be used and
wasn't). Both uses require knowing which variables are live at each point.

This page builds a complete live variable analysis from scratch, introducing each piece
of the [dataflow analysis](glossary.md#dataflow-analysis) framework before the code that
implements it.

---

## Step 1 — The target program

Here is the program we will analyse:

```java
--8<-- "sootup.examples/src/test/resources/Liveness/source/Example.java"
```

The interesting point is the `if/else`: the true branch uses `c` and defines `y`;
the false branch uses `x` and defines `y`. At the [join point](glossary.md#join-point)
after the two branches, facts from both paths must be combined.

---

## Step 2 — The five elements

Every [dataflow analysis](glossary.md#dataflow-analysis) is fully described by five choices:

| Element | This analysis |
|---|---|
| **Direction** | [Backward](glossary.md#backward-analysis) — liveness is about the *future*: will this value be read later? |
| **Fact domain** | `Set<Local>` — the set of locals that are live at this point |
| **[Boundary condition](glossary.md#boundary-condition)** | `∅` — nothing is live after the method returns |
| **[Initial fact](glossary.md#initial-fact)** | `∅` — start with the conservative assumption that nothing is live |
| **[Meet](glossary.md#meet-operator)** | Union (∪) — this is a [may analysis](glossary.md#may-analysis): a variable is live if it is live on *at least one* path |
| **[Transfer function](glossary.md#transfer-function)** | `IN[S] = use(S) ∪ (OUT[S] − def(S))` — [gen/kill](glossary.md#gen-kill) style |

**Why backward?** Because liveness is a property of the future. To know whether `x` is
live *before* statement S, you need to know whether `x` is used *after* S — which means
looking at what comes after. Facts therefore travel backward, from exit toward entry.

**Why union at join points?** Because we want soundness: if `x` might be used on *any*
path after a join, we must report it as live. Union ensures nothing is missed.

---

## Step 3 — The framework interface

The `DataflowAnalysis<F>` interface captures the five elements above as method signatures.
Any analysis that implements it can be driven by the generic `DataflowSolver`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/dataflow/DataflowAnalysis.java:interface-full"
```

---

## Step 4 — Building the analysis

### Fact type and boundary

The [analysis fact](glossary.md#analysis-fact) is `Set<Local>`. The
[boundary condition](glossary.md#boundary-condition) is the empty set (nothing is live
past the return), and the [initial fact](glossary.md#initial-fact) is also empty:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/liveness/LiveVariableAnalysis.java:boundary"
```

### Meet operator

The [meet](glossary.md#meet-operator) is union: if `x` is live on either incoming path,
it is live at the join point. `meetInto` mutates `target` in place so the solver can
reuse the same set object across iterations at each [join point](glossary.md#join-point),
avoiding repeated allocation:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/liveness/LiveVariableAnalysis.java:meet"
```

### Transfer function

The [transfer function](glossary.md#transfer-function) reads `OUT[S]` and writes `IN[S]`
(backward direction). It first *kills* the variable defined by S (if any), then *gens*
the variables used by S:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/liveness/LiveVariableAnalysis.java:transfer"
```

---

## Step 5 — The worklist solver

The generic `DataflowSolver` drives the iteration. In the backward case it meets all
successor `IN` facts into `OUT`, applies the transfer, and re-schedules predecessors
whenever `IN` changed:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/dataflow/DataflowSolver.java:worklist-loop"
```

The solver terminates at the [fixed point](glossary.md#fixed-point) — when the
[worklist](glossary.md#worklist) is empty because no fact changed in the last iteration.

**Hand trace on `Example.compute`** (simplified Jimple, abbreviated local names):

| Statement | IN (live *before*) | OUT (live *after*) |
|---|---|---|
| `return y` | `{y}` | `{}` (boundary) |
| `y = x` (else branch) | `{x}` | `{y}` |
| `y = c` (if branch) | `{c}` | `{y}` |
| `if a > 0` | `{a, b, c, x}` | `{c, x}` (union of both branches) |
| `x = a + b` | `{a, b, c}` | `{a, b, c, x}` |
| `(entry)` | `{a, b, c}` | — |

At the `if` join (looking backward), the two branches bring `{c}` and `{x}`, so their
union is `{c, x}`. Then the `if` condition itself uses `a`, adding it.

---

## Step 6 — Setting up and running the analysis

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/liveness/LiveVariableAnalysisTest.java:setup"
```

---

## Step 7 — Verifying the result

These assertions are what CI checks on every pull request. If the SootUp API changes
in a way that breaks the analysis, this test fails and the documentation-code gap is
caught before it reaches the docs site:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/liveness/LiveVariableAnalysisTest.java:assertions"
```

---

## What to try next

- [Constant Propagation](analysis-constprop.md) — the natural complement: a *forward*
  analysis with a richer [lattice](glossary.md#lattice) instead of a plain set.
- [Write your own interprocedural analysis](write_analyses.md) — extend analysis across
  method boundaries using the IFDS/IDE framework.
- [Core Concepts](concepts.md) — a non-code overview of CFG, IR, and analysis.
