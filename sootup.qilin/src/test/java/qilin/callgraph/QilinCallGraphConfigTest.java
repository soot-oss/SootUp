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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import qilin.core.config.ContextSensitivity;
import qilin.test.util.QilinFrameworkTests;
import qilin.util.PTAUtils;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
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
    View view = PTAUtils.createView(appPath, null);
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
    View view = PTAUtils.createView(appPath, null);
    assertThrows(
        IllegalStateException.class,
        () ->
            CallGraphConfig.builder()
                .view(view)
                .entryPoints(Collections.emptyList())
                .into(QilinCallGraphConfig::from)
                .build());
  }
}
