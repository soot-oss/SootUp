package sootup.apk.frontend.entrypoint;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.apk.frontend.manifest.AndroidComponentType;

/**
 * The fixed set of lifecycle callbacks the Android framework invokes on each component type. These
 * are a documented SDK contract, not something derivable from an individual app's bytecode (mirrors
 * FlowDroid's {@code AndroidEntryPointConstants}).
 */
public final class AndroidEntryPointConstants {

  private AndroidEntryPointConstants() {}

  private static final List<LifecycleMethod> APPLICATION_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod("onCreate", "void", Collections.emptyList()),
              new LifecycleMethod("onTerminate", "void", Collections.emptyList()),
              new LifecycleMethod("onLowMemory", "void", Collections.emptyList()),
              new LifecycleMethod("onTrimMemory", "void", Collections.singletonList("int")),
              new LifecycleMethod(
                  "onConfigurationChanged",
                  "void",
                  Collections.singletonList("android.content.res.Configuration")),
              new LifecycleMethod(
                  "attachBaseContext",
                  "void",
                  Collections.singletonList("android.content.Context"))));

  private static final List<LifecycleMethod> ACTIVITY_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod(
                  "onCreate", "void", Collections.singletonList("android.os.Bundle")),
              new LifecycleMethod("onStart", "void", Collections.emptyList()),
              new LifecycleMethod("onRestart", "void", Collections.emptyList()),
              new LifecycleMethod("onResume", "void", Collections.emptyList()),
              new LifecycleMethod("onPause", "void", Collections.emptyList()),
              new LifecycleMethod("onStop", "void", Collections.emptyList()),
              new LifecycleMethod("onDestroy", "void", Collections.emptyList()),
              new LifecycleMethod(
                  "onSaveInstanceState", "void", Collections.singletonList("android.os.Bundle")),
              new LifecycleMethod(
                  "onRestoreInstanceState", "void", Collections.singletonList("android.os.Bundle")),
              new LifecycleMethod(
                  "onNewIntent", "void", Collections.singletonList("android.content.Intent")),
              new LifecycleMethod(
                  "onActivityResult",
                  "void",
                  Arrays.asList("int", "int", "android.content.Intent")),
              new LifecycleMethod("onBackPressed", "void", Collections.emptyList()),
              new LifecycleMethod(
                  "onCreateOptionsMenu", "boolean", Collections.singletonList("android.view.Menu")),
              new LifecycleMethod(
                  "onOptionsItemSelected",
                  "boolean",
                  Collections.singletonList("android.view.MenuItem")),
              new LifecycleMethod(
                  "onRequestPermissionsResult",
                  "void",
                  Arrays.asList("int", "java.lang.String[]", "int[]")),
              // Activity extends ContextThemeWrapper -> ContextWrapper, so an override here is a
              // legitimate framework callback (see DroidBench's MethodOverride1: the leak is
              // entirely inside an overridden attachBaseContext, with no other entry point in the
              // app calling it).
              new LifecycleMethod(
                  "attachBaseContext",
                  "void",
                  Collections.singletonList("android.content.Context")),
              // Activity also implements ComponentCallbacks2 (onLowMemory/onTrimMemory) and
              // ComponentCallbacks (onConfigurationChanged) directly, not just Application (see
              // DroidBench's Lifecycle/EventOrdering1, which overrides onLowMemory on the Activity
              // itself).
              new LifecycleMethod("onLowMemory", "void", Collections.emptyList()),
              new LifecycleMethod("onTrimMemory", "void", Collections.singletonList("int")),
              new LifecycleMethod(
                  "onConfigurationChanged",
                  "void",
                  Collections.singletonList("android.content.res.Configuration"))));

  private static final List<LifecycleMethod> SERVICE_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod("onCreate", "void", Collections.emptyList()),
              new LifecycleMethod(
                  "onStartCommand", "int", Arrays.asList("android.content.Intent", "int", "int")),
              new LifecycleMethod(
                  "onBind",
                  "android.os.IBinder",
                  Collections.singletonList("android.content.Intent")),
              new LifecycleMethod(
                  "onUnbind", "boolean", Collections.singletonList("android.content.Intent")),
              new LifecycleMethod(
                  "onRebind", "void", Collections.singletonList("android.content.Intent")),
              new LifecycleMethod("onDestroy", "void", Collections.emptyList()),
              new LifecycleMethod("onLowMemory", "void", Collections.emptyList()),
              new LifecycleMethod("onTrimMemory", "void", Collections.singletonList("int")),
              new LifecycleMethod(
                  "attachBaseContext",
                  "void",
                  Collections.singletonList("android.content.Context"))));

  private static final List<LifecycleMethod> BROADCAST_RECEIVER_METHODS =
      Collections.unmodifiableList(
          Collections.singletonList(
              new LifecycleMethod(
                  "onReceive",
                  "void",
                  Arrays.asList("android.content.Context", "android.content.Intent"))));

  private static final List<LifecycleMethod> CONTENT_PROVIDER_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod("onCreate", "boolean", Collections.emptyList()),
              new LifecycleMethod(
                  "query",
                  "android.database.Cursor",
                  Arrays.asList(
                      "android.net.Uri",
                      "java.lang.String[]",
                      "java.lang.String",
                      "java.lang.String[]",
                      "java.lang.String")),
              new LifecycleMethod(
                  "insert",
                  "android.net.Uri",
                  Arrays.asList("android.net.Uri", "android.content.ContentValues")),
              new LifecycleMethod(
                  "delete",
                  "int",
                  Arrays.asList("android.net.Uri", "java.lang.String", "java.lang.String[]")),
              new LifecycleMethod(
                  "update",
                  "int",
                  Arrays.asList(
                      "android.net.Uri",
                      "android.content.ContentValues",
                      "java.lang.String",
                      "java.lang.String[]")),
              new LifecycleMethod(
                  "getType", "java.lang.String", Collections.singletonList("android.net.Uri"))));

  /** The lifecycle callbacks the framework may invoke on a component of the given type. */
  @NonNull
  public static List<LifecycleMethod> getLifecycleMethods(@NonNull AndroidComponentType type) {
    switch (type) {
      case APPLICATION:
        return APPLICATION_METHODS;
      case ACTIVITY:
        return ACTIVITY_METHODS;
      case SERVICE:
        return SERVICE_METHODS;
      case BROADCAST_RECEIVER:
        return BROADCAST_RECEIVER_METHODS;
      case CONTENT_PROVIDER:
        return CONTENT_PROVIDER_METHODS;
      default:
        return Collections.emptyList();
    }
  }

  /**
   * Looks up a single named lifecycle callback for a component type, e.g. {@code
   * getLifecycleMethod(SERVICE, "onStartCommand")}. Used where a caller needs one specific callback
   * rather than the full contract (e.g. ICC resolution, which targets only the callback relevant to
   * the specific triggering call).
   */
  @NonNull
  public static Optional<LifecycleMethod> getLifecycleMethod(
      @NonNull AndroidComponentType type, @NonNull String name) {
    return getLifecycleMethods(type).stream().filter(m -> m.getName().equals(name)).findFirst();
  }
}
