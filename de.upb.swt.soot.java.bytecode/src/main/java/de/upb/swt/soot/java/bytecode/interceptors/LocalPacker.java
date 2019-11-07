package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/toolkits/scalar/LocalPacker.java

/**
 * A BodyTransformer that attemps to minimize the number of local variables used in Body by
 * 'reusing' them when possible. Implemented as a singleton. For example the code:
 *
 * <p>for(int i; i < k; i++); for(int j; j < k; j++);
 *
 * <p>would be transformed into: for(int i; i < k; i++); for(int i; i < k; i++);
 *
 * <p>assuming to further conflicting uses of i and j.
 *
 * <p>Note: LocalSplitter is corresponds to the inverse transformation.
 *
 * @see LocalSplitter
 */
public class LocalPacker implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
