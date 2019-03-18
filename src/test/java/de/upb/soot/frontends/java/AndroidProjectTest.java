package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.core.SootClass;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.options.Options;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class AndroidProjectTest {
  private WalaClassLoader loader;

  @Ignore
  public void test() {
    Set<String> sourcePath = new HashSet<>();
    sourcePath.add("src/test/resources/android-target/AndroidTestProject");
    String apkPath = "src/test/resources/android-target/AndroidTestProject/test.apk";
    String androidJar = "src/test/resources/android-target/platforms/android-26/android.jar";
    loader = new WalaClassLoader(sourcePath, apkPath, androidJar, null);
    List<SootClass> sootClasses = loader.getSootClasses();
  }

  @Test
  public void testApp() {
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.cha", "on");
    Options.v().setPhaseOption("cg", "all-reachable:true");

    Options.v().set_allow_phantom_refs(true);
    Options.v().set_no_bodies_for_excluded(true);

    // load library class with old soot
    Options.v()
        .set_process_dir(
            Collections.singletonList("src/test/resources/android-target/InsecureBank/out.jar"));
    Options.v()
        .set_soot_classpath("src/test/resources/android-target/platforms/android-28/android.jar");
    Scene.v().loadNecessaryClasses();

    Set<String> sourcePath = new HashSet<>();
    sourcePath.add("src/test/resources/android-target/InsecureBank/app/src/main/java");
    Set<String> libPath = new HashSet<>();
    libPath.add("src/test/resources/android-target/platforms/android-28/android.jar");
    libPath.add("src/test/resources/android-target/InsecureBank/out.jar");
    loader = new WalaClassLoader(sourcePath, libPath, null);
    List<SootClass> sootClasses = loader.getSootClasses();
    JimpleConverter jimpleConverter = new JimpleConverter(sootClasses);
    jimpleConverter.convertAllClasses();
    PackManager.v().getPack("cg").apply();
    assertTrue(Scene.v().hasCallGraph());
    assertEquals(63342, Scene.v().getCallGraph().size());
  }
}
