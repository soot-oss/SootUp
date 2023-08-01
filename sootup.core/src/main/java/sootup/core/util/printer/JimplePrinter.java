package sootup.core.util.printer;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003-2020 Ondrej Lhotak, linghui Luo, Markus Schmidt and others
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

import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Trap;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.ClassModifier;
import sootup.core.model.Field;
import sootup.core.model.Method;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Prints out a class and all its methods.
 *
 * <p>modified by Linghui Luo, 11.10.2018
 */

// TODO: [ms] clean up or implement sth with addJimpleLn,getJimpleLnNum,addJimpleLnTags etc. check
// old soot for intention

public class JimplePrinter {

  /**
   * Options to control.. UseAbbreviations: print a brief overview of the given SootClass|SootMethod
   * OmitLocalsDeclaration: don't print Local declarations at the beginning of each method
   * AddJimpleLn: unsupported yet UseImports: Enable Java like imports to improve readability by
   * shortening the Signatures LegacyMode: Print Jimple like it was printed in old Soot (&lt;=
   * Version 4)
   */
  // TODO: [ms] enhancement: add option to print a class with all inherited members
  public enum Option {
    UseAbbreviations,
    OmitLocalsDeclaration,
    AddJimpleLn,
    UseImports,
    LegacyMode
  }

  private final Set<Option> options = EnumSet.noneOf(Option.class);
  private static int jimpleLnNum = 0; // actual line number

  public JimplePrinter(Option... options) {
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

  private LabeledStmtPrinter determinePrinter() {
    if (useAbbreviations()) {
      return new BriefStmtPrinter();
    } else if (options.contains(Option.LegacyMode)) {
      return new LegacyJimplePrinter();
    } else {
      return new NormalStmtPrinter();
    }
  }

  public void printTo(SootClass<?> cl, PrintWriter out) {

    LabeledStmtPrinter printer = determinePrinter();
    printer.enableImports(options.contains(Option.UseImports));

    // add jimple line number tags
    setJimpleLnNum(1);

    // Print class name + modifiers
    {

      /* FIXME: [ms] create own printer per language
            // if( cl instanceof JavaSootClass)
            {
              // print annotation:
              Iterable<AnnotationType> annotationIt = cl.getAnnotations().iterator();
              while (annotationIt.hasNext()) {
                printer.literal("// @" + annotationIt.next() + ";");
              }
            }
      */

      EnumSet<ClassModifier> modifiers = EnumSet.copyOf(cl.getModifiers());
      // remove unwanted modifier combinations
      if (cl.isInterface() && ClassModifier.isAbstract(modifiers)) {
        modifiers.remove(ClassModifier.ABSTRACT);
      }
      if (modifiers.size() != 0) {
        printer.modifier(ClassModifier.toString(modifiers));
        printer.literal(" ");
      }
      if (!ClassModifier.isInterface(modifiers) && !ClassModifier.isAnnotation(modifiers)) {
        printer.literal("class ");
      }

      printer.typeSignature(cl.getType());
    }

    // Print extension
    {
      Optional<? extends ClassType> superclassSignature = cl.getSuperclass();

      superclassSignature.ifPresent(
          javaClassSignature -> {
            printer.literal(" extends ");
            printer.typeSignature(javaClassSignature);
          });
    }

    // Print interfaces
    {
      Iterator<? extends ClassType> interfaceIt = cl.getInterfaces().iterator();

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
            setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), f.getSignature()));
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

  private void printMethods(SootClass<?> cl, LabeledStmtPrinter printer, PrintWriter out) {
    Iterator<? extends Method> methodIt = cl.getMethods().iterator();
    if (methodIt.hasNext()) {
      printer.incIndent();
      printer.newline();
      incJimpleLnNum();

      while (methodIt.hasNext()) {
        SootMethod method = (SootMethod) methodIt.next();

        if (method.hasBody()) {
          Body body = method.getBody();
          // print method's full signature information
          method.toString(printer);
          printer.newline();
          printBody(body, printer);

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
    printer.enableImports(options.contains(Option.UseImports));
    printBody(body, printer);
    out.print(printer);
  }

  public void printTo(StmtGraph<?> graph, PrintWriter out) {
    LabeledStmtPrinter printer = determinePrinter();
    printStmts(graph, printer);
    out.print(printer);
  }

  /**
   * Prints out the method corresponding to b Body, (declaration and body), in the textual format
   * corresponding to the IR used to encode b body.
   *
   * @param printer the StmtPrinter that determines how to print the statements
   */
  private void printBody(Body b, LabeledStmtPrinter printer) {

    if (addJimpleLn()) {
      setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), b.getMethodSignature()));
    }

    printer.handleIndent();
    printer.literal("{");
    printer.newline();
    incJimpleLnNum();

    printer.incIndent();

    if (!options.contains(Option.OmitLocalsDeclaration)) {
      printLocalsInBody(b, printer);
    }
    printStatementsInBody(b, printer);

    printer.decIndent();

    printer.handleIndent();
    printer.literal("}");
    incJimpleLnNum();
    printer.newline();
  }

  /** Prints the given <code>JimpleBody</code> to the specified <code>PrintWriter</code>. */
  private void printStatementsInBody(Body body, LabeledStmtPrinter printer) {
    final StmtGraph<?> stmtGraph = body.getStmtGraph();
    printStmts(stmtGraph, printer);
  }

  private void printStmts(StmtGraph<?> stmtGraph, LabeledStmtPrinter printer) {
    Iterable<Stmt> linearizedStmtGraph = printer.initializeSootMethod(stmtGraph);

    Stmt previousStmt;

    final Map<Stmt, String> labels = printer.getLabels();
    for (Stmt currentStmt : linearizedStmtGraph) {
      previousStmt = currentStmt;

      // Print appropriate header.
      {
        // Put an empty line if:
        // a) the previous stmt was a branch node
        // b) the current stmt is a join node (i.e. multiple predecessors)
        // c) the previous stmt does not have a successor stmt
        // d) if the current stmt has a label on it (i.e. branch-target/trap-handler or begin/end of
        // a trap)

        final boolean currentStmtHasLabel = labels.get(currentStmt) != null;
        if (previousStmt.branches()
            || stmtGraph.predecessors(currentStmt).size() != 1
            || previousStmt.getExpectedSuccessorCount() == 0
            || currentStmtHasLabel) {
          printer.newline();
        }

        if (currentStmtHasLabel) {
          printer.stmtRef(currentStmt, true);
          printer.literal(":");
          printer.newline();
        }
        // TODO: [ms] improve this as getReferences() and currentStmtHasLabel seems to be mutual
        // exclusive! otherwise a stmt would be printed twice ;-)

        if (printer.getReferences().containsKey(currentStmt)) {
          printer.stmtRef(currentStmt, false);
        }
      }

      printer.stmt(currentStmt);
      incJimpleLnNum();
    }

    // Print out exceptions
    {
      Iterator<Trap> trapIt = stmtGraph.getTraps().iterator();

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
        printer.literal(labels.get(trap.getBeginStmt()));
        printer.literal(" to ");
        printer.literal(labels.get(trap.getEndStmt()));
        printer.literal(" with ");
        printer.literal(labels.get(trap.getHandlerStmt()));
        printer.literal(";");
        printer.newline();
        incJimpleLnNum();
      }
    }
  }

  private int addJimpleLnTags(int lnNum, MethodSignature meth) {
    lnNum++;
    return lnNum;
  }

  private int addJimpleLnTags(int lnNum, FieldSignature f) {
    lnNum++;
    return lnNum;
  }

  /** Prints the given <code>JimpleBody</code> to the specified <code>PrintWriter</code>. */
  // Print out local variables
  private void printLocalsInBody(Body body, StmtPrinter up) {
    Map<Type, List<Local>> typeToLocals = new LinkedHashMap<>(body.getLocalCount() * 2 + 1, 0.7f);

    // group locals by type
    {
      for (Local local : body.getLocals()) {
        typeToLocals.computeIfAbsent(local.getType(), k -> new ArrayList<>()).add(local);
      }
    }

    // Print locals
    {
      final Collection<Type> types =
          typeToLocals.keySet().stream()
              .sorted(Comparator.comparing(Object::toString))
              .collect(Collectors.toList());
      for (Type type : types) {
        List<Local> localList = new ArrayList<>(typeToLocals.get(type));
        localList.sort(Comparator.comparing(Local::getName));
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
