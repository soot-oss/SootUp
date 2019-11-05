package de.upb.swt.soot.test.java.bytecode.inputlocation;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
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

/** @author Manuel Benz created on 07.06.18 */
public abstract class AnalysisInputLocationTest {

  public static final String jarFile = "../shared-test-resources/Soot-4.0-SNAPSHOT.jar";

  protected static final int CLASSES_IN_JAR = 25;
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
    final Optional<? extends AbstractClassSource> clazz = ns.getClassSource(sig);

    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassType());

    final Collection<? extends AbstractClassSource> classSources =
        ns.getClassSources(getIdentifierFactory());
    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertThat(classSources.size(), new GreaterOrEqual<>(minClassesFound));
    if (maxClassesFound != -1) {
      Assert.assertThat(classSources.size(), new LessOrEqual<>(maxClassesFound));
    }
  }
}
