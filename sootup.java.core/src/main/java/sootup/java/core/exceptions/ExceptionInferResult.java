package sootup.java.core.exceptions;

import java.util.*;
import javax.annotation.Nonnull;
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
      @Nonnull ClassType exceptionType, @Nonnull TypeHierarchy typeHierarchy) {
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
      boolean isOldExceptionContained =
          newExceptions.stream()
              .anyMatch(newException -> typeHierarchy.isSubtype(newException, oldException));
      if (isOldExceptionContained) {
        oldExceptionsIterator.remove();
      }
    }
    newExceptions.addAll(oldExceptions);
    return new ExceptionInferResult(newExceptions);
  }

  public static class ExceptionType {
    static final JavaIdentifierFactory idFactory = JavaIdentifierFactory.getInstance();
    static final ClassType THROWABLE = idFactory.getClassType("java.lang.Throwable");
    static final ClassType NUll_POINTER_EXCEPTION =
        idFactory.getClassType("java.lang.NullPointerException");
    static final ClassType ARRAY_STORE_EXCEPTION =
        idFactory.getClassType("java.lang.ArrayStoreException");
    static final ClassType INDEX_OUT_OF_BOUNDS_EXCEPTION =
        idFactory.getClassType("java.lang.IndexOutOfBoundsException");
    static final ClassType ARITHMETIC_EXCEPTION =
        idFactory.getClassType("java.lang.ArithmeticException");
    static final ClassType NEGATIVE_ARRAY_SIZE_EXCEPTION =
        idFactory.getClassType("java.lang.NegativeArraySizeException");
    static final ClassType CLASS_CAST_EXCEPTION =
        idFactory.getClassType("java.lang.ClassCastException");
    static final ClassType ILLEGAL_MONITOR_STATE_EXCEPTION = idFactory.getClassType("java.lang.IllegalMonitorStateException");

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
