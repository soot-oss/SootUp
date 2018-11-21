package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.core.IField;
import de.upb.soot.core.IMethod;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.Expr;
import de.upb.soot.jimple.common.ref.JInstanceFieldRef;
import de.upb.soot.jimple.common.ref.JStaticFieldRef;
import de.upb.soot.jimple.common.ref.Ref;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.javabytecode.stmt.JBreakpointStmt;
import de.upb.soot.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.soot.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.soot.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.soot.jimple.javabytecode.stmt.JRetStmt;
import de.upb.soot.jimple.javabytecode.stmt.JTableSwitchStmt;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.SootResolver;
import soot.Trap;
import soot.Unit;
import soot.UnknownType;
import soot.Value;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.util.Chain;

/**
 * An IR Converter between old and new soot.
 * 
 * @author Linghui Luo
 *
 */
public class JimpleConverter {
  private Chain<Local> locals = null;
  private Map<IStmt, Stmt> targets;
  List<de.upb.soot.core.SootClass> fromClasses;

  public JimpleConverter(List<de.upb.soot.core.SootClass> sootClasses) {
    this.fromClasses = sootClasses;
  }

  public soot.SootClass convertSootClass(de.upb.soot.core.SootClass fromClass) {
    soot.SootClass toClass = null;
    if (Scene.v().containsClass(fromClass.getName())) {
      toClass = Scene.v().getSootClass(fromClass.getName());
    } else {
      toClass = new SootClass(fromClass.getName());
      Scene.v().addClass(toClass);
    }
    toClass.setApplicationClass();

    // convert modifiers
    toClass.setModifiers(convertModifiers(fromClass.getModifiers()));

    // convert parents
    if (fromClass.hasSuperclass()) {
      Optional<de.upb.soot.core.SootClass> superClass = fromClass.getSuperclass();
      soot.SootClass s = getSootClass(superClass, fromClass.getSuperclassSignature());
      toClass.setSuperclass(s);
    }
    if (fromClass.hasOuterClass()) {
      Optional<de.upb.soot.core.SootClass> outClass = fromClass.getOuterClass();
      soot.SootClass o = getSootClass(outClass, fromClass.getOuterClassSignature());
      toClass.setOuterClass(o);
    }

    // convert fields
    Collection<? extends IField> fields = fromClass.getFields();
    for (IField fromField : fields) {

      soot.SootField f = convertSootField((SootField) fromField);
      toClass.addField(f);
      f.setDeclaringClass(toClass);
      f.setDeclared(true);

    }

    // convert methods
    for (IMethod method : fromClass.getMethods()) {
      de.upb.soot.core.SootMethod fromMethod = (de.upb.soot.core.SootMethod) method;
      soot.SootMethod m = convertSootMethod(toClass, fromMethod);
      if (toClass.getMethodByNameUnsafe(m.getName()) == null) {
        toClass.addMethod(m);
        m.setDeclaringClass(toClass);
        m.setDeclared(true);
      }
      // for (de.upb.soot.core.SootClass ex : fromMethod.getExceptions()) {

      // }
    }
    // add source position into tag
    toClass.addTag(new PositionTag(fromClass.getPosition()));
    toClass.setResolvingLevel(soot.SootClass.BODIES);
    soot.RefType reftype = Scene.v().getRefType(toClass.getName());
    reftype.setSootClass(toClass);
    return toClass;
  }

  public soot.SootField convertSootField(de.upb.soot.core.SootField fromField) {
    return new soot.SootField(fromField.getName(), convertType(fromField.getType()),
        convertModifiers(fromField.getModifiers()));
  }

  public soot.SootMethod convertSootMethod(SootClass toClass, de.upb.soot.core.SootMethod fromMethod) {
    List<soot.Type> types = new ArrayList<>();
    for (Type type : fromMethod.getParameterTypes()) {
      types.add(convertType(type));
    }
    soot.SootMethod toMethod = new SootMethod(fromMethod.getName(), types, convertType(fromMethod.getReturnType()));
    toMethod.setModifiers(convertModifiers(fromMethod.getModifiers()));
    List<soot.SootClass> exceptions = new ArrayList<>();
    // TODO. add exceptions
    toMethod.setExceptions(exceptions);
    // add source position into tag
    toMethod.addTag(new DebuggingInformationTag(fromMethod.getDebugInfo()));

    // set Body
    if (fromMethod.hasActiveBody()) {
      soot.jimple.JimpleBody body = convertBody(toMethod, fromMethod.getActiveBody());
      if (fromMethod.isConcrete()) {
        toMethod.setActiveBody(body);
        toClass.setResolvingLevel(soot.SootClass.BODIES);
      }
    }

    return toMethod;
  }

  public soot.jimple.Stmt convertStmt(IStmt fromStmt) {

    // convert stmts
    Stmt toStmt = null;
    if (fromStmt instanceof JAssignStmt) {
      toStmt = convertAssignStmt(fromStmt);
    } else if (fromStmt instanceof JIdentityStmt) {
      toStmt = convertIdentityStmt(fromStmt);
    } else if (fromStmt instanceof JIfStmt) {
      toStmt = convertIfStmt(fromStmt);
    } else if (fromStmt instanceof JGotoStmt) {
      toStmt = convertGotoStmt(fromStmt);
    } else if (fromStmt instanceof JInvokeStmt) {
      toStmt = convertInvokeStmt(fromStmt);
    } else if (fromStmt instanceof JReturnStmt) {
      toStmt = convertReturnStmt(fromStmt);
    } else if (fromStmt instanceof JReturnVoidStmt) {
      toStmt = convertReturnVoidStmt(fromStmt);
    } else if (fromStmt instanceof JThrowStmt) {
      toStmt = convertThrowStmt(fromStmt);
    } else if (fromStmt instanceof JNopStmt) {
      toStmt = convertNopStmt(fromStmt);
    } else if (fromStmt instanceof JLookupSwitchStmt) {
      toStmt = convertLookSwitchStmt(fromStmt);
    } else if (fromStmt instanceof JTableSwitchStmt) {
      toStmt = convertTableSwitchStmt(fromStmt);
    } else if (fromStmt instanceof JRetStmt) {
      toStmt = convertRetStmt(fromStmt);
    } else if (fromStmt instanceof JEnterMonitorStmt) {
      toStmt = convertEnterMonitorStmt(fromStmt);
    } else if (fromStmt instanceof JExitMonitorStmt) {
      toStmt = convertExitMintorStmt(fromStmt);
    } else if (fromStmt instanceof JBreakpointStmt) {
      toStmt = convertBreakpointStmt(fromStmt);
    } else {
      throw new RuntimeException("Unsupported conversion from " + fromStmt.getClass());
    }
    return toStmt;
  }

  public soot.jimple.JimpleBody convertBody(soot.SootMethod toMethod, Body fromBody) {
    de.upb.soot.core.SootMethod fromMethod = fromBody.getMethod();

    JimpleBody toBody = Jimple.v().newBody(toMethod);

    PatchingChain<Unit> units = toBody.getUnits();
    locals = toBody.getLocals();
    Chain<Trap> traps = toBody.getTraps();
    // need to look at the clone method of body.

    for (de.upb.soot.jimple.basic.Local fromLocal : fromBody.getLocals()) {
      // covert locals
      Local toLocal = new JimpleLocal(fromLocal.toString(), convertType(fromLocal.getType()));
      locals.add(toLocal);
    }

    for (de.upb.soot.jimple.basic.Trap fromTrap : fromBody.getTraps()) {
      // convert traps
      Trap toTrap = null;
      // TODO.
      traps.add(toTrap);
    }
    // reset targets of body
    targets = new HashMap<>();
    for (IStmt fromStmt : fromBody.getStmts()) {
      Stmt toStmt = null;
      if (targets.containsKey(fromStmt)) {
        toStmt = targets.get(fromStmt);
      } else {
        toStmt = convertStmt(fromStmt);
      }
      if (toStmt != null) {
        toStmt.addTag(new DebuggingInformationTag(fromMethod.getDebugInfo()));
        units.add(toStmt);
        
      } else {
        System.out.println(fromStmt.getClass().toString());
      }
    }

    return toBody;
  }

  private Stmt convertBreakpointStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertExitMintorStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertEnterMonitorStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertRetStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertTableSwitchStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertLookSwitchStmt(IStmt fromStmt) {
    JLookupSwitchStmt stmt = (JLookupSwitchStmt) fromStmt;
    List<soot.jimple.IntConstant> lookupValues = new ArrayList<>();
    for (IntConstant c : stmt.getLookupValues()) {
      lookupValues.add((soot.jimple.IntConstant) convertValue(c));
    }
    List<Stmt> targetList = new ArrayList<>();
    for (IStmt t : stmt.getTargets()) {
      // TODO. wala bug
      if (t == null) {
        targetList.add(null);
      } else {
        if (t != fromStmt) {
          Stmt target = getTarget(t);
          targetList.add(target);
        } else {
          targetList.add(null);
        }
      }
    }
    Stmt defaultTarget = getTarget(stmt.getDefaultTarget());
    return Jimple.v().newLookupSwitchStmt(convertValue(stmt.getKey()), lookupValues, targetList, defaultTarget);
  }

  private Stmt convertNopStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertThrowStmt(IStmt fromStmt) {
    // TODO Auto-generated method stub
    return null;
  }

  private Stmt convertReturnVoidStmt(IStmt fromStmt) {
    return Jimple.v().newReturnVoidStmt();
  }

  private Stmt convertReturnStmt(IStmt fromStmt) {
    JReturnStmt stmt = (JReturnStmt) fromStmt;
    Value op = convertValue(stmt.getOp());
    return Jimple.v().newReturnStmt(op);
  }

  private Stmt convertInvokeStmt(IStmt fromStmt) {
    JInvokeStmt stmt = (JInvokeStmt) fromStmt;
    return Jimple.v().newInvokeStmt(convertValue(stmt.getInvokeExpr()));
  }

  private Stmt convertGotoStmt(IStmt fromStmt) {
    JGotoStmt stmt = (JGotoStmt) fromStmt;
    Stmt target = getTarget(stmt.getTarget());
    return Jimple.v().newGotoStmt(target);
  }

  private Stmt convertIfStmt(IStmt fromStmt) {
    JIfStmt stmt = (JIfStmt) fromStmt;
    Stmt target = getTarget(stmt.getTarget());
    return Jimple.v().newIfStmt(convertValue(stmt.getCondition()), target);
  }

  private Stmt convertIdentityStmt(IStmt fromStmt) {
    JIdentityStmt stmt = (JIdentityStmt) fromStmt;
    return Jimple.v().newIdentityStmt(convertValue(stmt.getLeftOp()), convertValue(stmt.getRightOp()));
  }

  private Stmt convertAssignStmt(IStmt fromStmt) {
    JAssignStmt stmt = (JAssignStmt) fromStmt;
    return soot.jimple.Jimple.v().newAssignStmt(convertValue(stmt.getLeftOp()), convertValue(stmt.getRightOp()));
  }

  public soot.Value convertValue(de.upb.soot.jimple.basic.Value from) {
    if (from instanceof de.upb.soot.jimple.basic.Local) {
      de.upb.soot.jimple.basic.Local value = (de.upb.soot.jimple.basic.Local) from;
      return getLocal(value.getName(), convertType(value.getType()));
    } else if (from instanceof de.upb.soot.jimple.common.constant.Constant) {
      return convertConstant((de.upb.soot.jimple.common.constant.Constant) from);
    } else if (from instanceof de.upb.soot.jimple.common.expr.Expr) {
      return convertExpr((de.upb.soot.jimple.common.expr.Expr) from);
    } else if (from instanceof de.upb.soot.jimple.common.ref.Ref) {
      return convertRef((de.upb.soot.jimple.common.ref.Ref) from);
    }
    throw new RuntimeException("can not convert value of type: " + from.getClass().getName());
  }

  public soot.jimple.Ref convertRef(Ref from) {
    soot.jimple.Ref to = null;
    if (from instanceof de.upb.soot.jimple.common.ref.JArrayRef) {
      de.upb.soot.jimple.common.ref.JArrayRef ref = (de.upb.soot.jimple.common.ref.JArrayRef) from;
      to = Jimple.v().newArrayRef(convertValue(ref.getBase()), convertValue(ref.getIndex()));
    } else if (from instanceof de.upb.soot.jimple.common.ref.JStaticFieldRef) {
      JStaticFieldRef ref = (de.upb.soot.jimple.common.ref.JStaticFieldRef) from;
      SootFieldRef field = createSootFieldRef(ref.getFieldSignature(), true);
      to = Jimple.v().newStaticFieldRef(field);
    } else if (from instanceof de.upb.soot.jimple.common.ref.JInstanceFieldRef) {
      JInstanceFieldRef ref = (de.upb.soot.jimple.common.ref.JInstanceFieldRef) from;
      SootFieldRef field = createSootFieldRef(ref.getFieldSignature(), false);
      to = Jimple.v().newInstanceFieldRef(convertValue(ref.getBase()), field);
    } else if (from instanceof de.upb.soot.jimple.common.ref.JParameterRef) {
      to = Jimple.v().newParameterRef(convertType(from.getType()),
          ((de.upb.soot.jimple.common.ref.JParameterRef) from).getIndex());
    } else if (from instanceof de.upb.soot.jimple.common.ref.JThisRef) {
      to = Jimple.v().newThisRef((RefType) convertType(from.getType()));
    } else if (from instanceof de.upb.soot.jimple.common.ref.JCaughtExceptionRef) {
      to = Jimple.v().newCaughtExceptionRef();
    }
    if (to == null) {
      throw new RuntimeException("can not convert ref of type: " + from.getClass().getName());
    }
    return to;
  }

  public soot.jimple.Expr convertExpr(Expr from) {
    soot.jimple.Expr to = null;
    if (from instanceof de.upb.soot.jimple.common.expr.JCmpExpr) {
      de.upb.soot.jimple.common.expr.JCmpExpr expr = (de.upb.soot.jimple.common.expr.JCmpExpr) from;
      to = Jimple.v().newCmpExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JCmpgExpr) {
      de.upb.soot.jimple.common.expr.JCmpgExpr expr = (de.upb.soot.jimple.common.expr.JCmpgExpr) from;
      to = Jimple.v().newCmpgExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JCmplExpr) {
      de.upb.soot.jimple.common.expr.JCmplExpr expr = (de.upb.soot.jimple.common.expr.JCmplExpr) from;
      to = Jimple.v().newCmplExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JGeExpr) {
      de.upb.soot.jimple.common.expr.JGeExpr expr = (de.upb.soot.jimple.common.expr.JGeExpr) from;
      to = Jimple.v().newGeExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JGtExpr) {
      de.upb.soot.jimple.common.expr.JGtExpr expr = (de.upb.soot.jimple.common.expr.JGtExpr) from;
      to = Jimple.v().newGtExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JLeExpr) {
      de.upb.soot.jimple.common.expr.JLeExpr expr = (de.upb.soot.jimple.common.expr.JLeExpr) from;
      to = Jimple.v().newLeExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JLtExpr) {
      de.upb.soot.jimple.common.expr.JLtExpr expr = (de.upb.soot.jimple.common.expr.JLtExpr) from;
      to = Jimple.v().newLtExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JEqExpr) {
      de.upb.soot.jimple.common.expr.JEqExpr expr = (de.upb.soot.jimple.common.expr.JEqExpr) from;
      to = Jimple.v().newEqExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JNeExpr) {
      de.upb.soot.jimple.common.expr.JNeExpr expr = (de.upb.soot.jimple.common.expr.JNeExpr) from;
      to = Jimple.v().newNeExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JAddExpr) {
      de.upb.soot.jimple.common.expr.JAddExpr expr = (de.upb.soot.jimple.common.expr.JAddExpr) from;
      to = Jimple.v().newAddExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JSubExpr) {
      de.upb.soot.jimple.common.expr.JSubExpr expr = (de.upb.soot.jimple.common.expr.JSubExpr) from;
      to = Jimple.v().newSubExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JDivExpr) {
      de.upb.soot.jimple.common.expr.JDivExpr expr = (de.upb.soot.jimple.common.expr.JDivExpr) from;
      to = Jimple.v().newDivExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JMulExpr) {
      de.upb.soot.jimple.common.expr.JMulExpr expr = (de.upb.soot.jimple.common.expr.JMulExpr) from;
      to = Jimple.v().newMulExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JRemExpr) {
      de.upb.soot.jimple.common.expr.JRemExpr expr = (de.upb.soot.jimple.common.expr.JRemExpr) from;
      to = Jimple.v().newRemExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JAndExpr) {
      de.upb.soot.jimple.common.expr.JAndExpr expr = (de.upb.soot.jimple.common.expr.JAndExpr) from;
      to = Jimple.v().newAndExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JOrExpr) {
      de.upb.soot.jimple.common.expr.JOrExpr expr = (de.upb.soot.jimple.common.expr.JOrExpr) from;
      to = Jimple.v().newOrExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JShlExpr) {
      de.upb.soot.jimple.common.expr.JShlExpr expr = (de.upb.soot.jimple.common.expr.JShlExpr) from;
      to = Jimple.v().newShlExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JShrExpr) {
      de.upb.soot.jimple.common.expr.JShrExpr expr = (de.upb.soot.jimple.common.expr.JShrExpr) from;
      to = Jimple.v().newShrExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JUshrExpr) {
      de.upb.soot.jimple.common.expr.JUshrExpr expr = (de.upb.soot.jimple.common.expr.JUshrExpr) from;
      to = Jimple.v().newUshrExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JXorExpr) {
      de.upb.soot.jimple.common.expr.JXorExpr expr = (de.upb.soot.jimple.common.expr.JXorExpr) from;
      to = Jimple.v().newXorExpr(convertValue(expr.getOp1()), convertValue(expr.getOp2()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JCastExpr) {
      de.upb.soot.jimple.common.expr.JCastExpr expr = (de.upb.soot.jimple.common.expr.JCastExpr) from;
      to = Jimple.v().newCastExpr(convertValue(expr.getOp()), convertType(expr.getCastType()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JInstanceOfExpr) {
      de.upb.soot.jimple.common.expr.JInstanceOfExpr expr = (de.upb.soot.jimple.common.expr.JInstanceOfExpr) from;
      to = Jimple.v().newInstanceOfExpr(convertValue(expr.getOp()), convertType(expr.getCheckType()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.AbstractInvokeExpr) {
      de.upb.soot.jimple.common.expr.AbstractInvokeExpr e = (de.upb.soot.jimple.common.expr.AbstractInvokeExpr) from;
      List<soot.Value> args = new ArrayList<>();
      for (de.upb.soot.jimple.basic.Value arg : e.getArgs()) {
        args.add(convertValue(arg));
      }
      if (from instanceof de.upb.soot.jimple.common.expr.JSpecialInvokeExpr) {
        de.upb.soot.jimple.common.expr.JSpecialInvokeExpr expr = (de.upb.soot.jimple.common.expr.JSpecialInvokeExpr) from;
        SootMethodRef method = createSootMethodRef(expr.getMethodSignature(), false);
        to = Jimple.v().newSpecialInvokeExpr((Local) convertValue(expr.getBase()), method, args);
      } else if (from instanceof de.upb.soot.jimple.common.expr.JInterfaceInvokeExpr) {
        de.upb.soot.jimple.common.expr.JInterfaceInvokeExpr expr
            = (de.upb.soot.jimple.common.expr.JInterfaceInvokeExpr) from;
        SootMethodRef method = createSootMethodRef(expr.getMethodSignature(), false);
        if (method.declaringClass().isInterface()) {
          to = Jimple.v().newInterfaceInvokeExpr((Local) convertValue(expr.getBase()), method, args);
        } else {
          to = Jimple.v().newSpecialInvokeExpr((Local) convertValue(expr.getBase()), method, args);
        }
      } else if (from instanceof de.upb.soot.jimple.common.expr.JVirtualInvokeExpr) {
        de.upb.soot.jimple.common.expr.JVirtualInvokeExpr expr = (de.upb.soot.jimple.common.expr.JVirtualInvokeExpr) from;
        SootMethodRef method = createSootMethodRef(expr.getMethodSignature(), false);
        to = Jimple.v().newVirtualInvokeExpr((Local) convertValue(expr.getBase()), method, args);
      } else if (from instanceof de.upb.soot.jimple.common.expr.JStaticInvokeExpr) {
        de.upb.soot.jimple.common.expr.JStaticInvokeExpr expr = (de.upb.soot.jimple.common.expr.JStaticInvokeExpr) from;
        SootMethodRef method = createSootMethodRef(expr.getMethodSignature(), true);
        to = Jimple.v().newStaticInvokeExpr(method, args);
      } else if (from instanceof de.upb.soot.jimple.common.expr.JDynamicInvokeExpr) {
        // TODO.
      }
    } else if (from instanceof de.upb.soot.jimple.common.expr.JNewExpr) {
      de.upb.soot.jimple.common.expr.JNewExpr expr = (de.upb.soot.jimple.common.expr.JNewExpr) from;
      to = Jimple.v().newNewExpr((RefType) convertType(expr.getType()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JNewArrayExpr) {
      de.upb.soot.jimple.common.expr.JNewArrayExpr expr = (de.upb.soot.jimple.common.expr.JNewArrayExpr) from;
      to = Jimple.v().newNewArrayExpr(convertType(expr.getBaseType()), convertValue(expr.getSize()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JNewMultiArrayExpr) {
      de.upb.soot.jimple.common.expr.JNewMultiArrayExpr expr = (de.upb.soot.jimple.common.expr.JNewMultiArrayExpr) from;
      List<soot.Value> sizes = new ArrayList<>();
      for (de.upb.soot.jimple.basic.Value s : expr.getSizes()) {
        sizes.add(convertValue(s));
      }
      to = Jimple.v().newNewMultiArrayExpr((soot.ArrayType) convertType(expr.getBaseType()), sizes);
    } else if (from instanceof de.upb.soot.jimple.common.expr.JLengthExpr) {
      de.upb.soot.jimple.common.expr.JLengthExpr expr = (de.upb.soot.jimple.common.expr.JLengthExpr) from;
      to = Jimple.v().newLengthExpr(convertValue(expr.getOp()));
    } else if (from instanceof de.upb.soot.jimple.common.expr.JNegExpr) {
      de.upb.soot.jimple.common.expr.JNegExpr expr = (de.upb.soot.jimple.common.expr.JNegExpr) from;
      to = Jimple.v().newNegExpr(convertValue(expr.getOp()));
    }
    if (to == null) {
      throw new RuntimeException("can not convert expr of type: " + from.getClass().getName());
    }
    return to;
  }

  public soot.jimple.Constant convertConstant(Constant from) {
    if (from instanceof de.upb.soot.jimple.common.constant.IntConstant) {
      de.upb.soot.jimple.common.constant.IntConstant constant = (de.upb.soot.jimple.common.constant.IntConstant) from;
      return soot.jimple.IntConstant.v(constant.value);
    } else if (from instanceof de.upb.soot.jimple.common.constant.LongConstant) {
      de.upb.soot.jimple.common.constant.LongConstant constant = (de.upb.soot.jimple.common.constant.LongConstant) from;
      return soot.jimple.LongConstant.v(constant.value);
    } else if (from instanceof de.upb.soot.jimple.common.constant.DoubleConstant) {
      de.upb.soot.jimple.common.constant.DoubleConstant constant = (de.upb.soot.jimple.common.constant.DoubleConstant) from;
      return soot.jimple.DoubleConstant.v(constant.value);
    } else if (from instanceof de.upb.soot.jimple.common.constant.FloatConstant) {
      de.upb.soot.jimple.common.constant.FloatConstant constant = (de.upb.soot.jimple.common.constant.FloatConstant) from;
      return soot.jimple.FloatConstant.v(constant.value);
    } else if (from instanceof de.upb.soot.jimple.common.constant.StringConstant) {
      de.upb.soot.jimple.common.constant.StringConstant constant = (de.upb.soot.jimple.common.constant.StringConstant) from;
      return soot.jimple.StringConstant.v(constant.value);
    } else if (from instanceof de.upb.soot.jimple.common.constant.ClassConstant) {
      de.upb.soot.jimple.common.constant.ClassConstant constant = (de.upb.soot.jimple.common.constant.ClassConstant) from;
      return soot.jimple.ClassConstant.v(constant.value);
    } else if (from instanceof de.upb.soot.jimple.common.constant.NullConstant) {
      return soot.jimple.NullConstant.v();
    } else if (from instanceof de.upb.soot.jimple.common.constant.MethodHandle) {
      throw new RuntimeException("Not implemented yet!");
    } else {
      throw new RuntimeException("can not convert constant of type: " + from.getClass().getName());
    }
  }

  public soot.Type convertType(de.upb.soot.jimple.common.type.Type from) {
    soot.Type to = UnknownType.v();
    if (!(from instanceof de.upb.soot.jimple.common.type.UnknownType)) {
      if (from instanceof de.upb.soot.jimple.common.type.BooleanType) {
        return soot.BooleanType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.ByteType) {
        return soot.ByteType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.CharType) {
        return soot.CharType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.ShortType) {
        return soot.ShortType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.IntType) {
        return soot.IntType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.LongType) {
        return soot.LongType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.FloatType) {
        return soot.FloatType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.DoubleType) {
        return soot.DoubleType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.ArrayType) {
        de.upb.soot.jimple.common.type.ArrayType type = (de.upb.soot.jimple.common.type.ArrayType) from;
        return soot.ArrayType.v(convertType(type.baseType), type.numDimensions);
      } else if (from instanceof de.upb.soot.jimple.common.type.RefType) {
        de.upb.soot.jimple.common.type.RefType type = (de.upb.soot.jimple.common.type.RefType) from;
        String className = type.getTypeSignature().toString();
        return soot.RefType.v(className);
      } else if (from instanceof de.upb.soot.jimple.common.type.NullType) {
        return soot.NullType.v();
      } else if (from instanceof de.upb.soot.jimple.common.type.AnySubType) {
        de.upb.soot.jimple.common.type.AnySubType type = (de.upb.soot.jimple.common.type.AnySubType) from;
        return soot.AnySubType.v((soot.RefType) convertType(type.getBase()));
      } else if (from instanceof de.upb.soot.jimple.common.type.VoidType) {
        return soot.VoidType.v();
      } else {
        throw new RuntimeException("can not convert type from " + from.toString());
      }
    }
    return to;
  }

  public int convertModifiers(EnumSet<Modifier> modifiers) {
    int bytecode = 0;
    for (Modifier modifier : modifiers) {
      bytecode = bytecode | modifier.getBytecode();
    }
    return bytecode;
  }

  private SootMethodRef createSootMethodRef(MethodSignature methodSig, boolean isStatic) {
    String className = methodSig.declClassSignature.getFullyQualifiedName();
    SootClass declaringClass = null;
    if (!Scene.v().containsClass(className)) {
      if (fromClasses.stream().filter(c -> c.getName().equals(className)).findFirst().isPresent()) {
        declaringClass = new SootClass(className);
        Scene.v().addClass(declaringClass);
      } else {
        declaringClass = Scene.v().forceResolve(className, soot.SootClass.SIGNATURES);
      }
    }
    declaringClass = Scene.v().getSootClass(className);
    List<soot.Type> parameterTypes = new ArrayList<>();
    for (de.upb.soot.signatures.TypeSignature typeSig : methodSig.parameterSignatures) {
      String typeName = typeSig.toString();
      if (!Scene.v().containsType(typeSig.toString())) {
        Scene.v().addRefType(RefType.v(typeName));
      }
      parameterTypes.add(Scene.v().getType(typeName));
    }
    soot.Type returnType = Scene.v().getType(methodSig.typeSignature.toString());
    return Scene.v().makeMethodRef(declaringClass, methodSig.name, parameterTypes, returnType, isStatic);
  }

  private SootFieldRef createSootFieldRef(FieldSignature fieldSig, boolean isStatic) {
    SootClass declaringClass = SootResolver.v().makeClassRef(fieldSig.declClassSignature.getFullyQualifiedName());
    soot.Type type = Scene.v().getType(fieldSig.typeSignature.toString());
    return Scene.v().makeFieldRef(declaringClass, fieldSig.name, type, isStatic);
  }

  private Stmt getTarget(IStmt key) {
    if (key == null) {
      // TODO. fix this
      return null;

    }
    // TODO. what about the case when the target is the stmt itself?
    Stmt target = null;
    if (this.targets.containsKey(key)) {
      target = this.targets.get(key);
    } else {
      target = convertStmt(key);
      this.targets.put(key, target);
    }
    return target;

  }

  private soot.SootClass getSootClass(Optional<de.upb.soot.core.SootClass> op, Optional<JavaClassSignature> sigOp) {
    String className = sigOp.get().getFullyQualifiedName();
    if (!Scene.v().containsClass(className)) {
      if (op.isPresent()) {
        return convertSootClass(op.get());
      } else {
        return Scene.v().getSootClass(className);
      }
    } else {
      return Scene.v().getSootClass(className);
    }
  }

  private Local getLocal(String name, soot.Type type) {
    for (Local l : locals) {
      if (l.getName().equals(name) && l.getType().equals(type)) {
        return l;
      }
    }
    JimpleLocal local = new JimpleLocal(name, type);
    locals.add(local);
    return local;
  }

  public void convertAllClasses() {
    for (de.upb.soot.core.SootClass sootClass : fromClasses) {
      this.convertSootClass(sootClass);
    }
  }
}
