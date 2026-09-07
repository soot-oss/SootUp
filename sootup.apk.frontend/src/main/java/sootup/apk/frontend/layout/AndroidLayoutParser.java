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

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

/**
 * Parses an APK's compiled {@code res/layout*} XML files for {@code android:onClick} attributes —
 * Android's XML-declared way of wiring a widget straight to a {@code void method(View)} on
 * whichever Context (in practice, almost always the hosting Activity) inflates the layout, without
 * any {@code setOnClickListener} call site for {@link
 * sootup.apk.frontend.entrypoint.AndroidCallbackEntryPointCreator} (step 3) to find.
 *
 * <p>Extracted per zip entry name ({@link #parseOnClickMethodNamesByFileFromApk}), not just as one
 * flat set, so {@code sootup.apk.frontend.entrypoint.AndroidLayoutEntryPointCreator} can match a
 * name to the specific Activity(s) that actually inflate a given layout — resolved via {@code
 * sootup.apk.frontend.resources.AndroidResourceTableParser} against the APK's compiled resource
 * table ({@code resources.arsc}) — instead of checking every name against every declared activity.
 * A caller that can't resolve a given file back to a specific activity still has the flat union of
 * every name available as a sound fallback; see that class for how the two combine.
 */
public final class AndroidLayoutParser {

  private static final Logger logger = LoggerFactory.getLogger(AndroidLayoutParser.class);
  private static final String ONCLICK_ATTR = "onClick";

  private AndroidLayoutParser() {}

  /**
   * Extracts every {@code android:onClick} method name declared anywhere in {@code
   * res/layout*}{@code /*.xml} entries of the given APK, keyed by the zip entry name (e.g. {@code
   * "res/layout/activity_main.xml"}) it was found in.
   */
  @NonNull
  public static Map<String, Set<String>> parseOnClickMethodNamesByFileFromApk(
      @NonNull Path apkPath) {
    Map<String, Set<String>> onClickMethodNamesByFile = new LinkedHashMap<>();
    try (ZipFile archive = new ZipFile(apkPath.toFile())) {
      Enumeration<? extends ZipEntry> entries = archive.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!isLayoutXmlEntry(entry.getName())) {
          continue;
        }
        try (InputStream layoutStream = archive.getInputStream(entry)) {
          Set<String> namesInFile = parseOnClickMethodNames(layoutStream);
          if (!namesInFile.isEmpty()) {
            onClickMethodNamesByFile.put(entry.getName(), namesInFile);
          }
        } catch (Exception e) {
          // Best-effort: one unparsable/unexpected layout entry shouldn't stop the rest of the
          // app's layouts from being scanned (mirrors AndroidVersionInfo's manifest parsing,
          // which is similarly best-effort against real-world/obfuscated APKs).
          logger.debug("Could not parse layout resource '{}': {}", entry.getName(), e.toString());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read layout resources from " + apkPath, e);
    }
    return onClickMethodNamesByFile;
  }

  /** Extracts every {@code android:onClick} method name declared in a single layout XML stream. */
  @NonNull
  public static Set<String> parseOnClickMethodNames(@NonNull InputStream layoutStream) {
    try {
      byte[] data = ByteStreams.toByteArray(layoutStream);
      OnClickCollectingVisitor visitor = new OnClickCollectingVisitor();
      new AxmlReader(data).accept(visitor);
      return visitor.onClickMethodNames;
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse layout XML", e);
    }
  }

  /**
   * Whether a zip entry name is a compiled layout XML resource. Resource directory names (unlike
   * file names) aren't affected by identifier/resource-name obfuscation, since the platform relies
   * on the {@code layout(-<qualifier>)} directory itself to pick a configuration-specific variant
   * at runtime.
   */
  static boolean isLayoutXmlEntry(@NonNull String entryName) {
    return entryName.startsWith("res/layout") && entryName.endsWith(".xml");
  }

  /**
   * A single flat, self-recursive visitor: {@code android:onClick} can appear on any element
   * regardless of tag or nesting depth, so unlike {@code AndroidManifestParser} there's no need for
   * per-level visitor classes — every node is handled identically.
   */
  private static final class OnClickCollectingVisitor extends AxmlVisitor {
    @NonNull private final Set<String> onClickMethodNames = new LinkedHashSet<>();

    @Override
    public void attr(String ns, String name, int resourceId, int attrType, Object obj) {
      if (ONCLICK_ATTR.equals(name) && obj != null) {
        onClickMethodNames.add(String.valueOf(obj));
      }
    }

    @Override
    public NodeVisitor child(String ns, String name) {
      return this;
    }
  }
}
