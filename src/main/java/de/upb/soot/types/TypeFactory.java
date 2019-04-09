package de.upb.soot.types;

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;

public interface TypeFactory {
  JavaClassType getClassType(String className, String packageName);

  JavaClassType getClassType(String fullyQualifiedClassName);

  Type getType(String typeName);

  @Nonnull
  Optional<PrimitiveType> getPrimitiveType(@Nonnull String typeName);

  ArrayType getArrayType(Type baseType, int dim);

  JavaClassType fromPath(Path file);
}
