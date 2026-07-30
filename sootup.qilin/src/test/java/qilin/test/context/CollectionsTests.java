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

import org.junit.jupiter.api.Test;
import qilin.core.config.ContextSensitivity;
import qilin.test.util.QilinFrameworkTests;

public class CollectionsTests extends QilinFrameworkTests {
  @Test
  public void testArrayList0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.ArrayList0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testLinkedList0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.LinkedList0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testVector0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.Vector0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testHashMap0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.HashMap0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testTreeMap0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.TreeMap0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testHashSet0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.HashSet0",
            ContextSensitivity.objectSensitive(3, 2)));
  }

  @Test
  public void testTreeSet0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.TreeSet0",
            ContextSensitivity.objectSensitive(3, 2)));
  }

  @Test
  public void testHashTable0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.HashTable0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testPriorityQueue0() {
    checkAssertions(
        run(
            "qilin.microben.context.collections.PriorityQueue0",
            ContextSensitivity.objectSensitive(2, 1)));
  }

  @Test
  public void testStack0() {
    checkAssertions(
        run("qilin.microben.context.collections.Stack0", ContextSensitivity.objectSensitive(2, 1)));
  }
}
