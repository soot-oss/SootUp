/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class NoModifierClassTest extends MinimalTestSuiteBase {

  private String methodName;
  private String methodSignature;
  private String methodModifier;
  private List<String> jimpleLines;

  @Test
  public void defaultTest() {
    HashMap<String, HashMap<String, Object>> methodList = setValues();
    Set<String> methodListKeys = methodList.keySet();

    for (String methodListKey : methodListKeys) {
      methodName = methodListKey;
      HashMap<String, Object> mv = methodList.get(methodListKey);
      methodSignature = (String) mv.get("methodSignature");
      methodModifier = (String) mv.get("methodModifier");
      checkClassModifier("");
      checkMethodModifier(methodModifier, getMethodSignature());
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
    methodValues.put("methodModifier", "PUBLIC");
    methodList.put("publicMethod", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodModifier", "PRIVATE");
    methodList.put("privateMethod", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodModifier", "PROTECTED");
    methodList.put("protectedMethod", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put("methodModifier", "");
    methodList.put("noModifierMethod", methodValues);

    return methodList;
  }
}
