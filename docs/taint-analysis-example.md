# Taint Analysis Tutorial: Step-by-Step Guide

This tutorial guides you through implementing a taint analysis with SootUp's IFDS framework.
You will learn how to track sensitive data from *sources* to *sinks* in Java programs.

All code snippets on this page are taken from
[TaintAnalysisTest.java](https://github.com/soot-oss/SootUp/blob/develop/sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java),
which is executed as part of SootUp's test suite - so the code you see here is guaranteed to compile and work.

## What You'll Learn

- How to define sources, sinks and flow functions
- How to handle interprocedural taint propagation
- How to run the IFDS solver and detect information leaks

## Prerequisites

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>org.soot-oss</groupId>
        <artifactId>sootup.java.bytecode.frontend</artifactId>
        <version>{{ git_latest_release }}</version>
    </dependency>
    <dependency>
        <groupId>org.soot-oss</groupId>
        <artifactId>sootup.analysis.interprocedural</artifactId>
        <version>{{ git_latest_release }}</version>
    </dependency>
    <dependency>
        <groupId>de.upb.cs.swt</groupId>
        <artifactId>heros</artifactId>
        <version>1.2.3</version>
    </dependency>
</dependencies>
```

## Step 1: Understanding the Problem

Taint analysis tracks the flow of sensitive information through a program. It identifies:

- **Sources**: where sensitive data originates (e.g. user input, secrets)
- **Sinks**: where data might be leaked (e.g. network calls, logs)
- **Flow**: how data propagates through assignments and method calls

In this tutorial, the source is the String constant `"SECRET"` and every call to a method named `sink` is a sink.

### Example Scenario

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:basic-taint"
```

In this example:

- **Source**: `String a = "SECRET"`
- **Flow**: `a` → `b` → `c` (the static field `c` becomes tainted)
- **Sink**: `sc.sink(c)` - the secret leaks

The sink itself is an ordinary method:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:sink-class"
```

## Step 2: The IFDS Problem

IFDS (Interprocedural, Finite, Distributive, Subset) problems are solved by propagating *facts* along the
interprocedural control flow graph (ICFG). For a taint analysis, a fact is simply a tainted `Value`
(a local variable or a static field).

### 2.1 The Problem Class

The analysis logic extends `DefaultJimpleIFDSTabulationProblem`. It keeps the entry method, i.e. the method where
the analysis starts:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:problem-class"
```

### 2.2 Initial Seeds

The seeds tell the solver where to start: the first statement of the entry method, holding only the zero value.

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:initial-seeds"
```

### 2.3 Zero Value

The zero value (Λ) is a special fact which always holds. New facts are generated *from* it, e.g. when a
source is encountered.

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:zero-value"
```

### 2.4 Flow Functions Factory

The solver asks the problem for a flow function for each edge in the ICFG. There are four kinds of edges:

![IFDS Flow Functions](./assets/figures/flow-functions.png)

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:flow-functions-factory"
```

- **normal flow**: a statement inside a method, e.g. an assignment
- **call flow**: from a call site into the called method
- **return flow**: from the exit of the called method back to the caller
- **call-to-return flow**: facts that bypass the called method at the call site

We will implement each of them in the next step.

## Step 3: Implementing the Flow Functions

A flow function maps one incoming fact to the set of facts that hold afterwards.
Heros provides some common ones, e.g. `Identity` (keep all facts) or `Gen` (additionally generate a new fact).

### 3.1 Sources

A helper which decides whether a value is a source:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:is-source"
```

### 3.2 Normal Flow Function

Handles statements within a method. Only assignments change the set of tainted values:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:normal-flow"
```

1. **Generate**: `x = "SECRET"` taints `x`.
2. **Kill**: any other assignment `x = ...` removes the taint of `x` - this is how sanitization works.
3. **Propagate**: `x = y` taints `x` if `y` is tainted.

### 3.3 Call Flow Function

Maps facts of the caller into the callee when a method is called:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:call-flow"
```

A tainted argument taints the corresponding parameter of the callee. Local variables of the caller are not visible
in the callee, so all other facts are dropped - except static fields, which are global.

### 3.4 Return Flow Function

Maps facts of the callee back to the caller when the called method returns:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:return-flow"
```

- A tainted return value taints the variable that receives it at the call site.
- `return "SECRET"` is a source as well.
- Static fields keep their taint.

### 3.5 Call-to-Return Flow Function

Handles facts of the caller that are not affected by the call:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:call-to-return-flow"
```

All local facts survive the call, except the taint of the variable that is overwritten by the return value.

## Step 4: Running the Analysis

### 4.1 Loading the Program

Create a `JavaView` for the classes under analysis. We pass an empty list of `BodyInterceptor`s so that the
Jimple code stays close to the bytecode (e.g. no constant propagation that would inline `"SECRET"` into the sink call):

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:create-view"
```

Then look up the method the analysis starts from:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:find-entry-method"
```

### 4.2 Solving the IFDS Problem

Build the ICFG starting from the entry method, create the problem and let the `JimpleIFDSSolver` compute
all facts:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:run-analysis"
```

### 4.3 Detecting Leaks

After solving, `solver.ifdsResultsAt(stmt)` returns all facts that hold at a statement.
A leak exists if an argument of a `sink(..)` call is tainted:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:leaks-to-sink"
```

## Step 5: Testing Different Scenarios

### 5.1 Sanitization

`b` is overwritten with a harmless value before it reaches the sink, so the normal flow function kills its taint:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:basic-taint-sanitized"
```

### 5.2 Interprocedural Propagation

The taint enters `id` via the call flow function (`a` → `s`) and comes back via the return flow function (`s` → `b`):

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:function-propagates-taint"
```

### 5.3 Source in a Return Value

The secret is created by `return "SECRET"` inside `source()`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:function-returns-taint"
```

### 5.4 The Tests

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/taintAnalysis/TaintAnalysisTest.java:tests"
```

## Step 6: Extending the Analysis

This analysis is intentionally minimal. Some ideas to extend it:

- **Custom sources and sinks**: recognize method calls like `getUserInput()` as sources in the call-to-return flow
  function, and methods like `sendToServer(..)` as sinks.
- **Sanitizers**: treat calls like `sanitize(x)` as a kill of the receiver's taint.
- **Field sensitivity**: track instance fields (`JInstanceFieldRef`) in addition to locals and static fields.
- **Aliasing**: combine the analysis with a [pointer analysis](qilin.md) to handle taints via aliased objects.
