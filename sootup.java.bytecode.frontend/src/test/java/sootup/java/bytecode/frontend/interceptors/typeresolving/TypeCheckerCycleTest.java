package sootup.java.bytecode.frontend.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.NullType;
import sootup.interceptors.TypeAssigner;
import sootup.interceptors.typeresolving.*;
import sootup.java.core.views.JavaView;
import sootup.jimple.frontend.JimpleStringAnalysisInputLocation;


public class TypeCheckerCycleTest {

    @Test
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS, threadMode =  Timeout.ThreadMode.SEPARATE_THREAD)
    public void testWorklistLoopExplicitly() {
        JavaView view = getCycleView();
        MethodSignature signature = view.getIdentifierFactory()
                .getMethodSignature("Cycle", "test", "void", List.of());
        assertTrue(view.getMethod(signature).isPresent(), "Method should be present");
        Body.BodyBuilder builder = Body.builder(view.getMethod(signature).get().getBody(), EnumSet.noneOf(sootup.core.model.MethodModifier.class));

        AugEvalFunction evalFunction = new AugEvalFunction(view);
        BytecodeHierarchy hierarchy = new BytecodeHierarchy(view);
        Typing typing = new Typing(builder.getLocals());

        for (Local local : builder.getLocals()) {
            if (local.getName().equals("$u1")) {
                typing.set(local, NullType.getInstance());
            }
        }

        new CastCounter(builder, evalFunction, hierarchy, typing);

        assertNotNull(builder.build());
    }

    private static @NonNull JavaView getCycleView() {
        String jimple =
                """
                public class Cycle {
                    public void test() {
                        $u1 = newarray (java.lang.Object)[1];
                        $u2 = newarray (java.lang.Object)[1];
                        $u1 = (java.lang.Object) $u2;
                        $u2 = (java.lang.Object) $u1;
                        $u3 = $u1[0];
                        return;
                    }
                }
                """;
        JimpleStringAnalysisInputLocation location = new JimpleStringAnalysisInputLocation(jimple, SourceType.Application, List.of(new TypeAssigner()));
        return new JavaView(List.of(location));
    }
}
