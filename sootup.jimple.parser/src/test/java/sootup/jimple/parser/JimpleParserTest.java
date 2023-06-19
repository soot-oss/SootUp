package sootup.jimple.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.jimple.parser.categories.Java8Test;

import java.nio.file.Paths;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@Category(Java8Test.class)
public class JimpleParserTest {

    private SootClass<?> parseJimpleClass(CharStream cs) throws ResolveException {
        JimpleConverter jimpleVisitor = new JimpleConverter();
        final OverridingClassSource scs =
                jimpleVisitor.run(cs, new EagerInputLocation<>(), Paths.get(""));
        return new SootClass<>(scs, SourceType.Application);
    }

    @Test
    public void testQuotedTypeParsing() {
        String quotedTypeUsage = "public class TypeUsage { \n" +
                "static void <clinit>() { \n" +
                "java.util.HashSet v0; \n" +
                "specialinvoke v0.<java.util.HashSet: void <init>()>(); \n" +
                "interfaceinvoke v0.<java.util.Set: boolean add(java.lang.Object)>(\"org.example.InvokerTransformer\"); \n" +
                "return; \n" +
                "}}";

        SootClass<?> clazz = parseJimpleClass(CharStreams.fromString(quotedTypeUsage));
        Set<? extends SootMethod> methods = clazz.getMethods();
        SootMethod method = methods.iterator().next();
        Body body = method.getBody();
        assertEquals(1, body.getLocalCount());
    }
}
