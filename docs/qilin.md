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

!!! note "WIP: Beware most likely the API will change so you only need to specify SootUp objects!"

One can create an Andersen's context-insensitive analysis with following code:

=== "Java"

    ```Java
    String entrypoint = "dacapo.antlr.Main";
    PTAPattern ptaPattern = new PTAPattern("insens");
    PTA pta = PTAFactory.createPTA(ptaPattern, view, entrypoint);
    pta.run();
    ```

Users must specify the program's `View`, select a `PTAPattern`
(indicating the desired types of pointer analyses to perform),
and designate the `entrypoint` - which is serving as the entry point for the analysis.

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

