# Writing Your Own Analysis

This page walks you through writing an analysis from scratch — first intra-procedural
(within one method), then inter-procedural (across method boundaries).

If the terms *CFG*, *dataflow*, or *fixed point* are unfamiliar, read
[Core Concepts](concepts.md) first.

---

## Part 1 — Intra-procedural: a minimal null-tracking analysis

**The goal:** for each statement in a method, determine which local variables *might*
hold `null` at that point.

**The approach — forward dataflow:**

1. Start at the first statement. The initial fact is: "no local is known to be null."
2. At each `x = null` assignment, add `x` to the nullable set.
3. At each `x = someNonNullExpression` assignment, remove `x` (it can no longer be null
   on this path).
4. At join points (where two CFG edges meet), *union* the sets — if `x` is nullable on
   either incoming path, it is nullable here.
5. Repeat until the sets stop changing (fixed point).

### Dependencies

=== "Maven"
    ```xml
    <dependency>
        <groupId>org.soot-oss</groupId>
        <artifactId>sootup.core</artifactId>
        <version>{{ git_latest_release }}</version>
    </dependency>
    ```
=== "Gradle"
    ```groovy
    implementation "org.soot-oss:sootup.core:{{ git_latest_release }}"
    ```

### Implementation

```java
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;

import java.util.*;

/**
 * Forward dataflow analysis: tracks which locals might be null at each statement.
 * Result: a map from Stmt → set of locals that may be null just before that statement.
 */
public class MayBeNullAnalysis {

    private final ControlFlowGraph<?> cfg;

    // The dataflow fact at each statement: locals that may be null *before* the stmt.
    private final Map<Stmt, Set<Local>> factsBeforeStmt = new HashMap<>();

    public MayBeNullAnalysis(Body body) {
        this.cfg = body.getControlFlowGraph();
        run();
    }

    private void run() {
        // Initialise every statement with an empty set.
        for (Stmt stmt : cfg.getNodes()) {
            factsBeforeStmt.put(stmt, new HashSet<>());
        }

        // Worklist: statements whose outgoing fact may have changed.
        Deque<Stmt> worklist = new ArrayDeque<>();
        worklist.add(cfg.getStartingStmt());

        while (!worklist.isEmpty()) {
            Stmt stmt = worklist.poll();

            // The fact *before* this statement.
            Set<Local> before = factsBeforeStmt.get(stmt);

            // Transfer function: compute the fact *after* this statement.
            Set<Local> after = transfer(stmt, before);

            // Propagate to each successor: merge (union) the outgoing fact into it.
            for (Stmt successor : cfg.successors(stmt)) {
                Set<Local> successorFact = factsBeforeStmt.get(successor);
                if (successorFact.addAll(after)) {
                    // The successor's fact grew — re-process it.
                    worklist.add(successor);
                }
            }
        }
    }

    private Set<Local> transfer(Stmt stmt, Set<Local> nullable) {
        Set<Local> out = new HashSet<>(nullable);

        if (stmt instanceof JAssignStmt) {
            JAssignStmt assign = (JAssignStmt) stmt;
            if (assign.getLeftOp() instanceof Local) {
                Local lhs = (Local) assign.getLeftOp();
                if (assign.getRightOp() instanceof NullConstant) {
                    out.add(lhs);        // x = null  → x is now nullable
                } else {
                    out.remove(lhs);     // x = <non-null expr> → x is no longer nullable
                }
            }
        }
        return out;
    }

    /** Returns the set of locals that may be null just before {@code stmt}. */
    public Set<Local> getNullableLocalsBefore(Stmt stmt) {
        return Collections.unmodifiableSet(factsBeforeStmt.getOrDefault(stmt, Set.of()));
    }
}
```

### Using the analysis

```java
// Assume you already have a SootMethod (see Getting Started).
Body body = sootMethod.getBody();
MayBeNullAnalysis analysis = new MayBeNullAnalysis(body);

for (Stmt stmt : body.getControlFlowGraph().getNodes()) {
    Set<Local> nullable = analysis.getNullableLocalsBefore(stmt);
    if (!nullable.isEmpty()) {
        System.out.println("Before " + stmt + " — may be null: " + nullable);
    }
}
```

!!! note
    This is a *may* analysis (union at merge points) — it reports locals that are null
    on *at least one* path. A *must* analysis would intersect instead: it reports locals
    that are null on *every* path. Which you need depends on your goal.

---

## Part 2 — Inter-procedural: IFDS / IDE

Intra-procedural analysis stops at method boundaries. To track whether a value sourced
in `readInput()` can reach `executeQuery()` — potentially through several intermediate
methods — you need *inter-procedural* analysis.

### The core insight

The **IFDS framework** (Interprocedural Finite Distributive Subset problems) reduces
inter-procedural dataflow to a **graph reachability** problem. You describe:

- Which facts are generated at source statements (e.g. "this value came from user input")
- How facts propagate through normal statements (flow functions)
- How facts cross call edges (call-to-start, end-to-return flow functions)

IFDS then builds an *exploded supergraph* and answers "can fact D reach statement S?"
by checking reachability in that graph. This gives provably sound results without
manually managing call stacks.

The **IDE framework** extends IFDS to carry *values* along with facts (e.g. not just
"x is tainted" but "x's value is 3 * input + 7"), enabling constant propagation and
linear constant analysis.

### Dependencies

=== "Maven"
    ```xml
    <dependency>
        <groupId>org.soot-oss</groupId>
        <artifactId>sootup.analysis.interprocedural</artifactId>
        <version>{{ git_latest_release }}</version>
    </dependency>
    ```
=== "Gradle"
    ```groovy
    implementation "org.soot-oss:sootup.analysis.interprocedural:{{ git_latest_release }}"
    ```

### Where to look

SootUp integrates with [Heros](https://github.com/Sable/heros) for IFDS/IDE.
Working examples — taint analysis, linear constant propagation, type-state analysis —
are in the test suite of the `sootup.analysis.interprocedural` module.
They are the most concrete starting point for writing your own inter-procedural analysis.

For background on the algorithms themselves:

- [IFDS lecture (YouTube)](https://www.youtube.com/watch?v=QkK79fT0TFU&ab_channel=SecureSoftwareEngineering)
- [IDE lecture (YouTube)](https://www.youtube.com/watch?v=0uMHX3UY9bg&ab_channel=SecureSoftwareEngineering)

---

## What to read next

- [Built-in Analyses](builtin-analyses.md) — liveness and dominance analyses that ship with SootUp
- [Call Graphs](callgraphs.md) — required for inter-procedural analysis
- [Body Interceptors](bodyinterceptors.md) — pre-processing passes that simplify the IR before analysis
