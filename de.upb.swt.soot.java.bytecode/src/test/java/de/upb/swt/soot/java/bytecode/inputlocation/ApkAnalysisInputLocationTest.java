package de.upb.swt.soot.java.bytecode.inputlocation;

public class ApkAnalysisInputLocationTest {

    private final String testPath = "../shared-test-resources/apk-samples/";

    /*@Test
    public void testApkInput() {
        JavaProject project =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(
                                new ApkAnalysisInputLocation(testPath + "droidbench/Aliasing"))
                        .build();

        JavaView view = project.createOnDemandView();

        JavaClassType targetClass = JavaIdentifierFactory.getInstance().getClassType("de.ecspride.MainActivity");

        Optional<JavaSootClass> classOp = view.getClass(targetClass);
        assertTrue(classOp.isPresent());
    }*/

}