// https://bitbucket.org/delors/jcg/src/master/jcg_testcases/src/main/resources/Classloading.md

// In this test case, to different versions of a class are loaded using an
// URLClassLoader. On both versions a call to <Comparator<Integer>>.compare is
// performed. After those different versioned classes are loaded, methods are
// called on the classes which must be resolved to different targets.

import lib.annotations.callgraph.IndirectCall;
import lib.annotations.callgraph.IndirectCalls;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

public class CL3 {

    private static final String DIR = System.getProperty("user.dir") + "/shared-test-resources/java-target/callgraph/Classloading/";
    private static URL CLv1;
    private static URL CLv2;
    private static final String CLS_NAME = "lib.IntComparator";

    static {
        try {
            CLv1 = new URL("file://" + DIR + "classloading-version-1.jar");
            CLv2 = new URL("file://" + DIR + "classloading-version-2.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @IndirectCalls({
            @IndirectCall(name = "compare", line = 53, returnType = int.class, resolvedTargets = "Ljava/lang/Integer;"),
            @IndirectCall(name = "gc", line = 54, resolvedTargets = "Ljava/lang/System;")
    })
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader oldParent = Thread.currentThread().getContextClassLoader();
        ClassLoader newParent = Demo.class.getClassLoader();

        URL[] oldResource = new URL[]{CLv1};
        URL[] newResource = new URL[]{CLv2};

        URLClassLoader oldVersionLoader = new URLClassLoader(oldResource, oldParent);
        URLClassLoader newVersionLoader= new URLClassLoader(newResource, newParent);

        System.out.println(CLv1.toExternalForm());

        Class<?> oldClass = oldVersionLoader.loadClass(CLS_NAME);
        Class<?> newClass = newVersionLoader.loadClass(CLS_NAME);

        Comparator<Integer> oldComparator = (Comparator<Integer>) oldClass.newInstance();
        Comparator<Integer> newComparator = (Comparator<Integer>) newClass.newInstance();

        Integer one = new Integer(1);

        oldComparator.compare(one,one);
        newComparator.compare(one,one);
    }
}