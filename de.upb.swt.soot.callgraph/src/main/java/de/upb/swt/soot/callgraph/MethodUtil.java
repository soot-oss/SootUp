package de.upb.swt.soot.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Kadiray Karakaya and others
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
import de.upb.swt.soot.callgraph.spark.pag.CallGraphEdgeType;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;
import javax.annotation.Nullable;

public class MethodUtil {
  // TODO: replace this with the method in View after merging to develop
  @Nullable
  public static SootMethod methodSignatureToMethod(
      View<? extends SootClass> view, MethodSignature methodSignature) {
    SootMethod currentMethodCandidate =
        view.getClass(methodSignature.getDeclClassType())
            .flatMap(c -> c.getMethod(methodSignature))
            .orElse(null);
    return currentMethodCandidate;
  }

  public static CallGraphEdgeType findCallGraphEdgeType(AbstractInvokeExpr invokeExpr) {
    if (invokeExpr instanceof JVirtualInvokeExpr) {
      return CallGraphEdgeType.VIRTUAL;
    } else if (invokeExpr instanceof JSpecialInvokeExpr) {
      return CallGraphEdgeType.SPECIAL;
    } else if (invokeExpr instanceof JInterfaceInvokeExpr) {
      return CallGraphEdgeType.INTERFACE;
    } else if (invokeExpr instanceof JStaticInvokeExpr) {
      return CallGraphEdgeType.STATIC;
    } else {
      throw new RuntimeException("No such invokeExpr:" + invokeExpr);
    }
  }
}
