# Incorporate Qilin Pointer Analysis

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
    String entrypoint = "dacapo.antlr.Main";
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
tunneling, context debloating) are not yet migrated to this factory - they're still reached
through the legacy `PTAPattern`/`PTAFactory` string-pattern dispatch, see the table below.

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
whether there is an object that exists in both of their points-to sets.

Qilin does not currently offer a `isMayAlias` API within the PTA class.
However, a similar functionality can be found in `qilin.test.util.AliasAssertion` with the method:
```boolean isMayAlias(PTA pta, Value va, Value vb)```
This method allows to check for potential aliasing between two values given a PTA instance.

### A Full list of Pointer Analyses

[Qilin](https://github.com/QilinPTA/Qilin)'s toolbox includes a rich set of pointer analyses, which are given below:

Note that the symbol **k** used in the table should be replaced with a concrete small constant like 1 or 2.

| PTA patterns               | Description                                                   | Reference                                                                 |
|----------------------------|---------------------------------------------------------------|---------------------------------------------------------------------------|
| **insens**                 | Andersen's context-insensitive analysis                       | [Paper](https://link.springer.com/chapter/10.1007/3-540-36579-6_12)       |
| **k**c                     | **k**-callsite-sensitive pointer analysis (denoted **k**CFA). | [Paper](https://www.cse.psu.edu/~trj1/cse598-f11/docs/sharir_pnueli1.pdf) |
| **k**o                     | **k**-object-sensitive pointer analysis (denoted **k**OBJ).   | [Paper](https://dl.acm.org/doi/abs/10.1145/1044834.1044835)               |
| **k**t                     | **k**-type-sensitive pointer analysis (denoted **k**TYPE).    | [Paper](https://dl.acm.org/doi/abs/10.1145/1926385.1926390)               |
| **k**h                     | hybrid **k**-object-sensitive pointer analysis.               | [Paper](https://dl.acm.org/doi/10.1145/2499370.2462191)                   |
| **k**ht                    | hybrid **k**-type-sensitive pointer analysis.                 | [Paper](https://dl.acm.org/doi/10.1145/2499370.2462191)                   |
| B-2o                       | BEAN-guided 2OBJ. Only k=2 is supported.                      | [Paper](https://link.springer.com/chapter/10.1007/978-3-662-53413-7_24)   |
| D-2o                       | Data-driven 2OBJ. Only k=2 is supported.                      | [Paper](https://dl.acm.org/doi/10.1145/3133924)                           |
| D-2c                       | Data-driven 2CFA. Only k=2 is supported.                      | [Paper](https://dl.acm.org/doi/10.1145/3133924)                           |
| M-**k**o                   | MAHJONG-guided **k**OBJ.                                      | [Paper](https://dl.acm.org/doi/10.1145/3062341.3062360)                   |
| M-**k**c                   | MAHJONG-guided **k**CFA.                                      | [Paper](https://dl.acm.org/doi/10.1145/3062341.3062360)                   |
| E-**k**o                   | EAGLE-guided **k**OBJ.                                        | [Paper](https://dl.acm.org/doi/10.1145/3360574)                           |
| T-**k**o                   | TURNER-guided **k**OBJ.                                       | [Paper](https://drops.dagstuhl.de/opus/volltexte/2021/14059/)             |
| Z-**k**o                   | ZIPPER-guided **k**OBJ.                                       | [Paper](https://dl.acm.org/doi/10.1145/3276511)                           |
| Z-**k**c                   | ZIPPER-guided **k**CFA.                                       | [Paper](https://dl.acm.org/doi/10.1145/3276511)                           |
| Z-**k**o -cd               | The context debloated version of ZIPPER-guided **k**OBJ.      | [Paper](https://doi.org/10.1109/ASE51524.2021.9678880)                    |
| **k**o -cd -cda=CONCH      | The context debloated version of **k**OBJ using Conch.        | [Paper](https://doi.org/10.1109/ASE51524.2021.9678880)                    |
| **k**o -cd -cda=DEBLOATERX | The context debloated version of **k**OBJ using DebloaterX.   | [Paper](https://dl.acm.org/doi/10.1145/3622832)                           |
| s-**k**c                   | SELECTX-guided **k**CFA.                                      | [Paper](https://doi.org/10.1007/978-3-030-88806-0_13)                     |

## Qilin Pointer Analysis

Qilin builds a call graph on the fly with the pointer analysis.
For a core variant, prefer `PointerAnalysisFactory` (see above). To reach a toolkit variant
(anything in the table above beyond plain **k**c/**k**o/**k**t/**k**h/**k**ht), use the legacy
string-pattern factory:

=== "SootUp"

    ```java
    String MAINCLASS = "dacapo.antlr.Main"; // just an example
    PTAPattern ptaPattern = new PTAPattern("Z-2o"); // ZIPPER-guided 2OBJ, e.g.
    PointerAnalysisConfig config = PointerAnalysisConfig.builder().build();
    PTA pta = PTAFactory.createPTA(ptaPattern, view, MAINCLASS, config);
    pta.run();
    OnFlyCallGraph cg = pta.getCallGraph();
    ```
