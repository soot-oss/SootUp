/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class SwitchCaseStatementTest extends MinimalTestSuiteBase {

  private String methodName;
  private String methodSignature;
  private List<String> jimpleLines;
  private List<String> methodParameters;

  @Test
  public void defaultTest() {
    HashMap<String, HashMap<String, Object>> methodList = setValues();
    Set<String> methodListKeys = methodList.keySet();

    for (String methodListKey : methodListKeys) {
      methodName = methodListKey;
      HashMap<String, Object> mv = methodList.get(methodListKey);
      methodSignature = (String) mv.get("methodSignature");
      methodParameters = (List<String>) mv.get("methodParameters");
      jimpleLines = (List<String>) mv.get("jimpleLines");

      super.defaultTest();
    }
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), methodSignature, methodParameters);
  }

  @Override
  public List<String> expectedBodyStmts() {
    return jimpleLines;
  }

  public HashMap<String, HashMap<String, Object>> setValues() {
    HashMap<String, HashMap<String, Object>> methodList = new HashMap<>();
    HashMap<String, Object> methodValues = new HashMap<>();

    methodValues.put("methodSignature", "java.lang.String");
    methodValues.put("methodParameters", Collections.singletonList("java.lang.String"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: SwitchCaseStatement",
                "$r1 := @parameter0: java.lang.String",
                "$r2 = \"\"",
                "$r3 = staticinvoke <SwitchCaseStatement$Color: SwitchCaseStatement$Color valueOf(java.lang.String)>($r1)",
                "$r4 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
                "if $r3 == $r4 goto $r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
                "$r5 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
                "if $r3 == $r5 goto $r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
                "goto [?= $r2 = \"invalid color\"]",
                "$r6 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color RED>",
                "$r2 = \"color red detected\"",
                "goto [?= return $r2]",
                "$r7 = <SwitchCaseStatement$Color: SwitchCaseStatement$Color GREEN>",
                "$r2 = \"color green detected\"",
                "goto [?= return $r2]",
                "$r2 = \"invalid color\"",
                "goto [?= return $r2]",
                "return $r2")
            .collect(Collectors.toList()));
    methodList.put("switchCaseStatementEnum", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.emptyList());
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: SwitchCaseStatement",
                "$i0 = 2",
                "$r1 = null",
                "lookupswitch($i0) {     "
                    + "case 1: goto goto [?= return];     "
                    + "case 2: goto null;     "
                    + "case 3: goto null;     "
                    + "default: goto $r1 = \"number 1 detected\"; }",
                "$r1 = \"number 1 detected\"",
                "goto [?= return]",
                "$r1 = \"number 2 detected\"",
                "goto [?= return]",
                "$r1 = \"number 3 detected\"",
                "goto [?= return]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("switchCaseStatementInt", methodValues);

    return methodList;
  }
}
