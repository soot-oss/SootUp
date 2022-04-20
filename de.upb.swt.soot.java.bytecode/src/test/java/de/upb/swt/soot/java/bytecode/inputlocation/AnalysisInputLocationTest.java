package de.upb.swt.soot.java.bytecode.inputlocation;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Before;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 07.06.2018 Manuel Benz
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

/**
 * @author Manuel Benz created on 07.06.18
 * @author Kaustubh Kelkar updated on 16.04.2020
 */
public abstract class AnalysisInputLocationTest {

  final Path war = Paths.get("../shared-test-resources/java-warApp/dummyWarApp.war");
  final Path jar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
  final Path mrj = Paths.get("../shared-test-resources/multi-release-jar/mrjar.jar");
  final Path mmrj = Paths.get("../shared-test-resources/multi-release-jar-modular/mrjar.jar");

  private ClassProvider<JavaSootClass> classProvider;

  @Before
  public void setUp() {
    classProvider = new AsmJavaClassProvider(BytecodeBodyInterceptors.Default.bodyInterceptors());
  }

  protected IdentifierFactory getIdentifierFactory() {
    return JavaIdentifierFactory.getInstance();
  }

  protected void testClassReceival(
      AnalysisInputLocation<JavaSootClass> ns, ClassType sig, int minClassesFound) {

    final JavaProject project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(ns).build();
    final JavaView view = project.createOnDemandView();

    final Optional<? extends AbstractClassSource<JavaSootClass>> clazzOpt =
        ns.getClassSource(sig, view);
    assertTrue(clazzOpt.isPresent());
    assertEquals(sig, clazzOpt.get().getClassType());

    final Collection<? extends AbstractClassSource<?>> classSources = ns.getClassSources(view);

    assertTrue(classSources.size() >= minClassesFound);
  }
}
