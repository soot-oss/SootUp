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

import sootup.callgraph.scope.VirtualCallResolver;

/**
 * Immutable, type-safe replacement for the old {@code CoreConfig}/{@code PTAConfig} singletons. An
 * instance is threaded explicitly through {@code PTAScene} (and from there reachable via {@code
 * PTA#getConfig()}) so that multiple independent analysis runs - each with their own config - can
 * coexist in the same JVM.
 */
public final class PointerAnalysisConfig {

  public enum HeapAbstractionPolicy {
    ALLOC_SITE,
    HEURISTIC_MERGE
  }

  public enum DebloatApproach {
    CONCH,
    DEBLOATERX,
    COLLECTION,
    MOON
  }

  public enum TurnerConfig {
    DEFAULT,
    PHASE_ONE,
    PHASE_TWO
  }

  private final ContextSensitivity contextSensitivity;
  private final HeapAbstractionPolicy heapAbstractionPolicy;
  private final boolean singleEntry;
  private final boolean seedEntryPointClinits;
  private final VirtualCallResolver clinitVirtualCallResolver;
  private final boolean preciseArrayElement;
  private final boolean stringConstants;
  private final boolean preciseExceptions;
  private final boolean enforceEmptyContextForIgnoreTypes;
  private final String reflectionLogPath;
  private final boolean resolveDynamicInvoke;
  private final boolean preAnalysisOnly;
  private final boolean ctxDebloating;
  private final DebloatApproach debloatApproach;
  private final TurnerConfig turnerConfig;
  private final String analysisName;
  private final String outputDirectory;
  private final boolean dumpJimple;
  private final boolean dumpPointsToSet;
  private final boolean dumpLibraryPointsToSet;
  private final boolean dumpStats;

  private PointerAnalysisConfig(Builder b) {
    this.contextSensitivity = b.contextSensitivity;
    this.heapAbstractionPolicy = b.heapAbstractionPolicy;
    this.singleEntry = b.singleEntry;
    this.seedEntryPointClinits = b.seedEntryPointClinits;
    this.clinitVirtualCallResolver = b.clinitVirtualCallResolver;
    this.preciseArrayElement = b.preciseArrayElement;
    this.stringConstants = b.stringConstants;
    this.preciseExceptions = b.preciseExceptions;
    this.enforceEmptyContextForIgnoreTypes = b.enforceEmptyContextForIgnoreTypes;
    this.reflectionLogPath = b.reflectionLogPath;
    this.resolveDynamicInvoke = b.resolveDynamicInvoke;
    this.preAnalysisOnly = b.preAnalysisOnly;
    this.ctxDebloating = b.ctxDebloating;
    this.debloatApproach = b.debloatApproach;
    this.turnerConfig = b.turnerConfig;
    this.analysisName = b.analysisName != null ? b.analysisName : b.contextSensitivity.toString();
    this.outputDirectory = b.outputDirectory;
    this.dumpJimple = b.dumpJimple;
    this.dumpPointsToSet = b.dumpPointsToSet;
    this.dumpLibraryPointsToSet = b.dumpLibraryPointsToSet;
    this.dumpStats = b.dumpStats;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ContextSensitivity getContextSensitivity() {
    return contextSensitivity;
  }

  public HeapAbstractionPolicy getHeapAbstractionPolicy() {
    return heapAbstractionPolicy;
  }

  public boolean isSingleEntry() {
    return singleEntry;
  }

  /**
   * Whether each entry point's declaring-class {@code <clinit>} is eagerly seeded as a root before
   * traversal starts, independently of whether the fake-main body actually triggers it. See {@link
   * sootup.callgraph.AbstractCallGraphAlgorithm#AbstractCallGraphAlgorithm(sootup.core.views.View,
   * sootup.callgraph.scope.CallResolver, VirtualCallResolver, boolean)} for the four classic
   * static-initializer handling modes this, combined with {@link #getClinitVirtualCallResolver()},
   * reproduces: {@code FULL} = {@code true} + {@code new SuppressClinitCallResolver(view)}; {@code
   * ON_THE_FLY} (default) = {@code false} + {@link VirtualCallResolver#all()}; {@code APP} = {@code
   * false} + {@code new AppOnlyClinitCallResolver(view)}; {@code NONE} = {@code false} + {@code new
   * SuppressClinitCallResolver(view)}.
   */
  public boolean isSeedEntryPointClinits() {
    return seedEntryPointClinits;
  }

  /**
   * Governs admission of {@code <clinit>} calls discovered on the fly as the points-to analysis
   * grows (see {@link #isSeedEntryPointClinits()} for how the two combine into the four classic
   * modes). Qilin does not track the statement that triggered a given {@code <clinit>}, so
   * resolvers consulted here receive a synthetic invoke statement standing in for it - resolvers
   * that only inspect the callee (as {@code SuppressClinitCallResolver}/{@code
   * AppOnlyClinitCallResolver} do) are unaffected by this.
   */
  public VirtualCallResolver getClinitVirtualCallResolver() {
    return clinitVirtualCallResolver;
  }

  public boolean isPreciseArrayElement() {
    return preciseArrayElement;
  }

  public boolean isStringConstants() {
    return stringConstants;
  }

  public boolean isPreciseExceptions() {
    return preciseExceptions;
  }

  public boolean isEnforceEmptyContextForIgnoreTypes() {
    return enforceEmptyContextForIgnoreTypes;
  }

  /** Path to a Tamiflex reflection log, or {@code null} if reflection resolution is disabled. */
  public String getReflectionLogPath() {
    return reflectionLogPath;
  }

  /**
   * Whether to resolve invokedynamic call sites bootstrapped by {@code LambdaMetafactory} (lambdas
   * and method references) to their target method. Unlike reflection resolution this needs no
   * external log - the target is a constant in the bootstrap args - so it defaults to enabled.
   */
  public boolean isResolveDynamicInvoke() {
    return resolveDynamicInvoke;
  }

  public boolean isPreAnalysisOnly() {
    return preAnalysisOnly;
  }

  public boolean isContextDebloating() {
    return ctxDebloating;
  }

  public DebloatApproach getDebloatApproach() {
    return debloatApproach;
  }

  public TurnerConfig getTurnerConfig() {
    return turnerConfig;
  }

  /** Human-readable analysis name used for report/output directory naming. */
  public String getAnalysisName() {
    return analysisName;
  }

  public String getOutputDirectory() {
    return outputDirectory;
  }

  public boolean isDumpJimple() {
    return dumpJimple;
  }

  public boolean isDumpPointsToSet() {
    return dumpPointsToSet;
  }

  public boolean isDumpLibraryPointsToSet() {
    return dumpLibraryPointsToSet;
  }

  public boolean isDumpStats() {
    return dumpStats;
  }

  public static final class Builder {
    private ContextSensitivity contextSensitivity = ContextSensitivity.insensitive();
    private HeapAbstractionPolicy heapAbstractionPolicy = HeapAbstractionPolicy.ALLOC_SITE;
    private boolean singleEntry = false;
    private boolean seedEntryPointClinits = false;
    private VirtualCallResolver clinitVirtualCallResolver = VirtualCallResolver.all();
    private boolean preciseArrayElement = false;
    private boolean stringConstants = false;
    private boolean preciseExceptions = false;
    private boolean enforceEmptyContextForIgnoreTypes = false;
    private String reflectionLogPath = null;
    private boolean resolveDynamicInvoke = true;
    private boolean preAnalysisOnly = false;
    private boolean ctxDebloating = false;
    private DebloatApproach debloatApproach = DebloatApproach.CONCH;
    private TurnerConfig turnerConfig = TurnerConfig.DEFAULT;
    private String analysisName = null;
    private String outputDirectory = "";
    private boolean dumpJimple = false;
    private boolean dumpPointsToSet = false;
    private boolean dumpLibraryPointsToSet = false;
    private boolean dumpStats = false;

    private Builder() {}

    public Builder contextSensitivity(ContextSensitivity contextSensitivity) {
      this.contextSensitivity = contextSensitivity;
      return this;
    }

    public Builder heapAbstractionPolicy(HeapAbstractionPolicy heapAbstractionPolicy) {
      this.heapAbstractionPolicy = heapAbstractionPolicy;
      return this;
    }

    public Builder singleEntry(boolean singleEntry) {
      this.singleEntry = singleEntry;
      return this;
    }

    public Builder seedEntryPointClinits(boolean seedEntryPointClinits) {
      this.seedEntryPointClinits = seedEntryPointClinits;
      return this;
    }

    public Builder clinitVirtualCallResolver(VirtualCallResolver clinitVirtualCallResolver) {
      this.clinitVirtualCallResolver = clinitVirtualCallResolver;
      return this;
    }

    public Builder preciseArrayElement(boolean preciseArrayElement) {
      this.preciseArrayElement = preciseArrayElement;
      return this;
    }

    public Builder stringConstants(boolean stringConstants) {
      this.stringConstants = stringConstants;
      return this;
    }

    public Builder preciseExceptions(boolean preciseExceptions) {
      this.preciseExceptions = preciseExceptions;
      return this;
    }

    public Builder enforceEmptyContextForIgnoreTypes(boolean enforceEmptyContextForIgnoreTypes) {
      this.enforceEmptyContextForIgnoreTypes = enforceEmptyContextForIgnoreTypes;
      return this;
    }

    public Builder reflectionLogPath(String reflectionLogPath) {
      this.reflectionLogPath = reflectionLogPath;
      return this;
    }

    public Builder resolveDynamicInvoke(boolean resolveDynamicInvoke) {
      this.resolveDynamicInvoke = resolveDynamicInvoke;
      return this;
    }

    public Builder preAnalysisOnly(boolean preAnalysisOnly) {
      this.preAnalysisOnly = preAnalysisOnly;
      return this;
    }

    public Builder ctxDebloating(boolean ctxDebloating) {
      this.ctxDebloating = ctxDebloating;
      return this;
    }

    public Builder debloatApproach(DebloatApproach debloatApproach) {
      this.debloatApproach = debloatApproach;
      return this;
    }

    public Builder turnerConfig(TurnerConfig turnerConfig) {
      this.turnerConfig = turnerConfig;
      return this;
    }

    public Builder analysisName(String analysisName) {
      this.analysisName = analysisName;
      return this;
    }

    public Builder outputDirectory(String outputDirectory) {
      this.outputDirectory = outputDirectory;
      return this;
    }

    public Builder dumpJimple(boolean dumpJimple) {
      this.dumpJimple = dumpJimple;
      return this;
    }

    public Builder dumpPointsToSet(boolean dumpPointsToSet) {
      this.dumpPointsToSet = dumpPointsToSet;
      return this;
    }

    public Builder dumpLibraryPointsToSet(boolean dumpLibraryPointsToSet) {
      this.dumpLibraryPointsToSet = dumpLibraryPointsToSet;
      return this;
    }

    public Builder dumpStats(boolean dumpStats) {
      this.dumpStats = dumpStats;
      return this;
    }

    public PointerAnalysisConfig build() {
      return new PointerAnalysisConfig(this);
    }
  }
}
