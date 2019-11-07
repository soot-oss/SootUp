package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import javax.annotation.Nonnull;

// https://github.com/Sable/soot/blob/master/src/main/java/soot/toolkits/scalar/LocalSplitter.java

/**
 * A BodyTransformer that attemps to indentify and separate uses of a local variable that are
 * independent of each other. Conceptually the inverse transform with respect to the LocalPacker
 * transform.
 *
 * <p>For example the code:
 *
 * <p>for(int i; i < k; i++); for(int i; i < k; i++);
 *
 * <p>would be transformed into: for(int i; i < k; i++); for(int j; j < k; j++);
 */
public class LocalSplitter implements BodyInterceptor {

  @Nonnull
  @Override
  public Body interceptBody(@Nonnull Body originalBody) {
    // TODO Implement
    return originalBody;
  }
}
