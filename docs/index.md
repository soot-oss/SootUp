# What is SootUp?

Have you ever wondered how a linter catches a bug before you run your code?
How a security scanner finds a vulnerability in a compiled library you don't have source for?
How an IDE tells you that a variable might be null three call-levels deep?

The answer is **static analysis** — reasoning about a program's behaviour by reading its
structure, without executing it. SootUp is a Java library that gives you the building
blocks to write such analyses yourself.

You point SootUp at a Java `.jar`, Android `.apk`, or source tree, and it gives you back
a clean, structured, traversable representation of every class, method, and instruction
inside it. You then walk that representation and compute whatever you need to know.

## What SootUp provides

| Capability | What it means for you |
|---|---|
| **Jimple IR** | Bytecode translated into a simple, flat, human-readable form that is easy to traverse in Java code |
| **Class Hierarchy** | Answers "which classes implement this interface?" or "what is the supertype chain?" |
| **Call Graph** | Answers "which methods can be called at this call site?" — essential for inter-procedural analysis |
| **Dataflow framework (IFDS/IDE)** | A principled engine for tracking facts (e.g. tainted values, null pointers) across method boundaries |
| **Body Interceptors** | Optional pre-processing passes that simplify the IR before you analyse it (e.g. constant folding, SSA conversion) |
| **Jimple serialization** | Write the IR back to `.jimple` files for debugging or round-trip processing |

!!! tip "New to static analysis?"
    Before diving into the API, read [Core Concepts](concepts.md) — it explains what a
    Control Flow Graph is, what an IR is, and what "analysis" actually means in concrete
    terms. Five minutes there will make everything else click faster.

## Quick orientation

```
your code (.jar / .apk / .java)
        ↓
  AnalysisInputLocation   ← tells SootUp where to look
        ↓
       View               ← your in-memory handle to the loaded program
        ↓
  SootClass → SootMethod → Body → ControlFlowGraph
                                        ↓
                                 your analysis logic
```

Start with [Getting Started](getting-started.md) to see this in code within minutes.

## Publications & Citations

[The SootUp paper](https://doi.org/10.1007/978-3-031-57246-3_13) describes the design
decisions in detail.
[Works citing the paper](https://scholar.google.de/scholar?cites=7580240720760424326&as_sdt=2005&sciodt=0,5&hl=de)
show how the community is using SootUp.

## Supporters

The development of SootUp is financed by generous support from the German Research Foundation (DFG) and
the Heinz Nixdorf Institute (HNI).

<table>
<tr>
<td><img src="https://soot-oss.github.io/soot/images/dfg_logo_englisch_blau_en.jpg" width="250" > </td>
<td> </td>
<td><img src="https://soot-oss.github.io/soot/images/Heinz_Nixdorf_Institut_Logo_CMYK.jpg" width="250" ></td>
</tr>
</table>

[Become a sponsor!](https://github.com/sponsors/soot-oss)

---

!!! important "Coming from Soot?"
    SootUp is *not a version update* to Soot — it is a completely new implementation
    written from scratch. It is not a drop-in replacement. See [What's New](whatsnew.md)
    for the design changes and [Migrating from Soot](migrating.md) for a side-by-side
    API comparison.
