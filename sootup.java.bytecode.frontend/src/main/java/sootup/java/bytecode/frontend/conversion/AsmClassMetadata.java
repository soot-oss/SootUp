package sootup.java.bytecode.frontend.conversion;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InnerClassNode;
import sootup.core.IdentifierFactory;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.core.JavaClassFileMetadata;
import sootup.java.core.JavaInnerClassInfo;
import sootup.java.core.types.JavaClassType;

/** Extracts class attributes shared by regular and annotation class sources. */
final class AsmClassMetadata {
  private AsmClassMetadata() {}

  @NonNull
  static JavaClassFileMetadata read(
      @NonNull ClassNode node, @NonNull IdentifierFactory identifierFactory) {
    JavaClassType enclosingClass =
        node.outerClass == null
            ? null
            : AsmUtil.toJimpleClassType(node.outerClass, identifierFactory);
    MethodSignature enclosingMethod = null;
    if (enclosingClass != null && node.outerMethod != null) {
      List<Type> types = AsmUtil.toJimpleSignatureDesc(node.outerMethodDesc, identifierFactory);
      Type returnType = types.remove(types.size() - 1);
      enclosingMethod =
          identifierFactory.getMethodSignature(enclosingClass, node.outerMethod, returnType, types);
    }
    List<JavaInnerClassInfo> innerClasses = new ArrayList<>();
    if (node.innerClasses != null) {
      for (InnerClassNode entry : node.innerClasses) {
        innerClasses.add(
            new JavaInnerClassInfo(
                AsmUtil.toJimpleClassType(entry.name, identifierFactory),
                entry.outerName == null
                    ? null
                    : AsmUtil.toJimpleClassType(entry.outerName, identifierFactory),
                entry.innerName,
                entry.access));
      }
    }
    return new JavaClassFileMetadata(
        node.sourceFile, node.sourceDebug, enclosingClass, enclosingMethod, innerClasses);
  }

  @NonNull
  static Optional<JavaClassType> resolveOuterClass(
      @NonNull JavaClassFileMetadata metadata, @NonNull ClassType classType) {
    if (metadata.getEnclosingClass().isPresent()) {
      return metadata.getEnclosingClass();
    }
    // Member classes use their own InnerClasses entry instead of EnclosingMethod.
    for (JavaInnerClassInfo entry : metadata.getInnerClasses()) {
      if (entry.getInnerClass().equals(classType)) {
        return entry.getOuterClass();
      }
    }
    return Optional.empty();
  }
}
