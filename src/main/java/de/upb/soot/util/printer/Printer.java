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
package de.upb.soot.util.printer;

import de.upb.soot.core.Body;
import de.upb.soot.core.IMethod;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.graph.AbstractStmtGraph;
import de.upb.soot.graph.BriefStmtGraph;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.Trap;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.util.DeterministicHashMap;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Prints out a class and all its methods.
 * 
 * modified by Linghui Luo, 11.10.2018
 * 
 */
public class Printer {

  private final static int USE_ABBREVIATIONS = 0x0001;
  private final static int ADD_JIMPLE_LN = 0x0010;
  private int options = 0;
  private static int jimpleLnNum = 0; // actual line number

  public Printer() {
  }

  public boolean useAbbreviations() {
    return (options & USE_ABBREVIATIONS) != 0;
  }

  public boolean addJimpleLn() {
    return (options & ADD_JIMPLE_LN) != 0;
  }


  public void setOption(int opt) {
    options |= opt;
  }

  public void clearOption(int opt) {
    options &= ~opt;
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
    // add jimple line number tags
    setJimpleLnNum(1);

    // Print class name + modifiers
    {
      StringTokenizer st = new StringTokenizer(Modifier.toString(cl.getModifiers()));
      while (st.hasMoreTokens()) {
        String tok = st.nextToken();
        if (cl.isInterface() && tok.equals("abstract")) {
          continue;
        }
        out.print(tok + " ");
      }

      String classPrefix = "";

      if (!cl.isInterface()) {
        classPrefix = classPrefix + " class";
        classPrefix = classPrefix.trim();
      }

      out.print(classPrefix + " " + cl.getView().quotedNameOf(cl.getSignature().toString()) + "");
    }

    // Print extension
    {
      if (cl.hasSuperclass()) {
        out.print(" extends " + cl.getView().quotedNameOf(cl.getSuperclass().get().getSignature().toString()) + "");
      }
    }

    // Print interfaces
    {
      Iterator<SootClass> interfaceIt = cl.getInterfaces().iterator();

      if (interfaceIt.hasNext()) {
        out.print(" implements ");

        out.print("" + cl.getView().quotedNameOf(interfaceIt.next().getSignature().toString()) + "");

        while (interfaceIt.hasNext()) {
          out.print(",");
          out.print(" " + cl.getView().quotedNameOf(interfaceIt.next().getSignature().toString()) + "");
        }
      }
    }

    out.println();
    incJimpleLnNum();
    out.println("{");
    incJimpleLnNum();

    // Print fields
    {
      Iterator<SootField> fieldIt = cl.getFields().iterator();

      if (fieldIt.hasNext()) {
        while (fieldIt.hasNext()) {
          SootField f = fieldIt.next();

          if (f.isPhantom()) {
            continue;
          }
          out.println("    " + f.getDeclaration() + ";");
          if (addJimpleLn()) {
            setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), f));
          }
        }
      }
    }

    // Print methods
    printMethod(cl, out);

    out.println("}");
    incJimpleLnNum();
  }

  private void printMethod(SootClass cl, PrintWriter out) {
    Iterator<? extends IMethod> methodIt = cl.getMethods().iterator();
    if (methodIt.hasNext()) {
      if (cl.getMethods().size() != 0) {
        out.println();
        incJimpleLnNum();
      }

      while (methodIt.hasNext()) {
        SootMethod method = (SootMethod) methodIt.next();

        if (method.isPhantom()) {
          continue;
        }

        if (!Modifier.isAbstract(method.getModifiers()) && !Modifier.isNative(method.getModifiers())) {
          if (!method.hasActiveBody()) {
            // method.retrieveActiveBody(); // force loading the body
            if (!method.hasActiveBody()) {
              throw new RuntimeException("method " + method.getName() + " has no active body!");
            }
          }
          printTo(method.getActiveBody(), out);

          if (methodIt.hasNext()) {
            out.println();
            incJimpleLnNum();
          }
        } else {
          out.print("    ");
          out.print(method.getDeclaration());
          out.println(";");
          incJimpleLnNum();
          if (methodIt.hasNext()) {
            out.println();
            incJimpleLnNum();
          }
        }
      }
    }
  }

  /**
   * Prints out the method corresponding to b Body, (declaration and body), in the textual format corresponding to the IR
   * used to encode b body.
   *
   * @param out
   *          a PrintWriter instance to print to.
   */
  public void printTo(Body b, PrintWriter out) {

    boolean isPrecise = !useAbbreviations();

    String decl = b.getMethod().getDeclaration();

    out.println("    " + decl);

    if (addJimpleLn()) {
      setJimpleLnNum(addJimpleLnTags(getJimpleLnNum(), b.getMethod()));
    }

    out.println("    {");
    incJimpleLnNum();

    AbstractStmtGraph unitGraph = new BriefStmtGraph(b);

    LabeledStmtPrinter up;
    if (isPrecise) {
      up = new NormalStmtPrinter(b);
    } else {
      up = new BriefStmtPrinter(b);
    }

    printLocalsInBody(b, up);

    printStatementsInBody(b, out, up, unitGraph);

    out.println("    }");
    incJimpleLnNum();

  }

  /** Prints the given <code>JimpleBody</code> to the specified <code>PrintWriter</code>. */
  private void printStatementsInBody(Body body, PrintWriter out, LabeledStmtPrinter up, AbstractStmtGraph unitGraph) {
    LinkedHashSet<IStmt> units = body.getStmts();
    IStmt previousStmt;

    for (IStmt currentStmt : units) {
      previousStmt = currentStmt;

      // Print appropriate header.
      {
        // Put an empty line if the previous node was a branch node, the current node is a join node
        // or the previous statement does not have body statement as a successor, or if
        // body statement has a label on it

        if (currentStmt != units.iterator().next()) {
          if (unitGraph.getSuccsOf(previousStmt).size() != 1 || unitGraph.getPredsOf(currentStmt).size() != 1
              || up.labels().containsKey(currentStmt)) {
            up.newline();
          } else {
            // Or if the previous node does not have body statement as a successor.

            List<IStmt> succs = unitGraph.getSuccsOf(previousStmt);

            if (succs.get(0) != currentStmt) {
              up.newline();
            }
          }
        }

        if (up.labels().containsKey(currentStmt)) {
          up.stmtRef(currentStmt, true);
          up.literal(":");
          up.newline();
        }

        if (up.references().containsKey(currentStmt)) {
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

        out.println("        catch " + body.getMethod().getView().quotedNameOf(trap.getException().getSignature().toString()) + " from "
            + up.labels().get(trap.getBeginStmt()) + " to " + up.labels().get(trap.getEndStmt()) + " with "
            + up.labels().get(trap.getHandlerStmt()) + ";");

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
  private void printLocalsInBody(Body body, IStmtPrinter up) {
    // Print out local variables
    {
      Map<Type, List<Local>> typeToLocals = new DeterministicHashMap<Type, List<Local>>(body.getLocalCount() * 2 + 1, 0.7f);

      // Collect locals
      {
        Iterator<Local> localIt = body.getLocals().iterator();

        while (localIt.hasNext()) {
          Local local = localIt.next();

          List<Local> localList;

          Type t = local.getType();

          if (typeToLocals.containsKey(t)) {
            localList = typeToLocals.get(t);
          } else {
            localList = new ArrayList<Local>();
            typeToLocals.put(t, localList);
          }

          localList.add(local);
        }
      }

      // Print locals
      {
        Iterator<Type> typeIt = typeToLocals.keySet().iterator();

        while (typeIt.hasNext()) {
          Type type = typeIt.next();

          List<Local> localList = typeToLocals.get(type);
          Object[] locals = localList.toArray();
          up.type(type);
          up.literal(" ");

          for (int k = 0; k < locals.length; k++) {
            if (k != 0) {
              up.literal(", ");
            }

            up.local((Local) locals[k]);
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
