# Android APK Call Graph — Implementation Plan

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
   producing no entry point. The one remaining hand-built fixture (a real
   compiled multi-component sample APK) stays deferred — steps 1–5/7/8 are
   already validated individually; a combined fixture would mainly exercise
   wiring, better done alongside step 10's public API.

10. **Public API.** A single entry point (e.g. `AndroidCallGraphAlgorithm`)
    that hides the entry-point plumbing — give it an APK + platforms path,
    get back a `CallGraph`. Decide then whether to populate
    `sootup.apk.parser`/`sootup.apk.backend` for this (registering them in
    the root `pom.xml`) or keep everything inside `sootup.apk.frontend`.
    *(Not yet implemented — steps 1/2/3/4/5/7/8 are usable directly via
    `AndroidManifestParser` + `AndroidEntryPointCreator` +
    `AndroidCallbackEntryPointCreator` + `AndroidLayoutEntryPointCreator` +
    `AndroidAsyncEntryPointCreator` + `AndroidIccResolver` in the
    meantime.)*

## Implemented so far: steps 1, 2, 3, 4, 5, 7, 8

New packages under `sootup.apk.frontend`:

- `manifest/` — `AndroidComponentType`, `IntentFilter`, `ManifestComponent`,
  `AndroidManifest`, `AndroidManifestParser` (step 1).
- `entrypoint/` — `LifecycleMethod`, `AndroidEntryPointConstants` (step 2),
  `AndroidEntryPointCreator` (step 5), `AndroidCallbackConstants`,
  `AndroidCallbackEntryPointCreator` (step 3), `AndroidLayoutEntryPointCreator`
  (step 4), `AndroidAsyncConstants`, `AndroidAsyncEntryPointCreator` (step 8).
- `layout/` — `AndroidLayoutParser` (step 4).
- `icc/` — `AndroidIccResolver` (step 7).

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
  added.
- Step 8's `Runnable`/`Callable` scan is deliberately unscoped: it treats
  every implementation as reachable regardless of whether it's actually
  ever passed to `Handler.post`/`View.post`/an executor, or whether it's
  already reachable some other way (e.g. constructed and `.run()`-called
  directly in app code, which CHA would already find). This trades
  precision for simplicity and matches the plan's own framing of step 8 as
  "the same shape as step 3." A call-site-specific version (tracing the
  argument to `Handler.post` the way step 7 traces `Intent` targets) would
  be more precise but was judged not worth the added complexity given the
  broader scan is already sound.
