package sootup.apk.frontend.resources;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Validates {@link AndroidResourceTableParser} against real, checked-in {@code resources.arsc} data
 * — all built by a modern {@code aapt2}, the exact case that broke {@code de.upb.cs.swt:axml}'s own
 * {@code pxb.android.arsc.ArscParser} (see this class's own class doc for the confirmed root cause)
 * and motivated writing this parser instead of reusing it.
 */
public class AndroidResourceTableParserTest {

  @Test
  public void testLocationLeak1ResolvesItsSingleRealLayout() {
    // The clean, fully-confirmed case: LocationLeak1.apk's own onCreate calls
    // setContentView(2130903040) - decodes to exactly this resource ID - and this is the only
    // layout-type resource declared in its resources.arsc.
    Map<Integer, Set<String>> result =
        AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(
            Paths.get("src/test/resources/LocationLeak1.apk"), "layout");

    assertEquals(Set.of("res/layout/activity_location_leak1.xml"), result.get(2130903040));
    assertEquals(1, result.size());
  }

  @Test
  public void testCryptoResolvesItsLayoutAndItsMultipleConfigVariants() {
    // Crypto.apk declares res/layout/activity_main.xml AND res/layout-v1/activity_main.xml under
    // the same resource ID (two config variants of one resource) - both must come back, since
    // which one a device actually inflates isn't statically known (see the class doc: "union,
    // not one arbitrarily picked").
    Map<Integer, Set<String>> result =
        AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(
            Paths.get("src/test/resources/Crypto.apk"), "layout");

    assertEquals(1, result.size());
    Set<String> files = result.values().iterator().next();
    assertEquals(Set.of("res/layout/activity_main.xml", "res/layout-v1/activity_main.xml"), files);
  }

  @Test
  public void testFlowSensitivity1ResolvesAllTwentySevenBundledLayouts() {
    // A larger, real-world-shaped package (support-v4/v7 bundled in) - confirms the parser
    // handles many types/many entries/many config variants in one package without missing or
    // misattributing any, not just a minimal single-layout APK.
    Map<Integer, Set<String>> result =
        AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(
            Paths.get("src/test/resources/FlowSensitivity1.apk"), "layout");

    assertEquals(27, result.size());
    assertTrue(
        result.values().stream().anyMatch(files -> files.contains("res/layout/activity_main.xml")));
  }

  @Test
  public void testNonLayoutTypeResolvesDifferentEntries() {
    // Crypto.apk's setContentView literal (2130837504) turns out, on inspection, not to be a
    // layout resource at all - it's a *string* resource ("HelloWorld") whose ID happens to be
    // adjacent to the real layout ID. This is real, pre-existing drift in that checked-in fixture
    // between its compiled R constants and its packaged resources.arsc (not a parser bug - the
    // string-type lookup below independently confirms what the literal actually resolves to),
    // and it's exactly the scenario AndroidLayoutEntryPointCreator's fallback exists for: a
    // resolved resource ID that doesn't appear in the *requested* type's map at all.
    Map<Integer, Set<String>> stringResources =
        AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(
            Paths.get("src/test/resources/Crypto.apk"), "string");

    assertEquals(Set.of("HelloWorld"), stringResources.get(2130837504));
  }

  @Test
  public void testMalformedArscBytesYieldsEmptyMapNotException() {
    // Graceful empty result, not an exception - the caller's fallback is to treat every
    // candidate as unresolved (see AndroidLayoutEntryPointCreator's class doc).
    Map<Integer, Set<String>> result =
        AndroidResourceTableParser.parseFileNamesByResourceId(new byte[0], "layout");
    assertTrue(result.isEmpty());
  }

  @Test
  public void testUnknownTypeNameYieldsEmptyMap() {
    Map<Integer, Set<String>> result =
        AndroidResourceTableParser.parseFileNamesByResourceIdFromApk(
            Paths.get("src/test/resources/LocationLeak1.apk"), "this-type-does-not-exist");
    assertTrue(result.isEmpty());
  }
}
