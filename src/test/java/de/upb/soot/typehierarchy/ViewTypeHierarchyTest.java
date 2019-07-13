package de.upb.soot.typehierarchy;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.IdentifierFactory;
import de.upb.soot.Project;
import de.upb.soot.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.io.File;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class ViewTypeHierarchyTest {

  private View view;

  @Before
  public void setup() {
    String jarFile = "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";
    assertTrue(new File(jarFile).exists());
    String currentClassPath = System.getProperty("java.class.path");
    String rtJarClassPth =
        Arrays.stream(currentClassPath.split(File.pathSeparator))
            .filter(pathEntry -> pathEntry.endsWith(File.separator + "rt.jar"))
            .collect(Collectors.joining(File.pathSeparator));
    Project<JavaClassPathAnalysisInputLocation> p =
        new Project<>(
            new JavaClassPathAnalysisInputLocation(jarFile + File.pathSeparator + rtJarClassPth));
    view = p.createOnDemandView();
  }

  @Test
  public void implementersOf() {
    TypeHierarchy typeHierarchy = new GraphViewTypeHierarchy(view);
    IdentifierFactory factory = view.getIdentifierFactory();
    JavaClassType iNamespace = factory.getClassType("INamespace", "de.upb.soot.namespaces");
    Set<JavaClassType> implementers = typeHierarchy.implementersOf(iNamespace);

    System.out.println(implementers);
  }

  @Test
  public void subclassesOf() {}

  @Test
  public void implementedInterfacesOf() {}

  @Test
  public void superClassOf() {}

  @Test
  public void superClassesOf() {}
}
