package sootup.java.core.language;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Markus Schmidt
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

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.AbstractLocal;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.EnumConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.MethodType;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.signatures.SootClassMemberSignature;
import sootup.core.signatures.SootClassMemberSubSignature;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.jimple.basic.JavaSlotLocal;
import sootup.java.core.jimple.basic.JavaStackLocal;

/**
 * JavaJimple implements the Java specific terms for {@link Jimple}
 *
 * @author Markus Schmidt
 */
public class JavaJimple extends Jimple {

  public static boolean isJavaKeywordType(Type t) {
    // TODO: [JMP] Ensure that the check is complete.
    return t instanceof PrimitiveType || t instanceof VoidType || t instanceof NullType;
  }

  /** Constructs a Local with the given name and type. */
  public static JavaLocal newLocal(String name, Type t, Iterable<AnnotationUsage> annotations) {
    return new JavaLocalImpl(name, t, annotations);
  }

  /**
   * @deprecated Use {@link #newSlotLocal(String, Type, int)} or {@link #newLocal(String, Type)}.
   */
  @Deprecated
  public static JavaLocal newLocal(String name, Type t, int slotIndex) {
    return newLocal(name, t, slotIndex, Collections.emptyList());
  }

  /**
   * @deprecated Use an explicit generic or slot local factory.
   */
  @Deprecated
  public static JavaLocal newLocal(
      String name, Type t, int slotIndex, Iterable<AnnotationUsage> annotations) {
    return slotIndex == -1
        ? newLocal(name, t, annotations)
        : newSlotLocal(name, t, slotIndex, annotations);
  }

  /** Constructs a temporary originating from the JVM operand stack. */
  public static JavaStackLocal newStackLocal(String name, Type t) {
    return newStackLocal(name, t, Collections.emptyList());
  }

  public static JavaStackLocal newStackLocal(
      String name, Type t, Iterable<AnnotationUsage> annotations) {
    return new JavaStackLocalImpl(name, t, annotations);
  }

  /** Constructs a local originating from the given nonnegative JVM local variable slot. */
  public static JavaSlotLocal newSlotLocal(String name, Type t, int slotIndex) {
    return newSlotLocal(name, t, slotIndex, Collections.emptyList());
  }

  public static JavaSlotLocal newSlotLocal(
      String name, Type t, int slotIndex, Iterable<AnnotationUsage> annotations) {
    return new JavaSlotLocalImpl(name, t, slotIndex, annotations);
  }

  /**
   * Constructs a CaughtExceptionRef() grammar chunk.
   *
   * @param identifierFactory the factory that provides the {@code java.lang.Throwable} type
   * @return the created caught exception reference
   */
  public static JCaughtExceptionRef newCaughtExceptionRef(
      @NonNull IdentifierFactory identifierFactory) {
    return new JCaughtExceptionRef(identifierFactory.getType("java.lang.Throwable"));
  }

  public static ClassConstant newClassConstant(
      String value, @NonNull IdentifierFactory identifierFactory) {
    return new ClassConstant(value, identifierFactory.getType("java.lang.Class"));
  }

  public static EnumConstant newEnumConstant(
      String value, String type, @NonNull IdentifierFactory identifierFactory) {
    return new EnumConstant(value, identifierFactory.getClassType(type), identifierFactory);
  }

  public static StringConstant newStringConstant(
      String value, @NonNull IdentifierFactory identifierFactory) {
    return new StringConstant(value, identifierFactory.getType("java.lang.String"));
  }

  public static MethodHandle newMethodHandle(
      SootClassMemberSignature<? extends SootClassMemberSubSignature> ref,
      int tag,
      @NonNull IdentifierFactory identifierFactory) {
    return new MethodHandle(ref, tag, identifierFactory.getType("java.lang.invoke.MethodHandle"));
  }

  public static MethodHandle newMethodHandle(
      SootClassMemberSignature<? extends SootClassMemberSubSignature> ref,
      MethodHandle.Kind kind,
      @NonNull IdentifierFactory identifierFactory) {
    return new MethodHandle(ref, kind, identifierFactory.getType("java.lang.invoke.MethodHandle"));
  }

  public static MethodType newMethodType(
      List<Type> parameterTypes, Type returnType, @NonNull IdentifierFactory identifierFactory) {
    return new MethodType(
        identifierFactory.getMethodSubSignature("__METHODTYPE__", returnType, parameterTypes),
        identifierFactory.getClassType("java.lang.invoke.MethodType"));
  }

  /** Constructs a Local with the given name and type. */
  public static JavaLocal newLocal(String name, Type t) {
    return new JavaLocalImpl(name, t, Collections.emptyList());
  }

  /** Generic Java local with annotations and no JVM slot or operand stack provenance. */
  private static class JavaLocalImpl extends AbstractLocal implements JavaLocal {
    @NonNull private final Iterable<AnnotationUsage> annotations;

    private JavaLocalImpl(String name, Type type, @NonNull Iterable<AnnotationUsage> annotations) {
      super(name, type);
      this.annotations = annotations;
    }

    @Override
    public @NonNull Iterable<AnnotationUsage> getAnnotations() {
      return annotations;
    }

    @Override
    public @NonNull JavaLocal withName(@NonNull String name) {
      return new JavaLocalImpl(name, getType(), annotations);
    }

    @Override
    public @NonNull JavaLocal withType(@NonNull Type type) {
      return new JavaLocalImpl(getName(), type, annotations);
    }

    @Override
    public @NonNull JavaLocal withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
      return new JavaLocalImpl(getName(), getType(), annotations);
    }
  }

  /** Operand stack temporary that preserves Java annotations when copied. */
  private static final class JavaStackLocalImpl extends JavaLocalImpl implements JavaStackLocal {
    private JavaStackLocalImpl(
        String name, Type type, @NonNull Iterable<AnnotationUsage> annotations) {
      super(name, type, annotations);
    }

    @Override
    public @NonNull JavaStackLocal withName(@NonNull String name) {
      return new JavaStackLocalImpl(name, getType(), getAnnotations());
    }

    @Override
    public @NonNull JavaStackLocal withType(@NonNull Type type) {
      return new JavaStackLocalImpl(getName(), type, getAnnotations());
    }

    @Override
    public @NonNull JavaStackLocal withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
      return new JavaStackLocalImpl(getName(), getType(), annotations);
    }
  }

  /** Local preserving its original JVM slot index and Java annotations when copied. */
  private static final class JavaSlotLocalImpl extends JavaLocalImpl implements JavaSlotLocal {
    private final int slotIndex;

    private JavaSlotLocalImpl(
        String name, Type type, int slotIndex, @NonNull Iterable<AnnotationUsage> annotations) {
      super(name, type, annotations);
      if (slotIndex < 0) {
        throw new IllegalArgumentException("Slot index must be nonnegative");
      }
      this.slotIndex = slotIndex;
    }

    @Override
    public int getSlotIndex() {
      return slotIndex;
    }

    @Override
    public @NonNull JavaSlotLocal withName(@NonNull String name) {
      return new JavaSlotLocalImpl(name, getType(), slotIndex, getAnnotations());
    }

    @Override
    public @NonNull JavaSlotLocal withType(@NonNull Type type) {
      return new JavaSlotLocalImpl(getName(), type, slotIndex, getAnnotations());
    }

    @Override
    public @NonNull JavaSlotLocal withAnnotations(@NonNull Iterable<AnnotationUsage> annotations) {
      return new JavaSlotLocalImpl(getName(), getType(), slotIndex, annotations);
    }

    @Override
    public @NonNull JavaSlotLocal withSlotIndex(int slotIndex) {
      return new JavaSlotLocalImpl(getName(), getType(), slotIndex, getAnnotations());
    }
  }
}
