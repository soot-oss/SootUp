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

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import pxb.android.axml.AxmlReader;
import pxb.android.axml.AxmlVisitor;
import pxb.android.axml.NodeVisitor;

/**
 * Parses an APK's {@code AndroidManifest.xml} (Android's compiled binary XML format) into an {@link
 * AndroidManifest} model of the app's package name, its custom {@code Application} class and its
 * declared components.
 *
 * <p>This only covers the parts of the manifest relevant to call-graph entry-point generation. SDK
 * version parsing (used to pick an {@code android.jar}) remains the responsibility of {@link
 * sootup.apk.frontend.main.AndroidVersionInfo}.
 */
public final class AndroidManifestParser {

  private static final String MANIFEST_ENTRY_NAME = "AndroidManifest.xml";

  private AndroidManifestParser() {}

  /** Extracts and parses {@code AndroidManifest.xml} from the given APK file. */
  @NonNull
  public static AndroidManifest parseFromApk(@NonNull Path apkPath) {
    try (ZipFile archive = new ZipFile(apkPath.toFile())) {
      ZipEntry manifestEntry = archive.getEntry(MANIFEST_ENTRY_NAME);
      if (manifestEntry == null) {
        throw new RuntimeException("No " + MANIFEST_ENTRY_NAME + " found in " + apkPath);
      }
      try (InputStream manifestStream = archive.getInputStream(manifestEntry)) {
        return parse(manifestStream);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read " + MANIFEST_ENTRY_NAME + " from " + apkPath, e);
    }
  }

  /** Parses an already-opened {@code AndroidManifest.xml} binary XML stream. */
  @NonNull
  public static AndroidManifest parse(@NonNull InputStream manifestStream) {
    try {
      byte[] data = ByteStreams.toByteArray(manifestStream);
      ManifestVisitor manifestVisitor = new ManifestVisitor();
      new AxmlReader(data).accept(new RootVisitor(manifestVisitor));
      return manifestVisitor.build();
    } catch (IOException e) {
      throw new RuntimeException("Failed to parse " + MANIFEST_ENTRY_NAME, e);
    }
  }

  /** Resolves a possibly-relative {@code android:name} value against the manifest package. */
  @NonNull
  static String resolveClassName(@NonNull String rawName, @NonNull String packageName) {
    if (rawName.isEmpty()) {
      return rawName;
    }
    if (rawName.charAt(0) == '.') {
      return packageName + rawName;
    }
    if (!rawName.contains(".") && !packageName.isEmpty()) {
      return packageName + "." + rawName;
    }
    return rawName;
  }

  private static boolean toBoolean(@Nullable Object obj, boolean defaultValue) {
    if (obj instanceof Boolean) {
      return (Boolean) obj;
    }
    if (obj == null) {
      return defaultValue;
    }
    String s = String.valueOf(obj);
    if ("true".equalsIgnoreCase(s)) {
      return true;
    }
    if ("false".equalsIgnoreCase(s)) {
      return false;
    }
    try {
      return Integer.parseInt(s) != 0;
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  /**
   * Receives the very first {@code <manifest>} start tag from {@link AxmlReader#accept}: the
   * top-level visitor passed to {@code accept} plays the role of the (virtual) XML document node,
   * so the {@code <manifest>} element itself only arrives via a {@code child(...)} callback, not
   * via {@code attr(...)} calls on this instance.
   */
  private static final class RootVisitor extends AxmlVisitor {
    @NonNull private final ManifestVisitor manifestVisitor;

    RootVisitor(@NonNull ManifestVisitor manifestVisitor) {
      this.manifestVisitor = manifestVisitor;
    }

    @Override
    public NodeVisitor child(String ns, String name) {
      return "manifest".equals(name) ? manifestVisitor : AxmlReader.EMPTY_VISITOR;
    }
  }

  private static final class ManifestVisitor extends NodeVisitor {
    @NonNull private String packageName = "";
    @Nullable private String applicationClassName;
    @NonNull private final List<ManifestComponent> components = new ArrayList<>();

    @Override
    public void attr(String ns, String name, int resourceId, int attrType, Object obj) {
      if ("package".equals(name) && obj != null) {
        packageName = String.valueOf(obj);
      }
    }

    @Override
    public NodeVisitor child(String ns, String name) {
      return "application".equals(name) ? new ApplicationVisitor(this) : AxmlReader.EMPTY_VISITOR;
    }

    @NonNull String resolveClassName(@NonNull String rawName) {
      return AndroidManifestParser.resolveClassName(rawName, packageName);
    }

    void setApplicationClassName(@NonNull String applicationClassName) {
      this.applicationClassName = applicationClassName;
    }

    void addComponent(@NonNull ManifestComponent component) {
      components.add(component);
    }

    @NonNull AndroidManifest build() {
      return new AndroidManifest(packageName, applicationClassName, components);
    }
  }

  private static final class ApplicationVisitor extends NodeVisitor {
    @NonNull private final ManifestVisitor parent;

    ApplicationVisitor(@NonNull ManifestVisitor parent) {
      this.parent = parent;
    }

    @Override
    public void attr(String ns, String name, int resourceId, int attrType, Object obj) {
      if ("name".equals(name) && obj != null) {
        parent.setApplicationClassName(parent.resolveClassName(String.valueOf(obj)));
      }
    }

    @Override
    public NodeVisitor child(String ns, String name) {
      AndroidComponentType type = componentTypeOf(name);
      return type != null ? new ComponentVisitor(parent, type) : AxmlReader.EMPTY_VISITOR;
    }

    @Nullable
    private static AndroidComponentType componentTypeOf(String tag) {
      switch (tag) {
        case "activity":
          return AndroidComponentType.ACTIVITY;
        case "service":
          return AndroidComponentType.SERVICE;
        case "receiver":
          return AndroidComponentType.BROADCAST_RECEIVER;
        case "provider":
          return AndroidComponentType.CONTENT_PROVIDER;
        default:
          return null;
      }
    }
  }

  private static final class ComponentVisitor extends NodeVisitor {
    @NonNull private final ManifestVisitor parent;
    @NonNull private final AndroidComponentType type;
    @Nullable private String className;
    private boolean exported;
    private boolean exportedExplicit;
    private boolean enabled = true;
    @NonNull private final List<IntentFilter> intentFilters = new ArrayList<>();

    ComponentVisitor(@NonNull ManifestVisitor parent, @NonNull AndroidComponentType type) {
      this.parent = parent;
      this.type = type;
    }

    @Override
    public void attr(String ns, String name, int resourceId, int attrType, Object obj) {
      if ("name".equals(name) && obj != null) {
        className = parent.resolveClassName(String.valueOf(obj));
      } else if ("exported".equals(name)) {
        exported = toBoolean(obj, exported);
        exportedExplicit = true;
      } else if ("enabled".equals(name)) {
        enabled = toBoolean(obj, enabled);
      }
    }

    @Override
    public NodeVisitor child(String ns, String name) {
      return "intent-filter".equals(name)
          ? new IntentFilterVisitor(this)
          : AxmlReader.EMPTY_VISITOR;
    }

    void addIntentFilter(@NonNull IntentFilter intentFilter) {
      intentFilters.add(intentFilter);
    }

    @Override
    public void end() {
      if (className == null) {
        // malformed/obfuscated component declaration without a resolvable class name; skip it
        // rather than registering a bogus entry point.
        return;
      }
      // Pre-API-31 default: components with at least one intent-filter are exported unless
      // explicitly marked otherwise; components without one default to not exported.
      boolean resolvedExported = exportedExplicit ? exported : !intentFilters.isEmpty();
      parent.addComponent(
          new ManifestComponent(type, className, resolvedExported, enabled, intentFilters));
    }
  }

  private static final class IntentFilterVisitor extends NodeVisitor {
    @NonNull private final ComponentVisitor parent;
    @NonNull private final List<String> actions = new ArrayList<>();
    @NonNull private final List<String> categories = new ArrayList<>();

    IntentFilterVisitor(@NonNull ComponentVisitor parent) {
      this.parent = parent;
    }

    @Override
    public NodeVisitor child(String ns, String name) {
      if ("action".equals(name)) {
        return new NameCollectingVisitor(actions);
      }
      if ("category".equals(name)) {
        return new NameCollectingVisitor(categories);
      }
      return AxmlReader.EMPTY_VISITOR;
    }

    @Override
    public void end() {
      parent.addIntentFilter(new IntentFilter(actions, categories));
    }
  }

  /** Captures the {@code android:name} attribute of a leaf {@code <action>}/{@code <category>}. */
  private static final class NameCollectingVisitor extends NodeVisitor {
    @NonNull private final List<String> target;

    NameCollectingVisitor(@NonNull List<String> target) {
      this.target = target;
    }

    @Override
    public void attr(String ns, String name, int resourceId, int attrType, Object obj) {
      if ("name".equals(name) && obj != null) {
        target.add(String.valueOf(obj));
      }
    }
  }
}
