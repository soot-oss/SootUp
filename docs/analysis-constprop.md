# Example: Constant Propagation

## The problem

A variable holds a **constant** at a program point if every execution path that reaches
that point assigns the same constant value to it. Knowing this lets a compiler replace
`int y = x` with `int y = 4` (no runtime load needed), eliminate dead branches
(`if (0 == 0)` is always true), and prove the absence of array-index-out-of-bounds errors.

The challenge is that constants must be tracked *flow-sensitively*: `x = 1; x = 4;`
means `x` is 4 after the second assignment, not 1 or "either." This is where dataflow
analysis earns its keep.

This page builds a constant propagation analysis from scratch. It is the forward
counterpart to [Live Variable Analysis](analysis-liveness.md) — read that first if you
haven't already.

---

## Step 1 — The target program

```java
--8<-- "sootup.examples/src/test/resources/ConstProp/source/Example.java"
```

`x` is redefined four times. A flow-sensitive analysis correctly concludes `x = 4`
at the final assignment and therefore `y = 4`. A flow-*insensitive* approach would
see four definitions and declare `x` unknown (NAC).

---

## Step 2 — The five elements

| Element | This analysis |
|---|---|
| **Direction** | [Forward](glossary.md#forward-analysis) — constants accumulate as statements execute in order |
| **Fact domain** | `Map<Local, Value>` — for each local, one of `UNDEF`, `Constant(n)`, or `NAC` |
| **[Boundary condition](glossary.md#boundary-condition)** | Parameters → `NAC`; all other vars absent (= `UNDEF`) |
| **[Initial fact](glossary.md#initial-fact)** | Empty map (every variable starts as `UNDEF`) |
| **[Meet](glossary.md#meet-operator)** | Per-variable `meetValue`: `NAC ⊓ x = NAC`; `UNDEF ⊓ x = x`; `c ⊓ c = c`; `c1 ⊓ c2 = NAC` |
| **[Transfer function](glossary.md#transfer-function)** | Evaluate the RHS; update the LHS in the OUT map |

**Why forward?** Constants flow in the direction of execution: a definition at line 3
affects uses at line 5, not the other way around.

**Why map parameters to NAC at the boundary?** The caller decides parameter values; from
the method's perspective they are unknown. Anything is possible — NAC is the correct
conservative choice.

**Why a three-valued lattice?** Two is not enough. `UNDEF` ("no information yet") is
different from `NAC` ("too many values"). Without `UNDEF`, initialising every variable
to `NAC` before analysis would immediately kill any hope of precision at join points:
`NAC ⊓ c = NAC` even if only one path actually reaches the join.

---

## Step 3 — The lattice type

The lattice value for one variable. `UNDEF` is the bottom (least information); `NAC`
is the top (most information — the analysis has given up on knowing the value); constants
sit in between:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/constprop/Value.java:lattice-type"
```

---

## Step 4 — Building the analysis

### Fact type and boundary

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/constprop/ConstantPropagation.java:boundary"
```

### Meet operator

At a [join point](glossary.md#join-point) the facts from both paths are merged per
variable. The [meet](glossary.md#meet-operator) is applied to each variable's value
independently — if the two incoming paths agree on a constant, we keep it; if they
disagree, the result is `NAC`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/constprop/ConstantPropagation.java:meet-value"
```

### Transfer function

For assignment statements we evaluate the right-hand side expression and update the
left-hand side. For all other statements the transfer is the identity (OUT = IN):

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/constprop/ConstantPropagation.java:transfer"
```

---

## Step 5 — The worklist solver

The same `DataflowSolver` used for liveness drives this analysis too — only the direction
changes. In the forward case it meets all predecessor `OUT` facts into `IN`, applies the
transfer, and re-schedules successors when `OUT` changed:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/dataflow/DataflowSolver.java:worklist-loop"
```

**Hand trace on `Example.assign`** (no branches, so a single pass suffices):

| Statement | IN | OUT |
|---|---|---|
| `x = 1` | `{}` | `{x=1}` |
| `x = 2` | `{x=1}` | `{x=2}` |
| `x = 3` | `{x=2}` | `{x=3}` |
| `x = 4` | `{x=3}` | `{x=4}` |
| `y = x` | `{x=4}` | `{x=4, y=4}` |
| `return` | `{x=4, y=4}` | — |

---

## Step 6 — Setting up and running the analysis

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/constprop/ConstantPropagationTest.java:setup"
```

---

## Step 7 — Verifying the result

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/constprop/ConstantPropagationTest.java:assertions"
```

The test asserts that at the final assignment, every local with a known value holds the
constant `4`. CI runs this on every pull request; a breaking API change will fail this
test before it can silently invalidate the docs.

---

## What to try next

- [Live Variable Analysis](analysis-liveness.md) — the backward counterpart, if you
  haven't seen it yet.
- [Write your own interprocedural analysis](write_analyses.md) — extend analysis across
  method call boundaries using the IFDS/IDE framework.
- [Built-in Analyses](builtin-analyses.md) — liveness and dominance analyses that ship
  with SootUp, ready to use without implementing the solver yourself.
