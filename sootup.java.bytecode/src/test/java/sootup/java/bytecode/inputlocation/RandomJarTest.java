package sootup.java.bytecode.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

import java.util.ArrayList;
import java.util.Collection;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class RandomJarTest extends AnalysisInputLocationTest{

    private final String jarPath = System.getProperty("jarPath", "");

    public ArrayList<TestMetrics> testMetricsList = new ArrayList<>();

    @Test
    public void testJar() {
        System.out.println("Testing jar...");
        AnalysisInputLocation inputLocation =
                new JavaClassPathAnalysisInputLocation(jarPath);
        JavaView view = new JavaView(inputLocation);
        String exception = "No Exceptions :)";
        Collection<JavaSootClass> classes;
        long time_taken_for_classes = 0;
        long number_of_methods = 0;
        long time_taken_for_methods = 0;
        long number_of_classes = 0;
        try {
            long start = System.currentTimeMillis();
            classes = getClasses(view);
            number_of_classes = classes.size();
            time_taken_for_classes = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            number_of_methods = getMethods(classes);
            time_taken_for_methods = System.currentTimeMillis() - start;
        }
        catch (Exception e){
            exception = e.getMessage();
        }
        testMetricsList.add(new TestMetrics(jarPath,number_of_classes,number_of_methods,time_taken_for_classes,time_taken_for_methods,exception));
    }

    private Collection<JavaSootClass> getClasses(JavaView view){
        try {
            return view.getClasses();
        }
        catch (Exception e){
            throw new RuntimeException("Error while getting class list", e);
        }
    }

    private long getMethods(Collection<JavaSootClass> classes){
        try {
            return classes.stream().map(JavaSootClass::getMethods).mapToLong(Collection::size).sum();
        }
        catch (Exception e){
            throw new RuntimeException("Error while getting class list", e);
        }
    }



    public static class TestMetrics{
        String jar_name;
        long number_of_classes;
        long number_of_methods;
        long time_taken_for_classes;
        long time_taken_for_methods;
        String exception;

        public TestMetrics(String jar_name, long number_of_classes, long number_of_methods, long time_taken_for_classes, long time_taken_for_methods, String exception){
            this.jar_name = jar_name;
            this.number_of_classes = number_of_classes;
            this.number_of_methods = number_of_methods;
            this.time_taken_for_classes = time_taken_for_classes;
            this.time_taken_for_methods = time_taken_for_methods;
            this.exception = exception;
        }
    }
}

