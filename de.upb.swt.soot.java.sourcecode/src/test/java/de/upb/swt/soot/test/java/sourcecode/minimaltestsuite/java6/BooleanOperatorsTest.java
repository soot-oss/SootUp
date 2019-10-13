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
public class BooleanOperatorsTest extends MinimalTestSuiteBase {
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

      super.defaultTest();
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
                "r0 := @this: BooleanOperators",
                "$i0 = 0",
                "$z0 = $i0 <= 10",
                "if $z0 == 0 goto return",
                "$i1 = $i0",
                "$i2 = $i0 + 1",
                "$i0 = $i2",
                "$z1 = $i0 == 5",
                "if $z1 == 0 goto (branch)",
                "goto [?= return]",
                "goto [?= $z0 = $i0 <= 10]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("relationalOpEqual", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$i0 = 0",
                "$r1 = \"\"",
                "$z0 = $i0 < 10",
                "if $z0 == 0 goto return",
                "$i1 = $i0",
                "$i2 = $i0 + 1",
                "$i0 = $i2",
                "$z1 = $i0 != 5",
                "if $z1 == 0 goto (branch)",
                "$r1 = \"i != 5\"",
                "goto [?= (branch)]",
                "goto [?= $z0 = $i0 < 10]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("relationalOpNotEqual", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$z0 = 1",
                "if $z0 == 0 goto return",
                "$z1 = neg $z0",
                "$z0 = $z1",
                "goto [?= return]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("complementOp", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$z0 = 1",
                "$z1 = 1",
                "$z2 = 0",
                "$z3 = 0",
                "$r1 = \"\"",
                "$i0 = (int) $z0",
                "$i1 = (int) $z1",
                "$i2 = $i0 & $i1",
                "if $i2 == 0 goto $i3 = (int) $z2",
                "$r1 = \"A\"",
                "goto [?= $i3 = (int) $z2]",
                "$i3 = (int) $z2",
                "$i4 = (int) $z3",
                "$i5 = $i3 & $i4",
                "if $i5 == 0 goto $i6 = (int) $z0",
                "$r1 = \"B\"",
                "goto [?= $i6 = (int) $z0]",
                "$i6 = (int) $z0",
                "$i7 = (int) $z2",
                "$i8 = $i6 & $i7",
                "if $i8 == 0 goto $i9 = (int) $z3",
                "$r1 = \"C\"",
                "goto [?= $i9 = (int) $z3]",
                "$i9 = (int) $z3",
                "$i10 = (int) $z1",
                "$i11 = $i9 & $i10",
                "if $i11 == 0 goto return",
                "$r1 = \"D\"",
                "goto [?= return]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("logicalOpAnd", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$z0 = 1",
                "$z1 = 1",
                "$z2 = 0",
                "$z3 = 0",
                "$r1 = \"\"",
                "$i0 = (int) $z0",
                "$i1 = (int) $z1",
                "$i2 = $i0 | $i1",
                "if $i2 == 0 goto $i3 = (int) $z2",
                "$r1 = \"A\"",
                "goto [?= $i3 = (int) $z2]",
                "$i3 = (int) $z2",
                "$i4 = (int) $z3",
                "$i5 = $i3 | $i4",
                "if $i5 == 0 goto $i6 = (int) $z0",
                "$r1 = \"B\"",
                "goto [?= $i6 = (int) $z0]",
                "$i6 = (int) $z0",
                "$i7 = (int) $z2",
                "$i8 = $i6 | $i7",
                "if $i8 == 0 goto $i9 = (int) $z3",
                "$r1 = \"C\"",
                "goto [?= $i9 = (int) $z3]",
                "$i9 = (int) $z3",
                "$i10 = (int) $z1",
                "$i11 = $i9 | $i10",
                "if $i11 == 0 goto return",
                "$r1 = \"D\"",
                "goto [?= return]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("logicalOpOr", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$z0 = 1",
                "$z1 = 1",
                "$z2 = 0",
                "$z3 = 0",
                "$r1 = \"\"",
                "$i0 = (int) $z0",
                "$i1 = (int) $z1",
                "$i2 = $i0 ^ $i1",
                "if $i2 == 0 goto $i3 = (int) $z2",
                "$r1 = \"A\"",
                "goto [?= $i3 = (int) $z2]",
                "$i3 = (int) $z2",
                "$i4 = (int) $z3",
                "$i5 = $i3 ^ $i4",
                "if $i5 == 0 goto $i6 = (int) $z0",
                "$r1 = \"B\"",
                "goto [?= $i6 = (int) $z0]",
                "$i6 = (int) $z0",
                "$i7 = (int) $z2",
                "$i8 = $i6 ^ $i7",
                "if $i8 == 0 goto $i9 = (int) $z3",
                "$r1 = \"C\"",
                "goto [?= $i9 = (int) $z3]",
                "$i9 = (int) $z3",
                "$i10 = (int) $z1",
                "$i11 = $i9 ^ $i10",
                "if $i11 == 0 goto return",
                "$r1 = \"D\"",
                "goto [?= return]",
                "return")
            .collect(Collectors.toList()));
    methodList.put("logicalOpXor", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$z0 = 1",
                "$z1 = 1",
                "$z2 = 0",
                "$z3 = 0",
                "$r1 = \"\"",
                "if $z0 == 0 goto $z4 = 0",
                "$z4 = $z1",
                "goto [?= (branch)]",
                "$z4 = 0",
                "if $z4 == 0 goto (branch)",
                "$r1 = \"A\"",
                "goto [?= (branch)]",
                "if $z2 == 0 goto $z5 = 0",
                "$z5 = $z3",
                "goto [?= (branch)]",
                "$z5 = 0",
                "if $z5 == 0 goto (branch)",
                "$r1 = \"B\"",
                "goto [?= (branch)]",
                "if $z0 == 0 goto $z6 = 0",
                "$z6 = $z2",
                "goto [?= (branch)]",
                "$z6 = 0",
                "if $z6 == 0 goto (branch)",
                "$r1 = \"C\"",
                "goto [?= (branch)]",
                "if $z3 == 0 goto $z7 = 0",
                "$z7 = $z1",
                "goto [?= (branch)]",
                "$z7 = 0",
                "if $z7 == 0 goto return",
                "$r1 = \"D\"",
                "goto [?= return]",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("ConditionalOpAnd", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$z0 = 1",
                "$z1 = 1",
                "$z2 = 0",
                "$z3 = 0",
                "$r1 = \"\"",
                "if $z0 == 0 goto $z4 = $z1",
                "$z4 = 1",
                "goto [?= (branch)]",
                "$z4 = $z1",
                "if $z4 == 0 goto (branch)",
                "$r1 = \"A\"",
                "goto [?= (branch)]",
                "if $z2 == 0 goto $z5 = $z3",
                "$z5 = 1",
                "goto [?= (branch)]",
                "$z5 = $z3",
                "if $z5 == 0 goto (branch)",
                "$r1 = \"B\"",
                "goto [?= (branch)]",
                "if $z0 == 0 goto $z6 = $z2",
                "$z6 = 1",
                "goto [?= (branch)]",
                "$z6 = $z2",
                "if $z6 == 0 goto (branch)",
                "$r1 = \"C\"",
                "goto [?= (branch)]",
                "if $z3 == 0 goto $z7 = $z1",
                "$z7 = 1",
                "goto [?= (branch)]",
                "$z7 = $z1",
                "if $z7 == 0 goto return",
                "$r1 = \"D\"",
                "goto [?= return]",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("conditionalOpOr", methodValues);

    methodValues = new HashMap<>();
    methodValues.put("methodSignature", "void");
    methodValues.put(
        "jimpleLines",
        Stream.of(
                "r0 := @this: BooleanOperators",
                "$i0 = 5",
                "$r1 = \"\"",
                "$z0 = $i0 < 10",
                "if $z0 == 0 goto $r2 = \"i greater than 10\"",
                "$r2 = \"i less than 10\"",
                "goto [?= $r1 = $r2]",
                "$r2 = \"i greater than 10\"",
                "$r1 = $r2",
                "return")
            .collect(Collectors.toCollection(ArrayList::new)));
    methodList.put("conditionalOp", methodValues);

    return methodList;
  }
}
