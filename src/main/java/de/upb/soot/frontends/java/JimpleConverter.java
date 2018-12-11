package de.upb.soot.frontends.java;

import de.upb.soot.core.Body;
import de.upb.soot.jimple.common.stmt.IStmt;

import soot.Local;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
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

    PatchingChain<Unit> units = ret.getUnits();
    Chain<Local> locals = ret.getLocals();
    Chain<Trap> traps = ret.getTraps();
    // need to look at the clone method of body.

    for (IStmt fromStmt : body.getStmts()) {
      // convert stmts
      Stmt toStmt = null;
      // TODO.
      units.add(toStmt);
    }

    for (de.upb.soot.jimple.basic.Local fromLocal : body.getLocals()) {
      // covert locals
      Local toLocal = null;
      // TODO.
      locals.add(toLocal);
    }

    for (de.upb.soot.jimple.basic.Trap fromTrap : body.getTraps()) {
      // convert traps
      Trap toTrap = null;
      // TODO.
      traps.add(toTrap);
    }

    return ret;
  }

}
