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
import de.upb.swt.soot.core.util.EscapedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Prints out a class and all its methods.
 *
 * <p>modified by Linghui Luo, 11.10.2018
 */

// TODO: [ms] clean up or implement sth with addJimpleLn,getJimpleLnNum,addJimpleLnTags etc.

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

    PrintWriter output;
    StringWriter writer = new StringWriter();
    if (options.contains(Option.UseImports)) {
      // cache output to prepend the import list later
      output = new PrintWriter(new EscapedWriter(writer));
    } else {
      output = out;
    }

    // Print class name + modifiers
    {
      EnumSet<Modifier> modifiers = EnumSet.copyOf(cl.getModifiers());
      // remove unwanted modifier combinations
      if (cl.isInterface() && modifiers.contains("abstract")) {
        modifiers.remove(Modifier.ABSTRACT);
      }
      output.print(Modifier.toString(modifiers));
      output.print(" ");

      // TODO: CHECK: why is an interface not called interface?!
      // TODO: [ms] exclude Annotation, too when annotation branch is merged
      String classType = cl.isInterface() ? "" : "class";

      output.print(classType + " " + cl.getType() + "");
    }

    // Print extension
    {
      Optional<ClassType> superclassSignature = cl.getSuperclass();

      superclassSignature.ifPresent(
          javaClassSignature -> {
            output.print(" extends ");
            output.print(printer.type(javaClassSignature));
          });
    }

    // Print interfaces
    {
      Iterator<ClassType> interfaceIt = cl.getInterfaces().iterator();

      if (interfaceIt.hasNext()) {

        output.print(" implements ");
        output.print(printer.type(interfaceIt.next()));

        while (interfaceIt.hasNext()) {
          output.print(", ");
          output.print(printer.type(interfaceIt.next()));
        }
      }
    }

    output.println();
    incJimpleLnNum();
    output.println("{");
    incJimpleLnNum();

    // Print fields
    {
      Iterator<? extends Field> fieldIt = cl.getFields().iterator();

      if (fieldIt.hasNext()) {
        while (fieldIt.hasNext()) {
          SootField f = (SootField) fieldIt.next();

          output.println("    " + f.getDeclaration() + ";");
          if (addJimpleLn()) {
            setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), f));
          }
        }
      }
    }

    // Print methods
    printMethods(cl, output, printer);

    output.println("}");
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
      out.print(writer.toString());
    }
  }

  private void printMethods(SootClass cl, PrintWriter out, LabeledStmtPrinter printer) {
    Iterator<? extends Method> methodIt = cl.getMethods().iterator();
    if (methodIt.hasNext()) {
      if (cl.getMethods().size() != 0) {
        out.println();
        incJimpleLnNum();
      }

      while (methodIt.hasNext()) {
        SootMethod method = (SootMethod) methodIt.next();

        if (method.hasBody()) {
          Body body = method.getBody();
          printer.createLabelMaps(body);
          printTo(body, out, printer);

        } else {
          out.print("    ");
          method.toString(printer);
          out.println(";");
          incJimpleLnNum();
        }

        if (methodIt.hasNext()) {
          out.println();
          incJimpleLnNum();
        }
      }
    }
  }

  /**
   * Prints out the methodRef corresponding to b Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode b body.
   *
   * @param out a PrintWriter instance to print to.
   */
  public void printTo(Body b, PrintWriter out) {
    LabeledStmtPrinter printer;
    if (useAbbreviations()) {
      printer = new BriefStmtPrinter(b);
    } else {
      printer = new NormalStmtPrinter(b);
    }
    printer.enableImports(options.contains(Option.UseImports));
    printTo(b, out, printer);
  }

  /**
   * Prints out the methodRef corresponding to b Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode b body.
   *
   * @param out a PrintWriter instance to print to.
   * @param printer the StmtPrinter that determines how to print the statements
   */
  public void printTo(Body b, PrintWriter out, LabeledStmtPrinter printer) {

    b.getMethod().toString(printer);
    out.println(printer.toString());

    if (addJimpleLn()) {
      setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), b.getMethod()));
    }
    out.println("    {");
    incJimpleLnNum();

    AbstractStmtGraph unitGraph = new BriefStmtGraph(b);

    if (!options.contains(Option.OmitLocalsDeclaration)) {
      printLocalsInBody(b, printer);
    }
    printStatementsInBody(b, out, printer, unitGraph);

    out.println("    }");
    incJimpleLnNum();
  }

  /** Prints the given <code>JimpleBody</code> to the specified <code>PrintWriter</code>. */
  private void printStatementsInBody(
      Body body, PrintWriter out, LabeledStmtPrinter up, AbstractStmtGraph unitGraph) {
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
              || up.getLabels().containsKey(currentStmt)) {
            up.newline();
          } else {
            // Or if the previous node does not have body statement as a successor.

            List<Stmt> succs = unitGraph.getSuccsOf(previousStmt);

            if (succs.get(0) != currentStmt) {
              up.newline();
            }
          }
        }

        if (up.getLabels().containsKey(currentStmt)) {
          up.stmtRef(currentStmt, true);
          up.literal(":");
          up.newline();
        }

        if (up.getReferences().containsKey(currentStmt)) {
          up.stmtRef(currentStmt, false);
        }
      }

      up.startStmt(currentStmt);
      currentStmt.toString(up);
      up.endStmt(currentStmt);

      up.literal(";");
      up.newline();
    }
    out.print(up.toString());

    // Print out exceptions
    {
      Iterator<Trap> trapIt = body.getTraps().iterator();

      if (trapIt.hasNext()) {
        out.println();
        incJimpleLnNum();
      }

      while (trapIt.hasNext()) {
        Trap trap = trapIt.next();

        out.println(
            "        catch "
                + up.type(trap.getExceptionType())
                + " from "
                + up.getLabels().get(trap.getBeginStmt())
                + " to "
                + up.getLabels().get(trap.getEndStmt())
                + " with "
                + up.getLabels().get(trap.getHandlerStmt())
                + ";");

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
