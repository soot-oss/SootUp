package sootup.tests.typehierarchy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.typehierarchy.HierarchyComparator;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.core.views.JavaView;
import sootup.java.frontend.inputlocation.JavaSourcePathAnalysisInputLocation;

@Tag("Java8")
public class HierarchyComparatorTest {

  private static View view;

  @BeforeAll
  public static void setUp() {
    List<AnalysisInputLocation> inputLocations = new ArrayList<>();
    inputLocations.add(
        new JavaSourcePathAnalysisInputLocation(
            Collections.singleton("src/test/resources/javatypehierarchy/Comparator")));
    inputLocations.add(new DefaultRuntimeAnalysisInputLocation());

    view = new JavaView(inputLocations);
  }

  @Test
  public void testHierarchyComparatorOnClasses() {
    ClassType superclass = view.getIdentifierFactory().getClassType("Superclass", "");
    ClassType subclass = view.getIdentifierFactory().getClassType("Subclass", "");
    ClassType subSubclass = view.getIdentifierFactory().getClassType("SubSubclass", "");

    HierarchyComparator hc = new HierarchyComparator(view.getTypeHierarchy());
    assertEquals(-1, hc.compare(subclass, superclass));
    assertEquals(-1, hc.compare(subSubclass, superclass));
    assertEquals(-1, hc.compare(subSubclass, subclass));
    assertEquals(1, hc.compare(superclass, subclass));
    assertEquals(1, hc.compare(superclass, subSubclass));
    assertEquals(1, hc.compare(subclass, subSubclass));

    ArrayList<ClassType> classes = new ArrayList<>();
    classes.add(superclass);
    classes.add(subSubclass);
    classes.add(subclass);
    List<ClassType> classesSorted = classes.stream().sorted(hc).collect(Collectors.toList());
    assertEquals(subSubclass, classesSorted.get(0));
    assertEquals(subclass, classesSorted.get(1));
    assertEquals(superclass, classesSorted.get(2));
  }

  @Test
  public void testHierarchyComparatorOnInterfaces() {
    ClassType Interface = view.getIdentifierFactory().getClassType("Interface", "");
    ClassType subInterface = view.getIdentifierFactory().getClassType("SubInterface", "");
    ClassType subSubInterface2 = view.getIdentifierFactory().getClassType("SubSubInterface", "");

    HierarchyComparator hc = new HierarchyComparator(view.getTypeHierarchy());
    assertEquals(-1, hc.compare(subInterface, Interface));
    assertEquals(-1, hc.compare(subSubInterface2, Interface));
    assertEquals(-1, hc.compare(subSubInterface2, subInterface));
    assertEquals(1, hc.compare(Interface, subInterface));
    assertEquals(1, hc.compare(Interface, subSubInterface2));
    assertEquals(1, hc.compare(subInterface, subSubInterface2));

    ArrayList<ClassType> classes = new ArrayList<>();
    classes.add(Interface);
    classes.add(subSubInterface2);
    classes.add(subInterface);
    List<ClassType> classesSorted = classes.stream().sorted(hc).collect(Collectors.toList());
    assertEquals(subSubInterface2, classesSorted.get(0));
    assertEquals(subInterface, classesSorted.get(1));
    assertEquals(Interface, classesSorted.get(2));
  }

  @Test
  public void testHierarchyComparatorOnMixed() {
    ClassType Interface = view.getIdentifierFactory().getClassType("Interface", "");
    ClassType subInterface = view.getIdentifierFactory().getClassType("SubInterface", "");
    ClassType subSubInterface = view.getIdentifierFactory().getClassType("SubSubInterface", "");
    ClassType superclass = view.getIdentifierFactory().getClassType("Superclass", "");
    ClassType subclass = view.getIdentifierFactory().getClassType("Subclass", "");
    ClassType subSubclass = view.getIdentifierFactory().getClassType("SubSubclass", "");

    HierarchyComparator hc = new HierarchyComparator(view.getTypeHierarchy());

    assertEquals(-1, hc.compare(subSubInterface, Interface));
    assertEquals(-1, hc.compare(subSubInterface, subInterface));
    assertEquals(1, hc.compare(subSubInterface, superclass));
    assertEquals(1, hc.compare(subSubInterface, subclass));
    assertEquals(1, hc.compare(subSubInterface, subSubclass));

    assertEquals(-1, hc.compare(subInterface, Interface));
    assertEquals(1, hc.compare(subInterface, subSubInterface));
    assertEquals(1, hc.compare(subInterface, superclass));
    assertEquals(1, hc.compare(subInterface, subclass));
    assertEquals(1, hc.compare(subInterface, subSubclass));

    assertEquals(1, hc.compare(Interface, subInterface));
    assertEquals(1, hc.compare(Interface, subSubInterface));
    assertEquals(1, hc.compare(Interface, superclass));
    assertEquals(1, hc.compare(Interface, subclass));
    assertEquals(1, hc.compare(Interface, subSubclass));

    assertEquals(-1, hc.compare(superclass, Interface));
    assertEquals(-1, hc.compare(superclass, subInterface));
    assertEquals(-1, hc.compare(superclass, subSubInterface));
    assertEquals(1, hc.compare(superclass, subclass));
    assertEquals(1, hc.compare(superclass, subSubclass));

    assertEquals(-1, hc.compare(subclass, Interface));
    assertEquals(-1, hc.compare(subclass, subInterface));
    assertEquals(-1, hc.compare(subclass, subSubInterface));
    assertEquals(-1, hc.compare(subclass, superclass));
    assertEquals(1, hc.compare(subclass, subSubclass));

    assertEquals(-1, hc.compare(subSubclass, Interface));
    assertEquals(-1, hc.compare(subSubclass, subInterface));
    assertEquals(-1, hc.compare(subSubclass, subSubInterface));
    assertEquals(-1, hc.compare(subSubclass, superclass));
    assertEquals(-1, hc.compare(subSubclass, subclass));

    ArrayList<ClassType> classes = new ArrayList<>();
    classes.add(Interface);
    classes.add(subclass);
    classes.add(subSubInterface);
    classes.add(superclass);
    classes.add(subInterface);
    classes.add(subSubclass);
    List<ClassType> classesSorted = classes.stream().sorted(hc).collect(Collectors.toList());
    assertEquals(subSubclass, classesSorted.get(0));
    assertEquals(subclass, classesSorted.get(1));
    assertEquals(superclass, classesSorted.get(2));
    assertEquals(subSubInterface, classesSorted.get(3));
    assertEquals(subInterface, classesSorted.get(4));
    assertEquals(Interface, classesSorted.get(5));
  }
}
