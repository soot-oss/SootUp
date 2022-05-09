package de.upb.swt.soot.callgraph.spark.pag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

public enum CallGraphEdgeType {
  INVALID("INVALID"),
  /** Due to explicit invokestatic instruction. */
  STATIC("STATIC"),
  /** Due to explicit invokevirtual instruction. */
  VIRTUAL("VIRTUAL"),
  /** Due to explicit invokeinterface instruction. */
  INTERFACE("INTERFACE"),
  /** Due to explicit invokespecial instruction. */
  SPECIAL("SPECIAL"),
  /** Due to explicit invokedynamic instruction. */
  DYNAMIC("DYNAMIC"),
  /** Implicit call to static initializer. */
  CLINIT("CLINIT"),
  /** Fake edges from our generic callback model. */
  GENERIC_FAKE("GENERIC_FAKE"),
  /** Implicit call to Thread.run() due to Thread.start() call. */
  THREAD("THREAD"),
  /** Implicit call to java.lang.Runnable.run() due to Executor.execute() call. */
  EXECUTOR("EXECUTOR"),
  /** Implicit call to AsyncTask.doInBackground() due to AsyncTask.execute() call. */
  ASYNCTASK("ASYNCTASK"),
  /** Implicit call to java.lang.ref.Finalizer.register from new bytecode. */
  FINALIZE("FINALIZE"),
  /**
   * Implicit call to Handler.handleMessage(android.os.Message) due to Handler.sendxxxxMessagexxxx()
   * call.
   */
  HANDLER("HANDLER"),
  /** Implicit call to finalize() from java.lang.ref.Finalizer.invokeFinalizeMethod(). */
  INVOKE_FINALIZE("INVOKE_FINALIZE"),
  /** Implicit call to run() through AccessController.doPrivileged(). */
  PRIVILEGED("PRIVILEGED"),
  /** Implicit call to constructor from java.lang.Class.newInstance(). */
  NEWINSTANCE("NEWINSTANCE"),
  /** Due to call to Method.invoke(..). */
  REFL_INVOKE("REFL_INVOKE"),
  /** Due to call to Constructor.newInstance(..). */
  REFL_CONSTR_NEWINSTANCE("REFL_CONSTR_NEWINSTANCE"),
  /** Due to call to Class.newInstance(..) when reflection log is enabled. */
  REFL_CLASS_NEWINSTANCE("REFL_CLASS_NEWINSTANCE");

  private String name;

  CallGraphEdgeType(String name) {
    this.name = name;
  }

  String getName() {
    return name;
  }

  public boolean passesParameters() {
    return isExplicit()
        || this == THREAD
        || this == EXECUTOR
        || this == ASYNCTASK
        || this == FINALIZE
        || this == PRIVILEGED
        || this == NEWINSTANCE
        || this == INVOKE_FINALIZE
        || this == REFL_INVOKE
        || this == REFL_CONSTR_NEWINSTANCE
        || this == REFL_CLASS_NEWINSTANCE;
  }

  public boolean isFake() {
    return this == THREAD
        || this == EXECUTOR
        || this == ASYNCTASK
        || this == PRIVILEGED
        || this == HANDLER
        || this == GENERIC_FAKE;
  }

  /** Returns true if the call is due to an explicit invoke statement. */
  public boolean isExplicit() {
    return isInstance() || isStatic();
  }

  /** Returns true if the call is due to an explicit instance invoke statement. */
  public boolean isInstance() {
    return this == VIRTUAL || this == INTERFACE || this == SPECIAL;
  }

  /** Returns true if the call is due to an explicit virtual invoke statement. */
  public boolean isVirtual() {
    return this == VIRTUAL;
  }

  public boolean isSpecial() {
    return this == SPECIAL;
  }

  /** Returns true if the call is to static initializer. */
  public boolean isClinit() {
    return this == CLINIT;
  }

  /** Returns true if the call is due to an explicit static invoke statement. */
  public boolean isStatic() {
    return this == STATIC;
  }

  /** Returns true if the call is due to an explicit dynamic invoke statement. */
  public boolean isDynamic() {
    return this == DYNAMIC;
  }

  public boolean isThread() {
    return this == THREAD;
  }

  public boolean isExecutor() {
    return this == EXECUTOR;
  }

  public boolean isAsyncTask() {
    return this == ASYNCTASK;
  }

  public boolean isPrivileged() {
    return this == PRIVILEGED;
  }

  public boolean isReflection() {
    return this == REFL_CLASS_NEWINSTANCE || this == REFL_CONSTR_NEWINSTANCE || this == REFL_INVOKE;
  }

  public boolean isReflInvoke() {
    return this == REFL_INVOKE;
  }
}
