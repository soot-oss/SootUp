package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.util.EscapedWriter;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

/** @author Kaustubh Kelkar */
public class MethodOverridingTest extends MinimalTestSuiteBase {

  public MethodSignature getMethodSignature() {

    return identifierFactory.getMethodSignature(
        "calculateArea",
        identifierFactory.getClassType("MethodOverridingSubclass"),
        "void",
        Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "r0 := @this: MethodOverridingSubclass",
            "$r1 = <java.lang.System: java.io.PrintStream out>",
            "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Inside MethodOverridingSubclass-calculateArea()\")",
            "return")
        .collect(Collectors.toCollection(ArrayList::new));
  }

  // TODO [ms]: move test to an appropriate place
  @Test
  public void testSth() {
    SootClass clazz = loadClass(getDeclaredClassSignature());
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      new Printer(Printer.Option.UseImports).printTo(clazz, writerOut);
    }
    System.out.println(writer.toString());
  }
}
