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

import qilin.core.PTAScene;
import qilin.parm.ctxcons.CtxConstructor;
import qilin.pta.toolkits.bean.BeanObjectSensitivity;
import qilin.pta.toolkits.dd.DataDrivenCallSiteSensitivity;
import qilin.pta.toolkits.dd.DataDrivenHybridObjectSensitivity;
import qilin.pta.toolkits.dd.DataDrivenObjectSensitivity;
import qilin.pta.toolkits.dd.TunnelingCallSiteSensitivity;
import qilin.pta.toolkits.dd.TunnelingHybridObjectSensitivity;
import qilin.pta.toolkits.dd.TunnelingObjectSensitivity;
import qilin.pta.toolkits.dd.TunnelingTypeSensitivity;
import qilin.pta.toolkits.eagle.EagleObjectSensitivity;
import qilin.pta.toolkits.mahjong.MahjongCallSiteSensitivity;
import qilin.pta.toolkits.mahjong.MahjongObjectSensitivity;
import qilin.pta.toolkits.selectx.SelectxCallSiteSensitivity;
import qilin.pta.toolkits.turner.TurnerObjectSensitivity;
import qilin.pta.toolkits.zipper.ZipperCallSiteSensitivity;
import qilin.pta.toolkits.zipper.ZipperObjectSensitivity;
import qilin.pta.tools.BasePTA;
import qilin.pta.tools.DebloatedPTA;

/**
 * Type-safe, IDE-assisted description of a pointer-analysis context-sensitivity variant. Each
 * factory method validates its arguments at construction time instead of relying on regex parsing
 * plus a hand-written compatibility matrix, and {@link #createPTA} builds the concrete {@link
 * BasePTA} for the variant (context-debloating, where applicable, is applied per-variant here too)
 * - this is the single place a {@link qilin.core.PTA} gets constructed from, covering both the core
 * context-sensitivity variants (implemented alongside this class) and the research-toolkit ones
 * (bean, zipper, eagle, turner, mahjong, selectx, data-driven, tunneling - each implemented in its
 * own toolkit package, next to the {@code *PTA} class it builds). Not {@code sealed}: the toolkit
 * implementations live in different packages, and this project has no {@code module-info.java}, so
 * cross-package {@code permits} isn't available - the only publicly-constructible variants are
 * still exactly the ones exposed by the factory methods below.
 */
public abstract class ContextSensitivity {

  protected ContextSensitivity() {}

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

  /** Builds the concrete {@link BasePTA} for this variant. */
  public abstract BasePTA createPTA(PTAScene scene, PointerAnalysisConfig config);

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

  /** BEAN-guided 2-object-sensitivity. Only k=2/hk=1 is supported by {@code BeanPTA}. */
  public static ContextSensitivity beanObjectSensitive() {
    return new BeanObjectSensitivity();
  }

  /** ZIPPER-guided k-object-sensitivity. */
  public static ContextSensitivity zipperObjectSensitive(int k, int hk) {
    return new ZipperObjectSensitivity(k, hk, false);
  }

  /** ZIPPER-guided k-callsite-sensitivity. */
  public static ContextSensitivity zipperCallSite(int k, int hk) {
    return new ZipperCallSiteSensitivity(k, hk, false);
  }

  /**
   * Zipper-e (express): ZIPPER-guided k-object-sensitivity with the express-mode threshold cutoff.
   */
  public static ContextSensitivity zipperExpressObjectSensitive(int k, int hk) {
    return new ZipperObjectSensitivity(k, hk, true);
  }

  /**
   * Zipper-e (express): ZIPPER-guided k-callsite-sensitivity with the express-mode threshold
   * cutoff.
   */
  public static ContextSensitivity zipperExpressCallSite(int k, int hk) {
    return new ZipperCallSiteSensitivity(k, hk, true);
  }

  /** EAGLE-guided k-object-sensitivity. Heap-context depth is always k-1. */
  public static ContextSensitivity eagleObjectSensitive(int k) {
    return new EagleObjectSensitivity(k);
  }

  /** TURNER-guided k-object-sensitivity. */
  public static ContextSensitivity turnerObjectSensitive(int k) {
    return new TurnerObjectSensitivity(k);
  }

  /** MAHJONG-guided k-object-sensitivity. */
  public static ContextSensitivity mahjongObjectSensitive(int k, int hk) {
    return new MahjongObjectSensitivity(k, hk);
  }

  /** MAHJONG-guided k-callsite-sensitivity. */
  public static ContextSensitivity mahjongCallSite(int k, int hk) {
    return new MahjongCallSiteSensitivity(k, hk);
  }

  /** Data-driven 2-object-sensitivity. Only k=2/hk=1 is supported by {@code DataDrivenPTA}. */
  public static ContextSensitivity dataDrivenObjectSensitive() {
    return new DataDrivenObjectSensitivity();
  }

  /** Data-driven 2-callsite-sensitivity. Only k=2/hk=1 is supported by {@code DataDrivenPTA}. */
  public static ContextSensitivity dataDrivenCallSite() {
    return new DataDrivenCallSiteSensitivity();
  }

  /**
   * Data-driven hybrid-2-object-sensitivity. Only k=2/hk=1 is supported by {@code DataDrivenPTA}.
   */
  public static ContextSensitivity dataDrivenHybridObjectSensitive() {
    return new DataDrivenHybridObjectSensitivity();
  }

  /** Tunneling k-object-sensitivity. */
  public static ContextSensitivity tunnelingObjectSensitive(int k, int hk) {
    return new TunnelingObjectSensitivity(k, hk);
  }

  /** Tunneling k-callsite-sensitivity. */
  public static ContextSensitivity tunnelingCallSite(int k, int hk) {
    return new TunnelingCallSiteSensitivity(k, hk);
  }

  /** Tunneling k-type-sensitivity. */
  public static ContextSensitivity tunnelingTypeSensitive(int k, int hk) {
    return new TunnelingTypeSensitivity(k, hk);
  }

  /** Tunneling hybrid-k-object-sensitivity. */
  public static ContextSensitivity tunnelingHybridObjectSensitive(int k, int hk) {
    return new TunnelingHybridObjectSensitivity(k, hk);
  }

  /** SELECTX-guided k-callsite-sensitivity. */
  public static ContextSensitivity selectxCallSite(int k) {
    return new SelectxCallSiteSensitivity(k);
  }

  protected static void requireNonNegative(int v, String name) {
    if (v < 0) {
      throw new IllegalArgumentException(name + " must be >= 0, was " + v);
    }
  }

  protected static void requirePositive(int v) {
    if (v < 1) {
      throw new IllegalArgumentException("k" + " must be >= 1, was " + v);
    }
  }

  /** Shared by every object/type-sensitive variant: hk must be k or k-1. */
  protected static void requireObjectOrTypeHeapRange(int k, int hk) {
    if (hk > k || hk < k - 1) {
      throw new IllegalArgumentException(
          "heap context depth must be k or k-1 for object/type-sensitivity (k="
              + k
              + ", hk="
              + hk
              + ")");
    }
  }

  protected static String label(String approach, int k, String ctxSuffix, int hk) {
    return approach + "-" + k + ctxSuffix + "+" + hk + "heap";
  }

  protected static BasePTA maybeDebloat(BasePTA pta, PointerAnalysisConfig config) {
    return config.isCtxDebloating() ? new DebloatedPTA(pta, config.getDebloatApproach()) : pta;
  }
}
