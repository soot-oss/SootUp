package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.tag.Tag;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootClassSource;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class DexClassSource extends JavaSootClassSource {

    public DexClassSource(@Nonnull AnalysisInputLocation<? extends SootClass<?>> srcNamespace, @Nonnull ClassType classSignature, @Nonnull Path sourcePath) {
        super(srcNamespace, classSignature, sourcePath);
    }

    @Override
    protected Iterable<AnnotationUsage> resolveAnnotations() {
        return null;
    }

    @Override
    public JavaSootClass buildClass(@Nonnull SourceType sourceType) {
        return null;
    }

    @Nonnull
    @Override
    public Collection<? extends SootMethod> resolveMethods() throws ResolveException {
        return null;
    }

    @Nonnull
    @Override
    public Collection<? extends SootField> resolveFields() throws ResolveException {
        return null;
    }

    @Nonnull
    @Override
    public Set<Modifier> resolveModifiers() {
        return null;
    }

    @Nonnull
    @Override
    public Set<? extends ClassType> resolveInterfaces() {
        return null;
    }

    @Nonnull
    @Override
    public Optional<? extends ClassType> resolveSuperclass() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Optional<? extends ClassType> resolveOuterClass() {
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Position resolvePosition() {
        return null;
    }


    /**
     * Loads a single method from a dex file
     *
     * @param method
     *          The method to load
     * @param declaringClass
     *          The class that declares the method to load
     * @param annotations
     *          The worker object for handling annotations
     * @param dexMethodFactory
     *          The factory method for creating dex methods
     */
    protected void loadMethod(Method method, JavaSootClass declaringClass, DexAnnotation annotations, DexMethod dexMethodFactory) {
        SootMethod sm = dexMethodFactory.makeSootMethod(method);
        if (declaringClass.declaresMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType())) {
            return;
        }
        declaringClass.addMethod(sm);
        annotations.handleMethodAnnotation(sm, method);
    }

    /**
     * Loads a single field from a dex file
     *
     * @param declaringClass
     *          The class that declares the method to load
     * @param annotations
     *          The worker object for handling annotations
     * @param field
     *          The field to load
     */
    protected void loadField(SootClass declaringClass, DexAnnotation annotations, Field sf) {
        if (declaringClass.declaresField(sf.getName(), DexType.toSoot(sf.getType()))) {
            return;
        }
        SootField sootField = DexField.makeSootField(sf);
        sootField = declaringClass.getOrAddField(sootField);
        annotations.handleFieldAnnotation(sootField, sf);
    }

    private boolean options_oaat = true;

    public Dependencies makeSootClass(SootClass sc, ClassDef defItem, MultiDexContainer.DexEntry<? extends DexFile> dexEntry) {
        String superClass = defItem.getSuperclass();
        Dependencies deps = new Dependencies();

        // source file
        String sourceFile = defItem.getSourceFile();
        if (sourceFile != null) {
            sc.addTag(new SourceFileTag(sourceFile));
        }

        // super class for hierarchy level
        if (superClass != null) {
            String superClassName = Util.dottedClassName(superClass);
            SootClass sootSuperClass = SootResolver.v().makeClassRef(superClassName);
            sc.setSuperclass(sootSuperClass);
            deps.typesToHierarchy.add(sootSuperClass.getType());
        }

        // access flags
        int accessFlags = defItem.getAccessFlags();
        sc.setModifiers(accessFlags);

        // Retrieve interface names
        if (defItem.getInterfaces() != null) {
            for (String interfaceName : defItem.getInterfaces()) {
                String interfaceClassName = Util.dottedClassName(interfaceName);
                if (sc.implementsInterface(interfaceClassName)) {
                    continue;
                }

                SootClass interfaceClass = SootResolver.v().makeClassRef(interfaceClassName);
                interfaceClass.setModifiers(interfaceClass.getModifiers() | Modifier.INTERFACE);
                sc.addInterface(interfaceClass);
                deps.typesToHierarchy.add(interfaceClass.getType());
            }
        }

        if (options_oaat && sc.resolvingLevel() <= SootClass.HIERARCHY) {
            return deps;
        }
        DexAnnotation da = createDexAnnotation(sc, deps);

        // get the fields of the class
        for (org.jf.dexlib2.iface.Field sf : defItem.getStaticFields()) {
            loadField(sc, da, sf);
        }
        for (org.jf.dexlib2.iface.Field f : defItem.getInstanceFields()) {
            loadField(sc, da, f);
        }

        // get the methods of the class
        DexMethod dexMethod = createDexMethodFactory(dexEntry, sc);
        for (org.jf.dexlib2.iface.Method method : defItem.getDirectMethods()) {
            loadMethod(method, sc, da, dexMethod);
        }
        for (org.jf.dexlib2.iface.Method method : defItem.getVirtualMethods()) {
            loadMethod(method, sc, da, dexMethod);
        }

        da.handleClassAnnotation(defItem);

        // In contrast to Java, Dalvik associates the InnerClassAttribute
        // with the inner class, not the outer one. We need to copy the
        // tags over to correspond to the Soot semantics.
        InnerClassAttribute ica = (InnerClassAttribute) sc.getTag(InnerClassAttribute.NAME);
        if (ica != null) {
            Iterator<InnerClassTag> innerTagIt = ica.getSpecs().iterator();
            while (innerTagIt.hasNext()) {
                Tag t = innerTagIt.next();
                if (t instanceof InnerClassTag) {
                    InnerClassTag ict = (InnerClassTag) t;

                    // Get the outer class name
                    String outer = DexInnerClassParser.getOuterClassNameFromTag(ict);
                    if (outer == null || outer.length() == 0) {
                        // If we don't have any clue what the outer class is, we
                        // just remove the reference entirely
                        innerTagIt.remove();
                        continue;
                    }

                    // If the tag is already associated with the outer class,
                    // we leave it as it is
                    if (outer.equals(sc.getName())) {
                        continue;
                    }

                    // Check the inner class to make sure that this tag actually
                    // refers to the current class as the inner class
                    String inner = ict.getInnerClass().replaceAll("/", ".");
                    if (!inner.equals(sc.getName())) {
                        innerTagIt.remove();
                        continue;
                    }

                    SootClass osc = SootResolver.v().makeClassRef(outer);
                    if (osc == sc) {
                        if (!sc.hasOuterClass()) {
                            continue;
                        }
                        osc = sc.getOuterClass();
                    } else {
                        deps.typesToHierarchy.add(osc.getType());
                    }

                    // Get the InnerClassAttribute of the outer class
                    InnerClassAttribute icat = (InnerClassAttribute) osc.getTag(InnerClassAttribute.NAME);
                    if (icat == null) {
                        icat = new InnerClassAttribute();
                        osc.addTag(icat);
                    }

                    // Transfer the tag from the inner class to the outer class
                    icat.add(new InnerClassTag(ict.getInnerClass(), ict.getOuterClass(), ict.getShortName(), ict.getAccessFlags()));

                    // Remove the tag from the inner class as inner classes do
                    // not have these tags in the Java / Soot semantics. The
                    // DexPrinter will copy it back if we do dex->dex.
                    innerTagIt.remove();

                    // Add the InnerClassTag to the inner class. This tag will
                    // be put in an InnerClassAttribute
                    // within the PackManager in method handleInnerClasses().
                    if (!sc.hasTag(InnerClassTag.NAME)) {
                        if (((InnerClassTag) t).getInnerClass().replaceAll("/", ".").equals(sc.toString())) {
                            sc.addTag(t);
                        }
                    }
                }
            }
            // remove tag if empty
            if (ica.getSpecs().isEmpty()) {
                sc.getTags().remove(ica);
            }
        }

        return deps;
    }

    /**
     * Allow custom implementations to use different dex annotation implementations
     *
     * @param clazz
     * @param deps
     * @return
     */
    protected DexAnnotation createDexAnnotation(SootClass clazz, Dependencies deps) {
        return new DexAnnotation(clazz, deps);
    }

    /**
     * Allow custom implementations to use different dex method factories
     *
     * @param dexFile
     * @param sc
     * @return
     */
    protected DexMethod createDexMethodFactory(MultiDexContainer.DexEntry<? extends DexFile> dexEntry, SootClass sc) {
        return new DexMethod(dexEntry, sc);
    }


}

