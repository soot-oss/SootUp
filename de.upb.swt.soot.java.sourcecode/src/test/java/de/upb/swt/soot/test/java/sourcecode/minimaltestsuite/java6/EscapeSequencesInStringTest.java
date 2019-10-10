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
public class EscapeSequencesInStringTest extends MinimalTestSuiteBase {
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

      loadMethod(expectedBodyStmts(), getMethodSignature());
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

    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes backslash b \\u0008\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeBackslashB", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes backslash t \\t\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeBackslashT", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes backslash n \\n\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeBackslashN", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes backslash f \\f\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeBackslashF", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes backslash r \\r\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeBackslashR", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes double quotes \\\"\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeDoubleQuotes", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes single quote \\\'\"",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("escapeSingleQuote", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: EscapeSequencesInString",
                "$r1 = \"This escapes backslash \\\\\"",
                "return")
            .collect(Collectors.toList()));
    methodList.put("escapeBackslash", methodValues);

    return methodList;
  }
}
