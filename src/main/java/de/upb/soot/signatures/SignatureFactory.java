package de.upb.soot.signatures;

import java.nio.file.Path;
import java.util.List;

public interface SignatureFactory {
  PackageSignature getPackageSignature(String packageName);

  ClassSignature getClassSignature(String className, String packageName);

  ClassSignature getClassSignature(String fullyQualifiedClassName);

  TypeSignature getTypeSignature(String typeName);

  MethodSignature getMethodSignature(String methodName, String fullyQualifiedNameDeclClass, String fqReturnType,
      List<String> parameters);

  MethodSignature getMethodSignature(String methodName, ClassSignature declaringClassSignature, String fqReturnType,
      List<String> parameters);

  ClassSignature fromPath(Path file);
}
