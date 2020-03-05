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
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;

/** Interface for different methods of printing out a Stmt. */
public interface StmtPrinter {
  void startStmt(Stmt u);

  void endStmt(Stmt u);

  void startStmtBox(StmtBox u);

  void endStmtBox(StmtBox u);

  void startValueBox(ValueBox u);

  void endValueBox(ValueBox u);

  void setIndent(int offset);

  void incIndent();

  void decIndent();

  void noIndent();

  void literal(String s);

  void newline();

  void local(Local jimpleLocal);

  void typeSignature(Type t);

  void methodSignature(MethodSignature sig);

  void method(SootMethod m);

  void constant(Constant c);

  void field(SootField f);

  void fieldSignature(FieldSignature fieldSig);

  void stmtRef(Stmt u, boolean branchTarget);

  void identityRef(IdentityRef r);

  void modifier(String toString);
}
