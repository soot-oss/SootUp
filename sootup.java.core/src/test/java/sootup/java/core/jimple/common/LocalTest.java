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

package sootup.java.core.jimple.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.IgnoreLocalNameComparator;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.SlotLocal;
import sootup.core.jimple.common.StackLocal;
import sootup.core.jimple.javabytecode.stmt.JBreakpointStmt;
import sootup.core.types.PrimitiveType;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.jimple.basic.JavaLocal;
import sootup.java.core.jimple.basic.JavaSlotLocal;
import sootup.java.core.jimple.basic.JavaStackLocal;
import sootup.java.core.language.JavaJimple;

public class LocalTest {

  @Test
  public void testEquivTo() {

    JimpleComparator comparator = new IgnoreLocalNameComparator();

    Local l1 = Jimple.newLocal("i1", PrimitiveType.getInt());
    Local l2 = Jimple.newLocal("i2", PrimitiveType.getInt());
    Local l3 = Jimple.newLocal("i1", PrimitiveType.getBoolean());

    assertTrue(l1.equivTo(l1));
    assertTrue(l1.equivTo(l1, comparator));

    assertFalse(l1.equivTo(l2));
    assertTrue(l1.equivTo(l2, comparator));

    assertFalse(l1.equivTo(l3));
    assertFalse(l1.equivTo(l3, comparator));

    assertFalse(l2.equivTo(l3));
    assertFalse(l2.equivTo(l3, comparator));

    assertFalse(
        l1.equivTo(new JBreakpointStmt(StmtPositionInfo.getNoStmtPositionInfo()), comparator));
  }

  @Test
  public void javaWithersPreserveProvenanceAndAnnotations() {
    var annotations =
        List.of(
            new AnnotationUsage(
                new JavaIdentifierFactory().getClassType("example.Annotation"), Map.of()));
    List<JavaLocal> locals =
        List.of(
            JavaJimple.newLocal("generic", PrimitiveType.getInt(), annotations),
            JavaJimple.newStackLocal("stack", PrimitiveType.getInt(), annotations),
            JavaJimple.newSlotLocal("slot", PrimitiveType.getInt(), 7, annotations));
    for (JavaLocal local : locals) {
      for (JavaLocal copy :
          List.of(local.withName("renamed"), local.withType(PrimitiveType.getFloat()))) {
        assertSame(local.getClass(), copy.getClass());
        assertSame(annotations, copy.getAnnotations());
        if (local instanceof SlotLocal) {
          assertEquals(7, ((SlotLocal) copy).getSlotIndex());
        }
      }
      JavaLocal copy = local.withAnnotations(List.of());
      assertSame(local.getClass(), copy.getClass());
      assertFalse(copy.getAnnotations().iterator().hasNext());
      if (copy instanceof SlotLocal) {
        assertEquals(7, ((SlotLocal) copy).getSlotIndex());
      }
    }
    var slot = JavaJimple.newSlotLocal("slot", PrimitiveType.getInt(), 7, annotations);
    assertEquals(8, slot.withSlotIndex(8).getSlotIndex());
    assertSame(annotations, slot.withSlotIndex(8).getAnnotations());
    assertEquals(Jimple.newLocal("slot", PrimitiveType.getInt()), slot);
    assertTrue(Jimple.newStackLocal("slot", PrimitiveType.getInt()).equivTo(slot));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void javaFactoriesValidateSlots() {
    assertInstanceOf(SlotLocal.class, JavaJimple.newLocal("a", PrimitiveType.getInt(), 0));
    JavaLocal generic = JavaJimple.newLocal("a", PrimitiveType.getInt(), -1);
    assertFalse(generic instanceof SlotLocal);
    assertFalse(generic instanceof StackLocal);
    assertThrows(
        IllegalArgumentException.class, () -> JavaJimple.newLocal("a", PrimitiveType.getInt(), -2));
    assertThrows(
        IllegalArgumentException.class,
        () -> JavaJimple.newSlotLocal("a", PrimitiveType.getInt(), -1));
    assertThrows(
        IllegalArgumentException.class,
        () -> JavaJimple.newSlotLocal("a", PrimitiveType.getInt(), 0).withSlotIndex(-1));
  }

  @Test
  public void combinedInterfacesSupportFluentCopies() {
    var annotations =
        List.of(
            new AnnotationUsage(
                new JavaIdentifierFactory().getClassType("example.Annotation"), Map.of()));
    JavaSlotLocal slot = JavaJimple.newSlotLocal("slot", PrimitiveType.getInt(), 2);
    JavaSlotLocal copy =
        slot.withAnnotations(annotations)
            .withSlotIndex(4)
            .withName("renamed")
            .withType(PrimitiveType.getFloat())
            .withIndex(6);
    assertEquals(6, copy.getSlotIndex());
    assertEquals("renamed", copy.getName());
    assertEquals(PrimitiveType.getFloat(), copy.getType());
    assertSame(annotations, copy.getAnnotations());
    assertEquals(2, slot.getSlotIndex());
    assertFalse(slot.getAnnotations().iterator().hasNext());

    JavaStackLocal stack = JavaJimple.newStackLocal("stack", PrimitiveType.getInt());
    JavaStackLocal stackCopy =
        stack.withAnnotations(annotations).withName("renamed").withType(PrimitiveType.getFloat());
    assertSame(annotations, stackCopy.getAnnotations());
    assertInstanceOf(StackLocal.class, stackCopy);
    assertFalse(stackCopy instanceof SlotLocal);
  }

  @Test
  public void copiesThroughEachInterfacePreserveOtherCapabilities() {
    var annotations =
        List.of(
            new AnnotationUsage(
                new JavaIdentifierFactory().getClassType("example.Annotation"), Map.of()));
    JavaSlotLocal original =
        JavaJimple.newSlotLocal("slot", PrimitiveType.getInt(), 7, annotations);
    Local localView = original;
    SlotLocal slotView = original;
    JavaLocal javaView = original;
    for (Local copy :
        List.of(
            localView.withName("fromLocal"),
            localView.withType(PrimitiveType.getFloat()),
            slotView.withName("fromSlot"),
            slotView.withType(PrimitiveType.getFloat()),
            javaView.withName("fromJava"),
            javaView.withType(PrimitiveType.getFloat()))) {
      JavaSlotLocal combined = assertInstanceOf(JavaSlotLocal.class, copy);
      assertEquals(7, combined.getSlotIndex());
      assertSame(annotations, combined.getAnnotations());
    }
    var changedSlot = assertInstanceOf(JavaSlotLocal.class, slotView.withSlotIndex(8));
    assertEquals(8, changedSlot.getSlotIndex());
    assertSame(annotations, changedSlot.getAnnotations());
    var changedAnnotations =
        assertInstanceOf(JavaSlotLocal.class, javaView.withAnnotations(List.of()));
    assertEquals(7, changedAnnotations.getSlotIndex());
    assertFalse(changedAnnotations.getAnnotations().iterator().hasNext());
  }
}
