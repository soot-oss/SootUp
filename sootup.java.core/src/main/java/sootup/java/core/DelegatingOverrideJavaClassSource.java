package sootup.java.core;

import java.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.frontend.ResolveException;
import sootup.core.model.ClassModifier;
import sootup.core.model.Position;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.types.ClassType;
import sootup.java.core.types.JavaClassType;

public final class DelegatingOverrideJavaClassSource extends JavaSootClassSource {

    private final @NonNull JavaSootClassSource delegate;

    // Optional overrides: when non-null, they override the delegate result
    private final @Nullable Collection<JavaSootMethod> methodsOverride;
    private final @Nullable Collection<JavaSootField>  fieldsOverride;
    private final @Nullable Set<ClassModifier>         modifiersOverride;
    private final @Nullable Set<JavaClassType>         interfacesOverride;
    private final @Nullable Optional<JavaClassType>    superclassOverride;
    private final @Nullable Optional<JavaClassType>    outerClassOverride;
    private final @Nullable Position                   positionOverride;
    private final @Nullable Iterable<AnnotationUsage>  annotationsOverride;
    private final @Nullable Iterable<AnnotationUsage>  methodAnnotationsOverride;
    private final @Nullable Iterable<AnnotationUsage>  fieldAnnotationsOverride;

    public DelegatingOverrideJavaClassSource(@NonNull JavaSootClassSource delegate) {
        super(delegate);
        this.delegate = Objects.requireNonNull(delegate);

        this.methodsOverride = null;
        this.fieldsOverride = null;
        this.modifiersOverride = null;
        this.interfacesOverride = null;
        this.superclassOverride = null;
        this.outerClassOverride = null;
        this.positionOverride = null;
        this.annotationsOverride = null;
        this.methodAnnotationsOverride = null;
        this.fieldAnnotationsOverride = null;
    }

    private DelegatingOverrideJavaClassSource(
            @NonNull JavaSootClassSource delegate,
            @Nullable Collection<JavaSootMethod> methodsOverride,
            @Nullable Collection<JavaSootField> fieldsOverride,
            @Nullable Set<ClassModifier> modifiersOverride,
            @Nullable Set<JavaClassType> interfacesOverride,
            @Nullable Optional<JavaClassType> superclassOverride,
            @Nullable Optional<JavaClassType> outerClassOverride,
            @Nullable Position positionOverride,
            @Nullable Iterable<AnnotationUsage> annotationsOverride,
            @Nullable Iterable<AnnotationUsage> methodAnnotationsOverride,
            @Nullable Iterable<AnnotationUsage> fieldAnnotationsOverride) {
        super(delegate);
        this.delegate = delegate;
        this.methodsOverride = methodsOverride;
        this.fieldsOverride = fieldsOverride;
        this.modifiersOverride = modifiersOverride;
        this.interfacesOverride = interfacesOverride;
        this.superclassOverride = superclassOverride;
        this.outerClassOverride = outerClassOverride;
        this.positionOverride = positionOverride;
        this.annotationsOverride = annotationsOverride;
        this.methodAnnotationsOverride = methodAnnotationsOverride;
        this.fieldAnnotationsOverride = fieldAnnotationsOverride;
    }

    @Override
    public @NonNull Collection<? extends SootMethod> resolveMethods() throws ResolveException {
        return (methodsOverride != null) ? methodsOverride : delegate.resolveMethods();
    }

    @Override
    public @NonNull Collection<? extends SootField> resolveFields() throws ResolveException {
        return (fieldsOverride != null) ? fieldsOverride : delegate.resolveFields();
    }

    @Override
    public @NonNull Set<ClassModifier> resolveModifiers() {
        return (modifiersOverride != null) ? modifiersOverride : delegate.resolveModifiers();
    }

    @Override
    public @NonNull Set<? extends ClassType> resolveInterfaces() {
        return (interfacesOverride != null) ? interfacesOverride : delegate.resolveInterfaces();
    }

    @Override
    public @NonNull Optional<? extends ClassType> resolveSuperclass() {
        return (superclassOverride != null) ? superclassOverride : delegate.resolveSuperclass();
    }

    @Override
    public @NonNull Optional<? extends ClassType> resolveOuterClass() {
        return (outerClassOverride != null) ? outerClassOverride : delegate.resolveOuterClass();
    }

    @Override
    public @NonNull Position resolvePosition() {
        return (positionOverride != null) ? positionOverride : delegate.resolvePosition();
    }

    @Override
    public @NonNull Iterable<AnnotationUsage> resolveAnnotations() {
        return (annotationsOverride != null) ? annotationsOverride : delegate.resolveAnnotations();
    }

    public @NonNull DelegatingOverrideJavaClassSource withMethods(
            @NonNull Collection<JavaSootMethod> methods) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                new ArrayList<>(methods),
                fieldsOverride,
                modifiersOverride,
                interfacesOverride,
                superclassOverride,
                outerClassOverride,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withFields(
            @NonNull Collection<JavaSootField> fields) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                new ArrayList<>(fields),
                modifiersOverride,
                interfacesOverride,
                superclassOverride,
                outerClassOverride,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withModifiers(
            @NonNull Set<ClassModifier> modifiers) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                fieldsOverride,
                EnumSet.copyOf(modifiers),
                interfacesOverride,
                superclassOverride,
                outerClassOverride,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withInterfaces(
            @NonNull Set<JavaClassType> interfaces) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                fieldsOverride,
                modifiersOverride,
                new HashSet<>(interfaces),
                superclassOverride,
                outerClassOverride,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withSuperclass(
            @NonNull Optional<JavaClassType> superclass) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                fieldsOverride,
                modifiersOverride,
                interfacesOverride,
                superclass,
                outerClassOverride,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withOuterClass(
            @NonNull Optional<JavaClassType> outerClass) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                fieldsOverride,
                modifiersOverride,
                interfacesOverride,
                superclassOverride,
                outerClass,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withPosition(@Nullable Position pos) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                fieldsOverride,
                modifiersOverride,
                interfacesOverride,
                superclassOverride,
                outerClassOverride,
                pos,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withAnnotations(
            @NonNull Iterable<AnnotationUsage> annotations) {
        return new DelegatingOverrideJavaClassSource(
                delegate,
                methodsOverride,
                fieldsOverride,
                modifiersOverride,
                interfacesOverride,
                superclassOverride,
                outerClassOverride,
                positionOverride,
                annotations,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }

    public @NonNull DelegatingOverrideJavaClassSource withReplacedMethod(
            @NonNull JavaSootMethod toReplace, @NonNull JavaSootMethod replacement)
            throws ResolveException {
        Collection<JavaSootMethod> base =
                (methodsOverride != null)
                        ? new ArrayList<>(methodsOverride)
                        : collectJavaMethods(delegate.resolveMethods());
        replace(base, toReplace, replacement);
        return withMethods(base);
    }

    public @NonNull DelegatingOverrideJavaClassSource withReplacedField(
            @NonNull JavaSootField toReplace, @NonNull JavaSootField replacement)
            throws ResolveException {
        Collection<JavaSootField> base =
                (fieldsOverride != null)
                        ? new ArrayList<>(fieldsOverride)
                        : collectJavaFields(delegate.resolveFields());
        replace(base, toReplace, replacement);
        return withFields(base);
    }


    private static Collection<JavaSootMethod> collectJavaMethods(
            Collection<? extends SootMethod> in) {
        List<JavaSootMethod> out = new ArrayList<>();
        for (SootMethod m : in) {
            if (m instanceof JavaSootMethod jm) out.add(jm);
        }
        return out;
    }

    private static Collection<JavaSootField> collectJavaFields(
            Collection<? extends SootField> in) {
        List<JavaSootField> out = new ArrayList<>();
        for (SootField f : in) {
            if (f instanceof JavaSootField jf) out.add(jf);
        }
        return out;
    }

    private static <T> void replace(Collection<T> c, T oldV, T newV) {
        if (c instanceof List<T> list) {
            int idx = list.indexOf(oldV);
            if (idx >= 0) list.set(idx, newV);
            else {
                c.remove(oldV);
                c.add(newV);
            }
        } else {
            c.remove(oldV);
            c.add(newV);
        }
    }


    @Override
    public String toString() {
        return "DelegatingOverrideJavaClassSource{" +
                ", delegate=" + delegate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DelegatingOverrideJavaClassSource that)) return false;
        return Objects.equals(delegate, that.delegate) &&
                Objects.equals(methodsOverride, that.methodsOverride) &&
                Objects.equals(fieldsOverride, that.fieldsOverride) &&
                Objects.equals(modifiersOverride, that.modifiersOverride) &&
                Objects.equals(interfacesOverride, that.interfacesOverride) &&
                Objects.equals(superclassOverride, that.superclassOverride) &&
                Objects.equals(outerClassOverride, that.outerClassOverride) &&
                Objects.equals(positionOverride, that.positionOverride) &&
                Objects.equals(annotationsOverride, that.annotationsOverride) &&
                Objects.equals(methodAnnotationsOverride, that.methodAnnotationsOverride) &&
                Objects.equals(fieldAnnotationsOverride, that.fieldAnnotationsOverride);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                delegate,
                methodsOverride,
                fieldsOverride,
                modifiersOverride,
                interfacesOverride,
                superclassOverride,
                outerClassOverride,
                positionOverride,
                annotationsOverride,
                methodAnnotationsOverride,
                fieldAnnotationsOverride);
    }
}
