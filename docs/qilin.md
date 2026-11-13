# Qilin Pointer Analysis and Call Graphs

[Qilin](https://github.com/QilinPTA/Qilin) is a framework of context-sensitive points-to analyses. Like
[Spark](callgraph-spark.md), it builds the call graph on the fly together with the points-to sets. With
context sensitivity, the call graph can be more precise than Spark's, see the
[comparison in the call graph tutorial](callgraphs.md#the-running-example). For the options that all
call graph algorithms share, read the [tutorial](callgraphs.md) first.

### Dependencies

=== "Maven"
    ```maven
    <dependency>
        <groupId>org.soot-oss</groupId>
        <artifactId>sootup.qilin</artifactId>
        <version>{{ git_latest_release }}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    compile "org.soot-oss:sootup.qilin:{{ git_latest_release }}"
    ```

### How to create a pointer analysis

For the core context-sensitivity variants (insensitive, call-site, object, type, and their
hybrid variants), use the type-safe `PointerAnalysisFactory` - no singletons, no string patterns,
and independent `View`s (or repeated calls against the same `View`) can be analyzed concurrently
since nothing is cached on `View` or held in JVM-global state:

=== "Java"

    ```Java
    ClassType entrypoint = view.getIdentifierFactory().getClassType("dacapo.antlr.Main");
    PointerAnalysisConfig config = PointerAnalysisConfig.builder()
        .contextSensitivity(ContextSensitivity.objectSensitive(2, 1)) // 2-object-sensitive, 1-level heap ctx
        .build();
    PTA pta = PointerAnalysisFactory.create(view, entrypoint, config);
    pta.run();
    ```

Other `ContextSensitivity` factory methods: `insensitive()`, `callSite(k[, hk])`,
`objectSensitive(k[, hk])`, `typeSensitive(k, hk)`, `hybridObjectSensitive(k, hk)`,
`hybridTypeSensitive(k, hk)`.

The research-toolkit variants (bean, zipper, eagle, turner, mahjong, selectx, data-driven,
tunneling, context debloating) go through the same `PointerAnalysisFactory`/`ContextSensitivity`
pair - see the table below for their factory methods.

### Resolving reflection, native methods, and invokedynamic

Some call targets can't be derived from a method's own bytecode. Qilin resolves these through a
shared `qilin.core.effect.MethodEffectModel` hook, applied once per method just before its body is
turned into constraints:

- **Reflection** (`Class.forName`, `Method.invoke`, ...) - off by default; set
  `PointerAnalysisConfig.builder().reflectionLogPath(path)` to a Tamiflex trace log to resolve it.
- **Native methods** - a fixed table of simulated JDK natives (`Object.clone`, `Thread.start0`,
  ...); unrecognized natives are left unmodeled.
- **invokedynamic (lambdas/method references)** - on by default
  (`PointerAnalysisConfig.isResolveDynamicInvoke()`). Resolves the common case - a lambda body or
  method reference whose target is a plain static method - directly from the bootstrap's
  `MethodHandle` constant, no log needed. Captured (closure) lambdas, constructor references
  (`Foo::new`), and unbound instance method references are not yet resolved.

### How to use pointer analysis results

First, we can use Qilin's pointer analysis to get a On-the-Fly constructed callgraph:

```java
OnFlyCallGraph cg = pta.getCallGraph();
```

Second, we can use it to get the points-to results for some interested local variables, fields, etc.

```java
PointsToSet pts0 = pta.reachingObjects(method, v0);
PointsToSet pts1 = pta.reachingObjects(method, v1, f); // PTS(v1.f)
```

Third, we can check whether two variables, `a` and `b`, are aliases by checking
whether there is an object that exists in both of their points-to sets:

```java
boolean mayAlias = pta.isMayAlias(method, a, b);
```

`isMayAlias` also special-cases `null`/String/class constants (e.g. two `null` constants always
may-alias, a `null` never may-aliases a non-null value). It accepts `Local` references plus those
constants; passing any other `Value` kind throws `IllegalArgumentException`.

### A Full list of Pointer Analyses

[Qilin](https://github.com/QilinPTA/Qilin)'s toolbox includes a rich set of pointer analyses, which are given below:

Note that **k** used below is a concrete small constant like 1 or 2, and `hk` the heap-context
depth (defaults vary by variant - see `ContextSensitivity`'s Javadoc for each factory method).

| `ContextSensitivity` factory method                | Description                                                   | Reference                                                                 |
|------------------------------------------------------|-----------------------------------------------------------------|----------------------------------------------------------------------------|
| `insensitive()`                                       | Andersen's context-insensitive analysis                         | [Paper](https://link.springer.com/chapter/10.1007/3-540-36579-6_12)       |
| `callSite(k[, hk])`                                   | **k**-callsite-sensitive pointer analysis (denoted **k**CFA).   | [Paper](https://www.cse.psu.edu/~trj1/cse598-f11/docs/sharir_pnueli1.pdf) |
| `objectSensitive(k[, hk])`                            | **k**-object-sensitive pointer analysis (denoted **k**OBJ).     | [Paper](https://dl.acm.org/doi/abs/10.1145/1044834.1044835)               |
| `typeSensitive(k, hk)`                                | **k**-type-sensitive pointer analysis (denoted **k**TYPE).      | [Paper](https://dl.acm.org/doi/abs/10.1145/1926385.1926390)               |
| `hybridObjectSensitive(k, hk)`                        | hybrid **k**-object-sensitive pointer analysis.                 | [Paper](https://dl.acm.org/doi/10.1145/2499370.2462191)                   |
| `hybridTypeSensitive(k, hk)`                          | hybrid **k**-type-sensitive pointer analysis.                   | [Paper](https://dl.acm.org/doi/10.1145/2499370.2462191)                   |
| `beanObjectSensitive()`                               | BEAN-guided 2OBJ. Only k=2 is supported.                        | [Paper](https://link.springer.com/chapter/10.1007/978-3-662-53413-7_24)   |
| `dataDrivenObjectSensitive()`                         | Data-driven 2OBJ. Only k=2 is supported.                        | [Paper](https://dl.acm.org/doi/10.1145/3133924)                           |
| `dataDrivenCallSite()`                                | Data-driven 2CFA. Only k=2 is supported.                        | [Paper](https://dl.acm.org/doi/10.1145/3133924)                           |
| `dataDrivenHybridObjectSensitive()`                   | Data-driven hybrid-2OBJ. Only k=2 is supported.                 | [Paper](https://dl.acm.org/doi/10.1145/3133924)                           |
| `mahjongObjectSensitive(k, hk)`                       | MAHJONG-guided **k**OBJ.                                        | [Paper](https://dl.acm.org/doi/10.1145/3062341.3062360)                   |
| `mahjongCallSite(k, hk)`                              | MAHJONG-guided **k**CFA.                                        | [Paper](https://dl.acm.org/doi/10.1145/3062341.3062360)                   |
| `eagleObjectSensitive(k)`                             | EAGLE-guided **k**OBJ.                                          | [Paper](https://dl.acm.org/doi/10.1145/3360574)                           |
| `turnerObjectSensitive(k)`                            | TURNER-guided **k**OBJ.                                         | [Paper](https://drops.dagstuhl.de/opus/volltexte/2021/14059/)             |
| `zipperObjectSensitive(k, hk)`                        | ZIPPER-guided **k**OBJ.                                         | [Paper](https://dl.acm.org/doi/10.1145/3276511)                           |
| `zipperCallSite(k, hk)`                               | ZIPPER-guided **k**CFA.                                         | [Paper](https://dl.acm.org/doi/10.1145/3276511)                           |
| `tunnelingObjectSensitive/CallSite/TypeSensitive/HybridObjectSensitive(k, hk)` | Tunneling context sensitivity, per underlying variant. |                                                                             |
| `selectxCallSite(k)`                                  | SELECTX-guided **k**CFA.                                        | [Paper](https://doi.org/10.1007/978-3-030-88806-0_13)                     |

Context debloating is a config toggle layered on top of an object-sensitive variant (the default
k-obj, or `zipperObjectSensitive`/`mahjongObjectSensitive`/`eagleObjectSensitive`), not a separate
factory method: set `PointerAnalysisConfig.builder().ctxDebloating(true)` and pick a
`debloatApproach` (`CONCH`, `DEBLOATERX`, `COLLECTION` for the Zipper-cd algorithm, or `MOON`).
[Debloating paper](https://doi.org/10.1109/ASE51524.2021.9678880),
[DebloaterX paper](https://dl.acm.org/doi/10.1145/3622832).

## Qilin call graphs

### Building via `CallGraphConfig`

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/QilinCallGraphTest.java:via-config"
```

Qilin analyzes exactly one main class, so `mainClass(...)` is required. Unless you set
`singleEntry(true)`, Qilin also models the JVM's start-up code. That model references JDK 8 internals,
so with a JDK 9+ runtime in the view (e.g. `DefaultRuntimeAnalysisInputLocation`), set
`singleEntry(true)`.

Shared settings on `CallGraphConfig.builder()` take precedence over the matching
`PointerAnalysisConfig` settings:

| Shared option | `PointerAnalysisConfig` option it overrides |
|---|---|
| `seedEntryPointClinits` (default for Qilin: `false`) | `seedEntryPointClinits` |
| `virtualCallResolver` | `clinitVirtualCallResolver`. Qilin only consults it for `<clinit>` calls |
| `reflectionModel` (only if set) | `reflectionModel` / `reflectionLogPath` |
| `dynamicInvokeResolver` (only if set) | `dynamicInvokeResolver` |
| `callResolver` | not used by Qilin |

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/QilinCallGraphTest.java:common-override"
```

### Running the analysis directly

You get the same call graph from the `PTA` object, together with all points-to queries shown above:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/QilinCallGraphTest.java:direct"
```

### Every `PointerAnalysisConfig` option

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/QilinCallGraphTest.java:all-options"
```

| Option | Default | Effect |
|---|---|---|
| `contextSensitivity` | `insensitive()` | The analysis variant, see the [full list](#a-full-list-of-pointer-analyses) |
| `heapAbstractionPolicy` | `ALLOC_SITE` | `ALLOC_SITE`: one abstract object per `new` statement. `HEURISTIC_MERGE`: one object per type for `StringBuffer`, `StringBuilder` and all `Throwable`s |
| `singleEntry` | `false` | `true`: only the main method is an entry point. `false`: also model the JVM start-up (needs a JDK 8 runtime) |
| `seedEntryPointClinits` | `false` | Add the main class's `<clinit>` (and its superclasses') as roots. See [static initializers](callgraphs.md#seedentrypointclinits-static-initializers) |
| `clinitVirtualCallResolver` | `VirtualCallResolver.all()` | Decides which discovered `<clinit>` calls are modeled |
| `preciseArrayElement` | `false` | Give array-element nodes the array's element type instead of `Object`, so type filtering applies |
| `stringConstants` | `false` | `true`: one abstract object per string constant. `false`: one object for all string constants |
| `preciseExceptions` | `false` | Track thrown exceptions per method. `false`: all exceptions flow through one global node |
| `enforceEmptyContextForIgnoreTypes` | `false` | Analyze the methods of certain types (e.g. `StringBuilder`) without context |
| `reflectionLogPath` | `null` | A TamiFlex `refl.log` to resolve reflection with |
| `reflectionModel` | `null` | Any `ReflectionModel`. Takes precedence over `reflectionLogPath` |
| `resolveDynamicInvoke` | `true` | Resolve `invokedynamic` (lambdas, method references) |
| `dynamicInvokeResolver` | `bootstrapMethodHandles()` | How `invokedynamic` is resolved. See [invokedynamic](callgraphs.md#dynamicinvokeresolver-lambdas-and-method-references) |
| `preAnalysisOnly` | `false` | Guided and debloated analyses: only run the pre-analysis |
| `ctxDebloating` | `false` | Context debloating on top of an object-sensitive variant |
| `debloatApproach` | `CONCH` | `CONCH`, `DEBLOATERX`, `COLLECTION` (Zipper-cd) or `MOON` |
| `turnerConfig` | `DEFAULT` | For `turnerObjectSensitive`: `DEFAULT`, or only `PHASE_ONE` / `PHASE_TWO` of Turner's pre-analysis |
| `analysisName` | `null` | Name used for the statistics and output files |
| `outputDirectory` | `""` | Where the dumps and statistics are written |
| `dumpJimple` | `false` | *Accepted, but currently has no effect* |
| `dumpPointsToSet` | `false` | Write the points-to sets of the application's locals |
| `dumpLibraryPointsToSet` | `false` | Also write the points-to sets of library locals |
| `dumpStats` | `false` | Write the statistics. Implies `evaluate` |
| `evaluate` | `true` | Compute and print statistics after the analysis. Turn it off when you only need the results: the alias statistics are quadratic in the number of locals |

Two examples, merged heap objects and context debloating:

=== "Heap abstraction"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/QilinCallGraphTest.java:heap-merge"
    ```

=== "Context debloating"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/QilinCallGraphTest.java:debloating"
    ```

Next: [Spark](callgraph-spark.md) · [CHA & RTA](callgraph-cha-rta.md) · back to the [tutorial](callgraphs.md)
