package sootup.java.bytecode.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class LocalSlotIndexTest {

  private final String directory = "src/test/resources/bugfixes/";

  @Test
  public void testSlotIndicesInStaticMethod() {
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

    Body body = method.getBody();
    Map<String, Local> localsByName =
        body.getLocals().stream().collect(Collectors.toMap(Local::getName, l -> l, (a, b) -> a));

    // Parameter args has slot index 0 in static method
    Local argsLocal = localsByName.get("args");
    assertNotNull(argsLocal);
    assertEquals(0, argsLocal.getSlotIndex());

    // Disjoint scope locals reusing slots
    Local aLocal = localsByName.get("a");
    assertNotNull(aLocal);
    assertEquals(1, aLocal.getSlotIndex());

    Local bLocal = localsByName.get("b");
    assertNotNull(bLocal);
    assertEquals(2, bLocal.getSlotIndex());

    Local cLocal = localsByName.get("c");
    assertNotNull(cLocal);
    assertEquals(1, cLocal.getSlotIndex());

    Local dLocal = localsByName.get("d");
    assertNotNull(dLocal);
    assertEquals(2, dLocal.getSlotIndex());

    // Stack temporaries must have slot index -1
    for (Local l : body.getLocals()) {
      if (l.getName().startsWith("$stack")) {
        assertEquals(-1, l.getSlotIndex());
      }
    }
  }

  @Test
  public void testSlotIndicesInInstanceMethod() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("LocalNamesReusedSlots$Foo"))
            .get()
            .getMethod(
                "use",
                Collections.singletonList(
                    view.getIdentifierFactory().getClassType("LocalNamesReusedSlots$Foo")))
            .get();

    Body body = method.getBody();
    Map<String, Local> localsByName =
        body.getLocals().stream().collect(Collectors.toMap(Local::getName, l -> l, (a, b) -> a));

    // In instance methods, 'this' occupies slot 0
    Local thisLocal = localsByName.get("this");
    assertNotNull(thisLocal);
    assertEquals(0, thisLocal.getSlotIndex());

    // Formal parameter 'other' occupies slot 1
    Local otherLocal = localsByName.get("other");
    assertNotNull(otherLocal);
    assertEquals(1, otherLocal.getSlotIndex());

    // Stack temporaries must have slot index -1
    for (Local l : body.getLocals()) {
      if (l.getName().startsWith("$stack")) {
        assertEquals(-1, l.getSlotIndex());
      }
    }
  }

  @Test
  public void testSlotIndicesInSameVarNamesInDifferentScopes() {
    AnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            directory, SourceType.Application, Collections.emptyList());

    JavaView view = new JavaView(inputLocation);
    JavaSootMethod method =
        view.getClass(view.getIdentifierFactory().getClassType("SameVarNamesInDifferentScopes"))
            .get()
            .getMethod("foo", Collections.emptyList())
            .get();

    Body body = method.getBody();
    Set<Local> locals = body.getLocals();

    long slotIndexedLocalsCount = locals.stream().filter(l -> l.getSlotIndex() >= 0).count();
    assertTrue(slotIndexedLocalsCount > 0, "Expected at least one Local with slot index >= 0");

    Map<String, Local> localsByName =
        locals.stream().collect(Collectors.toMap(Local::getName, l -> l, (a, b) -> a));

    Local thisLocal = localsByName.get("this");
    assertNotNull(thisLocal);
    assertEquals(0, thisLocal.getSlotIndex());

    Local candidate = localsByName.get("candidate");
    assertNotNull(candidate);
    assertEquals(1, candidate.getSlotIndex());

    Local candidate1 = localsByName.get("candidate_1");
    assertNotNull(candidate1);
    assertEquals(2, candidate1.getSlotIndex());

    Local theresAnother = localsByName.get("theresAnother");
    assertNotNull(theresAnother);
    assertEquals(1, theresAnother.getSlotIndex());
  }
}
