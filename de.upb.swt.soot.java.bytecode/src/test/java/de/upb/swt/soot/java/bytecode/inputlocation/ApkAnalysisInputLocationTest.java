package de.upb.swt.soot.java.bytecode.inputlocation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootField;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

public class ApkAnalysisInputLocationTest {

  private final String testPath = "../shared-test-resources/droidbench/Aliasing/Fields.apk";
  private final Path path = Paths.get(testPath);

  @Test
  public void testApkInput() {
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(new ApkAnalysisInputLocation(path))
            .build();

    JavaView view = project.createOnDemandView();

    Collection<JavaSootClass> classes = view.getClasses();

    JavaClassType targetClass =
        JavaIdentifierFactory.getInstance().getClassType("de.upb.futuresoot.fields.MainActivity");

    Optional<JavaSootClass> classOp = view.getClass(targetClass);
    assertTrue(classOp.isPresent());
    JavaSootClass javaSootClass = classOp.get();
    Set<? extends JavaSootField> fields = javaSootClass.getFields();
    assertFalse(fields.isEmpty());
    assertTrue(fields.size() == 8);
  }
}
