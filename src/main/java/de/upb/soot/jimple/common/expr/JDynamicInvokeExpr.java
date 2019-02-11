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
import de.upb.soot.core.IMethod;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.visitor.IExprVisitor;
import de.upb.soot.jimple.visitor.IVisitor;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.util.printer.IStmtPrinter;
import de.upb.soot.views.IView;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JDynamicInvokeExpr extends AbstractInvokeExpr {
  /**
   * 
   */
  private static final long serialVersionUID = 8212277443400470834L;
  protected MethodSignature bsm;
  protected ValueBox[] bsmArgBoxes;
  protected int tag;

  /**
   * Assigns values returned by newImmediateBox to an array bsmArgBoxes of type ValueBox.
   */
  public JDynamicInvokeExpr(IView view, MethodSignature bootstrapMethodRef, List<? extends Value> bootstrapArgs,
      MethodSignature methodRef, int tag, List<? extends Value> methodArgs) {
    super(view, methodRef, new ValueBox[methodArgs.size()]);
    if (!methodRef.toString().startsWith("<" + SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME + ": ")) {
      throw new IllegalArgumentException(
          "Receiver type of JDynamicInvokeExpr must be " + SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME + "!");
    }
    this.bsm = bootstrapMethodRef;
    this.bsmArgBoxes = new ValueBox[bootstrapArgs.size()];
    this.tag = tag;

    for (int i = 0; i < bootstrapArgs.size(); i++) {
      this.bsmArgBoxes[i] = Jimple.newImmediateBox(bootstrapArgs.get(i));
    }
    for (int i = 0; i < methodArgs.size(); i++) {
      this.argBoxes[i] = Jimple.newImmediateBox(methodArgs.get(i));
    }
  }

  /**
   * Makes a parameterized call to JDynamicInvokeExpr method.
   */
  public JDynamicInvokeExpr(IView view, MethodSignature bootstrapMethodRef, List<? extends Value> bootstrapArgs,
      MethodSignature methodRef, List<? extends Value> methodArgs) {
    /*
     * Here the static-handle is chosen as default value, because this works for Java.
     */
    this(view, bootstrapMethodRef, bootstrapArgs, methodRef, Opcodes.H_INVOKESTATIC, methodArgs);
  }

  public int getBootstrapArgCount() {
    return bsmArgBoxes.length;
  }

  public Value getBootstrapArg(int index) {
    return bsmArgBoxes[index].getValue();
  }

  @Override
  public Object clone() {
    List<Value> clonedBsmArgs = new ArrayList<>(getBootstrapArgCount());
    for (int i = 0; i < getBootstrapArgCount(); i++) {
      clonedBsmArgs.add(i, getBootstrapArg(i));
    }

    List<Value> clonedArgs = new ArrayList<>(getArgCount());
    for (int i = 0; i < getArgCount(); i++) {
      clonedArgs.add(i, getArg(i));
    }

    return new JDynamicInvokeExpr(this.getView(), bsm, clonedBsmArgs, method, tag, clonedArgs);
  }

  @Override
  public boolean equivTo(Object o) {
    if (o instanceof JDynamicInvokeExpr) {
      JDynamicInvokeExpr ie = (JDynamicInvokeExpr) o;
      if (!(getMethod().equals(ie.getMethod()) && bsmArgBoxes.length == ie.bsmArgBoxes.length)) {
        return false;
      }
      int i = 0;
      for (ValueBox element : bsmArgBoxes) {
        if (!(element.getValue().equivTo(ie.getBootstrapArg(i)))) {
          return false;
        }
        i++;
      }
      if (!(getMethod().equals(ie.getMethod())
          && (argBoxes == null ? 0 : argBoxes.length) == (ie.argBoxes == null ? 0 : ie.argBoxes.length))) {
        return false;
      }
      if (argBoxes != null) {
        i = 0;
        for (ValueBox element : argBoxes) {
          if (!(element.getValue().equivTo(ie.getArg(i)))) {
            return false;
          }
          i++;
        }
      }
      if (!method.equals(ie.method)) {
        return false;
      }
      return bsm.equals(ie.bsm);
    }
    return false;
  }

  public Optional<SootMethod> getBootstrapMethod() {
    JavaClassSignature signature = bsm.declClassSignature;
    Optional<AbstractClass> op = this.getView().getClass(signature);
    if (op.isPresent()) {
      AbstractClass klass = op.get();
      Optional<? extends IMethod> m = klass.getMethod(bsm);
      return m.map(c -> (SootMethod) c);
    }
    return Optional.empty();
  }

  /**
   * Returns a hash code for this object, consistent with structural equality.
   */
  @Override
  public int equivHashCode() {
    return bsm.hashCode() * getMethod().hashCode() * 17;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(Jimple.DYNAMICINVOKE);
    buffer.append(" \"");
    buffer.append(method); // quoted method name (can be any UTF8
    // string)
    buffer.append("\" <");
    buffer.append(method.getSubSignature());
    buffer.append(">(");

    argBoxesToString(buffer);

    buffer.append(") ");
    buffer.append(bsm);
    buffer.append("(");
    final int len = bsmArgBoxes.length;
    if (0 < len) {
      buffer.append(bsmArgBoxes[0].getValue().toString());
      for (int i = 1; i < len; i++) {
        buffer.append(", ");
        buffer.append(bsmArgBoxes[i].getValue().toString());
      }
    }
    buffer.append(")");

    return buffer.toString();
  }

  @Override
  public void toString(IStmtPrinter up) {
    up.literal(Jimple.DYNAMICINVOKE);
    up.literal(" \"" + method.name + "\" <" + method.getSubSignature() + ">(");

    argBoxesToPrinter(up);

    up.literal(") ");
    Optional<SootMethod> op = getBootstrapMethod();
      op.ifPresent(up::method);
    up.literal("(");
    final int len = bsmArgBoxes.length;
    if (0 < len) {
      bsmArgBoxes[0].toString(up);
      for (int i = 1; i < len; i++) {
        up.literal(", ");
        bsmArgBoxes[i].toString(up);
      }
    }
    up.literal(")");
  }

  @Override
  public void accept(IVisitor sw) {
    ((IExprVisitor) sw).caseDynamicInvokeExpr(this);
  }

  /**
   * Returns a list containing elements of type ValueBox.
   */
  public List<Value> getBootstrapArgs() {
    List<Value> l = new ArrayList<>();
    for (ValueBox element : bsmArgBoxes) {
      l.add(element.getValue());
    }

    return l;
  }

  public int getHandleTag() {
    return tag;
  }

  @Override
  public boolean equivTo(Object o, Comparator comparator) {
    return comparator.compare(this, o) == 0;
  }

}
