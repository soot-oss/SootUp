package sootup.callgraph.scope;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

@Tag("Java8")
@Tag("Java9")
public class CallGraphScopeTest {

  private final JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  private JavaView createLibraryView() {
    String classPath = "src/test/resources/callgraph/Library/binary/";
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(classPath + "application/", SourceType.Application));
    inputLocations.add(
        new JavaClassPathAnalysisInputLocation(classPath + "library/", SourceType.Library));
    return new JavaView(inputLocations);
  }

  @Test
  public void testExcludedCallsCollectingCallGraphScopeRecordsExcludedMethods() {
    JavaView view = createLibraryView();
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            "app.Application", "main", "void", Collections.singletonList("java.lang.String[]"));

    ClassType libraryClassType = identifierFactory.getClassType("lib.Library");
    MethodSignature libraryConstructor =
        identifierFactory.getMethodSignature(
            libraryClassType, "<init>", "void", Collections.emptyList());
    MethodSignature libraryUse =
        identifierFactory.getMethodSignature(
            libraryClassType, "use", "void", Collections.emptyList());
    MethodSignature libraryA =
        identifierFactory.getMethodSignature(
            libraryClassType, "a", "void", Collections.emptyList());

    ExcludedCallsCollectingCallGraphScope scope = new ExcludedCallsCollectingCallGraphScope(view);
    ClassHierarchyAnalysisAlgorithm cha = new ClassHierarchyAnalysisAlgorithm(view, scope);
    CallGraph cg = cha.initialize(Collections.singletonList(mainMethodSignature));

    // the call edges into the library are still discovered...
    assertTrue(cg.callTargetsFrom(mainMethodSignature).contains(libraryConstructor));
    assertTrue(cg.callTargetsFrom(mainMethodSignature).contains(libraryUse));
    assertTrue(cg.containsMethod(libraryConstructor));
    assertTrue(cg.containsMethod(libraryUse));

    // ...but library methods are not expanded further, since the scope excludes them
    assertTrue(cg.callsFrom(libraryConstructor).isEmpty());
    assertTrue(cg.callsFrom(libraryUse).isEmpty());
    assertFalse(cg.containsMethod(libraryA));

    // and the scope recorded exactly the excluded methods that were actually visited
    assertEquals(2, scope.getVisitedExcludedMethods().size());
    assertTrue(scope.getVisitedExcludedMethods().contains(libraryConstructor));
    assertTrue(scope.getVisitedExcludedMethods().contains(libraryUse));
    assertFalse(scope.getVisitedExcludedMethods().contains(libraryA));
  }
}
