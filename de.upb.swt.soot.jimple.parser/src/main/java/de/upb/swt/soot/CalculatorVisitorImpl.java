package de.upb.swt.soot;

import de.upb.swt.soot.jimple.CalculatorBaseVisitor;
import de.upb.swt.soot.jimple.CalculatorParser;

public class CalculatorVisitorImpl extends CalculatorBaseVisitor<Double> {

  @Override
  public Double visitOperation(CalculatorParser.OperationContext ctx) {
    if (ctx.operator == null) {
      throw new UnsupportedOperationException(
          "An operator of +, -, /, * is required to perform the operation");
    }
    String operator = ctx.operator.getText();
    double left = Double.parseDouble(ctx.left.getText());
    double right = Double.parseDouble(ctx.right.getText());

    switch (operator) {
      case "+":
        return left + right;
      case "-":
        return left - right;
      case "/":
        return left / right;
      case "*":
        return left * right;
      default:
        throw new UnsupportedOperationException("Calculator does not support " + operator);
    }
  }
}
