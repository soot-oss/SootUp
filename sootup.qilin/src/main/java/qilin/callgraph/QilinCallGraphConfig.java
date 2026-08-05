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

package qilin.callgraph;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import qilin.core.PTA;
import qilin.core.PointerAnalysisFactory;
import qilin.core.config.PointerAnalysisConfig;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraphConfig;
import sootup.callgraph.config.CommonCallGraphSettings;
import sootup.core.types.ClassType;

/**
 * Qilin-specific stage of the unified {@link CallGraphConfig} builder chain. Obtained via {@code
 * CallGraphConfigBuilder.into(QilinCallGraphConfig::from)}, since {@code sootup.callgraph} cannot
 * depend on {@code sootup.qilin} directly:
 *
 * <pre>
 *   CallGraph cg = CallGraphConfig.builder()
 *       .view(view).entryPoints(entryPoints)
 *       .into(QilinCallGraphConfig::from)
 *       .mainClass(mainClassType)
 *       .pointerAnalysisConfig(PointerAnalysisConfig.builder().contextSensitivity(ContextSensitivity.callSite(2)))
 *       .build()
 *       .computeCallGraph();
 * </pre>
 *
 * <p>Requires an explicit {@link Builder#mainClass}: {@code PTAScene} hard-requires exactly one main
 * class at construction time (a real architectural constraint, not just a config-surface choice), so
 * the common stage's {@code entryPoints} list - which may name several methods - is not used to
 * derive it. It still feeds {@link PointerAnalysisConfig#isSeedEntryPointClinits() the seed flag}/{@link
 * PointerAnalysisConfig#getClinitVirtualCallResolver()} via {@link CommonCallGraphSettings}, which
 * this stage overrides onto whatever {@link PointerAnalysisConfig.Builder} the caller supplies (or a
 * fresh default one).
 *
 * <p>The common stage's {@code CallResolver} (pre-dispatch) is <b>not</b> consulted: qilin's own
 * dispatch resolution ({@code qilin.core.VirtualCalls}) doesn't integrate with {@code
 * sootup.callgraph.scope.CallResolver}.
 */
public final class QilinCallGraphConfig implements CallGraphConfig {

  @NonNull private final CommonCallGraphSettings common;
  @NonNull private final ClassType mainClass;
  private final PointerAnalysisConfig.@NonNull Builder configBuilder;

  private QilinCallGraphConfig(
      @NonNull CommonCallGraphSettings common,
      @NonNull ClassType mainClass,
      PointerAnalysisConfig.@NonNull Builder configBuilder) {
    this.common = common;
    this.mainClass = mainClass;
    this.configBuilder = configBuilder;
  }

  /** Entry point for {@code CallGraphConfigBuilder.into(QilinCallGraphConfig::from)}. */
  @NonNull
  public static Builder from(@NonNull CommonCallGraphSettings common) {
    return new Builder(common);
  }

  @NonNull
  @Override
  public CallGraph computeCallGraph() {
    PointerAnalysisConfig config =
        configBuilder
            .seedEntryPointClinits(common.getSeedEntryPointClinits(false))
            .clinitVirtualCallResolver(common.getVirtualCallResolver())
            .build();
    PTA pta = PointerAnalysisFactory.create(common.getView(), mainClass, config);
    pta.run();
    return pta.getCallGraph();
  }

  public static final class Builder {

    @NonNull private final CommonCallGraphSettings common;
    @Nullable private ClassType mainClass;
    private PointerAnalysisConfig.@NonNull Builder configBuilder = PointerAnalysisConfig.builder();

    Builder(@NonNull CommonCallGraphSettings common) {
      this.common = common;
    }

    /** Required: qilin's single-entry-point main class (see class javadoc). */
    @NonNull
    public Builder mainClass(@NonNull ClassType mainClass) {
      this.mainClass = mainClass;
      return this;
    }

    /**
     * Optional: a pre-configured {@link PointerAnalysisConfig.Builder} for qilin-specific knobs
     * (context sensitivity, heap abstraction policy, debloating, ...). Its {@code
     * seedEntryPointClinits}/{@code clinitVirtualCallResolver} are overridden with the common
     * stage's values at {@link #build()} time, so setting them here has no effect. Defaults to a
     * fresh {@link PointerAnalysisConfig#builder()} if not supplied.
     */
    @NonNull
    public Builder pointerAnalysisConfig(PointerAnalysisConfig.@NonNull Builder configBuilder) {
      this.configBuilder = configBuilder;
      return this;
    }

    @NonNull
    public QilinCallGraphConfig build() {
      if (mainClass == null) {
        throw new IllegalStateException(
            "mainClass is required - qilin.core.PTAScene needs exactly one main class");
      }
      return new QilinCallGraphConfig(common, mainClass, configBuilder);
    }
  }
}
