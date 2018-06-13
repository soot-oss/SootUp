package de.upb.soot.signatures;

import java.nio.file.Path;
import java.util.List;

public interface SignatureFactory {
    PackageSignature getPackageSignature(String packageName);

    ClassSignature getClassSignature(String className, String packageName);

    ClassSignature getClassSignature(String fullyQualifiedClassName);

    TypeSignature getTypeSignature(String typeName);

    MethodSignature getMethodSignature(String methodName, String fullyQualifiedNameDeclClass,
                                       String fqReturnType, List<String> parameters);

    MethodSignature getMethodSignature(String methodName, ClassSignature declaringClassSignature,
                                       String fqReturnType, List<String> parameters);

    // TODO: this would not work for java 9 modules: their path is e.g., modules/java.base/java/lang/System
    // thus, I moved it to the corresponding namespace
    // currently, I cannot think of a general way for java 9 modules anyway....
    ClassSignature fromPath(Path file);
}
