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
import java.util.LinkedHashSet;
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
 * <p>This only extracts the method names declared in the XML; matching a name to the Activity(s)
 * that actually inflate a given layout would require parsing the APK's compiled resource table
 * (`resources.arsc`) to resolve {@code R.layout.*} constants back to file names, which isn't
 * implemented. See {@code sootup.apk.frontend.entrypoint.AndroidLayoutEntryPointCreator} for how
 * the extracted names are turned into entry points despite that.
 */
public final class AndroidLayoutParser {

  private static final Logger logger = LoggerFactory.getLogger(AndroidLayoutParser.class);
  private static final String ONCLICK_ATTR = "onClick";

  private AndroidLayoutParser() {}

  /**
   * Extracts every {@code android:onClick} method name declared anywhere in {@code
   * res/layout*}{@code /*.xml} entries of the given APK.
   */
  @NonNull
  public static Set<String> parseOnClickMethodNamesFromApk(@NonNull Path apkPath) {
    Set<String> onClickMethodNames = new LinkedHashSet<>();
    try (ZipFile archive = new ZipFile(apkPath.toFile())) {
      Enumeration<? extends ZipEntry> entries = archive.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!isLayoutXmlEntry(entry.getName())) {
          continue;
        }
        try (InputStream layoutStream = archive.getInputStream(entry)) {
          onClickMethodNames.addAll(parseOnClickMethodNames(layoutStream));
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
    return onClickMethodNames;
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
