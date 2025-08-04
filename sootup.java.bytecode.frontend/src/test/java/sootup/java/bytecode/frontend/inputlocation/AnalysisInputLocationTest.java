package sootup.java.bytecode.frontend.inputlocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.types.ClassType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

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
  final Path cls = Paths.get("../shared-test-resources/miniTestSuite/java6/binary/Employee.class");

  protected IdentifierFactory getIdentifierFactory() {
    return JavaIdentifierFactory.getInstance();
  }

  protected void testClassReceival(
      AnalysisInputLocation ns, List<ClassType> sigs, int classesFound) {

    final JavaView view = new JavaView(ns);

    for (ClassType classType : sigs) {
      final Optional<? extends SootClassSource> clazzOpt = ns.getClassSource(classType, view);
      assertTrue(clazzOpt.isPresent());
      assertEquals(classType, clazzOpt.get().getClassType());
    }
    assertEquals(ns.getClassSources(view).count(), classesFound);
  }
}
