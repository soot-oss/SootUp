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
public class InitializeArraysWhileDeclarationTest extends MinimalTestSuiteBase {

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

  public HashMap<String, HashMap<String, Object>> setValues() {
    HashMap<String, HashMap<String, Object>> methodList = new HashMap<>();
    HashMap<String, Object> methodValues = new HashMap<>();

    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (int[])[3]",
                "$r1[0] = 1",
                "$r1[1] = 2",
                "$r1[2] = 3",
                "return")
            .collect(Collectors.toList()));
    methodList.put("intArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (byte[])[3]",
                "$r1[0] = 4",
                "$r1[1] = 5",
                "$r1[2] = 6",
                "return")
            .collect(Collectors.toList()));
    methodList.put("byteArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (short[])[3]",
                "$r1[0] = 10",
                "$r1[1] = 20",
                "$r1[2] = 30",
                "return")
            .collect(Collectors.toList()));
    methodList.put("shortArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (long[])[3]",
                "$r1[0] = 547087L",
                "$r1[1] = 564645L",
                "$r1[2] = 654786L",
                "return")
            .collect(Collectors.toList()));
    methodList.put("longArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (float[])[4]",
                "$r1[0] = 3.14F",
                "$r1[1] = 5.46F",
                "$r1[2] = 2.987F",
                "$r1[3] = 4.87F",
                "return")
            .collect(Collectors.toList()));
    methodList.put("floatArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (double[])[2]",
                "$r1[0] = 6.765414",
                "$r1[1] = 9.676565646",
                "return")
            .collect(Collectors.toList()));
    methodList.put("doubleArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (boolean[])[2]",
                "$r1[0] = 1",
                "$r1[1] = 0",
                "return")
            .collect(Collectors.toList()));
    methodList.put("booleanArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (char[])[3]",
                "$r1[0] = 65",
                "$r1[1] = 98",
                "$r1[2] = 38",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("charArrays", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: InitializeArraysWhileDeclaration",
                "$r1 = newarray (java.lang.String[])[2]",
                "$r1[0] = \"Hello World\"",
                "$r1[1] = \"Greetings\"",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("stringArrays", methodValues);

    return methodList;
  }
}
