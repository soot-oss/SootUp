package sootup.java.bytecode.frontend;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Local;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

/**
 * Locals in disjoint scopes reuse the same LVT slots with different types:
 *
 * <pre>
 * Start  Length  Slot  Name   Signature
 *    18      20     1     a   LLocalNamesReusedSlots$Foo;
 *    28      10     2     b   LLocalNamesReusedSlots$Foo;
 *    56      20     1     c   LLocalNamesReusedSlots$Bar;
 *    66      10     2     d   LLocalNamesReusedSlots$Bar;
 *     0      85     0  args   [Ljava/lang/String;
 * </pre>
 *
 * AsmMethodSource has to pick the debug name valid at the respective bytecode offset.
 */
public class LocalNamesReusedSlotsTest {
  final String directory = "src/test/resources/bugfixes/";

  @Test
  public void testLocalNamesFromLocalVariableTable() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots"))
            .get()
            .getMethod(
                "main",
                Collections.singletonList(
                    view.getIdentifierFactory().getType("java.lang.String[]")))
            .get();

    Map<String, String> localTypes =
        method.getBody().getLocals().stream()
            .collect(Collectors.toMap(Local::getName, l -> l.getType().toString()));

    Assertions.assertEquals(
        "LocalNamesReusedSlots$Foo", localTypes.get("a"), localTypes.toString());
    Assertions.assertEquals(
        "LocalNamesReusedSlots$Foo", localTypes.get("b"), localTypes.toString());
    Assertions.assertEquals(
        "LocalNamesReusedSlots$Bar", localTypes.get("c"), localTypes.toString());
    Assertions.assertEquals(
        "LocalNamesReusedSlots$Bar", localTypes.get("d"), localTypes.toString());
    Assertions.assertEquals("java.lang.String[]", localTypes.get("args"), localTypes.toString());
  }
}
