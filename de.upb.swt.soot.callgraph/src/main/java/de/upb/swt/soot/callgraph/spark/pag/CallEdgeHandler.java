package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.View;
import org.apache.commons.lang3.tuple.Pair;

public class CallEdgeHandler {

  private View<? extends SootClass> view;

  public CallEdgeHandler(View<? extends SootClass> view) {
    this.view = view;
  }

  public boolean passesParameters(Pair<MethodSignature, MethodSignature> edge) {
    MethodSignature target = edge.getValue();
    ClassType ct = target.getDeclClassType();
    SootClass sc = view.getClassOrThrow(ct);
    SootMethod tgt = sc.getMethod(target).get();
    return passesParameters(tgt);
  }

  private boolean passesParameters(SootMethod method) {

    return false;
  }
}
