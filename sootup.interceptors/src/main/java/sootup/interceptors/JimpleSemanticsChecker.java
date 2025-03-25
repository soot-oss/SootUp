package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2023 Markus Schmidt
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
      Body.@NonNull BodyBuilder builder, @NonNull View view) {
    // FIXME: implement
    throw new UnsupportedOperationException("List of Validators is not incorporated yet.");
  }

  @Override
  public abstract void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view);

  public static class LoggingJimpleChecker extends JimpleSemanticsChecker {

    @Override
    public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
      final List<ValidationException> exceptions = validate(builder, view);
      for (ValidationException validationException : exceptions) {
        logger.warn(validationException.getMessage());
      }
    }
  }

  public static class ThrowingJimpleChecker extends LoggingJimpleChecker {

    @Override
    public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
      super.interceptBody(builder, view);
      throw new IllegalStateException("There are semantic errors in the Jimple - see warn log.");
    }
  }
}
