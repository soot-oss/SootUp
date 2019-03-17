package de.upb.soot.frontends.java;

import categories.Java8Test;
import de.upb.soot.core.SootClass;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

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
}
