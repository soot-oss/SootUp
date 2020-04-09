package de.upb.swt.soot.test.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
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
import categories.Java8Test;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Manuel Benz created on 06.06.18 */
@Category(Java8Test.class)
public class PathBasedAnalysisInputLocationTest extends AnalysisInputLocationTest {

  @Test
  public void testJar() {

    final Path jar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
    PathBasedAnalysisInputLocation pathBasedNamespace =
        PathBasedAnalysisInputLocation.createForClassContainer(jar);
    System.err.println(jar.toFile().getAbsolutePath());
    final ClassType class1 = getIdentifierFactory().getClassType("Employee", "ds");
    final ClassType mainClass = getIdentifierFactory().getClassType("MiniApp");
    testClassReceival(pathBasedNamespace, class1, CLASSES_IN_JAR);
    testClassReceival(pathBasedNamespace, mainClass, CLASSES_IN_JAR);
  }
}
