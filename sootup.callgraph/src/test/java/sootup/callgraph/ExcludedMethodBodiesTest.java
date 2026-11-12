package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sootup.callgraph.scope.CallFilter;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.callgraph.scope.ExcludedCallsCollectingCallResolver;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * A method whose calls the {@link CallResolver} excludes as a whole is still a node of the call
 * graph, but its body is not loaded: the algorithm would only read it to exclude each of its calls.
 */
@Tag("Java8")
public class ExcludedMethodBodiesTest {

  private final JavaIdentifierFactory identifierFactory = new JavaIdentifierFactory();
  private final ClassType libraryType = identifierFactory.getClassType("lib.Library");
  private final MethodSignature main =
      identifierFactory.getMethodSignature(
          "app.Application", "main", "void", Collections.singletonList("java.lang.String[]"));
  private final MethodSignature libraryConstructor =
      identifierFactory.getMethodSignature(libraryType, "<init>", "void", Collections.emptyList());
  private final MethodSignature libraryUse =
      identifierFactory.getMethodSignature(libraryType, "use", "void", Collections.emptyList());

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

  private boolean isLibraryMethod(SootMethod method) {
    return method != null && method.getDeclaringClassType().equals(libraryType);
  }

  @Test
  public void libraryBodiesAreNotLoadedByDefault() {
    JavaView view = createLibraryView();
    DefaultCallResolver resolver = Mockito.spy(new DefaultCallResolver(view));
    ClassHierarchyAnalysisAlgorithm cha =
        Mockito.spy(new ClassHierarchyAnalysisAlgorithm(view, resolver));

    CallGraph cg = cha.initialize(Collections.singletonList(main));

    // the calls into the library are in the call graph, the library's own calls are not
    assertTrue(cg.callTargetsFrom(main).contains(libraryConstructor));
    assertTrue(cg.callTargetsFrom(main).contains(libraryUse));
    assertTrue(cg.callsFrom(libraryConstructor).isEmpty());
    assertTrue(cg.callsFrom(libraryUse).isEmpty());
    // the library methods were not loaded
    Mockito.verify(cha, Mockito.never()).getBody(argThat(this::isLibraryMethod));
    // each reached library method is decided once
    for (MethodSignature libraryMethod : List.of(libraryConstructor, libraryUse)) {
      Mockito.verify(resolver, Mockito.times(1))
          .tryAdvanceMethod(argThat(m -> m != null && m.getSignature().equals(libraryMethod)));
    }
  }

  @Test
  public void aStatementFilterExaminesEveryStatement() {
    JavaView view = createLibraryView();
    // excludes the same calls as the default resolver, but statement by statement
    List<InvokableStmt> examined = new ArrayList<>();
    CallResolver perStatement =
        caller ->
            CallFilter.of(
                statement -> {
                  if (isLibraryMethod(caller)) {
                    examined.add(statement);
                    return false;
                  }
                  return true;
                });
    ClassHierarchyAnalysisAlgorithm cha =
        Mockito.spy(new ClassHierarchyAnalysisAlgorithm(view, perStatement));

    CallGraph perStatementGraph = cha.initialize(Collections.singletonList(main));
    CallGraph defaultGraph =
        new ClassHierarchyAnalysisAlgorithm(view, new DefaultCallResolver(view))
            .initialize(Collections.singletonList(main));

    Mockito.verify(cha, Mockito.atLeastOnce()).getBody(argThat(this::isLibraryMethod));
    assertFalse(examined.isEmpty());
    assertEquals(perStatementGraph.getMethodSignatures(), defaultGraph.getMethodSignatures());
    assertEquals(perStatementGraph.callCount(), defaultGraph.callCount());
  }

  @Test
  public void excludedMethodsAreCollectedWithoutLoadingTheirBodies() {
    JavaView view = createLibraryView();
    ExcludedCallsCollectingCallResolver resolver = new ExcludedCallsCollectingCallResolver(view);
    ClassHierarchyAnalysisAlgorithm cha =
        Mockito.spy(new ClassHierarchyAnalysisAlgorithm(view, resolver));

    cha.initialize(Collections.singletonList(main));

    assertEquals(Set.of(libraryConstructor, libraryUse), resolver.getVisitedExcludedMethods());
    Mockito.verify(cha, Mockito.never()).getBody(argThat(this::isLibraryMethod));
  }
}
