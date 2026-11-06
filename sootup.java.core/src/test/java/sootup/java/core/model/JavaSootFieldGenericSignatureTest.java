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
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.FieldModifier;
import sootup.core.signatures.FieldSignature;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootField;
import sootup.java.core.language.JavaJimple;

class JavaSootFieldGenericSignatureTest {
  private final JavaIdentifierFactory factory = new JavaIdentifierFactory();
  private final FieldSignature signature =
      factory.getFieldSignature(
          "value", factory.getClassType("Example"), factory.getClassType("java.lang.String"));

  @Test
  void existingConstructorsAndBuilderDefaultToAbsentGenericSignature() {
    for (JavaSootField field :
        List.of(
            new JavaSootField(
                signature, Collections.emptySet(), NoPositionInformation.getInstance()),
            new JavaSootField(
                signature,
                Collections.emptySet(),
                Collections.emptyList(),
                NoPositionInformation.getInstance()),
            new JavaSootField(
                signature,
                Collections.emptySet(),
                Collections.emptyList(),
                NoPositionInformation.getInstance(),
                null),
            JavaSootField.JavaSootFieldBuilder.builder()
                .withSignature(signature)
                .withModifier(Collections.emptySet())
                .build())) {
      assertTrue(field.getGenericSignature().isEmpty());
    }
  }

  @Test
  void copiesPreserveGenericSignatureConstantValueAndUnchangedAnnotations() {
    AnnotationUsage annotation =
        new AnnotationUsage(factory.getClassType("Marker"), Collections.emptyMap());
    JavaSootField field =
        new JavaSootField(
            signature,
            EnumSet.of(FieldModifier.PUBLIC, FieldModifier.STATIC, FieldModifier.FINAL),
            List.of(annotation),
            NoPositionInformation.getInstance(),
            JavaJimple.newStringConstant("constant", factory),
            "Ljava/lang/String;");
    FieldSignature renamed =
        factory.getFieldSignature("renamed", signature.getDeclClassType(), signature.getType());
    List<JavaSootField> copies =
        List.of(
            field.withSignature(renamed),
            field.withModifiers(EnumSet.of(FieldModifier.PRIVATE)),
            field.withAnnotations(Collections.emptyList()));
    for (JavaSootField copy : copies) {
      assertEquals(field.getGenericSignature(), copy.getGenericSignature());
      assertEquals(field.getConstantValue(), copy.getConstantValue());
      assertEquals(field.getPosition(), copy.getPosition());
    }
    assertEquals(List.of(annotation), copies.get(0).getAnnotations());
    assertEquals(List.of(annotation), copies.get(1).getAnnotations());
    assertEquals(renamed, copies.get(0).getSignature());
    assertTrue(copies.get(1).isPrivate());
    assertEquals(Collections.emptyList(), copies.get(2).getAnnotations());
    assertEquals(List.of(annotation), field.getAnnotations());
  }

  @Test
  void builderAcceptsAndClearsGenericSignature() {
    var builder =
        JavaSootField.JavaSootFieldBuilder.builder()
            .withSignature(signature)
            .withModifier(Collections.emptySet())
            .withConstantValue(JavaJimple.newStringConstant("constant", factory))
            .withGenericSignature("Ljava/lang/String;");
    JavaSootField field = builder.build();
    assertEquals("Ljava/lang/String;", field.getGenericSignature().orElseThrow());
    JavaSootField cleared = builder.withGenericSignature(null).build();
    assertTrue(cleared.getGenericSignature().isEmpty());
    assertEquals(field.getConstantValue(), cleared.getConstantValue());
    assertTrue(field.getGenericSignature().isPresent());
    JavaSootField constantCleared =
        builder.withGenericSignature("Ljava/lang/String;").withConstantValue(null).build();
    assertEquals(field.getGenericSignature(), constantCleared.getGenericSignature());
    assertTrue(constantCleared.getConstantValue().isEmpty());
    assertTrue(field.getConstantValue().isPresent());
  }
}
