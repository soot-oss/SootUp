package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertTrue;

public class ApkAnalysisInputLocationTest {

    private final String testPath = "../shared-test-resources/dexpler-apk/zng.apk";
    private final Path path = Paths.get(testPath);

    @Test
    public void testApkInput() {
        JavaProject project =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(
                                new ApkAnalysisInputLocation(path))
                        .build();

        JavaView view = project.createOnDemandView();

        JavaClassType targetClass = JavaIdentifierFactory.getInstance().getClassType("de.ecspride.MainActivity");

        Optional<JavaSootClass> classOp = view.getClass(targetClass);
        assertTrue(classOp.isPresent());
    }

}