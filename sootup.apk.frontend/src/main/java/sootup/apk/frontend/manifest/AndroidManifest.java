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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The parsed, structured contents of an APK's {@code AndroidManifest.xml} relevant to call-graph
 * construction: the app's package name, its (optional) custom {@code Application} class, and its
 * declared activities, services, broadcast receivers and content providers.
 *
 * @see AndroidManifestParser
 */
public final class AndroidManifest {

  @NonNull private final String packageName;
  @Nullable private final String applicationClassName;
  @NonNull private final List<ManifestComponent> components;

  public AndroidManifest(
      @NonNull String packageName,
      @Nullable String applicationClassName,
      @NonNull List<ManifestComponent> components) {
    this.packageName = packageName;
    this.applicationClassName = applicationClassName;
    this.components = Collections.unmodifiableList(new ArrayList<>(components));
  }

  @NonNull
  public String getPackageName() {
    return packageName;
  }

  /** The app's custom {@code android.app.Application} subclass, if one is declared. */
  @NonNull
  public Optional<String> getApplicationClassName() {
    return Optional.ofNullable(applicationClassName);
  }

  @NonNull
  public List<ManifestComponent> getComponents() {
    return components;
  }

  @NonNull
  public List<ManifestComponent> getComponents(@NonNull AndroidComponentType type) {
    return components.stream()
        .filter(component -> component.getType() == type)
        .collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return "AndroidManifest{"
        + "packageName='"
        + packageName
        + '\''
        + ", applicationClassName='"
        + applicationClassName
        + '\''
        + ", components="
        + components
        + '}';
  }
}
