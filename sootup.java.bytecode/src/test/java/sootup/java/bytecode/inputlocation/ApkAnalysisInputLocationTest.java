package sootup.java.bytecode.inputlocation;

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
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;

@Category(Java8Test.class)
public class ApkAnalysisInputLocationTest extends AnalysisInputLocationTest {

  @Test
  public void testApk() {
    AnalysisInputLocation pathBasedNamespace =
        new ApkAnalysisInputLocation(apk, null);
    final ClassType mainClass =
        getIdentifierFactory().getClassType("de.upb.futuresoot.fields.MainActivity");
    testClassReceival(pathBasedNamespace, Collections.singletonList(mainClass), 1392);
  }
}
