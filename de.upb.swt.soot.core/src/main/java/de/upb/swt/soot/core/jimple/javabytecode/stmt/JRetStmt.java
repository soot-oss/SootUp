/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.javabytecode.stmt;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractStmt;
import de.upb.swt.soot.core.jimple.visitor.StmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents the deprecated JVM <code>ret</code> statement */
public final class JRetStmt extends AbstractStmt implements Copyable {

  private final ValueBox stmtAddressBox;
  // List useBoxes;

  public JRetStmt(Value stmtAddress, StmtPositionInfo positionInfo) {
    this(Jimple.newImmediateBox(stmtAddress), positionInfo);
  }

  private JRetStmt(ValueBox stmtAddressBox, StmtPositionInfo positionInfo) {
    super(positionInfo);
    this.stmtAddressBox = stmtAddressBox;
  }

  @Override
  public String toString() {
    return Jimple.RET + " " + stmtAddressBox.getValue().toString();
  }

  @Override
  public void toString(StmtPrinter up) {
    up.literal(Jimple.RET);
    up.literal(" ");
    stmtAddressBox.toString(up);
  }

  public Value getStmtAddress() {
    return stmtAddressBox.getValue();
  }

  public ValueBox getStmtAddressBox() {
    return stmtAddressBox;
  }

  @Override
  public List<ValueBox> getUseBoxes() {

    List<ValueBox> useBoxes = new ArrayList<>(stmtAddressBox.getValue().getUseBoxes());
    useBoxes.add(stmtAddressBox);

    return useBoxes;
  }

  @Override
  public void accept(Visitor sw) {
    ((StmtVisitor) sw).caseRetStmt(this);
  }

  @Override
  public boolean fallsThrough() {
    return true;
  }

  @Override
  public boolean branches() {
    return false;
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseRetStmt(this, o);
  }

  @Override
  public int equivHashCode() {
    return stmtAddressBox.getValue().equivHashCode();
  }

  @Nonnull
  public JRetStmt withStmtAddress(Value stmtAddress) {
    return new JRetStmt(stmtAddress, getPositionInfo());
  }

  @Nonnull
  public JRetStmt withPositionInfo(StmtPositionInfo positionInfo) {
    return new JRetStmt(getStmtAddress(), positionInfo);
  }
}
