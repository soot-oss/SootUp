import dexpler.DexClassSource;
import org.junit.Test;
import sootup.core.cache.provider.FullCacheProvider;
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
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;
import sootup.java.core.views.MutableJavaView;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static Util.Util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class ApkToDexTest {

    @Test
    public void testApkConversion() {
        MutableJavaView view;
        long startTime = System.currentTimeMillis();
        String apk_path = "resources/FlowSensitivity1.apk";
        ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation = new ApkAnalysisInputLocation<>(Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms");
        JavaProject javaProject =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(sootClassApkAnalysisInputLocation)
                        .build();
        view = javaProject.createMutableView();
        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        Map<String, EnumSet<ClassModifier>> classNamesList = sootClassApkAnalysisInputLocation.classNamesList;
        AtomicInteger successfulconvertedNumber = new AtomicInteger();
        try {
            classNamesList.forEach((className, classModifiers) -> {
                if(isByteCodeClassName(className)){
                    className = dottedClassName(className);
                }
                Optional<? extends AbstractClassSource<JavaSootClass>> classSource =
                        sootClassApkAnalysisInputLocation.getClassSource(identifierFactory.getClassType(className), view);
                if (classSource.isPresent()) {
                    DexClassSource dexClassSource = (DexClassSource) classSource.get();
                    ClassType classType = view.getIdentifierFactory().getClassType(className);
                    Set<SootMethod> sootMethods = new HashSet<>(dexClassSource.resolveMethods());
                    JavaSootClass sootClass = new JavaSootClass(new OverridingJavaClassSource(new EagerInputLocation<>(), null, classType, null,null,null,new LinkedHashSet<>(), sootMethods, NoPositionInformation.getInstance(),
                            EnumSet.of(ClassModifier.PUBLIC),
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),SourceType.Application);
                    view.addClass(sootClass);
                    successfulconvertedNumber.getAndIncrement();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
            System.out.println("Time Taken to load 740 classes: " + dateFormat.format(System.currentTimeMillis() - startTime));
        }
    }

    @Test
    public void loadAllClasses(){
        String apk_path = "resources/FlowSensitivity1.apk";
        long startTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation = new ApkAnalysisInputLocation<>(Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms");
        JavaProject javaProject =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(sootClassApkAnalysisInputLocation)
                        .build();
        JavaView view = javaProject.createView(new FullCacheProvider<>());
        view.getClasses();
        System.out.println("Time Taken to load 740 classes: " + dateFormat.format(System.currentTimeMillis() - startTime));
        assertEquals(740, view.getAmountOfStoredClasses());
    }

    @Test
    public void loadOneClass(){
        String apk_path = "resources/FlowSensitivity1.apk";
        ApkAnalysisInputLocation<SootClass<JavaSootClassSource>> sootClassApkAnalysisInputLocation = new ApkAnalysisInputLocation<>(Paths.get(apk_path), "/Users/palaniappanmuthuraman/Documents/android-platforms");
        JavaProject javaProject =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(sootClassApkAnalysisInputLocation)
                        .build();
        View view = javaProject.createView();
        String className = "android.support.v4.app.FragmentState$1";
        String methodName = "FragmentState$1";
        ClassType classType = view.getIdentifierFactory().getClassType(className);
        assertTrue(view.getClass(classType).isPresent());
        // Retrieve class
        SootClass<JavaSootClassSource> sootClass =
                (SootClass<JavaSootClassSource>) view.getClass(classType).get();
        // write MethodSignature
        MethodSignature methodSignature = new MethodSignature(classType, methodName, Collections.emptyList(), VoidType.getInstance());
        // Retrieve method
        assertTrue(sootClass.getMethod(methodSignature.getSubSignature()).isPresent());
        SootMethod sootMethod = sootClass.getMethod(methodSignature.getSubSignature()).get();
    }
}
