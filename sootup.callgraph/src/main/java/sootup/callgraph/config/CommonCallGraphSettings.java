package sootup.callgraph.config;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Immutable snapshot of the properties {@link CallGraphConfigBuilder} collects before a caller
 * transitions to a family-specific stage (see {@link CallGraphConfigBuilder#cha()}, {@link
 * CallGraphConfigBuilder#rta()}, {@link CallGraphConfigBuilder#into}). Public (unlike the builder's
 * internal state) because family-specific builders living in other modules (e.g. {@code
 * sootup.spark.SparkCallGraphConfig}, {@code qilin.callgraph.QilinCallGraphConfig}) need to read
 * it.
 */
public final class CommonCallGraphSettings {

  @NonNull private final View view;
  @NonNull private final List<MethodSignature> entryPoints;
  @NonNull private final CallResolver callResolver;
  @NonNull private final VirtualCallResolver virtualCallResolver;
  @Nullable private final Boolean seedEntryPointClinits;

  CommonCallGraphSettings(
      @NonNull View view,
      @NonNull List<MethodSignature> entryPoints,
      @NonNull CallResolver callResolver,
      @NonNull VirtualCallResolver virtualCallResolver,
      @Nullable Boolean seedEntryPointClinits) {
    this.view = view;
    this.entryPoints = entryPoints;
    this.callResolver = callResolver;
    this.virtualCallResolver = virtualCallResolver;
    this.seedEntryPointClinits = seedEntryPointClinits;
  }

  @NonNull
  public View getView() {
    return view;
  }

  @NonNull
  public List<MethodSignature> getEntryPoints() {
    return entryPoints;
  }

  /** Controls which statements' calls are resolved and expanded at all (pre-dispatch). */
  @NonNull
  public CallResolver getCallResolver() {
    return callResolver;
  }

  /**
   * Controls which resolved dynamic-dispatch candidates - including discovered {@code <clinit>}
   * calls - are admitted/expanded (post-dispatch).
   */
  @NonNull
  public VirtualCallResolver getVirtualCallResolver() {
    return virtualCallResolver;
  }

  /**
   * Whether each entry point's declaring-class {@code <clinit>} is eagerly seeded as a root before
   * traversal starts, or {@code null} if the caller never explicitly set it - each family stage
   * should fall back to its own natural default in that case (see {@code
   * CallGraphConfigBuilder#seedEntryPointClinits} for why there's no single common default).
   */
  @NonNull
  public Boolean getSeedEntryPointClinits(boolean defaultValue) {
    return seedEntryPointClinits != null ? seedEntryPointClinits : defaultValue;
  }
}
