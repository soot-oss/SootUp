// https://bitbucket.org/delors/jcg/src/master/jcg_testcases/src/main/resources/Classloading.md

// This test case uses an URLClassLoader in order to load classes from an
// external .jar file. That class will be instantiated using
// Class<?>.newInstance. Afterwards, it calls the compare on the Comparator
// interface, which will resolve to the IntComparator from the given .jar at
// runtime.

import lib.annotations.callgraph.IndirectCall;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

public class CL1 {

    private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static URL CLv1;

    static {
        try {
            CLv1 = new URL("file://" + DIR + "classloading-version-1.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static final String CLS_NAME = "lib.IntComparator";

    @IndirectCall(name = "compareTo", returnType = int.class, line = 34, resolvedTargets = "Ljava/lang/Integer;")
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        URL[] urls = new URL[] { CLv1 };
        URLClassLoader cl = URLClassLoader.newInstance(urls, parent);
        Class<?> cls = cl.loadClass(CLS_NAME);
        Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
        Integer one = Integer.valueOf(1);
        comparator.compare(one, one);
    }
}