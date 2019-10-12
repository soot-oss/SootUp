package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class TransientVariableTest extends MinimalTestSuiteBase {
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
            "transientVariable", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Test
  @Override
  public void defaultTest() {
    super.defaultTest();
    assertTrue(
            getFields().stream()
                    .anyMatch(
                            sootField -> {
                              return sootField.getName().equals("transientVar")
                                      && sootField.getModifiers().contains(Modifier.TRANSIENT);
                            }));
  }

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