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

package qilin.test.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.config.ContextSensitivity;
import qilin.core.config.PointerAnalysisConfig;
import qilin.test.util.QilinFrameworkTests;
import qilin.util.PTAUtils;
import sootup.callgraph.scope.SuppressClinitCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * Verifies that {@link PointerAnalysisConfig#getClinitVirtualCallResolver()} actually gates
 * on-the-fly {@code <clinit>} admission in {@code Solver#processStmts}, using the same {@code
 * <clinit>} trigger (a static field read) as {@link ClinitTests#testClinitStaticLoad()}.
 */
public class ClinitHandlingTests extends QilinFrameworkTests {

  private static final String MAIN_CLASS = "qilin.microben.core.clinit.ClinitStaticLoad";
  private static final String TRIGGERED_CLASS = "qilin.microben.core.clinit.ClinitStaticLoad$A";

  private PTA run(VirtualCallResolver clinitVirtualCallResolver) {
    View view = PTAUtils.createView(appPath, null);
    ClassType mainClassType = view.getIdentifierFactory().getClassType(MAIN_CLASS);
    PointerAnalysisConfig config =
        PointerAnalysisConfig.builder()
            .contextSensitivity(ContextSensitivity.insensitive())
            .singleEntry(true)
            .clinitVirtualCallResolver(clinitVirtualCallResolver)
            .build();
    PTA pta = PointerAnalysisFactory.create(view, mainClassType, config);
    pta.run();
    return pta;
  }

  private boolean triggeredClinitIsReachable(PTA pta) {
    MethodSignature triggeredClinit =
        pta.getView()
            .getIdentifierFactory()
            .getStaticInitializerSignature(
                pta.getView().getIdentifierFactory().getClassType(TRIGGERED_CLASS));
    Set<MethodSignature> reachable =
        pta.getNakedReachableMethods().stream()
            .map(SootMethod::getSignature)
            .collect(Collectors.toSet());
    return reachable.contains(triggeredClinit);
  }

  @Test
  public void testAdmitAllResolverStillModelsDiscoveredClinit() {
    PTA pta = run(VirtualCallResolver.all());
    assertTrue(
        triggeredClinitIsReachable(pta),
        "the default admit-all resolver must not change today's ON_THE_FLY-equivalent behavior");
  }

  @Test
  public void testSuppressClinitCallResolverDropsDiscoveredClinit() {
    PTA pta = run(new SuppressClinitCallResolver(PTAUtils.createView(appPath, null)));
    assertFalse(
        triggeredClinitIsReachable(pta),
        "a suppressing resolver must prevent Solver#processStmts from injecting the <clinit> edge");
  }
}
