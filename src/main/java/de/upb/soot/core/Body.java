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

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.Trap;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.ref.JThisRef;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.util.EscapedWriter;
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

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that models the Jimple body (code attribute) of a method.
 * 
 * Modified by Linghui Luo
 *
 */
public class Body implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -755840890323977315L;

  private Position position;

  private static final Logger logger = LoggerFactory.getLogger(Body.class);
  /** The method associated with this Body. */
  protected transient SootMethod method = null;

  /** The locals for this Body. */
  protected LinkedHashSet<Local> locals = new LinkedHashSet<Local>();

  /** The traps for this Body. */
  protected LinkedHashSet<Trap> traps = new LinkedHashSet<Trap>();

  /** The stmts for this Body. */
  protected LinkedHashSet<IStmt> stmts = new LinkedHashSet<IStmt>();

  private static BodyValidator[] validators;

  /**
   * Returns an array containing some validators in order to validate the JimpleBody
   *
   * @return the array containing validators
   */
  private synchronized static BodyValidator[] getValidators() {
    if (validators == null) {
      validators = new BodyValidator[] { LocalsValidator.getInstance(), TrapsValidator.getInstance(),
          StmtBoxesValidator.getInstance(), UsesValidator.getInstance(), ValueBoxesValidator.getInstance(),
          // CheckInitValidator.getInstance(),
          CheckTypesValidator.getInstance(), CheckVoidLocalesValidator.getInstance(), CheckEscapingValidator.getInstance() };
    }
    return validators;
  };

  /**
   * Creates a Body associated to the given method. Used by subclasses during initialization. Creation of a Body is triggered
   * by e.g. Jimple.getInstance().newBody(options).
   */
  public Body(SootMethod m) {
    this.method = m;
  }

  /** Creates an extremely empty Body. The Body is not associated to any method. */
  protected Body() {
  }

  /**
   * Returns the method associated with this Body.
   *
   * @return the method that owns this body.
   */
  public SootMethod getMethod() {
    if (method == null) {
      throw new RuntimeException("no method associated w/ body");
    }
    return method;
  }

  /**
   * Sets the method associated with this Body.
   *
   * @param method
   *          the method that owns this body.
   *
   */
  public void setMethod(SootMethod method) {
    this.method = method;
  }

  /** Returns the number of locals declared in this body. */
  public int getLocalCount() {
    return locals.size();
  }

  /** Copies the contents of the given Body into this one. */
  public Map<Object, Object> importBodyContentsFrom(Body b) {
    HashMap<Object, Object> bindings = new HashMap<Object, Object>();
    {
      // Clone units in body's statement list
      for (IStmt original : b.getStmts()) {
        IStmt copy = original.clone();

        // Add cloned unit to our unitChain.
        stmts.add(copy);

        // Build old <-> new map to be able to patch up references to other units
        // within the cloned units. (these are still refering to the original
        // unit objects).
        bindings.put(original, copy);
      }
    }

    {
      // Clone trap units.
      for (Trap original : b.getTraps()) {
        Trap copy = (Trap) original.clone();

        // Add cloned unit to our trap list.
        traps.add(copy);

        // Store old <-> new mapping.
        bindings.put(original, copy);
      }
    }

    {
      // Clone local units.
      for (Local original : b.getLocals()) {
        Local copy = (Local) original.clone();

        // Add cloned unit to our trap list.
        locals.add(copy);

        // Build old <-> new mapping.
        bindings.put(original, copy);
      }
    }
    return bindings;
  }

  protected void runValidation(BodyValidator validator) {
    final List<ValidationException> exceptionList = new ArrayList<ValidationException>();
    validator.validate(this, exceptionList);
    if (!exceptionList.isEmpty()) {
      throw exceptionList.get(0);
    }
  }

  /** Verifies that a ValueBox is not used in more than one place. */
  public void validateValueBoxes() {
    runValidation(ValueBoxesValidator.getInstance());
  }

  /** Verifies that each Local of getUseAndDefBoxes() is in this body's locals Chain. */
  public void validateLocals() {
    runValidation(LocalsValidator.getInstance());
  }

  /** Verifies that the begin, end and handler units of each trap are in this body. */
  public void validateTraps() {
    runValidation(TrapsValidator.getInstance());
  }

  /** Verifies that the StmtBoxes of this Body all point to a Stmt contained within this body. */
  public void validateStmtBoxes() {
    runValidation(StmtBoxesValidator.getInstance());
  }

  /** Verifies that each use in this Body has a def. */
  public void validateUses() {
    runValidation(UsesValidator.getInstance());
  }

  /** Returns a backed chain of the locals declared in this Body. */
  public LinkedHashSet<Local> getLocals() {
    return locals;
  }

  /** Returns a backed view of the traps found in this Body. */
  public LinkedHashSet<Trap> getTraps() {
    return traps;
  }

  /** Return unit containing the \@this-assignment **/
  public IStmt getThisStmt() {
    for (IStmt u : getStmts()) {
      if (u instanceof JIdentityStmt && ((JIdentityStmt) u).getRightOp() instanceof JThisRef) {
        return u;
      }
    }

    throw new RuntimeException("couldn't find this-assignment!" + " in " + getMethod());
  }

  /** Return LHS of the first identity stmt assigning from \@this. **/
  public Local getThisLocal() {
    return (Local) (((JIdentityStmt) getThisStmt()).getLeftOp());
  }

  /** Return LHS of the first identity stmt assigning from \@parameter i. **/
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
   * @return a list of size as per <code>getMethod().getParameterCount()</code> with all elements ordered as per the
   *         parameter index.
   * @throws RuntimeException
   *           if a JParameterRef is missing
   */
  public List<Local> getParameterLocals() {
    final int numParams = getMethod().getParameterCount();
    final List<Local> retVal = new ArrayList<Local>(numParams);

    // Parameters are zero-indexed, so the keeping of the index is safe
    for (IStmt u : getStmts()) {
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
    return retVal;
  }

  /**
   * Returns the list of parameter references used in this body. The list is as long as the number of parameters declared in
   * the associated method's signature. The list may have <code>null</code> entries for parameters not referenced in the
   * body. The returned list is of fixed size.
   */
  public List<Value> getParameterRefs() {
    Value[] res = new Value[getMethod().getParameterCount()];
    for (IStmt s : getStmts()) {
      if (s instanceof JIdentityStmt) {
        Value rightOp = ((JIdentityStmt) s).getRightOp();
        if (rightOp instanceof JParameterRef) {
          JParameterRef parameterRef = (JParameterRef) rightOp;
          res[parameterRef.getIndex()] = parameterRef;
        }
      }
    }
    return Arrays.asList(res);
  }

  /**
   * Returns the Chain of Stmts that make up this body. The units are returned as a PatchingChain. The client can then
   * manipulate the chain, adding and removing units, and the changes will be reflected in the body. Since a PatchingChain is
   * returned the client need <i>not</i> worry about removing exception boundary units or otherwise corrupting the chain.
   *
   * @return the units in this Body
   *
   *         see PatchingChain
   * @see Stmt
   */
  public LinkedHashSet<IStmt> getStmts() {
    return stmts;
  }

  public void checkInit() {
    runValidation(CheckInitValidator.getInstance());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
    PrintWriter writerOut = new PrintWriter(new EscapedWriter(new OutputStreamWriter(streamOut)));
    try {
      new Printer().printTo(this, writerOut);
    } catch (RuntimeException e) {
      logger.error(e.getMessage(), e);
    }
    writerOut.flush();
    writerOut.close();
    return streamOut.toString();
  }

  public void addStmt(IStmt stmt) {
    this.stmts.add(stmt);
  }

  public void setPosition(Position position) {
    this.position = position;
  }

  public Position getPosition() {
    return this.position;
  }

  private Local getLocal(int idx) {
    Local local = null;
    // TODO

    return local;
  }

  /** Clones the current body, making deep copies of the contents. */
  @Override
  public Object clone() {
    Body b = new Body(this.method);
    b.importBodyContentsFrom(this);
    return b;
  }

  /**
   * Make sure that the JimpleBody is well formed. If not, throw an exception. Right now, performs only a handful of checks.
   */
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
  public void validate(List<ValidationException> exceptionList) {
    validate(exceptionList);
    final boolean runAllValidators
        = this.method.getView().getOptions().debug() || this.method.getView().getOptions().validate();
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
    final LinkedHashSet<IStmt> stmts = getStmts();
    final LinkedHashSet<Local> locals = getLocals();
    IStmt lastStmt = null;

    // add this-ref before everything else
    if (!getMethod().isStatic()) {
      if (declaringClass == null) {
        throw new IllegalArgumentException(
            String.format("No declaring class given for method %s", method.getSubSignature()));
      }
      Local l = Jimple.newLocal("this", RefType.getInstance(declaringClass));
      IStmt s = Jimple.newIdentityStmt(l, Jimple.newThisRef((RefType) l.getType()));

      locals.add(l);
      /*
       * TODO: check Stmt problems unitChain.addFirst(s); lastStmt = s;
       */
    }

    int i = 0;
    for (Type t : getMethod().getParameterTypes()) {
      Local l = Jimple.newLocal("parameter" + i, t);
      IStmt s = Jimple.newIdentityStmt(l, Jimple.newParameterRef(l.getType(), i));
      // TODO: check: Stmt problems
      /*
       * localChain.add(l); if (lastStmt == null) { unitChain.addFirst(s); } else { unitChain.insertAfter(s, lastStmt); }
       * lastStmt = s;
       */
      i++;
    }
  }

  /** Returns the first non-identity stmt in this body. */
  public IStmt getFirstNonIdentityStmt() {
    Iterator<IStmt> it = getStmts().iterator();
    Object o = null;
    while (it.hasNext()) {
      if (!((o = it.next()) instanceof JIdentityStmt)) {
        break;
      }
    }
    if (o == null) {
      throw new RuntimeException("no non-id statements!");
    }
    return (IStmt) o;
  }

  public List<ValueBox> getUseBoxes() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<ValueBox> getDefBoxes() {
    // TODO Auto-generated method stub
    return null;
  }

  public List<StmtBox> getAllStmtBoxes() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Add local to locals.
   * 
   * @param local
   */
  public void addLocal(Local local) {
    this.getLocals().add(local);
  }
}
