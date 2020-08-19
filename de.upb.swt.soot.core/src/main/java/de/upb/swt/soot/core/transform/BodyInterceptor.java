package de.upb.swt.soot.core.transform;

import de.upb.swt.soot.core.model.Body;
import javax.annotation.Nonnull;

/**
 * @author Christian Brüggemann
 * @see #interceptBody(Body)
 */
public interface BodyInterceptor {

  /**
   * Takes a body and may apply a transformation to it, for example removing unused local variables.
   * Since {@link Body} is immutable, this needs to create a new instance.
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
