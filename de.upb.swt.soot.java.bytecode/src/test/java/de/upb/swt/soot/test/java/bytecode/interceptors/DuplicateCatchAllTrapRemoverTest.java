package de.upb.swt.soot.test.java.bytecode.interceptors;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.JTrap;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.DuplicateCatchAllTrapRemover;
import de.upb.swt.soot.java.bytecode.interceptors.UnusedLocalEliminator;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.*;

import static org.junit.Assert.*;

@Category(Java8Test.class)
public class DuplicateCatchAllTrapRemoverTest {

    /** Tests the correct handling of an empty {@link Body}. */
    @Test
    public void testNoInput(){
        Set<Local> locals = Collections.emptySet();
        List<Trap> traps = Collections.emptyList();
        List<Stmt> stmts = Collections.emptyList();
        Body originalBody = new Body(locals, traps, stmts, null);
        Body processedBody = new UnusedLocalEliminator().interceptBody(originalBody);

        assertNotNull(processedBody);
        assertArrayEquals(originalBody.getTraps().toArray(), processedBody.getTraps().toArray());
    }

    /** Test the correct removal of duplicate catch all traps. */
    @Test
    public void testRemoveDuplicate(){
        Body originalBody = createBody(true);
        Body processedBody = new DuplicateCatchAllTrapRemover().interceptBody(originalBody);

        Collection<Trap> originalTraps = originalBody.getTraps();
        Collection<Trap> processedTraps = processedBody.getTraps();

        assertEquals(3, originalTraps.size());
        assertEquals(2, processedTraps.size());
        for(Trap trap : processedTraps){
            assertTrue(originalTraps.contains(trap));
        }
    }

    /** Tests the correct handling of a {@link Body} without duplicate catch all traps. */
    @Test
    public void testRemoveNothing(){
        Body originalBody = createBody(false);
        Body processedBody = new DuplicateCatchAllTrapRemover().interceptBody(originalBody);

        assertArrayEquals(originalBody.getTraps().toArray(), processedBody.getTraps().toArray());
    }

    /**
     * Creates the {@link Body} for each test case. Depending on the parameter, it adds 0 or 1 Traps that should be removed
     * @param containsDuplicate  determines whether the Body contains a Trap that should be removed
     * @return the created Body
     */
    private Body createBody(boolean containsDuplicate){
        JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
        JavaJimple javaJimple = JavaJimple.getInstance();
        StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

        JavaClassType objectType = factory.getClassType("java.lang.Object");
        JavaClassType stringType = factory.getClassType("java.lang.String");
        Local a = JavaJimple.newLocal("a", objectType);
        Local b = JavaJimple.newLocal("b", stringType);
        Set<Local> locals = ImmutableUtils.immutableSet(a, b);

        Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
        Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
        Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
        Stmt jump = JavaJimple.newGotoStmt(bToA, noPositionInfo);

        List<Trap> traps = new ArrayList<>();
        ExceptionType exceptionType = new ExceptionType();
        Trap trap1 = new JTrap(exceptionType, strToA, bToA, jump);
        traps.add(trap1);
        Trap trap2 = new JTrap(exceptionType, strToA, bToA, ret);
        traps.add(trap2);
        if(containsDuplicate){
            Trap trap3 = new JTrap(exceptionType, strToA, bToA, ret);
            traps.add(trap3);
        }
        List<Stmt> stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);

        return new Body(locals, traps, stmts, null);
    }

    private static class ExceptionType extends ClassType{
        @Override
        public boolean isBuiltInClass() {
            return false;
        }

        @Override
        public String getFullyQualifiedName() {
            return null;
        }

        @Override
        public String getClassName() {
            return "java.lang.Throwable";
        }

        @Override
        public PackageName getPackageName() {
            return null;
        }
    }
}
