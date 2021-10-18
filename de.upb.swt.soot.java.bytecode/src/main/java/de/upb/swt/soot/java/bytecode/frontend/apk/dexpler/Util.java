package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.LocalGenerator;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.JAssignStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import javafx.scene.Scene;

import java.util.*;

public class Util {
  /**
   * Return the dotted class name of a type descriptor, i.e. change Ljava/lang/Object; to java.lang.Object.
   *
   * @raises IllegalArgumentException if classname is not of the form Lpath; or [Lpath;
   * @return the dotted name.
   */
  public static String dottedClassName(String typeDescriptor) {
    if (!isByteCodeClassName(typeDescriptor)) {
      // typeDescriptor may not be a class but something like "[[[[[[[[J"
      String t = typeDescriptor;
      int idx = 0;
      while (idx < t.length() && t.charAt(idx) == '[') {
        idx++;
      }
      String c = t.substring(idx);
      if (c.length() == 1 && (c.startsWith("I") || c.startsWith("B") || c.startsWith("C") || c.startsWith("S")
          || c.startsWith("J") || c.startsWith("D") || c.startsWith("F") || c.startsWith("Z"))) {
        Type ty = getType(t);
        return ty == null ? "" : getType(t).toString();
      }
      throw new IllegalArgumentException("typeDescriptor is not a class typedescriptor: '" + typeDescriptor + "'");
    }
    String t = typeDescriptor;
    int idx = 0;
    while (idx < t.length() && t.charAt(idx) == '[') {
      idx++;
    }
    // Debug.printDbg("t ", t ," idx ", idx);
    String className = typeDescriptor.substring(idx);

    className = className.substring(className.indexOf('L') + 1, className.indexOf(';'));

    className = className.replace('/', '.');
    // for (int i = 0; i<idx; i++) {
    // className += "[]";
    // }
    return className;
  }

  public static Type getType(String type) {
    int idx = 0;
    int arraySize = 0;
    Type returnType = null;
    boolean notFound = true;
    while (idx < type.length() && notFound) {
      switch (type.charAt(idx)) {
        case '[':
          while (idx < type.length() && type.charAt(idx) == '[') {
            arraySize++;
            idx++;
          }
          continue;
        // break;

        case 'L':
          String objectName = type.replaceAll("^[^L]*L", "").replaceAll(";$", "");
          // FIXME - what is alternative
          returnType = RefType.v(objectName.replace("/", "."));
          notFound = false;
          break;

        case 'J':
          returnType = PrimitiveType.LongType.getInstance();
          notFound = false;
          break;

        case 'S':
          returnType = PrimitiveType.ShortType.getInstance();
          notFound = false;
          break;

        case 'D':
          returnType = PrimitiveType.DoubleType.getInstance();
          notFound = false;
          break;

        case 'I':
          returnType = PrimitiveType.IntType.getInstance();
          notFound = false;
          break;

        case 'F':
          returnType = PrimitiveType.FloatType.getInstance();
          notFound = false;
          break;

        case 'B':
          returnType = PrimitiveType.ByteType.getInstance();
          notFound = false;
          break;

        case 'C':
          returnType = PrimitiveType.CharType.getInstance();
          notFound = false;
          break;

        case 'V':
          returnType = VoidType.getInstance();
          notFound = false;
          break;

        case 'Z':
          returnType = PrimitiveType.BooleanType.getInstance();
          notFound = false;
          break;

        default:
          throw new RuntimeException("unknown type: '" + type + "'");
      }
      idx++;
    }
    if (returnType != null && arraySize > 0) {
      returnType = new ArrayType(returnType, arraySize);
    }
    return returnType;
  }

  /**
   * Check if passed class name is a byte code classname.
   *
   * @param className
   *          the classname to check.
   */
  public static boolean isByteCodeClassName(String className) {
    return ((className.startsWith("L") || className.startsWith("[")) && className.endsWith(";")
        && ((className.indexOf('/') != -1 || className.indexOf('.') == -1)));
  }

  /**
   * Concatenate two arrays.
   *
   * @param first
   *          first array
   * @param second
   *          second array.
   */
  public static <T> T[] concat(T[] first, T[] second) {
    T[] result = Arrays.copyOf(first, first.length + second.length);
    System.arraycopy(second, 0, result, first.length, second.length);
    return result;
  }

  /**
   * Returns if the type is a floating point type.
   *
   * @param t
   *          the type to test
   */
  public static boolean isFloatLike(Type t) {
    return t.equals(PrimitiveType.FloatType.getInstance()) || t.equals(PrimitiveType.DoubleType.getInstance()) || t.equals(ReferenceType("java.lang.Float"))
        || t.equals(ReferenceType("java.lang.Double"));
  }

  /**
   * Remove all statements except from IdentityStatements for parameters. Return default value (null or zero or nothing
   * depending on the return type).
   *
   * @param bodyBuilder
   */
  public static void emptyBody(Body.BodyBuilder bodyBuilder) {
    // identity statements
    List<Stmt> idStmts = new ArrayList<Stmt>();
    List<Local> idLocals = new ArrayList<Local>();
    for (Stmt stmt : bodyBuilder.getStmts()) {
      if (stmt instanceof JIdentityStmt) {
        JIdentityStmt i = (JIdentityStmt) stmt;
        if (i.getRightOp() instanceof JParameterRef || i.getRightOp() instanceof JThisRef) {
          idStmts.add(stmt);
          idLocals.add((Local) i.getLeftOp());
        }
      }
    }

    bodyBuilder.getStmts().clear();
    bodyBuilder.getLocals().clear();
    bodyBuilder.getTraps().clear();

    final LocalGenerator lg = new LocalGenerator(bodyBuilder.getLocals());

    for (Stmt stmt : idStmts) {
      bodyBuilder.getStmts().add(stmt);
    }
    for (Local l : idLocals) {
      bodyBuilder.getLocals().add(l);
    }

    Type rType = bodyBuilder.getMethodSignature().getType();

    bodyBuilder.getStmts().add(Jimple.v().newNopStmt());

    if (rType instanceof VoidType) {
      bodyBuilder.getStmts().add(Jimple.v().newReturnVoidStmt());
    } else {
      Type t = bodyBuilder.getMethodSignature().getType();
      Local l = lg.generateLocal(t);

      JAssignStmt ass = null;
      if (t instanceof ReferenceType || t instanceof ArrayType) {
        ass = Jimple.newAssignStmt(l, NullConstant.getInstance(), ass.getPositionInfo());
      } else if (t instanceof PrimitiveType.LongType) {
        ass = Jimple.newAssignStmt(l, LongConstant.getInstance(0), ass.getPositionInfo());
      } else if (t instanceof PrimitiveType.FloatType) {
        ass = Jimple.newAssignStmt(l, FloatConstant.getInstance(0.0f), ass.getPositionInfo());
      } else if (t instanceof PrimitiveType.IntType) {
        ass = Jimple.newAssignStmt(l, IntConstant.getInstance(0), ass.getPositionInfo());
      } else if (t instanceof PrimitiveType.DoubleType) {
        ass = Jimple.newAssignStmt(l, DoubleConstant.getInstance(0), ass.getPositionInfo());
      } else if (t instanceof PrimitiveType.BooleanType || t instanceof PrimitiveType.ByteType || t instanceof PrimitiveType.CharType || t instanceof PrimitiveType.ShortType) {
        ass = Jimple.newAssignStmt(l, IntConstant.getInstance(0), ass.getPositionInfo());
      } else {
        throw new RuntimeException("error: return type unknown: " + t + " class: " + t.getClass());
      }
      bodyBuilder.getStmts().add(ass);
      bodyBuilder.getStmts().add(Jimple.newReturnStmt(l, ass.getPositionInfo()));
    }

  }

  /**
   * Insert a runtime exception before unit stmt of body bodyBuilder. Useful to analyze broken code (which make reference to inexisting
   * class for instance) exceptionType: e.g., "java.lang.RuntimeException"
   */
  public static void addExceptionAfterUnit(Body.BodyBuilder bodyBuilder, String exceptionType, Stmt stmt, String m) {
    Set<Local> locals = bodyBuilder.getLocals();
    // FIXME - what class type below?
    Local l = new Local(exceptionType , JavaIdentifierFactory.getInstance().getClassType(""));

    List<Stmt> stmts = new ArrayList<>();
    Stmt stmt1 = Jimple.newAssignStmt(l, Jimple.newNewExpr(JavaIdentifierFactory.getInstance().getClassType(exceptionType)), stmt.getPositionInfo());
    Stmt stmt2
        = Jimple
            .newInvokeStmt(Jimple.newSpecialInvokeExpr(l,
                            JavaIdentifierFactory.getInstance().getClassType(exceptionType), "<init>",
                    Collections.singletonList((Type) JavaIdentifierFactory.getInstance().getClassType("java.lang.String")), VoidType.getInstance(), false),
                new StringConstant(m));
    Stmt stmt3 = Jimple.newThrowStmt(l, stmt.getPositionInfo());
    stmts.add(stmt1);
    stmts.add(stmt2);
    stmts.add(stmt3);

    bodyBuilder.getStmtGraph().insertBefore(stmts, stmt);
  }

  public static List<String> splitParameters(String parameters) {
    List<String> pList = new ArrayList<String>();

    int idx = 0;
    boolean object = false;

    String curr = "";
    while (idx < parameters.length()) {
      char c = parameters.charAt(idx);
      curr += c;
      switch (c) {
        // array
        case '[':
          break;
        // end of object
        case ';':
          object = false;
          pList.add(curr);
          curr = "";
          break;
        // start of object
        case 'L':
          object = true;
          break;
        default:
          if (object) {
            // caracter part of object
          } else { // primitive
            pList.add(curr);
            curr = "";
          }
          break;

      }
      idx++;
    }

    return pList;
  }
}
