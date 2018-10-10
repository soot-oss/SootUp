package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.jimple.common.stmt.IStmt;

import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.util.Chain;

/**
 * An IR Converter between old and new soot.
 * 
 * @author Linghui Luo
 *
 */
public class JimpleConverter {

  public soot.SootClass convertSootClass(de.upb.soot.core.SootClass fromClass) {
    soot.SootClass toClass = new SootClass(fromClass.getName());
    // TODO. conversion

    // add source position into tag
    toClass.addTag(new PositionTag(fromClass.getPosition()));
    return toClass;
  }

  public soot.SootMethod convertSootMethod(de.upb.soot.core.SootMethod fromMethod) {
    soot.SootMethod toMethod = new SootMethod(null, null, null);
    // TODO. conversion

    // add source position into tag
    toMethod.addTag(new DebuggingInformationTag(fromMethod.getDebugInfo()));
    return toMethod;
  }

  public soot.jimple.JimpleBody convertBody(Body body) {
    de.upb.soot.core.SootMethod fromMethod = body.getMethod();
    soot.SootMethod toMethod = convertSootMethod(fromMethod);

    JimpleBody ret = Jimple.v().newBody(toMethod);

    Chain<Local> locals = ret.getLocals();
    PatchingChain<Unit> units = ret.getUnits();

    for (de.upb.soot.jimple.basic.Local fromLocal : body.getLocals()) {
      // covert locals
      Local toLocal = null;
      locals.add(toLocal);
    }

    for (IStmt stmt : body.getStmts()) {
      // convert stmt
      Unit toUnit = null;

      units.add(toUnit);
    }

    return ret;

  }

}
