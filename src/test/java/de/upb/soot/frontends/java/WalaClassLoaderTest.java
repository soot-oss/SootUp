package de.upb.soot.frontends.java;

import static org.junit.Assert.assertEquals;

import com.ibm.wala.util.collections.Pair;
import de.upb.soot.core.SootClass;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

/**
 * This test loads application source code with WALA, library code with old soot and build call
 * graph with old soot.
 *
 * @author Linghui Luo
 */
public class WalaClassLoaderTest {
  private List<String> classesNew = new ArrayList<>();
  private List<String> classesOld = new ArrayList<>();
  private List<String> applicationClassesNew = new ArrayList();
  private List<String> applicationClassesOld = new ArrayList();

  private StringBuilder future = new StringBuilder();
  private StringBuilder old = new StringBuilder();

  private boolean flag = false;

  @Test
  public void testAndroidProject() {
    LoadClassesFromFutureSootAndOldSoot();
    PackManager.v().getPack("cg").apply();

    HashSet<Pair<String, String>> futureSoot = saveCallGraph(Scene.v().getCallGraph());

    future.append("futureSoot #cg edges: " + futureSoot.size() + "\n");

    HashSet<Pair<String, String>> oldSoot = saveCallGraph(getCallGraphFromOldSoot());

    assertEquals(996, classesNew.size());
    assertEquals(293, applicationClassesNew.size());
    assertEquals(classesNew.size(), classesOld.size());
    assertEquals(applicationClassesNew.size(), applicationClassesOld.size());

    old.append("oldSoot #cg edges: " + oldSoot.size() + "\n");

    int numOfEdgesNotFound1 = compareCallGraph(oldSoot, futureSoot);
    assertEquals(0, numOfEdgesNotFound1);

    future.append("Edges in futureSoot but NotFound in oldSoot:\n");

    int numOfEdgesNotFound2 = compareCallGraph(futureSoot, oldSoot);
    assertEquals(7, numOfEdgesNotFound2);
    /**
     * The following edges does not exist in oldSoot but in futureSoot, because in old soot static
     * field intialization are not included in jimple. see the difference here
     * https://gist.github.com/linghuiluo/8b54c06ae23c7f6f5109a967cce151ad
     */
    // eg: public static final int AppBaseTheme=0x7f050000;
    // 1.[<de.ecspride.R$style: void <clinit>()>,<de.ecspride.R$style: void <clinit>()>]
    // 2.[<de.ecspride.R$id: void <clinit>()>,<de.ecspride.R$id: void <clinit>()>]
    // 3.[<de.ecspride.R$layout: void <clinit>()>,<de.ecspride.R$layout: void <clinit>()>]
    // 4.[<de.ecspride.R$string: void <clinit>()>,<de.ecspride.R$string: void <clinit>()>]
    // 5.[<de.ecspride.BuildConfig: void <clinit>()>,<de.ecspride.BuildConfig: void <clinit>()>]
    // 6.[<de.ecspride.R$drawable: void <clinit>()>,<de.ecspride.R$drawable: void <clinit>()>]
    // 7.[<de.ecspride.R$menu: void <clinit>()>,<de.ecspride.R$menu: void <clinit>()>]

    old.append("--Classes exist in old soot but not in futureSoot--" + "\n");
    int k = 0;
    for (String c : classesOld) {
      if (!classesNew.contains(c)) {
        old.append(k + ": " + c + "\n");
        k++;
      }
    }
    assertEquals(0, k);

    if (flag) {
      System.out.println(old.toString());
      System.err.println(future.toString());
    }
  }

  /** load source code with FutureSoot, library code with old Soot. */
  private void LoadClassesFromFutureSootAndOldSoot() {
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.cha", "on");
    Options.v().setPhaseOption("cg", "all-reachable:true");

    String projectDir =
        new File("src/test/resources/android-target/ActivityLifecycle1").getAbsolutePath();
    Set<String> sourcePath = new HashSet<>();
    sourcePath.add(projectDir + File.separator + "src");
    sourcePath.add(projectDir + File.separator + "gen");

    Set<String> libPath = new HashSet<>();
    File libDir = new File(projectDir + File.separator + "libs");
    for (File file : libDir.listFiles()) {
      if (file.getName().endsWith(".jar")) {
        libPath.add(file.getAbsolutePath());
      }
    }

    String androidJarPath =
        new File("src/test/resources/android-target/platforms/android-17/android.jar")
            .getAbsolutePath();
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_no_bodies_for_excluded(true);

    // load library class with old soot
    Options.v().set_process_dir(new ArrayList<String>(libPath));
    Options.v().set_soot_classpath(androidJarPath);

    future.append("classpath " + Scene.v().getSootClassPath() + "\n");
    future.append("processdir " + Options.v().process_dir() + "\n");

    Scene.v().loadNecessaryClasses();

    // load application class with wala
    libPath.add(androidJarPath);
    WalaClassLoader loader = new WalaClassLoader(sourcePath, libPath, null);
    List<SootClass> sootClasses = loader.getSootClasses();
    assertEquals(10, sootClasses.size());

    // convert application class to old jimple
    JimpleConverter jimpleConverter = new JimpleConverter(sootClasses);
    jimpleConverter.convertAllClasses();

    future.append(Scene.v().getClasses().size() + "\n");
    output("de.ecspride.R$style", "futureSoot");

    Scene.v().getClasses().stream().forEach(c -> classesNew.add(c.getName()));
    Scene.v().getApplicationClasses().stream().forEach(c -> applicationClassesNew.add(c.getName()));
  }

  /**
   * load apk with old and generate call graph.
   *
   * @return
   */
  private CallGraph getCallGraphFromOldSoot() {
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.cha", "on");
    Options.v().setPhaseOption("cg", "all-reachable:true");
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_no_bodies_for_excluded(true);

    // set soot to process apk
    Options.v().set_src_prec(Options.src_prec_apk);
    String apkPath =
        new File("src/test/resources/android-target/ActivityLifecycle1/ActivityLifecycle1.apk")
            .getAbsolutePath();
    Options.v().set_process_dir(Collections.singletonList(apkPath));

    // set android plaform path
    String androidJar = new File("src/test/resources/android-target/platforms").getAbsolutePath();
    Options.v().set_android_jars(androidJar);

    Scene.v().loadNecessaryClasses();
    old.append("old classpath " + Scene.v().getSootClassPath() + "\n");
    old.append("old processdir " + Options.v().process_dir() + "\n");
    old.append(Scene.v().getClasses().size() + "\n");
    output("de.ecspride.R$style", "oldSoot");

    Scene.v().getClasses().stream().forEach(c -> classesOld.add(c.getName()));
    Scene.v().getApplicationClasses().stream().forEach(c -> applicationClassesOld.add(c.getName()));
    PackManager.v().getPack("cg").apply();
    return Scene.v().getCallGraph();
  }

  /**
   * @param cg
   * @return edges in call graph
   */
  private HashSet<Pair<String, String>> saveCallGraph(CallGraph cg) {
    HashSet<Pair<String, String>> ret = new HashSet<>();
    Iterator<soot.jimple.toolkits.callgraph.Edge> it = cg.iterator();
    while (it.hasNext()) {
      soot.jimple.toolkits.callgraph.Edge edge = it.next();
      if (edge.getSrc().toString().startsWith("<de.ecspride")) {
        ret.add(Pair.make(edge.getSrc().toString(), edge.getTgt().toString()));
      }
    }
    return ret;
  }

  /**
   * Count the number of edges which is in the expected call graph, but not in the actual call
   * graph.
   *
   * @param expected
   * @param actual
   * @return
   */
  private int compareCallGraph(
      HashSet<Pair<String, String>> expected, HashSet<Pair<String, String>> actual) {
    int i = 0;
    for (Pair<String, String> edge : expected) {
      boolean found = false;
      for (Pair<String, String> edgeOld : actual) {
        if (edge.fst.equals(edgeOld.fst) && edge.snd.equals(edgeOld.snd)) {
          found = true;
        }
      }
      if (!found) {
        i++;
        future.append("\t" + i + ":" + edge + "\n");
      }
    }
    return i;
  }

  private void output(String className, String fileName) {
    if (flag) {
      soot.SootClass c = Scene.v().getSootClass(className);
      File file = new File(fileName + ".jimple");
      PrintWriter writer;
      try {
        writer = new PrintWriter(file);
        soot.Printer.v().printTo(c, writer);
        writer.flush();
        writer.close();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }
}
