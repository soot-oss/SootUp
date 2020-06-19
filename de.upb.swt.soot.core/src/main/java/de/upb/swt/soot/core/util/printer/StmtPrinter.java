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
import de.upb.swt.soot.core.jimple.common.constant.Constant;
import de.upb.swt.soot.core.jimple.common.ref.IdentityRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import javax.annotation.Nonnull;

/** Interface for different methods of printing out a Stmt. */
public abstract class StmtPrinter {
  @Nonnull protected Body body = Body.getNoBody();

  @Nonnull
  public Body getBody() {
    return body;
  }

  public abstract void startStmt(Stmt u);

  public abstract void endStmt(Stmt u);

  public abstract void setIndent(int offset);

  public abstract void handleIndent();

  public abstract void incIndent();

  public abstract void decIndent();

  public abstract void noIndent();

  public abstract void literal(String s);

  public abstract void newline();

  public abstract void local(Local jimpleLocal);

  public abstract void typeSignature(Type t);

  public abstract void methodSignature(MethodSignature sig);

  public abstract void method(SootMethod m);

  public abstract void constant(Constant c);

  public abstract void field(SootField f);

  public abstract void fieldSignature(FieldSignature fieldSig);

  public abstract void stmtRef(Stmt u, boolean branchTarget);

  public abstract void identityRef(IdentityRef r);

  public abstract void modifier(String toString);
}
