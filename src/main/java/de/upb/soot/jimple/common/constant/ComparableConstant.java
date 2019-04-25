package de.upb.soot.jimple.common.constant;

public interface ComparableConstant extends Constant {
  ComparableConstant equalEqual(ComparableConstant c);

  ComparableConstant notEqual(ComparableConstant c);
}
