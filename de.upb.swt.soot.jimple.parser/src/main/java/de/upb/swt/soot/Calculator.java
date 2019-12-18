package de.upb.swt.soot;

import de.upb.swt.soot.jimple.CalculatorLexer;
import de.upb.swt.soot.jimple.CalculatorParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Calculator {
  /*
    public static void main(String[] args) {
      JimpleReader jparser = new JimpleReader();
      jparser.setIdentifierFactory(JavaIdentifierFactory.getInstance());
    }
  */

  public static void main(String[] args) {

    Calculator calculator = new Calculator();
    System.out.println(calculator.calculate("2 + 5")); // 7.0
    System.out.println(calculator.calculate("2 * 5")); // 10.0
    System.out.println(calculator.calculate("5 - 3")); // 2.0
    System.out.println(calculator.calculate("5 / 3")); // 1.6666666666666667
    System.out.println(
        calculator.calculate("5 # 3")); // Error: line 1:2 token recognition error at: '#'
  }

  private Double calculate(String source) {
    CodePointCharStream input = CharStreams.fromString(source);
    return compile(input);
  }

  private Double compile(CharStream source) {
    CalculatorLexer lexer = new CalculatorLexer(source);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    CalculatorParser parser = new CalculatorParser(tokenStream);
    ParseTree tree = parser.operation();
    CalculatorVisitorImpl visitor = new CalculatorVisitorImpl();
    return visitor.visit(tree);
  }
}
