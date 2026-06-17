# Static Analysis Glossary

Definitions for technical terms used throughout this documentation.

---

**Analysis fact** {#analysis-fact}
:   A value (a set, a map, a number) that summarises what the analysis knows about
    program state at a single point in the [CFG](#control-flow-graph). The type of
    the fact is chosen by the analysis designer; for liveness it is `Set<Local>`,
    for constant propagation it is `Map<Local, Value>`.

**Backward analysis** {#backward-analysis}
:   An analysis in which facts travel from successors to predecessors — that is, from
    the method exit toward the method entry. Backward analyses answer questions about
    the *future* of a value: "will this variable be read again?" See also
    [Forward analysis](#forward-analysis).

**Boundary condition** {#boundary-condition}
:   The [analysis fact](#analysis-fact) assigned to the *boundary node* before iteration
    begins — the method entry for [forward analyses](#forward-analysis), the method exit
    for [backward analyses](#backward-analysis). It represents what the analysis knows
    *outside* the method body. For liveness the boundary is the empty set (nothing is
    live after a return); for constant propagation it maps all parameters to NAC
    (their values are unknown at the call site).

**Control Flow Graph (CFG)** {#control-flow-graph}
:   A directed graph whose nodes are individual [Jimple](jimple.md) statements and
    whose edges represent possible execution order: an edge A → B means "statement B
    may execute immediately after statement A." An `if` creates two outgoing edges; a
    loop back-edge returns to an earlier node. In SootUp, `body.getControlFlowGraph()`
    returns a `ControlFlowGraph` whose `successors(stmt)` and `predecessors(stmt)` give
    the adjacent nodes.

**Dataflow analysis** {#dataflow-analysis}
:   An analysis that computes an [analysis fact](#analysis-fact) at each node of a
    [CFG](#control-flow-graph) by repeatedly applying [transfer functions](#transfer-function)
    and [meet operators](#meet-operator) until a [fixed point](#fixed-point) is reached.
    The word "dataflow" refers to facts "flowing" along CFG edges during iteration.

**Fixed point** {#fixed-point}
:   The state in which one more iteration of the [dataflow analysis](#dataflow-analysis)
    would change no [fact](#analysis-fact) anywhere in the [CFG](#control-flow-graph).
    At this point the analysis has converged to its final answer. The
    [worklist](#worklist) algorithm detects this implicitly: it terminates when the
    worklist is empty, which happens exactly when nothing changed.

**Forward analysis** {#forward-analysis}
:   An analysis in which facts travel from predecessors to successors — that is, from
    the method entry toward the method exit. Forward analyses answer questions about the
    *past* of a value: "what constant was assigned to this variable?" See also
    [Backward analysis](#backward-analysis).

**Gen/Kill** {#gen-kill}
:   Informal names for the two parts of a [transfer function](#transfer-function).
    The *gen* set contains facts that the statement *produces* (e.g. a definition of a
    variable is "generated"). The *kill* set contains facts that the statement
    *invalidates* (e.g. a new definition of `x` kills any previous definition of `x`).
    Transfer for most analyses reduces to: `OUT = gen(S) ∪ (IN − kill(S))`.

**Initial fact** {#initial-fact}
:   The [analysis fact](#analysis-fact) assigned to every non-boundary node at the
    start of iteration. Typically the "bottom" element of the [lattice](#lattice) —
    the empty set for set-based analyses, the empty map for map-based ones. Facts can
    only grow (under union) or change (under more complex meet) during iteration; they
    never shrink back below the initial value.

**Intermediate Representation (IR)** {#intermediate-representation}
:   An internal form of a program that is easier to traverse and analyse than either
    source code or raw bytecode. SootUp's IR is [Jimple](jimple.md): a flat,
    three-address, register-machine representation derived from JVM bytecode.

**Join point** {#join-point}
:   A [CFG](#control-flow-graph) node with more than one incoming edge — for example,
    the first statement after an `if/else` or the header of a loop. At a join point,
    facts from multiple paths must be combined using the [meet operator](#meet-operator).

**Lattice** {#lattice}
:   A partially ordered set in which every two elements have a least upper bound (join,
    ⊔) and a greatest lower bound (meet, ⊓). The [analysis fact](#analysis-fact) of a
    dataflow analysis forms a lattice; the ordering represents "more information" vs.
    "less information." For a set-based analysis, the lattice is the power set of
    locals ordered by ⊆, with ∪ as join and ∩ as meet.

**May analysis** {#may-analysis}
:   An analysis that over-approximates: it reports everything that *could* be true on
    *at least one* execution path. [Union](#meet-operator) at [join points](#join-point)
    ("if it is true on either path, report it"). May analyses are sound but may report
    false positives. Liveness is a may analysis: a variable is reported live if it might
    be used on some path, even if not on every path.

**Meet operator (⊓)** {#meet-operator}
:   The function that combines [analysis facts](#analysis-fact) from multiple incoming
    paths at a [join point](#join-point). For [may analyses](#may-analysis) this is
    union (∪); for [must analyses](#must-analysis) this is intersection (∩). In the
    SootUp educational framework, `meetInto(fact, target)` applies the meet by mutating
    `target` in place.

**Must analysis** {#must-analysis}
:   An analysis that under-approximates: it reports only what is true on *every*
    execution path. [Intersection](#meet-operator) at [join points](#join-point) ("only
    report it if it is true on all paths"). Must analyses have no false positives but
    may have false negatives. See also [May analysis](#may-analysis).

**Transfer function** {#transfer-function}
:   A function that maps the [analysis fact](#analysis-fact) at one side of a statement
    to the fact at the other side, based on the semantics of that statement.
    For [forward analyses](#forward-analysis): `OUT[S] = f(IN[S])`.
    For [backward analyses](#backward-analysis): `IN[S] = f(OUT[S])`.
    See [Gen/Kill](#gen-kill) for the most common pattern.

**Worklist** {#worklist}
:   The set of [CFG](#control-flow-graph) nodes whose [facts](#analysis-fact) may still
    change and therefore need to be re-processed. The solver initialises the worklist
    with all nodes, then picks one at a time: if the node's fact changes after applying
    the [transfer function](#transfer-function) and [meet](#meet-operator), the affected
    neighbours are added back to the worklist. The algorithm terminates (reaches a
    [fixed point](#fixed-point)) when the worklist is empty.
