/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.soot.jimple.common.expr;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.AbstractViewResident;
import de.upb.soot.core.IMethod;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class AbstractInvokeExpr extends AbstractViewResident implements Expr {
  /**
   * 
   */
  private static final long serialVersionUID = 1796920588315752175L;
  protected MethodSignature method;
  protected final ValueBox[] argBoxes;

  protected AbstractInvokeExpr(IView view, MethodSignature method, ValueBox[] argBoxes) {
    super(view);
    this.method = method;
    this.argBoxes = argBoxes.length == 0 ? null : argBoxes;
  }

  public Optional<SootMethod> getMethod() {
    JavaClassSignature signature = method.declClassSignature;
    Optional<AbstractClass> op = this.getView().getClass(signature);
    if (op.isPresent()) {
      AbstractClass klass = op.get();
      Optional<? extends IMethod> m = klass.getMethod(method);
      return m.map(c -> (SootMethod) c);
    }
    return Optional.empty();
  }

  @Override
  public abstract Object clone();

  public Value getArg(int index) {
    return argBoxes[index].getValue();
  }

  /**
   * Returns a list of arguments, consisting of values contained in the box.
   */
  public List<Value> getArgs() {
    List<Value> l = new ArrayList<>();
    if (argBoxes != null) {
      for (ValueBox element : argBoxes) {
        l.add(element.getValue());
      }
    }
    return l;
  }

  public int getArgCount() {
    return argBoxes == null ? 0 : argBoxes.length;
  }

  public void setArg(int index, Value arg) {
    argBoxes[index].setValue(arg);
  }

  public ValueBox getArgBox(int index) {
    return argBoxes[index];
  }

  @Override
  public Type getType() {
    return this.getView().getType(method.typeSignature);
  }

  @Override
  public List<ValueBox> getUseBoxes() {
    if (argBoxes == null) {
      return Collections.emptyList();
    }
    List<ValueBox> list = new ArrayList<>();
    Collections.addAll(list, argBoxes);
    for (ValueBox element : argBoxes) {
      list.addAll(element.getValue().getUseBoxes());
    }
    return list;
  }

  protected void argBoxesToString(StringBuilder builder) {
    if (argBoxes != null) {
      final int len = argBoxes.length;
      if (0 < len) {
        builder.append(argBoxes[0].getValue().toString());
        for (int i = 1; i < len; i++) {
          builder.append(", ");
          builder.append(argBoxes[i].getValue().toString());
        }
      }
    }
  }

  protected void argBoxesToPrinter(IStmtPrinter up) {
    if (argBoxes != null) {
      final int len = argBoxes.length;
      if (0 < len) {
        argBoxes[0].toString(up);
        for (int i = 1; i < argBoxes.length; i++) {
          up.literal(", ");
          argBoxes[i].toString(up);
        }
      }
    }
  }

}
