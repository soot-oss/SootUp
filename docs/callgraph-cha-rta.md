# SootUp CHA & RTA

SootUp's own call graph algorithms are in `sootup.callgraph`: Class Hierarchy Analysis (CHA) and Rapid
Type Analysis (RTA). Neither one computes points-to sets, so both are fast: each reachable method is
analyzed once, and each call is resolved against the type hierarchy. Start with the
[call graph tutorial](callgraphs.md) if you haven't read it yet. It explains the shared options
(`callResolver`, `virtualCallResolver`, `seedEntryPointClinits`, `reflectionModel`,
`dynamicInvokeResolver`) that both algorithms support.

## How they resolve a call

For each reachable method, both algorithms go through its invoke statements:

- **Static and special calls** (`staticinvoke`, `specialinvoke`: static methods, constructors,
  `super.m()`, private methods) have exactly one target. The algorithms look it up in the declaring
  class or, if it isn't declared there, in its superclasses and interfaces.
- **Virtual and interface calls** (`virtualinvoke`, `interfaceinvoke`) have one possible target per
  subtype of the receiver's static type.
    - **CHA** takes every non-abstract subtype from the `TypeHierarchy` and looks up which method each
      subtype would actually run (its own, or an inherited one).
    - **RTA** does the same, but only for subtypes that have been *instantiated* (with `new`) in a
      reachable method. If a class only becomes instantiated later in the traversal, RTA revisits the
      calls it had already resolved.

In the [running example](callgraphs.md#the-running-example), CHA reports `Hexagon.area()` because
`Hexagon` implements `Shape`. RTA doesn't, because no reachable code ever executes `new Hexagon()`.

## Building via `CallGraphConfig`

=== "CHA"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:cha"
    ```

=== "RTA"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphComparisonTest.java:rta"
    ```

CHA has no options of its own beyond the [shared options](callgraphs.md#step-5-shared-options).

### RTA: `preInstantiatedClasses`

Sometimes objects are created by code that isn't analyzed, e.g. by a framework, or by deserialization.
RTA would never see a `new` for them. `preInstantiatedClasses` tells RTA to treat those classes as
instantiated from the start:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:rta-preinstantiated"
```

## Using the algorithm objects directly

`ClassHierarchyAnalysisAlgorithm` and `RapidTypeAnalysisAlgorithm` implement `CallGraphAlgorithm`:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:direct"
```

`addClass` extends an existing call graph: every method of the given class becomes an additional entry
point, and the algorithm adds whatever becomes reachable:

```java
--8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:add-class"
```

The constructors take the shared options as positional arguments. The overload with every argument:

=== "CHA"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:cha-constructor"
    ```

=== "RTA"

    ```java
    --8<-- "sootup.examples/src/test/java/sootup/examples/callgraph/CallGraphOptionsTest.java:rta-constructor"
    ```

There are shorter overloads for common subsets: `(view)`, `(view, callResolver)`,
`(view, virtualCallResolver)`, `(view, callResolver, virtualCallResolver)`, and
`(view, callResolver, virtualCallResolver, seedEntryPointClinits)`. The last one has two extended
variants: with a `ReflectionModel`, or with a `DynamicInvokeResolver`. RTA's constructors take
`preInstantiatedClasses` as their second argument. Anything you leave out gets the same default as
with `CallGraphConfig`.

## Performance notes

- CHA and RTA never load the body of a method that the `CallResolver` excludes. With the default
  resolver, that is every library method, which is why a call graph for a small program with the
  whole JDK in the view still takes only a few seconds.
- The type hierarchy is built once per `View` and cached. Reusing one view for several call graphs
  saves that work.
- RTA does a bit more bookkeeping per call than CHA (it remembers the calls to classes that aren't
  instantiated yet), but it reaches fewer methods, so it also loads fewer bodies.

Next: [Spark](callgraph-spark.md) · [Qilin](qilin.md) · back to the [tutorial](callgraphs.md)
