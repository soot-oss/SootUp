/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Navindra Umanee <navindra@cs.mcgill.ca>
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
 *  The original class name was UnitBoxOwner in soot, renamed by Linghui Luo, 22.06.2018
 */

package de.upb.soot.jimple.basic;

import java.util.List;

/**
 * An implementor of this interface indicates that it may contain UnitBoxes.
 * 
 * <p>
 * Currently this is implemented by soot.shimple.PhiExpr and used by soot.jimple.internal.JAssignStmt.
 * </p>
 *
 * @author Navindra Umanee
 **/
public interface StmtBoxOwner {

  List<IStmtBox> getStmtBoxes();

  void clearStmtBoxes();
}
