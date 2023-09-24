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

package qilin.core.pag;

import java.util.Optional;
import qilin.CoreConfig;
import qilin.core.PTAScene;
import qilin.core.context.ContextElement;
import soot.util.Numberable;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.views.View;

/**
 * Represents an allocation site node in the pointer assignment graph.
 *
 * @author Ondrej Lhotak
 */
public class AllocNode extends Node implements ContextElement, Numberable {
  protected Object newExpr;
  private final SootMethod method;

  public AllocNode(Object newExpr, Type t, SootMethod m) {
    super(t);
    this.method = m;
    if (t instanceof ClassType rt) {
      View view = PTAScene.v().getView();
      Optional<SootClass> osc = view.getClass(rt);
      if (osc.isPresent() && osc.get().isAbstract()) {
        boolean usesReflectionLog = CoreConfig.v().getAppConfig().REFLECTION_LOG != null;
        if (!usesReflectionLog) {
          throw new RuntimeException("Attempt to create allocnode with abstract type " + t);
        }
      }
    }
    this.newExpr = newExpr;
  }

  /** Returns the new expression of this allocation site. */
  public Object getNewExpr() {
    return newExpr;
  }

  public String toString() {
    return "AllocNode " + getNumber() + " " + newExpr + " in method " + method;
  }

  public String toString2() {
    return newExpr + " in method " + method;
  }

  public SootMethod getMethod() {
    return method;
  }

  public AllocNode base() {
    return this;
  }
}
