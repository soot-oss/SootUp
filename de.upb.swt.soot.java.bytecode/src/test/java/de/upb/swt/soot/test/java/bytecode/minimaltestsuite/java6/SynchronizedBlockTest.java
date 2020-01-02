package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class SynchronizedBlockTest extends MinimalBytecodeTestSuiteBase {

  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        "run", getDeclaredClassSignature(), "void", Collections.emptyList());
  }

  /** TODO assertTrue(isSynchronized); */
  @Override
  public List<String> expectedBodyStmts() {
    return Stream.of(
            "l0 := @this: SynchronizedBlock",
            "$stack3 = l0.<SynchronizedBlock: LSender; sender>",
            "l1 = $stack3",
            "entermonitor $stack3",
            "label1:",
            "$stack5 = l0.<SynchronizedBlock: LSender; sender>",
            "$stack4 = l0.<SynchronizedBlock: java.lang.String; msg>",
            "virtualinvoke $stack5.<Sender: void send(java.lang.String)>($stack4)",
            "$stack6 = l1",
            "exitmonitor $stack6",
            "label2:",
            "goto label5",
            "label3:",
            "$stack7 := @caughtexception",
            "l2 = $stack7",
            "$stack8 = l1",
            "exitmonitor $stack8",
            "label4:",
            "throw l2",
            "label5:",
            "return",
            "catch java.lang.Throwable from label1 to label2 with label3",
            "catch java.lang.Throwable from label3 to label4 with label3")
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
