package de.upb.soot.minimaltestsuite.java6.ArraysTest;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.core.Body;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.Utils;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.minimaltestsuite.LoadClassesWithWala;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class InitializeMultidimensionalArraysTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/Arrays/";
  private String className = "InitializeMultidimensionalArrays";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void intArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "intArrays", loadClassesWithWala.declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (int[][])[3]",
                "$r2 = newarray (int[])[3]",
                "$r2[0] = 1",
                "$r2[1] = 2",
                "$r2[2] = 3",
                "$r1[0] = $r2",
                "$r3 = newarray (int[])[2]",
                "$r3[0] = 5",
                "$r3[1] = 6",
                "$r1[1] = $r3",
                "$r4 = newarray (int[])[3]",
                "$r4[0] = 7",
                "$r4[1] = 8",
                "$r4[2] = 9",
                "$r1[2] = $r4",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void byteArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "byteArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (byte[][])[2]",
                "$r2 = newarray (byte[])[2]",
                "$r2[0] = 4",
                "$r2[1] = 5",
                "$r1[0] = $r2",
                "$r3 = newarray (byte[])[1]",
                "$r3[0] = 2",
                "$r1[1] = $r3",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void shortArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "shortArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (short[][])[2]",
                "$r2 = newarray (short[])[3]",
                "$r2[0] = 10",
                "$r2[1] = 20",
                "$r2[2] = 30",
                "$r1[0] = $r2",
                "$r3 = newarray (short[])[1]",
                "$r3[0] = 40",
                "$r1[1] = $r3",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void longArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "longArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (long[][])[3]",
                "$r2 = newarray (long[])[2]",
                "$r2[0] = 547087L",
                "$r2[1] = 654786L",
                "$r1[0] = $r2",
                "$r3 = newarray (long[])[3]",
                "$r3[0] = 547287L",
                "$r3[1] = 864645L",
                "$r3[2] = 6533786L",
                "$r1[1] = $r3",
                "$r4 = newarray (long[])[2]",
                "$r4[0] = 34565L",
                "$r4[1] = 234L",
                "$r1[2] = $r4",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void floatArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "floatArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (float[][])[2]",
                "$r2 = newarray (float[])[2]",
                "$r2[0] = 3.14F",
                "$r2[1] = 5.46F",
                "$r1[0] = $r2",
                "$r3 = newarray (float[])[2]",
                "$r3[0] = 2.987F",
                "$r3[1] = 4.87F",
                "$r1[1] = $r3",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void doubleArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "doubleArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (double[][])[3]",
                "$r2 = newarray (double[])[2]",
                "$r2[0] = 6.765414",
                "$r2[1] = 9.676565646",
                "$r1[0] = $r2",
                "$r3 = newarray (double[])[1]",
                "$r3[0] = 45.345435",
                "$r1[1] = $r3",
                "$r4 = newarray (double[])[2]",
                "$r4[0] = 3.5656",
                "$r4[1] = 68.234234",
                "$r1[2] = $r4",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void booleanArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "booleanArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (boolean[][])[2]",
                "$r2 = newarray (boolean[])[2]",
                "$r2[0] = 1",
                "$r2[1] = 0",
                "$r1[0] = $r2",
                "$r3 = newarray (boolean[])[1]",
                "$r3[0] = 1",
                "$r1[1] = $r3",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void charArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "charArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (char[][])[3]",
                "$r2 = newarray (char[])[3]",
                "$r2[0] = 65",
                "$r2[1] = 98",
                "$r2[2] = 38",
                "$r1[0] = $r2",
                "$r3 = newarray (char[])[2]",
                "$r3[0] = 99",
                "$r3[1] = 36",
                "$r1[1] = $r3",
                "$r4 = newarray (char[])[2]",
                "$r4[0] = 50",
                "$r4[1] = 71",
                "$r1[2] = $r4",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }

  @Test
  public void stringArraysTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "stringArrays",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    Body body = method.getBody();
    assertNotNull(body);

    List<String> actualStmts =
        body.getStmts().stream()
            .map(Stmt::toString)
            .collect(Collectors.toCollection(ArrayList::new));

    List<String> expectedStmts =
        Stream.of(
                "r0 := @this: InitializeMultidimensionalArrays",
                "$r1 = newarray (java.lang.String[][])[2]",
                "$r2 = newarray (java.lang.String[])[1]",
                "$r2[0] = \"Hello World\"",
                "$r1[0] = $r2",
                "$r3 = newarray (java.lang.String[])[2]",
                "$r3[0] = \"Greetings\"",
                "$r3[1] = \"Welcome\"",
                "$r1[1] = $r3",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
