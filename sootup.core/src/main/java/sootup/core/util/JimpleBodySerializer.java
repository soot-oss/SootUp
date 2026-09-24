package sootup.core.util;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2024 Markus Schmidt and others
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

import java.util.*;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.Body;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;

/**
 * Serializes a {@link Body} or {@link ControlFlowGraph} to Java source code that, when compiled and
 * executed, recreates the same in-memory representation using the SootUp API.
 *
 * <p>Usage: copy the returned snippet into a test method, supply the {@code identifierFactory}
 * variable and an appropriate {@link MethodSignature}, then call {@code body.build()}.
 *
 * <p>Why: You want to test different scenarios and have existing similar bytecode? Transform it to
 * Jimple and serialize the ControlFlowGraph to Java sourcecode that generates the Jimple Objects
 * directly Use it as a base that you can modify easily/fast for writing test input.
 */
public class JimpleBodySerializer {

  /** Serialize a full Body (includes the {@code Body.builder(...).build()} call). */
  public static String serialize(Body body) {
    return serialize(body.getControlFlowGraph(), body.getLocals());
  }

  /**
   * Serialize a CFG and its locals. The generated snippet ends with a fully configured {@code
   * Body.builder(graph)} call — supply a {@code MethodSignature} where indicated.
   */
  public static String serialize(ControlFlowGraph<?> cfg, Set<Local> locals) {
    Ctx ctx = new Ctx();
    StringBuilder sb = new StringBuilder();

    for (Local local : locals) {
      String varName = toLocalVarName(local.getName(), ctx.usedNames);
      ctx.localVarNames.put(local, varName);
      sb.append("Local ")
          .append(varName)
          .append(" = new Local(\"")
          .append(escapeString(local.getName()))
          .append("\", ")
          .append(serializeType(local.getType()))
          .append(");\n");
    }

    sb.append("\n");
    sb.append("StmtPositionInfo noPos = StmtPositionInfo.getNoStmtPositionInfo();\n");

    List<Stmt> stmts = cfg.getStmts();
    assignStmtVarNames(stmts, ctx);
    for (Stmt stmt : stmts) {
      sb.append(serializeStmt(stmt, ctx.stmtVarNames.get(stmt), ctx)).append("\n");
    }

    sb.append("\n");
    sb.append("MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();\n");
    if (cfg.getStartingStmt() != null) {
      sb.append("graph.setStartingStmt(")
          .append(ctx.stmtVarNames.get(cfg.getStartingStmt()))
          .append(");\n");
    }
    for (Stmt stmt : stmts) {
      List<Stmt> succs = cfg.successors(stmt);
      if (stmt instanceof BranchingStmt) {
        for (int i = 0; i < succs.size(); i++) {
          sb.append("graph.putEdge(")
              .append(ctx.stmtVarNames.get(stmt))
              .append(", ")
              .append(i)
              .append(", ")
              .append(ctx.stmtVarNames.get(succs.get(i)))
              .append(");\n");
        }
      } else if (!succs.isEmpty()) {
        sb.append("graph.putEdge(")
            .append(ctx.stmtVarNames.get(stmt))
            .append(", ")
            .append(ctx.stmtVarNames.get(succs.get(0)))
            .append(");\n");
      }
      Map<ClassType, Stmt> exSuccs = cfg.exceptionalSuccessors(stmt);
      for (Map.Entry<ClassType, Stmt> entry : exSuccs.entrySet()) {
        sb.append("graph.addExceptionalEdge(")
            .append(ctx.stmtVarNames.get(stmt))
            .append(", ")
            .append(serializeType(entry.getKey()))
            .append(", ")
            .append(ctx.stmtVarNames.get(entry.getValue()))
            .append(");\n");
      }
    }

    sb.append("\n");
    sb.append("Set<Local> locals = new LinkedHashSet<>(Arrays.asList(");
    boolean first = true;
    for (Local local : locals) {
      if (!first) sb.append(", ");
      sb.append(ctx.localVarNames.get(local));
      first = false;
    }
    sb.append("));\n");
    sb.append(
        "Body body = Body.builder(graph)\n"
            + "    .setMethodSignature(/* provide MethodSignature here */)\n"
            + "    .setLocals(locals)\n"
            + "    .build();\n");

    return sb.toString();
  }

  private static String serializeStmt(Stmt stmt, String varName, Ctx ctx) {
    String typeName = stmt.getClass().getSimpleName();
    StringBuilder sb = new StringBuilder();
    sb.append(typeName).append(" ").append(varName).append(" = ");

    if (stmt instanceof JAssignStmt) {
      JAssignStmt a = (JAssignStmt) stmt;
      sb.append("new JAssignStmt(")
          .append(serializeValue(a.getLeftOp(), ctx))
          .append(", ")
          .append(serializeValue(a.getRightOp(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JIdentityStmt) {
      JIdentityStmt id = (JIdentityStmt) stmt;
      sb.append("new JIdentityStmt(")
          .append(serializeValue(id.getLeftOp(), ctx))
          .append(", ")
          .append(serializeValue(id.getRightOp(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JIfStmt) {
      sb.append("new JIfStmt(")
          .append(serializeValue(((JIfStmt) stmt).getCondition(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JSwitchStmt) {
      JSwitchStmt sw = (JSwitchStmt) stmt;
      if (sw.isTableSwitch()) {
        List<IntConstant> vals = sw.getValues();
        sb.append("new JSwitchStmt(")
            .append(serializeValue(sw.getKey(), ctx))
            .append(", ")
            .append(vals.get(0).getValue())
            .append(", ")
            .append(vals.get(vals.size() - 1).getValue())
            .append(", noPos);");
      } else {
        sb.append("new JSwitchStmt(").append(serializeValue(sw.getKey(), ctx));
        sb.append(", Arrays.asList(");
        List<IntConstant> vals = sw.getValues();
        for (int i = 0; i < vals.size(); i++) {
          if (i > 0) sb.append(", ");
          sb.append("IntConstant.getInstance(").append(vals.get(i).getValue()).append(")");
        }
        sb.append("), noPos);");
      }
    } else if (stmt instanceof JReturnStmt) {
      sb.append("new JReturnStmt(")
          .append(serializeValue(((JReturnStmt) stmt).getOp(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JReturnVoidStmt) {
      sb.append("new JReturnVoidStmt(noPos);");
    } else if (stmt instanceof JGotoStmt) {
      sb.append("new JGotoStmt(noPos);");
    } else if (stmt instanceof JNopStmt) {
      sb.append("new JNopStmt(noPos);");
    } else if (stmt instanceof JThrowStmt) {
      sb.append("new JThrowStmt(")
          .append(serializeValue(((JThrowStmt) stmt).getOp(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JInvokeStmt) {
      sb.append("new JInvokeStmt(")
          .append(serializeValue(((JInvokeStmt) stmt).getInvokeExpr().get(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JEnterMonitorStmt) {
      sb.append("new JEnterMonitorStmt(")
          .append(serializeValue(((JEnterMonitorStmt) stmt).getOp(), ctx))
          .append(", noPos);");
    } else if (stmt instanceof JExitMonitorStmt) {
      sb.append("new JExitMonitorStmt(")
          .append(serializeValue(((JExitMonitorStmt) stmt).getOp(), ctx))
          .append(", noPos);");
    } else {
      sb.append(
          "/* TODO: unsupported stmt type: "
              + stmt.getClass().getSimpleName()
              + ": "
              + stmt
              + " */;");
    }
    return sb.toString();
  }

  static String serializeValue(sootup.core.jimple.common.Value value, Ctx ctx) {
    if (value instanceof Local) {
      String varName = ctx.localVarNames.get(value);
      return varName != null ? varName : "/* unknown local: " + value + " */";
    }
    if (value instanceof IntConstant) {
      return "IntConstant.getInstance(" + ((IntConstant) value).getValue() + ")";
    }
    if (value instanceof LongConstant) {
      return "LongConstant.getInstance(" + ((LongConstant) value).getValue() + "L)";
    }
    if (value instanceof FloatConstant) {
      float v = ((FloatConstant) value).getValue();
      if (Float.isNaN(v)) return "FloatConstant.getInstance(Float.NaN)";
      if (Float.isInfinite(v))
        return "FloatConstant.getInstance("
            + (v > 0 ? "Float.POSITIVE_INFINITY" : "Float.NEGATIVE_INFINITY")
            + ")";
      return "FloatConstant.getInstance(" + v + "f)";
    }
    if (value instanceof DoubleConstant) {
      double v = ((DoubleConstant) value).getValue();
      if (Double.isNaN(v)) return "DoubleConstant.getInstance(Double.NaN)";
      if (Double.isInfinite(v))
        return "DoubleConstant.getInstance("
            + (v > 0 ? "Double.POSITIVE_INFINITY" : "Double.NEGATIVE_INFINITY")
            + ")";
      return "DoubleConstant.getInstance(" + v + ")";
    }
    if (value instanceof StringConstant) {
      StringConstant sc = (StringConstant) value;
      return "new StringConstant(\""
          + escapeString(sc.getValue())
          + "\", "
          + serializeType(sc.getType())
          + ")";
    }
    if (value instanceof NullConstant) {
      return "NullConstant.getInstance()";
    }
    if (value instanceof BooleanConstant) {
      return "BooleanConstant.getInstance(" + ((BooleanConstant) value).getValue() + ")";
    }
    if (value instanceof ClassConstant) {
      ClassConstant cc = (ClassConstant) value;
      return "new ClassConstant(\""
          + escapeString(cc.getValue())
          + "\", "
          + serializeType(cc.getType())
          + ")";
    }
    // Binary expressions
    if (value instanceof AbstractBinopExpr) {
      AbstractBinopExpr b = (AbstractBinopExpr) value;
      return "new "
          + value.getClass().getSimpleName()
          + "("
          + serializeValue(b.getOp1(), ctx)
          + ", "
          + serializeValue(b.getOp2(), ctx)
          + ")";
    }
    // Unary expressions
    if (value instanceof JCastExpr) {
      JCastExpr c = (JCastExpr) value;
      return "new JCastExpr("
          + serializeValue(c.getOp(), ctx)
          + ", "
          + serializeType(c.getType())
          + ")";
    }
    if (value instanceof JInstanceOfExpr) {
      JInstanceOfExpr io = (JInstanceOfExpr) value;
      return "new JInstanceOfExpr("
          + serializeValue(io.getOp(), ctx)
          + ", "
          + serializeType(io.getCheckType())
          + ")";
    }
    if (value instanceof AbstractUnopExpr) {
      // JNegExpr, JLengthExpr
      return "new "
          + value.getClass().getSimpleName()
          + "("
          + serializeValue(((AbstractUnopExpr) value).getOp(), ctx)
          + ")";
    }
    // Object/array creation
    if (value instanceof JNewExpr) {
      return "new JNewExpr(" + serializeType(((JNewExpr) value).getType()) + ")";
    }
    if (value instanceof JNewArrayExpr) {
      JNewArrayExpr na = (JNewArrayExpr) value;
      return "new JNewArrayExpr("
          + serializeType(na.getBaseType())
          + ", "
          + serializeValue(na.getSize(), ctx)
          + ", identifierFactory)";
    }
    if (value instanceof JNewMultiArrayExpr) {
      JNewMultiArrayExpr nma = (JNewMultiArrayExpr) value;
      StringBuilder sb = new StringBuilder("new JNewMultiArrayExpr(");
      sb.append(serializeType(nma.getBaseType())).append(", Arrays.asList(");
      List<Immediate> sizes = nma.getSizes();
      for (int i = 0; i < sizes.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(serializeValue(sizes.get(i), ctx));
      }
      sb.append("))");
      return sb.toString();
    }
    // References
    if (value instanceof JThisRef) {
      return "new JThisRef(" + serializeType(((JThisRef) value).getType()) + ")";
    }
    if (value instanceof JParameterRef) {
      JParameterRef p = (JParameterRef) value;
      return "new JParameterRef(" + serializeType(p.getType()) + ", " + p.getIndex() + ")";
    }
    if (value instanceof JCaughtExceptionRef) {
      return "new JCaughtExceptionRef("
          + serializeType(((JCaughtExceptionRef) value).getType())
          + ")";
    }
    if (value instanceof JArrayRef) {
      JArrayRef a = (JArrayRef) value;
      return "new JArrayRef("
          + ctx.localVarNames.getOrDefault(a.getBase(), "/* ? */")
          + ", "
          + serializeValue(a.getIndex(), ctx)
          + ")";
    }
    if (value instanceof JStaticFieldRef) {
      return "new JStaticFieldRef("
          + serializeFieldSig(((JStaticFieldRef) value).getFieldSignature())
          + ")";
    }
    if (value instanceof JInstanceFieldRef) {
      JInstanceFieldRef ifr = (JInstanceFieldRef) value;
      return "new JInstanceFieldRef("
          + ctx.localVarNames.getOrDefault(ifr.getBase(), "/* ? */")
          + ", "
          + serializeFieldSig(ifr.getFieldSignature())
          + ")";
    }
    // Invoke expressions
    if (value instanceof JStaticInvokeExpr) {
      JStaticInvokeExpr ie = (JStaticInvokeExpr) value;
      return "new JStaticInvokeExpr("
          + serializeMethodSig(ie.getMethodSignature())
          + ", "
          + serializeArgs(ie, ctx)
          + ")";
    }
    if (value instanceof JVirtualInvokeExpr) {
      JVirtualInvokeExpr ie = (JVirtualInvokeExpr) value;
      return "new JVirtualInvokeExpr("
          + ctx.localVarNames.getOrDefault(ie.getBase(), "/* ? */")
          + ", "
          + serializeMethodSig(ie.getMethodSignature())
          + ", "
          + serializeArgs(ie, ctx)
          + ")";
    }
    if (value instanceof JSpecialInvokeExpr) {
      JSpecialInvokeExpr ie = (JSpecialInvokeExpr) value;
      return "new JSpecialInvokeExpr("
          + ctx.localVarNames.getOrDefault(ie.getBase(), "/* ? */")
          + ", "
          + serializeMethodSig(ie.getMethodSignature())
          + ", "
          + serializeArgs(ie, ctx)
          + ")";
    }
    if (value instanceof JInterfaceInvokeExpr) {
      JInterfaceInvokeExpr ie = (JInterfaceInvokeExpr) value;
      return "new JInterfaceInvokeExpr("
          + ctx.localVarNames.getOrDefault(ie.getBase(), "/* ? */")
          + ", "
          + serializeMethodSig(ie.getMethodSignature())
          + ", "
          + serializeArgs(ie, ctx)
          + ")";
    }
    return "/* TODO: " + value.getClass().getSimpleName() + ": " + value + " */";
  }

  static String serializeType(Type type) {
    // BooleanType, ByteType, ShortType, CharType all extend IntType — check subtypes first
    if (type instanceof PrimitiveType.BooleanType) return "PrimitiveType.BooleanType.getInstance()";
    if (type instanceof PrimitiveType.ByteType) return "PrimitiveType.ByteType.getInstance()";
    if (type instanceof PrimitiveType.ShortType) return "PrimitiveType.ShortType.getInstance()";
    if (type instanceof PrimitiveType.CharType) return "PrimitiveType.CharType.getInstance()";
    if (type instanceof PrimitiveType.IntType) return "PrimitiveType.IntType.getInstance()";
    if (type instanceof PrimitiveType.LongType) return "PrimitiveType.LongType.getInstance()";
    if (type instanceof PrimitiveType.FloatType) return "PrimitiveType.FloatType.getInstance()";
    if (type instanceof PrimitiveType.DoubleType) return "PrimitiveType.DoubleType.getInstance()";
    if (type instanceof VoidType) return "VoidType.getInstance()";
    if (type instanceof NullType) return "NullType.getInstance()";
    if (type instanceof ArrayType) {
      ArrayType at = (ArrayType) type;
      return "identifierFactory.getArrayType("
          + serializeType(at.getBaseType())
          + ", "
          + at.getDimension()
          + ")";
    }
    if (type instanceof ClassType) {
      return "identifierFactory.getClassType(\""
          + ((ClassType) type).getFullyQualifiedName()
          + "\")";
    }
    return "/* unknown type: " + type + " */";
  }

  private static String serializeMethodSig(MethodSignature m) {
    StringBuilder sb = new StringBuilder("new MethodSignature(");
    sb.append(serializeType(m.getDeclClassType()));
    sb.append(", \"").append(escapeString(m.getName())).append("\", ");
    List<Type> params = m.getParameterTypes();
    if (params.isEmpty()) {
      sb.append("Collections.emptyList()");
    } else {
      sb.append("Arrays.asList(");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(serializeType(params.get(i)));
      }
      sb.append(")");
    }
    sb.append(", ").append(serializeType(m.getSubSignature().getType())).append(")");
    return sb.toString();
  }

  private static String serializeFieldSig(FieldSignature f) {
    return "new FieldSignature("
        + serializeType(f.getDeclClassType())
        + ", \""
        + escapeString(f.getName())
        + "\", "
        + serializeType(f.getSubSignature().getType())
        + ")";
  }

  private static String serializeArgs(AbstractInvokeExpr expr, Ctx ctx) {
    if (expr.getArgCount() == 0) {
      return "Collections.emptyList()";
    }
    StringBuilder sb = new StringBuilder("Arrays.asList(");
    for (int i = 0; i < expr.getArgCount(); i++) {
      if (i > 0) sb.append(", ");
      sb.append(serializeValue(expr.getArg(i), ctx));
    }
    sb.append(")");
    return sb.toString();
  }

  private static void assignStmtVarNames(List<Stmt> stmts, Ctx ctx) {
    Map<String, Integer> counters = new HashMap<>();
    for (Stmt stmt : stmts) {
      String prefix = stmtVarPrefix(stmt);
      int count = counters.getOrDefault(prefix, 0);
      counters.put(prefix, count + 1);
      ctx.stmtVarNames.put(stmt, prefix + "_" + count);
    }
  }

  private static String stmtVarPrefix(Stmt stmt) {
    String name = stmt.getClass().getSimpleName();
    // strip leading 'J', lowercase first letter: JAssignStmt -> assignStmt
    if (name.startsWith("J") && name.length() > 1 && Character.isUpperCase(name.charAt(1))) {
      name = Character.toLowerCase(name.charAt(1)) + name.substring(2);
    }
    return name;
  }

  private static String toLocalVarName(String localName, Set<String> used) {
    String base = "local_" + localName.replace('$', '_').replace('-', '_').replace('.', '_');
    if (used.add(base)) {
      return base;
    }
    int i = 2;
    while (!used.add(base + "_" + i)) {
      i++;
    }
    return base + "_" + i;
  }

  private static String escapeString(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  static final class Ctx {
    final Map<Local, String> localVarNames = new LinkedHashMap<>();
    final Map<Stmt, String> stmtVarNames = new LinkedHashMap<>();
    final Set<String> usedNames = new HashSet<>();
  }
}
