package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2004-2020 Jennifer Lhotak, Linghui Luo, Markus Schmidt and others
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.visitor.AbstractTypeVisitor;
import sootup.core.types.*;

/**
 * Generates locals for Body.
 *
 * @author Linghui Luo
 * @author Markus Schmidt
 */
public class LocalGenerator {
  private final Set<Local> locals;
  @Nullable private Local thisLocal;
  private final Map<Integer, Local> parameterLocals = new HashMap<>();
  NamingSwitch ns = new NamingSwitch(new StringBuilder(7));

  /**
   * Creates Locals {@link Local} with a standard naming scheme. If a Set of Locals is provided, the
   * LocalGenerator checks whether the name is already taken.
   */
  public LocalGenerator(@Nonnull Set<Local> existingLocals) {
    locals = existingLocals;
  }

  /** generate this local with given type */
  public Local generateThisLocal(@Nonnull Type type) {
    if (this.thisLocal == null) {
      this.thisLocal = generateFieldLocal(type);
    }
    return this.thisLocal;
  }

  /** generates a new {@link Local} given the type for field. */
  public Local generateFieldLocal(@Nonnull Type type) {
    // field Locals traditionally do not begin with "$"

    Local localCandidate;
    StringBuilder name = ns.getResult();
    // is there a name collision? retry!
    do {
      name.setLength(0);
      type.accept(ns);
      localCandidate = Jimple.newLocal(name.toString(), type);
    } while (locals.contains(localCandidate));

    locals.add(localCandidate);
    return localCandidate;
  }

  /** generates a new {@link Local} given the type for local. */
  public Local generateLocal(@Nonnull Type type) {
    // non-field Locals traditionally begin with "$"

    StringBuilder name = ns.getResult();
    name.setLength(0); // clear buffer - remove possible leftovers from last generate call
    name.append("$");

    Local localCandidate;
    type.accept(ns);
    localCandidate = Jimple.newLocal(name.toString(), type);
    // is there a name collision? retry!
    while (locals.contains(localCandidate)) {
      name.setLength(1);
      type.accept(ns);
      localCandidate = Jimple.newLocal(name.toString(), type);
    }

    locals.add(localCandidate);
    return localCandidate;
  }

  public Local generateParameterLocal(@Nonnull Type type, int index) {
    if (!this.parameterLocals.containsKey(index)) {
      Local paraLocal = generateLocal(type);
      this.parameterLocals.put(index, paraLocal);
    }
    return this.parameterLocals.get(index);
  }

  private static class NamingSwitch extends AbstractTypeVisitor<StringBuilder> {
    private int tempInt = 0;
    private int tempBoolean = 0;
    private int tempLong = 0;
    private int tempDouble = 0;
    private int tempFloat = 0;
    private int tempRefLikeType = 0;
    private int tempByte = 0;
    private int tempShort = 0;
    private int tempChar = 0;
    private int tempUnknownType = 0;

    private NamingSwitch(@Nonnull StringBuilder str) {
      this.result = str;
    }

    @Override
    public void caseBooleanType() {
      result.append("z").append(tempBoolean++);
    }

    @Override
    public void caseByteType() {
      result.append("b").append(tempByte++);
    }

    @Override
    public void caseCharType() {
      result.append("c").append(tempChar++);
    }

    @Override
    public void caseShortType() {
      result.append("s").append(tempShort++);
    }

    @Override
    public void caseIntType() {
      result.append("i").append(tempInt++);
    }

    @Override
    public void caseLongType() {
      result.append("l").append(tempLong++);
    }

    @Override
    public void caseDoubleType() {
      result.append("d").append(tempDouble++);
    }

    @Override
    public void caseFloatType() {
      result.append("f").append(tempFloat++);
    }

    @Override
    public void caseArrayType() {
      result.append("r").append(tempRefLikeType++);
    }

    @Override
    public void caseClassType(@Nonnull ClassType classType) {
      result.append("r").append(tempRefLikeType++);
    }

    @Override
    public void caseNullType() {
      defaultCaseType();
    }

    @Override
    public void caseVoidType() {
      // how does a local with a voidtype make sense..? but obviously there was code/ a letter
      // assigned for it in old soot.. result.append("v").append(tempVoid++);
      defaultCaseType();
    }

    @Override
    public void caseUnknownType() {
      result.append("u").append(tempUnknownType++);
    }

    @Override
    public void defaultCaseType() {
      throw new IllegalStateException("Unhandled Type of Local variable to Generate!");
    }
  }

  /** Return all locals created for the body referenced in this LocalGenrator. */
  public Set<Local> getLocals() {
    return this.locals;
  }

  @Nullable
  public Local getThisLocal() {
    return this.thisLocal;
  }

  public Local getParameterLocal(int i) {
    return this.parameterLocals.get(i);
  }
}
