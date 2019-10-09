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
public class MethodReturningVarTest extends MinimalTestSuiteBase {
  private String methodName;
  private String methodSignature;
  private List<String> jimpleLines;

  @Test
  public void defaultTest() {
    HashMap<String, HashMap<String, Object>> methodList = setValues();
    Set<String> methodListKeys = methodList.keySet();

    for (String methodListKey : methodListKeys) {
      methodName = methodListKey;
      HashMap<String, Object> mv = methodList.get(methodListKey);
      methodSignature = (String) mv.get("methodSignature");
      jimpleLines = (List<String>) mv.get("jimpleLines");

      test(expectedBodyStmts(), getMethodSignature());
    }
  }

  @Override
  public MethodSignature getMethodSignature() {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), methodSignature, Collections.emptyList());
  }

  @Override
  public List<String> expectedBodyStmts() {
    return jimpleLines;
  }

  private HashMap<String, HashMap<String, Object>> setValues() {
    HashMap<String, HashMap<String, Object>> methodList = new HashMap<>();
    HashMap<String, Object> methodValues = new HashMap<>();

    methodValues.put("methodSignature", "short");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$i0 = 10", "return $i0")
            .collect(Collectors.toList()));
    methodList.put("shortVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "byte");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$i0 = 0", "return $i0")
            .collect(Collectors.toList()));
    methodList.put("byteVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "char");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$i0 = 97", "return $i0")
            .collect(Collectors.toList()));
    methodList.put("charVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "int");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$i0 = 512", "return $i0")
            .collect(Collectors.toList()));
    methodList.put("intVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "long");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$i0 = 123456789", "return $i0")
            .collect(Collectors.toList()));
    methodList.put("longVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "float");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$f0 = 3.14F", "return $f0")
            .collect(Collectors.toList()));
    methodList.put("floatVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "double");
    methodValues.put(
        "jimpleLines",
        Stream.of("r0 := @this: MethodReturningVar", "$d0 = 1.96969654", "return $d0")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("doubleVariable", methodValues);

    return methodList;
  }
}
