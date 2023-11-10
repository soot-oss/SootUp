package sootup.java.bytecode.interceptors;

import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.validation.ValidationException;
import sootup.core.views.View;

/**
 * This Interceptor executes validations on Jimple semantics
 *
 * <p>e.g. like a Local must be assigned before its use
 */
public abstract class JimpleSemanticsChecker implements BodyInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(JimpleSemanticsChecker.class);

  protected List<ValidationException> validate(
      @Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
    // FIXME: implement
    throw new UnsupportedOperationException("List of Validators is not incorporated yet.");
  }

  @Override
  public abstract void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view);

  public static class LoggingJimpleChecker extends JimpleSemanticsChecker {

    @Override
    public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
      final List<ValidationException> exceptions = validate(builder, view);
      for (ValidationException validationException : exceptions) {
        logger.warn(validationException.getMessage());
      }
    }
  }

  public static class ThrowingJimpleChecker extends LoggingJimpleChecker {

    @Override
    public void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view) {
      super.interceptBody(builder, view);
      throw new IllegalStateException("There are semantic errors in the Jimple - see warn log.");
    }
  }
}
