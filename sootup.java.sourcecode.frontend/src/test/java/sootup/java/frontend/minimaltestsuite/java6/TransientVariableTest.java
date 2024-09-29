package sootup.java.frontend.minimaltestsuite.java6;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.signatures.MethodSignature;
import sootup.java.frontend.minimaltestsuite.MinimalSourceTestSuiteBase;

/** @author Kaustubh Kelkar */
@Tag("Java8")
public class TransientVariableTest extends MinimalSourceTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        getDeclaredClassSignature(), "transientVariable", "void", Collections.emptyList());
  }

  @Disabled
  public void testTransientVar() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField ->
                    sootField.getName().equals("transientVar")
                        && sootField.getModifiers().contains(FieldModifier.TRANSIENT)));
  }

  /**
   *
   *
   * <pre>
   *     public void transientVariable(){
   * System.out.println(transientVar);
   * }
   * </pre>
   */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: TransientVariable",
            "r1 = <java.lang.System: java.io.PrintStream out>",
            "i0 = r0.<TransientVariable: int transientVar>",
            "virtualinvoke r1.<java.io.PrintStream: void println(int)>(i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
