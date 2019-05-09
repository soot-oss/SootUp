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

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.jimple.basic.IStmtBox;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.Trap;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.ref.JThisRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.util.EscapedWriter;
import de.upb.soot.util.Utils;
import de.upb.soot.util.printer.Printer;
import de.upb.soot.validation.BodyValidator;
import de.upb.soot.validation.CheckEscapingValidator;
import de.upb.soot.validation.CheckInitValidator;
import de.upb.soot.validation.CheckTypesValidator;
import de.upb.soot.validation.CheckVoidLocalesValidator;
import de.upb.soot.validation.IdentityStatementsValidator;
import de.upb.soot.validation.LocalsValidator;
import de.upb.soot.validation.StmtBoxesValidator;
import de.upb.soot.validation.TrapsValidator;
import de.upb.soot.validation.UsesValidator;
import de.upb.soot.validation.ValidationException;
import de.upb.soot.validation.ValueBoxesValidator;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class that models the Jimple body (code attribute) of a methodRef.
 *
 * @author Linghui Luo
 */
public class Body implements Serializable {
  /** */
  private static final long serialVersionUID = -755840890323977315L;

  /** The locals for this Body. */
  protected final List<Local> locals;

  /** The traps for this Body. */
  protected final List<Trap> traps;

  /** The stmts for this Body. */
  protected final List<IStmt> stmts;

  @Nullable private final Position position;

  /** An array containing some validators in order to validate the JimpleBody */
  @Nonnull
  private static final List<BodyValidator> validators =
      Utils.immutableList(
          new LocalsValidator(),
          new TrapsValidator(),
          new StmtBoxesValidator(),
          new UsesValidator(),
          new ValueBoxesValidator(),
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
      @Nonnull List<Local> locals,
      @Nonnull List<Trap> traps,
      @Nonnull List<IStmt> stmts,
      @Nullable Position position) {
    this.locals = Collections.unmodifiableList(locals);
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
  protected synchronized void setMethod(@Nullable SootMethod value) {
    if (this._method != null) {
      throw new IllegalStateException(
          "The declaring class of this soot class member has already been set.");
    }

    this._method = value;
  }

  /** Returns the number of locals declared in this body. */
  public int getLocalCount() {
    return locals.size();
  }

  protected void runValidation(BodyValidator validator) {
    final List<ValidationException> exceptionList = new ArrayList<>();
    validator.validate(this, exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /** Verifies that a ValueBox is not used in more than one place. */
  public void validateValueBoxes() {
    runValidation(new ValueBoxesValidator());
  }

  /** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
  public void validateLocals() {
    runValidation(new LocalsValidator());
  }

  /** Verifies that the begin, end and handler units of each trap are in this body. */
  public void validateTraps() {
    runValidation(new TrapsValidator());
  }

  /** Verifies that the StmtBoxes of this Body all point to a Stmt contained within this body. */
  public void validateStmtBoxes() {
    runValidation(new StmtBoxesValidator());
  }

  /** Verifies that each use in this Body has a def. */
  public void validateUses() {
    runValidation(new UsesValidator());
  }

  /** Returns a backed chain of the locals declared in this Body. */
  public Collection<Local> getLocals() {
    return locals;
  }

  /** Returns a backed view of the traps found in this Body. */
  public Collection<Trap> getTraps() {
    return traps;
  }

  /** Return unit containing the \@this-assignment * */
  public IStmt getThisStmt() {
    for (IStmt u : getStmts()) {
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
    for (IStmt s : getStmts()) {
      if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getRightOp() instanceof JParameterRef) {
        JIdentityStmt is = (JIdentityStmt) s;
        JParameterRef pr = (JParameterRef) is.getRightOp();
        if (pr.getIndex() == i) {
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
    for (IStmt u : stmts) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt is = (JIdentityStmt) u;
        if (is.getRightOp() instanceof JParameterRef) {
          JParameterRef pr = (JParameterRef) is.getRightOp();
          retVal.add(pr.getIndex(), (Local) is.getLeftOp());
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
  public List<IStmt> getStmts() {
    return Collections.unmodifiableList(stmts);
  }

  public void checkInit() {
    runValidation(new CheckInitValidator());
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    PrintWriter writerOut = new PrintWriter(new EscapedWriter(new OutputStreamWriter(streamOut)));
    try {
      new Printer().printTo(this, writerOut);
    } catch (RuntimeException e) {
      throw new RuntimeException();
    }
    writerOut.flush();
    writerOut.close();
    return streamOut.toString();
  }

  public Position getPosition() {
    return this.position;
  }

  /** Clones the current body, making deep copies of the contents. */
  @Override
  public Object clone() {
    return new Body(this.locals, this.traps, this.stmts, this.position);
  }

  // FIXME "This code does not work and has to be adapted in future features."
  //   https://github.com/secure-software-engineering/soot-reloaded/pull/89#discussion_r267259693
  //
  //  /**
  //   * Make sure that the JimpleBody is well formed. If not, throw an exception. Right now,
  // performs only a handful of checks.
  //   */
  //  public void validate() {
  //    final List<ValidationException> exceptionList = new ArrayList<>();
  //    validate(exceptionList);
  //    if (!exceptionList.isEmpty()) {
  //      throw exceptionList.get(0);
  //    }
  //  }

  //  /**
  //   * Validates the jimple body and saves a list of all validation errors
  //   *
  //   * @param exceptionList
  //   *          the list of validation errors
  //   */
  //  public void validate(List<ValidationException> exceptionList) {
  //    validate(exceptionList);
  //    final boolean runAllValidators
  //        = this.method.getView().getOptions().debug() ||
  // this.method.getView().getOptions().validate();
  //    for (BodyValidator validator : validators) {
  //      if (!validator.isBasicValidator() && !runAllValidators) {
  //        continue;
  //      }
  //      validator.validate(this, exceptionList);
  //    }
  //  }

  public void validateIdentityStatements() {
    runValidation(new IdentityStatementsValidator());
  }

  /** Returns the first non-identity stmt in this body. */
  public IStmt getFirstNonIdentityStmt() {
    Iterator<IStmt> it = getStmts().iterator();
    IStmt o = null;
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

  public Collection<ValueBox> getUseBoxes() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  public Collection<ValueBox> getDefBoxes() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  /**
   * Returns the result of iterating through all Stmts in this body and querying them for their
   * StmtBoxes. All StmtBoxes thus found are returned. Branching Stmts and statements which use
   * PhiExpr will have StmtBoxes; a StmtBox contains a Stmt that is either a target of a branch or
   * is being used as a pointer to the end of a CFG block.
   *
   * <p>This methodRef is typically used for pointer patching, e.g. when the unit chain is cloned.
   *
   * @return A collection of all the StmtBoxes held by this body's units.
   */
  public Collection<IStmtBox> getAllStmtBoxes() {
    List<IStmtBox> stmtBoxList = new ArrayList<>();
    for (IStmt item : stmts) {
      stmtBoxList.addAll(item.getStmtBoxes());
    }

    for (Trap item : traps) {
      stmtBoxList.addAll(item.getStmtBoxes());
    }
    return Collections.unmodifiableCollection(stmtBoxList);
  }
}
