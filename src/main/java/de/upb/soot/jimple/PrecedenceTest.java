package de.upb.soot.jimple;

public class PrecedenceTest {
  public static boolean needsBrackets(ValueBox subExprBox, Value expr) {
    Value sub = subExprBox.getValue();
    if (!(sub instanceof Precedence)) {
      return false;
    }
    Precedence subP = (Precedence) sub;
    Precedence exprP = (Precedence) expr;
    return subP.getPrecedence() < exprP.getPrecedence();
  }

  public static boolean needsBracketsRight(ValueBox subExprBox, Value expr) {
    Value sub = subExprBox.getValue();
    if (!(sub instanceof Precedence)) {
      return false;
    }
    Precedence subP = (Precedence) sub;
    Precedence exprP = (Precedence) expr;
    return subP.getPrecedence() <= exprP.getPrecedence();
  }
}
