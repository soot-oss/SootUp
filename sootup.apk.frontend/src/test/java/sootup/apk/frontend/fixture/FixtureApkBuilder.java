package sootup.apk.frontend.fixture;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jf.smali.Smali;
import org.jf.smali.SmaliOptions;
import pxb.android.axml.AxmlWriter;
import pxb.android.axml.NodeVisitor;
import sootup.apk.frontend.manifest.AndroidComponentType;
import sootup.apk.frontend.manifest.IntentFilter;

/**
 * Builds a real, minimal {@code .apk} file for step 9 fixture tests: assembles hand-written {@code
 * .smali} sources into a genuine {@code classes.dex} via {@code org.smali:smali} (the same library
 * {@code dexlib2} — already a compile dependency of this module — belongs to), and packs it with a
 * hand-encoded binary {@code AndroidManifest.xml} (and optional {@code res/layout} entries) via the
 * same {@code axml} writer already exercised by {@code AndroidLayoutParserTest}.
 *
 * <p>The result is a plain zip, not a signed/aligned/{@code resources.arsc}-bearing APK — that's
 * fine, since {@link sootup.apk.frontend.ApkAnalysisInputLocation} and this module's manifest/
 * layout parsers only ever read specific known zip entries directly, the same way they read the
 * checked-in DroidBench sample APKs. This exercises the real production pipeline end to end
 * (dexlib2's dex reader, this module's {@code instruction/*} Jimple translators, the real AXML
 * manifest/layout parsers) rather than the {@code JimpleStringAnalysisInputLocation} shortcut step
 * 7's tests use — deliberately, since step 9 exists to validate the actual APK ingestion path.
 */
final class FixtureApkBuilder {

  private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
  private static final int NAME_ATTR_RESOURCE_ID = 0x01010003;
  private static final int EXPORTED_ATTR_RESOURCE_ID = 0x01010010;
  private static final int ONCLICK_ATTR_RESOURCE_ID = 0x0101026c;

  private static final class Component {
    final AndroidComponentType type;
    final String fullyQualifiedClassName;
    final List<IntentFilter> intentFilters;

    Component(
        AndroidComponentType type,
        String fullyQualifiedClassName,
        List<IntentFilter> intentFilters) {
      this.type = type;
      this.fullyQualifiedClassName = fullyQualifiedClassName;
      this.intentFilters = intentFilters;
    }
  }

  private final List<String> smaliSources = new ArrayList<>();
  private final List<Component> components = new ArrayList<>();
  private final java.util.Map<String, String> layoutOnClickByFileName =
      new java.util.LinkedHashMap<>();

  FixtureApkBuilder smali(String smaliSource) {
    smaliSources.add(smaliSource);
    return this;
  }

  FixtureApkBuilder component(
      AndroidComponentType type, String fullyQualifiedClassName, List<IntentFilter> intentFilters) {
    components.add(new Component(type, fullyQualifiedClassName, intentFilters));
    return this;
  }

  FixtureApkBuilder activity(String fullyQualifiedClassName) {
    return component(AndroidComponentType.ACTIVITY, fullyQualifiedClassName, List.of());
  }

  FixtureApkBuilder layoutOnClick(String layoutFileName, String onClickMethodName) {
    layoutOnClickByFileName.put(layoutFileName, onClickMethodName);
    return this;
  }

  /** Assembles the smali, writes the manifest/layouts, and zips everything into a real .apk. */
  Path build() throws IOException {
    Path tmpDir = Files.createTempDirectory("sootup-fixture-apk");

    List<String> smaliFilePaths = new ArrayList<>();
    for (int i = 0; i < smaliSources.size(); i++) {
      Path smaliFile = tmpDir.resolve("Fixture" + i + ".smali");
      Files.write(smaliFile, smaliSources.get(i).getBytes(StandardCharsets.UTF_8));
      smaliFilePaths.add(smaliFile.toString());
    }

    Path dexOut = tmpDir.resolve("classes.dex");
    SmaliOptions options = new SmaliOptions();
    options.apiLevel = 19;
    options.outputDexFile = dexOut.toString();
    boolean assembled = Smali.assemble(options, smaliFilePaths);
    if (!assembled) {
      throw new IllegalStateException("smali assembly failed for fixture sources: " + smaliSources);
    }

    Path apkPath = tmpDir.resolve("fixture.apk");
    try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(apkPath))) {
      writeEntry(zip, "classes.dex", Files.readAllBytes(dexOut));
      writeEntry(zip, "AndroidManifest.xml", buildManifestXml());
      for (java.util.Map.Entry<String, String> layout : layoutOnClickByFileName.entrySet()) {
        writeEntry(zip, "res/layout/" + layout.getKey(), buildLayoutXml(layout.getValue()));
      }
    }
    return apkPath;
  }

  private static void writeEntry(ZipOutputStream zip, String name, byte[] content)
      throws IOException {
    zip.putNextEntry(new ZipEntry(name));
    zip.write(content);
    zip.closeEntry();
  }

  private byte[] buildManifestXml() throws IOException {
    AxmlWriter writer = new AxmlWriter();
    NodeVisitor manifestNode = writer.child(null, "manifest");
    NodeVisitor applicationNode = manifestNode.child(null, "application");
    for (Component component : components) {
      NodeVisitor componentNode = applicationNode.child(null, tagFor(component.type));
      componentNode.attr(
          ANDROID_NS,
          "name",
          NAME_ATTR_RESOURCE_ID,
          NodeVisitor.TYPE_STRING,
          component.fullyQualifiedClassName);
      componentNode.attr(
          ANDROID_NS, "exported", EXPORTED_ATTR_RESOURCE_ID, NodeVisitor.TYPE_INT_BOOLEAN, true);
      for (IntentFilter filter : component.intentFilters) {
        NodeVisitor filterNode = componentNode.child(null, "intent-filter");
        for (String action : filter.getActions()) {
          NodeVisitor actionNode = filterNode.child(null, "action");
          actionNode.attr(
              ANDROID_NS, "name", NAME_ATTR_RESOURCE_ID, NodeVisitor.TYPE_STRING, action);
        }
        for (String category : filter.getCategories()) {
          NodeVisitor categoryNode = filterNode.child(null, "category");
          categoryNode.attr(
              ANDROID_NS, "name", NAME_ATTR_RESOURCE_ID, NodeVisitor.TYPE_STRING, category);
        }
      }
    }
    return writer.toByteArray();
  }

  private byte[] buildLayoutXml(String onClickMethodName) throws IOException {
    AxmlWriter writer = new AxmlWriter();
    NodeVisitor root = writer.child(null, "LinearLayout");
    NodeVisitor button = root.child(null, "Button");
    button.attr(
        ANDROID_NS,
        "onClick",
        ONCLICK_ATTR_RESOURCE_ID,
        NodeVisitor.TYPE_STRING,
        onClickMethodName);
    return writer.toByteArray();
  }

  private static String tagFor(AndroidComponentType type) {
    switch (type) {
      case ACTIVITY:
        return "activity";
      case SERVICE:
        return "service";
      case BROADCAST_RECEIVER:
        return "receiver";
      case CONTENT_PROVIDER:
        return "provider";
      default:
        throw new IllegalArgumentException("Unsupported component type: " + type);
    }
  }
}
