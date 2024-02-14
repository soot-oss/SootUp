import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import dexpler.DexClassSource;
import org.junit.Test;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.views.JavaView;
import sootup.java.core.views.MutableJavaView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Util.Util.dottedClassName;
import static Util.Util.isByteCodeClassName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ApkToDexTest {

  @Test
  public void testApkConversion() {
    MutableJavaView view;
    long startTime = System.currentTimeMillis();
    Path path = Paths.get("/Users/palaniappanmuthuraman/Desktop/APK's");
    File dir = new File(path.toString());
    File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".apk"));
      assert files != null;
      for (File child : files) {
        String apk_path = child.getAbsolutePath();
        ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
                new ApkAnalysisInputLocation<>(
                        Paths.get(apk_path),
                        "/Users/palaniappanmuthuraman/Documents/android-platforms",
                        DexClassLoadingOptions.Default.getBodyInterceptors());
        view = new MutableJavaView(sootClassApkAnalysisInputLocation);
        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        Map<String, EnumSet<ClassModifier>> classNamesList =
                sootClassApkAnalysisInputLocation.classNamesList;
        AtomicInteger successfulconvertedNumber = new AtomicInteger();
        boolean failed = false;
        System.out.println("Started " + child.getName() + " with " + classNamesList.size() + " number of classes.");
        try {
          MutableJavaView finalView = view;
          classNamesList.forEach(
                  (className, classModifiers) -> {
                    if (isByteCodeClassName(className)) {
                      className = dottedClassName(className);
                    }
                    Optional<? extends AbstractClassSource<JavaSootClass>> classSource =
                            sootClassApkAnalysisInputLocation.getClassSource(
                                    identifierFactory.getClassType(className), finalView);
                    if (classSource.isPresent()) {
                      DexClassSource dexClassSource = (DexClassSource) classSource.get();
                      ClassType classType = finalView.getIdentifierFactory().getClassType(className);
                      Set<SootMethod> sootMethods = new HashSet<>(dexClassSource.resolveMethods());
                      JavaSootClass sootClass =
                              new JavaSootClass(
                                      new OverridingJavaClassSource(
                                              new EagerInputLocation<>(),
                                              null,
                                              classType,
                                              null,
                                              null,
                                              null,
                                              new LinkedHashSet<>(),
                                              sootMethods,
                                              NoPositionInformation.getInstance(),
                                              EnumSet.of(ClassModifier.PUBLIC),
                                              Collections.emptyList(),
                                              Collections.emptyList(),
                                              Collections.emptyList()),
                                      SourceType.Application);
                      finalView.addClass(sootClass);
                      successfulconvertedNumber.getAndIncrement();
                    }
                    if(successfulconvertedNumber.get() % 100 == 0){
                      System.out.println("Successfully converted " + successfulconvertedNumber + " out of" + classNamesList.size() + " classes.");
                    }
                  });
        } catch (Exception exception) {
          exception.printStackTrace();
          System.out.println("Failed to convert the " + child.getName() + " which has " + classNamesList.size());
          failed = true;
        } finally {
          if(!failed) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
            System.out.println(
                    "Time Taken to load " + child.getName() + " "
                            + successfulconvertedNumber
                            + " out of " + classNamesList.size() + " classes: "
                            + dateFormat.format(System.currentTimeMillis() - startTime));
          }
        }
      }
  }

  public void writeToCSVFile(String app_name, int number_of_classes, long time_taken){
    String file_path = "resources/stats_file.csv";
    boolean fileIsEmpty = new File(file_path).length() == 0;
    try (CSVWriter csvWriter = new CSVWriter(new FileWriter(file_path, true))) {
      // If the file is empty, write headers
      if (fileIsEmpty) {
        String[] headers = {"app_name", "number_of_classes", "time_taken"};
        csvWriter.writeNext(headers);
      }

      // Add data to the CSV file
      String[] data = {app_name, number_of_classes +"" , time_taken+""}; // Replace with your actual data
      csvWriter.writeNext(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void loadAllClasses() {
    long startTime = System.currentTimeMillis();
//    Path path = Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/droidbench_apks");
    Path path = Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/playstore_apks");
//    Path path = Paths.get("/Users/palaniappanmuthuraman/Documents/Thesis/Evaluation/Evaluation_TaintBench/apks/taintbench_apks");
    File dir = new File(path.toString());
    File[] files = dir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".apk"));
    List<String> failedApks = new ArrayList<>();
    assert files != null;
//    for (File child : files) {
//      String name = child.getName();
      String name = "viber.apk";
      String apk_path = path + "/" + name;
      SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
      ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
              new ApkAnalysisInputLocation<>(
                      Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms",DexClassLoadingOptions.Default.getBodyInterceptors());
      JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
      Collection<JavaSootClass> classes;
      try{
        System.out.println("Loading Apk: " + name);
        classes = view.getClasses();
        view.getMethod(view.getIdentifierFactory().parseMethodSignature("<com.viber.voip.ViberApplication: void ViberApplication(android.app.Activity,boolean)>"));
        classes.forEach(JavaSootClass::getMethods);
//        writeToCSVFile(name,view.getNumberOfStoredClasses(),(System.currentTimeMillis() - startTime) / 1000);
      }
      catch (Exception exception){
        exception.printStackTrace();
        failedApks.add(name);
        System.out.println("Failed to convert the " + name +  " which has " + view.getNumberOfStoredClasses());
      }
//    }
    System.out.println(files.length - failedApks.size() + " passed out of " + files.length);
    System.out.println(failedApks);
  }

  @Test
  public void loadOneClass() {
    String apk_path = "resources/FlowSensitivity1.apk";
    ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation =
        new ApkAnalysisInputLocation<>(
            Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms", Collections.emptyList());
    JavaView view = new JavaView(sootClassApkAnalysisInputLocation);
    String className = "android.support.v4.app.FragmentState$1";
    String methodName = "FragmentState$1";
    ClassType classType = view.getIdentifierFactory().getClassType(className);
    assertTrue(view.getClass(classType).isPresent());
    // Retrieve class
    SootClass<JavaSootClassSource> sootClass =
        (SootClass<JavaSootClassSource>) view.getClass(classType).get();
    // write MethodSignature
    MethodSignature methodSignature =
        new MethodSignature(classType, methodName, Collections.emptyList(), VoidType.getInstance());
    // Retrieve method
    assertTrue(sootClass.getMethod(methodSignature.getSubSignature()).isPresent());
    SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();
  }
}
