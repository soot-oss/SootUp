package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class VirtualMethodTest extends MinimalTestSuiteBase {

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "virtualMethodDemo", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: VirtualMethod",
            "$r1 = new TempEmployee",
            "specialinvoke $r1.<TempEmployee: void <init>(int,int)>(1500, 150)",
            "$r2 = new RegEmployee",
            "specialinvoke $r2.<RegEmployee: void <init>(int,int)>(1500, 500)",
            "$r3 = <java.lang.System: java.io.PrintStream out>",
            "$i0 = virtualinvoke $r1.<Employee: int getSalary()>()",
            "virtualinvoke $r3.<java.io.PrintStream: void println(int)>($i0)",
            "$r4 = <java.lang.System: java.io.PrintStream out>",
            "$i1 = virtualinvoke $r2.<Employee: int getSalary()>()",
            "virtualinvoke $r4.<java.io.PrintStream: void println(int)>($i1)",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Test
  public void defaultTest() {
    SootMethod method = loadMethod(getMethodSignature());
    assertJimpleStmts(method, expectedBodyStmts());
  }
}
