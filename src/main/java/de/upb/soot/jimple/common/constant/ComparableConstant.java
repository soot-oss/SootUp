package de.upb.soot.jimple.common.constant;

public interface ComparableConstant extends Constant {
  BooleanConstant equalEqual(ComparableConstant c);

  BooleanConstant notEqual(ComparableConstant c);
}
