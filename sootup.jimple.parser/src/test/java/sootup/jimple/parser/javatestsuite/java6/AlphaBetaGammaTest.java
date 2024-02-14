package sootup.jimple.parser.javatestsuite.java6;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.jimple.parser.javatestsuite.JimpleTestSuiteBase;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("Java8")
public class AlphaBetaGammaTest extends JimpleTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "αβγ", "void", Collections.emptyList());
  }

  @Test
  public void test() {
    SootMethod method = loadMethod(getMethodSignature());
    assertEquals("αβγ", method.getName());
  }
}
