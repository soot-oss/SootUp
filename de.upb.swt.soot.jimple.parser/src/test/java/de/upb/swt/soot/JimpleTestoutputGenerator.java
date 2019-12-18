package de.upb.swt.soot;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collections;
import org.junit.Test;

public class JimpleTestoutputGenerator {

  @Test
  public void generateFromJavasource() {

    Project project =
        JavaProject.builder(new JavaLanguage(8))
            .addClassPath(
                new JavaSourcePathAnalysisInputLocation(
                    Collections.singleton("src/test/resources/minimaltestsuite/")))
            .build();
    JavaView view = (JavaView) project.createOnDemandView();

    final SootClass sc =
        view.getClass(JavaIdentifierFactory.getInstance().getClassType("JavaEmptyClass")).get();

    // TODO: print the class to file

  }
}
