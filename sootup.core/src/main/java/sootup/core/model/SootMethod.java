package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Jan Martin Persch and others
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
import java.util.List;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/**
 * Soot's counterpart of the source language's method concept. Soot representation of a Java method.
 * Can be declared to belong to a SootClass. Does not contain the actual code, which belongs to a
 * Body.
 *
 * @author Linghui Luo
 * @author Jan Martin Persch
 */
public interface SootMethod extends Method {

  @NonNull Set<MethodModifier> getModifiers();

  @NonNull MethodSignature getSignature();

  boolean isProtected();

  boolean isPrivate();

  boolean isPublic();

  boolean isStatic();

  boolean isFinal();

  boolean isAbstract();

  boolean isNative();

  boolean isSynchronized();

  boolean isConcrete();

  boolean hasBody();

  @NonNull Body getBody();

  @NonNull Type getReturnType();

  int getParameterCount();

  @NonNull Type getParameterType(int index);

  @NonNull List<Type> getParameterTypes();

  @NonNull MethodSubSignature getSubSignature();

  @NonNull String getName();

  @NonNull List<ClassType> getExceptionSignatures();

  @NonNull BodySource getBodySource();

  boolean isMain(@NonNull IdentifierFactory idf);

  boolean isConstructor(@NonNull IdentifierFactory idf);

  boolean isDefaultConstructor(@NonNull IdentifierFactory idf);

  void toString(@NonNull StmtPrinter printer);

  @NonNull SootMethod withOverridingMethodSource(
      Function<OverridingBodySource, OverridingBodySource> overrider);

  @NonNull SootMethod withSource(@NonNull BodySource source);

  @NonNull SootMethod withModifiers(@NonNull Iterable<MethodModifier> modifiers);

  @NonNull SootMethod withThrownExceptions(@NonNull Iterable<ClassType> thrownExceptions);

  @NonNull SootMethod withBody(@NonNull Body body);

  @NonNull Position getPosition();

  @NonNull ClassType getDeclaringClassType();
}
