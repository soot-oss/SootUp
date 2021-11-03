package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 * 
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 * 
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

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.ref.JFieldRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.core.JavaSootClass;
import soot.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**

 */
public class DexRefsChecker extends DexTransformer {
  // Note: we need an instance variable for inner class access, treat this as
  // a local variable (including initialization before use)

  public static DexRefsChecker v() {
    return new DexRefsChecker();
  }

  Local l = null;

  @Override
  public void interceptBody(final Body.BodyBuilder bodyBuilder) {
    // final ExceptionalStmtGraph g = new ExceptionalStmtGraph(bodyBuilder);
    // final SmartLocalDefs localDefs = new SmartLocalDefs(g, new
    // SimpleLiveLocals(g));
    // final SimpleLocalUses localUses = new SimpleLocalUses(g, localDefs);

    for (Stmt stmt : getRefCandidates(bodyBuilder)) {
      boolean hasField = false;
      JFieldRef jFieldRef = null;
      SootField sootField = null;
      if (stmt.containsFieldRef()) {
        jFieldRef = stmt.getFieldRef();
        sootField = jFieldRef.getField();
        if (sootField != null) {
          hasField = true;
        }
      } else {
        throw new RuntimeException("Stmt '" + stmt + "' does not contain array ref nor field ref.");
      }

      if (!hasField) {
        System.out.println("Warning: add missing field '" + jFieldRef + "' to class!");
        JavaSootClass sc = null;
        String frStr = jFieldRef.toString();
        if (frStr.contains(".<")) {
          sc = Scene.v().getSootClass(frStr.split(".<")[1].split(" ")[0].split(":")[0]);
        } else {
          sc = Scene.v().getSootClass(frStr.split(":")[0].replaceAll("^<", ""));
        }
        String fname = jFieldRef.toString().split(">")[0].split(" ")[2];
        int modifiers = soot.Modifier.PUBLIC;
        Type ftype = jFieldRef.getType();
        sc.addField(Scene.v().makeSootField(fname, ftype, modifiers));
      } else {
        // System.out.println("field "+ sootField.getName() +" '"+ sootField +"'
        // phantom: "+ isPhantom +" declared: "+ isDeclared);
      }

    } // for if statements
  }

  /**
   * Collect all the if statements comparing two locals with an Eq or Ne expression
   *
   * @param bodyBuilder
   *          the bodyBuilder to analyze
   */
  private Set<Stmt> getRefCandidates(Body.BodyBuilder bodyBuilder) {
    Set<Stmt> candidates = new HashSet<Stmt>();
    Iterator<Stmt> i = bodyBuilder.getStmts().iterator();
    while (i.hasNext()) {
      Stmt s = i.next();
      if (s.containsFieldRef()) {
        candidates.add(s);
      }
    }
    return candidates;
  }

}
