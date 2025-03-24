package sootup.java.core.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2025 Zun Wang
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
import java.util.*;
import org.jspecify.annotations.NonNull;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.util.ImmutableUtils;
import sootup.java.core.JavaIdentifierFactory;

public class ExceptionInferResult {

  private final Set<ClassType> exceptions;

  public ExceptionInferResult(Set<ClassType> exceptions) {
    this.exceptions = ImmutableUtils.immutableSetOf(exceptions);
  }

  public ExceptionInferResult(ClassType exception) {
    this.exceptions = ImmutableUtils.immutableSet(exception);
  }

  static ExceptionInferResult createThrowableExceptions() {
    return new ExceptionInferResult(ImmutableUtils.immutableSet(ExceptionType.THROWABLE));
  }

  static ExceptionInferResult createNullPointerException() {
    return new ExceptionInferResult(
        ImmutableUtils.immutableSet(ExceptionType.NUll_POINTER_EXCEPTION));
  }

  static ExceptionInferResult createSingleException(
      @NonNull ClassType exceptionType, @NonNull TypeHierarchy typeHierarchy) {
    if (!typeHierarchy.contains(exceptionType)) {
      throw new IllegalArgumentException(
          "The given exceptionType \"" + exceptionType + "\" is not in type hierarchy!");
    }
    return new ExceptionInferResult(exceptionType);
  }

  static ExceptionInferResult createEmptyException() {
    return new ExceptionInferResult(ImmutableUtils.emptyImmutableSet());
  }

  static ExceptionInferResult createDefaultResult() {
    return new ExceptionInferResult(
        ImmutableUtils.immutableSet(ErrorType.VM_ERROR, ErrorType.THREAD_DEATH));
  }

  public Set<ClassType> getExceptions() {
    return this.exceptions;
  }

  protected ExceptionInferResult addException(ClassType newException, TypeHierarchy typeHierarchy) {
    if (!typeHierarchy.contains(newException)) {
      throw new IllegalArgumentException(
          "The given exceptionType \"" + newException + "\" is not in type hierarchy!");
    }
    Set<ClassType> resultSet = new HashSet<>(this.exceptions);
    for (ClassType exception : exceptions) {
      if (exception.equals(newException) || typeHierarchy.isSubtype(exception, newException)) {
        return this;
      }
      if (typeHierarchy.isSubtype(newException, exception)) {
        resultSet.remove(exception);
      }
    }
    resultSet.add(newException);
    return new ExceptionInferResult(resultSet);
  }

  protected ExceptionInferResult addExceptions(
      ExceptionInferResult newResult, TypeHierarchy typeHierarchy) {
    Set<ClassType> newExceptions = new HashSet<>(newResult.exceptions);
    Iterator<ClassType> newExceptionsIterator = newExceptions.iterator();
    while (newExceptionsIterator.hasNext()) {
      ClassType newException = newExceptionsIterator.next();
      boolean isNewExceptionContained =
          this.exceptions.stream()
              .anyMatch(
                  oldException ->
                      oldException.equals(newException)
                          || typeHierarchy.isSubtype(oldException, newException));
      if (isNewExceptionContained) {
        newExceptionsIterator.remove();
      }
    }
    Set<ClassType> oldExceptions = new HashSet<>(this.exceptions);
    Iterator<ClassType> oldExceptionsIterator = oldExceptions.iterator();
    while (oldExceptionsIterator.hasNext()) {
      ClassType oldException = oldExceptionsIterator.next();
      newExceptions.stream()
          .forEach(
              newException -> {
                if (typeHierarchy.isSubtype(newException, oldException)) {
                  oldExceptionsIterator.remove();
                }
              });
    }
    newExceptions.addAll(oldExceptions);
    return new ExceptionInferResult(newExceptions);
  }

  public static class ExceptionType {
    public static final JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    public static final ClassType THROWABLE = idFactory.getClassType("java.lang.Throwable");
    public static final ClassType NUll_POINTER_EXCEPTION =
        idFactory.getClassType("java.lang.NullPointerException");
    public static final ClassType ARRAY_STORE_EXCEPTION =
        idFactory.getClassType("java.lang.ArrayStoreException");
    public static final ClassType INDEX_OUT_OF_BOUNDS_EXCEPTION =
        idFactory.getClassType("java.lang.IndexOutOfBoundsException");
    public static final ClassType ARITHMETIC_EXCEPTION =
        idFactory.getClassType("java.lang.ArithmeticException");
    public static final ClassType NEGATIVE_ARRAY_SIZE_EXCEPTION =
        idFactory.getClassType("java.lang.NegativeArraySizeException");
    public static final ClassType CLASS_CAST_EXCEPTION =
        idFactory.getClassType("java.lang.ClassCastException");
    public static final ClassType ILLEGAL_MONITOR_STATE_EXCEPTION =
        idFactory.getClassType("java.lang.IllegalMonitorStateException");
  }

  public static class ErrorType {
    public static final JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    public static final ClassType INITIALIZATION_ERROR = idFactory.getClassType("java.lang.Error");
    public static final ClassType VM_ERROR =
        idFactory.getClassType("java.lang.VirtualMachineError");
    public static final ClassType THREAD_DEATH = idFactory.getClassType("java.lang.ThreadDeath");
    public static final ClassType RESOLVE_FIELD_ERROR =
        idFactory.getClassType("java.lang.NoSuchFieldError");
    public static final ClassType RESOLVE_CLASS_ERROR =
        idFactory.getClassType("java.lang.LinkageError");
    public static final ClassType ABSTRACT_METHOD_ERROR =
        idFactory.getClassType("java.lang.AbstractMethodError");
    public static final ClassType NO_SUCH_METHOD_ERROR =
        idFactory.getClassType("java.lang.NoSuchMethodError");
    public static final ClassType UNSATISFIED_LINK_ERROR =
        idFactory.getClassType("java.lang.UnsatisfiedLinkError");
  }
}
