# Call Graphs: a Step-by-Step Tutorial

A **call graph** records which methods a program may call. Its nodes are methods. Each edge is one
call: a *caller*, a *callee*, and the invoke statement in the caller that makes the call
(`CallGraph.Call`). Most interprocedural analyses need a call graph, e.g. the
[taint analysis](taint-analysis-example.md) and the [typestate analysis](analysis-typestate.md).

The hard part is the **virtual call**. For `shape.area()` the compiler only knows the static type
`Shape`, so a call graph algorithm has to estimate which `area()` implementations can run there.
Algorithms differ in how precise that estimate is and how much it costs. This tutorial uses one small
program to show the difference, then walks through every configuration option.

All code on this page comes from tests in `sootup.examples/src/test/java/sootup/examples/callgraph/`.
It compiles, and it does what the comments say.

!!! info "Dependencies"

    CHA and RTA are in `sootup.callgraph`. Spark needs `sootup.spark`, and Qilin needs `sootup.qilin`.
    See [Installation](installation.md).

## Step 1: Load the program into a View

Every algorithm works on a [View](analysisinput.md). Add the program under analysis as an
**application** input location, and the Java runtime as a **library** input location:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:view"
```

The runtime is needed because every program calls into the JDK, even if only `Object.<init>()`.
Without it, those targets can't be resolved. By default, the algorithms record calls *into* library
methods but don't analyze library method bodies. [`callResolver`](#callresolver-which-methods-get-analyzed)
changes that.

## Step 2: Choose the entry points

A call graph starts at one or more entry methods and contains everything reachable from them. For an
application, the entry point is usually `main`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:entry"
```

You can also parse the signature from a string:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:entry-string"
```

Any method can be an entry point, e.g. every public method of a library, or the handler methods of a
framework. If you pass no entry points, CHA and RTA start from every `main(String[])` method of the
application.

## Step 3: Pick an algorithm

### The running example

```java
--8<-- "sootup.examples/src/test/resources/CallgraphComparison/source/Shape.java"
```

`Circle`, `Square`, `Triangle` and `Hexagon` implement `Shape`. Each `area()` returns a constant.

```java
--8<-- "sootup.examples/src/test/resources/CallgraphComparison/source/Main.java"
```

At run time, call site 1 (line 9) calls `Circle.area()` and call site 2 (line 13) calls
`Square.area()`. Nothing else is ever called. This is what each algorithm computes:

| Algorithm | Call site 1 `circle.area()` | Call site 2 `square.area()` | Reasoning |
|---|---|---|---|
| **CHA** | Circle, Square, Triangle, Hexagon | Circle, Square, Triangle, Hexagon | Every class that implements `Shape` |
| **RTA** | Circle, Square, Triangle | Circle, Square, Triangle | Only classes that the program instantiates with `new` somewhere. `Hexagon` is gone |
| **Spark**, **Qilin** `insensitive()` | Circle | Square, Triangle | Only objects that can actually *flow* into the variable. Both `new Triangle()` and `new Square()` pass through `id()`, which is analyzed once, so its return value may be either |
| **Qilin** `callSite(1)` | Circle | Square | `id()` is analyzed separately for each call site, so `id(new Square())` returns only a `Square`. Exact |

From top to bottom, the algorithms get more expensive and more precise. Every algorithm
**over-approximates**: it may report calls that never happen, but it doesn't miss calls the program
makes (within the [limits of static analysis](#limits-reflection-invokedynamic-and-static-initializers)).
Every extra edge makes the analyses built on top of the call graph slower and less precise. For
example, a taint analysis on the CHA graph would also track flows through `Hexagon.area()`.

`CallGraphComparisonTest` asserts exactly this table.

!!! note "More context is not automatically more precision"

    Which kind of context helps depends on the code. `id()` is static, so it has no receiver object.
    An *object-sensitive* analysis (`ContextSensitivity.objectSensitive(2, 1)`) therefore has no
    context to tell the two `id(...)` calls apart, and reports Square and Triangle at call site 2,
    just like the context-insensitive analysis. A *call-site-sensitive* analysis distinguishes the
    two calls by their call sites. For code that routes objects through instance methods (getters,
    containers, builders), object sensitivity is usually the better choice.

!!! tip "Which one should I use?"

    - **CHA**: start here. Fast and simple, and it needs no knowledge about objects. Also the base
      graph for Spark.
    - **RTA**: almost as fast as CHA, and usually noticeably smaller. A good default for whole-program
      analyses.
    - **Spark**: context-insensitive points-to analysis. Use it when you also need points-to sets, or
      when RTA is too imprecise.
    - **Qilin**: the full range of context-sensitive points-to analyses. Use it when precision matters
      more than run time.

Each family has its own page with all of its options:

- [SootUp CHA & RTA](callgraph-cha-rta.md)
- [Spark](callgraph-spark.md)
- [Qilin](qilin.md)

## Step 4: Build the call graph

`CallGraphConfig.builder()` gives every algorithm the same shape. Set the shared properties, choose the
family, set the family's own options (if any), then call `build()` and `computeCallGraph()`:

=== "CHA"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:cha"
    ```

=== "RTA"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:rta"
    ```

=== "Spark"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:spark"
    ```

=== "Qilin (context-insensitive)"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:qilin-insensitive"
    ```

=== "Qilin (1-call-site)"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:qilin-1cfa"
    ```

`.cha()` and `.rta()` are built in. Spark and Qilin are in their own modules, so you enter them with
`.into(SparkCallGraphConfig::from)` and `.into(QilinCallGraphConfig::from)`.

You can also create the algorithm objects yourself, see
[SootUp CHA & RTA](callgraph-cha-rta.md#using-the-algorithm-objects-directly).

## Step 5: Shared options

These options go on `CallGraphConfig.builder()` before you choose the family. The examples use CHA.
The other families support the same options, with the exceptions listed in
[Which family uses which option](#which-family-uses-which-option).

### `callResolver`: which methods get analyzed

A `CallResolver` is asked once for every method the algorithm reaches, *before* the method's body is
loaded. It returns a `CallFilter` that decides which calls in the method get resolved:

- `CallFilter.all()`: every call.
- `CallFilter.none()`: no call. The body is not even loaded. The method stays in the call graph as a
  node, but has no outgoing edges.
- `CallFilter.of(stmt -> ...)`: only the calls in statements the predicate accepts.

The default is `DefaultCallResolver`. It returns `none()` for library methods, so the JDK is not
analyzed:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:call-resolver-default"
```

To analyze library methods too (this can make the graph, and the run time, much bigger):

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:call-resolver-all"
```

To find out which methods were reached but not analyzed, e.g. when you're looking for an expected
method that is missing:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:call-resolver-collecting"
```

Your own resolver can cut off a class or package, and fall back to the default for everything else:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:call-resolver-custom"
```

Or decide per statement:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:call-resolver-per-stmt"
```

!!! info "Excluding a method only removes its outgoing edges"

    A `CallResolver` only decides whether a method's *own* calls get resolved. Calls *into* the method
    from other methods stay in the call graph.

### `virtualCallResolver`: which resolved targets to keep

A `VirtualCallResolver` is asked *after* a call has been resolved, once per possible target. That
includes the static initializers (`<clinit>`) that the call triggers. It returns an
`ExplorationVerdict`:

| Verdict | Edge added? | Callee analyzed? |
|---|---|---|
| `EXPLORE_METHOD` (default) | yes | yes |
| `STOP_AFTER_CALL` | yes | not via this edge |
| `STOP` | no | no |

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:virtual-call-resolver-custom"
```

The default is `VirtualCallResolver.all()`. `SuppressClinitCallResolver` and
`AppOnlyClinitCallResolver` handle static initializers, see the next section. Both take an optional
second resolver, which decides about all other targets:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:clinit-chaining"
```

### `seedEntryPointClinits`: static initializers

The JVM runs a class's static initializer (`<clinit>`) before the class is first used. The second
example program has two of them:

```java
--8<-- "sootup.examples/src/test/resources/CallgraphFeatures/source/App.java"
```

```java
--8<-- "sootup.examples/src/test/resources/CallgraphFeatures/source/Registry.java"
```

`Registry.register()` triggers `Registry.<clinit>`, and the algorithm finds this call while it walks
through `main`. `App.<clinit>` is different: the JVM runs it before `main`, so no statement in the
program calls it. `seedEntryPointClinits(true)` adds the `<clinit>` of each entry point's class as an
extra root.

Together, the seed flag and the `virtualCallResolver` reproduce the four classic `<clinit>` modes of
Soot and Qilin:

| Mode | `seedEntryPointClinits` | `virtualCallResolver` | `App.<clinit>` | `Registry.<clinit>` | JDK `<clinit>`s (e.g. `Class.<clinit>`) |
|---|---|---|---|---|---|
| FULL | `true` | `new SuppressClinitCallResolver(view)` | yes | – | – |
| ON_THE_FLY | `false` | `VirtualCallResolver.all()` | – | yes | yes |
| APP | `false` | `new AppOnlyClinitCallResolver(view)` | – | yes | – |
| NONE | `false` | `new SuppressClinitCallResolver(view)` | – | – | – |

CHA, RTA and Spark default to `true` + `all()`, so they include all of them. Qilin defaults to
ON_THE_FLY.

=== "Default (CHA/RTA)"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:clinit-default"
    ```

=== "No seeding"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:clinit-no-seed"
    ```

=== "FULL"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:clinit-suppress"
    ```

=== "APP"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:clinit-app-only"
    ```

### `reflectionModel`: reflection

In general, static analysis can't know which class `Class.forName("MyPlugin")` loads. With the default,
`ReflectionModel.none()`, reflective calls are treated as ordinary library calls, so in the example
above `MyPlugin.<init>()` is never reached:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:reflection-none"
```

[TamiFlex](https://github.com/secure-software-engineering/tamiflex) records the reflective calls of a
real run in a log. `TamiflexReflectionModel` rewrites each logged call into plain Jimple (here:
`new MyPlugin` and the constructor call) before the call graph algorithm sees the body:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:reflection-tamiflex"
```

`ReflectionModel` is a functional interface, so you can wrap a model or write your own. A model must
return the same body every time it is asked about the same method: CHA, RTA, Spark and Qilin may ask
more than once. To model more reflective APIs, extend `AbstractReflectionModel`. It finds the
reflective calls, caches the rewritten bodies, and calls one hook per API (`transformClassForName`,
`transformMethodInvoke`, ...).

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:reflection-custom"
```

### `dynamicInvokeResolver`: lambdas and method references

Lambdas and method references compile to `invokedynamic`. The default resolver,
`DynamicInvokeResolver.bootstrapMethodHandles()`, reads the target method from the bootstrap
arguments. A lambda body (`lambda$main$0`) is reached through the functional object: in the example,
`task.run()` calls it.

=== "Default"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:indy-default"
    ```

=== "Off"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:indy-none"
    ```

=== "Creation-site edges"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:indy-creation-site"
    ```

`withCreationSiteEdges()` also adds an edge from the `invokedynamic` statement to the lambda body.
This helps when the view doesn't contain the code that calls the lambda, e.g. a lambda that is passed
to a library you don't analyze.

### Which family uses which option

| Option | CHA | RTA | Spark (CHA-seeded) | Spark (on-the-fly) | Qilin |
|---|---|---|---|---|---|
| `callResolver` | ✓ | ✓ | ✓ (through CHA) | – | – |
| `virtualCallResolver` | ✓ | ✓ | ✓ (through CHA) | – | `<clinit>` calls only |
| `seedEntryPointClinits` | ✓ (default `true`) | ✓ (default `true`) | ✓ (through CHA) | – | ✓ (default `false`) |
| `reflectionModel` | ✓ | ✓ | ✓ | ✓ | ✓ (overrides `reflectionLogPath`) |
| `dynamicInvokeResolver` | ✓ | ✓ | ✓ | ✓ | ✓ |

## Step 6: Query the call graph

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:query"
```

The `area()` targets per call site in Step 3 come from this helper:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:query"
```

To compare two call graphs, use `diff`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:diff"
```

`CallGraphDifference` also has `uniqueOtherGraphMethods()`, `intersectedMethods()`, and the
corresponding methods for calls.

## Step 7: Export to DOT

`exportAsDot()` writes the graph in [Graphviz](https://graphviz.org/) format. The nodes are method
signatures, and the edge labels are the line numbers of the calls:

=== "Unsorted"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:dot"
    ```

=== "Sorted (stable output)"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:dot-sorted"
    ```

Render it with `dot -Tsvg callgraph.dot -o callgraph.svg`.

## Limits: reflection, invokedynamic and static initializers

A call graph is only as complete as its model of the program. Calls that don't appear in the bytecode
need extra help:

- Reflection: [`reflectionModel`](#reflectionmodel-reflection).
- Lambdas and method references: [`dynamicInvokeResolver`](#dynamicinvokeresolver-lambdas-and-method-references).
- Static initializers: [`seedEntryPointClinits`](#seedentrypointclinits-static-initializers).
- Native methods: Qilin models a fixed set of JDK natives. CHA, RTA and Spark treat them as calls
  without a body.
- Code that isn't in the view, e.g. callbacks from a framework: add the callback methods as entry
  points, or use RTA's [`preInstantiatedClasses`](callgraph-cha-rta.md#rta-preinstantiatedclasses).

## Coming from Soot

| Soot | SootUp |
|---|---|
| `CHATransformer.v().transform()` | `CallGraphConfig.builder()...cha()` |
| `cg.spark` with `rta:true` | `CallGraphConfig.builder()...rta()` |
| `cg.spark` with `on-fly-cg:true` | `.into(SparkCallGraphConfig::from)` with `onFlyCallGraph(true)` |
| `Scene.v().getCallGraph()` | the returned `CallGraph`. There is no global state |
| `cg.edgesOutOf(m)` | `cg.callsFrom(signature)` |
| `set_whole_program(true)`, `loadNecessaryClasses()` | not needed: the `View` loads classes on demand |

See also [Migration Help](migrating.md).
