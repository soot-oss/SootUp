package de.upb.swt.soot.test.java.bytecode.interceptors;

import de.upb.swt.soot.core.graph.ImmutableStmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.NopEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class DeadAssignmentEliminatorTest {

    @Test
    public void TestModification(){
        Body.BodyBuilder testBuilder = createBody(false);
        Body testBody = testBuilder.build();
        new NopEliminator().interceptBody(testBuilder);
        Body processedBody = testBuilder.build();
        ImmutableStmtGraph expectedGraph = testBody.getStmtGraph();
        ImmutableStmtGraph actualGraph = processedBody.getStmtGraph();

        assertEquals(expectedGraph.nodes().size() - 1, actualGraph.nodes().size());
    }

    @Test
    public void TestNoModification(){
        Body.BodyBuilder testBuilder = createBody(true);
        Body testBody = testBuilder.build();
        new NopEliminator().interceptBody(testBuilder);
        Body processedBody = testBuilder.build();
        ImmutableStmtGraph expectedGraph = testBody.getStmtGraph();
        ImmutableStmtGraph actualGraph = processedBody.getStmtGraph();

        assertEquals(expectedGraph.nodes().size(), actualGraph.nodes().size());
    }

    private static Body.BodyBuilder createBody(boolean essentialOption){
        JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
        JavaJimple javaJimple = JavaJimple.getInstance();
        StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

        JavaClassType objectType = factory.getClassType("java.lang.Object");
        JavaClassType stringType = factory.getClassType("java.lang.String");

        Local a = JavaJimple.newLocal("a", objectType);
        Local b = JavaJimple.newLocal("b", stringType);

        Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
        Stmt strToB = JavaJimple.newAssignStmt(b, javaJimple.newStringConstant("string"), noPositionInfo);
        Stmt aToB = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
        Stmt ret = JavaJimple.newReturnStmt(a, noPositionInfo);

        Set<Local> locals = ImmutableUtils.immutableSet(a, b);
        List<Trap> traps = new ArrayList<>();

        Body.BodyBuilder builder = Body.builder();
        builder.setStartingStmt(strToA);
        builder.setMethodSignature(
            JavaIdentifierFactory.getInstance()
                .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

        if(essentialOption){
            builder.addFlow(strToA, strToB);
            builder.addFlow(strToB, ret);
        } else {
            builder.addFlow(strToA, aToB);
            builder.addFlow(aToB, ret);
        }
        builder.setLocals(locals);
        builder.setTraps(traps);
        builder.setPosition(NoPositionInformation.getInstance());

        return builder;
    }
}
