# Android APK Call Graph — Implementation Plan

**Status: all 10 steps implemented, plus post-completion fixes, a
synthesized `dummyMain` addition, and dynamic-`BroadcastReceiver` entry
points (see the "Post-completion" sections below).** Start at
`AndroidApkAnalysis` (module root of `sootup.apk.frontend`) for the public
entry point, or at any of this section's per-step notes for how a given
piece works and why. The rest of this document is the design history, kept
because the *why* behind each step's tradeoffs and limitations doesn't
belong in code comments and is exactly what a future change to this area
needs first.

## Context

`sootup.apk.frontend` already converts an APK's DEX bytecode to Jimple
(`ApkAnalysisInputLocation`, `DexClassProvider`, the `instruction/*`
translators). `sootup.callgraph` already implements generic, Android-agnostic
CHA and RTA (`ClassHierarchyAnalysisAlgorithm`, `RapidTypeAnalysisAlgorithm`),
driven entirely by:

```java
CallGraph CallGraphAlgorithm.initialize(List<MethodSignature> entryPoints)
```

Neither module has any notion of Android components, lifecycles, or
callbacks today. `AndroidVersionInfo` parses `AndroidManifest.xml` only far
enough to read `targetSdkVersion`/`minSdkVersion` — nothing about
`<activity>`/`<service>`/`<receiver>`/`<provider>` declarations. There is no
entry-point model, no layout parsing, and no Intent/ICC resolution. The
existing `CallGraphTest` fakes an entry point by hand-naming
`de.ecspride.MainActivity#onCreate`.

`sootup.apk.parser` and `sootup.apk.backend` exist as empty, unregistered
directories (no `pom.xml`, not listed in the root `pom.xml` `<modules>`).
They are not used by this plan; all new code lives in `sootup.apk.frontend`,
which is the only registered, working APK module.

The key insight that shapes this plan: **CHA/RTA don't need to change.**
Everything Android-specific reduces to *what entry points get passed to
`initialize(...)`*. The work is entirely about producing that list correctly.

## Step-by-step plan

1. **Manifest component model + parser.** Parse `<application>`,
   `<activity>`, `<service>`, `<receiver>`, `<provider>`, their
   `<intent-filter>` action/category children, `exported`/`enabled` flags,
   and the app's `Application` subclass, into a structured
   `AndroidManifest` model. Built on the `axml` library already used by
   `AndroidVersionInfo`. **[Implemented in this pass.]**

2. **Static lifecycle tables.** Android lifecycle callbacks are a fixed
   per-component-type contract (Activity: `onCreate → onStart → onResume →
   ... → onDestroy`; Service: `onCreate/onStartCommand/onBind/onDestroy`;
   `BroadcastReceiver#onReceive`; `ContentProvider` CRUD methods;
   `Application#onCreate`), not something discovered from bytecode. Encode
   this as a lookup table keyed by component type (mirrors FlowDroid's
   `AndroidEntryPointConstants`). **[Implemented in this pass.]**

3. **Callback/listener discovery.** Scan every app class for ones
   implementing a known Android UI callback interface (`View.OnClickListener`,
   `TextWatcher`, adapter listeners, etc.) — code reachable from the
   framework once registered on a widget, even though nothing in the app
   directly calls it. Deliberately whole-view rather than tied to a
   `setOnClickListener`-style call site: an implementation can be an
   anonymous inner class, a top-level class, or reused across several
   registrations, so scanning for the interface is more robust than tracing
   registration call sites. This over-approximates (doesn't prove the class
   was ever actually registered anywhere), matching how CHA/RTA are already
   over-approximate. **[Implemented in this pass.]**

4. **Layout XML parsing.** Parse `res/layout*/*.xml` (binary XML, same
   `axml` dependency) for `android:onClick` attributes — a widget wired
   straight to a `void method(View)` by name, with no `setOnClickListener`
   call site for step 3 to find. Extraction only yields method *names*, not
   which activity uses which layout (that needs a `resources.arsc` parser to
   resolve `R.layout.*` constants back to file names, not implemented), so
   every extracted name is checked against every manifest-declared activity
   — an over-approximation in the same spirit as step 3. **[Implemented in
   this pass.]**

5. **Entry-point generation.** Combine the manifest model (1) and lifecycle
   tables (2) into the actual `List<MethodSignature>` passed to
   `CallGraphAlgorithm.initialize(...)`.

   **Design decision — no synthesized dummy-main body.** The classic
   Soot/FlowDroid approach builds one synthetic `dummyMainMethod` with a
   Jimple body that conditionally/repeatedly calls every lifecycle and
   callback method, modeling the OS's unpredictable invocation order. That
   body only matters for analyses that are sensitive to *statement order or
   control flow* (e.g. taint tracking). `CallGraphAlgorithm.initialize`
   already accepts a **list** of entry points and treats each independently
   as a root — so for call-graph reachability specifically, a flat
   deduplicated list of lifecycle-method signatures is call-graph-equivalent
   to a synthetic root method that unconditionally calls all of them, without
   the ~significant~ extra engineering cost of hand-building `Body`/`Stmt`/
   `Local`/CFG objects and an `AnalysisInputLocation` to host a synthetic
   class. If a later step (e.g. dataflow/taint analysis on top of the call
   graph) needs real control flow between entry points, a synthesized
   dummy-main can be added later as an additive change — the manifest model
   and lifecycle tables from steps 1–2 are reused either way.

   **Update:** that later step happened — see "Post-completion addition: a
   real synthesized `dummyMain` method, after all" near the end of this
   document. The flat-list path described above remains the default; the
   dummy-main is an additive alternative for callers that specifically need
   the single-root shape.

   For each manifest component (and the `Application` class, if declared),
   walk the component's class upward through its Java superclasses (via the
   `View`'s already-built type hierarchy) looking for the first
   non-library (i.e. app-defined) class that overrides each lifecycle
   method. This correctly finds lifecycle overrides declared on a shared
   base class (e.g. a `BaseActivity` that most activities extend) and
   correctly omits lifecycle methods the app never overrides (nothing to
   add as a root; the framework's own default implementation isn't part of
   the app's dex and doesn't call back into it). **[Implemented in this
   pass.]**

6. **Dispatch-semantics sanity check at the seam.** Confirm
   `sootup.callgraph.scope`'s virtual-call resolution doesn't need changes
   given that lifecycle methods are now reachable only via the entry-point
   list, never via an in-app call site (the OS calls them, the app doesn't).
   Expected to require no code changes — verified by the tests added in this
   pass rather than as a separate implementation step.

7. **Inter-component communication (ICC).** Model explicit Intents
   (`new Intent(this, Target.class)` → edge to `Target`'s `onCreate`) and
   implicit Intents (action/category matching against the manifest's
   `<intent-filter>`s from step 1). The hardest, most Android-specific
   piece; layered on top of step 5's entry-point list as additional edges,
   not a prerequisite for a first working version.

   **Design decision — post-processing pass over an already-built
   `MutableCallGraph`, not a `CallGraphAlgorithm` subclass.** ICC is
   different in kind from steps 1–5: those only ever produce *entry points*
   (roots with no known caller); ICC needs a real edge from a specific
   `startActivity(...)`-shaped call site to a specific target, which means
   operating on the call graph's edges directly. `AbstractCallGraphAlgorithm`
   does expose extension points for this shape of thing
   (`resolveAllImplicitCallsFromSourceMethod`, mirroring how it already
   handles `Thread#start()`→`run()`), but they're `protected` internals of
   the CHA/RTA class hierarchy — subclassing them would tie ICC resolution
   to algorithm internals for no real benefit. `MutableCallGraph` already
   exposes everything needed (`addMethod`, `addCall`,
   `getMethodSignatures()`) as public API, and `CallGraphAlgorithm.addClass`
   already establishes the precedent that augmenting a finished graph
   after the fact is a normal, first-class operation here — so
   `AndroidIccResolver.addIccEdges(MutableCallGraph, ...)` is a standalone
   pass a caller runs *after* building the graph from steps 1/3/4/5's
   combined entry points, not a new algorithm. It assumes that combination
   already happened: since step 5 unconditionally probes every manifest
   component's lifecycle callbacks regardless of any ICC evidence, a valid
   ICC target is always already a graph node by the time this pass runs —
   so it only ever needs to add the missing *edge*, never re-run CHA/RTA or
   expand a fresh subtree via `addClass`. This made a worklist/fixed-point
   traversal unnecessary: a single pass over `cg.getMethodSignatures()`
   suffices.

   Target resolution is intentionally bounded to a single method body's
   worth of constant tracing (no interprocedural or points-to analysis): for
   each `Intent`-typed local reachable from a call site's first argument,
   scan that same body for a `new Intent(Context, Class)` constructor call
   or `setClass`/`setClassName` (explicit), or `setAction` (implicit,
   matched against manifest `<intent-filter>` actions — categories aren't
   checked, over-approximating the same way steps 3/4 do). An `Intent`
   built across methods, behind a conditional assigning different targets,
   or targeted via `setComponent(ComponentName)` isn't traced — sound but
   incomplete, the same tradeoff made throughout this module. Dynamically
   registered `BroadcastReceiver`s (`registerReceiver(...)`, as opposed to a
   manifest `<receiver>`) aren't modeled at all; only manifest-declared
   components are considered, matching what real ICC actually requires (an
   explicit target that isn't manifest-declared would crash at runtime, so
   it correctly resolves to no edge). **[Implemented in this pass.]**

8. **Async/threading entry points.** `AsyncTask.doInBackground` /
   `onPostExecute`, a `Runnable` passed to `Thread`/`Handler.post`,
   `TimerTask`, service binder callbacks — same shape as step 3, additional
   implicit edges so RTA doesn't classify legitimately-reachable code as
   dead.

   `Thread#start()`→`run()` is deliberately excluded: `sootup.callgraph`
   already resolves it precisely from the actual call site's receiver type
   (`AbstractCallGraphAlgorithm#implicitStartRunCall`). What that can't see
   is everything else that runs a `Runnable` without an explicit `.run()`
   call site in app code — `Handler#post`/`postDelayed`,
   `View#post`/`postDelayed`, `ExecutorService#submit`/`execute` — so, like
   step 3, this takes the broader interface-scan approach rather than
   tracing each API's call sites individually: any app class implementing
   `Runnable` or `Callable` has its `run()`/`call()` treated as reachable,
   full stop, regardless of whether it can be proven to reach one of those
   APIs. `TimerTask` needs no separate handling — it `implements Runnable`
   itself, so a subclass's `run()` override is already caught by the same
   scan.

   `AsyncTask` is a different shape: an abstract *class*, not an interface,
   so membership is checked via `TypeHierarchy#superClassesOf`, not
   `implementedInterfacesOf`. Its callback methods are targeted by their
   *type-erased* signature (e.g. `doInBackground(Object[]):Object`, not
   `doInBackground(Params...):Result`) — that's the actual bytecode
   signature the compiler generates a bridge method for in the subclass
   itself when it declares typed overrides, and using the erased signature
   is what lets `resolveOverride` find that bridge; CHA/RTA then follows the
   bridge's own outgoing call to the typed override normally, no further
   special-casing needed. **[Implemented in this pass.]**

9. **Validation harness.** Regression tests against the existing
   DroidBench-style APKs (`FlowSensitivity1.apk`, `LocationLeak1.apk`,
   `Crypto.apk`) plus small hand-built APKs isolating one feature each
   (manifest-only activity, XML `onClick`, listener, ICC, `AsyncTask`).
   Steps 1/2/5 are covered by tests against the three existing APKs; step 3
   likewise — `LocationLeak1.apk`/`FlowSensitivity1.apk` bundle the
   `android.support.v4`/`v7` compat libraries directly into their own dex,
   which contain real listener implementations, so no hand-built APK was
   needed for step 3 either. Step 4 is the first case that genuinely needed
   a hand-built fixture: none of the three sample APKs use `android:onClick`
   at all (confirmed by scanning them), so `AndroidLayoutParser` is tested
   against a real compiled binary layout XML built with the same `axml`
   library's writer (a genuine round trip through the format, not a
   string/DOM stand-in), and `AndroidLayoutEntryPointCreator`'s wiring is
   tested by declaring a real, dex-declared support-library class as a
   synthetic manifest activity (`AndroidManifest`'s constructor is public
   for exactly this). Step 7 needed real *multi-component* Jimple bodies
   (an ICC call site plus a target with its own lifecycle method), which no
   single-activity sample APK has and no existing APK's dex conveniently
   contains either (unlike step 3's bundled-library luck) — so
   `AndroidIccResolverTest` builds genuine Jimple method bodies directly via
   `JimpleStringAnalysisInputLocation` (`sootup.jimple.frontend`, the same
   mechanism that module tests itself with), giving real parsed Jimple IR
   to trace rather than a hand-rolled stand-in for it. Step 8, like step 3,
   got lucky again: `FlowSensitivity1.apk`'s bundled `android.support`
   compat libraries contain real `Runnable`, `Callable`, and multi-callback
   `AsyncTask` implementations (`ActivityChooserModel$PersistHistoryAsyncTask`,
   `PrintHelperKitkat$2$1`), including a genuine negative case for free — an
   abstract intermediate class (`ModernAsyncTask$WorkerRunnable`) that
   implements `Callable` but never itself overrides `call()`, correctly
   producing no entry point.

   **The five hand-built, feature-isolated fixture APKs are now real,
   compiled APKs — not a workaround.** Producing a genuine `.apk` normally
   needs the Android SDK's own build tooling (`aapt2`, `d8`), which isn't
   installed in this environment (or, presumably, in CI). Rather than settle
   for a weaker synthetic stand-in, `org.smali:smali` — the assembler
   belonging to the same `dexlib2` library `sootup.apk.frontend` already
   depends on to *read* dex — turned out to expose a small, genuinely
   reusable programmatic API (`Smali.assemble(SmaliOptions, List<String>)`,
   compiling hand-written `.smali` source files straight to a `classes.dex`
   byte-for-byte identical in shape to what `d8` would produce). Combined
   with the `axml` writer already used for step 4's manifest/layout
   fixtures, `sootup.apk.frontend.fixture.FixtureApkBuilder` assembles real
   `.smali` sources, encodes a real binary `AndroidManifest.xml` (and
   `res/layout` entries where needed), and zips them into a plain,
   unsigned/unaligned `.apk` — which is all `ApkAnalysisInputLocation` and
   this module's manifest/layout parsers ever need, since they read specific
   known zip entries directly rather than going through the Android
   runtime, the same way the checked-in DroidBench sample APKs are
   consumed. This is the first validation in the whole plan that exercises
   the *complete* real pipeline end to end — dexlib2's dex reader, this
   module's own `instruction/*` Jimple translators, the real AXML
   manifest/layout parsers — rather than a shortcut (`JimpleStringAnalysisInputLocation`
   for step 7, bundled third-party bytecode for steps 3/8).

   That completeness immediately paid for itself: `IccFixtureTest` (real
   compiled `const-class`/`new-instance`/`invoke-direct` bytecode) failed on
   the very first run, exposing a real bug in `AndroidIccResolver` that
   every one of step 7's *own* tests had missed — see the dedicated section
   below.

   `ManifestLifecycleFixtureTest`/`OnClickFixtureTest`/`ListenerFixtureTest`/
   `AsyncTaskFixtureTest` cover the manifest-only-activity+service+receiver,
   `android:onClick`, listener, and `AsyncTask` fixtures respectively — each
   proving its target helper method is unreachable from lifecycle entry
   points alone and reachable once the relevant step's entry points are
   added, against real compiled bytecode. **[Implemented in this pass.]**

10. **Public API.** A single entry point that hides the entry-point
    plumbing — give it an APK + platforms path, get back a `CallGraph`.

    **`AndroidApkAnalysis`, kept in `sootup.apk.frontend`.**
    `sootup.apk.parser`/`sootup.apk.backend` stay empty and unregistered —
    by this point every step's implementation already lives in
    `sootup.apk.frontend` with no natural seam suggesting a split, and
    registering two new Maven modules (new `pom.xml`s, root `pom.xml`
    `<modules>` and license-check-roots entries) would add real overhead
    for no corresponding benefit. `AndroidApkAnalysis.create(apkPath,
    platformsPath)` builds the `JavaView`, parses the manifest, and
    combines every entry-point source (steps 3/4/5/8) into one list, all
    eagerly; a separate `buildCallGraph(CallGraphAlgorithm)` (plus
    `buildCallGraphWithCHA()`/`buildCallGraphWithRTA()` convenience
    wrappers) runs the caller's chosen algorithm over that list and layers
    step 7's ICC edges on top — split out so a caller only wanting the
    manifest or entry points isn't forced into call-graph construction.
    This is deliberately *only* wiring: every underlying piece stays
    independently usable and separately tested, exactly the call sequence
    every fixture test already performed by hand (see any class under
    `sootup.apk.frontend.fixture` for the unwrapped version).

    Step 9's writeup deferred one thing to here: "a combined fixture would
    mainly exercise wiring, better done alongside step 10's public API."
    `CombinedFixtureAnalysisTest` is that fixture — one real, compiled,
    six-class APK exercising steps 3/4/5/7/8 at once, run entirely through
    `AndroidApkAnalysis` (not by calling each step's creator directly),
    checking both the combined entry-point list and the resulting CHA *and*
    RTA call graphs. It passed on the first run. **[Implemented in this
    pass.]**

## Implemented so far: steps 1, 2, 3, 4, 5, 7, 8, 9, 10 — the full plan

New packages under `sootup.apk.frontend`:

- `manifest/` — `AndroidComponentType`, `IntentFilter`, `ManifestComponent`,
  `AndroidManifest`, `AndroidManifestParser` (step 1).
- `entrypoint/` — `LifecycleMethod`, `AndroidEntryPointConstants` (step 2),
  `AndroidEntryPointCreator` (step 5), `AndroidCallbackConstants`,
  `AndroidCallbackEntryPointCreator` (step 3), `AndroidLayoutEntryPointCreator`
  (step 4), `AndroidAsyncConstants`, `AndroidAsyncEntryPointCreator` (step 8).
- `layout/` — `AndroidLayoutParser` (step 4).
- `icc/` — `AndroidIccResolver` (step 7).
- (module root) — `AndroidApkAnalysis` (step 10), alongside the pre-existing
  `ApkAnalysisInputLocation`/`DexBodyInterceptors`.

Test-only additions for step 9 (`src/test/java/sootup/apk/frontend/fixture/`):
`FixtureApkBuilder` (the smali+axml→real-`.apk` builder),
`ManifestLifecycleFixtureTest`, `OnClickFixtureTest`, `ListenerFixtureTest`,
`IccFixtureTest`, `AsyncTaskFixtureTest`, and (step 10)
`CombinedFixtureAnalysisTest`. `ApkTestContext` (used by every other test in
this module) is now `public` so the `fixture` package can reuse its
view-construction logic via a new `forApkPath(Path)` overload (the original
`forApk(String)` — for the checked-in resource APKs — now just delegates to
it).

Step 9 also added two `pom.xml` dependencies: `org.smali:smali` (test
scope, `sootup.apk.frontend` only — the assembler; version-matched to the
`dexlib2` version already pinned in the root `pom.xml`'s
`dependencyManagement`, where the version now also lives).

### Bug found by step 9: `AndroidIccResolver` never worked against real dex

Every one of step 7's own tests passed, yet `IccFixtureTest` — the first
*real, compiled* multi-component APK step 7 was ever run against — failed
on the first attempt. The cause: `AndroidIccResolver`'s constant tracing
originally checked whether an invoke's argument *was itself* a
`ClassConstant`/`StringConstant`. That's true of hand-written Jimple text
(step 7's own tests write `specialinvoke $i0.<Intent: void
<init>(Context,Class)>(this, class "LTargetActivity;")`, inlining the
constant directly as an argument, which is valid Jimple syntax) — but it
can never be true of Jimple translated from real dex bytecode: Dalvik
invoke instructions only take register operands, so `const-class`/
`const-string` always load into a register (→ a Jimple local) *first*, via
a separate assignment statement, and the invoke references that local, not
the constant. Real dex-derived Jimple for `new Intent(this,
Target.class)` looks like:

```
$u1 = class "Ltest/fixture/icc/TargetActivity;"
specialinvoke $u0.<android.content.Intent: void <init>(Context,Class)>(this, $u1)
```

— never a constant inlined as the argument. `AndroidIccResolver` now does
a first pass over each method body building `Local → constant value` maps
(`collectConstantLocals`), and every place that reads a `ClassConstant`/
`StringConstant` argument (`classArg`, `firstArgAsString`,
`lastArgAsString`) resolves *through* those maps when the argument is a
`Local`, not just when it's a constant literal. The five step 7 tests in
`AndroidIccResolverTest` still pass unchanged (inlined constants are still
handled, just no longer the *only* case), and `IccFixtureTest` now passes
against real bytecode too.

The lesson generalizes: **every prior step's tests that used
`JimpleStringAnalysisInputLocation` or hand-picked existing bytecode were
real, but none of them exercised the actual dex→Jimple translation for a
*newly constructed* scenario** — steps 3/8's tests read pre-existing
bundled bytecode (already real), and step 7's tests wrote Jimple text
directly (bypassing translation entirely). Step 9 is the first place a
scenario was both *newly authored* and *run through the real dex
translator*, which is exactly the combination that caught this.

Step 7 also changed `sootup.apk.frontend`'s `pom.xml`: `sootup.callgraph` is
now a normal compile dependency (it was test-scoped before, since steps 1–5
only ever produced `MethodSignature` lists — plain `sootup.core` types —
and left actually calling `CallGraphAlgorithm.initialize(...)` to the
caller/tests). `AndroidIccResolver` is the first piece of this module that
has to reference `MutableCallGraph` itself, in main sources. A new
`sootup.jimple.frontend` test-scope dependency was added for
`AndroidIccResolverTest`'s Jimple-string fixtures (see step 9 above).

### Correctness fix found while building step 3: the app/library class boundary

Step 5's `resolveOverride` originally stopped walking a component's
superclass chain on `SootClass#isLibraryClass()`. Building step 3 (which
needs the same "is this an app class" boundary, this time to decide *which*
classes to scan at all rather than where to stop) surfaced that
`isLibraryClass()` isn't a safe signal here: it reflects the `SourceType`
reported by a class's `AnalysisInputLocation`, and this module's tests (and
presumably any caller following the same pattern) add the platform jar via
`JavaClassPathAnalysisInputLocation`'s single-argument constructor, which
defaults to `SourceType.Application` — the same source type
`ApkAnalysisInputLocation` reports for the APK's own dex classes. A scratch
scan confirmed `isLibraryClass()` returning `false` for `android.jar`
classes (and even bundled `junit`/JDK stub classes) in this setup, which
would have made step 3's "scan all app classes" sweep in the entirety of
android.jar, and would have let step 5 mistake a framework base class's own
default implementation (e.g. `Activity#onCreate`) for an app override once
a manifest component didn't override it directly (this happened not to bite
any of the three existing sample APKs, since each declares exactly one
activity that overrides `onCreate` itself — so the walk never needed to
reach the framework boundary to find a match).

Fixed by adding `ApkAnalysisInputLocation#getApplicationClassNames()` — the
fully qualified names of classes actually declared in the APK's dex, which
is what "app class" should mean regardless of how `SourceType` was
configured on the classpath locations added alongside it. Both
`AndroidEntryPointCreator.resolveOverride` (step 5, now `public` — steps
3/4/7/8 all need "find the app override of this callback starting from a
given class," just with a different starting class and callback each time)
and `AndroidCallbackEntryPointCreator`/`AndroidLayoutEntryPointCreator`/
`AndroidIccResolver`/`AndroidAsyncEntryPointCreator` take this set
explicitly rather than consulting `isLibraryClass()`.

### Post-completion fix: the same `SourceType` boundary bug, a third time — this time actually bloating the call graph

The app/library boundary bug above was fixed everywhere *this module's own
code* checked it (`resolveOverride` and friends, via
`getApplicationClassNames()`). It was never fixed at the *source* —
`AndroidApkAnalysis.create()` was still handing `JavaClassPathAnalysisInputLocation`
the platform jar via the single-argument constructor, so `isLibraryClass()`
was still `false` for every android.jar class. This doesn't affect this
module's own entry-point logic (which stopped consulting
`isLibraryClass()` back at step 3), but `sootup.callgraph`'s own
`DefaultCallResolver` — the thing that decides whether to expand a
method's own outgoing calls at all — relies on exactly that flag: `if
(sourceMethod`'s declaring class `isLibraryClass()`) don't resolve any
calls from it`. With android.jar misclassified as non-library, CHA/RTA
happily expanded *into* android.jar's own method bodies and kept going
recursively, pulling a large, uncontrolled slice of the framework's own
internal call graph into the result — this is what a user noticed
directly ("the call graph contains all methods from android.jar").

Fixed with one line: `AndroidApkAnalysis.create()` now constructs the
platform jar's input location with `SourceType.Library` explicitly. The
direct edge from an app method into whatever framework API it calls is
unaffected (`VirtualCallResolver.all()`, which governs whether a resolved
*target* is admitted, doesn't consult `isLibraryClass()` at all) — only
the recursive expansion *into* that framework method's own body stops.

A related, second bug surfaced while measuring the first fix's effect: the
size reduction from the `SourceType` fix alone was smaller than expected.
`buildCallGraphWithRTA()` seeded `RapidTypeAnalysisAlgorithm`'s
instantiated-types set with `view.getClasses()` — every class in the
*entire* view, app and android.jar alike (copied from this module's
pre-existing `CallGraphTest`, without questioning it at the time). RTA
isn't a static filter over a fixed universe, though — it *discovers*
instantiated types as it explores reachable code and uses the set to prune
virtual-dispatch candidates down to types actually seen being `new`'d.
Seeding it with literally every android.jar class up front means every
subtype of a common framework base type (every `View` subclass the SDK
ships, for instance) is "instantiated" from the start — RTA degenerates
into CHA-equivalence for any dispatch through such a type, silently
losing the precision RTA exists to provide. Fixed by seeding
`buildCallGraphWithRTA()` with only the APK's own app classes: the
framework instantiates manifest components and callback classes itself
via `new`s that never appear in the app's own bytecode for RTA to
discover, so those need seeding explicitly, while every other real
instantiation — including of framework types the app itself constructs,
e.g. `new ArrayList()` — is picked up organically as RTA explores from the
entry points.

Measured across the full public DroidBench corpus (188 real APKs, all 17
categories — see the validation note two sections up): the `SourceType`
fix alone reduced total CHA graph size from 177,386 to 172,420 methods
(888,583 → 796,836 calls) — a real but modest reduction, because
android.jar in the `android-platforms` jars used here is largely a stub
jar with little of its own body to recursively expand into. The RTA
seeding fix mattered far more: before it, RTA was essentially
CHA-equivalent in size; after it, RTA totals 122,329 methods and 497,165
calls against CHA's 172,420/796,836 on the same corpus — RTA now does
real, additional work over CHA, which it wasn't before. `CallGraphTest`
(pre-existing, not part of this plan) still constructs RTA the old,
unfixed way and its hardcoded expected-count assertions still pass — it
wasn't touched, since changing its seeding could invalidate counts
verified against that specific pattern.

### Post-completion fix: component constructors were never entry points

Found via the user's own differential Soot-vs-SootUp call graph comparison
on DroidBench's `Button1` (a real, independent methodology this plan's own
DroidBench sweep couldn't have caught, since the sweep only checks that
nothing crashes and counts look sane — it has no ground truth to diff
against). Soot/FlowDroid's dummy-main explicitly calls `new
Button1()`/`Button1.<init>()` before calling `onCreate`; SootUp's
entry-point list had no equivalent — `Button1.<init>()` was a real method
in the dex (confirmed directly against the actual downloaded `Button1.apk`,
not just the current DroidBench source tree, which turned out to have
drifted from what the prebuilt APK actually contains — see below) but
never became a call-graph node or root at all, because nothing calls it:
not app code (the OS instantiates the component reflectively), and not
this module's entry-point model (which only ever added *lifecycle
methods*, never the implicit `new` that precedes them).

Fixed by adding each manifest component's own no-arg constructor as an
entry point in `AndroidEntryPointCreator.collectEntryPoints`, alongside its
lifecycle methods. Unlike lifecycle methods this isn't `resolveOverride`'d
up the superclass chain: Android always instantiates the exact declared
component class, and javac always emits a default no-arg constructor on it
even when the source never wrote one, so a direct lookup on the component's
own class is correct and sufficient. This is conceptually the same class of
gap as static initializers (`<clinit>`), which `sootup.callgraph` already
handles automatically for every entry point's declaring class — instance
constructors just have no such generic handling, since outside Android
nothing calls `new X()` "implicitly" from outside the program for
`sootup.callgraph` to reason about.

Also worth recording as a methodology note: chasing this down required
fetching DroidBench's actual `Button1.java`/manifest/layout source from
GitHub to understand what the app *should* do, but the current source tree
didn't have the `setDataIntent(Intent)` method the comparison report showed
as "only in Soot" at all — the checked-in prebuilt `Button1.apk` predates
a source refactor. The real ground truth turned out to be the downloaded
APK's actual bytecode (inspected directly through this module's own dex
pipeline), not the current source tree — worth remembering for any future
Soot-diff finding that references a method name that doesn't appear where
expected in DroidBench's current source.

Known limitations carried forward deliberately (not silent gaps — flagged
for later steps):

- No resourceId-based fallback for obfuscated manifests (the existing
  `AndroidVersionInfo` has this for `uses-sdk` only); component `android:name`
  is read by attribute name only.
- `exported` defaults to "true iff an `<intent-filter>` is present, else
  false" per pre-API-31 Android semantics; the API-31+ "must be explicit
  when intent filters are present" enforcement isn't modeled (harmless here
  since it doesn't affect entry-point generation, only manifest validity).
- Step 3 covers UI listener interfaces only (`View.OnClickListener` and
  friends); `Runnable`/`AsyncTask`/`Handler` callbacks are step 8's job, not
  step 3's, to avoid overlapping with `sootup.callgraph`'s existing
  `Thread#start()`→`run()` implicit-call handling. (Now implemented — see
  step 8 below.)
- Step 4 extracts `android:onClick` method names but can't map a layout to
  the specific activity that inflates it (no `resources.arsc`/`R.layout.*`
  resolution), so it checks every name against every declared activity —
  sound but imprecise, same tradeoff as step 3.
- Step 7's `Intent` target tracing is a single whole-method-body scan, not
  interprocedural or control-flow-sensitive: an `Intent` built in a helper
  method, assigned a different target down different branches, or targeted
  via `setComponent(ComponentName)` resolves to no edge rather than a wrong
  one. Only manifest-declared components are considered — dynamically
  registered `BroadcastReceiver`s (`registerReceiver(...)`) aren't modeled
  (a natural step 3-shaped follow-up: scanning for `registerReceiver` call
  sites the way step 3 scans for listener interfaces, rather than the
  manifest). `ContentProvider` URI-based ICC isn't modeled at all. `Intent`
  category matching is skipped (any action match is treated as a match).
  `AndroidIccResolver` also assumes it runs *after* a graph is already built
  from steps 1/3/4/5's combined entry points — run on a CHA/RTA graph built
  from step 5 alone, an ICC target that only step 3/4 would have added as a
  node won't get its own subtree expanded, though the edge to it is still
  added. (This entry originally also said constant tracing only handled a
  constant inlined directly as an invoke argument — step 9's real-bytecode
  fixture caught that this is never how real dex-derived Jimple looks;
  fixed, see the dedicated writeup above. It also originally listed
  dynamically registered `BroadcastReceiver`s as unmodeled entirely — fixed
  by `AndroidDynamicReceiverEntryPointCreator`, see the dedicated writeup
  near the end of this document; `IntentFilter`-based *targeting* of such a
  receiver is still unmodeled, as noted there.)
- Step 8's `Runnable`/`Callable`/`AsyncTask` scan (like step 3's listener
  scan) doesn't trace the specific call site that hands an instance to
  `Handler.post`/`View.post`/an executor/a registration API — it's still a
  blanket "does this class implement/extend the right thing" scan, now
  restricted to classes actually instantiated in reachable code (see the
  instantiated-type-filtering writeup below), not to classes actually
  *passed to* one of those APIs. A call-site-specific version (tracing the
  argument the way step 7 traces `Intent` targets) would be more precise
  but was judged not worth the added complexity given the broader scan is
  already sound once restricted to instantiated types.

### Post-completion fix: steps 3/8 tightened with instantiated-type filtering

Found the same way as the constructor-entry-point fix above: a differential
Soot-vs-SootUp comparison on the user's own `Button1.apk` (via their
separate `CallGraphComparison` project), which turned out to be a
different, larger APK than DroidBench's minimal GitHub sample — it bundles
the full `android.support.v4`/`v7` libraries. Soot/FlowDroid produced 19
nodes/23 edges; SootUp produced 657 nodes/1298 edges, almost entirely
`android.support.*` internals no path from the app's own code ever
reaches.

Root cause: steps 3 and 8 scan *every app class* (i.e. every class in the
dex, including all of a bundled library's own internal machinery) for one
implementing a known listener interface or extending `AsyncTask`/
`Runnable`/`Callable`, with no check for whether anything ever actually
constructs an instance of it. A library can ship dozens of listener/task
implementations for its own internal use that a given app never
exercises; each admitted candidate's CHA transitive closure can pull in
large swaths of otherwise-untouched library code.

Fixed with a two-phase entry-point computation
(`AndroidApkAnalysis.collectEntryPoints`, and mirrored for tests in
`ApkTestContext.instantiatedClassNamesFromCoreEntryPoints`):

1. **Phase 1 — core entry points.** Manifest lifecycle callbacks (step 5)
   and `android:onClick` targets (step 4) don't have this problem: the OS
   instantiates manifest components itself, and an `android:onClick`
   target is a method on an already-known component class, not a class
   discovered by scanning for interfaces. Expand these with CHA plus
   step 7's ICC edges to get a reachable-code baseline.
2. **Phase 2 — filtered discovery.** Scan that baseline
   (`InstantiatedTypeCollector`, reusing `sootup.callgraph`'s own
   `InstantiateClassValueVisitor` pattern-matcher) for every class `new`'d
   in any reachable method body, then run steps 3/8's candidate scan
   restricted to classes in that set.

This is a sound restriction, not a heuristic: a listener/task class can
only ever run if something constructs and hands off a live instance of it
to a registration API, so "never `new`'d in reachable code" is a valid
precondition for "can never actually fire" — not an approximation traded
for precision.

Deliberately not a fixed point: a class instantiated only inside another
class that phase 2 itself just discovered (chaining two levels deep) won't
be picked up. Same bounded-limitation shape as step 7's single-pass ICC
resolution above, and flagged the same way rather than solved here.

Validated two ways:

- **DroidBench sweep** (all 188 APKs, same methodology as the earlier
  `SourceType`/RTA-seeding fix): CHA totals dropped from 172,420 methods /
  796,836 calls to 94,723 methods / 467,389 calls (both ~45%/41% lower);
  RTA totals dropped from 122,329 methods / 497,165 calls to 67,222
  methods / 297,562 calls (~45%/40% lower). All 188 APKs still analyze
  without error.
- **The user's actual `Button1.apk`** (the support-library-bundling one
  that originally motivated this fix): CHA output went from 657 nodes /
  1298 edges to 14 nodes / 13 edges — in the same ballpark as Soot/
  FlowDroid's 19 nodes / 23 edges, rather than ~30x larger. All four of
  `Button1`'s own app-class nodes (`<init>`, `<clinit>`, `onCreate`,
  `sendMessage`) are present, resolving the original "application class
  nodes are not appearing in SootUp" complaint. The remaining gap to
  Soot/FlowDroid's count is attributable to FlowDroid's synthetic
  `dummyMain` entry-point method and its `setDataIntent(Intent)`-style
  lifecycle modeling, which this module doesn't attempt to replicate —
  out of scope for this fix.

### Post-completion addition: a real synthesized `dummyMain` method, after all

Step 5's original design decision explicitly chose *not* to build a
synthetic dummy-main body, on the grounds that `CallGraphAlgorithm.initialize`
already treats a flat `List<MethodSignature>` as independent roots, making a
single-root wrapper method call-graph-equivalent to the flat list — real
engineering cost (hand-building `Body`/`Stmt`/`Local`/CFG objects and an
`AnalysisInputLocation` to host a synthetic class) for no reachability
benefit. That reasoning still holds for reachability alone. It stops holding
the moment a caller downstream of the call graph is built around the
classic single-entry-point convention instead — e.g. a taint/dataflow
analysis that expects to seed its own supergraph from one concrete method
with a real body, the shape FlowDroid and most Soot-based tooling assume.
Rather than push every such caller to re-derive a synthetic root from
`getEntryPoints()` itself, `AndroidDummyMainFactory` now builds one, as an
additive option alongside the flat-list path (which remains the default and
is unchanged) — exactly the "can be added later" escape hatch step 5's
original writeup left open.

**`AndroidDummyMainFactory.createDummyMainClass(view, entryPoints)`** builds
a synthetic `sootup.apk.frontend.dummyMain.AndroidDummyMain#dummyMain()`
class/method with a real Jimple `Body`: a straight-line sequence (no
loops/conditionals — still not modeling the OS's unpredictable invocation
order, since nothing downstream needs statement-order sensitivity for
reachability) that calls every entry point in the combined list exactly
once. One receiver local is allocated per distinct declaring class (via a
Jimple `new` plus a `specialinvoke` of that class's own `<init>` entry point
when present in the list) and reused across every other entry point
declared on the same class, matching how the OS really does invoke
lifecycle callbacks repeatedly on the one instance it constructed. Static
entry points get a plain static invoke; instance entry points get a virtual
or interface invoke depending on whether the declaring class resolves as an
interface. Every parameter gets a placeholder immediate (`null` for
reference/array types, zero for primitives) — the body is only ever
traversed statically by call-graph construction, never executed, so actual
argument values don't matter, only well-typed arity.

`AndroidApkAnalysis.create()` now builds this class unconditionally and
registers it via `view.addClass(...)` (the view is now a `MutableJavaView`,
not a plain `JavaView`, to support this), exposing its signature through
the new `getDummyMainSignature()`. Two new call-graph builders root at it
instead of the flat list: `buildCallGraphFromDummyMain(CallGraphAlgorithm)`
and its CHA/RTA convenience wrappers
(`buildCallGraphFromDummyMainWithCHA()`/`buildCallGraphFromDummyMainWithRTA()`,
seeded the same way as their flat-list counterparts). Both paths still layer
step 7's ICC edges on top afterward. The original flat-list
`buildCallGraph`/`buildCallGraphWithCHA`/`buildCallGraphWithRTA` are
unchanged and remain the default — this is purely additive.

Validated by `CombinedFixtureAnalysisTest` against the same six-class
combined fixture APK used for step 10's own validation: one test asserts
the dummy main's body contains a call to every entry point and nothing
else, each exactly once; a second asserts `buildCallGraphFromDummyMainWithCHA()`
still reaches step 5/3/8's helper methods (one extra hop through the dummy
main, in place of being a direct root) — structurally equivalent to the
flat-list version of the same check.

Known limitation: like the flat entry-point list itself, the dummy main is
built once, eagerly, in `create()` — it reflects whatever `getEntryPoints()`
returned at that time. It is not usable as a general-purpose synthesis
target for entry points discovered by a caller after the fact (e.g. if a
future ICC or async fix runs a second pass and adds more entry points, the
already-built dummy main's body won't include them).

### Post-completion addition: dynamically-registered `BroadcastReceiver`s

Step 7's known-limitations list flagged this from the start: "Dynamically
registered `BroadcastReceiver`s (`registerReceiver(...)`, as opposed to a
manifest `<receiver>`) aren't modeled at all; only manifest-declared
components are considered." A receiver an app builds and hands to
`Context#registerReceiver(BroadcastReceiver, IntentFilter)` at runtime has
no `<receiver>` element for step 5's manifest walk to find at all, so its
`onReceive` was invisible to every existing entry-point source.

**`AndroidDynamicReceiverEntryPointCreator`**, new in `entrypoint/`, closes
this the same way steps 3 and 8 already close their own equivalent gaps —
deliberately *not* the "trace the `registerReceiver` call site" approach
step 7's limitations note first suggested, since that's exactly the
call-site-specific precision steps 3/8 already considered and declined in
favor of a blanket scan (see their class docs): any app class extending
`android.content.BroadcastReceiver` (a superclass relationship, checked via
`TypeHierarchy#superClassesOf` the same way step 8 checks for `AsyncTask`,
not `implementedInterfacesOf`) has its `onReceive(Context, Intent)` treated
as reachable, restricted to classes actually instantiated in already-
reachable code (`InstantiatedTypeCollector`, the same non-heuristic filter
steps 3/8 use, for the same reason: a receiver can only ever be registered
if something first constructs a live instance of it). The lifecycle-method
table itself isn't duplicated — it reuses
`AndroidEntryPointConstants.getLifecycleMethods(BROADCAST_RECEIVER)`, the
same table step 5 already uses for manifest-declared receivers.

A manifest-declared `<receiver>` also extends `BroadcastReceiver`, so this
scan harmlessly rediscovers it whenever it's instantiated in reachable
code too — `AndroidApkAnalysis`'s combined entry-point set is already
deduplicated (a `LinkedHashSet`), so this doesn't produce duplicate work.
The two sources stay conceptually distinct: a manifest receiver's
constructor is added as an entry point unconditionally by step 5 (the OS
instantiates it reflectively — no in-app construction site to find), while
this creator is the *only* source for a receiver with no manifest
declaration at all.

Wired into `AndroidApkAnalysis.collectEntryPoints`'s phase 2 alongside
steps 3/8, using the same phase-1 instantiated-type baseline.

Validated by `AndroidDynamicReceiverEntryPointCreatorTest` (existing sample
APKs: `Crypto.apk` has no `BroadcastReceiver` subclass at all, a
deduplication check against `FlowSensitivity1.apk`) and a new
`sootup.apk.frontend.fixture.DynamicReceiverFixtureTest` — like step 8's
`AsyncTaskFixtureTest`, no checked-in sample APK happens to bundle a
library with an unregistered receiver subclass, so this needed a real
hand-built, compiled fixture: an `Activity` that constructs a receiver
subclass in `onCreate` (standing in for the real `registerReceiver(...)`
call site, which — matching this creator's design — is never itself
traced) with no manifest `<receiver>` declaration anywhere, proving its
`onReceive` (and a helper it calls) is unreachable from lifecycle entry
points alone and reachable once this creator's entry points are added.

Known limitation carried forward, same shape as steps 3/8: this remains a
blanket "does this class extend the right thing" scan, not a trace of the
specific `registerReceiver` call site — so, like an unregistered listener
class, a `BroadcastReceiver` subclass that is constructed but genuinely
never registered anywhere is still (soundly, over-approximately) treated
as reachable once instantiated. `IntentFilter`-based *targeting* of a
dynamically registered receiver (matching an implicit `Intent`'s action
against the filter passed to `registerReceiver`, the runtime dynamic-
receiver analogue of step 7's manifest `<intent-filter>` matching) is not
attempted — out of scope here, unchanged from step 7's own known
limitations.
