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
public class MethodAcceptingVarTest extends MinimalTestSuiteBase {
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

  private HashMap<String, HashMap<String, Object>> setValues() {
    HashMap<String, HashMap<String, Object>> methodList = new HashMap<>();
    HashMap<String, Object> methodValues = new HashMap<>();

    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("short"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$s0 := @parameter0: short",
                "$s1 = $s0",
                "$s2 = $s0 + 1",
                "$s0 = $s2",
                "return")
            .collect(Collectors.toList()));
    methodList.put("shortVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("byte"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$b0 := @parameter0: byte",
                "$b1 = $b0",
                "$b2 = $b0 + 1",
                "$b0 = $b2",
                "return")
            .collect(Collectors.toList()));
    methodList.put("byteVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("char"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar", "$c0 := @parameter0: char", "$c0 = 97", "return")
            .collect(Collectors.toList()));
    methodList.put("charVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("int"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$i0 := @parameter0: int",
                "$i1 = $i0",
                "$i2 = $i0 + 1",
                "$i0 = $i2",
                "return")
            .collect(Collectors.toList()));
    methodList.put("intVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("long"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$l0 := @parameter0: long",
                "$l0 = 123456777",
                "return")
            .collect(Collectors.toList()));
    methodList.put("longVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("float"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$f0 := @parameter0: float",
                "$f0 = 7.77F",
                "return")
            .collect(Collectors.toList()));
    methodList.put("floatVariable", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodParameters", Collections.singletonList("double"));
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: MethodAcceptingVar",
                "$d0 := @parameter0: double",
                "$d0 = 1.787777777",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("doubleVariable", methodValues);

    return methodList;
  }
}
