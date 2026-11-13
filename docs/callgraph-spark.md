# Spark Call Graphs

[Spark](https://plg.uwaterloo.ca/~olhotak/pubs/thesis-olhotak-msc.pdf) is a context-insensitive,
field-sensitive, Andersen-style points-to analysis. It builds a *pointer assignment graph* (PAG) from
the reachable methods and propagates allocation sites through it. Spark can be used in two ways:

- **On the fly**: the call graph grows together with the points-to sets. A virtual call is only
  resolved to the run-time types of the objects its receiver may point to. This gives the precise call
  graph from the [tutorial's comparison](callgraphs.md#the-running-example).
- **CHA-seeded** (the default): Spark computes points-to sets on a CHA call graph that is built up
  front. The call graph you get back *is* that CHA graph. Use this mode when you only need points-to
  sets.

Start with the [call graph tutorial](callgraphs.md) for the shared options. For context-sensitive
analyses, see [Qilin](qilin.md).

## Dependencies

=== "Maven"

    ```maven
    <dependency>
        <groupId>org.soot-oss</groupId>
        <artifactId>sootup.spark</artifactId>
        <version>{{ git_latest_release }}</version>
    </dependency>
    ```

=== "Gradle"

    ```groovy
    compile "org.soot-oss:sootup.spark:{{ git_latest_release }}"
    ```

## Building via `CallGraphConfig`

=== "On the fly (precise call graph)"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:spark"
    ```

=== "CHA-seeded (default)"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/SparkCallGraphTest.java:cha-seeded"
    ```

With CHA seeding, the shared `callResolver`, `virtualCallResolver` and `seedEntryPointClinits` settings
configure the CHA graph. In on-the-fly mode they are not used, and Spark currently doesn't model
`<clinit>` calls at all. `reflectionModel` and `dynamicInvokeResolver` apply in both modes.

## `SparkOptions`

Every option, with its default:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/SparkCallGraphTest.java:options"
```

| Option | Default | Effect |
|---|---|---|
| `onFlyCallGraph` | `false` | Build the call graph together with the points-to sets instead of using CHA. Much more precise call graph |
| `ignoreBaseObjects` | `false` | Field-based analysis: `a.f` and `b.f` are the same node, no matter what `a` and `b` point to. Cheaper, less precise |
| `typesForSites` | `false` | One abstract object per *type* instead of per `new` statement. Smaller points-to sets, less precise |
| `simpleEdgesBidirectional` | `false` | Make assignment edges bidirectional, which turns the analysis into a unification-based (Steensgaard-style) one. Cheaper, much less precise |
| `parmsAsFields` | `false` | *Not implemented yet, no effect.* In Soot: model parameters as field-reference nodes |
| `returnsAsFields` | `false` | *Not implemented yet, no effect.* In Soot: model return values as field-reference nodes |
| `mergeStringBuffer` | `true` | *Not implemented yet, no effect.* In Soot: one abstract object for all `StringBuffer`/`StringBuilder` allocations |
| `simulateNatives` | `true` | *Not implemented yet, no effect.* In Soot: model the effects of JDK native methods. Qilin does model them |

For example, with one abstract object per type:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/SparkCallGraphTest.java:types-for-sites"
```

In the running example, this makes no difference, since every class is instantiated only once.

## The `Spark` facade: points-to sets

To get the points-to analysis as well as the call graph, use `Spark.builder()`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/SparkCallGraphTest.java:facade"
```

`PointsToAnalysis` answers queries for a `Value` in a method:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/SparkCallGraphTest.java:points-to"
```

| Query | Result |
|---|---|
| `reachingObjects(value, method)` | the allocation sites (`AllocationNode`s) `value` may point to |
| `reachingTypes(value, method)` | the run-time types of those objects |
| `aliases(value, method)` | the PAG nodes that may point to a common object |
| `getPointsToMap()` | the raw PAG node → allocation sites map, for debugging |

`spark.getPag()` gives access to the pointer assignment graph itself. `PAGVisualizer` can render it.

### Using your own call graph

In the default mode, you can pass a call graph you have already built, e.g. a CHA graph with your own
`<clinit>` or scoping settings:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/SparkCallGraphTest.java:facade-own-cg"
```

!!! warning "Use the same reflection model and invokedynamic resolver"

    Build the call graph with the same `ReflectionModel` and `DynamicInvokeResolver` instances that
    you give to Spark. Otherwise the reflective and invokedynamic edges in the graph won't match the
    PAG. `callGraph(...)` is ignored (with a warning) when `onFlyCallGraph` is `true`.

Next: [Qilin](qilin.md) · [CHA & RTA](callgraph-cha-rta.md) · back to the [tutorial](callgraphs.md)
