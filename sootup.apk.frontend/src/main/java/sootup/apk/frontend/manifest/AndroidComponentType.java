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

/**
 * The kinds of Android components that can be declared in {@code AndroidManifest.xml} and that
 * carry framework-invoked lifecycle callbacks.
 */
public enum AndroidComponentType {
  APPLICATION("android.app.Application"),
  ACTIVITY("android.app.Activity"),
  SERVICE("android.app.Service"),
  BROADCAST_RECEIVER("android.content.BroadcastReceiver"),
  CONTENT_PROVIDER("android.content.ContentProvider");

  private final String frameworkBaseClass;

  AndroidComponentType(String frameworkBaseClass) {
    this.frameworkBaseClass = frameworkBaseClass;
  }

  /** The fully qualified name of the Android SDK base class for this component type. */
  public String getFrameworkBaseClass() {
    return frameworkBaseClass;
  }
}
