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

  public enum Option {
    UseAbbreviations,
    OmitLocalsDeclaration,
    AddJimpleLn,
    UseImports
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
    LabeledStmtPrinter printer;
    if (useAbbreviations()) {
      printer = new BriefStmtPrinter();
    } else {
      printer = new NormalStmtPrinter();
    }
    printer.enableImports(options.contains(Option.UseImports));
    printTo(cl, out, printer);
  }

  public void printTo(SootClass cl, PrintWriter out, LabeledStmtPrinter printer) {
    // add jimple line number tags
    setJimpleLnNum(1);

    // Print class name + modifiers
    {
      EnumSet<Modifier> modifiers = EnumSet.copyOf(cl.getModifiers());
      // remove unwanted modifier combinations
      if (cl.isInterface() && Modifier.isAbstract(modifiers)) {
        modifiers.remove(Modifier.ABSTRACT);
      }
      printer.literal(Modifier.toString(modifiers));
      printer.literal(modifiers.size() == 0 ? "" : " ");

      // TODO: CHECK: why is an interface not called interface?!
      // TODO: [ms] exclude Annotation, too when annotation branch is merged

      printer.literal(cl.isInterface() ? "" : "class ");
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
        while (fieldIt.hasNext()) {
          SootField f = (SootField) fieldIt.next();

          printer.literal("    " + f.getDeclaration() + ";");
          printer.newline();
          if (addJimpleLn()) {
            setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), f));
          }
        }
      }
    }

    // Print methods
    printMethods(cl, printer);
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

    out.println(printer.output().toString());
  }

  private void printMethods(SootClass cl, LabeledStmtPrinter printer) {
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
          printTo(body, printer);

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
   * Prints out the methodRef corresponding to b Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode b body.
   */
  public void printTo(Body b, PrintWriter out) {
    LabeledStmtPrinter printer;
    if (useAbbreviations()) {
      printer = new BriefStmtPrinter(b);
    } else {
      printer = new NormalStmtPrinter(b);
    }
    printer.enableImports(options.contains(Option.UseImports));
    printTo(b, printer);
    out.print(printer);
  }

  /**
   * Prints out the methodRef corresponding to b Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode b body.
   *
   * @param printer the StmtPrinter that determines how to print the statements
   */
  public void printTo(Body b, LabeledStmtPrinter printer) {

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

        if (currentStmt != units.iterator().next()) {
          if (unitGraph.getSuccsOf(previousStmt).size() != 1
              || unitGraph.getPredsOf(currentStmt).size() != 1
              || printer.getLabels().containsKey(currentStmt)) {
            printer.newline();
          } else {
            // Or if the previous node does not have body statement as a successor.

            List<Stmt> succs = unitGraph.getSuccsOf(previousStmt);

            if (succs.get(0) != currentStmt) {
              printer.newline();
            }
          }
        }

        if (printer.getLabels().containsKey(currentStmt)) {
          printer.stmtRef(currentStmt, true);
          printer.literal(":");
          printer.newline();
        }

        if (printer.getReferences().containsKey(currentStmt)) {
          printer.stmtRef(currentStmt, false);
        }
      }

      printer.startStmt(currentStmt);
      currentStmt.toString(printer);
      printer.endStmt(currentStmt);

      printer.literal(";");
      printer.newline();
    }
    //    out.print(printer.toString());

    // Print out exceptions
    {
      Iterator<Trap> trapIt = body.getTraps().iterator();

      if (trapIt.hasNext()) {
        printer.newline();
        incJimpleLnNum();
      }

      while (trapIt.hasNext()) {
        Trap trap = trapIt.next();

        // TODO: [ms] set indent here?
        printer.literal("        catch ");
        printer.typeSignature(trap.getExceptionType());
        printer.literal(" from ");
        printer.literal(printer.getLabels().get(trap.getBeginStmt()));
        printer.literal(" to ");
        printer.literal(printer.getLabels().get(trap.getEndStmt()));
        printer.literal(" with ");
        printer.literal(printer.getLabels().get(trap.getHandlerStmt()));
        printer.literal(";");

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
