package de.upb.swt.soot.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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

import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import java.util.Collections;
import javax.annotation.Nonnull;

public class JavaSootField extends SootField {

  @Nonnull private final Iterable<AnnotationType> annotations;

  /**
   * Constructs a Soot field with the given name, type and modifiers.
   *
   * @param signature
   * @param modifiers
   * @param annotations
   */
  public JavaSootField(
      @Nonnull FieldSignature signature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<AnnotationType> annotations) {
    super(signature, modifiers);
    this.annotations = annotations;
  }

  @Nonnull
  public Iterable<AnnotationType> getAnnotations() {
    return annotations;
  }

  @Nonnull
  public JavaSootField withAnnotations(@Nonnull Iterable<AnnotationType> annotations) {
    return new JavaSootField(getSignature(), getModifiers(), annotations);
  }

  @Nonnull
  public static AnnotationOrSignatureStep builder() {
    return new JavaSootFieldBuilder();
  }

  public interface AnnotationOrSignatureStep extends SignatureStep {
    BuildStep withAnnotation(Iterable<AnnotationType> annotations);
  }

  /**
   * Defines a {@link JavaSootFieldBuilder} to provide a fluent API.
   *
   * @author Markus Schmidt
   */
  public static class JavaSootFieldBuilder extends SootFieldBuilder
      implements AnnotationOrSignatureStep {

    private Iterable<AnnotationType> annotations = null;

    @Nonnull
    public Iterable<AnnotationType> getAnnotations() {
      return annotations != null ? annotations : Collections.emptyList();
    }

    @Override
    @Nonnull
    public BuildStep withAnnotation(Iterable<AnnotationType> annotations) {
      this.annotations = annotations;
      return this;
    }

    @Override
    @Nonnull
    public JavaSootField build() {
      return new JavaSootField(getSignature(), getModifiers(), getAnnotations());
    }
  }
}
