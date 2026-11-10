/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package sootup.callgraph.reflection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.Value;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.AbstractInvokeExpr;
import sootup.core.jimple.common.expr.JInterfaceInvokeExpr;
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JSpecialInvokeExpr;
import sootup.core.jimple.common.expr.JStaticInvokeExpr;
import sootup.core.jimple.common.expr.JVirtualInvokeExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JInvokeStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.Position;
import sootup.core.model.SootClass;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.ReferenceType;
import sootup.core.views.View;

/**
 * Resolves reflection according to dynamic traces recorded by <a
 * href="https://github.com/secure-software-engineering/tamiflex">TamiFlex</a>. Log line format:
 * {@code kind;target;callerClass.callerMethod;lineNumber;...}.
 *
 * <p>A log entry applies to the call sites of its kind in every same-named method of the caller
 * class whose line range contains {@code lineNumber} (all of them if the line is unknown, or if no
 * call site matches the line).
 */
public class TamiflexReflectionModel extends AbstractReflectionModel {
  private static final Logger logger = LoggerFactory.getLogger(TamiflexReflectionModel.class);

  private record Entry(int line, String target) {}

  /** "callerClass.callerMethod" -> kind -> entries */
  private final Map<String, Map<ReflectionKind, List<Entry>>> entries = new HashMap<>();

  private final IdentifierFactory idf;
  private final ClassType objectType;

  public TamiflexReflectionModel(@NonNull View view, @NonNull Path logFile) {
    super(view);
    this.idf = view.getIdentifierFactory();
    this.objectType = idf.getClassType("java.lang.Object");
    parseTamiflexLog(logFile);
  }

  public TamiflexReflectionModel(@NonNull View view, @NonNull String logFile) {
    this(view, Path.of(logFile));
  }

  /** Targets the log records for call site {@code s} of the given kind. */
  private Set<String> targetsOf(MethodContext ctx, ReflectionKind kind, InvokableStmt s) {
    SootMethod m = ctx.method();
    List<Entry> es =
        entries
            .getOrDefault(
                m.getDeclaringClassType().getFullyQualifiedName() + "." + m.getName(),
                Collections.emptyMap())
            .getOrDefault(kind, Collections.emptyList());
    if (es.isEmpty()) {
      return Collections.emptySet();
    }
    Set<String> ret = new LinkedHashSet<>();
    for (Entry e : es) {
      if (e.line() < 0 || containsLine(s, e.line()) || !anySiteContainsLine(ctx, kind, e.line())) {
        ret.add(e.target());
      }
    }
    return ret;
  }

  private static boolean containsLine(Stmt s, int line) {
    Position pos = s.getPositionInfo().getStmtPosition();
    return pos.getFirstLine() <= line && line <= pos.getLastLine();
  }

  /** Whether a call site of {@code kind} at {@code line} exists in any overload of the method. */
  private boolean anySiteContainsLine(MethodContext ctx, ReflectionKind kind, int line) {
    SootMethod m = ctx.method();
    List<Body> bodies = new ArrayList<>();
    bodies.add(ctx.body());
    view.getClass(m.getDeclaringClassType())
        .ifPresent(
            c ->
                c.getMethods().stream()
                    .filter(
                        o ->
                            o.getName().equals(m.getName())
                                && o.hasBody()
                                && !o.getSignature().equals(m.getSignature()))
                    .forEach(o -> bodies.add(o.getBody())));
    for (Body body : bodies) {
      for (Stmt s : body.getStmts()) {
        if (s.isInvokableStmt()
            && s.asInvokableStmt().getInvokeExpr().isPresent()
            && kindOf(s.asInvokableStmt().getInvokeExpr().get().getMethodSignature().toString())
                == kind
            && containsLine(s, line)) {
          return true;
        }
      }
    }
    logger.warn(
        "Mismatch between statement and reflection log entry - {};{};{}",
        kind,
        ctx.method().getSignature(),
        line);
    return false;
  }

  public static String dot2slashStyle(String clazz) {
    String x = clazz.replace('.', '/');
    return "L" + x + ";";
  }

  private static StmtPositionInfo noPos() {
    return StmtPositionInfo.getNoStmtPositionInfo();
  }

  @Override
  protected List<Stmt> transformClassForName(MethodContext ctx, InvokableStmt s) {
    // <java.lang.Class: java.lang.Class forName(java.lang.String)>
    // <java.lang.Class: java.lang.Class forName(java.lang.String,boolean,java.lang.ClassLoader)>
    if (!(s instanceof JAssignStmt assign)) {
      return Collections.emptyList();
    }
    List<Stmt> ret = new ArrayList<>();
    ClassType classType = idf.getClassType("java.lang.Class");
    for (String clazz : targetsOf(ctx, ReflectionKind.ClassForName, s)) {
      ClassConstant cc = new ClassConstant(dot2slashStyle(clazz), classType);
      ret.add(new JAssignStmt(assign.getLeftOp(), cc, noPos()));
    }
    return ret;
  }

  @Override
  protected List<Stmt> transformClassNewInstance(MethodContext ctx, InvokableStmt s) {
    // <java.lang.Class: java.lang.Object newInstance()>
    if (!(s instanceof JAssignStmt assign)) {
      return Collections.emptyList();
    }
    LValue lvalue = assign.getLeftOp();
    List<Stmt> ret = new ArrayList<>();
    MethodSubSignature initSubSig = idf.parseMethodSubSignature("void <init>()");
    for (String clsName : targetsOf(ctx, ReflectionKind.ClassNewInstance, s)) {
      Optional<? extends SootClass> cls = view.getClass(idf.getClassType(clsName));
      if (cls.isEmpty()) {
        continue;
      }
      Optional<? extends SootMethod> constructor = cls.get().getMethod(initSubSig);
      if (constructor.isPresent()) {
        ret.add(new JAssignStmt(lvalue, new JNewExpr(cls.get().getType()), noPos()));
        ret.add(
            new JInvokeStmt(
                new JSpecialInvokeExpr(
                    (Local) lvalue, constructor.get().getSignature(), Collections.emptyList()),
                noPos()));
      }
    }
    return ret;
  }

  @Override
  protected List<Stmt> transformConstructorNewInstance(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>
    if (!(s instanceof JAssignStmt assign)) {
      return Collections.emptyList();
    }
    Set<String> targets = targetsOf(ctx, ReflectionKind.ConstructorNewInstance, s);
    if (targets.isEmpty()) {
      return Collections.emptyList();
    }
    LValue lvalue = assign.getLeftOp();
    List<Stmt> ret = new ArrayList<>();
    Value args = s.getInvokeExpr().get().getArg(0);
    Local arg = null;
    if (args instanceof Local argsLocal) {
      JArrayRef arrayRef = Jimple.newArrayRef(argsLocal, IntConstant.getInstance(0));
      arg = Jimple.newLocal("intermediate/" + arrayRef, objectType);
      ctx.builder().addLocal(arg);
      ret.add(new JAssignStmt(arg, arrayRef, noPos()));
    }
    for (String constructorSignature : targets) {
      Optional<? extends SootMethod> constructor =
          view.getMethod(idf.parseMethodSignature(constructorSignature));
      if (constructor.isEmpty()) {
        continue;
      }
      ret.add(
          new JAssignStmt(
              lvalue, new JNewExpr(constructor.get().getDeclaringClassType()), noPos()));
      ret.add(
          new JInvokeStmt(
              new JSpecialInvokeExpr(
                  (Local) lvalue,
                  constructor.get().getSignature(),
                  argsFor(constructor.get().getParameterCount(), arg)),
              noPos()));
    }
    return ret;
  }

  /** Every parameter receives the first element of the reflective argument array (or null). */
  private static List<Immediate> argsFor(int argCount, Local arg) {
    List<Immediate> mArgs = new ArrayList<>(argCount);
    for (int i = 0; i < argCount; i++) {
      mArgs.add(arg != null ? arg : NullConstant.getInstance());
    }
    return mArgs;
  }

  @Override
  protected List<Stmt> transformMethodInvoke(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>
    Set<String> targets = targetsOf(ctx, ReflectionKind.MethodInvoke, s);
    if (targets.isEmpty()) {
      return Collections.emptyList();
    }
    List<Stmt> ret = new ArrayList<>();
    AbstractInvokeExpr iie = s.getInvokeExpr().get();
    Value base = iie.getArg(0);
    Value args = iie.getArg(1);
    Local arg = null;
    if (args instanceof Local argsLocal && args.getType() instanceof ArrayType) {
      JArrayRef arrayRef = Jimple.newArrayRef(argsLocal, IntConstant.getInstance(0));
      arg = Jimple.newLocal("intermediate/" + arrayRef, objectType);
      ctx.builder().addLocal(arg);
      ret.add(new JAssignStmt(arg, arrayRef, noPos()));
    }
    for (String methodSignature : targets) {
      MethodSignature sig = idf.parseMethodSignature(methodSignature);
      Optional<? extends SootMethod> method = view.getMethod(sig);
      if (method.isEmpty()) {
        continue;
      }
      List<Immediate> mArgs = argsFor(method.get().getParameterCount(), arg);
      AbstractInvokeExpr ie;
      if (method.get().isStatic()) {
        ie = new JStaticInvokeExpr(sig, mArgs);
      } else if (base instanceof Local baseLocal) {
        boolean inInterface =
            view.getClass(sig.getDeclClassType()).map(SootClass::isInterface).orElse(false);
        ie =
            inInterface
                ? new JInterfaceInvokeExpr(baseLocal, sig, mArgs)
                : new JVirtualInvokeExpr(baseLocal, sig, mArgs);
      } else {
        continue;
      }
      if (s instanceof JAssignStmt assign) {
        ret.add(new JAssignStmt(assign.getLeftOp(), ie, noPos()));
      } else {
        ret.add(new JInvokeStmt(ie, noPos()));
      }
    }
    return ret;
  }

  /** Field ref for a reflective access to {@code fieldSignature} on {@code base}, if resolvable. */
  private Optional<JFieldRef> fieldRef(String fieldSignature, Value base) {
    Optional<? extends SootField> field = view.getField(idf.parseFieldSignature(fieldSignature));
    if (field.isEmpty()) {
      return Optional.empty();
    }
    if (field.get().isStatic()) {
      return Optional.of(Jimple.newStaticFieldRef(field.get().getSignature()));
    }
    if (base instanceof Local baseLocal) {
      return Optional.of(Jimple.newInstanceFieldRef(baseLocal, field.get().getSignature()));
    }
    return Optional.empty();
  }

  @Override
  protected List<Stmt> transformFieldSet(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Field: void set(java.lang.Object,java.lang.Object)>
    List<Stmt> ret = new ArrayList<>();
    AbstractInvokeExpr iie = s.getInvokeExpr().get();
    Value base = iie.getArg(0);
    Value rValue = iie.getArg(1);
    for (String fieldSignature : targetsOf(ctx, ReflectionKind.FieldSet, s)) {
      fieldRef(fieldSignature, base)
          .ifPresent(ref -> ret.add(new JAssignStmt(ref, rValue, noPos())));
    }
    return ret;
  }

  @Override
  protected List<Stmt> transformFieldGet(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Field: java.lang.Object get(java.lang.Object)>
    if (!(s instanceof JAssignStmt assign)) {
      return Collections.emptyList();
    }
    List<Stmt> ret = new ArrayList<>();
    Value base = s.getInvokeExpr().get().getArg(0);
    for (String fieldSignature : targetsOf(ctx, ReflectionKind.FieldGet, s)) {
      fieldRef(fieldSignature, base)
          .filter(ref -> ref.getType() instanceof ReferenceType)
          .ifPresent(ref -> ret.add(new JAssignStmt(assign.getLeftOp(), ref, noPos())));
    }
    return ret;
  }

  @Override
  protected List<Stmt> transformArrayNewInstance(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>
    if (!(s instanceof JAssignStmt assign)) {
      return Collections.emptyList();
    }
    List<Stmt> ret = new ArrayList<>();
    for (String arrayType : targetsOf(ctx, ReflectionKind.ArrayNewInstance, s)) {
      ArrayType at = (ArrayType) idf.getType(arrayType);
      JNewArrayExpr newExpr =
          Jimple.newNewArrayExpr(at.getElementType(), IntConstant.getInstance(1), idf);
      ret.add(new JAssignStmt(assign.getLeftOp(), newExpr, noPos()));
    }
    return ret;
  }

  @Override
  protected List<Stmt> transformArrayGet(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Array: java.lang.Object get(java.lang.Object,int)> - log independent
    if (!(s instanceof JAssignStmt assign)) {
      return Collections.emptyList();
    }
    List<Stmt> ret = new ArrayList<>();
    Value base = s.getInvokeExpr().get().getArg(0);
    if (!(base instanceof Local baseLocal)) {
      return ret;
    }
    JArrayRef arrayRef = null;
    if (base.getType() instanceof ArrayType) {
      arrayRef = Jimple.newArrayRef(baseLocal, IntConstant.getInstance(0));
    } else if (base.getType().equals(objectType)) {
      Local local = Jimple.newLocal("intermediate/" + base, new ArrayType(objectType, 1));
      ctx.builder().addLocal(local);
      ret.add(new JAssignStmt(local, base, noPos()));
      arrayRef = Jimple.newArrayRef(local, IntConstant.getInstance(0));
    }
    if (arrayRef != null) {
      ret.add(new JAssignStmt(assign.getLeftOp(), arrayRef, noPos()));
    }
    return ret;
  }

  @Override
  protected List<Stmt> transformArraySet(MethodContext ctx, InvokableStmt s) {
    // <java.lang.reflect.Array: void set(java.lang.Object,int,java.lang.Object)> - log independent
    AbstractInvokeExpr iie = s.getInvokeExpr().get();
    Value base = iie.getArg(0);
    if (base instanceof Local baseLocal && base.getType() instanceof ArrayType) {
      JArrayRef arrayRef = Jimple.newArrayRef(baseLocal, IntConstant.getInstance(0));
      return List.of(new JAssignStmt(arrayRef, iie.getArg(2), noPos()));
    }
    return Collections.emptyList();
  }

  /* parse reflection log generated by Tamiflex. */
  private void parseTamiflexLog(Path logFile) {
    try (BufferedReader reader = Files.newBufferedReader(logFile)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] portions = line.split(";", -1);
        if (portions.length < 4) {
          logger.debug("illegal tamiflex log: {}", line);
          continue;
        }
        ReflectionKind kind = ReflectionKind.parse(portions[0]);
        if (kind == null) {
          logger.debug("illegal tamiflex reflection kind: {}", portions[0]);
          continue;
        }
        String mappedTarget = portions[1];
        String inClzDotMthdStr = portions[2];
        int lineNumber;
        try {
          lineNumber = portions[3].isEmpty() ? -1 : Integer.parseInt(portions[3]);
        } catch (NumberFormatException e) {
          logger.debug("illegal line number in tamiflex log: {}", line);
          continue;
        }
        if (!isKnownTarget(kind, mappedTarget)) {
          logger.debug("unknown mapped target for {}: {}", kind, mappedTarget);
          continue;
        }
        entries
            .computeIfAbsent(inClzDotMthdStr, k -> new EnumMap<>(ReflectionKind.class))
            .computeIfAbsent(kind, k -> new ArrayList<>())
            .add(new Entry(lineNumber, mappedTarget));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private boolean isKnownTarget(ReflectionKind kind, String mappedTarget) {
    return switch (kind) {
      case ClassNewInstance -> view.getClass(idf.getClassType(mappedTarget)).isPresent();
      case ConstructorNewInstance, MethodInvoke ->
          view.getMethod(idf.parseMethodSignature(mappedTarget)).isPresent();
      case FieldSet, FieldGet -> view.getField(idf.parseFieldSignature(mappedTarget)).isPresent();
      case ClassForName, ArrayNewInstance -> true;
      default -> false;
    };
  }
}
