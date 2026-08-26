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
   not a prerequisite for a first working version. *(Not yet implemented.)*

8. **Async/threading entry points.** `AsyncTask.doInBackground` /
   `onPostExecute`, a `Runnable` passed to `Thread`/`Handler.post`,
   `TimerTask`, service binder callbacks — same shape as step 3, additional
   implicit edges so RTA doesn't classify legitimately-reachable code as
   dead. *(Not yet implemented.)*

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
   for exactly this). The remaining feature-isolated APKs stay deferred
   until steps 7/8 exist.

10. **Public API.** A single entry point (e.g. `AndroidCallGraphAlgorithm`)
    that hides the entry-point plumbing — give it an APK + platforms path,
    get back a `CallGraph`. Decide then whether to populate
    `sootup.apk.parser`/`sootup.apk.backend` for this (registering them in
    the root `pom.xml`) or keep everything inside `sootup.apk.frontend`.
    *(Not yet implemented — steps 1/2/5 are usable directly via
    `AndroidManifestParser` + `AndroidEntryPointCreator` in the meantime.)*

## Implemented so far: steps 1, 2, 3, 4, 5

New packages under `sootup.apk.frontend`:

- `manifest/` — `AndroidComponentType`, `IntentFilter`, `ManifestComponent`,
  `AndroidManifest`, `AndroidManifestParser` (step 1).
- `entrypoint/` — `LifecycleMethod`, `AndroidEntryPointConstants` (step 2),
  `AndroidEntryPointCreator` (step 5), `AndroidCallbackConstants`,
  `AndroidCallbackEntryPointCreator` (step 3), `AndroidLayoutEntryPointCreator`
  (step 4).
- `layout/` — `AndroidLayoutParser` (step 4).

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
`AndroidEntryPointCreator.resolveOverride` (step 5, now package-visible and
reused by steps 3 and 4) and `AndroidCallbackEntryPointCreator`/
`AndroidLayoutEntryPointCreator` take this set explicitly rather than
consulting `isLibraryClass()`.

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
  `Thread#start()`→`run()` implicit-call handling.
- Step 4 extracts `android:onClick` method names but can't map a layout to
  the specific activity that inflates it (no `resources.arsc`/`R.layout.*`
  resolution), so it checks every name against every declared activity —
  sound but imprecise, same tradeoff as step 3.
- No ICC, no async/threading entry points yet — steps 7, 8 above.
