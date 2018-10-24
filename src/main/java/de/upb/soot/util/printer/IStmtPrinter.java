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
package de.upb.soot.util.printer;

import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.ref.IdentityRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.signatures.MethodSignature;


/**
 * Interface for different methods of printing out a IStmt.
 */
public interface IStmtPrinter {
  public void startStmt(IStmt u);

  public void endStmt(IStmt u);

  public void startStmtBox(IStmtBox u);

  public void endStmtBox(IStmtBox u);

  public void startValueBox(ValueBox u);

  public void endValueBox(ValueBox u);

  public void incIndent();

  public void decIndent();

  public void noIndent();

  public void setIndent(String newIndent);

  public String getIndent();

  public void literal(String s);

  public void newline();

  public void local(Local jimpleLocal);

  public void type(Type t);

  public void methodSignature(MethodSignature sig);

  public void method(SootMethod m);

  public void constant(Constant c);

  public void field(SootField f);

  public void stmtRef(IStmt u, boolean branchTarget);

  public void identityRef(IdentityRef r);

  public StringBuffer output();
}
