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
public class AccessArraysTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/Arrays/";
  private String className = "AccessArrays";
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (int[])[3]",
                "$r1[0] = 1",
                "$r1[1] = 2",
                "$r1[2] = 3",
                "$i0 = 0",
                "$r2 = $r1",
                "$i1 = 0",
                "$i2 = lengthof $r2",
                "$z0 = $i1 < $i2",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i1]",
                "$i0 = $r3",
                "$i3 = $i1",
                "$i4 = $i1 + 1",
                "$i1 = $i4",
                "goto [?= $i2 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (byte[])[3]",
                "$r1[0] = 4",
                "$r1[1] = 5",
                "$r1[2] = 6",
                "$i0 = 0",
                "$r2 = $r1",
                "$i1 = 0",
                "$i2 = lengthof $r2",
                "$z0 = $i1 < $i2",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i1]",
                "$i0 = $r3",
                "$i3 = $i1",
                "$i4 = $i1 + 1",
                "$i1 = $i4",
                "goto [?= $i2 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (short[])[3]",
                "$r1[0] = 10",
                "$r1[1] = 20",
                "$r1[2] = 30",
                "$i0 = 0",
                "$r2 = $r1",
                "$i1 = 0",
                "$i2 = lengthof $r2",
                "$z0 = $i1 < $i2",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i1]",
                "$i0 = $r3",
                "$i3 = $i1",
                "$i4 = $i1 + 1",
                "$i1 = $i4",
                "goto [?= $i2 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (long[])[3]",
                "$r1[0] = 547087L",
                "$r1[1] = 564645L",
                "$r1[2] = 654786L",
                "$i0 = 0",
                "$r2 = $r1",
                "$i1 = 0",
                "$i2 = lengthof $r2",
                "$z0 = $i1 < $i2",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i1]",
                "$i0 = $r3",
                "$i3 = $i1",
                "$i4 = $i1 + 1",
                "$i1 = $i4",
                "goto [?= $i2 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (float[])[4]",
                "$r1[0] = 3.14F",
                "$r1[1] = 5.46F",
                "$r1[2] = 2.987F",
                "$r1[3] = 4.87F",
                "$d0 = 0.0",
                "$r2 = $r1",
                "$i0 = 0",
                "$i1 = lengthof $r2",
                "$z0 = $i0 < $i1",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i0]",
                "$d0 = $r3",
                "$i2 = $i0",
                "$i3 = $i0 + 1",
                "$i0 = $i3",
                "goto [?= $i1 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (double[])[2]",
                "$r1[0] = 6.765414",
                "$r1[1] = 9.676565646",
                "$d0 = 0.0",
                "$r2 = $r1",
                "$i0 = 0",
                "$i1 = lengthof $r2",
                "$z0 = $i0 < $i1",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i0]",
                "$d0 = $r3",
                "$i2 = $i0",
                "$i3 = $i0 + 1",
                "$i0 = $i3",
                "goto [?= $i1 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (boolean[])[2]",
                "$r1[0] = 1",
                "$r1[1] = 0",
                "$r2 = null",
                "$r3 = $r1",
                "$i0 = 0",
                "$i1 = lengthof $r3",
                "$z0 = $i0 < $i1",
                "if $z0 == 0 goto return",
                "$r4 = $r3[$i0]",
                "$r2 = $r4",
                "$i2 = $i0",
                "$i3 = $i0 + 1",
                "$i0 = $i3",
                "goto [?= $i1 = lengthof $r3]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (char[])[3]",
                "$r1[0] = 65",
                "$r1[1] = 98",
                "$r1[2] = 38",
                "$i0 = 0",
                "$r2 = $r1",
                "$i1 = 0",
                "$i2 = lengthof $r2",
                "$z0 = $i1 < $i2",
                "if $z0 == 0 goto return",
                "$r3 = $r2[$i1]",
                "$i0 = $r3",
                "$i3 = $i1",
                "$i4 = $i1 + 1",
                "$i1 = $i4",
                "goto [?= $i2 = lengthof $r2]",
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
                "r0 := @this: AccessArrays",
                "$r1 = newarray (java.lang.String[])[2]",
                "$r1[0] = \"Hello World\"",
                "$r1[1] = \"Greetings\"",
                "$r2 = null",
                "$r3 = $r1",
                "$i0 = 0",
                "$i1 = lengthof $r3",
                "$z0 = $i0 < $i1",
                "if $z0 == 0 goto return",
                "$r4 = $r3[$i0]",
                "$r2 = $r4",
                "$i2 = $i0",
                "$i3 = $i0 + 1",
                "$i0 = $i3",
                "goto [?= $i1 = lengthof $r3]",
                "return")
            .collect(Collectors.toCollection(ArrayList::new));

    assertEquals(expectedStmts, actualStmts);
  }
}
