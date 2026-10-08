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
import org.jspecify.annotations.NonNull;

/**
 * A single {@code <activity>}, {@code <service>}, {@code <receiver>} or {@code <provider>} declared
 * in {@code AndroidManifest.xml}, with its fully qualified class name already resolved (relative
 * {@code android:name} values such as {@code ".MainActivity"} are expanded against the manifest's
 * package name).
 */
public final class ManifestComponent {

  @NonNull private final AndroidComponentType type;
  @NonNull private final String className;
  private final boolean exported;
  private final boolean enabled;
  @NonNull private final List<IntentFilter> intentFilters;

  public ManifestComponent(
      @NonNull AndroidComponentType type,
      @NonNull String className,
      boolean exported,
      boolean enabled,
      @NonNull List<IntentFilter> intentFilters) {
    this.type = type;
    this.className = className;
    this.exported = exported;
    this.enabled = enabled;
    this.intentFilters = Collections.unmodifiableList(new ArrayList<>(intentFilters));
  }

  @NonNull
  public AndroidComponentType getType() {
    return type;
  }

  /** The fully qualified name of the class implementing this component. */
  @NonNull
  public String getClassName() {
    return className;
  }

  /** Whether this component is reachable from other apps (relevant for future ICC modeling). */
  public boolean isExported() {
    return exported;
  }

  public boolean isEnabled() {
    return enabled;
  }

  @NonNull
  public List<IntentFilter> getIntentFilters() {
    return intentFilters;
  }

  @Override
  public String toString() {
    return "ManifestComponent{"
        + "type="
        + type
        + ", className='"
        + className
        + '\''
        + ", exported="
        + exported
        + ", enabled="
        + enabled
        + ", intentFilters="
        + intentFilters
        + '}';
  }
}
