package sootup.apk.frontend.layout;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import org.junit.jupiter.api.Test;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;

/**
 * Validates step 4 of {@code ANDROID_CALL_GRAPH_PLAN.md}. None of this module's checked-in sample
 * APKs happen to use {@code android:onClick} in their layouts (verified by scanning them), so the
 * extraction logic is validated with a real compiled binary XML document built via the same {@code
 * axml} library's writer — a genuine round trip through the binary XML format, not a string/DOM
 * stand-in for it.
 */
public class AndroidLayoutParserTest {

  private static byte[] buildLayout(String... onClickMethodNames) throws IOException {
    AxmlWriter writer = new AxmlWriter();
    NodeVisitor root = writer.child(null, "LinearLayout");
    for (String methodName : onClickMethodNames) {
      NodeVisitor button = root.child(null, "Button");
      if (methodName != null) {
        button.attr(
            "http://schemas.android.com/apk/res/android",
            "onClick",
            0x0101026c,
            NodeVisitor.TYPE_STRING,
            methodName);
      }
    }
    return writer.toByteArray();
  }

  @Test
  public void testExtractsOnClickMethodNames() throws IOException {
    byte[] layout = buildLayout("onSaveClicked", "onCancelClicked");
    Set<String> onClickMethodNames =
        AndroidLayoutParser.parseOnClickMethodNames(new ByteArrayInputStream(layout));
    assertEquals(Set.of("onSaveClicked", "onCancelClicked"), onClickMethodNames);
  }

  @Test
  public void testNestedElementsAreAlsoScanned() throws IOException {
    // android:onClick can appear on any element regardless of nesting depth, not just direct
    // children of the root.
    AxmlWriter writer = new AxmlWriter();
    NodeVisitor root = writer.child(null, "LinearLayout");
    NodeVisitor nested = root.child(null, "RelativeLayout");
    NodeVisitor button = nested.child(null, "Button");
    button.attr(
        "http://schemas.android.com/apk/res/android",
        "onClick",
        0x0101026c,
        NodeVisitor.TYPE_STRING,
        "onDeeplyNestedClick");

    Set<String> onClickMethodNames =
        AndroidLayoutParser.parseOnClickMethodNames(new ByteArrayInputStream(writer.toByteArray()));
    assertEquals(Set.of("onDeeplyNestedClick"), onClickMethodNames);
  }

  @Test
  public void testLayoutWithoutOnClickReturnsEmptySet() throws IOException {
    byte[] layout = buildLayout((String) null);
    Set<String> onClickMethodNames =
        AndroidLayoutParser.parseOnClickMethodNames(new ByteArrayInputStream(layout));
    assertTrue(onClickMethodNames.isEmpty());
  }

  @Test
  public void testIsLayoutXmlEntry() {
    assertTrue(AndroidLayoutParser.isLayoutXmlEntry("res/layout/activity_main.xml"));
    assertTrue(AndroidLayoutParser.isLayoutXmlEntry("res/layout-land/activity_main.xml"));
    assertTrue(AndroidLayoutParser.isLayoutXmlEntry("res/layout-v21/activity_main.xml"));
    assertFalse(AndroidLayoutParser.isLayoutXmlEntry("res/values/strings.xml"));
    assertFalse(AndroidLayoutParser.isLayoutXmlEntry("res/layout/icon.png"));
    assertFalse(AndroidLayoutParser.isLayoutXmlEntry("AndroidManifest.xml"));
  }

  @Test
  public void testRealSampleApksHaveNoOnClickAttributes() {
    // Ground-truth regression check: confirms the real-APK scanning path (zip enumeration across
    // every res/layout* entry, including FlowSensitivity1.apk's ~50 bundled appcompat layouts)
    // runs cleanly and, consistent with a manual scan of these APKs, finds no android:onClick
    // usage in any of them.
    for (String apk :
        java.util.List.of(
            "src/test/resources/Crypto.apk",
            "src/test/resources/LocationLeak1.apk",
            "src/test/resources/FlowSensitivity1.apk")) {
      Set<String> onClickMethodNames =
          AndroidLayoutParser.parseOnClickMethodNamesFromApk(Paths.get(apk));
      assertTrue(onClickMethodNames.isEmpty(), apk + " unexpectedly has onClick attributes");
    }
  }
}
