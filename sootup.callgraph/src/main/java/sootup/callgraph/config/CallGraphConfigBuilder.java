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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import sootup.callgraph.scope.CallResolver;
import sootup.callgraph.scope.DefaultCallResolver;
import sootup.callgraph.scope.VirtualCallResolver;
import sootup.core.signatures.MethodSignature;
import sootup.core.views.View;

/**
 * Stage 1 of the unified {@link sootup.callgraph.CallGraphConfig} builder chain: collects the
 * properties shared across every call-graph algorithm family, then transitions to a family-specific
 * stage. Obtained via {@link sootup.callgraph.CallGraphConfig#builder()}.
 *
 * <p>{@code view} and {@code entryPoints} are required (validated once a family stage is entered,
 * since {@code view} is needed to construct the default {@link CallResolver} and family-specific
 * builders validate their own required fields at {@code build()} time). Every other property
 * defaults to the same values {@code AbstractCallGraphAlgorithm}'s single-{@code View} constructor
 * uses, so a caller who sets nothing beyond {@code view}/{@code entryPoints} gets today's default
 * behavior for whichever family they pick.
 */
public final class CallGraphConfigBuilder {

  private View view;
  private List<MethodSignature> entryPoints = Collections.emptyList();
  private CallResolver callResolver;
  private VirtualCallResolver virtualCallResolver = VirtualCallResolver.all();

  /**
   * {@code null} means "not explicitly set" - each family stage falls back to its own natural
   * default rather than a common one, since CHA/RTA/Spark default to {@code true} (matching {@code
   * AbstractCallGraphAlgorithm}'s historical behavior) while Qilin defaults to {@code false}
   * (matching {@code PointerAnalysisConfig}'s historical {@code ON_THE_FLY} default) - forcing one
   * family's default onto another changes its behavior silently.
   */
  private Boolean seedEntryPointClinits;

  public CallGraphConfigBuilder() {}

  @NonNull
  public CallGraphConfigBuilder view(@NonNull View view) {
    this.view = view;
    return this;
  }

  @NonNull
  public CallGraphConfigBuilder entryPoints(@NonNull List<MethodSignature> entryPoints) {
    this.entryPoints = entryPoints;
    return this;
  }

  /** Controls which statements' calls are resolved and expanded at all (pre-dispatch). */
  @NonNull
  public CallGraphConfigBuilder callResolver(@NonNull CallResolver callResolver) {
    this.callResolver = callResolver;
    return this;
  }

  /**
   * Controls which resolved dynamic-dispatch candidates - including discovered {@code <clinit>}
   * calls - are admitted/expanded (post-dispatch). See {@code SuppressClinitCallResolver}/{@code
   * AppOnlyClinitCallResolver} for ready-made {@code <clinit>}-scoping resolvers.
   */
  @NonNull
  public CallGraphConfigBuilder virtualCallResolver(
      @NonNull VirtualCallResolver virtualCallResolver) {
    this.virtualCallResolver = virtualCallResolver;
    return this;
  }

  /**
   * Whether each entry point's declaring-class {@code <clinit>} is eagerly seeded as a root before
   * traversal starts. Leave unset to get whichever family's own natural default applies (see the
   * field javadoc above for why there's no single common default).
   */
  @NonNull
  public CallGraphConfigBuilder seedEntryPointClinits(boolean seedEntryPointClinits) {
    this.seedEntryPointClinits = seedEntryPointClinits;
    return this;
  }

  @NonNull
  private CommonCallGraphSettings snapshot() {
    if (view == null) {
      throw new IllegalStateException("view is required");
    }
    CallResolver resolvedCallResolver =
        callResolver != null ? callResolver : new DefaultCallResolver(view);
    return new CommonCallGraphSettings(
        view, entryPoints, resolvedCallResolver, virtualCallResolver, seedEntryPointClinits);
  }

  /** Transitions to the CHA-specific stage. */
  public ChaCallGraphConfig.@NonNull Builder cha() {
    return new ChaCallGraphConfig.Builder(snapshot());
  }

  /** Transitions to the RTA-specific stage. */
  public RtaCallGraphConfig.@NonNull Builder rta() {
    return new RtaCallGraphConfig.Builder(snapshot());
  }

  /**
   * Transitions to a family-specific stage that {@code sootup.callgraph} cannot depend on directly
   * (Spark, Qilin - each in its own downstream module). Pass the family's static {@code from}
   * factory method as a reference, e.g.:
   *
   * <pre>
   *   CallGraphConfig.builder()....into(SparkCallGraphConfig::from). ... .build();
   *   CallGraphConfig.builder()....into(QilinCallGraphConfig::from). ... .build();
   * </pre>
   */
  @NonNull
  public <B> B into(@NonNull Function<CommonCallGraphSettings, B> familyBuilderFactory) {
    return familyBuilderFactory.apply(snapshot());
  }
}
