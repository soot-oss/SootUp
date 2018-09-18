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
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.StmtBox;
import de.upb.soot.jimple.basic.Trap;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.basic.ValueBox;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.ref.JThisRef;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.Stmt;
import de.upb.soot.util.Chain;
import de.upb.soot.util.EscapedWriter;
import de.upb.soot.util.HashChain;
import de.upb.soot.util.Printer;
import de.upb.soot.validation.BodyValidator;
import de.upb.soot.validation.CheckEscapingValidator;
import de.upb.soot.validation.CheckInitValidator;
import de.upb.soot.validation.CheckTypesValidator;
import de.upb.soot.validation.CheckVoidLocalesValidator;
import de.upb.soot.validation.LocalsValidator;
import de.upb.soot.validation.TrapsValidator;
import de.upb.soot.validation.UsesValidator;
import de.upb.soot.validation.ValidationException;
import de.upb.soot.validation.ValueBoxesValidator;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that models the body (code attribute) of a method.
 * 
 * @author Linghui Luo
 *
 */
@SuppressWarnings("serial")
public class Body implements Serializable {



  // TODO: remove legacy inner class dummy stubs
  class Unit extends JIdentityStmt {

    public Unit(Value local, Value identityValue) {
      super(local, identityValue);
    }

    @Override
    public Unit clone() {
      return null;
    }

    public void addAllTagsOf(Unit original) {
    }

    @Override
    public List<StmtBox> getUnitBoxes() {
      return null;
    }

  }

  class UnitBox {
    public UnitBox() {

    }

    public Unit getUnit() {
      return null;
    }

    public void setUnit(Unit newObject) {
    }

  }

  public enum UnitBoxesValidator implements BodyValidator {
    INSTANCE;

    public static UnitBoxesValidator getInstance() {
      return INSTANCE;
    }

    @Override
    /** Verifies that the UnitBoxes of this Body all point to a Unit contained within this body. */
    public void validate(Body body, List<ValidationException> exception) {
    }

    @Override
    public boolean isBasicValidator() {
      return true;
    }
  }

  public class UnitPatchingChain implements Chain<Body.Unit> {
    public UnitPatchingChain(HashChain<Body.Unit> units) {
    }

    @Override
    public void insertBefore(List<Unit> toInsert, Unit point) {

    }

    @Override
    public void insertAfter(List<Unit> toInsert, Unit point) {

    }

    @Override
    public void insertAfter(Unit toInsert, Unit point) {

    }

    @Override
    public void insertAfter(Collection<? extends Unit> toInsert, Unit point) {

    }

    @Override
    public void insertBefore(Unit toInsert, Unit point) {

    }

    @Override
    public void insertBefore(Collection<? extends Unit> toInsert, Unit point) {

    }

    @Override
    public void insertBefore(Chain<Unit> toInsert, Unit point) {

    }

    @Override
    public void insertAfter(Chain<Unit> toInsert, Unit point) {

    }

    @Override
    public void swapWith(Unit out, Unit in) {

    }

    @Override
    public boolean remove(Object u) {
      return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
      return false;
    }

    @Override
    public void addFirst(Unit u) {
    }

    @Override
    public boolean addAll(Collection<? extends Unit> c) {
      return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
      return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
      return false;
    }

    @Override
    public void clear() {
    }

    @Override
    public void addLast(Unit copy) {
    }

    @Override
    public void removeFirst() {
    }

    @Override
    public void removeLast() {
    }

    @Override
    public boolean follows(Unit someObject, Unit someReferenceObject) {
      return false;
    }

    @Override
    public Unit getFirst() {
      return null;
    }

    @Override
    public Unit getLast() {
      return null;
    }

    @Override
    public Unit getSuccOf(Unit point) {
      return null;
    }

    @Override
    public Unit getPredOf(Unit point) {
      return null;
    }

    @Override
    public Iterator<Unit> snapshotIterator() {
      return null;
    }

    @Override
    public Iterator<Unit> iterator() {
      return null;
    }

    @Override
    public Object[] toArray() {
      return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
      return null;
    }

    @Override
    public boolean add(Unit unit) {
      return false;
    }

    @Override
    public Iterator<Unit> iterator(Unit u) {
      return null;
    }

    @Override
    public Iterator<Unit> iterator(Unit head, Unit tail) {
      return null;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public boolean contains(Object o) {
      return false;
    }

    @Override
    public long getModificationCount() {
      return 0;
    }

    @Override
    public Collection<Unit> getElementsUnsorted() {
      return null;
    }

  }

  /* End of legacy dummy classes */




  private static final Logger logger = LoggerFactory.getLogger(Body.class);
  /** The method associated with this Body. */
  protected transient SootMethod method = null;

  /** The chain of locals for this Body. */
  protected Chain<Local> localChain = new HashChain<Local>();

  /** The chain of traps for this Body. */
  protected Chain<Trap> trapChain = new HashChain<Trap>();

  /** The chain of units for this Body. */
  protected UnitPatchingChain unitChain = new UnitPatchingChain(new HashChain<Unit>());

  private static BodyValidator[] validators;

  /** Creates a deep copy of this Body. */
  @Override
  public Body clone() {

    // TODO: needs implementation (abstract removed)
    return null;

  }

  /**
   * Returns an array containing some validators in order to validate the JimpleBody
   *
   * @return the array containing validators
   */
  private synchronized static BodyValidator[] getValidators() {
    if (validators == null) {
      validators = new BodyValidator[] { LocalsValidator.getInstance(), TrapsValidator.getInstance(),
          UnitBoxesValidator.getInstance(), UsesValidator.getInstance(), ValueBoxesValidator.getInstance(),
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
    return localChain.size();
  }

  /** Copies the contents of the given Body into this one. */
  public Map<Object, Object> importBodyContentsFrom(Body b) {
    HashMap<Object, Object> bindings = new HashMap<Object, Object>();

    {
      // Clone units in body's statement list
      for (Unit original : b.getUnits()) {
        Unit copy = original.clone();

        copy.addAllTagsOf(original);

        // Add cloned unit to our unitChain.
        unitChain.addLast(copy);

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
        trapChain.addLast(copy);

        // Store old <-> new mapping.
        bindings.put(original, copy);
      }
    }

    {
      // Clone local units.
      for (Local original : b.getLocals()) {
        Local copy = (Local) original.clone();

        // Add cloned unit to our trap list.
        localChain.addLast(copy);

        // Build old <-> new mapping.
        bindings.put(original, copy);
      }
    }

    {
      // Patch up references within units using our (old <-> new) map.
      for (UnitBox box : getAllUnitBoxes()) {
        Unit newObject, oldObject = box.getUnit();

        // if we have a reference to an old object, replace it
        // it's clone.
        if ((newObject = (Unit) bindings.get(oldObject)) != null) {
          box.setUnit(newObject);
        }

      }
    }

    {
      // backpatching all local variables.
      for (ValueBox vb : getUseBoxes()) {
        if (vb.getValue() instanceof Local) {
          vb.setValue((Value) bindings.get(vb.getValue()));
        }
      }
      for (ValueBox vb : getDefBoxes()) {
        if (vb.getValue() instanceof Local) {
          vb.setValue((Value) bindings.get(vb.getValue()));
        }
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

  /** Verifies a few sanity conditions on the contents on this body. */
  public void validate() {
    List<ValidationException> exceptionList = new ArrayList<ValidationException>();
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
    final boolean runAllValidators = Options.getInstance().debug() || Options.getInstance().validate();
    for (BodyValidator validator : getValidators()) {
      if (!validator.isBasicValidator() && !runAllValidators) {
        continue;
      }
      validator.validate(this, exceptionList);
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

  /** Verifies that the UnitBoxes of this Body all point to a Unit contained within this body. */
  public void validateUnitBoxes() {
    runValidation(UnitBoxesValidator.getInstance());
  }

  /** Verifies that each use in this Body has a def. */
  public void validateUses() {
    runValidation(UsesValidator.getInstance());
  }

  /** Returns a backed chain of the locals declared in this Body. */
  public Chain<Local> getLocals() {
    return localChain;
  }

  /** Returns a backed view of the traps found in this Body. */
  public Chain<Trap> getTraps() {
    return trapChain;
  }

  /** Return unit containing the \@this-assignment **/
  public Unit getThisUnit() {
    for (Unit u : getUnits()) {
      if (u instanceof JIdentityStmt && ((JIdentityStmt) u).getRightOp() instanceof JThisRef) {
        return u;
      }
    }

    throw new RuntimeException("couldn't find this-assignment!" + " in " + getMethod());
  }

  /** Return LHS of the first identity stmt assigning from \@this. **/
  public Local getThisLocal() {
    return (Local) (((JIdentityStmt) getThisUnit()).getLeftOp());
  }

  /** Return LHS of the first identity stmt assigning from \@parameter i. **/
  public Local getParameterLocal(int i) {
    for (Unit s : getUnits()) {
      if (s instanceof JIdentityStmt && ((JIdentityStmt) s).getRightOp() instanceof JParameterRef) {
        JIdentityStmt is = s;
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
    for (Unit u : getUnits()) {
      if (u instanceof JIdentityStmt) {
        JIdentityStmt is = (u);
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
    for (Unit s : getUnits()) {
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
   * Returns the Chain of Units that make up this body. The units are returned as a PatchingChain. The client can then
   * manipulate the chain, adding and removing units, and the changes will be reflected in the body. Since a PatchingChain is
   * returned the client need <i>not</i> worry about removing exception boundary units or otherwise corrupting the chain.
   *
   * @return the units in this Body
   *
   *         see PatchingChain
   * @see Unit
   */
  public UnitPatchingChain getUnits() {
    return unitChain;
  }

  /**
   * Returns the result of iterating through all Units in this body and querying them for their UnitBoxes. All UnitBoxes thus
   * found are returned. Branching Units and statements which use PhiExpr will have UnitBoxes; a UnitBox contains a Unit that
   * is either a target of a branch or is being used as a pointer to the end of a CFG block.
   *
   * <p>
   * This method is typically used for pointer patching, eg when the unit chain is cloned.
   *
   * @return A list of all the UnitBoxes held by this body's units.
   * @see UnitBox
   * @see #getUnitBoxes(boolean)
   * @see Unit#getUnitBoxes() see soot.shimple.PhiExpr#getUnitBoxes()
   **/
  public List<UnitBox> getAllUnitBoxes() {

    // TODO: check code from old soot
    return null;
    /*
     * ArrayList<UnitBox> unitBoxList = new ArrayList<UnitBox>(); { Iterator<Unit> it = unitChain.iterator(); while
     * (it.hasNext()) { Unit item = it.next(); unitBoxList.addAll(item.getUnitBoxes()); } }
     * 
     * { Iterator<Trap> it = trapChain.iterator(); while (it.hasNext()) { Trap item = it.next();
     * unitBoxList.addAll(item.getUnitBoxes()); } }
     * 
     * { Iterator<Tag> it = getTags().iterator(); while (it.hasNext()) { Tag t = it.next(); if (t instanceof CodeAttribute) {
     * unitBoxList.addAll(((CodeAttribute) t).getUnitBoxes()); } } }
     * 
     * return unitBoxList;
     */
  }

  /**
   * If branchTarget is true, returns the result of iterating through all branching Units in this body and querying them for
   * their UnitBoxes. These UnitBoxes contain Units that are the target of a branch. This is useful for, say, labeling blocks
   * or updating the targets of branching statements.
   *
   * <p>
   * If branchTarget is false, returns the result of iterating through the non-branching Units in this body and querying them
   * for their UnitBoxes. Any such UnitBoxes (typically from PhiExpr) contain a Unit that indicates the end of a CFG block.
   *
   * @return a list of all the UnitBoxes held by this body's branching units.
   *
   * @see UnitBox
   * @see #getAllUnitBoxes()
   * @see Unit#getUnitBoxes() see soot.shimple.PhiExpr#getUnitBoxes()
   **/

  public List<UnitBox> getUnitBoxes(boolean branchTarget) {
    // TODO: check code from old soot
    return null;

    /*
     * ArrayList<UnitBox> unitBoxList = new ArrayList<UnitBox>(); { Iterator<Unit> it = unitChain.iterator(); while
     * (it.hasNext()) { Unit item = it.next(); if (branchTarget) { if (item.branches()) {
     * unitBoxList.addAll(item.getUnitBoxes()); } } else { if (!item.branches()) { unitBoxList.addAll(item.getUnitBoxes()); }
     * } } }
     * 
     * { Iterator<Trap> it = trapChain.iterator(); while (it.hasNext()) { Trap item = it.next();
     * unitBoxList.addAll(item.getUnitBoxes()); } }
     * 
     * { Iterator<Tag> it = getTags().iterator(); while (it.hasNext()) { Tag t = it.next(); if (t instanceof CodeAttribute) {
     * unitBoxList.addAll(((CodeAttribute) t).getUnitBoxes()); } } }
     * 
     * return unitBoxList;
     */
  }

  /**
   * Returns the result of iterating through all Units in this body and querying them for ValueBoxes used. All of the
   * ValueBoxes found are then returned as a List.
   *
   * @return a list of all the ValueBoxes for the Values used this body's units.
   *
   * @see Value
   * @see Unit#getUseBoxes
   * @see ValueBox
   * @see Value
   *
   */
  public List<ValueBox> getUseBoxes() {
    ArrayList<ValueBox> useBoxList = new ArrayList<ValueBox>();

    Iterator<Unit> it = unitChain.iterator();
    while (it.hasNext()) {
      Unit item = it.next();
      useBoxList.addAll(item.getUseBoxes());
    }
    return useBoxList;
  }

  /**
   * Returns the result of iterating through all Units in this body and querying them for ValueBoxes defined. All of the
   * ValueBoxes found are then returned as a List.
   *
   * @return a list of all the ValueBoxes for Values defined by this body's units.
   *
   * @see Value
   * @see Unit#getDefBoxes
   * @see ValueBox
   * @see Value
   */
  public List<ValueBox> getDefBoxes() {
    ArrayList<ValueBox> defBoxList = new ArrayList<ValueBox>();

    Iterator<Unit> it = unitChain.iterator();
    while (it.hasNext()) {
      Unit item = it.next();
      defBoxList.addAll(item.getDefBoxes());
    }
    return defBoxList;
  }

  /**
   * Returns a list of boxes corresponding to Values either used or defined in any unit of this Body.
   *
   * @return a list of ValueBoxes for held by the body's Units.
   *
   * @see Value
   * @see Unit#getUseAndDefBoxes
   * @see ValueBox
   * @see Value
   */
  public List<ValueBox> getUseAndDefBoxes() {
    ArrayList<ValueBox> useAndDefBoxList = new ArrayList<ValueBox>();

    Iterator<Unit> it = unitChain.iterator();
    while (it.hasNext()) {
      Unit item = it.next();
      useAndDefBoxList.addAll(item.getUseBoxes());
      useAndDefBoxList.addAll(item.getDefBoxes());
    }
    return useAndDefBoxList;
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
      Printer.getInstance().printTo(this, writerOut);
    } catch (RuntimeException e) {
      logger.error(e.getMessage(), e);
    }
    writerOut.flush();
    writerOut.close();
    return streamOut.toString();
  }

  public long getModificationCount() {
    return localChain.getModificationCount() + unitChain.getModificationCount() + trapChain.getModificationCount();
  }

  public void addStmt(Stmt stmt) {
    // TODO Auto-generated method stub

  }
}
