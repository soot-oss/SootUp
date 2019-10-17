package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DeclareInnerClassTest extends MinimalTestSuiteBase{

    public MethodSignature getMethodSignature() {
        return identifierFactory.getMethodSignature(
                "methodDisplayOuter",
                getDeclaredClassSignature(),
                "void",
                Collections.emptyList());
    }

    @Test
    @Override
    public void defaultTest() {
        super.defaultTest();
//        loadMethod(expectedBodyStmts1(), getStaticMethodSignature());
//        SootMethod staticMethod = loadMethod(expectedBodyStmts1(), getStaticMethodSignature());
        SootClass sootClass = loadClass(getDeclaredClassSignature());
        /**
         * TODO check for inner class inside method body
         */
        //assertTrue(sootClass.getFields().stream().anyMatch(sootField -> {return sootField.getModifiers().equals()}));
    }

    @Override
    public List<String> expectedBodyStmts() {
        return Stream.of(
                "r0 := @this: StaticMethodInterfaceImpl",
                "$r1 = <java.lang.System: java.io.PrintStream out>",
                "virtualinvoke $r1.<java.io.PrintStream: void println(java.lang.String)>(\"Inside display - StaticmethodInterfaceImpl\")",
                "return")
                .collect(Collectors.toList());
    }
}
