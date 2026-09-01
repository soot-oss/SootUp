package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.entrypoint.AndroidAsyncEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidCallbackEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidDummyMainFactory;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.entrypoint.AndroidLayoutEntryPointCreator;
import sootup.apk.frontend.entrypoint.InstantiatedTypeCollector;
import sootup.apk.frontend.icc.AndroidIccResolver;
import sootup.apk.frontend.layout.AndroidLayoutParser;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.MutableCallGraph;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;
import sootup.java.core.views.MutableJavaView;

/**
 * The public entry point for this module (step 10 of {@code ANDROID_CALL_GRAPH_PLAN.md}): give it
 * an APK and an Android platforms directory, get back everything steps 1–9 compute — the parsed
 * manifest, the resolved {@link JavaView}, the combined entry-point list, and a full {@link
 * CallGraph} with ICC edges already added — without needing to know that plumbing exists.
 *
 * <p>This wraps exactly the call sequence every one of this module's own tests already performs by
 * hand (see any class under {@code sootup.apk.frontend.fixture} for the unwrapped version):
 *
 * <ol>
 *   <li>Resolve the APK's target SDK version ({@link AndroidVersionInfo}) to pick a matching {@code
 *       android.jar}, and build a {@link JavaView} over the APK's dex plus that platform jar.
 *   <li>Parse {@code AndroidManifest.xml} (step 1) and collect the APK's dex-declared class names
 *       (the reliable app/platform boundary the rest of this module's entry-point resolution
 *       depends on — see {@link ApkAnalysisInputLocation#getApplicationClassNames()}).
 *   <li>Combine every entry-point source: manifest lifecycle callbacks (step 5), listener/callback
 *       interface implementations (step 3), {@code android:onClick} targets (step 4), and
 *       async/threading constructs (step 8).
 *   <li>Run a caller-chosen {@link CallGraphAlgorithm} (CHA or RTA) over that combined list, then
 *       layer step 7's ICC edges on top of the resulting graph.
 * </ol>
 *
 * <p>Each underlying piece stays independently usable and separately tested — this class adds no
 * new analysis logic of its own, only wiring. A caller who needs finer control (a different
 * android.jar per component, a custom {@link CallGraphAlgorithm}, only some of the entry-point
 * sources) should use the individual creators directly instead of this façade.
 */
public final class AndroidApkAnalysis {

  @NonNull private final MutableJavaView view;
  @NonNull private final AndroidManifest manifest;
  @NonNull private final Set<String> applicationClassNames;
  @NonNull private final List<MethodSignature> entryPoints;
  @NonNull private final MethodSignature dummyMainSignature;

  private AndroidApkAnalysis(
      @NonNull MutableJavaView view,
      @NonNull AndroidManifest manifest,
      @NonNull Set<String> applicationClassNames,
      @NonNull List<MethodSignature> entryPoints,
      @NonNull MethodSignature dummyMainSignature) {
    this.view = view;
    this.manifest = manifest;
    this.applicationClassNames = applicationClassNames;
    this.entryPoints = entryPoints;
    this.dummyMainSignature = dummyMainSignature;
  }

  /**
   * Analyzes the given APK: builds the {@link JavaView}, parses the manifest, and resolves the
   * combined entry-point list. Building a {@link CallGraph} from the result is a separate step
   * ({@link #buildCallGraphWithCHA()}/{@link #buildCallGraphWithRTA()}/{@link
   * #buildCallGraph(CallGraphAlgorithm)}) so a caller only interested in the manifest or entry
   * points isn't forced to pay for call-graph construction too.
   *
   * @param apkPath path to the APK file to analyze
   * @param androidPlatformsPath path to a directory of {@code android-<api>/android.jar} platform
   *     jars (see {@link AndroidVersionInfo} for where to obtain one)
   */
  @NonNull
  public static AndroidApkAnalysis create(
      @NonNull Path apkPath, @NonNull String androidPlatformsPath) {
    AndroidVersionInfo versionInfo = new AndroidVersionInfo(apkPath, androidPlatformsPath);

    ApkAnalysisInputLocation apkInputLocation =
        new ApkAnalysisInputLocation(
            apkPath, versionInfo, DexBodyInterceptors.Default.bodyInterceptors());
    JavaClassPathAnalysisInputLocation classPathInputLocation =
        new JavaClassPathAnalysisInputLocation(
            androidPlatformsPath
                + File.separator
                + "android-"
                + versionInfo.getApi_version()
                + File.separator
                + "android.jar",
            SourceType.Library);
    MutableJavaView view = new MutableJavaView(List.of(apkInputLocation, classPathInputLocation));

    AndroidManifest manifest = AndroidManifestParser.parseFromApk(apkPath);
    Set<String> applicationClassNames = apkInputLocation.getApplicationClassNames();
    List<MethodSignature> entryPoints =
        collectEntryPoints(apkPath, view, manifest, applicationClassNames);

    JavaSootClass dummyMainClass = AndroidDummyMainFactory.createDummyMainClass(view, entryPoints);
    view.addClass(dummyMainClass);
    MethodSignature dummyMainSignature =
        AndroidDummyMainFactory.getDummyMainSignature(view.getIdentifierFactory());

    return new AndroidApkAnalysis(
        view, manifest, applicationClassNames, entryPoints, dummyMainSignature);
  }

  /**
   * Computed in two phases so steps 3/8's blanket "implements a known interface" scan can be
   * restricted to classes with actual evidence of use, rather than pulling in every listener/task
   * implementation anywhere in a bundled library's own internal machinery regardless of whether the
   * app's code ever exercises it (confirmed directly against a real DroidBench sample bundling the
   * support library: without this, one small single-activity app's call graph ballooned to 657
   * methods, almost entirely library-internal code no path from the app's own logic ever reaches).
   *
   * <ul>
   *   <li><b>Phase 1 — core entry points.</b> Manifest lifecycle callbacks (step 5) and {@code
   *       android:onClick} targets (step 4) don't have this problem: the OS instantiates manifest
   *       components itself, and an {@code android:onClick} target is a method on an already-known
   *       component class, not a class discovered by scanning for interfaces. Expand these with CHA
   *       plus ICC edges (step 7) to get a reachable-code baseline.
   *   <li><b>Phase 2 — filtered discovery.</b> Scan that baseline for instantiated types ({@link
   *       InstantiatedTypeCollector}), then run steps 3/8's candidate scan restricted to classes in
   *       that set — a listener/task class can only ever run if something constructs and hands off
   *       a live instance of it, so "never `new`'d in reachable code" is a sound, not just
   *       plausible, precondition for "can never actually fire".
   * </ul>
   *
   * <p>Deliberately not a fixed point: a class instantiated only inside another class that phase 2
   * itself just discovered (chaining two levels deep) won't be picked up. Documented as a known,
   * bounded limitation in {@code ANDROID_CALL_GRAPH_PLAN.md} rather than solved here, the same way
   * step 7's ICC resolution bounds itself to a single pass.
   */
  @NonNull
  private static List<MethodSignature> collectEntryPoints(
      @NonNull Path apkPath,
      @NonNull JavaView view,
      @NonNull AndroidManifest manifest,
      @NonNull Set<String> applicationClassNames) {
    List<MethodSignature> coreEntryPoints = new ArrayList<>();
    coreEntryPoints.addAll(
        AndroidEntryPointCreator.getEntryPoints(view, manifest, applicationClassNames));
    Set<String> onClickMethodNames = AndroidLayoutParser.parseOnClickMethodNamesFromApk(apkPath);
    coreEntryPoints.addAll(
        AndroidLayoutEntryPointCreator.getOnClickEntryPoints(
            view, manifest, applicationClassNames, onClickMethodNames));

    MutableCallGraph coreGraph =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(coreEntryPoints);
    AndroidIccResolver.addIccEdges(coreGraph, view, manifest, applicationClassNames);
    Set<String> instantiatedClassNames =
        InstantiatedTypeCollector.collectInstantiatedClassNames(view, coreGraph);

    Set<MethodSignature> combined = new LinkedHashSet<>(coreEntryPoints);
    combined.addAll(
        AndroidCallbackEntryPointCreator.getCallbackEntryPoints(
            view, applicationClassNames, instantiatedClassNames));
    combined.addAll(
        AndroidAsyncEntryPointCreator.getAsyncEntryPoints(
            view, applicationClassNames, instantiatedClassNames));

    return new ArrayList<>(combined);
  }

  @NonNull
  public JavaView getView() {
    return view;
  }

  @NonNull
  public AndroidManifest getManifest() {
    return manifest;
  }

  /** The fully qualified names of classes actually declared in the APK's dex. */
  @NonNull
  public Set<String> getApplicationClassNames() {
    return applicationClassNames;
  }

  /** The combined entry points from steps 3/4/5/8 — every root this façade knows how to derive. */
  @NonNull
  public List<MethodSignature> getEntryPoints() {
    return entryPoints;
  }

  /**
   * The signature of the synthetic {@code dummyMain} method ({@link AndroidDummyMainFactory})
   * already registered in {@link #getView()}: a real method with a real Jimple body that
   * unconditionally calls every entry point from {@link #getEntryPoints()} once. For callers built
   * around the classic single-entry-point convention (e.g. a taint analysis that expects one
   * concrete method with a body to seed its own supergraph) rather than {@code
   * CallGraphAlgorithm.initialize(List)}'s flat list.
   */
  @NonNull
  public MethodSignature getDummyMainSignature() {
    return dummyMainSignature;
  }

  /**
   * Runs {@code algorithm} over this analysis's entry points, then adds step 7's ICC edges on top
   * of the result.
   *
   * @throws IllegalStateException if {@code algorithm} doesn't produce a {@link MutableCallGraph}
   *     (every {@code sootup.callgraph} algorithm shipped in this codebase does; this only guards
   *     against a hypothetical third-party {@link CallGraphAlgorithm} that doesn't)
   */
  @NonNull
  public CallGraph buildCallGraph(@NonNull CallGraphAlgorithm algorithm) {
    CallGraph callGraph = algorithm.initialize(entryPoints);
    if (!(callGraph instanceof MutableCallGraph)) {
      throw new IllegalStateException(
          "Cannot add ICC edges: "
              + algorithm.getClass().getName()
              + " produced a "
              + callGraph.getClass().getName()
              + ", which isn't a MutableCallGraph. Both ClassHierarchyAnalysisAlgorithm and "
              + "RapidTypeAnalysisAlgorithm satisfy this.");
    }
    MutableCallGraph mutableCallGraph = (MutableCallGraph) callGraph;
    AndroidIccResolver.addIccEdges(mutableCallGraph, view, manifest, applicationClassNames);
    return mutableCallGraph;
  }

  /** Convenience for {@code buildCallGraph(new ClassHierarchyAnalysisAlgorithm(getView()))}. */
  @NonNull
  public CallGraph buildCallGraphWithCHA() {
    return buildCallGraph(new ClassHierarchyAnalysisAlgorithm(view));
  }

  /**
   * Like {@link #buildCallGraph(CallGraphAlgorithm)}, but rooted at {@link #getDummyMainSignature()}
   * alone instead of the flat {@link #getEntryPoints()} list. Structurally equivalent (every entry
   * point is one call away from the dummy main, which itself calls each exactly once) — use this
   * only when a caller specifically needs the single-root shape, e.g. to hand {@link
   * #getDummyMainSignature()}'s method to an analysis built around that convention.
   */
  @NonNull
  public CallGraph buildCallGraphFromDummyMain(@NonNull CallGraphAlgorithm algorithm) {
    CallGraph callGraph = algorithm.initialize(List.of(dummyMainSignature));
    if (!(callGraph instanceof MutableCallGraph)) {
      throw new IllegalStateException(
          "Cannot add ICC edges: "
              + algorithm.getClass().getName()
              + " produced a "
              + callGraph.getClass().getName()
              + ", which isn't a MutableCallGraph. Both ClassHierarchyAnalysisAlgorithm and "
              + "RapidTypeAnalysisAlgorithm satisfy this.");
    }
    MutableCallGraph mutableCallGraph = (MutableCallGraph) callGraph;
    AndroidIccResolver.addIccEdges(mutableCallGraph, view, manifest, applicationClassNames);
    return mutableCallGraph;
  }

  /** Convenience for {@code buildCallGraphFromDummyMain(new ClassHierarchyAnalysisAlgorithm(getView()))}. */
  @NonNull
  public CallGraph buildCallGraphFromDummyMainWithCHA() {
    return buildCallGraphFromDummyMain(new ClassHierarchyAnalysisAlgorithm(view));
  }

  /**
   * Convenience for RTA, seeded with every <em>app</em> class (not android.jar) as a
   * potentially-instantiated type.
   *
   * <p>{@link RapidTypeAnalysisAlgorithm} isn't a static filter over a fixed universe — it
   * discovers instantiated types as it explores reachable code (see its {@code
   * instantiatedClasses.add(...)} during worklist processing) and uses the set to prune virtual
   * dispatch candidates down to types actually seen being {@code new}'d. Seeding it with every
   * class in the *entire* view (as an earlier version of this method did, and as this module's
   * pre-existing {@code CallGraphTest} still does) means every android.jar subtype of a common
   * framework base type (e.g. every {@code View} subclass the SDK ships) is considered
   * "instantiated" from the start — for any virtual call through that base type, RTA then behaves
   * exactly like CHA, silently losing the precision RTA exists to provide.
   *
   * <p>App classes are seeded explicitly because the framework instantiates manifest components
   * (and framework-invoked callback classes — listeners, {@code AsyncTask}s) itself, via {@code
   * new}s that never appear in the app's own bytecode for RTA to discover; every other real
   * instantiation (including of framework types the app itself {@code new}s, e.g. {@code new
   * ArrayList()}) is picked up organically as RTA explores from the entry points. A caller wanting
   * a different seed should build {@link RapidTypeAnalysisAlgorithm} directly and pass it to {@link
   * #buildCallGraph(CallGraphAlgorithm)} instead.
   */
  @NonNull
  public CallGraph buildCallGraphWithRTA() {
    return buildCallGraph(new RapidTypeAnalysisAlgorithm(view, applicationClassTypes()));
  }

  /**
   * Convenience for {@code buildCallGraphFromDummyMain(new RapidTypeAnalysisAlgorithm(getView(),
   * ...))}, seeded the same way as {@link #buildCallGraphWithRTA()}.
   */
  @NonNull
  public CallGraph buildCallGraphFromDummyMainWithRTA() {
    return buildCallGraphFromDummyMain(new RapidTypeAnalysisAlgorithm(view, applicationClassTypes()));
  }

  @NonNull
  private Set<ClassType> applicationClassTypes() {
    return applicationClassNames.stream()
        .map(name -> view.getIdentifierFactory().getClassType(name))
        .collect(Collectors.toSet());
  }
}
