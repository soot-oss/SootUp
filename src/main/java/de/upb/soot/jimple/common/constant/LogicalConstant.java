package de.upb.soot.jimple.common.constant;

public interface LogicalConstant extends Constant {
  LogicalConstant and(LogicalConstant c);

  LogicalConstant or(LogicalConstant c);

  LogicalConstant xor(LogicalConstant c);
}
