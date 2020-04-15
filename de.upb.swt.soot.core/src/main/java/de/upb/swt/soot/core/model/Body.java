package de.upb.swt.soot.core.model;
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

import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.ref.JParameterRef;
import de.upb.swt.soot.core.jimple.common.ref.JThisRef;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.EscapedWriter;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.validation.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class that models the Jimple body (code attribute) of a methodRef.
 *
 * @author Linghui Luo
 */
public final class Body implements Copyable {
  /** The locals for this Body. */
  private final Set<Local> locals;

  /** The traps for this Body. */
  private final List<Trap> traps;

  /** The stmts for this Body. */
  private final List<Stmt> stmts;

  @Nullable private final Position position;

  /** An array containing some validators in order to validate the JimpleBody */
  @Nonnull
  private static final List<BodyValidator> validators =
      ImmutableUtils.immutableList(
          new LocalsValidator(),
          new TrapsValidator(),
          new StmtsValidator(),
          new UsesValidator(),
          new ValuesValidator(),
          new CheckInitValidator(),
          new CheckTypesValidator(),
          new CheckVoidLocalesValidator(),
          new CheckEscapingValidator());

  /**
   * Creates an body which is not associated to any methodRef.
   *
   * @param locals please use {@link LocalGenerator} to generate local for a body.
   */
  public Body(
      @Nonnull Set<Local> locals,
      @Nonnull List<Trap> traps,
      @Nonnull List<Stmt> stmts,
      @Nullable Position position) {
    this.locals = Collections.unmodifiableSet(locals);
    this.traps = Collections.unmodifiableList(traps);
    this.stmts = Collections.unmodifiableList(stmts);
    this.position = position;

    // FIXME: [JMP] Virtual method call in constructor
    checkInit();
  }

  /** The methodRef associated with this Body. */
  @Nullable private volatile SootMethod _method;

  /**
   * Returns the methodRef associated with this Body.
   *
   * @return the methodRef that owns this body.
   */
  public SootMethod getMethod() {
    SootMethod owner = this._method;

    if (owner == null) {
      throw new IllegalStateException(
          "The owning method of this body instance has not been not set yet.");
    }

    return owner;
  }

  /**
   * Sets the methodRef associated with this Body.
   *
   * @param value the methodRef that owns this body.
   */
  synchronized void setMethod(@Nullable SootMethod value) {
    if (this._method != null) {
      throw new IllegalStateException(
          "The declaring class of this SootMethod has already been set.");
    }

    this._method = value;
  }

  /** Returns the number of locals declared in this body. */
  public int getLocalCount() {
    return locals.size();
  }

  private void runValidation(BodyValidator validator) {
    final List<ValidationException> exceptionList = new ArrayList<>();
    validator.validate(this, exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /** Verifies that a Value is not used in more than one place. */
  public void validateValues() {
    runValidation(new ValuesValidator());
  }

  /** Verifies that each Local of getUsesAndDefs() is in this body's locals Chain. */
  public void validateLocals() {
    runValidation(new LocalsValidator());
  }

  /** Verifies that the begin, end and handler units of each trap are in this body. */
  public void validateTraps() {
    runValidation(new TrapsValidator());
  }

  /** Verifies that the Stmts of this Body all point to a Stmt contained within this body. */
  public void validateStmts() {
    runValidation(new StmtsValidator());
  }

  /** Verifies that each use in this Body has a def. */
  public void validateUses() {
    runValidation(new UsesValidator());
  }

  /** Returns a backed chain of the locals declared in this Body. */
  public Set<Local> getLocals() {
    return locals;
  }

  /** Returns a backed view of the traps found in this Body. */
  public Collection<Trap> getTraps() {
    return traps;
  }

  /** Return unit containing the \@this-assignment * */
  public Stmt getThisStmt() {
    for (Stmt u : getStmts()) {
      if (u instanceof JIdentityStmt && ((JIdentityStmt) u).getRightOp() instanceof JThisRef) {
        return u;
      }
    }

    throw new RuntimeException("couldn't find this-assignment!" + " in " + getMethod());
  }

  /** Return LHS of the first identity stmt assigning from \@this. * */
  public Local getThisLocal() {
    return (Local) (((JIdentityStmt) getThisStmt()).getLeftOp());
  }

  /** Return LHS of the first identity stmt assigning from \@parameter i. * */
  public Local getParameterLocal(int i) {
    for (Stmt s : getStmts()) {
      if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getRightOp() instanceof JParameterRef) {
        JIdentityStmt is = (JIdentityStmt) s;
        JParameterRef pr = (JParameterRef) is.getRightOp();
        if (pr.getNum() == i) {
          return (Local) is.getLeftOp();
        }
      }
    }

    throw new RuntimeException("couldn't find JParameterRef" + i + "! in " + getMethod());
  }

  /**
   * Get all the LHS of the identity statements assigning from parameter references.
   *
   * @return a list of size as per <code>getMethod().getParameterCount()</code> with all elements
   *     ordered as per the parameter index.
   * @throws RuntimeException if a JParameterRef is missing
   */
  public Collection<Local> getParameterLocals() {
    final int numParams = getMethod().getParameterCount();
    final List<Local> retVal = new ArrayList<>(numParams);
    for (Stmt u : stmts) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt is = (JIdentityStmt) u;
        if (is.getRightOp() instanceof JParameterRef) {
          JParameterRef pr = (JParameterRef) is.getRightOp();
          retVal.add(pr.getNum(), (Local) is.getLeftOp());
        }
      }
    }
    if (retVal.size() != numParams) {
      throw new RuntimeException("couldn't find JParameterRef! in " + getMethod());
    }
    return Collections.unmodifiableCollection(retVal);
  }

  /**
   * Returns the statements that make up this body.
   *
   * @return the statements in this Body
   */
  public List<Stmt> getStmts() {
    return stmts;
  }

  private void checkInit() {
    runValidation(new CheckInitValidator());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringWriter writer = new StringWriter();
    try (PrintWriter writerOut = new PrintWriter(new EscapedWriter(writer))) {
      new Printer().printTo(this, writerOut);
    }
    return writer.toString();
  }

  @Nullable
  public Position getPosition() {
    return this.position;
  }

  public void validateIdentityStatements() {
    runValidation(new IdentityStatementsValidator());
  }

  /** Returns the first non-identity stmt in this body. */
  public Stmt getFirstNonIdentityStmt() {
    Iterator<Stmt> it = getStmts().iterator();
    Stmt o = null;
    while (it.hasNext()) {
      if (!((o = it.next()) instanceof JIdentityStmt)) {
        break;
      }
    }
    if (o == null) {
      throw new RuntimeException("no non-id statements!");
    }
    return o;
  }

  /**
   * Returns the results of iterating through all Stmts in this Body and querying them for Values
   * defined. All of the Values found are then returned as a List.
   *
   * @return a List of all the Values for Values defined by this Body's Stmts.
   */
  public Collection<Value> getUses() {
    ArrayList<Value> useList = new ArrayList<>();

    for (Stmt stmt : stmts) {
      useList.addAll(stmt.getUses());
    }
    return useList;
  }

  /**
   * Returns the results of iterating through all Stmts in this Body and querying them for Values
   * defined. All of the Values found are then returned as a List.
   *
   * @return a List of all the Values for Values defined by this Body's Stmts.
   */
  public Collection<Value> getDefs() {
    ArrayList<Value> defList = new ArrayList<>();

    for (Stmt stmt : stmts) {
      defList.addAll(stmt.getDefs());
    }
    return defList;
  }

  /**
   * Returns the result of iterating through all Stmts in this body. All Stmts thus found are
   * returned. Branching Stmts and statements which use PhiExpr will have Stmts; a Stmt contains a
   * Stmt that is either a target of a branch or is being used as a pointer to the end of a CFG
   * block.
   *
   * <p>This methodRef is typically used for pointer patching, e.g. when the unit chain is cloned.
   *
   * @return A collection of all the Stmts held by this body's units.
   */
  public Collection<Stmt> getAllStmts() {
    List<Stmt> stmtList = new ArrayList<>();
    for (Stmt item : stmts) {
      stmtList.addAll(item.getStmts());
    }

    for (Trap item : traps) {
      stmtList.addAll(item.getStmts());
    }
    return Collections.unmodifiableCollection(stmtList);
  }

  @Nonnull
  public Body withLocals(Set<Local> locals) {
    return new Body(locals, traps, stmts, position);
  }

  @Nonnull
  public Body withTraps(List<Trap> traps) {
    return new Body(locals, traps, stmts, position);
  }

  @Nonnull
  public Body withStmts(List<Stmt> stmts) {
    return new Body(locals, traps, stmts, position);
  }

  @Nonnull
  public Body withPosition(Position position) {
    return new Body(locals, traps, stmts, position);
  }
}
