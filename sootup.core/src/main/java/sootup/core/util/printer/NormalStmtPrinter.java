package sootup.core.util.printer;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003-2020 Ondrej Lhotak, Linghui Luo and others
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

import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;

/** StmtPrinter implementation for normal (full) Jimple */
public class NormalStmtPrinter extends LabeledStmtPrinter {

  public NormalStmtPrinter() {
    super();
  }

  @Override
  public void method(SootMethod m) {
    handleIndent();
    output.append(m.getSignature());
  }

  @Override
  public void field(SootField f) {
    handleIndent();
    output.append(f.getSignature());
  }

  @Override
  public void identityRef(IdentityRef r) {
    handleIndent();
    if (r instanceof JThisRef) {
      output.append("@this: ");
      typeSignature(r.getType());
    } else if (r instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) r;
      output.append("@parameter").append(pr.getIndex()).append(": ");
      typeSignature(r.getType());
    } else if (r instanceof JCaughtExceptionRef) {
      output.append("@caughtexception");
    } else {
      throw new RuntimeException();
    }
  }

  @Override
  public void literal(String s) {
    output.append(s);
  }
}
