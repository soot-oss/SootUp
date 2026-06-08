package sootup.jimple.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.*;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;
import sootup.jimple.JimpleParser;

public class LazyJimpleMethodSourceTest {

  private LazyJimpleMethodSource createLazySource(String code) {
    CharStream cs = CharStreams.fromString(code);
    JimpleParser parser = JimpleConverterUtil.createJimpleParser(cs, Paths.get(""));
    JimpleParser.MethodContext methodCtx = parser.file().member(0).method();
    
    EagerInputLocation loc = new EagerInputLocation();
    View view = new JavaView(loc);
    JimpleConverterUtil util = new JimpleConverterUtil(Paths.get(""));
    ClassType clazz = view.getIdentifierFactory().getClassType("Test");
    
    // Mocking the signature and modifiers
    MethodSignature sig = view.getIdentifierFactory().getMethodSignature(
        clazz, "m", VoidType.getInstance(), Collections.emptyList());
    EnumSet<MethodModifier> modifiers = EnumSet.noneOf(MethodModifier.class);
    List<ClassType> exceptions = Collections.emptyList();
    Position pos = JimpleConverterUtil.buildPositionFromCtx(methodCtx);

    return new LazyJimpleMethodSource(
        sig,
        methodCtx,
        Paths.get(""),
        Collections.emptyList(),
        view,
        util,
        clazz,
        modifiers,
        exceptions,
        pos
    );
  }

  @Test
  public void testLazyResolution() {
    String code = "class Test { void m() { r0 = 15; return; } }";
    LazyJimpleMethodSource source = createLazySource(code);
    
    // The body should be resolved only when resolveBody is called
    Body body = source.resolveBody(Collections.emptyList());
    assertNotNull(body);
    
    // Verify the body content (e.g., local count)
    assertEquals(1, body.getLocalCount());
  }

  @Test
  public void testEmptyBody() {
    String code = "class Test { void m() { } }";
    LazyJimpleMethodSource source = createLazySource(code);
    Body body = source.resolveBody(Collections.emptyList());
    assertNotNull(body);
    assertEquals(0, body.getLocalCount());
  }
}
