package sootup.java.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 the SootUp contributors
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootMethod;

class JavaSootMethodGenericSignatureTest {
  private final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  private final MethodSignature signature =
      factory.getMethodSignature(
          factory.getClassType("Example"),
          "value",
          factory.getClassType("java.lang.Object"),
          Collections.emptyList());
  private final Body body = Body.builder().setMethodSignature(signature).build();
  private final OverridingBodySource source = new OverridingBodySource(signature, body);

  @Test
  void existingConstructorsAndBuilderDefaultToAbsentGenericSignature() {
    for (JavaSootMethod method :
        List.of(
            new JavaSootMethod(
                source,
                signature,
                Collections.emptySet(),
                Collections.emptyList(),
                NoPositionInformation.getInstance()),
            new JavaSootMethod(
                source,
                signature,
                Collections.emptySet(),
                Collections.emptyList(),
                Collections.emptyList(),
                NoPositionInformation.getInstance()),
            JavaSootMethod.JavaSootMethodBuilder.builder()
                .withSource(source)
                .withSignature(signature)
                .build())) {
      assertTrue(method.getGenericSignature().isEmpty());
    }
  }

  @Test
  void copiesPreserveGenericSignature() {
    AnnotationUsage annotation =
        new AnnotationUsage(factory.getClassType("Marker"), Collections.emptyMap());
    JavaSootMethod method =
        new JavaSootMethod(
            source,
            signature,
            EnumSet.of(MethodModifier.PUBLIC),
            Collections.emptyList(),
            List.of(annotation),
            NoPositionInformation.getInstance(),
            "<T:Ljava/lang/Object;>()TT;");
    OverridingBodySource replacementSource = new OverridingBodySource(signature, body);
    List<JavaSootMethod> copies =
        List.of(
            method.withSource(replacementSource),
            method.withOverridingMethodSource(s -> s.withBody(body)),
            method.withModifiers(EnumSet.of(MethodModifier.PRIVATE)),
            method.withThrownExceptions(List.of(factory.getClassType("java.lang.Exception"))),
            method.withAnnotations(Collections.emptyList()),
            method.withBody(body));
    for (JavaSootMethod copy : copies) {
      assertEquals(method.getGenericSignature(), copy.getGenericSignature());
      assertEquals(method.getSignature(), copy.getSignature());
    }
    assertSame(replacementSource, copies.get(0).getBodySource());
    assertTrue(copies.get(2).isPrivate());
    assertEquals(1, copies.get(3).getExceptionSignatures().size());
    assertEquals(Collections.emptyList(), copies.get(4).getAnnotations());
    assertSame(body, copies.get(5).getBody());
    assertEquals(List.of(annotation), method.getAnnotations());
  }

  @Test
  void builderAcceptsAndClearsGenericSignature() {
    var builder =
        JavaSootMethod.JavaSootMethodBuilder.builder()
            .withSource(source)
            .withSignature(signature)
            .withGenericSignature("<T:Ljava/lang/Object;>()TT;");
    JavaSootMethod method = builder.build();
    assertEquals("<T:Ljava/lang/Object;>()TT;", method.getGenericSignature().orElseThrow());
    assertTrue(builder.withGenericSignature(null).build().getGenericSignature().isEmpty());
    assertTrue(method.getGenericSignature().isPresent());
  }
}
