/*Author Kaustubh Kelkar*/

package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author Kaustubh Kelkar */
public class StaticMethodTest extends MinimalTestSuiteBase {

  SootClass sootClass = new SootClass();
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "staticMethod", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  public boolean isMethodStatic(){
    if (sootClass.getFields().contains("static")) return true;
    else return false;
  }

  @Override
  public List<String> getJimpleLines() {
    return Stream.of(
            "$r0 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r0.<java.io.PrintStream: void println(java.lang.String)>(\"static method\")",
            "return")
        .collect(Collectors.toList());
  }
}
