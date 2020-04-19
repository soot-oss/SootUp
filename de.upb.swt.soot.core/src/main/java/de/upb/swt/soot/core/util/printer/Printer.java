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

import de.upb.swt.soot.core.graph.AbstractStmtGraph;
import de.upb.swt.soot.core.graph.BriefStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.Method;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import java.io.PrintWriter;
import java.util.*;

/**
 * Prints out a class and all its methods.
 *
 * <p>modified by Linghui Luo, 11.10.2018
 */

// TODO: [ms] clean up or implement sth with addJimpleLn,getJimpleLnNum,addJimpleLnTags etc. check
// old soot for intention

public class Printer {

  /**
   * Options to control.. UseAbbreviations: print a breif overview of the given SootClass|SootMethod
   * OmitLocalsDeclaration: don't print Local declarations at the beginning of each method
   * AddJimpleLn: unsupported yet UseImports: Enable Java like imports to improve readability by
   * shortening the Signatures LegacyMode: Print Jimple like it was printed in old Soot (<= Version
   * 4)
   */
  public enum Option {
    UseAbbreviations,
    OmitLocalsDeclaration,
    AddJimpleLn,
    UseImports,
    LegacyMode
  }

  private final Set<Option> options = EnumSet.noneOf(Option.class);
  private static int jimpleLnNum = 0; // actual line number

  public Printer(Option... options) {
    this.options.addAll(Arrays.asList(options));
  }

  private boolean useAbbreviations() {
    return options.contains(Option.UseAbbreviations);
  }

  private boolean addJimpleLn() {
    return options.contains(Option.AddJimpleLn);
  }

  public void setOption(Option opt) {
    options.add(opt);
  }

  public void clearOption(Option opt) {
    options.remove(opt);
  }

  public int getJimpleLnNum() {
    return jimpleLnNum;
  }

  public void setJimpleLnNum(int newVal) {
    jimpleLnNum = newVal;
  }

  public void incJimpleLnNum() {
    jimpleLnNum++;
  }

  public void printTo(SootClass cl, PrintWriter out) {
    LabeledStmtPrinter printer = determinePrinter();
    printer.enableImports(options.contains(Option.UseImports));
    printTo(cl, printer, out);
  }

  private LabeledStmtPrinter determinePrinter() {
    if (useAbbreviations()) {
      return new BriefStmtPrinter();
    } else if (options.contains(Option.LegacyMode)) {
      return new LegacyJimplePrinter();
    } else {
      return new NormalStmtPrinter();
    }
  }

  private void printTo(SootClass cl, LabeledStmtPrinter printer, PrintWriter out) {
    // add jimple line number tags
    setJimpleLnNum(1);

    // Print class name + modifiers
    {
      EnumSet<Modifier> modifiers = EnumSet.copyOf(cl.getModifiers());
      // remove unwanted modifier combinations
      if (cl.isInterface() && Modifier.isAbstract(modifiers)) {
        modifiers.remove(Modifier.ABSTRACT);
      }
      printer.modifier(Modifier.toString(modifiers));
      printer.literal(modifiers.size() == 0 ? "" : " ");

      printer.literal(
          Modifier.isInterface(modifiers) || Modifier.isAnnotation(modifiers) ? "" : "class ");
      printer.typeSignature(cl.getType());
    }

    // Print extension
    {
      Optional<ClassType> superclassSignature = cl.getSuperclass();

      superclassSignature.ifPresent(
          javaClassSignature -> {
            printer.literal(" extends ");
            printer.typeSignature(javaClassSignature);
          });
    }

    // Print interfaces
    {
      Iterator<ClassType> interfaceIt = cl.getInterfaces().iterator();

      if (interfaceIt.hasNext()) {

        printer.literal(" implements ");
        printer.typeSignature(interfaceIt.next());

        while (interfaceIt.hasNext()) {
          printer.literal(", ");
          printer.typeSignature(interfaceIt.next());
        }
      }
    }

    printer.newline();
    incJimpleLnNum();
    printer.literal("{");
    incJimpleLnNum();

    // Print fields
    {
      Iterator<? extends Field> fieldIt = cl.getFields().iterator();

      if (fieldIt.hasNext()) {
        printer.incIndent();
        while (fieldIt.hasNext()) {
          SootField f = (SootField) fieldIt.next();
          printer.newline();
          printer.handleIndent();
          printer.literal(f.getDeclaration());
          printer.literal(";");
          printer.newline();
          if (addJimpleLn()) {
            setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), f));
          }
        }

        printer.decIndent();
      }
    }

    // Print methods
    printMethods(cl, printer, out);
    printer.literal("}");

    printer.newline();
    incJimpleLnNum();

    // if enabled: print the list of imports and append class contents
    if (options.contains(Option.UseImports)) {
      Map<String, PackageName> entries = printer.getImports();
      // remove current class itself from imports
      entries.remove(cl.getType().getClassName());

      for (Map.Entry<String, PackageName> item : entries.entrySet()) {
        out.println("import " + item.getValue() + "." + item.getKey() + ";");
      }
      out.println();
    }

    out.println(printer.toString());
  }

  private void printMethods(SootClass cl, LabeledStmtPrinter printer, PrintWriter out) {
    Iterator<? extends Method> methodIt = cl.getMethods().iterator();
    if (methodIt.hasNext()) {
      printer.incIndent();
      printer.newline();
      incJimpleLnNum();

      while (methodIt.hasNext()) {
        SootMethod method = (SootMethod) methodIt.next();

        if (method.hasBody()) {
          Body body = method.getBody();
          printer.createLabelMaps(body);
          printTo(body, printer, out);

        } else {
          printer.handleIndent();
          method.toString(printer);
          printer.literal(";");
          incJimpleLnNum();
        }

        if (methodIt.hasNext()) {
          printer.newline();
          incJimpleLnNum();
        }
      }
      printer.decIndent();
    }
  }

  /**
   * Prints out the method corresponding to body Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode body body.
   */
  public void printTo(Body body, PrintWriter out) {
    LabeledStmtPrinter printer = determinePrinter();
    printer.createLabelMaps(body);
    printer.enableImports(options.contains(Option.UseImports));
    printTo(body, printer, out);
    out.print(printer);
  }

  /**
   * Prints out the method corresponding to b Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode b body.
   *
   * @param printer the StmtPrinter that determines how to print the statements
   */
  private void printTo(Body b, LabeledStmtPrinter printer, PrintWriter out) {

    b.getMethod().toString(printer);

    if (addJimpleLn()) {
      setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), b.getMethod()));
    }

    printer.handleIndent();
    printer.literal("{");
    printer.newline();
    incJimpleLnNum();

    printer.incIndent();

    AbstractStmtGraph unitGraph = new BriefStmtGraph(b);

    if (!options.contains(Option.OmitLocalsDeclaration)) {
      printLocalsInBody(b, printer);
    }
    printStatementsInBody(b, printer, unitGraph);

    printer.decIndent();

    printer.handleIndent();
    printer.literal("}");
    incJimpleLnNum();
    printer.newline();
  }

  /** Prints the given <code>JimpleBody</code> to the specified <code>PrintWriter</code>. */
  private void printStatementsInBody(
      Body body, LabeledStmtPrinter printer, AbstractStmtGraph unitGraph) {
    Collection<Stmt> units = body.getStmts();
    Stmt previousStmt;

    for (Stmt currentStmt : units) {
      previousStmt = currentStmt;

      // Print appropriate header.
      {
        // Put an empty line if the previous node was a branch node, the current node is a join node
        // or the previous statement does not have body statement as a successor, or if
        // body statement has a label on it

        final boolean currentStmtHasLabel = printer.getLabels().containsKey(currentStmt);
        if (currentStmt != units.iterator().next()) {
          if (unitGraph.getSuccsOf(previousStmt).size() != 1
              || unitGraph.getPredsOf(currentStmt).size() != 1
              || currentStmtHasLabel) {
            printer.newline();
          } else {
            // Or if the previous node does not have body statement as a successor.

            List<Stmt> succs = unitGraph.getSuccsOf(previousStmt);

            if (succs.get(0) != currentStmt) {
              printer.newline();
            }
          }
        }

        if (currentStmtHasLabel) {
          printer.stmtRef(currentStmt, true);
          printer.literal(":");
          printer.newline();
        }

        // print phi node info
        if (printer.getReferences().containsKey(currentStmt)) {
          printer.stmtRef(currentStmt, false);
        }
      }

      printer.stmt(currentStmt);
    }

    // Print out exceptions
    {
      Iterator<Trap> trapIt = body.getTraps().iterator();

      if (trapIt.hasNext()) {
        printer.newline();
        incJimpleLnNum();
      }

      while (trapIt.hasNext()) {
        Trap trap = trapIt.next();

        printer.noIndent();
        printer.literal(" catch ");
        printer.typeSignature(trap.getExceptionType());
        printer.literal(" from ");
        printer.literal(printer.getLabels().get(trap.getBeginStmt()));
        printer.literal(" to ");
        printer.literal(printer.getLabels().get(trap.getEndStmt()));
        printer.literal(" with ");
        printer.literal(printer.getLabels().get(trap.getHandlerStmt()));
        printer.literal(";");
        printer.newline();
        incJimpleLnNum();
      }
    }
  }

  private int addJimpleLnTags(int lnNum, SootMethod meth) {
    lnNum++;
    return lnNum;
  }

  private int addJimpleLnTags(int lnNum, SootField f) {
    lnNum++;
    return lnNum;
  }

  /** Prints the given <code>JimpleBody</code> to the specified <code>PrintWriter</code>. */
  private void printLocalsInBody(Body body, StmtPrinter up) {
    // Print out local variables
    {
      Map<Type, List<Local>> typeToLocals = new LinkedHashMap<>(body.getLocalCount() * 2 + 1, 0.7f);

      // Collect locals
      {
        for (Local local : body.getLocals()) {
          List<Local> localList;

          Type t = local.getType();

          if (typeToLocals.containsKey(t)) {
            localList = typeToLocals.get(t);
          } else {
            localList = new ArrayList<>();
            typeToLocals.put(t, localList);
          }

          localList.add(local);
        }
      }

      // Print locals
      {
        for (Type type : typeToLocals.keySet()) {
          List<Local> localList = new ArrayList<>(typeToLocals.get(type));
          up.typeSignature(type);
          up.literal(" ");

          final int len = localList.size();
          if (len > 0) {
            up.local(localList.get(0));
            for (int i = 1; i < len; i++) {
              up.literal(", ");
              up.local(localList.get(i));
            }
          }

          up.literal(";");
          up.newline();
        }
      }

      if (!typeToLocals.isEmpty()) {
        up.newline();
      }
    }
  }
}
