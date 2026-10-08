package sootup.apk.frontend.manifest;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AndroidManifestParserTest {

  @Test
  public void testCryptoManifest() {
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/Crypto.apk"));

    assertEquals("com.example", manifest.getPackageName());
    assertFalse(manifest.getApplicationClassName().isPresent());

    List<ManifestComponent> activities = manifest.getComponents(AndroidComponentType.ACTIVITY);
    assertEquals(1, activities.size());

    ManifestComponent mainActivity = activities.get(0);
    // relative ".MainActivity" must be resolved against the package name
    assertEquals("com.example.MainActivity", mainActivity.getClassName());
    assertTrue(mainActivity.isExported());
    assertTrue(mainActivity.isEnabled());
    assertEquals(1, mainActivity.getIntentFilters().size());
    assertTrue(
        mainActivity.getIntentFilters().get(0).getActions().contains("android.intent.action.MAIN"));
    assertTrue(
        mainActivity
            .getIntentFilters()
            .get(0)
            .getCategories()
            .contains("android.intent.category.LAUNCHER"));
  }

  @Test
  public void testLocationLeakManifest() {
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/LocationLeak1.apk"));

    assertEquals("de.ecspride", manifest.getPackageName());

    List<ManifestComponent> activities = manifest.getComponents(AndroidComponentType.ACTIVITY);
    assertEquals(1, activities.size());

    ManifestComponent activity = activities.get(0);
    // already fully qualified, no relative resolution needed
    assertEquals("de.ecspride.LocationLeak1", activity.getClassName());
    // no explicit android:exported, but it declares an intent-filter -> defaults to exported
    assertTrue(activity.isExported());
  }

  @Test
  public void testFlowSensitivityManifest() {
    AndroidManifest manifest =
        AndroidManifestParser.parseFromApk(Paths.get("src/test/resources/FlowSensitivity1.apk"));

    assertEquals("de.ecspride", manifest.getPackageName());

    List<ManifestComponent> activities = manifest.getComponents(AndroidComponentType.ACTIVITY);
    assertEquals(1, activities.size());
    assertEquals("de.ecspride.MainActivity", activities.get(0).getClassName());
  }

  @Test
  public void testResolveClassName() {
    assertEquals(
        "com.example.MainActivity",
        AndroidManifestParser.resolveClassName(".MainActivity", "com.example"));
    assertEquals(
        "com.example.MainActivity",
        AndroidManifestParser.resolveClassName("MainActivity", "com.example"));
    assertEquals(
        "com.example.MainActivity",
        AndroidManifestParser.resolveClassName("com.example.MainActivity", "com.example"));
  }

  @Test
  public void testMissingManifestThrows() {
    Path missingApk = Paths.get("src/test/resources/DoesNotExist.apk");
    org.junit.jupiter.api.Assertions.assertThrows(
        RuntimeException.class, () -> AndroidManifestParser.parseFromApk(missingApk));
  }
}
