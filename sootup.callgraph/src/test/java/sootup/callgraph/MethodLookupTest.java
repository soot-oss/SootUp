package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

public class MethodLookupTest {

  private static JavaView view;
  private static JavaIdentifierFactory factory;

  @BeforeAll
  static void setUp() {
    view = new JavaView(new DefaultRuntimeAnalysisInputLocation());
    factory = view.getIdentifierFactory();
  }

  private static MethodSignature sig(String s) {
    return factory.parseMethodSignature(s);
  }

  private static Optional<MethodSignature> find(MethodLookup lookup, String s) {
    return lookup.find(sig(s)).map(SootMethod::getSignature);
  }

  @Test
  public void superclassChain() {
    MethodLookup lookup = new MethodLookup(view);
    assertEquals(
        Optional.of(sig("java.lang.Object#getClass(): java.lang.Class")),
        find(lookup, "java.lang.String#getClass(): java.lang.Class"));
  }

  @Test
  public void defaultMethod() {
    MethodLookup lookup = new MethodLookup(view);
    assertEquals(
        Optional.of(sig("java.util.Collection#stream(): java.util.stream.Stream")),
        find(lookup, "java.util.ArrayList#stream(): java.util.stream.Stream"));
  }

  @Test
  public void abstractFoundButNotConcrete() {
    MethodLookup lookup = new MethodLookup(view);
    MethodSignature get = sig("java.util.AbstractList#get(int): java.lang.Object");
    assertTrue(lookup.find(get).orElseThrow().isAbstract());
    assertFalse(lookup.findConcrete(get).isPresent());
  }

  @Test
  public void signaturePolymorphic() {
    MethodLookup lookup = new MethodLookup(view);
    assertEquals(
        Optional.of(
            sig("java.lang.invoke.MethodHandle#invokeExact(java.lang.Object[]): java.lang.Object")),
        find(lookup, "<java.lang.invoke.MethodHandle: int invokeExact(java.lang.String,int)>"));
  }

  @Test
  public void missAndHitCached() {
    MethodLookup lookup = new MethodLookup(view);
    MethodSignature missing = sig("java.lang.String#noSuchMethod(): void");
    assertFalse(lookup.find(missing).isPresent());
    assertFalse(lookup.find(missing).isPresent());
    MethodSignature hit = sig("java.lang.String#getClass(): java.lang.Class");
    assertSame(lookup.find(hit), lookup.find(hit));
    // unknown class
    assertFalse(lookup.find(sig("does.not.Exist#m(): void")).isPresent());
  }

  @Test
  public void staticHelpersAgree() {
    MethodLookup lookup = new MethodLookup(view);
    for (String s :
        new String[] {
          "java.lang.String#getClass(): java.lang.Class",
          "java.util.ArrayList#stream(): java.util.stream.Stream",
          "java.util.AbstractList#get(int): java.lang.Object",
        }) {
      assertEquals(
          AbstractCallGraphAlgorithm.findConcreteMethod(view, sig(s)).map(SootMethod::getSignature),
          find(lookup, s),
          s);
    }
  }
}
