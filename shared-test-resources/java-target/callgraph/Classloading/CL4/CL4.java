// https://bitbucket.org/delors/jcg/src/master/jcg_testcases/src/main/resources/Classloading.md

// This test case defines a custom classloader, that loads the class
// lib.IntComparator from a byte array. The array bytes contains the bytes of
// the ByteClassLoader class. In order to extract the bytes, we used
// java.nio.file.Files.readAllBytes().

import lib.annotations.callgraph.IndirectCall;
import java.util.Comparator;

public class Demo {

    @IndirectCall(name = "callback", line = 14, resolvedTargets = "Lcl4/Demo;")
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = new ByteClassLoader(ClassLoader.getSystemClassLoader());
        Class<?> cls = cl.loadClass("lib.IntComparator");
        Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
        Integer one = 1;
        comparator.compare(one, one);
    }

    public static void callback() { }
}