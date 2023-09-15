/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.test.context;

import org.junit.Test;
import qilin.test.util.JunitTests;

public class CollectionsTests extends JunitTests {
    @Test
    public void testArrayList0() {
        checkAssertions(run("qilin.microben.context.collections.ArrayList0", "2o"));
    }

    @Test
    public void testLinkedList0() {
        checkAssertions(run("qilin.microben.context.collections.LinkedList0", "2o"));
    }

    @Test
    public void testVector0() {
        checkAssertions(run("qilin.microben.context.collections.Vector0", "2o"));
    }

    @Test
    public void testHashMap0() {
        checkAssertions(run("qilin.microben.context.collections.HashMap0", "2o"));
    }

    @Test
    public void testTreeMap0() {
        checkAssertions(run("qilin.microben.context.collections.TreeMap0", "2o"));
    }

    @Test
    public void testHashSet0() {
        checkAssertions(run("qilin.microben.context.collections.HashSet0", "3o"));
    }

    @Test
    public void testTreeSet0() {
        checkAssertions(run("qilin.microben.context.collections.TreeSet0", "3o"));
    }

    @Test
    public void testHashTable0() {
        checkAssertions(run("qilin.microben.context.collections.HashTable0", "2o"));
    }

    @Test
    public void testPriorityQueue0() {
        checkAssertions(run("qilin.microben.context.collections.PriorityQueue0", "2o"));
    }

    @Test
    public void testStack0() {
        checkAssertions(run("qilin.microben.context.collections.Stack0", "2o"));
    }
}