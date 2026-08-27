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
 * Callback methods invoked by the framework on asynchronous/threading constructs, but never through
 * an in-app call site the way an ordinary method call is — the same reason step 3's listener
 * interfaces need special handling, just for a different family of APIs.
 *
 * <p>{@code java.lang.Thread}'s own {@code start()}→{@code run()} dispatch is deliberately
 * <em>not</em> covered here: {@code sootup.callgraph}'s {@code
 * AbstractCallGraphAlgorithm#implicitStartRunCall} already resolves it, precisely, from the actual
 * {@code Thread#start()} call site's receiver type. What that mechanism can't see is everything
 * else that runs a {@code Runnable} without ever calling {@code .run()} explicitly in app code:
 * {@code Handler#post(Runnable)}/{@code postDelayed}, {@code View#post}/{@code postDelayed}, {@code
 * ExecutorService#submit/execute}, and so on. Rather than trace every such API's call sites
 * individually, this — like step 3 — takes the broader, simpler, sound-but-imprecise approach: any
 * app class implementing {@link #RUNNABLE_INTERFACE} has {@code run()} treated as reachable,
 * regardless of how (or whether) it can be proven to actually reach one of those APIs. {@code
 * java.util.TimerTask} needs no separate entry here: it {@code implements Runnable} itself, so a
 * {@code TimerTask} subclass's {@code run()} override is already caught by the same scan.
 */
public final class AndroidAsyncConstants {

  public static final String RUNNABLE_INTERFACE = "java.lang.Runnable";
  public static final String CALLABLE_INTERFACE = "java.util.concurrent.Callable";
  public static final String ASYNC_TASK_CLASS = "android.os.AsyncTask";

  private static final Map<String, List<LifecycleMethod>> INTERFACE_METHODS =
      buildInterfaceMethods();

  private static Map<String, List<LifecycleMethod>> buildInterfaceMethods() {
    Map<String, List<LifecycleMethod>> table = new LinkedHashMap<>();
    table.put(
        RUNNABLE_INTERFACE,
        Collections.singletonList(new LifecycleMethod("run", "void", Collections.emptyList())));
    table.put(
        CALLABLE_INTERFACE,
        Collections.singletonList(
            new LifecycleMethod("call", "java.lang.Object", Collections.emptyList())));
    return Collections.unmodifiableMap(table);
  }

  /**
   * {@code android.os.AsyncTask}'s callbacks, in their type-erased form (the actual bytecode
   * signature javac/dx generate for a generic {@code AsyncTask<Params, Progress, Result>} subclass,
   * via a compiler-generated bridge method when the subclass declares typed overrides such as
   * {@code doInBackground(Void...)}). Using the erased signature here is what makes {@link
   * AndroidEntryPointCreator#resolveOverride} find the bridge method — CHA/RTA then follows the
   * bridge's own outgoing call to the app's typed override normally, no special-casing needed
   * beyond getting this one entry point right.
   */
  private static final List<LifecycleMethod> ASYNC_TASK_METHODS =
      Collections.unmodifiableList(
          Arrays.asList(
              new LifecycleMethod("onPreExecute", "void", Collections.emptyList()),
              new LifecycleMethod(
                  "doInBackground",
                  "java.lang.Object",
                  Collections.singletonList("java.lang.Object[]")),
              new LifecycleMethod(
                  "onProgressUpdate", "void", Collections.singletonList("java.lang.Object[]")),
              new LifecycleMethod(
                  "onPostExecute", "void", Collections.singletonList("java.lang.Object")),
              new LifecycleMethod(
                  "onCancelled", "void", Collections.singletonList("java.lang.Object")),
              new LifecycleMethod("onCancelled", "void", Collections.emptyList())));

  private AndroidAsyncConstants() {}

  /** Interfaces whose implementations should be scanned for, and the methods to resolve. */
  @NonNull
  public static Map<String, List<LifecycleMethod>> getInterfaceMethods() {
    return INTERFACE_METHODS;
  }

  /** The callbacks to resolve on any app class extending {@link #ASYNC_TASK_CLASS}. */
  @NonNull
  public static List<LifecycleMethod> getAsyncTaskMethods() {
    return ASYNC_TASK_METHODS;
  }
}
