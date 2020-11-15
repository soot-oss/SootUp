package de.upb.swt.soot.java.bytecode.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020
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

import com.google.common.collect.Lists;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Zun Wang
 */

public class AssertUtils {

    //assert whether two stmtGraph have the same stmts
    public static void assertStmtGraphEquiv(StmtGraph expected, StmtGraph actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        final boolean condition = expected.equivTo(actual);
        if (!condition) {
            System.out.println("expected:");
            System.out.println(Lists.newArrayList(expected.iterator()));
            System.out.println("actual:");
            System.out.println(Lists.newArrayList(actual.iterator()) + "\n");

            for (Stmt s : expected) {
                System.out.println(s + " => " + expected.successors(s));
            }
            System.out.println();
            for (Stmt s : actual) {
                System.out.println(s + " => " + actual.successors(s));
            }
        }
        assertTrue(condition);
    }

}
