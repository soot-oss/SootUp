/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Ondrej Lhotak
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
package de.upb.swt.soot.core.util.printer;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtBox;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import java.util.HashMap;
import java.util.Map;

/** Partial default StmtPrinter implementation. */
public abstract class AbstractStmtPrinter implements StmtPrinter {

  protected boolean startOfLine = true;

  protected final char indentChar = '\u0020';
  protected final int indentStep = 4;
  protected int indent = 0;

  protected StringBuilder output = new StringBuilder();
  private HashMap<String, PackageName> imports = new HashMap<>();

  boolean useImports = false;

  void enableImports(boolean enable) {
    useImports = enable;
  }

  /**
   * * addImport keeps track of imported Packages/Classes
   *
   * @return whether this ClassName does not collide with another ClassName from a different package
   *     that was already added
   */
  public boolean addImport(Type referencedImport) {
    if (referencedImport instanceof ClassType) {
      final String referencedClassName = ((ClassType) referencedImport).getClassName();
      final PackageName referencedPackageName = ((ClassType) referencedImport).getPackageName();
      // handle ClassName/import collisions
      final PackageName packageName = imports.get(referencedClassName);
      if (packageName == null) {
        imports.put(referencedClassName, referencedPackageName);
        return true;
      } else if (packageName.equals(referencedPackageName)) {
        return true;
      }
    }
    return false;
  }

  public String type(Type type) {
    if (useImports) {
      if (type instanceof ClassType) {
        if (addImport(type)) {
          return ((ClassType) type).getClassName();
        }
      } else if (type instanceof ArrayType) {
        Type baseType = ((ArrayType) type).getBaseType();
        if (baseType instanceof ClassType && addImport(baseType)) {
          return ((ClassType) baseType).getClassName();
        }
      }
    }
    return type.toString();
  }

  public Map<String, PackageName> getImports() {
    return imports;
  }

  @Override
  public void startStmt(Stmt u) {
    handleIndent();
  }

  @Override
  public void endStmt(Stmt u) {}

  @Override
  public void startStmtBox(StmtBox ub) {
    handleIndent();
  }

  @Override
  public void endStmtBox(StmtBox ub) {}

  @Override
  public void startValueBox(ValueBox vb) {
    handleIndent();
  }

  @Override
  public void endValueBox(ValueBox vb) {}

  @Override
  public void noIndent() {
    startOfLine = false;
  }

  @Override
  public void setIndent(int offset) {
    indent += offset;
  }

  @Override
  public void incIndent() {
    indent += indentStep;
  }

  @Override
  public void decIndent() {
    indent -= indentStep;
  }

  @Override
  public abstract void literal(String s);

  @Override
  public abstract void typeSignature(Type t);

  @Override
  public abstract void method(SootMethod m);

  @Override
  public abstract void field(SootField f);

  @Override
  public abstract void identityRef(IdentityRef r);

  @Override
  public abstract void stmtRef(Stmt u, boolean branchTarget);

  @Override
  public void newline() {
    output.append("\n");
    startOfLine = true;
  }

  @Override
  public void local(Local l) {
    handleIndent();
    output.append(l.getName());
  }

  @Override
  public void constant(Constant c) {
    handleIndent();
    output.append(c.toString());
  }

  @Override
  public String toString() {
    return output.toString();
  }

  @Override
  public StringBuilder output() {
    return output;
  }

  protected void handleIndent() {
    if (startOfLine) {
      for (int i = indent; i > 0; i--) {
        output.append(indentChar);
      }
    }
    startOfLine = false;
  }
}
