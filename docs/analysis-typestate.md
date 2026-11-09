# Example: Typestate Analysis with Heros

## The problem

Many APIs have an unwritten rule about the *order* in which their methods may be called.
A file handle must be opened before it is written to. An `Iterator` must not be advanced
after the collection was modified. A `Cipher` must be initialised before it encrypts.
The Java type system does not express any of this: every one of those calls compiles.

A **typestate** is the missing piece of information — not "what type is this object?"
but "what state is this object in, right now, at this program point?". A typestate
analysis attaches a small finite automaton to an API class, and then checks that every
object of that class only ever sees call sequences the automaton accepts.

Two things make this harder than the analyses in
[Live Variable Analysis](analysis-liveness.md) and
[Constant Propagation](analysis-constprop.md):

- The object usually travels across method boundaries, so the analysis has to be
  **interprocedural**.
- The analysis has to track *which* objects exist and, for each of them, *which state* it
  is in. That is two different kinds of information at once.

[IDE](glossary.md#ide) — the framework [Heros](https://github.com/Sable/heros) implements
and SootUp binds to — is built exactly for that shape of problem. This page configures it
step by step. Every snippet is taken from
`sootup.examples/src/test/java/sootup/examples/typestate`, which is compiled and run by
CI, so what you read here is what actually executes.

---

## Step 1 — The target program

The API whose protocol we want to enforce:

```java
--8<-- "sootup.examples/src/test/resources/Typestate/source/FileHandle.java"
```

And the program using it:

```java
--8<-- "sootup.examples/src/test/resources/Typestate/source/Example.java"
```

Note that `correctUsage` performs the `write` inside a *second* method. An
intraprocedural analysis would simply not see it.

---

## Step 2 — The protocol as an automaton

The rule "open, then write as often as you like, then close" is a two-state automaton:

```text
                      open
           ┌─────────────────────────┐
           │                         ▼
    ╔═════════════╗            ┌───────────┐ ──┐
    ║   CLOSED    ║            │   OPEN    │   │ write
    ╚═════════════╝            └───────────┘ ◄─┘
           ▲                         │
           └─────────────────────────┘
                      close

    ╔═╗  initial state, and the only accepting state
```

| From     | Event   | To       |
|----------|---------|----------|
| `CLOSED` | `open`  | `OPEN`   |
| `OPEN`   | `write` | `OPEN`   |
| `OPEN`   | `close` | `CLOSED` |

Every transition not in that table is a violation. `CLOSED --write--> ?` has no entry, so
calling `write` on a closed handle is an error.

Written with the small `Typestate` helper from the example:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateAnalysisTest.java:automaton"
```

States are plain `int`s on purpose — a state has to fit inside the value the solver
propagates, and it has to be cheap to compare:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/Typestate.java:automaton-api"
```

---

## Step 3 — Why IFDS is not enough, and what IDE adds

[IFDS](glossary.md#ifds) turns an interprocedural analysis into a graph reachability
problem over an [exploded supergraph](glossary.md#exploded-supergraph). Each node of that
graph is a pair *(statement, fact)*, and the analysis question becomes: which pairs are
reachable from the starting pair?

That answers *"is this local one of the handles I care about?"* — a yes/no question. It
cannot answer *"and which state is it in?"*, because the only thing IFDS computes is
reachability.

[IDE](glossary.md#ide) adds a second dimension. Every edge of the exploded supergraph
additionally carries an **[edge function](glossary.md#edge-function)**: a function that
transforms a *value* as the fact flows along that edge. So the analysis now computes two
things at every program point — the set of facts that reach it, and for each fact a value.

For a typestate analysis the split is natural:

| IDE element | Our choice | Meaning |
|---|---|---|
| Facts `D` | Jimple `Value` | the locals that currently point at a tracked object |
| Values `V` | `TypestateFact` | the automaton state that object is in |
| [Zero fact](glossary.md#zero-fact) | an artificial local `<<zero>>` | always reachable; new facts are generated out of it |
| [Flow functions](glossary.md#flow-function) | four methods | *which* locals to track across a statement |
| [Edge functions](glossary.md#edge-function) | four methods | *how the state changes* along that same step |
| [Meet](glossary.md#meet-operator) lattice | `TOP` / states / `ERROR` / `BOTTOM` | how to combine values where paths join |

!!! tip "The one sentence to remember"
    Flow functions decide **which** facts survive a statement. Edge functions decide
    **what value** each surviving fact carries. Both are asked for every statement, and
    for the same four kinds of edge.

---

## Step 4 — Dependencies

The Heros binding lives in its own module.

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

It brings Heros itself in transitively. The two classes you will subclass or instantiate
are `DefaultJimpleIDETabulationProblem` (your problem description) and `JimpleIDESolver`
(the solver that runs it).

---

## Step 5 — The value type

`TypestateFact` is the `V` of the problem. Besides the automaton's own states it needs
three special values, and Heros gives each of them a specific job:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateFact.java:fact"
```

- `TOP` is the neutral element of the [meet](glossary.md#meet-operator). The solver starts
  every value at top and never stores it, so "top" and "no entry in the result map" are
  the same thing.
- `BOTTOM` is the absorbing element: two paths reach this point with the object in
  different states and the analysis cannot commit to either.
- `ERROR` is *ours*, not something Heros requires. It records that a call happened for
  which the automaton has no transition — that is the bug we are looking for.

---

## Step 6 — The problem class

Everything else is one class. It extends the SootUp template, which fixes the node type to
`Stmt` and the method type to `SootMethod` for you:

```java
public class TypestateProblem
    extends DefaultJimpleIDETabulationProblem<
        Value, TypestateFact, InterproceduralCFG<Stmt, SootMethod>> {
```

The template asks for six things: a zero value, the initial seeds, an all-top function, a
meet lattice, a flow function factory and an edge function factory. The next steps fill
them in one by one.

---

## Step 7 — The zero fact and the seeds

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:zero-and-seeds"
```

The **[zero fact](glossary.md#zero-fact)** is a fact that is reachable at every statement
by construction. It is not a variable of the program; it is a placeholder that means
"control flow gets here". It matters because IFDS and IDE can only ever *propagate* facts,
never invent them: a new fact has to be generated from a fact that already holds — and the
zero fact is the one that always holds.

The **[seeds](glossary.md#seed)** say where analysis begins: here, the zero fact at the
first statement of the entry method. `DefaultSeeds.make` is a Heros convenience for the
common "one fact, a set of start statements" case.

!!! warning "The zero fact does not carry top"
    The solver sets the value of the zero fact at a seed to the lattice's **bottom**
    element, not top. That is why the edge function that creates an object (Step 10) must
    be a *constant* function — it has to produce the initial state no matter what came in.
    Deriving the state from the incoming value would produce `BOTTOM` for every object.

---

## Step 8 — The meet lattice

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:lattice"
```

`meet` is applied wherever paths join. Two paths that agree keep their state; if one of
them says nothing (`TOP`) the other one wins; if one of them found a violation the
violation wins, because a bug on one path is still a bug; and if two paths genuinely
disagree — say one leaves the handle `OPEN` and the other `CLOSED` — the result is
`BOTTOM`, meaning "no single state describes this point".

`createAllTopFunction` returns the function that maps everything to `TOP`. Heros uses it
to initialise its jump function table, i.e. as the value of an edge nothing is known about
yet.

---

## Step 9 — Flow functions: which locals to track

Heros asks for four flow functions, one per kind of edge in the exploded supergraph:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:flow-factory"
```

### Normal flow — inside a method body

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:normal-flow"
```

`new C()` for a class we have a rule for is where a tracked object comes into existence:
`Gen` takes the zero fact and returns *both* it and the new fact, so the local becomes
tracked from here on. An assignment between locals adds the new name; anything else
overwrites the left-hand side and therefore kills whatever was tracked under that name:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/AliasFlowFunction.java:alias-flow"
```

### Call flow — entering a callee

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:call-flow"
```

Facts are renamed: the fact for an argument at the call site becomes the fact for the
corresponding parameter local in the callee. Nothing else is passed in, so the callee only
sees what it was actually given.

Calls *into the API itself* are deliberately not entered. `FileHandle.open()` has a body,
but analysing that body would tell us nothing — the protocol step is the *call*, and it is
modelled on the call-to-return edge instead. Returning `KillAll` here is how you say
"summarise this call, do not descend into it".

### Return flow — leaving a callee

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:return-flow"
```

The mirror image: parameters are mapped back onto the caller's arguments and the returned
value onto the assignment target; everything else dies with the callee's frame. Because
the *edge* function on this edge is the identity (Step 10), the state the object reached
inside the callee travels back out with it. This is the step that makes the analysis
interprocedural — remove it and `correctUsage` would never see its `write`.

### Call-to-return flow — flowing around a call

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:call-to-return-flow"
```

This edge exists for the facts that are *not* affected by the call. For an API call the
receiver has to travel along it, because this is where the protocol step is applied. For
any other call the arguments must **not** travel along it: they went into the callee and
come back through the return edge, and keeping a second, unchanged copy here would meet
with the updated one and lose the effect of the call.

---

## Step 10 — Edge functions: where the state actually changes

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateProblem.java:edge-factory"
```

Only two of the four ever do anything, and both of them have to be careful about *which*
fact they were asked about:

- `l = new C()` starts the automaton. The generated fact gets the constant function
  "initial state", regardless of the incoming value.
- `receiver.m()` on a tracked class is a protocol step, provided the fact being asked
  about really is the receiver. The same call also carries *other* tracked objects past
  it; for those, the edge function must be the identity, or their state would change too.
- The constructor is filtered out: `<init>` creates the object, it is not a step of the
  protocol.
- Call and return edges are the identity — they only rename facts.

---

## Step 11 — Writing an edge function

An [edge function](glossary.md#edge-function) has to support four operations:
`computeTarget` (apply it), `composeWith` (do this one, then that one), `meetWith`
(combine two of them) and `equalTo`. Heros composes edge functions over and over while it
builds method summaries, so the *representation* matters:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateEdgeFunction.java:edge-function-table"
```

Representing a function as "apply this list of events" would grow without bound inside a
loop and the solver would never reach a [fixed point](glossary.md#fixed-point). Because the
automaton is finite, every function from value to value can be written down as a **table**
instead: one slot per state, plus one slot for each special value. Composition and meet
then work slot by slot, `equalTo` is a table comparison, and only finitely many distinct
edge functions exist — so the solver terminates.

The two functions the analysis actually builds:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateEdgeFunction.java:edge-function-factories"
```

And the four operations, all of them entry-by-entry over the table:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateEdgeFunction.java:edge-function-ops"
```

!!! note "Composition order"
    `f.composeWith(g)` means *apply `f` first, then `g`* — not the mathematical
    `g ∘ f` reading of "compose with". Getting this backwards is the single most common
    mistake when writing an IDE problem, and it produces plausible-looking but wrong
    states rather than a crash.

---

## Step 12 — Building the ICFG and solving

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateAnalysisTest.java:setup"
```

`JimpleBasedInterproceduralCFG` is SootUp's implementation of the Heros
[ICFG](glossary.md#interprocedural-cfg) interface. Given a `View` and a list of entry
points it builds a [call graph](callgraphs.md) with class hierarchy analysis and stitches
the per-method CFGs together at call sites. The two `boolean`s switch on exceptional
control flow and reflective call resolution; both are off here.

Then it is three lines: construct the problem, hand it to `JimpleIDESolver`, call
`solve()`. Results are read per statement with `resultsAt`, which returns
`Map<D, V>` — here, a map from local to automaton state — with the zero fact and all
top values already filtered out.

---

## Step 13 — The result

For `correctUsage`, the analysis computes this (the values shown are the ones holding
*before* each statement executes):

```text
$stack1 = new FileHandle                                     {}
specialinvoke $stack1.<FileHandle: void <init>()>()          {$stack1=CLOSED}
l0 = $stack1                                                 {$stack1=CLOSED}
virtualinvoke l0.<FileHandle: void open()>()                 {$stack1=CLOSED, l0=CLOSED}
staticinvoke <Example: void writeGreeting(FileHandle)>(l0)   {$stack1=CLOSED, l0=OPEN}
virtualinvoke l0.<FileHandle: void close()>()                {$stack1=CLOSED, l0=OPEN}
return                                                       {$stack1=CLOSED, l0=CLOSED}
```

`l0` is `OPEN` across the call to `writeGreeting` and `CLOSED` again at the end — the
`write` inside the callee did not break anything. For `protocolViolation` the same run
ends with `l0=ERROR`: `CLOSED` has no `write` transition, and `ERROR` is sticky.

---

## Step 14 — Verifying the result

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/typestate/TypestateAnalysisTest.java:assertions"
```

CI runs these on every pull request, so a SootUp API change that breaks the analysis fails
the build before it can silently invalidate this page.

The third test is the honest one. `$stack1` and `l0` are the same object, yet `$stack1`
stays `CLOSED` even in `protocolViolation`: the analysis tracks *names*, not objects, and
it never learns that a call on `l0` also changes the state of `$stack1`. Fixing that needs
a [pointer analysis](qilin.md) to supply the alias set of each tracked object — which is
exactly what production typestate tools such as Boomerang do on top of an IDE solver.

---

!!! warning "Common pitfalls"
    - **The zero fact carries bottom, not top.** Generating edge functions must be
      constant. See Step 7.
    - **`composeWith` applies `this` first.** See Step 11.
    - **Constructors are not protocol steps.** `<init>` shows up as an ordinary call on
      the receiver and will trigger a transition unless you filter it out.
    - **An edge function is asked for every pair of facts**, not just the interesting one.
      Always check that the fact you were handed is the receiver before transitioning, and
      return `EdgeIdentity.v()` otherwise.
    - **API calls surface on the call-to-return edge.** If the ICFG has a body for the API
      method, the call edge fires too; return `KillAll` there unless you really want to
      analyse the API's implementation.
    - **`solve()` before `resultsAt()`.** `resultsAt` on an unsolved solver returns an
      empty map rather than failing.
    - **No aliasing.** Without a pointer analysis the results hold for the *local*, not
      for the object.

---

## What to try next

- [Write a Dataflow Analysis](write_analyses.md) — the intraprocedural framework and the
  IFDS/IDE background this page builds on.
- [Incorporate Pointer Analysis](qilin.md) — the missing ingredient for alias-aware
  typestate analysis.
- [Call Graphs](callgraphs.md) — the ICFG is only as good as the call graph underneath it.
