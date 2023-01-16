package sootup.core.util.printer;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann, Markus Schmidt
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

import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.ref.IdentityRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;

/** StmtPrinter implementation for normal Jimple */
public class BriefStmtPrinter extends LabeledStmtPrinter {

  public BriefStmtPrinter() {
    super();
  }

  @Override
  public void method(SootMethod m) {
    handleIndent();
    if (m.isStatic()) {
      output.append(m.getDeclaringClassType().getFullyQualifiedName());
      output.append(".");
    }
    output.append(m.getSignature().getName());
  }

  @Override
  public void field(SootField f) {
    handleIndent();
    if (f.isStatic()) {
      output.append(f.getDeclaringClassType().getFullyQualifiedName());
      output.append(".");
    }
    output.append(f.getSignature().getName());
  }

  @Override
  public void identityRef(IdentityRef r) {
    handleIndent();
    if (r instanceof JThisRef) {
      output.append("@this");
    } else if (r instanceof JParameterRef) {
      JParameterRef pr = (JParameterRef) r;
      output.append("@parameter").append(pr.getIndex());
    } else if (r instanceof JCaughtExceptionRef) {
      output.append("@caughtexception");
    } else {
      throw new RuntimeException();
    }
  }

  private boolean eatSpace = false;

  @Override
  public void literal(String s) {
    if (eatSpace && s.equals(" ")) {
      eatSpace = false;
      return;
    }
    eatSpace = false;
    if (s.equals(Jimple.STATICINVOKE)
        || s.equals(Jimple.VIRTUALINVOKE)
        || s.equals(Jimple.INTERFACEINVOKE)) {
      eatSpace = true;
      return;
    }

    output.append(s);
  }
}
