/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.config;

import qilin.parm.ctxcons.CallsiteCtxConstructor;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.parm.ctxcons.HybObjCtxConstructor;
import qilin.parm.ctxcons.HybTypeCtxConstructor;
import qilin.parm.ctxcons.InsensCtxConstructor;
import qilin.parm.ctxcons.ObjCtxConstructor;
import qilin.parm.ctxcons.TypeCtxConstructor;

/**
 * Type-safe, IDE-assisted replacement for the old {@code PTAPattern} string DSL (e.g. {@code
 * "2o1h"}, {@code "D-2o"}). Each factory method validates its arguments at construction time
 * instead of relying on regex parsing plus a hand-written compatibility matrix.
 */
public abstract class ContextSensitivity {

  private ContextSensitivity() {}

  /** Builds the {@link CtxConstructor} implementing this context-sensitivity variant. */
  public abstract CtxConstructor createCtxConstructor();

  /** The method-context depth (k). */
  public abstract int contextDepth();

  /** The heap-context depth (hk). */
  public abstract int heapContextDepth();

  /**
   * The context depth passed to the {@code UniformSelector}. Equal to {@link #contextDepth()} for
   * every variant except hybrid-object-sensitivity, which selects on k+1.
   */
  public int selectorContextDepth() {
    return contextDepth();
  }

  public static ContextSensitivity insensitive() {
    return Insensitive.INSTANCE;
  }

  public static ContextSensitivity callSite(int k) {
    return callSite(k, k - 1);
  }

  public static ContextSensitivity callSite(int k, int hk) {
    return new CallSite(k, hk);
  }

  public static ContextSensitivity objectSensitive(int k) {
    return objectSensitive(k, k);
  }

  public static ContextSensitivity objectSensitive(int k, int hk) {
    return new ObjectSens(k, hk);
  }

  public static ContextSensitivity typeSensitive(int k, int hk) {
    return new TypeSens(k, hk);
  }

  public static ContextSensitivity hybridObjectSensitive(int k, int hk) {
    return new HybridObjectSens(k, hk);
  }

  public static ContextSensitivity hybridTypeSensitive(int k, int hk) {
    return new HybridTypeSens(k, hk);
  }

  private static void requireNonNegative(int v, String name) {
    if (v < 0) {
      throw new IllegalArgumentException(name + " must be >= 0, was " + v);
    }
  }

  private static final class Insensitive extends ContextSensitivity {
    static final Insensitive INSTANCE = new Insensitive();

    @Override
    public CtxConstructor createCtxConstructor() {
      return new InsensCtxConstructor();
    }

    @Override
    public int contextDepth() {
      return 0;
    }

    @Override
    public int heapContextDepth() {
      return 0;
    }

    @Override
    public String toString() {
      return "insensitive";
    }
  }

  private static final class CallSite extends ContextSensitivity {
    private final int k;
    private final int hk;

    CallSite(int k, int hk) {
      requireNonNegative(k, "k");
      requireNonNegative(hk, "hk");
      this.k = k;
      this.hk = hk;
    }

    @Override
    public CtxConstructor createCtxConstructor() {
      return new CallsiteCtxConstructor();
    }

    @Override
    public int contextDepth() {
      return k;
    }

    @Override
    public int heapContextDepth() {
      return hk;
    }

    @Override
    public String toString() {
      return k + "c+" + hk + "heap";
    }
  }

  private static final class ObjectSens extends ContextSensitivity {
    private final int k;
    private final int hk;

    ObjectSens(int k, int hk) {
      requireNonNegative(k, "k");
      requireNonNegative(hk, "hk");
      if (hk > k || hk < k - 1) {
        throw new IllegalArgumentException(
            "heap context depth must be k or k-1 for object-sensitivity (k=" + k + ", hk=" + hk + ")");
      }
      this.k = k;
      this.hk = hk;
    }

    @Override
    public CtxConstructor createCtxConstructor() {
      return new ObjCtxConstructor();
    }

    @Override
    public int contextDepth() {
      return k;
    }

    @Override
    public int heapContextDepth() {
      return hk;
    }

    @Override
    public String toString() {
      return k + "o+" + hk + "heap";
    }
  }

  private static final class TypeSens extends ContextSensitivity {
    private final int k;
    private final int hk;

    TypeSens(int k, int hk) {
      requireNonNegative(k, "k");
      requireNonNegative(hk, "hk");
      if (hk > k || hk < k - 1) {
        throw new IllegalArgumentException(
            "heap context depth must be k or k-1 for type-sensitivity (k=" + k + ", hk=" + hk + ")");
      }
      this.k = k;
      this.hk = hk;
    }

    @Override
    public CtxConstructor createCtxConstructor() {
      return new TypeCtxConstructor();
    }

    @Override
    public int contextDepth() {
      return k;
    }

    @Override
    public int heapContextDepth() {
      return hk;
    }

    @Override
    public String toString() {
      return k + "t+" + hk + "heap";
    }
  }

  private static final class HybridObjectSens extends ContextSensitivity {
    private final int k;
    private final int hk;

    HybridObjectSens(int k, int hk) {
      requireNonNegative(k, "k");
      requireNonNegative(hk, "hk");
      this.k = k;
      this.hk = hk;
    }

    @Override
    public CtxConstructor createCtxConstructor() {
      return new HybObjCtxConstructor();
    }

    @Override
    public int contextDepth() {
      return k;
    }

    @Override
    public int heapContextDepth() {
      return hk;
    }

    @Override
    public int selectorContextDepth() {
      // matches the legacy HybridObjectSensPTA, which selects with UniformSelector(k + 1, hk).
      return k + 1;
    }

    @Override
    public String toString() {
      return k + "hybobj+" + hk + "heap";
    }
  }

  private static final class HybridTypeSens extends ContextSensitivity {
    private final int k;
    private final int hk;

    HybridTypeSens(int k, int hk) {
      requireNonNegative(k, "k");
      requireNonNegative(hk, "hk");
      this.k = k;
      this.hk = hk;
    }

    @Override
    public CtxConstructor createCtxConstructor() {
      return new HybTypeCtxConstructor();
    }

    @Override
    public int contextDepth() {
      return k;
    }

    @Override
    public int heapContextDepth() {
      return hk;
    }

    @Override
    public String toString() {
      return k + "hybtype+" + hk + "heap";
    }
  }
}
