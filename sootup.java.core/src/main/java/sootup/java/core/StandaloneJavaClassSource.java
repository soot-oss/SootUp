package sootup.java.core;

import java.nio.file.Path;
import java.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.core.util.CollectionUtils;
import sootup.java.core.types.JavaClassType;

@SuppressWarnings({"unchecked"})
public class StandaloneJavaClassSource extends OverridingJavaClassSource {

    private static final Position NO_POSITION =
            new Position() {
                @Override public int getFirstLine() { return -1; }
                @Override public int getLastLine()  { return -1; }
                @Override public int getFirstCol()  { return -1; }
                @Override public int getLastCol()   { return -1; }
            };

    public StandaloneJavaClassSource(
            @NonNull AnalysisInputLocation srcNamespace,
            @NonNull Path sourcePath,
            @NonNull ClassType classType,
            @Nullable JavaClassType superClass,
            @NonNull Set<JavaClassType> interfaces,
            @Nullable JavaClassType outerClass,
            @NonNull Set<JavaSootField> sootFields,
            @NonNull Set<JavaSootMethod> sootMethods,
            @NonNull Position position,
            @NonNull EnumSet<ClassModifier> modifiers,
            @NonNull Iterable<AnnotationUsage> annotations,
            @NonNull Iterable<AnnotationUsage> methodAnnotations,
            @Nullable Iterable<AnnotationUsage> fieldAnnotations) {

        super(
                srcNamespace,
                sourcePath,
                classType,
                superClass,
                interfaces,
                outerClass,
                sootFields,
                sootMethods,
                position,
                modifiers,
                annotations,
                methodAnnotations,
                fieldAnnotations);
    }

    public StandaloneJavaClassSource(
            @NonNull Set<JavaSootMethod> sootMethods,
            @NonNull Set<JavaSootField> sootFields,
            @NonNull EnumSet<ClassModifier> modifiers,
            @NonNull Set<JavaClassType> interfaces,
            @NonNull JavaClassType superClass,
            @NonNull JavaClassType outerClass,
            @NonNull Position position,
            @NonNull Path sourcePath,
            @NonNull ClassType classType,
            @NonNull AnalysisInputLocation srcNamespace) {

        super(
                sootMethods,
                sootFields,
                modifiers,
                interfaces,
                superClass,
                outerClass,
                position,
                sourcePath,
                classType,
                srcNamespace);
    }

    public StandaloneJavaClassSource(
            @NonNull AnalysisInputLocation srcNamespace,
            @NonNull Path sourcePath,
            @NonNull ClassType classType) {

        super(
                srcNamespace,
                sourcePath,
                classType,
                null,                       // no superclass
                Collections.emptySet(),     // no interfaces
                null,                       // no outer class
                Collections.emptySet(),     // no fields
                Collections.emptySet(),     // no methods
                NO_POSITION,                // default position
                EnumSet.noneOf(ClassModifier.class), // no modifiers
                Collections.emptyList(),    // no annotations
                Collections.emptyList(),    // no method annotations
                Collections.emptyList());   // no field annotations
    }

    public StandaloneJavaClassSource(
            @NonNull JavaSootClassSource sourceToConvert,
            @NonNull AnalysisInputLocation srcNamespace,
            @NonNull Path sourcePath)
            throws ResolveException {

        super(
                srcNamespace,
                sourcePath,
                sourceToConvert.getClassType(),
                (JavaClassType) sourceToConvert.resolveSuperclass().orElse(null),
                convertToJavaClassTypes(sourceToConvert.resolveInterfaces()),
                (JavaClassType) sourceToConvert.resolveOuterClass().orElse(null),
                convertToJavaFields(sourceToConvert.resolveFields()),
                convertToJavaMethods(sourceToConvert.resolveMethods()),
                sourceToConvert.resolvePosition(),
                EnumSet.copyOf(sourceToConvert.resolveModifiers()),
                sourceToConvert.resolveAnnotations(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }


    private static Set<JavaClassType> convertToJavaClassTypes(Set<? extends ClassType> types) {
        Set<JavaClassType> result = new HashSet<>();
        for (ClassType type : types) {
            if (type instanceof JavaClassType) {
                result.add((JavaClassType) type);
            }
        }
        return result;
    }

    private static Set<JavaSootField> convertToJavaFields(Collection<? extends SootField> fields) {
        Set<JavaSootField> result = new HashSet<>();
        for (SootField field : fields) {
            if (field instanceof JavaSootField) {
                result.add((JavaSootField) field);
            }
        }
        return result;
    }

    private static Set<JavaSootMethod> convertToJavaMethods(Collection<? extends SootMethod> methods) {
        Set<JavaSootMethod> result = new HashSet<>();
        for (SootMethod method : methods) {
            if (method instanceof JavaSootMethod) {
                result.add((JavaSootMethod) method);
            }
        }
        return result;
    }


    @Override
    public StandaloneJavaClassSource withMethods(
            @NonNull Collection<JavaSootMethod> overriddenSootMethods) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    (JavaClassType) resolveSuperclass().orElse(null),
                    convertToJavaClassTypes(resolveInterfaces()),
                    (JavaClassType) resolveOuterClass().orElse(null),
                    new HashSet<>((Collection<JavaSootField>) resolveFields()),
                    new HashSet<>(overriddenSootMethods),
                    resolvePosition(),
                    EnumSet.copyOf(resolveModifiers()),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withFields(
            @NonNull Collection<JavaSootField> overriddenSootFields) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    (JavaClassType) resolveSuperclass().orElse(null),
                    convertToJavaClassTypes(resolveInterfaces()),
                    (JavaClassType) resolveOuterClass().orElse(null),
                    new HashSet<>(overriddenSootFields),
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods()),
                    resolvePosition(),
                    EnumSet.copyOf(resolveModifiers()),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withModifiers(
            @NonNull Set<ClassModifier> overriddenModifiers) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    (JavaClassType) resolveSuperclass().orElse(null),
                    convertToJavaClassTypes(resolveInterfaces()),
                    (JavaClassType) resolveOuterClass().orElse(null),
                    new HashSet<>((Collection<JavaSootField>) resolveFields()),
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods()),
                    resolvePosition(),
                    EnumSet.copyOf(overriddenModifiers),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withInterfaces(
            @NonNull Set<JavaClassType> overriddenInterfaces) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    (JavaClassType) resolveSuperclass().orElse(null),
                    overriddenInterfaces,
                    (JavaClassType) resolveOuterClass().orElse(null),
                    new HashSet<>((Collection<JavaSootField>) resolveFields()),
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods()),
                    resolvePosition(),
                    EnumSet.copyOf(resolveModifiers()),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withSuperclass(
            @NonNull Optional<JavaClassType> overriddenSuperclass) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    overriddenSuperclass.orElse(null),
                    convertToJavaClassTypes(resolveInterfaces()),
                    (JavaClassType) resolveOuterClass().orElse(null),
                    new HashSet<>((Collection<JavaSootField>) resolveFields()),
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods()),
                    resolvePosition(),
                    EnumSet.copyOf(resolveModifiers()),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withOuterClass(
            @NonNull Optional<JavaClassType> overriddenOuterClass) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    (JavaClassType) resolveSuperclass().orElse(null),
                    convertToJavaClassTypes(resolveInterfaces()),
                    overriddenOuterClass.orElse(null),
                    new HashSet<>((Collection<JavaSootField>) resolveFields()),
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods()),
                    resolvePosition(),
                    EnumSet.copyOf(resolveModifiers()),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withPosition(@Nullable Position position) {
        try {
            return new StandaloneJavaClassSource(
                    getAnalysisInputLocation(),
                    getSourcePath(),
                    getClassType(),
                    (JavaClassType) resolveSuperclass().orElse(null),
                    convertToJavaClassTypes(resolveInterfaces()),
                    (JavaClassType) resolveOuterClass().orElse(null),
                    new HashSet<>((Collection<JavaSootField>) resolveFields()),
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods()),
                    position != null ? position : NO_POSITION,
                    EnumSet.copyOf(resolveModifiers()),
                    resolveAnnotations(),
                    Collections.emptyList(),
                    Collections.emptyList());
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve class elements", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withReplacedMethod(
            @NonNull JavaSootMethod toReplace, @NonNull JavaSootMethod replacement) {
        try {
            Set<JavaSootMethod> newMethods =
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods());
            CollectionUtils.replace(newMethods, toReplace, replacement);
            return withMethods(newMethods);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve methods", e);
        }
    }

    @Override
    public StandaloneJavaClassSource withReplacedField(
            @NonNull JavaSootField toReplace, @NonNull JavaSootField replacement) {
        try {
            Set<JavaSootField> newFields =
                    new HashSet<>((Collection<JavaSootField>) resolveFields());
            CollectionUtils.replace(newFields, toReplace, replacement);
            return withFields(newFields);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve fields", e);
        }
    }


    @NonNull
    public StandaloneJavaClassSource withoutMethod(@NonNull JavaSootMethod methodToRemove) {
        try {
            Set<JavaSootMethod> methods =
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods());
            methods.remove(methodToRemove);
            return withMethods(methods);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve methods", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withoutField(@NonNull JavaSootField fieldToRemove) {
        try {
            Set<JavaSootField> fields =
                    new HashSet<>((Collection<JavaSootField>) resolveFields());
            fields.remove(fieldToRemove);
            return withFields(fields);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve fields", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withoutMethods(
            @NonNull Collection<JavaSootMethod> methodsToRemove) {
        try {
            Set<JavaSootMethod> methods =
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods());
            methods.removeAll(methodsToRemove);
            return withMethods(methods);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve methods", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withoutFields(
            @NonNull Collection<JavaSootField> fieldsToRemove) {
        try {
            Set<JavaSootField> fields =
                    new HashSet<>((Collection<JavaSootField>) resolveFields());
            fields.removeAll(fieldsToRemove);
            return withFields(fields);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve fields", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withAddedMethod(@NonNull JavaSootMethod methodToAdd) {
        try {
            Set<JavaSootMethod> methods =
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods());
            methods.add(methodToAdd);
            return withMethods(methods);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve methods", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withAddedField(@NonNull JavaSootField fieldToAdd) {
        try {
            Set<JavaSootField> fields =
                    new HashSet<>((Collection<JavaSootField>) resolveFields());
            fields.add(fieldToAdd);
            return withFields(fields);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve fields", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withAddedMethods(
            @NonNull Collection<JavaSootMethod> methodsToAdd) {
        try {
            Set<JavaSootMethod> methods =
                    new HashSet<>((Collection<JavaSootMethod>) resolveMethods());
            methods.addAll(methodsToAdd);
            return withMethods(methods);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve methods", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withAddedFields(
            @NonNull Collection<JavaSootField> fieldsToAdd) {
        try {
            Set<JavaSootField> fields =
                    new HashSet<>((Collection<JavaSootField>) resolveFields());
            fields.addAll(fieldsToAdd);
            return withFields(fields);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve fields", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withAddedInterface(@NonNull JavaClassType interfaceToAdd) {
        try {
            Set<JavaClassType> interfaces =
                    new HashSet<>(convertToJavaClassTypes(resolveInterfaces()));
            interfaces.add(interfaceToAdd);
            return withInterfaces(interfaces);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve interfaces", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withoutInterface(@NonNull JavaClassType interfaceToRemove) {
        try {
            Set<JavaClassType> interfaces =
                    new HashSet<>(convertToJavaClassTypes(resolveInterfaces()));
            interfaces.remove(interfaceToRemove);
            return withInterfaces(interfaces);
        } catch (ResolveException e) {
            throw new RuntimeException("Failed to resolve interfaces", e);
        }
    }

    @NonNull
    public StandaloneJavaClassSource withAddedModifier(@NonNull ClassModifier modifierToAdd) {
        EnumSet<ClassModifier> modifiers = EnumSet.copyOf(resolveModifiers());
        modifiers.add(modifierToAdd);
        return withModifiers(modifiers);
    }

    @NonNull
    public StandaloneJavaClassSource withoutModifier(@NonNull ClassModifier modifierToRemove) {
        EnumSet<ClassModifier> modifiers = EnumSet.copyOf(resolveModifiers());
        modifiers.remove(modifierToRemove);
        return withModifiers(modifiers);
    }

    @Override
    public String toString() {
        try {
            return "StandaloneJavaClassSource{" +
                    "classType=" + getClassType() +
                    ", methodsCount=" + resolveMethods().size() +
                    ", fieldsCount=" + resolveFields().size() +
                    ", interfacesCount=" + resolveInterfaces().size() +
                    ", modifiers=" + resolveModifiers() +
                    ", superclass=" + resolveSuperclass().map(Object::toString).orElse("none") +
                    ", outerClass=" + resolveOuterClass().map(Object::toString).orElse("none") +
                    '}';
        } catch (ResolveException e) {
            return "StandaloneJavaClassSource{classType=" + getClassType() +
                    ", error=" + e.getMessage() + '}';
        }
    }
}
