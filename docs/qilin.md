# Incorporate Qilin Pointer Analysis
see [QilinPTA](https://github.com/QilinPTA/Qilin).

### Dependencies
=== "Maven"
    ```maven
        <dependency>
            <groupId>org.soot-oss</groupId>
            <artifactId>sootup.java.sourcecode</artifactId>
            <version></version>
        </dependency>
    ```

=== "Gradle"

    ```groovy
        compile "org.soot-oss:sootup.qilin:{{ git_latest_release }}"
    ```

### How to create a pointer analysis

One can create an Andersen's context-insensitive analysis with following code:

=== "Java"

    ```Java
        String MAINCLASS = "dacapo.antlr.Main"; // just an example
        PTAPattern ptaPattern = new PTAPattern("insens");
        PTA pta = PTAFactory.createPTA(ptaPattern, view, MAINCLASS);
        pta.run();
    ```

Users must specify the program's `View`, select a `PTAPattern`
(indicating the desired types of pointer analyses to perform),
and designate the `MAINCLASS` (serving as the entry point for the analysis).

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
However, a similar functionality can be found
in `qilin.test.util.AliasAssertion` with the method:
`boolean isMayAlias(PTA pta, Value va, Value vb)`.
This method allows users to check for potential aliasing between two values
given a PTA instance.

### A Full list of Point Analyses

Qilin's toolbox includes a rich set of pointer analyses, which are given below:

Note that the symbol <em>k</em> used in the table should be replaced with a concrete small constant like 1 or 2.

| PTA patterns                         | Description                                                             | Reference  |
|--------------------------------------|-------------------------------------------------------------------------|---|
| -pta=<em>insens</em>                 | Andersen's context-insensitive analysis                                 | https://link.springer.com/chapter/10.1007/3-540-36579-6_12 |
| -pta=<em>k</em>c                     | <em>k</em>-callsite-sensitive pointer analysis (denoted <em>k</em>CFA). | https://www.cse.psu.edu/~trj1/cse598-f11/docs/sharir_pnueli1.pdf|
| -pta=<em>k</em>o                     | <em>k</em>-object-sensitive pointer analysis (denoted <em>k</em>OBJ).   | https://dl.acm.org/doi/abs/10.1145/1044834.1044835 |
| -pta=<em>k</em>t                     | <em>k</em>-type-sensitive pointer analysis (denoted <em>k</em>TYPE).    | https://dl.acm.org/doi/abs/10.1145/1926385.1926390 |
| -pta=<em>k</em>h                     | hybrid <em>k</em>-object-sensitive pointer analysis.                    | https://dl.acm.org/doi/10.1145/2499370.2462191 |
| -pta=<em>k</em>ht                    | hybrid <em>k</em>-type-sensitive pointer analysis.                      | https://dl.acm.org/doi/10.1145/2499370.2462191 |
| -pta=B-2o                            | BEAN-guided 2OBJ. Only k=2 is supported.                                | https://link.springer.com/chapter/10.1007/978-3-662-53413-7_24 |
| -pta=D-2o                            | Data-driven 2OBJ. Only k=2 is supported.                                | https://dl.acm.org/doi/10.1145/3133924 |
| -pta=D-2c                            | Data-driven 2CFA. Only k=2 is supported.                                | https://dl.acm.org/doi/10.1145/3133924 |
| -pta=M-<em>k</em>o                   | MAHJONG-guided <em>k</em>OBJ.                                           | https://dl.acm.org/doi/10.1145/3062341.3062360 |
| -pta=M-<em>k</em>c                   | MAHJONG-guided <em>k</em>CFA.                                           | https://dl.acm.org/doi/10.1145/3062341.3062360 |
| -pta=E-<em>k</em>o                   | EAGLE-guided <em>k</em>OBJ.                                             | https://dl.acm.org/doi/10.1145/3360574 |
| -pta=T-<em>k</em>o                   | TURNER-guided <em>k</em>OBJ.                                            | https://drops.dagstuhl.de/opus/volltexte/2021/14059/ |
| -pta=Z-<em>k</em>o                   | ZIPPER-guided <em>k</em>OBJ.                                            | https://dl.acm.org/doi/10.1145/3276511 |
| -pta=Z-<em>k</em>c                   | ZIPPER-guided <em>k</em>CFA.                                            | https://dl.acm.org/doi/10.1145/3276511 |
| -pta=Z-<em>k</em>o -cd               | The context debloated version of ZIPPER-guided <em>k</em>OBJ.           | https://doi.org/10.1109/ASE51524.2021.9678880 |
| -pta=<em>k</em>o -cd -cda=CONCH      | The context debloated version of <em>k</em>OBJ using Conch.             | https://doi.org/10.1109/ASE51524.2021.9678880 |
| -pta=<em>k</em>o -cd -cda=DEBLOATERX | The context debloated version of <em>k</em>OBJ using DebloaterX.        | https://dl.acm.org/doi/10.1145/3622832 |
| -pta=s-<em>k</em>c                   | SELECTX-guided <em>k</em>CFA.                                           | https://doi.org/10.1007/978-3-030-88806-0_13 |

