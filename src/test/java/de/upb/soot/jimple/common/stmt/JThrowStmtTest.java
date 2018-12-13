/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
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

package de.upb.soot.jimple.common.stmt;

import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class JThrowStmtTest {

  @Test
  public void test() {

    IView view = new JavaView(null);
    DefaultSignatureFactory factory = new DefaultSignatureFactory();

    Local local = new Local("$r0", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    Local localEqual = new Local("$r0", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    Local localDifferent = new Local("$r1", new RefType(view, factory.getTypeSignature("java.lang.Exception")));
    Local localDifferent2 = new Local("$r0", new RefType(view, factory.getTypeSignature("sompepackage.MyException")));

    IStmt tStmt = new JThrowStmt(local);

    // equivTo
    Assert.assertTrue(tStmt.equivTo(tStmt));
    Assert.assertTrue(tStmt.equivTo(new JThrowStmt(localEqual)));

    Assert.assertFalse(tStmt.equivTo(new JNopStmt()));
    Assert.assertFalse(tStmt.equivTo(new JThrowStmt(localDifferent)));
    Assert.assertFalse(tStmt.equivTo(new JThrowStmt(localDifferent2)));

    // toString
    Assert.assertEquals("throw $r0", tStmt.toString());
  }

}
