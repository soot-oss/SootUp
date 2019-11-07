package de.upb.swt.soot.test.callgraph.typehierarchy;

import categories.Java8Test;

import lib.annotations.callgraph.IndirectCall;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

@Category(Java8Test.class)
public class ClassloadingTest {

  private static final String DIR = System.getProperty("user.dir") + "/shared-test-resources/java-target/callgraph/Classloading/";
  private static URL CLv1;

  @Before
  public void setup() {

    try {
      CLv1 = new URL("file://" + DIR + "classloading-version-1.jar");
      CLv2 = new URL("file://" + DIR + "classloading-version-2.jar");
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    // Do I have to create and configure a view here?
  }

  private static final String CLS_NAME = "lib.IntComparator";

  /*
   * CL1
   * This test case uses an URLClassLoader in order to load classes from an
   * external .jar file. That class will be instantiated using
   * Class<?>.newInstance. Afterwards, it calls the compare on the Comparator
   * interface, which will resolve to the IntComparator from the given .jar at
   * runtime.
   *
   * Source: https://bitbucket.org/delors/jcg/src/master/jcg_testcases/src/main/resources/Classloading.md
   */
  @Test
  @IndirectCall(name = "compareTo", returnType = int.class, line = 34, resolvedTargets = "Ljava/lang/Integer;")
  public void compareTo() throws ClassNotFoundException, IllegalAccessException, InstantiationException {

    ClassLoader parent = ClassLoader.getSystemClassLoader();
    URL[] urls = new URL[] { CLv1 };
    URLClassLoader cl = URLClassLoader.newInstance(urls, parent);
    Class<?> cls = cl.loadClass(CLS_NAME);
    Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
    Integer one = Integer.valueOf(1);
    comparator.compare(one, one);
  }


  /*
   * CL2
   * This test case is basically the same as CL1. In contrast to this the generic
   * type of the class is already specified before calling
   * Class<Comparator<Integer>>.newInstance.
   *
   * Source: https://bitbucket.org/delors/jcg/src/master/jcg_testcases/src/main/resources/Classloading.md
   */
  @Test
  @IndirectCall(name = "compareTo", returnType = int.class, line = 35, resolvedTargets = "Ljava/lang/Integer;")
  public void compareTo() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
    ClassLoader parent = ClassLoader.getSystemClassLoader();
    URL[] urls = new URL[] { CLv2 };
    URLClassLoader cl = URLClassLoader.newInstance(urls, parent);
    Class<Comparator<Integer>> cls = (Class<Comparator<Integer>>) cl.loadClass(CLS_NAME);
    Comparator<Integer> comparator = cls.newInstance();
    Integer one = Integer.valueOf(1);
    comparator.compare(one, one);
  }

}

