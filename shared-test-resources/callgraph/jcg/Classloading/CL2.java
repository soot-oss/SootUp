package demo;
import lib.annotations.callgraph.IndirectCall;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;
public class Main {
    private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static URL CLv2;
    static {
        try {
            CLv2 = new URL("file://" + DIR + "classloading-version-2.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    private static final String CLS_NAME = "lib.IntComparator";
    @IndirectCall(name = "compareTo", returnType = int.class, line = 35, resolvedTargets = "Ljava/lang/Integer;")
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        URL[] urls = new URL[] { CLv2 };
        URLClassLoader cl = URLClassLoader.newInstance(urls, parent);
        Class<Comparator<Integer>> cls = (Class<Comparator<Integer>>) cl.loadClass(CLS_NAME);
        Comparator<Integer> comparator = cls.newInstance();
        Integer one = Integer.valueOf(1);
        comparator.compare(one, one);
    }
}
