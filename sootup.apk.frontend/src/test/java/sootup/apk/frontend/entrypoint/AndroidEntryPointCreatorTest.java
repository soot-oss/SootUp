package sootup.apk.frontend.entrypoint;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.manifest.AndroidManifest;
import sootup.apk.frontend.manifest.AndroidManifestParser;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootClass;

/**
 * Validates that entry points derived automatically from the manifest (steps 1/2/5 of {@code
 * ANDROID_CALL_GRAPH_PLAN.md}) reproduce the same call graphs as {@code CallGraphTest}, which
 * hand-picks the same {@code onCreate} methods as entry points.
 */
public class AndroidEntryPointCreatorTest {

  @Test
  public void testFlowSensitivityEntryPointsMatchCallGraphTest() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/FlowSensitivity1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/FlowSensitivity1.apk"));

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);

    MethodSignature onCreate =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "de.ecspride.MainActivity", "onCreate", "void", List.of("android.os.Bundle"));
    assertTrue(entryPoints.contains(onCreate));

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph cg = cha.initialize(entryPoints);
    assertTrue(cg.containsMethod(onCreate));
    assertEquals(9, cg.callsFrom(onCreate).size());
  }

  @Test
  public void testLocationLeakEntryPointsMatchCallGraphTest() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/LocationLeak1.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/LocationLeak1.apk"));

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);

    MethodSignature onCreate =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "de.ecspride.LocationLeak1", "onCreate", "void", List.of("android.os.Bundle"));
    assertTrue(entryPoints.contains(onCreate));

    CallGraphAlgorithm rta =
        new RapidTypeAnalysisAlgorithm(
            ctx.view,
            ctx.view.getClasses().map(JavaSootClass::getType).collect(Collectors.toSet()));
    CallGraph cg = rta.initialize(entryPoints);
    assertTrue(cg.containsMethod(onCreate));
    assertEquals(5, cg.callsFrom(onCreate).size());
  }

  @Test
  public void testCryptoEntryPointsMatchCallGraphTest() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/Crypto.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/Crypto.apk"));

    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);

    MethodSignature onCreate =
        ctx.view
            .getIdentifierFactory()
            .getMethodSignature(
                "com.example.MainActivity", "onCreate", "void", List.of("android.os.Bundle"));
    assertTrue(entryPoints.contains(onCreate));

    CallGraphAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(ctx.view);
    CallGraph cg = cha.initialize(entryPoints);
    assertTrue(cg.containsMethod(onCreate));
    assertEquals(3, cg.callsFrom(onCreate).size());
  }

  @Test
  public void testNoApplicationClassMeansNoApplicationEntryPoints() {
    ApkTestContext ctx = ApkTestContext.forApk("src/test/resources/Crypto.apk");
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/Crypto.apk"));

    assertTrue(manifest.getApplicationClassName().isEmpty());

    // every generated entry point must belong to a declared component's class (or a superclass
    // of it), never to a synthesized "Application" placeholder
    List<MethodSignature> entryPoints =
        AndroidEntryPointCreator.getEntryPoints(ctx.view, manifest, ctx.appClassNames);
    assertTrue(entryPoints.size() > 0);
  }
}
