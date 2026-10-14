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

**Edge function** {#edge-function}
:   In [IDE](#ide), the function attached to one edge of the
    [exploded supergraph](#exploded-supergraph); it describes how the *value* carried by a
    [fact](#analysis-fact) changes when that fact flows along the edge. Edge functions must
    support composition (apply one, then the next) and [meet](#meet-operator), and the solver
    composes them repeatedly, so they have to be represented in a form that cannot grow without
    bound — otherwise the analysis never reaches a [fixed point](#fixed-point).

**Exploded supergraph** {#exploded-supergraph}
:   The graph [IFDS](#ifds) and [IDE](#ide) actually solve on. Its nodes are pairs
    *(statement, fact)* drawn from the whole program — the per-method [CFGs](#control-flow-graph)
    stitched together at call sites, with every statement "exploded" into one node per fact.
    A [flow function](#flow-function) is what decides which of these nodes are connected.

**Fixed point** {#fixed-point}
:   The state in which one more iteration of the [dataflow analysis](#dataflow-analysis)
    would change no [fact](#analysis-fact) anywhere in the [CFG](#control-flow-graph).
    At this point the analysis has converged to its final answer. The
    [worklist](#worklist) algorithm detects this implicitly: it terminates when the
    worklist is empty, which happens exactly when nothing changed.

**Flow function** {#flow-function}
:   In [IFDS](#ifds) and [IDE](#ide), the interprocedural counterpart of a
    [transfer function](#transfer-function): it maps an incoming [fact](#analysis-fact) to the set
    of facts that hold afterwards. Four of them are needed — for a normal statement, for entering
    a callee, for leaving a callee, and for flowing around a call.

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

**IDE** {#ide}
:   *Interprocedural Distributive Environment problems.* An extension of [IFDS](#ifds) in which
    every [fact](#analysis-fact) additionally carries a *value* from a [lattice](#lattice), and
    every edge of the [exploded supergraph](#exploded-supergraph) carries an
    [edge function](#edge-function) transforming that value. IFDS answers "does this fact hold
    here?"; IDE answers "and what value does it have?".

**IFDS** {#ifds}
:   *Interprocedural Finite Distributive Subset problems.* A framework that reduces an
    interprocedural [dataflow analysis](#dataflow-analysis) to graph reachability on the
    [exploded supergraph](#exploded-supergraph), so that calls and returns are matched up
    correctly without the analysis having to manage a call stack itself.

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

**Interprocedural CFG (ICFG)** {#interprocedural-cfg}
:   A [CFG](#control-flow-graph) spanning the whole program: the CFGs of the individual methods,
    connected at each call site to the CFGs of the methods a [call graph](callgraphs.md) says may
    be invoked there. In SootUp, `JimpleBasedInterproceduralCFG`.

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

**Seed** {#seed}
:   The starting point of an [IFDS](#ifds) or [IDE](#ide) analysis: a set of
    [facts](#analysis-fact), each paired with the statement at which it is assumed to hold.
    Usually a single [zero fact](#zero-fact) at the first statement of the entry method.

**Transfer function** {#transfer-function}
:   A function that maps the [analysis fact](#analysis-fact) at one side of a statement
    to the fact at the other side, based on the semantics of that statement.
    For [forward analyses](#forward-analysis): `OUT[S] = f(IN[S])`.
    For [backward analyses](#backward-analysis): `IN[S] = f(OUT[S])`.
    See [Gen/Kill](#gen-kill) for the most common pattern.

**Typestate** {#typestate}
:   The state an object is in at a program point with respect to a protocol its API defines — for
    example "open" or "closed" for a file handle. A typestate analysis attaches a finite automaton
    to an API class and checks that every object of that class only sees call sequences the
    automaton accepts. See [the worked example](analysis-typestate.md).

**Worklist** {#worklist}
:   The set of [CFG](#control-flow-graph) nodes whose [facts](#analysis-fact) may still
    change and therefore need to be re-processed. The solver initialises the worklist
    with all nodes, then picks one at a time: if the node's fact changes after applying
    the [transfer function](#transfer-function) and [meet](#meet-operator), the affected
    neighbours are added back to the worklist. The algorithm terminates (reaches a
    [fixed point](#fixed-point)) when the worklist is empty.

**Zero fact** {#zero-fact}
:   An artificial [fact](#analysis-fact) in [IFDS](#ifds) and [IDE](#ide) that holds at every
    statement by construction. Because these frameworks can only propagate facts and never invent
    them, a genuinely new fact has to be generated out of a fact that already holds — and the zero
    fact is the one that always does. Note that its *value* at a [seed](#seed) is the lattice's
    bottom element, not top.
