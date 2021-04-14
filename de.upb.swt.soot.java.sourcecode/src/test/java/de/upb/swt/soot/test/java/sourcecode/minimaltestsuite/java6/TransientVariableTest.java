package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalSourceTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Ignore;

/** @author Kaustubh Kelkar */
public class TransientVariableTest extends MinimalSourceTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "transientVariable", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Ignore
  public void testTransientVar() {
    SootClass<?>  clazz = loadClass(getDeclaredClassSignature());
    assertTrue(
        clazz.getFields().stream()
            .anyMatch(
                sootField ->
                    sootField.getName().equals("transientVar")
                        && sootField.getModifiers().contains(Modifier.TRANSIENT)));
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
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = r0.<TransientVariable: int transientVar>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(int)>($i0)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
