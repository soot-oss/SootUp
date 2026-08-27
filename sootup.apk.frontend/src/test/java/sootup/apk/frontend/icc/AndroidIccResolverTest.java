package sootup.apk.frontend.icc;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.entrypoint.AndroidEntryPointCreator;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.IntentFilter;
import sootup.apk.frontend.manifest.ManifestComponent;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.MutableCallGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;
import sootup.jimple.frontend.JimpleStringAnalysisInputLocation;

/**
 * Validates step 7 of {@code ANDROID_CALL_GRAPH_PLAN.md}. None of this module's checked-in sample
 * APKs declare more than one manifest component, so there's no real multi-component ICC call site
 * to scan for. Instead these tests build small, genuine Jimple method bodies directly (via {@link
 * JimpleStringAnalysisInputLocation}, the same mechanism {@code sootup.jimple.frontend} itself
 * tests with) — real parsed Jimple IR exercising the actual constant-tracing logic, not a
 * hand-rolled stand-in for it.
 */
public class AndroidIccResolverTest {

  private static final String MAIN_ACTIVITY_ONCREATE =
      "void onCreate(android.os.Bundle) {\n"
          + "    this := @this: %s;\n"
          + "    b := @parameter0: android.os.Bundle;\n"
          + "%s"
          + "    return;\n"
          + "  }\n";

  private static final String TARGET_ACTIVITY =
      "class TargetActivity extends java.lang.Object {\n"
          + "  void onCreate(android.os.Bundle) {\n"
          + "    this := @this: TargetActivity;\n"
          + "    b := @parameter0: android.os.Bundle;\n"
          + "    return;\n"
          + "  }\n"
          + "}\n";

  private static JavaView viewOf(String... jimpleClasses) {
    List<sootup.core.inputlocation.AnalysisInputLocation> locations = new java.util.ArrayList<>();
    for (String jimpleClass : jimpleClasses) {
      locations.add(new JimpleStringAnalysisInputLocation(jimpleClass));
    }
    return new JavaView(locations);
  }

  private static MethodSignature onCreateOf(View view, String className) {
    return view.getIdentifierFactory()
        .getMethodSignature(className, "onCreate", "void", List.of("android.os.Bundle"));
  }

  @Test
  public void testExplicitIntentAddsEdgeToTargetActivity() {
    String mainActivity =
        "class MainActivity extends java.lang.Object {\n"
            + String.format(
                MAIN_ACTIVITY_ONCREATE,
                "MainActivity",
                "    $i0 = new android.content.Intent;\n"
                    + "    specialinvoke $i0.<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>(this, class \"LTargetActivity;\");\n"
                    + "    virtualinvoke this.<android.app.Activity: void startActivity(android.content.Intent)>($i0);\n")
            + "}\n";

    JavaView view = viewOf(mainActivity, TARGET_ACTIVITY);
    AndroidManifest manifest =
        new AndroidManifest(
            "",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "MainActivity", true, true, List.of()),
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "TargetActivity", true, true, List.of())));
    Set<String> appClassNames = Set.of("MainActivity", "TargetActivity");

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(view, manifest, appClassNames);
    MutableCallGraph cg =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);

    MethodSignature mainOnCreate = onCreateOf(view, "MainActivity");
    MethodSignature targetOnCreate = onCreateOf(view, "TargetActivity");

    // before step 7: both nodes exist (independently, via step 5) but aren't connected, since
    // startActivity can't be resolved by ordinary virtual dispatch (its declaring class,
    // android.app.Activity, isn't part of this minimal view).
    assertTrue(cg.containsMethod(mainOnCreate));
    assertTrue(cg.containsMethod(targetOnCreate));
    assertFalse(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));

    AndroidIccResolver.addIccEdges(cg, view, manifest, appClassNames);

    assertTrue(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));
  }

  @Test
  public void testImplicitIntentMatchesByAction() {
    String mainActivity =
        "class MainActivity extends java.lang.Object {\n"
            + String.format(
                MAIN_ACTIVITY_ONCREATE,
                "MainActivity",
                "    $i0 = new android.content.Intent;\n"
                    + "    specialinvoke $i0.<android.content.Intent: void <init>()>();\n"
                    + "    virtualinvoke $i0.<android.content.Intent: android.content.Intent setAction(java.lang.String)>(\"com.example.CUSTOM_ACTION\");\n"
                    + "    virtualinvoke this.<android.app.Activity: void startActivity(android.content.Intent)>($i0);\n")
            + "}\n";

    JavaView view = viewOf(mainActivity, TARGET_ACTIVITY);
    AndroidManifest manifest =
        new AndroidManifest(
            "",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "MainActivity", true, true, List.of()),
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY,
                    "TargetActivity",
                    true,
                    true,
                    List.of(
                        new IntentFilter(
                            List.of("com.example.CUSTOM_ACTION"), Collections.emptyList())))));
    Set<String> appClassNames = Set.of("MainActivity", "TargetActivity");

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(view, manifest, appClassNames);
    MutableCallGraph cg =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);

    AndroidIccResolver.addIccEdges(cg, view, manifest, appClassNames);

    MethodSignature mainOnCreate = onCreateOf(view, "MainActivity");
    MethodSignature targetOnCreate = onCreateOf(view, "TargetActivity");
    assertTrue(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));
  }

  @Test
  public void testActionWithNoMatchingIntentFilterAddsNoEdge() {
    String mainActivity =
        "class MainActivity extends java.lang.Object {\n"
            + String.format(
                MAIN_ACTIVITY_ONCREATE,
                "MainActivity",
                "    $i0 = new android.content.Intent;\n"
                    + "    specialinvoke $i0.<android.content.Intent: void <init>()>();\n"
                    + "    virtualinvoke $i0.<android.content.Intent: android.content.Intent setAction(java.lang.String)>(\"com.example.UNRELATED_ACTION\");\n"
                    + "    virtualinvoke this.<android.app.Activity: void startActivity(android.content.Intent)>($i0);\n")
            + "}\n";

    JavaView view = viewOf(mainActivity, TARGET_ACTIVITY);
    AndroidManifest manifest =
        new AndroidManifest(
            "",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "MainActivity", true, true, List.of()),
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY,
                    "TargetActivity",
                    true,
                    true,
                    List.of(
                        new IntentFilter(
                            List.of("com.example.CUSTOM_ACTION"), Collections.emptyList())))));
    Set<String> appClassNames = Set.of("MainActivity", "TargetActivity");

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(view, manifest, appClassNames);
    MutableCallGraph cg =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);

    AndroidIccResolver.addIccEdges(cg, view, manifest, appClassNames);

    MethodSignature mainOnCreate = onCreateOf(view, "MainActivity");
    MethodSignature targetOnCreate = onCreateOf(view, "TargetActivity");
    assertFalse(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));
  }

  @Test
  public void testIntentBuiltInHelperMethodIsNotTraced() {
    // The Intent local at the call site comes from another method's return value, not a `new
    // Intent` in the same body - out of scope for this intentionally bounded, whole-body-only
    // scan (documented limitation), so no edge should be added.
    String mainActivity =
        "class MainActivity extends java.lang.Object {\n"
            + "  void onCreate(android.os.Bundle) {\n"
            + "    this := @this: MainActivity;\n"
            + "    b := @parameter0: android.os.Bundle;\n"
            + "    $i0 = virtualinvoke this.<MainActivity: android.content.Intent buildIntent()>();\n"
            + "    virtualinvoke this.<android.app.Activity: void startActivity(android.content.Intent)>($i0);\n"
            + "    return;\n"
            + "  }\n"
            + "  android.content.Intent buildIntent() {\n"
            + "    this := @this: MainActivity;\n"
            + "    $i0 = new android.content.Intent;\n"
            + "    specialinvoke $i0.<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>(this, class \"LTargetActivity;\");\n"
            + "    return $i0;\n"
            + "  }\n"
            + "}\n";

    JavaView view = viewOf(mainActivity, TARGET_ACTIVITY);
    AndroidManifest manifest =
        new AndroidManifest(
            "",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "MainActivity", true, true, List.of()),
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "TargetActivity", true, true, List.of())));
    Set<String> appClassNames = Set.of("MainActivity", "TargetActivity");

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(view, manifest, appClassNames);
    MutableCallGraph cg =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);

    AndroidIccResolver.addIccEdges(cg, view, manifest, appClassNames);

    MethodSignature mainOnCreate = onCreateOf(view, "MainActivity");
    MethodSignature targetOnCreate = onCreateOf(view, "TargetActivity");
    assertFalse(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));
  }

  @Test
  public void testExplicitClassNotDeclaredInManifestAddsNoEdge() {
    // A class that genuinely exists (and is targeted explicitly) but was never declared as a
    // manifest component: Android would refuse to launch it at runtime, so it must not be
    // resolved as an ICC target either.
    String mainActivity =
        "class MainActivity extends java.lang.Object {\n"
            + String.format(
                MAIN_ACTIVITY_ONCREATE,
                "MainActivity",
                "    $i0 = new android.content.Intent;\n"
                    + "    specialinvoke $i0.<android.content.Intent: void <init>(android.content.Context,java.lang.Class)>(this, class \"LTargetActivity;\");\n"
                    + "    virtualinvoke this.<android.app.Activity: void startActivity(android.content.Intent)>($i0);\n")
            + "}\n";

    JavaView view = viewOf(mainActivity, TARGET_ACTIVITY);
    AndroidManifest manifest =
        new AndroidManifest(
            "",
            null,
            List.of(
                new ManifestComponent(
                    AndroidComponentType.ACTIVITY, "MainActivity", true, true, List.of())));
    Set<String> appClassNames = Set.of("MainActivity", "TargetActivity");

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(view, manifest, appClassNames);
    MutableCallGraph cg =
        (MutableCallGraph)
            (CallGraph) new ClassHierarchyAnalysisAlgorithm(view).initialize(entryPoints);

    AndroidIccResolver.addIccEdges(cg, view, manifest, appClassNames);

    MethodSignature mainOnCreate = onCreateOf(view, "MainActivity");
    MethodSignature targetOnCreate = onCreateOf(view, "TargetActivity");
    assertFalse(cg.containsMethod(targetOnCreate));
    assertFalse(cg.callTargetsFrom(mainOnCreate).contains(targetOnCreate));
  }
}
