package sootup.core.util.printer;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003-2020 Ondrej Lhotak, Linghui Luo, Markus Schmidt
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

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.PackageName;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/** Partial default StmtPrinter implementation. */
public abstract class AbstractStmtPrinter extends StmtPrinter {

  protected boolean startOfLine = true;
  protected final char indentChar = '\u0020';
  protected final int indentStep = 4;
  protected int indent = 0;

  protected StringBuilder output = new StringBuilder();
  private final HashMap<String, PackageName> imports = new HashMap<>();

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
      } else return packageName.equals(referencedPackageName);
    }
    return false;
  }

  public Map<String, PackageName> getImports() {
    return imports;
  }

  public void stmt(Stmt currentStmt) {
    startStmt(currentStmt);
    currentStmt.toString(this);
    endStmt(currentStmt);
    output.append(";");
    newline();
  }

  @Override
  public void startStmt(Stmt u) {
    handleIndent();
  }

  @Override
  public void endStmt(Stmt u) {}

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

  public void modifier(String str) {
    handleIndent();
    output.append(str);
  }

  @Override
  public void typeSignature(@Nonnull Type type) {
    handleIndent();
    if (useImports) {
      if (type instanceof ClassType) {
        if (addImport(type)) {
          output.append(Jimple.escape(((ClassType) type).getClassName()));
        }
      } else if (type instanceof ArrayType) {
        ((ArrayType) type).toString(this);
      } else {
        // primitive types: there should be no need to escape sth
        output.append(type.toString());
      }
    } else {
      output.append(Jimple.escape(type.toString()));
    }
  }

  @Override
  public void newline() {
    output.append("\n");
    startOfLine = true;
  }

  @Override
  public void local(Local l) {
    handleIndent();
    output.append(l.toString());
  }

  @Override
  public void constant(Constant c) {
    handleIndent();
    output.append(c);
  }

  public void handleIndent() {
    if (startOfLine) {
      for (int i = indent; i > 0; i--) {
        output.append(indentChar);
      }
    }
    startOfLine = false;
  }

  @Override
  public String toString() {
    return output.toString();
  }
}
