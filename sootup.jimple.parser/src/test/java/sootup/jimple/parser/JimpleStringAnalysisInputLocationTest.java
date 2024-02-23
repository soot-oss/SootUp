package sootup.jimple.parser;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2024 Markus Schmidt
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.core.views.View;

@Tag("Java8")
public class JimpleStringAnalysisInputLocationTest {

  @Test
  public void testInvalidInput() {
    String methodStr = "This is not Jimple its just a Sentence.";
    assertThrows(
        IllegalArgumentException.class, () -> new JimpleStringAnalysisInputLocation(methodStr));
  }

  @Test
  public void test() {

    String methodStr =
        "class DummyClass extends java.lang.Object {\n\t"
            + "void banana(){\n\t\t"
            + "params = new java.security.AlgorithmParameters;\n\t\t"
            + "return;\n\t"
            + "}\n"
            + "}";

    JimpleStringAnalysisInputLocation analysisInputLocation =
        new JimpleStringAnalysisInputLocation(methodStr);

    View view = new JimpleView(Collections.singletonList(analysisInputLocation));
    assertTrue(view.getClass(analysisInputLocation.getClassType()).isPresent());

    MethodSignature methodSig =
        view.getIdentifierFactory()
            .getMethodSignature(
                analysisInputLocation.getClassType(),
                "banana",
                VoidType.getInstance(),
                Collections.emptyList());
    assertTrue(view.getMethod(methodSig).isPresent());
  }
}
