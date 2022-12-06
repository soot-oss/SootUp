package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypePromotionVisitor extends TypeChecker {

  private boolean fail = false;
  private boolean typingChanged = true;

  private static final Logger logger = LoggerFactory.getLogger(TypePromotionVisitor.class);

  public TypePromotionVisitor(
      Body.BodyBuilder builder, AugEvalFunction evalFunction, BytecodeHierarchy hierarchy) {
    super(builder, evalFunction, hierarchy);
  }

  public Typing getPromotedTyping(Typing typing) {
    setTyping(typing);
    this.fail = false;
    while (typingChanged && !fail) {
      this.typingChanged = false;
      for (Stmt stmt : getBody().getStmts()) {
        stmt.accept(this);
      }
    }
    if (fail) {
      return null;
    }
    return getTyping();
  }

  public void visit(Value value, Type stdType, Stmt stmt) {
    AugEvalFunction evalFunction = getFuntion();
    BytecodeHierarchy hierarchy = getHierarchy();
    Body body = getBody();
    Typing typing = getTyping();
    Type evaType = evalFunction.evaluate(typing, value, stmt, body);
    if (evaType.equals(stdType)) {
      return;
    }
    if (!hierarchy.isAncestor(stdType, evaType)) {
      logger.error(
          stdType.toString()
              + " is not compatible with the value "
              + value.toString()
              + " in the statement: "
              + stmt.toString()
              + "!");
      this.fail = true;
    } else if (value instanceof Local && TypeUtils.isIntermediateType(evaType)) {
      Local local = (Local) value;
      Type promotedType = promote(evaType, stdType);
      if (promotedType != null && !promotedType.equals(evaType)) {
        typing.set(local, promotedType);
        this.typingChanged = true;
      }
    }
  }

  private Type promote(Type low, Type high) {
    if (low instanceof PrimitiveType.Integer1Type) {
      if (high instanceof PrimitiveType.IntType) {
        return PrimitiveType.getInteger127();
      } else if (high instanceof PrimitiveType.ShortType) {
        return PrimitiveType.getByte();
      } else if (high instanceof IntegerType) {
        return high;
      } else {
        logger.error(low + " cannot be promoted with the supertype " + high.toString() + "!");
        return null;
      }
    } else if (low instanceof PrimitiveType.Integer127Type) {
      if (high instanceof PrimitiveType.ShortType) {
        return PrimitiveType.getByte();
      } else if (high instanceof PrimitiveType.IntType) {
        return PrimitiveType.getInteger127();
      } else if (high instanceof PrimitiveType.ByteType
          || high instanceof PrimitiveType.CharType
          || high instanceof PrimitiveType.Integer32767Type) {
        return high;
      } else {
        logger.error(low + " cannot be promoted with the supertype " + high.toString() + "!");
        return null;
      }
    } else if (low instanceof PrimitiveType.Integer32767Type) {
      if (high instanceof PrimitiveType.IntType) {
        return PrimitiveType.getInteger32767();
      } else if (high instanceof PrimitiveType.ShortType
          || high instanceof PrimitiveType.CharType) {
        return high;
      } else {
        logger.error(low + " cannot be promoted with the supertype " + high.toString() + "!");
        return null;
      }
    } else {
      logger.error(low + " cannot be promoted with the supertype " + high.toString() + "!");
      return null;
    }
  }
}
