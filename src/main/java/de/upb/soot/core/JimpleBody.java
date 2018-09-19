package de.upb.soot.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import de.upb.soot.Options;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.util.Chain;
import de.upb.soot.validation.BodyValidator;
import de.upb.soot.validation.FieldRefValidator;
import de.upb.soot.validation.IdentityStatementsValidator;
import de.upb.soot.validation.IdentityValidator;
import de.upb.soot.validation.InvokeArgumentValidator;
import de.upb.soot.validation.JimpleTrapValidator;
import de.upb.soot.validation.MethodValidator;
import de.upb.soot.validation.NewValidator;
import de.upb.soot.validation.ReturnStatementsValidator;
import de.upb.soot.validation.TypesValidator;
import de.upb.soot.validation.ValidationException;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/* Legacy Dummy class */

class PatchingChain<E extends Body.Unit> extends AbstractCollection<E> implements Chain<E> {

  @Override
  public void insertBefore(List<E> toInsert, E point) {

  }

  @Override
  public void insertAfter(List<E> toInsert, E point) {

  }

  @Override
  public void insertAfter(E toInsert, E point) {

  }

  @Override
  public void insertAfter(Collection<? extends E> toInsert, E point) {

  }

  @Override
  public void insertBefore(E toInsert, E point) {

  }

  @Override
  public void insertBefore(Collection<? extends E> toInsert, E point) {

  }

  @Override
  public void insertBefore(Chain<E> toInsert, E point) {

  }

  @Override
  public void insertAfter(Chain<E> toInsert, E point) {

  }

  @Override
  public void swapWith(E out, E in) {

  }

  @Override
  public void addFirst(E u) {

  }

  @Override
  public void addLast(E u) {

  }

  @Override
  public void removeFirst() {

  }

  @Override
  public void removeLast() {

  }

  @Override
  public boolean follows(E someObject, E someReferenceObject) {
    return false;
  }

  @Override
  public E getFirst() {
    return null;
  }

  @Override
  public E getLast() {
    return null;
  }

  @Override
  public E getSuccOf(E point) {
    return null;
  }

  @Override
  public E getPredOf(E point) {
    return null;
  }

  @Override
  public Iterator<E> snapshotIterator() {
    return null;
  }

  @Override
  public Iterator<E> iterator(E u) {
    return null;
  }

  @Override
  public Iterator<E> iterator(E head, E tail) {
    return null;
  }

  @Override
  public long getModificationCount() {
    return 0;
  }

  @Override
  public Collection<E> getElementsUnsorted() {
    return null;
  }

  @Override
  public Iterator<E> iterator() {
    return null;
  }

  @Override
  public int size() {
    return 0;
  }
}
/* End of legacy dummy class */

/** Implementation of the Body class for the Jimple IR. */
public class JimpleBody extends Body {
  private static BodyValidator[] validators;

  /**
   * Returns an array containing some validators in order to validate the JimpleBody
   *
   * @return the array containing validators
   */
  private synchronized static BodyValidator[] getValidators() {
    if (validators == null) {
      validators = new BodyValidator[] { IdentityStatementsValidator.getInstance(), TypesValidator.getInstance(),
          ReturnStatementsValidator.getInstance(), InvokeArgumentValidator.getInstance(), FieldRefValidator.getInstance(),
          NewValidator.getInstance(), JimpleTrapValidator.getInstance(), IdentityValidator.getInstance(),
          MethodValidator.getInstance()
          // InvokeValidator.getInstance()
      };
    }
    return validators;
  };

  /**
   * Construct an empty JimpleBody
   */
  public JimpleBody(SootMethod m) {
    super(m);
  }

  /**
   * Construct an extremely empty JimpleBody, for parsing into.
   */
  public JimpleBody() {
  }

  /** Clones the current body, making deep copies of the contents. */
  @Override
  public Object clone() {
    Body b = new JimpleBody(getMethod());
    b.importBodyContentsFrom(this);
    return b;
  }

  /**
   * Make sure that the JimpleBody is well formed. If not, throw an exception. Right now, performs only a handful of checks.
   */
  @Override
  public void validate() {
    final List<ValidationException> exceptionList = new ArrayList<ValidationException>();
    validate(exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /**
   * Validates the jimple body and saves a list of all validation errors
   *
   * @param exceptionList
   *          the list of validation errors
   */
  @Override
  public void validate(List<ValidationException> exceptionList) {
    super.validate(exceptionList);
    final boolean runAllValidators = Options.getInstance().debug() || Options.getInstance().validate();
    for (BodyValidator validator : getValidators()) {
      if (!validator.isBasicValidator() && !runAllValidators) {
        continue;
      }
      validator.validate(this, exceptionList);
    }
  }

  public void validateIdentityStatements() {
    runValidation(IdentityStatementsValidator.getInstance());
  }

  /** Inserts usual statements for handling this & parameters into body. */
  public void insertIdentityStmts() {
    insertIdentityStmts(getMethod().getDeclaringClass());
  }

  /**
   * Inserts usual statements for handling this & parameters into body.
   *
   * @param declaringClass
   *          the class, which should be used for this references. Can be null for static methods
   */
  public void insertIdentityStmts(SootClass declaringClass) {
    final Jimple jimple = Jimple.getInstance();
    final UnitPatchingChain unitChain = getUnits();
    final Chain<Local> localChain = getLocals();
    Unit lastUnit = null;

    // add this-ref before everything else
    if (!getMethod().isStatic()) {
      if (declaringClass == null) {
        throw new IllegalArgumentException(
            String.format("No declaring class given for method %s", method.getSubSignature()));
      }
      Local l = jimple.newLocal("this", RefType.getInstance(declaringClass));
      Stmt s = jimple.newIdentityStmt(l, jimple.newThisRef((RefType) l.getType()));

      localChain.add(l);
      /*
       * TODO: check Unit problems unitChain.addFirst(s); lastUnit = s;
       */
    }

    int i = 0;
    for (Type t : getMethod().getParameterTypes()) {
      Local l = jimple.newLocal("parameter" + i, t);
      Stmt s = jimple.newIdentityStmt(l, jimple.newParameterRef(l.getType(), i));
      // TODO: check: Unit problems
      /*
       * localChain.add(l); if (lastUnit == null) { unitChain.addFirst(s); } else { unitChain.insertAfter(s, lastUnit); }
       * lastUnit = s;
       */
      i++;
    }
  }

  /** Returns the first non-identity stmt in this body. */
  public Stmt getFirstNonIdentityStmt() {
    Iterator<Unit> it = getUnits().iterator();
    Object o = null;
    while (it.hasNext()) {
      if (!((o = it.next()) instanceof JIdentityStmt)) {
        break;
      }
    }
    if (o == null) {
      throw new RuntimeException("no non-id statements!");
    }
    return (Stmt) o;
  }
}
