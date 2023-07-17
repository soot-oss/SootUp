package sootup.core.util.printer;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.StringTools;

/**
 * StmtPrinter implementation for normal (full) Jimple for OldSoot
 *
 * <p>List of differences between old and current Jimple: - tableswitch and lookupswitch got merged
 * into switch - now imports are possible - disabled
 *
 * @author Markus Schmidt
 */
public class LegacyJimplePrinter extends NormalStmtPrinter {

  // source:
  // https://github.com/soot-oss/soot/blob/1ad74494974165e8b5f2286c90f218a00eadc243/eclipse/ca.mcgill.sable.soot/src/ca/mcgill/sable/soot/editors/JimpleScanner.java
  Set<String> soot_jimple_keywords =
      ImmutableSet.of(
          "ignored",
          "abstract",
          "final",
          "native",
          "public",
          "protected",
          "private",
          "static",
          "synchronized",
          "transient",
          "volatile",
          "class",
          "interface",
          "void",
          "boolean",
          "byte",
          "short",
          "char",
          "int",
          "long",
          "float",
          "double",
          "null_type",
          "unknown",
          "extends",
          "implements",
          "breakpoint",
          "case",
          "catch",
          "cmp",
          "cmpg",
          "cmpl",
          "default",
          "entermonitor",
          "exitmonitor",
          "goto",
          "if",
          "instanceof",
          "interfaceinvoke",
          "lengthof",
          "lookupswitch",
          "neg",
          "new",
          "newarray",
          "newmultiarray",
          "nop",
          "ret",
          "return",
          "specialinvoke",
          "staticinvoke",
          "tableswitch",
          "throw",
          "throws",
          "virtualinvoke",
          "null",
          "from",
          "to",
          "with",
          "annotation",
          "enum");

  public LegacyJimplePrinter() {
    super();
  }

  String sootEscape(String str) {
    if (str.length() == 0) {
      return "''";
    }
    return StringTools.getQuotedStringOf(str, soot_jimple_keywords.contains(str));
  }

  @Override
  void enableImports(boolean enable) {
    if (enable) {
      throw new RuntimeException(
          "Imports are not supported in Legacy Jimple: don't enable UseImports");
    }
  }

  @Override
  public void typeSignature(@Nonnull Type type) {
    handleIndent();
    if (type instanceof ClassType) {
      ClassType ctype = (ClassType) type;
      final String[] splits = ctype.getPackageName().getPackageName().split("\\.");
      for (String split : splits) {
        if (split.length() == 0) {
          continue;
        }
        output.append(sootEscape(split));
        output.append(".");
      }
      output.append(sootEscape(ctype.getClassName()));

    } else {
      // primitivetypes
      output.append(type);
    }
  }

  @Override
  public void stmt(Stmt currentStmt) {
    startStmt(currentStmt);
    // replace switch with lookupswitch
    if (currentStmt instanceof JSwitchStmt) {
      // prepend to switch Stmt
      literal(((JSwitchStmt) currentStmt).isTableSwitch() ? "table" : "lookup");
    }
    currentStmt.toString(this);
    endStmt(currentStmt);
    literal(";");
    newline();
  }
}
