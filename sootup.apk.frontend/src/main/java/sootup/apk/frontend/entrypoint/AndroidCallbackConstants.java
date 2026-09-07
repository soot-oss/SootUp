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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;

/**
 * Android SDK callback/listener interfaces, and the methods the framework invokes on an
 * implementation once it's registered (e.g. {@code View#setOnClickListener}, {@code
 * LocationManager#requestLocationUpdates}, {@code Application#registerActivityLifecycleCallbacks}).
 * Not limited to UI widgets — any interface the framework calls back on once an instance is handed
 * to a registration API belongs here, whatever subsystem it belongs to.
 *
 * <p>An app class implementing one of these interfaces is reachable from the framework the same way
 * a lifecycle callback is: nothing in the app's own bytecode necessarily calls it directly. Unlike
 * the per-component lifecycle table in {@link AndroidEntryPointConstants}, membership here doesn't
 * depend on a manifest declaration — any class in the app implementing one of these interfaces is a
 * potential callback target, however it was wired up (a layout's {@code android:onClick}, a call to
 * {@code setOnClickListener}, etc.). This intentionally over-approximates: it doesn't verify the
 * implementing class was ever actually registered anywhere, matching how the rest of this module's
 * CHA/RTA-based call graph is already an over-approximation.
 */
public final class AndroidCallbackConstants {

  private AndroidCallbackConstants() {}

  private static final Map<String, List<LifecycleMethod>> LISTENER_INTERFACE_METHODS =
      buildListenerInterfaceMethods();

  private static Map<String, List<LifecycleMethod>> buildListenerInterfaceMethods() {
    Map<String, List<LifecycleMethod>> table = new LinkedHashMap<>();

    table.put(
        "android.view.View$OnClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onClick", "void", Collections.singletonList("android.view.View"))));
    table.put(
        "android.view.View$OnLongClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onLongClick", "boolean", Collections.singletonList("android.view.View"))));
    table.put(
        "android.view.View$OnTouchListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onTouch",
                "boolean",
                Arrays.asList("android.view.View", "android.view.MotionEvent"))));
    table.put(
        "android.view.View$OnKeyListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onKey",
                "boolean",
                Arrays.asList("android.view.View", "int", "android.view.KeyEvent"))));
    table.put(
        "android.view.View$OnFocusChangeListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onFocusChange", "void", Arrays.asList("android.view.View", "boolean"))));
    table.put(
        "android.view.View$OnCreateContextMenuListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onCreateContextMenu",
                "void",
                Arrays.asList(
                    "android.view.ContextMenu",
                    "android.view.View",
                    "android.view.ContextMenu$ContextMenuInfo"))));
    table.put(
        "android.view.MenuItem$OnMenuItemClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onMenuItemClick", "boolean", Collections.singletonList("android.view.MenuItem"))));

    table.put(
        "android.widget.AdapterView$OnItemClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onItemClick",
                "void",
                Arrays.asList("android.widget.AdapterView", "android.view.View", "int", "long"))));
    table.put(
        "android.widget.AdapterView$OnItemLongClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onItemLongClick",
                "boolean",
                Arrays.asList("android.widget.AdapterView", "android.view.View", "int", "long"))));
    table.put(
        "android.widget.AdapterView$OnItemSelectedListener",
        Arrays.asList(
            new LifecycleMethod(
                "onItemSelected",
                "void",
                Arrays.asList("android.widget.AdapterView", "android.view.View", "int", "long")),
            new LifecycleMethod(
                "onNothingSelected",
                "void",
                Collections.singletonList("android.widget.AdapterView"))));
    table.put(
        "android.widget.CompoundButton$OnCheckedChangeListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onCheckedChanged",
                "void",
                Arrays.asList("android.widget.CompoundButton", "boolean"))));
    table.put(
        "android.widget.SeekBar$OnSeekBarChangeListener",
        Arrays.asList(
            new LifecycleMethod(
                "onProgressChanged",
                "void",
                Arrays.asList("android.widget.SeekBar", "int", "boolean")),
            new LifecycleMethod(
                "onStartTrackingTouch",
                "void",
                Collections.singletonList("android.widget.SeekBar")),
            new LifecycleMethod(
                "onStopTrackingTouch",
                "void",
                Collections.singletonList("android.widget.SeekBar"))));

    table.put(
        "android.content.DialogInterface$OnClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onClick", "void", Arrays.asList("android.content.DialogInterface", "int"))));
    table.put(
        "android.content.DialogInterface$OnMultiChoiceClickListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onClick",
                "void",
                Arrays.asList("android.content.DialogInterface", "int", "boolean"))));
    table.put(
        "android.content.DialogInterface$OnCancelListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onCancel", "void", Collections.singletonList("android.content.DialogInterface"))));
    table.put(
        "android.content.DialogInterface$OnDismissListener",
        Collections.singletonList(
            new LifecycleMethod(
                "onDismiss",
                "void",
                Collections.singletonList("android.content.DialogInterface"))));

    table.put(
        "android.text.TextWatcher",
        Arrays.asList(
            new LifecycleMethod(
                "beforeTextChanged",
                "void",
                Arrays.asList("java.lang.CharSequence", "int", "int", "int")),
            new LifecycleMethod(
                "onTextChanged",
                "void",
                Arrays.asList("java.lang.CharSequence", "int", "int", "int")),
            new LifecycleMethod(
                "afterTextChanged", "void", Collections.singletonList("android.text.Editable"))));

    table.put(
        "android.location.LocationListener",
        Arrays.asList(
            new LifecycleMethod(
                "onLocationChanged",
                "void",
                Collections.singletonList("android.location.Location")),
            new LifecycleMethod(
                "onStatusChanged",
                "void",
                Arrays.asList("java.lang.String", "int", "android.os.Bundle")),
            new LifecycleMethod(
                "onProviderEnabled", "void", Collections.singletonList("java.lang.String")),
            new LifecycleMethod(
                "onProviderDisabled", "void", Collections.singletonList("java.lang.String"))));
    table.put(
        "android.app.Application$ActivityLifecycleCallbacks",
        Arrays.asList(
            new LifecycleMethod(
                "onActivityCreated",
                "void",
                Arrays.asList("android.app.Activity", "android.os.Bundle")),
            new LifecycleMethod(
                "onActivityStarted", "void", Collections.singletonList("android.app.Activity")),
            new LifecycleMethod(
                "onActivityResumed", "void", Collections.singletonList("android.app.Activity")),
            new LifecycleMethod(
                "onActivityPaused", "void", Collections.singletonList("android.app.Activity")),
            new LifecycleMethod(
                "onActivityStopped", "void", Collections.singletonList("android.app.Activity")),
            new LifecycleMethod(
                "onActivitySaveInstanceState",
                "void",
                Arrays.asList("android.app.Activity", "android.os.Bundle")),
            new LifecycleMethod(
                "onActivityDestroyed", "void", Collections.singletonList("android.app.Activity"))));

    return Collections.unmodifiableMap(table);
  }

  /**
   * Maps each known listener interface's fully qualified name to the callback methods the framework
   * invokes on an implementation of it.
   */
  @NonNull
  public static Map<String, List<LifecycleMethod>> getListenerInterfaceMethods() {
    return LISTENER_INTERFACE_METHODS;
  }
}
