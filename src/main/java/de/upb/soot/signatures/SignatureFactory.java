package de.upb.soot.signatures;

import java.nio.file.Path;
import java.util.List;

public interface SignatureFactory {
  PackageSignature getPackageSignature(String packageName);

  JavaClassSignature getClassSignature(String className, String packageName);

  JavaClassSignature getClassSignature(String fullyQualifiedClassName);

  TypeSignature getTypeSignature(String typeName);

  MethodSignature getMethodSignature(String methodName, String fullyQualifiedNameDeclClass, String fqReturnType,
      List<String> parameters);

  MethodSignature getMethodSignature(String methodName, JavaClassSignature declaringClassSignature, String fqReturnType,
      List<String> parameters);

  JavaClassSignature fromPath(Path file);
}
