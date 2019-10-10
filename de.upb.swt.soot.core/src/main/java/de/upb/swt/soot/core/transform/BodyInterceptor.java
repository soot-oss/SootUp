package de.upb.swt.soot.core.transform;

import de.upb.swt.soot.core.model.Body;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/** @author Christian Brüggemann */
public interface BodyInterceptor {

  /**
   * Takes a body and may apply a transformation to it, for example removing unused local variables.
   * Since {@link Body} is immutable, this needs to create a new instance. Consider using the <code>
   * with</code>-methods of {@link Body} such as {@link Body#withStmts(List)}, {@link
   * Body#withLocals(Set)} and others to simplify the creation of new instances.
   *
   * <p>In case no transformation is applied, this method may return the original body it received
   * as its parameter.
   *
   * <p><b>Warning:</b> Implementations of this method must not modify the original body or any of
   * its contents.
   */
  @Nonnull
  Body interceptBody(@Nonnull Body originalBody);
}
