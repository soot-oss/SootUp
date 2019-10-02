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
import de.upb.swt.soot.core.types.Type;
import java.util.HashSet;

/** Partial default StmtPrinter implementation. */
public abstract class AbstractStmtPrinter implements StmtPrinter {

  protected boolean startOfLine = true;
  protected String indent = "        ";
  protected StringBuilder output = new StringBuilder();
  protected HashSet<String> quotableLocals;

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
  public void incIndent() {
    indent = indent + "    ";
  }

  @Override
  public void decIndent() {
    if (indent.length() >= 4) {
      indent = indent.substring(4);
    }
  }

  @Override
  public void setIndent(String indent) {
    this.indent = indent;
  }

  @Override
  public String getIndent() {
    return indent;
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
    String ret = output.toString();
    output = new StringBuilder();
    return ret;
  }

  @Override
  public StringBuilder output() {
    return output;
  }

  protected void handleIndent() {
    if (startOfLine) {
      output.append(indent);
    }
    startOfLine = false;
  }
}
