package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2025 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

/**
 * Tests that classes of an APK know their outer class, which dex files store in the system
 * annotations <code>EnclosingClass</code>, <code>EnclosingMethod</code> and <code>InnerClass
 * </code>.
 */
public class DexOuterClassTest {

  private static JavaView view;

  @BeforeAll
  public static void createView() {
    Path apkPath = Paths.get("src/test/resources/LocationLeak1.apk");
    view =
        new JavaView(
            new ApkAnalysisInputLocation(
                apkPath,
                new AndroidVersionInfo(apkPath, ""),
                DexBodyInterceptors.Default.bodyInterceptors()));
  }

  private static JavaSootClass getClass(String name) {
    ClassType type = view.getIdentifierFactory().getClassType(name);
    Optional<JavaSootClass> clazz = view.getClass(type);
    Assertions.assertTrue(clazz.isPresent(), name);
    return clazz.get();
  }

  @Test
  public void memberClassHasEnclosingClass() {
    // R$attr carries an EnclosingClass annotation
    JavaSootClass clazz = getClass("de.ecspride.R$attr");
    Assertions.assertTrue(clazz.isInnerClass());
    Assertions.assertEquals(
        Optional.of(view.getIdentifierFactory().getClassType("de.ecspride.R")),
        clazz.getOuterClass());
  }

  @Test
  public void anonymousClassHasClassOfEnclosingMethod() {
    // An anonymous class carries an EnclosingMethod annotation
    JavaSootClass clazz =
        getClass("android.support.v4.widget.SearchViewCompat$SearchViewCompatHoneycombImpl$1");
    Assertions.assertTrue(clazz.isInnerClass());
    Assertions.assertEquals(
        Optional.of(
            view.getIdentifierFactory()
                .getClassType(
                    "android.support.v4.widget.SearchViewCompat$SearchViewCompatHoneycombImpl")),
        clazz.getOuterClass());
  }

  @Test
  public void topLevelClassHasNoOuterClass() {
    JavaSootClass clazz = getClass("de.ecspride.LocationLeak1");
    Assertions.assertFalse(clazz.isInnerClass());
    Assertions.assertEquals(Optional.empty(), clazz.getOuterClass());
  }
}
