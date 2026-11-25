package sootup.apk.frontend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaSootField;
import sootup.java.core.views.JavaView;

public class DexFieldConstantValueTest {

  static JavaView view;

  @BeforeAll
  public static void createView() {
    Path apkPath = Paths.get("src/test/resources/LocationLeak1.apk");
    AndroidVersionInfo androidVersionInfo = new AndroidVersionInfo(apkPath, "");
    view =
        new JavaView(
            new ApkAnalysisInputLocation(
                apkPath, androidVersionInfo, DexBodyInterceptors.Default.bodyInterceptors()));
  }

  private Optional<Constant> constantValueOf(String className, String fieldName) {
    JavaSootField field =
        view
            .getClass(view.getIdentifierFactory().getClassType(className))
            .get()
            .getFields()
            .stream()
            .filter(f -> f.getName().equals(fieldName))
            .findFirst()
            .get();
    return field.getConstantValue();
  }

  @Test
  public void testStaticIntFieldHasItsInitialValue() {
    assertEquals(
        Optional.of(IntConstant.getInstance(0x7f030000)),
        constantValueOf("de.ecspride.R$layout", "activity_location_leak1"));
    assertEquals(
        Optional.of(IntConstant.getInstance(0x7f040002)),
        constantValueOf("de.ecspride.R$string", "hello_world"));
  }

  @Test
  public void testBooleanFieldHasAnIntConstant() {
    // like in method bodies and in class files, booleans are int constants
    assertEquals(
        Optional.of(IntConstant.getInstance(1)),
        constantValueOf("de.ecspride.BuildConfig", "DEBUG"));
  }

  @Test
  public void testFieldWithoutInitialValueHasNone() {
    // the activity's instance fields have no initial value in the dex file
    assertFalse(
        view
            .getClass(view.getIdentifierFactory().getClassType("de.ecspride.LocationLeak1"))
            .get()
            .getFields()
            .stream()
            .filter(f -> !f.isStatic())
            .anyMatch(f -> f.getConstantValue().isPresent()));
  }

  @Test
  public void testConstantHasTheTypeOfTheValue() {
    assertEquals(
        PrimitiveType.getInt(),
        constantValueOf("de.ecspride.R$id", "menu_settings").get().getType());
  }
}
