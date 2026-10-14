# Core Concepts

This page explains the foundational ideas behind SootUp — no class names, no code.
Read it once and everything in the rest of the documentation will make more sense.

---

## What is a program?

At the machine level, a Java program is a collection of `.class` files. Each file
contains one class or interface. Each class contains *fields* (named pieces of memory)
and *methods* (named sequences of instructions). Each method, when called, executes its
instructions one at a time — or sometimes branches, loops, or throws an exception.

That is the complete picture: classes, fields, methods, and instructions. Everything
in static analysis ultimately refers to one of these four things.

---

## What is static analysis?

A program can be *run* (dynamic analysis — you observe what actually happens) or
*read* (static analysis — you reason about what could happen, without executing it).

Static analysis answers questions like:

- "Can this variable ever be `null` when we reach line 40?"
- "Does any execution path lead from `readInput()` to `writeToDatabase()` without
  sanitization in between?"
- "Is there a path where this lock is acquired but never released?"

The key constraint is that static analysis must reason about **all possible inputs and
all possible executions** at once — not just one concrete run. This makes it
conservative: a good static analysis never misses a real bug, but it may report
situations that could not actually occur at runtime (false positives).

---

## What is an Intermediate Representation (IR)?

Neither Java source code nor raw bytecode is ideal for writing analysis tools:

- **Source code** isn't always available (you may only have the `.jar`), and its
  structure — nested expressions, implicit coercions, syntactic sugar — varies with the
  Java version and requires a full parser.
- **Bytecode** is universal but is a *stack-machine* format: instructions push and pop
  an implicit operand stack rather than naming their operands. Tracking data flow
  through unnamed stack slots is cumbersome.

An **Intermediate Representation** is a third form, derived from bytecode, that trades
compactness for clarity. SootUp's IR is called **Jimple**. Its defining properties:

- **Named locals** — every value is held in a named variable; there is no implicit stack.
- **Three-address code** — every statement involves at most one operation and at most
  three operands (`a = b + c`). No nested expressions.
- **Explicit types** — every local has a declared type; every call site names the exact
  receiver type.
- **Flat structure** — one statement per line, no nesting. Control flow is expressed
  via explicit `goto` and `if` statements.

The result is a form where every interesting fact about a statement is directly readable
from that statement alone, which makes writing traversals and analyses straightforward.

---

## What is a Control Flow Graph (CFG)?

Instructions inside a method do not always execute top-to-bottom. An `if` statement
creates a branch; a loop creates a back-edge; a `throw` creates an exceptional path.

A **Control Flow Graph** (CFG) makes this structure explicit:

- Each instruction is a **node** in the graph.
- An **edge** from node A to node B means "B can execute immediately after A."
- A method with no branches has a single chain of edges (a straight line).
- An `if` statement creates two outgoing edges from the condition node — one for "true",
  one for "false".
- A loop creates a back-edge from the loop body's last instruction back to the loop header.

When you "analyse a method", you almost always mean: traverse its CFG and compute some
information at each node (e.g. "which variables are live here?", "what values can this
variable hold?").

---

## What is a Call Graph?

A CFG captures flow *within* one method. A **Call Graph** captures flow *between*
methods: each node is a method, each edge says "method A can invoke method B."

Call graphs are essential for *inter-procedural analysis* — analysis that follows
execution across method boundaries, e.g. tracking a tainted value from an HTTP request
handler through helper methods down to a SQL query.

Building a precise call graph for Java is non-trivial because of dynamic dispatch
(virtual calls) and reflection. SootUp provides several algorithms with different
precision/performance trade-offs: CHA, RTA, and pointer-analysis-based approaches.
See [Call Graphs](callgraphs.md).

---

## What is Dataflow Analysis?

Dataflow analysis is the workhorse of static analysis. The idea:

1. Associate a **fact** with each point in the CFG. A fact is whatever you want to
   know at that point: "which variables might be null?", "what constant value does `x`
   hold?", "which taint sources can reach here?".
2. Define how facts **transfer** across a statement: if `x = null` makes `x` nullable,
   then after that assignment the fact "x is nullable" is in scope.
3. Define how facts **merge** at join points (where two edges converge): if one path
   makes `x` nullable and the other does not, the merged fact is "x might be nullable."
4. Iterate until facts stop changing (**fixed point**). The result is a sound
   approximation of what is true on all possible executions.

Intra-procedural analysis runs within one method's CFG. Inter-procedural analysis
propagates facts across the call graph too, which requires a more careful framework
(see [IFDS/IDE](write_analyses.md)).

---

## How SootUp maps onto these concepts

| Concept | SootUp type |
|---|---|
| The program on disk | `AnalysisInputLocation` |
| The loaded, queryable program | `View` |
| A class | `SootClass` |
| A method | `SootMethod` |
| The IR of a method's body | `Body` (contains Jimple `Stmt`s and `Local`s) |
| The CFG of a method | `ControlFlowGraph` |
| The call graph | `CallGraph` |

Once these are clear, the [Getting Started](getting-started.md) walkthrough is a direct
translation of these concepts into API calls.
