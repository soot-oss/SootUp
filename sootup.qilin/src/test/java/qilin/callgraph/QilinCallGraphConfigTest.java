/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.callgraph;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import qilin.core.config.ContextSensitivity;
import qilin.test.util.QilinFrameworkTests;
import qilin.util.ViewFactory;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.reflection.ReflectionModel;
import sootup.callgraph.reflection.TamiflexReflectionModel;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * End-to-end test of {@code
 * sootup.callgraph.CallGraphConfig.builder()....into(QilinCallGraphConfig::from)} - the
 * Qilin-specific stage of the unified staged {@link CallGraphConfig} builder chain.
 */
public class QilinCallGraphConfigTest extends QilinFrameworkTests {

  @Test
  public void testIntoQilinStageBuildsCallGraph() {
    View view = ViewFactory.createView(appPath, null);
    ClassType mainClassType =
        view.getIdentifierFactory().getClassType("qilin.microben.core.clinit.ClinitStaticLoad");

    CallGraph cg =
        CallGraphConfig.builder()
            .view(view)
            .into(QilinCallGraphConfig::from)
            .mainClass(mainClassType)
            .pointerAnalysisConfig(configBuilder(ContextSensitivity.insensitive()))
            .build()
            .computeCallGraph();

    assertTrue(cg.getMethodSignatures().size() > 0, "the built call graph must not be empty");
  }

  @Test
  public void testMissingMainClassThrows() {
    View view = ViewFactory.createView(appPath, null);
    assertThrows(
        IllegalStateException.class,
        () ->
            CallGraphConfig.builder()
                .view(view)
                .entryPoints(Collections.emptyList())
                .into(QilinCallGraphConfig::from)
                .build());
  }

  /** Reflection model set on the common stage reaches qilin's PAG. */
  @Test
  public void testCommonReflectionModelResolvesMethodInvoke() {
    View view = ViewFactory.createView(appPath, null);
    String cls = "qilin.microben.core.reflog.MethodInvoke";
    ClassType mainClassType = view.getIdentifierFactory().getClassType(cls);
    MethodSignature main =
        view.getIdentifierFactory()
            .getMethodSignature(cls, "main", "void", List.of("java.lang.String[]"));
    MethodSignature target =
        view.getIdentifierFactory()
            .getMethodSignature(
                cls + "$MethodInvokeInstance",
                "id",
                "java.lang.Object",
                List.of("java.lang.Object"));

    CallGraph without = qilinCallGraph(view, mainClassType, ReflectionModel.none());
    assertFalse(without.callTargetsFrom(main).contains(target));

    CallGraph with =
        qilinCallGraph(
            view,
            mainClassType,
            new TamiflexReflectionModel(view, refLogPath + File.separator + "Reflection.log"));
    assertTrue(with.callTargetsFrom(main).contains(target));
  }

  private CallGraph qilinCallGraph(View view, ClassType mainClass, ReflectionModel model) {
    return CallGraphConfig.builder()
        .view(view)
        .reflectionModel(model)
        .into(QilinCallGraphConfig::from)
        .mainClass(mainClass)
        // no log path: reflection comes only from the common stage
        .pointerAnalysisConfig(
            configBuilder(ContextSensitivity.insensitive()).reflectionLogPath(null))
        .build()
        .computeCallGraph();
  }
}
