package sootup.java.bytecode.interceptors;

import categories.Java8Test;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.types.UnknownType;
import sootup.core.types.VoidType;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(Java8Test.class)
public class LocalSplitterTest {
    JavaView view;

    @Before
    public void Setup() {
        String classPath = "src/test/java/resources/interceptors";
        JavaClassPathAnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        view = new JavaView(inputLocation);
    }

    @Test
    public void testStmtToUsesSimpleAssignment(){
        Body originalBody = getBody("case0");
        LocalSplitter localSplitter = new LocalSplitter();
        Map<Stmt, List<Pair<Stmt, Value>>> actual = localSplitter.getStmtToUses(Body.builder(originalBody, Collections.emptySet()));
        Map<Stmt, List<Pair<Stmt, Value>>> expected = new HashMap<>();

        Stmt s1 = getStmt(originalBody, "$l2 = 1");
        List<Pair<Stmt, Value>> s1Uses = new ArrayList<>();
        Stmt l1_gets_l2_plus_1 = getStmt(originalBody, "$l1 = $l2 + 1");
        s1Uses.add(new MutablePair<>(l1_gets_l2_plus_1, l1_gets_l2_plus_1.getUses().get(1)));
        expected.put(s1, s1Uses);

        Stmt s2 = l1_gets_l2_plus_1;
        Stmt l2_gets_l1_plus_1 = getStmt(originalBody, "$l2 = $l1 + 1");
        List<Pair<Stmt, Value>> s2Uses = new ArrayList<>();
        s2Uses.add(new MutablePair<>(l2_gets_l1_plus_1, l2_gets_l1_plus_1.getUses().get(1)));
        expected.put(s2, s2Uses);

        assertTrue(expected.keySet().containsAll(actual.keySet()));
        assertTrue(expected.values().containsAll(actual.values()));
        assertTrue(actual.keySet().containsAll(expected.keySet()));
        assertTrue(actual.values().containsAll(expected.values()));
    }

    @Test
    public void testStmtToUsesSelfAssignment(){
        Body originalBody = getBody("case1");
        LocalSplitter localSplitter = new LocalSplitter();
        Map<Stmt, List<Pair<Stmt, Value>>> actual = localSplitter.getStmtToUses(Body.builder(originalBody, Collections.emptySet()));
        Map<Stmt, List<Pair<Stmt, Value>>> expected = new HashMap<>();

        Stmt s1 = getStmt(originalBody, "$l1 = 0");
        List<Pair<Stmt, Value>> s1Uses = new ArrayList<>();
        Stmt l1_gets_l1_plus_1 = getStmt(originalBody, "$l1 = $l1 + 1");
        s1Uses.add(new MutablePair<>(l1_gets_l1_plus_1, l1_gets_l1_plus_1.getUses().get(1)));
        expected.put(s1, s1Uses);

        Stmt s2 = getStmt(originalBody, "$l2 = 1");
        Stmt l2_gets_l2_plus_1 = getStmt(originalBody, "$l2 = $l2 + 1");
        List<Pair<Stmt, Value>> s2Uses = new ArrayList<>();
        s2Uses.add(new MutablePair<>(l2_gets_l2_plus_1, l2_gets_l2_plus_1.getUses().get(1)));
        expected.put(s2, s2Uses);

        assertTrue(expected.keySet().containsAll(actual.keySet()));
        assertTrue(expected.values().containsAll(actual.values()));
        assertTrue(actual.keySet().containsAll(expected.keySet()));
        assertTrue(actual.values().containsAll(expected.values()));
    }




    private Stmt getStmt(Body body, String s){
        return body.getStmts().stream().filter(e -> e.toString().equals(s)).findFirst().get();
    }


    @Test
    public void testSimpleAssignment() {
        Body originalBody = getBody("case0");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l2#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l2#4", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);

        Body newBody = builder.build();
        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts = "$l0 := @this: LocalSplitterTarget;\n" +
                "$l1#1 = 0;\n" +
                "$l2#2 = 1;\n" +
                "$l1#3 = $l2#2 + 1;\n" +
                "$l2#4 = $l1#3 + 1;\n" +
                "\n" +
                "return;";

        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }

    private Body getBody(String methodName) {
        ClassType type = new JavaClassType("LocalSplitterTarget", PackageName.DEFAULT_PACKAGE);
        MethodSignature sig = new MethodSignature(type, new MethodSubSignature(methodName, Collections.EMPTY_LIST, VoidType.getInstance()));
        SootMethod sootMethod = view.getMethod(sig).get();
        Body originalBody = sootMethod.getBody();
        return originalBody;
    }

    @Test
    public void testSelfAssignment() {
        Body originalBody = getBody("case1");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l2#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l2#4", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);

        Body newBody = builder.build();
        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts = "$l0 := @this: LocalSplitterTarget;\n" +
                "$l1#1 = 0;\n" +
                "$l2#2 = 1;\n" +
                "$l1#3 = $l1#1 + 1;\n" +
                "$l2#4 = $l2#2 + 1;\n" +
                "\n" +
                "return;";

        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }

    @Test
    public void testBranch() {
        Body originalBody = getBody("case2");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);

        Body newBody = builder.build();
        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts = "$l0 := @this: LocalSplitterTarget;\n" +
                "$l1#1 = 0;\n" +
                "\n" +
                "if $l1#1 >= 0 goto label1;\n" +
                "$l1#2 = $l1#1 + 1;\n" +
                "\n" +
                "goto label2;\n" +
                "\n" +
                "label1:\n" +
                "$l1#3 = $l1#1 - 1;\n" +
                "$l1#2 = $l1#3 + 2;\n" +
                "\n" +
                "label2:\n" +
                "return $l1#2;";
        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }


    @Test
    public void testBranchMoreLocals() {
        Body originalBody = getBody("case3");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#4", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#5", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#6", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);

        Body newBody = builder.build();
        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts = "$l0 := @this: LocalSplitterTarget;\n" +
                "$l1#1 = 0;\n" +
                "\n" +
                "if $l1#1 >= 0 goto label1;\n" +
                "$l1#2 = $l1#1 + 1;\n" +
                "$l1#3 = $l1#2 + 2;\n" +
                "$l1#4 = $l1#3 + 3;\n" +
                "\n" +
                "goto label2;\n" +
                "\n" +
                "label1:\n" +
                "$l1#5 = $l1#1 - 1;\n" +
                "$l1#6 = $l1#5 - 2;\n" +
                "$l1#4 = $l1#6 - 3;\n" +
                "\n" +
                "label2:\n" +
                "return $l1#4;";
        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }


    @Test
    public void testBranchMoreBranches() {
        Body originalBody = getBody("case4");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#4", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#5", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#6", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#7", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);

        Body newBody = builder.build();
        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts =
                "$l0 := @this: LocalSplitterTarget;\n" +
                        "$l1#1 = 0;\n" +
                        "\n" +
                        "if $l1#1 >= 0 goto label1;\n" +
                        "$l1#2 = $l1#1 + 1;\n" +
                        "$l1#3 = $l1#2 + 2;\n" +
                        "\n" +
                        "goto label2;\n" +
                        "\n" +
                        "label1:\n" +
                        "$l1#4 = $l1#1 - 1;\n" +
                        "$l1#3 = $l1#4 - 2;\n" +
                        "\n" +
                        "label2:\n" +
                        "if $l1#3 <= 1 goto label3;\n" +
                        "$l1#5 = $l1#3 + 3;\n" +
                        "$l1#6 = $l1#5 + 5;\n" +
                        "\n" +
                        "goto label4;\n" +
                        "\n" +
                        "label3:\n" +
                        "$l1#7 = $l1#3 - 3;\n" +
                        "$l1#6 = $l1#7 - 5;\n" +
                        "\n" +
                        "label4:\n" +
                        "return $l1#6;";
        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }


    @Test
    public void testBranchElseIf() {
        Body originalBody = getBody("case5");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#4", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#5", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);

        Body newBody = builder.build();
        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts =
                "$l0 := @this: LocalSplitterTarget;\n" +
                        "$l1#1 = 0;\n" +
                        "\n" +
                        "if $l1#1 >= 0 goto label1;\n" +
                        "$l1#2 = $l1#1 + 1;\n" +
                        "$l1#3 = $l1#2 + 2;\n" +
                        "\n" +
                        "goto label3;\n" +
                        "\n" +
                        "label1:\n" +
                        "if $l1#1 >= 5 goto label2;\n" +
                        "$l1#4 = $l1#1 - 1;\n" +
                        "$l1#3 = $l1#4 - 2;\n" +
                        "\n" +
                        "goto label3;\n" +
                        "\n" +
                        "label2:\n" +
                        "$l1#5 = $l1#1 * 1;\n" +
                        "$l1#3 = $l1#5 * 2;\n" +
                        "\n" +
                        "label3:\n" +
                        "return $l1#3;";
        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }


    @Test
    public void testForLoop() {
        Body originalBody = getBody("case6");

        List<Local> expectedLocals = new ArrayList<>();
        expectedLocals.addAll(originalBody.getLocals());
        expectedLocals.add(new Local("$l1#1", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#2", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#3", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#4", UnknownType.getInstance(), NoPositionInformation.getInstance()));
        expectedLocals.add(new Local("$l1#5", UnknownType.getInstance(), NoPositionInformation.getInstance()));

        Body.BodyBuilder builder = Body.builder(originalBody, Collections.emptySet());
        LocalSplitter localSplitter = new LocalSplitter();
        localSplitter.interceptBody(builder, view);


        Body newBody = builder.build();

        System.out.println(newBody);

//        assertTrue(expectedLocals.containsAll(newBody.getLocals()));
//        assertTrue(newBody.getLocals().containsAll(expectedLocals));

        String expectedStmts =
                "$l0 := @this: LocalSplitterTarget;\n" +
                        "$l1#1 = 0;\n" +
                        "\n" +
                        "if $l1#1 >= 0 goto label1;\n" +
                        "$l1#2 = $l1#1 + 1;\n" +
                        "$l1#3 = $l1#2 + 2;\n" +
                        "\n" +
                        "goto label3;\n" +
                        "\n" +
                        "label1:\n" +
                        "if $l1#1 >= 5 goto label2;\n" +
                        "$l1#4 = $l1#1 - 1;\n" +
                        "$l1#3 = $l1#4 - 2;\n" +
                        "\n" +
                        "goto label3;\n" +
                        "\n" +
                        "label2:\n" +
                        "$l1#5 = $l1#1 * 1;\n" +
                        "$l1#3 = $l1#5 * 2;\n" +
                        "\n" +
                        "label3:\n" +
                        "return $l1#3;";
//        assertEquals(expectedStmts, newBody.getStmtGraph().toString().trim());
    }

}
