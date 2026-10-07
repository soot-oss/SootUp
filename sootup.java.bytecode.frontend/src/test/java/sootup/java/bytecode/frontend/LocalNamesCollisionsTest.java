package sootup.java.bytecode.frontend;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Local;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.views.JavaView;

/**
 * One LVT name used in several slots: the variable whose scope starts first keeps the name, later
 * ones are numbered, skipping names the LVT itself uses.
 */
public class LocalNamesCollisionsTest {
  final String directory = "src/test/resources/bugfixes/";

  private Map<String, String> localTypes(String method) {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(inputLocation);
    return view
        .getClass(view.getIdentifierFactory().getClassType("LocalNamesCollisions"))
        .get()
        .getMethod(method, Collections.singletonList(view.getIdentifierFactory().getType("int")))
        .get()
        .getBody()
        .getLocals()
        .stream()
        .collect(Collectors.toMap(Local::getName, l -> l.getType().toString()));
  }

  @Test
  public void testOneNameInTwoSlotsIsNumbered() {
    // `i` is slot 2 in the first loop and slot 3 in the second
    Map<String, String> locals = localTypes("oneNameTwoSlots");
    Assertions.assertEquals("int", locals.get("i"), locals.toString());
    Assertions.assertEquals("int", locals.get("i_1"), locals.toString());
    Assertions.assertEquals("int", locals.get("sum"), locals.toString());
    Assertions.assertEquals("int", locals.get("k"), locals.toString());
    Assertions.assertEquals("int", locals.get("n"), locals.toString());
    Assertions.assertFalse(locals.containsKey("l2"), locals.toString());
    Assertions.assertFalse(locals.containsKey("l3"), locals.toString());
  }

  @Test
  public void testNumberingSkipsNamesOfTheLocalVariableTable() {
    // `i_1` is a variable of its own, so the second `i` becomes `i_2`
    Map<String, String> locals = localTypes("numberedNameTaken");
    Assertions.assertEquals("int", locals.get("i"), locals.toString());
    Assertions.assertEquals("int", locals.get("i_1"), locals.toString());
    Assertions.assertEquals("int", locals.get("i_2"), locals.toString());
    Assertions.assertEquals("int", locals.get("k"), locals.toString());
    Assertions.assertEquals(
        Set.of("n", "i", "i_1", "i_2", "k"), locals.keySet(), locals.toString());
  }
}
