package de.upb.swt.soot.test.java.bytecode.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;

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
  final String warFile = war.toString();

  private IdentifierFactory identifierFactory;
  private ClassProvider classProvider;

  @Before
  public void setUp() {
    identifierFactory = JavaIdentifierFactory.getInstance();
    classProvider = new AsmJavaClassProvider(BytecodeBodyInterceptors.Default.bodyInterceptors());
  }

  protected IdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  protected ClassProvider getClassProvider() {
    return classProvider;
  }

  protected void testClassReceival(AnalysisInputLocation ns, ClassType sig, int minClassesFound) {
    testClassReceival(ns, sig, minClassesFound, -1);
  }

  protected void testClassReceival(
      AnalysisInputLocation ns, ClassType sig, int minClassesFound, int maxClassesFound) {
    boolean classFromJar = false;

    final Optional<? extends AbstractClassSource> clazz = ns.getClassSource(sig);

    for (String s : PathBasedAnalysisInputLocation.classesInXML) {
      if (sig.getClassName().equals(s)) System.out.println("The class from web.xml is present");
    }

    clazz.ifPresent(
        abstractClassSource -> Assert.assertEquals(sig, abstractClassSource.getClassType()));

    final Collection<? extends AbstractClassSource> classSources =
        ns.getClassSources(getIdentifierFactory());

    Assert.assertNotNull(PathBasedAnalysisInputLocation.jarsFromPath);
    for (Path jarPath : PathBasedAnalysisInputLocation.jarsFromPath) {
      classFromJar = true;
      PathBasedAnalysisInputLocation nsJar =
          PathBasedAnalysisInputLocation.createForClassContainer(jarPath);
      final Collection<? extends AbstractClassSource> classSourcesFromJar =
          nsJar.getClassSources(getIdentifierFactory());
      Assert.assertNotNull(classSourcesFromJar);
      Assert.assertFalse(classSourcesFromJar.isEmpty());
      Assert.assertThat(classSourcesFromJar.size(), new GreaterOrEqual<>(minClassesFound));
      if (maxClassesFound != -1) {
        Assert.assertThat(classSourcesFromJar.size(), new LessOrEqual<>(maxClassesFound));
      }
    }
    if (!classFromJar) {
      Assert.assertThat(classSources.size(), new GreaterOrEqual<>(minClassesFound));
      if (maxClassesFound != -1) {
        Assert.assertThat(classSources.size(), new LessOrEqual<>(maxClassesFound));
      }
    }
  }
}
