# Modify a ControlFlowGraph

## The goal

The (fictional) bank *SootBank* ships a class `Vault` whose `withdraw` method was written in a hurry:

```java
--8<-- "sootup.examples/src/test/resources/MutatingCFG/source/Vault.java"
```

An audit lists four problems, and nobody has access to the sources any more, only to the
`.class` files. We will fix all of them in the [ControlFlowGraph](glossary.md) of the method:

1. every call must be **logged**: `Audit.log(amount)` at the very beginning,
2. `Legacy.check` is insecure and must be **replaced** by `Secure.check`,
3. the leftover debug output `System.out.println` must be **removed**,
4. non-positive amounts must be **rejected** (`return -1`),
5. a `RuntimeException` during the withdrawal must be **caught**, reported via
   `Audit.failed(e)` and answered with `return -1`.

Each requirement needs a different kind of modification, so step by step this chapter walks
through the API of `MutableControlFlowGraph`. At the end we pack everything into a
[BodyInterceptor](bodyinterceptors.md), so every view loads the hardened method automatically.

All code on this page is taken from
`sootup.examples/src/test/java/sootup/examples/mutatingCFG`, which is run as a test on every
pull request.

---

## Step 0 — Load the method and look at its graph

We load the example classes with SootUp's default body interceptors, which turn bytecode into
readable Jimple:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:load-view"
```

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:load"
```

This is the Jimple of `withdraw`:

```jimple
{
    Vault this;
    int $stack3, $stack4, $stack5, l1;
    java.io.PrintStream $stack2;

    this := @this: Vault;
    l1 := @parameter0: int;
    staticinvoke <Legacy: void check(int)>(l1);
    $stack2 = <java.lang.System: java.io.PrintStream out>;
    virtualinvoke $stack2.<java.io.PrintStream: void println(int)>(l1);
    $stack3 = this.<Vault: int balance>;
    $stack4 = $stack3 - l1;
    this.<Vault: int balance> = $stack4;
    $stack5 = this.<Vault: int balance>;

    return $stack5;
}
```

A `ControlFlowGraph` can be read on two levels:

- **Stmts**: `successors(stmt)`, `predecessors(stmt)`, `exceptionalSuccessors(stmt)`,
  `getStmts()` (in Jimple order), `getStartingStmt()`, `containsNode(stmt)`.
- **Blocks**: a [BasicBlock](glossary.md) is a maximal sequence of stmts that is always executed
  from head to tail. Only the tail may branch, and all stmts of a block share the same exceptional
  successors. `getBlocks()`, `getBlockOf(stmt)`, `getBlocksSorted()`.

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:inspect"
```

`withdraw` has no branches and no traps, so its graph is a single block.

!!! info "Falls through or branches?"
    Every [Stmt](jimple-stmts.md) is either a `FallsThroughStmt` (it continues with the next stmt,
    e.g. an assignment or an invoke), a `BranchingStmt` (`goto`, `switch`) or it ends the flow
    (`return`, `throw`). `JIfStmt` is both: its successor `0` is the fall-through flow, its successor
    `1` the branch target. Many modification methods only accept one of these kinds, and the types
    in their signatures say which.

---

## Step 1 — Get a mutable copy

The graph of a loaded `Body` is unmodifiable. `Body.builder(body, modifiers)` copies the body
into a `BodyBuilder`, whose graph is a `MutableBlockControlFlowGraph`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:mutable-copy"
```

The copy shares the `Stmt` objects with the original body, but not the graph. Whatever we change
from now on leaves `body` untouched.

---

## Step 2 — Insert: log every call

`insertBefore(existing, stmts...)` puts new stmts in front of `existing`. All flows that reached
`existing` now reach the first new stmt. `insertAfter(existing, stmts...)` works the other way
round. Both accept only `FallsThroughStmt`s that are no `JIfStmt`, because the inserted stmts
always continue into `existing` or into its old successor.

The log call has to come after the identity stmts (`this := @this`, `l1 := @parameter0`), which
must stay at the beginning of a body:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:insert"
```

Both methods also take a list of stmts and a trap map `Map<ClassType, Stmt>` (exception type →
handler stmt) for the new stmts. The single-stmt overloads used here pass an empty map, so the new
stmts are covered by no trap. Pass the traps of the neighbouring stmt
(`graph.exceptionalSuccessors(existing)`) if the new stmts belong into the same `try` block.

---

## Step 3 — Replace: swap the insecure check

`replaceNode(old, new)` puts `new` at the place of `old`. All flows into and out of `old` move to
`new`, and so do its traps.

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:replace"
```

!!! warning "One stmt, one place"
    Stmts are compared by identity, and a stmt object can be in a graph only once. Always create
    a new stmt instead of reusing one: `replaceNode` and all insert methods reject a stmt that is
    already in the graph.

`replaceNode` can also change the kind of a stmt, e.g. `goto` → `nop` or `nop` → `goto`. When the
new stmt expects fewer successors than the old one still has, it throws. Flows that are missing
after the replacement (e.g. the target of a new `goto`) are added with the edge methods of step 5.

---

## Step 4 — Remove: drop the debug output

`removeNode(stmt)` removes a stmt and **keeps the flow**: its predecessors now continue with its
successor. `removeNode(stmt, false)` removes the stmt together with its flows, which may leave a
part of the graph unreachable. Keeping the flow is impossible for a `BranchingStmt` with more
than one successor.

`println` needs a second stmt that loads `System.out`, so we remove both. The local that held
`System.out` is unused afterwards, and we remove it from the builder as well:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:remove"
```

To remove a whole block, use `removeBlock(block)`.

---

## Step 5 — Change the flow: a guard clause

Now the method needs a branch: `if l1 <= 0 goto reject`, where `reject` is a new `return -1`.
This is a job for the edge methods:

| Method | Effect |
|---|---|
| `putEdge(FallsThroughStmt from, Stmt to)` | `from` falls through into `to`. A `to` that is not in the graph yet is added, with the traps of `from`. |
| `putEdge(BranchingStmt from, int idx, Stmt to)` | sets successor `idx` of `from` |
| `setEdges(BranchingStmt from, Stmt... targets)` | sets all successors of `from` at once, in order |
| `removeEdge(Stmt from, Stmt to)` | removes all flows `from` → `to` and returns their successor indices |
| `replaceSuccessorEdge(Stmt from, Stmt oldTo, Stmt newTo)` | redirects all flows `from` → `oldTo` to `newTo` |
| `addNode(Stmt)` / `addNode(Stmt, traps)` | adds a stmt without any flow |
| `addBlock(List<Stmt>)` / `addBlock(List<Stmt>, traps)` | adds a whole sequence of stmts |
| `setStartingStmt(Stmt)` | sets the entry of the method |

A `FallsThroughStmt` has exactly one fall-through successor, so before the log call can fall
through into the guard, we cut its current flow:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:branch"
```

The two `putEdge(guard, ...)` calls could also be written as `graph.setEdges(guard, afterLog, reject)`.
For a `switch`, the successor indices follow the order of its cases, and the default target comes
last.

The method now consists of three blocks: entry up to the guard, the withdrawal, and `reject`.

---

## Step 6 — Exceptional flow: catch a RuntimeException

In a `ControlFlowGraph` an exceptional flow goes from a stmt to the handler stmt for one
exception type. A handler is a block that starts with `e := @caughtexception`. The trap ranges
(`catch ... from ... to ... with ...`) you see in Jimple are computed from these flows when the
body is printed or serialized.

We add the handler as a new block, then give every stmt after the guard an exceptional flow into
it:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:trap"
```

Note the `following` variable: we read the successor *before* changing the graph, because the
graph may restructure its blocks while we edit (see the rules below).

The way back works at three granularities:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:undo-trap"
```

---

## Step 7 — Build the new body

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:build"
```

`build()` checks that every stmt has the right number of flows for its kind. The hardened method:

```jimple
{
    Vault this;
    int $stack3, $stack4, $stack5, l1;
    java.lang.RuntimeException e;

    this := @this: Vault;
    l1 := @parameter0: int;
    staticinvoke <Audit: void log(int)>(l1);

    if l1 <= 0 goto label4;

  label1:
    staticinvoke <Secure: void check(int)>(l1);
    $stack3 = this.<Vault: int balance>;
    $stack4 = $stack3 - l1;
    this.<Vault: int balance> = $stack4;
    $stack5 = this.<Vault: int balance>;

  label2:
    return $stack5;

  label3:
    e := @caughtexception;
    staticinvoke <Audit: void failed(java.lang.RuntimeException)>(e);

    return -1;

  label4:
    return -1;

 catch java.lang.RuntimeException from label1 to label2 with label3;
}
```

All five requirements are met.

---

## Step 8 — Automate it: a BodyInterceptor

A `Body` built by hand lives only in our variable. To make *every* view load the hardened method,
we do the same work in a [BodyInterceptor](bodyinterceptors.md). It receives the `BodyBuilder` of
each method while the method is loaded:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/VaultHardeningInterceptor.java:interceptor"
    // harden(...) contains the steps 2 – 6 from above
}
```

It is registered on the input location after the default interceptors:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/mutatingCFG/MutatingControlFlowGraphTest.java:register-interceptor"
```

### Many edits: batch mode

The interceptor wraps its edits in `beginBatch()` / `endBatch()`. After each edit, a
`MutableBlockControlFlowGraph` merges blocks that became mergeable, so that blocks stay maximal.
During a sequence of edits that often means splitting and merging the same blocks again and
again. Inside a batch, merging is postponed until the outermost `endBatch()`. The stmt-level flows
are exact during the batch, only `getBlocks()` may return more, smaller blocks than necessary.
Batches can be nested. Put `endBatch()` in a `finally` block.

!!! tip "Changing a single method without an interceptor"
    To replace the body of one `SootMethod` in an existing view, wrap the new body into an
    `OverridingBodySource` and create a new method with `withOverridingMethodSource`; see
    `sootup.examples/.../mutatingSootClass/MutatingTest.java`.

---

## Rules of the graph

- **Only a block's tail may branch.** Stmts inside a block fall through. The graph splits and
  merges blocks for you.
- **One fall-through successor.** A `FallsThroughStmt` falls through into exactly one stmt. For
  the body to be printable as linear code, a stmt should also be the fall-through target of at
  most one stmt; connect further predecessors with a `goto`. A fall-through cycle (`a` falls into
  `b`, `b` into `a`) can never be linearized and is rejected.
- **New stmts, always.** A stmt object can be in a graph only once.
- **The starting stmt is a block head.** It changes with `setStartingStmt`, or when you insert
  before or remove the first stmt.
- **Keep `Stmt`s, not `BasicBlock`s.** Blocks are split and merged by almost every modification,
  so a block you got before an edit may be outdated after it. Look it up again with
  `getBlockOf(stmt)`.
- **Iterate over a copy.** `getStmts()` returns a new list, so you can change the graph while you
  loop over it. When you walk along `successors(...)`, read the next stmt before you change the
  current one (see step 6).
- **Invalid arguments fail early.** An invalid edit throws an `IllegalArgumentException` and does
  not change the graph. `MutableBlockControlFlowGraph.validateBlocks()` checks the internal
  consistency, which is handy while debugging your own transformation.

## Costs

| Operation | Cost |
|---|---|
| `getBlockOf`, `successors`, `predecessors`, `containsNode` | O(1) |
| insert/remove at the head or tail of a block, `replaceNode` | O(1) plus the edge changes |
| insert/remove in the middle of a block | proportional to the shorter part of the block |
| split/merge of a block | proportional to the smaller part |
| `getStmts()` | O(n) copy; the order is cached and survives changes inside a block |
