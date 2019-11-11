package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.transform.BodyInterceptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

/** Built-in sets of {@link BodyInterceptor}s for the bytecode frontend */
public enum BytecodeBodyInterceptors {
  Default(
      new CastAndReturnInliner(),
      new DuplicateCatchAllTrapRemover(),
      new UnreachableCodeEliminator(),
      new LocalSplitter(),
      new Aggregator(),
      new UnusedLocalEliminator(),
      new TypeAssigner(),
      new LocalNameStandardizer(),
      new CopyPropagator(),
      new DeadAssignmentEliminator(),
      new NopEliminator(),
      new ConditionalBranchFolder(),
      new EmptySwitchEliminator());

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  BytecodeBodyInterceptors(BodyInterceptor... bodyInterceptors) {
    this.bodyInterceptors = Collections.unmodifiableList(Arrays.asList(bodyInterceptors));
  }

  @Nonnull
  public List<BodyInterceptor> bodyInterceptors() {
    return bodyInterceptors;
  }
}
