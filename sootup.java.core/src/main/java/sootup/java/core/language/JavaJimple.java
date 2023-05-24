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
import sootup.core.IdentifierFactory;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.constant.ClassConstant;
import sootup.core.jimple.common.constant.EnumConstant;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.jimple.common.constant.MethodType;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.jimple.basic.JavaLocal;

/**
 * JavaJimple implements the Java specific terms for {@link Jimple}
 *
 * @author Markus Schmidt
 */
public class JavaJimple extends Jimple {

  private static final JavaJimple INSTANCE = new JavaJimple();

  public static JavaJimple getInstance() {
    return INSTANCE;
  }

  @Override
  public IdentifierFactory getIdentifierFactory() {
    return JavaIdentifierFactory.getInstance();
  }

  public static boolean isJavaKeywordType(Type t) {
    // TODO: [JMP] Ensure that the check is complete.
    return t instanceof PrimitiveType || t instanceof VoidType || t instanceof NullType;
  }

  /** Constructs a Local with the given name and type. */
  public static JavaLocal newLocal(String name, Type t, Iterable<AnnotationUsage> annotations) {
    return new JavaLocal(name, t, annotations);
  }

  public JCaughtExceptionRef newCaughtExceptionRef() {
    return new JCaughtExceptionRef(getIdentifierFactory().getType("java.lang.Throwable"));
  }

  public ClassConstant newClassConstant(String value) {
    return new ClassConstant(value, getIdentifierFactory().getType("java.lang.Class"));
  }

  public EnumConstant newEnumConstant(String value, String type) {
    return new EnumConstant(value, getIdentifierFactory().getClassType(type));
  }

  public StringConstant newStringConstant(String value) {
    return new StringConstant(value, getIdentifierFactory().getType("java.lang.String"));
  }

  public MethodHandle newMethodHandle(JFieldRef ref, int tag) {
    return new MethodHandle(
        ref, tag, getIdentifierFactory().getType("java.lang.invoke.MethodHandle"));
  }

  public MethodHandle newMethodHandle(MethodSignature ref, int tag) {
    return new MethodHandle(
        ref, tag, getIdentifierFactory().getType("java.lang.invoke.MethodHandle"));
  }

  public MethodType newMethodType(List<Type> parameterTypes, Type returnType) {
    return new MethodType(
        getIdentifierFactory().getMethodSubSignature("__METHODTYPE__", returnType, parameterTypes),
        getIdentifierFactory().getClassType("java.lang.invoke.MethodType"));
  }

  /** Constructs a Local with the given name and type. */
  public static JavaLocal newLocal(String name, Type t) {
    return new JavaLocal(name, t, Collections.emptyList());
  }
}
