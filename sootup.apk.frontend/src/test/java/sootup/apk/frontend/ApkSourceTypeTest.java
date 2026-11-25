package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

/** The classes of an APK can be loaded as application or library classes. */
public class ApkSourceTypeTest {

  private static final Path APK = Paths.get("src/test/resources/FlowSensitivity1.apk");

  private static ApkAnalysisInputLocation location(SourceType sourceType) {
    return new ApkAnalysisInputLocation(
        APK,
        new AndroidVersionInfo(APK, ""),
        DexBodyInterceptors.Default.bodyInterceptors(),
        sourceType);
  }

  @Test
  public void classesAreApplicationClassesByDefault() {
    ApkAnalysisInputLocation location =
        new ApkAnalysisInputLocation(
            APK, new AndroidVersionInfo(APK, ""), DexBodyInterceptors.Default.bodyInterceptors());
    JavaView view = new JavaView(location);
    JavaSootClass mainActivity =
        view.getClass(view.getIdentifierFactory().getClassType("de.ecspride.MainActivity")).get();
    assertTrue(mainActivity.isApplicationClass());
    // libraries statically linked into the app's dex are reported as library classes regardless
    // (see ApkAnalysisInputLocation#isBundledLibraryClass)
    JavaSootClass popupMenu =
        view.getClass(
                view.getIdentifierFactory().getClassType("android.support.v7.widget.PopupMenu"))
            .get();
    assertTrue(popupMenu.isLibraryClass());
  }

  @Test
  public void classesHaveTheGivenSourceType() {
    JavaView view = new JavaView(location(SourceType.Library));
    ClassType supportClass =
        view.getIdentifierFactory().getClassType("android.support.v7.widget.PopupMenu");
    JavaSootClass library = view.getClass(supportClass).get();
    assertTrue(library.isLibraryClass());
    assertFalse(library.isApplicationClass());
    assertTrue(view.getClasses().allMatch(JavaSootClass::isLibraryClass));
  }
}
