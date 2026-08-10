> Design document for `sootup.spark`'s reflective call resolution. Note: at time of writing this
> lives on branch `feature/spark-reflection-resolution`, not yet merged into `develop` -- see
> [Status](#status) at the end.

# Reflective Call Resolution in Spark

## Motivation

Spark's on-the-fly (OTF) call graph is built by propagating points-to information along the PAG
(pointer assignment graph) starting from the analysis's entry points. Reflective APIs --
`Class.forName(String)`, `Class#newInstance()`/`Constructor#newInstance(Object[])`,
`Class#getMethod(String, Class[])`/`Method#invoke(Object, Object[])` -- break that propagation at
the source: the class or method being invoked is named by a runtime `String` value, not by a
static type or a fixed `MethodSignature` the way an ordinary `new Foo()` or `foo.bar()` call site
is. Before this feature, every one of those calls was simply opaque to Spark: the code they invoke
never became reachable, and no points-to edges ever flowed through them.

In practice, a large amount of real-world Java routes through exactly this pattern. Two motivating
cases from `sosa-analyzer`'s DaCapo benchmark suite:

- **JAXP factory lookups.** `TransformerFactory.newInstance()` internally forwards a class name
  string through `FactoryFinder.newInstance(String, ClassLoader)`, which calls
  `Class.forName(param)` on a `Local`, not a literal -- the literal is one or more parameter-passing
  hops away from the `Class.forName` call site itself.
- **DaCapo's own harness classes.** `org.dacapo.harness.Xalan`/`Lusearch`/`Luindex` each do
  `Class.forName("org.dacapo.<benchmark>.<Impl>", true, loader)` (a literal, directly at the call
  site) followed by `Class#getMethod("main", ...)` + `Constructor#newInstance(...)` +
  `Method#invoke(...)` to construct and drive the real benchmark class -- the textbook
  reflection-based plugin/wrapper shape.

Both shapes are addressed by this feature: the first requires resolving a literal that reaches
`Class.forName` *interprocedurally*; the second requires resolving the full
`forName` &rarr; `newInstance`/`getConstructor` &rarr; `getMethod` &rarr; `invoke` chain once the
class is known.

## Where this lives

All new code is under `sootup.spark`:

```
sootup.spark/src/main/java/sootup/spark/
├── node/
│   ├── StringConstantNode.java        (new)
│   ├── ClassConstantNode.java         (new)
│   ├── ReflectiveClassToken.java      (new)
│   └── ReflectiveMethodToken.java     (new)
├── MethodPAGStmtVisitor.java          (modified)
└── Solver.java                        (modified)
```

Nothing outside `sootup.spark` changed. Every new type extends the existing `AllocationNode` base
class, so any consumer that only cares about `Node.getType()` / `Node.getAllocationSite()` keeps
working unmodified -- the resolution machinery is additive.

## The core idea: value-carrying allocation nodes

Spark's PAG already models `new Foo()` as an `AllocationNode` and propagates it along assignment,
parameter-passing, and field-store/load edges exactly like any other pointer value. The key
insight this feature reuses: **a string literal can ride those same edges**, if the node
representing it carries its actual value instead of just recording "some `String` was allocated
here."

### `StringConstantNode`

`ValueToNodeConversionVisitor.caseStringConstant` used to convert every Jimple `StringConstant`
into a generic, value-less `AllocationNode`. It now builds a `StringConstantNode` instead --
identical in every way except it also carries `value: String` (the literal's actual text).
Equality and `hashCode()` are keyed on `value` plus the allocation site (mirroring
`AllocationNode`'s existing `isTypesForSites()` convention), specifically so that under
`isTypesForSites()` merging, two *different* literals reaching the same site never collapse into
one node -- that would let one literal's value silently stand in for another's.

Because it's a normal `AllocationNode` subtype, it flows through every PAG edge Spark already
builds -- assignments, parameter passing, field stores/loads, returns -- for free. This is what
makes interprocedural resolution possible: a literal doesn't need a bespoke backward-slicing pass
to be traced across a call boundary, it just needs to *already be at the far end of a PAG edge*
by the time something asks for its points-to set.

### `ClassConstantNode`

A related, smaller fix in the same area: a class-literal constant (`Foo.class`, i.e. a Jimple
`ClassConstant`) used to be dropped outright (`ignore()`). Any receiver holding one -- e.g. a
`Class` argument passed as `Foo.class` and later called with `.cast(...)` / `.isInstance(...)` --
therefore had an empty points-to set and could never dispatch to anything. `ClassConstantNode`
carries the represented class's descriptor (`value: String`) the same way `StringConstantNode`
carries a string's text, restoring dispatch on `Class`-typed literals. This is not part of the
`Class.forName` resolution chain below, but is a prerequisite for `.class`-literal-driven dispatch
to work at all, and ships in the same commit.

## Reflective tokens

Two more `AllocationNode` subtypes model the objects reflection APIs *hand back*, so a chain of
calls on them (`Class.forName(...).getConstructor().newInstance()`, or
`clazz.getMethod(...).invoke(...)`) can be dispatched on like any other object graph.

### `ReflectiveClassToken`

Represents the `Class`/`Constructor` object returned by `Class.forName(...)`,
`Class#getConstructor()`, or `Class#getDeclaredConstructor()`, once the class name is known.
`Node.getType()` stays the call's *static* return type (`java.lang.Class` or
`java.lang.reflect.Constructor`) -- that's what ordinary virtual dispatch would see if some
unrecognized reflective method were called on it. The field that actually matters,
`represented: ClassType`, is what `Solver`'s reflective-call handling dispatches on. Equality is
keyed on `represented` plus allocation site, for the same "never silently merge two different
targets" reason as `StringConstantNode`.

### `ReflectiveMethodToken`

Represents the `java.lang.reflect.Method` object returned by `Class#getMethod(String, Class[])` /
`Class#getDeclaredMethod(String, Class[])` on a `ReflectiveClassToken` receiver, once the method
name is a literal at that call site. Carries `declaringClass: ClassType` and `methodName: String`.
The `Class[]` parameter-type array is not tracked (mirrors `ReflectiveClassToken` not tracking
`getConstructor(Class[])`'s array) -- `methodName` alone decides what a later `Method#invoke(...)`
dispatches to, by scanning the invocation's runtime receiver type for a same-named method. Exact
whenever the declaring class has at most one method by that name; an over-approximation (arbitrary
overload picked) otherwise -- the same tradeoff `Solver`'s constructor resolution already makes.

## Resolution pipeline

There are four distinct resolution steps, each triggered at a different point in the chain.

### 1. `Class.forName("literal")` -- eager, at the call site

`MethodPAGStmtVisitor.handleClassForName` fires on every static call to `java.lang.Class.forName`.
If `arg0` is a `StringConstant` right there at the call site, it resolves immediately via
`wireReflectiveClassToken`: parses the literal into a `ClassType`, confirms it resolves against the
view, and (if so) builds a `ReflectiveClassToken` and wires a PAG edge from it to the call's `lhs`
-- exactly as if the source had written `Class<Widget> c = <token for Widget>` directly.

### 2. `Class.forName(param)` -- deferred, interprocedural

If `arg0` is a `Local` instead (the literal is somewhere upstream, forwarded through one or more
parameter-passing hops), eager resolution can't work: the literal's value isn't visible yet. The
call site is instead recorded as a `PendingReflectiveForName(srcSig, arg, callType, lhs, stmt)` and
revisited on every fixpoint iteration (see [Fixpoint integration](#fixpoint-integration) below):
once `arg`'s points-to set contains a `StringConstantNode`, its `value` is fed through the *same*
`wireReflectiveClassToken` helper the eager path uses -- so both paths produce identical PAG state,
just at different times. This generalizes to arbitrary depth of parameter forwarding for free: once
a `StringConstantNode` exists at its origin, it rides whatever chain of PAG edges Spark already
builds, no matter how many methods it passes through before reaching a `Class.forName` call.

Resolving the literal is deliberately defensive end-to-end (parsing the string into a `ClassType`,
then resolving that type against the view are both wrapped to swallow `RuntimeException`): under
context-insensitive merging, a `Local`'s points-to set can include completely unrelated string
literals that reached the same parameter from a different call site sharing that method (e.g. a
namespace URI happening to reach the same formal parameter as a real class name, if that parameter's
containing method is called elsewhere with that value). Such strings are not valid class names, and
both parsing and classpath resolution can throw for them (not just report "not found") --
those are treated identically to a genuinely absent class: no resolution, no crash.

<a id="deferred-resolution"></a>

### 3. Dispatch on a `ReflectiveClassToken` receiver

`Solver.resolveReflectiveCall` recognizes four method names called on a `ReflectiveClassToken`:

| Called method | Effect |
|---|---|
| `newInstance()` | `instantiateReflectively`: resolves a constructor (see arity handling below), makes it reachable, points the call's `lhs` **and** the constructor's `this` at one fresh allocation of the represented type -- as if `new Foo()` had been written directly. |
| `getConstructor()` / `getDeclaredConstructor()` | `propagateReflectiveToken`: hands back *another* `ReflectiveClassToken` for the same represented class, so a later `.newInstance()` on the `Constructor` object resolves identically to the direct `Class#newInstance()` case. |
| `getMethod(name, ...)` / `getDeclaredMethod(name, ...)` | `propagateReflectiveMethodToken`: if `name` is a literal at *this* call site (no interprocedural propagation attempted here -- a name computed elsewhere and forwarded in is left unresolved), hands back a `ReflectiveMethodToken`. |
| anything else (e.g. `getName()`) | left unresolved, same as before this feature existed. |

Constructor selection (`Solver.resolveConstructor`) prefers the no-arg constructor when one exists
(the common case for JAXP-style factories); otherwise it falls back to the first declared
constructor whose parameter count fits within the call site's own syntactic argument count. That
bound is not just a precision choice -- see [Arity bounding](#arity-bounding-not-just-precision)
below.

### 4. `Method#invoke(receiver, args)` on a `ReflectiveMethodToken` receiver

`Solver.resolveReflectiveMethodInvoke` is the reflective analogue of ordinary virtual dispatch: for
every allocation in the invocation's `receiver` argument's points-to set, it looks up a method
named `methodToken.methodName()` declared directly on that allocation's type (`findMethodByName` --
no superclass walk, no overload disambiguation, same "exact when unambiguous" tradeoff as
constructor resolution), makes it reachable, and wires `receiver` to the target's `this`. The
`args` array's individual elements are not tracked (same scope limit as `newInstance(Object[])`) --
this makes the target reachable and analyzable, but not its formal parameters or return value.

## Fixpoint integration

`Solver.solveOnTheFly`'s existing OTF loop already had three ingredients: a worklist of newly
discovered methods, an incremental points-to propagator, and a list of pending virtual calls
resolved once their receiver's points-to set is known. This feature adds a second pending-call
list and two more resolution branches inside the existing "resolve pending calls" step, so
everything shares one fixpoint rather than running a second one:

```
while (changed):
  1. Build PAGs for newly-discovered methods (adds to `pending` and
     `pendingReflectiveForName` as new call sites are visited).
  2. Propagate points-to information (IncrementalPointsToAnalysis.propagate()).
  3. For each pending virtual/interface call site:
       for each allocation `o` in the receiver's points-to set:
         - o is a ReflectiveClassToken   -> resolveReflectiveCall(...)
         - o is a ReflectiveMethodToken  -> resolveReflectiveMethodInvoke(...)
         - otherwise                     -> ordinary CHA-style virtual dispatch
  4. For each pending Class.forName(arg) site (arg not a literal at the call site):
       for each allocation `o` in arg's points-to set:
         - o is a StringConstantNode -> wireReflectiveClassToken(o.value, ...)
```

Step 3's three-way branch is why `ReflectiveClassToken`/`ReflectiveMethodToken` extend
`AllocationNode` rather than being a completely separate mechanism: the *existing* "what's in this
receiver's points-to set" query already returns them alongside ordinary allocations, so no new
query path was needed -- only a new case in the branch that already existed to interpret each
result.

Each resolution kind is deduplicated by its own key (`ResolvedReflectiveCall`,
`ResolvedReflectiveForName`, `ResolvedReflectiveMethodCall`), keyed on the *resolved target*
(represented class, or `(declaringClass, methodName)` pair) rather than on token identity. This
matters because context-insensitive merging routinely produces multiple distinct token allocations
representing the same logical target -- e.g. a fresh `ReflectiveClassToken` minted by
`propagateReflectiveToken` on every `getConstructor()` call, or several `Class.forName` call sites
whose tokens all funnel into one shared `newInstance()` receiver. Keying dedup on the whole token
treated every one of those as "new" forever, growing the dedup sets (and the redundant resolution
work they exist to bound) unboundedly across OTF iterations instead of letting the fixpoint
converge.

## Precision & soundness tradeoffs

- **Computed names are never resolved**, by design. A class or method name built via string
  concatenation, read from a config file, or otherwise not traceable to a literal, is left exactly
  as unresolved as it was before this feature. This is deliberately conservative: guessing at
  targets for a genuinely dynamic name would be unsound.
- **Argument arrays are not unpacked.** `newInstance(Object[])`'s array and `invoke(Object,
  Object[])`'s array are not tracked element-by-element -- a resolved callee becomes reachable and
  analyzable, but its formal parameters aren't fed from the reflective call's actual argument
  values.
- **No overload disambiguation.** Both constructor and method resolution pick a single candidate by
  name (and, for constructors, no-arg-preferred) without inspecting `getConstructor(Class[])` /
  `getMethod(String, Class[])`'s own parameter-type array. Exact whenever the target has at most one
  candidate by that selection rule; an arbitrary-overload over-approximation otherwise.

### Arity bounding is not just precision

Both `resolveConstructor` and `findMethodByName` additionally bound candidates by the *call site's*
own syntactic argument count (0 or 1 for `Class#newInstance()` / `Constructor#newInstance(Object[])`,
exactly 2 for `Method#invoke(Object, Object[])`) -- and this bound is load-bearing, not just a
precision refinement. `sosa-analyzer`'s FAIR converter builds a resolved callee's actual-argument
list directly from the call site's own syntactic arguments, never from the callee's real declared
arity. A resolved candidate whose parameter count exceeds what the call site can syntactically
supply doesn't just lose precision -- downstream, `IFDSCallSemantics.buildSplitCall` indexes past
the end of that list and throws `IndexOutOfBoundsException`. Finding no constructor/method within
the bound means the call site's real target genuinely can't be represented here; the correct
response is to wire nothing, not to pick an out-of-bound candidate that will later crash a
consumer. `ReflectiveArityBoundTest` is a regression test traced directly back to this crash.

### Context-insensitive merging risk

Because Spark here is context-insensitive, a shared method's parameter can carry the union of
every literal passed to it across all its call sites. `ReflectiveInstantiationIndirect.java`'s test
fixture deliberately keeps its "does a literal resolve" wrapper chain
(`createViaLiteral`/`createViaWrapper`) separate from its "does a computed name resolve" chain
(`createViaLiteralUnrelated`/`createViaWrapperUnrelated`), specifically so a shared-parameter merge
between a literal call and a computed-name call can't accidentally make the computed-name case look
resolved. The defensive parse/resolve handling in `wireReflectiveClassToken` (see
["`Class.forName(param)`" above](#deferred-resolution)) exists for the same underlying
risk: an unrelated literal merged in from another call site is not a valid class name, and must
fail closed rather than throw.

## Worked examples

All fixtures live under `sootup.spark/src/test/resources/pta/source/`.

**Direct literal** (`ReflectiveInstantiation.java`):
```java
Class<?> c = Class.forName("ReflectiveInstantiation$Widget");
Object viaNewInstance = c.newInstance();               // resolves: Widget's ctor reachable
Object viaComputedName = Class.forName(args[0]).newInstance(); // stays unresolved
```

**Literal reaching `Class.forName` through parameter forwarding**
(`ReflectiveInstantiationIndirect.java`), the JAXP `TransformerFactory.newInstance` &rarr;
`FactoryFinder.newInstance` &rarr; `Class.forName(param)` shape:
```java
static Object createViaLiteral(String className) throws Exception {
  return Class.forName(className).newInstance();       // className is a parameter, not a literal here
}
static Object createViaWrapper(String className) throws Exception {
  return createViaLiteral(className);                  // one more hop
}
...
Object viaTwoHops = createViaWrapper("ReflectiveInstantiationIndirect$Widget"); // resolves
```

**Full `getMethod`/`invoke` chain, the DaCapo harness shape** (`ReflectiveMethodInvocation.java`):
```java
Class<?> c = Class.forName("ReflectiveMethodInvocation$Widget");
Constructor<?> ctor = c.getConstructor(int.class);
Object widget = ctor.newInstance(42);                   // single-arg ctor resolves

Method m = c.getMethod("doWork");
m.invoke(widget);                                        // doWork() reachable

Method computed = c.getMethod(args[0]);
computed.invoke(widget);                                  // stays unresolved
```

**Arity bound preventing a crash** (`ReflectiveArityBound.java`):
```java
static class TooManyCtorArgs { TooManyCtorArgs(int a, int b) { ... } }
...
Class<?> c1 = Class.forName("ReflectiveArityBound$TooManyCtorArgs");
Object x = c1.getConstructor().newInstance();  // 0 syntactic args at this call site;
                                                // the 2-arg ctor is NOT wired (would crash the
                                                // FAIR converter downstream if it were)
```

## Testing

`sootup.spark`'s test suite (`mvn -pl sootup.spark test`) passes at 104/104 with this change,
including five new test classes exercising every path above:
`ReflectiveInstantiationTest`, `ReflectiveInstantiationIndirectTest`,
`ReflectiveMethodInvocationTest`, `ReflectiveArityBoundTest`. (`LambdaResolutionTest` is also new in
the same branch but covers unrelated `invokedynamic`/lambda dispatch work -- see
[Adjacent work](#adjacent-work-in-this-branch) below.)

## Relationship to downstream consumers

`sosa-analyzer`'s DaCapo benchmark suite (`xalan`/`lusearch`/`luindex`) is the motivating consumer.
Its harness invokers (`XalanInvoker`/`LusearchInvoker`/`LuindexInvoker`) chain into
`org.dacapo.harness.*` classes that reach the real benchmark implementation
(`XSLTBench`/`Search`/`Index`) via exactly the `Class.forName(literal)` &rarr;
`Constructor#newInstance` &rarr; `Method#invoke` chain this feature resolves. Measured end to end
(with an otherwise-identical Spark build lacking this feature as the control):

| | with reflection resolution | without |
|---|---|---|
| Luindex: real `org.apache.lucene.*` methods reached | 515 | 0 |
| Luindex: `methodsAnalyzed` | 856 | 6 |
| Lusearch: real `org.apache.lucene.*` methods reached | 698 | 306 |
| Lusearch: `Search::main` present in the call graph | yes | no |

(Lusearch retains partial coverage without this feature only because `sosa-analyzer` separately
seeds `Search$QueryThread.run()` as an additional, disconnected entry point -- a workaround for a
*different* gap, native `Thread.start()` dispatch, which this feature does not address.)

## Adjacent work in this branch

The same commit also fixes `invokedynamic` (`JDynamicInvokeExpr`) handling for
`LambdaMetafactory`-backed call sites (ordinary lambdas and method references): previously every
`invokedynamic` site resolved to zero call graph edges, hiding lambda/method-reference bodies from
Spark entirely. `MethodPAGStmtVisitor.handleLambdaInvokeDynamic` now resolves the real
implementation method from the bootstrap arguments and wires its captured arguments (and receiver,
for bound/unbound instance references) into the target's parameters. This is unrelated to
reflection resolution -- covered here only because it shipped in the same commit and touches the
same `handleInvokeExprOtf` dispatch point (`LambdaResolutionTest` covers it) -- and is out of scope
for the rest of this document.

## Status

This work is on `feature/spark-reflection-resolution`, branched from `develop` at the point where
`fix/pag-invokedynamic-otf`'s original scope (the OTF crash fix + client-supplied-CG option) had
already merged via PR #1688. It has **not** been merged into `develop`, and therefore is **not**
part of the published `3.0.1-SNAPSHOT` Maven Central Portal snapshot artifact -- any consumer that
wants it today needs a locally built jar (`mvn -pl sootup.spark -am install`) rather than the plain
`org.soot-oss:sootup.spark:3.0.1-SNAPSHOT` coordinate.
