/*
 * Copyright © 2019 Jan Martin Persch
 * All rights reserved.
 */

package de.upb.soot.frontends.asm;

import categories.Java8Test;
import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.views.IView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Optional;

/**
 * Defines the {@link ModuleCompositionTest} class.
 *
 * @author Jan Martin Persch
 */
@Category(Java8Test.class)
public class ModuleCompositionTest {
  @Test
  public void testModuleComposition() {
    String javaClassPath = "target/test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar";

    INamespace cpBased = new JavaClassPathNamespace(javaClassPath);

    Project p = new Project(cpBased);

    IView view = p.createFullView();

    Optional<AbstractClass> sootClass = view.getClass(p.getSignatureFactory().getClassSignature("de.upb.soot.Utils"));

    System.out.println(sootClass);
  }
}
