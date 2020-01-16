package demo;
import lib.annotations.callgraph.IndirectCall;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;
public class Demo {
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
