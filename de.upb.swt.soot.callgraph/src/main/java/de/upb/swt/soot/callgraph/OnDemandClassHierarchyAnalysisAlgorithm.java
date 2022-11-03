package de.upb.swt.soot.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann, Ben Hermann
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

import com.google.common.annotations.Beta;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.typerhierachy.TypeHierarchy;
import de.upb.swt.soot.core.views.View;
import java.util.List;
import javax.annotation.Nonnull;

@Beta
public class OnDemandClassHierarchyAnalysisAlgorithm extends ClassHierarchyAnalysisAlgorithm {

  public OnDemandClassHierarchyAnalysisAlgorithm(
      View<? extends SootClass<?>> view, TypeHierarchy hierarchy) {
    super(view, hierarchy);
  }

  @Nonnull
  @Override
  // FIXME: [ms] nonnull returns null
  public CallGraph initialize(@Nonnull List<MethodSignature> entryPoints) {
    return null;
  }
}