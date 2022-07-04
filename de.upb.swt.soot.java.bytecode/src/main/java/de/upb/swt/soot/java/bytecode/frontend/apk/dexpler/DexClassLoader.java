package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

import com.sun.tools.classfile.Dependencies;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootField;
import de.upb.swt.soot.java.core.JavaTaggedSootClass;
import de.upb.swt.soot.java.core.JavaTaggedSootField;
import de.upb.swt.soot.java.core.views.JavaView;
import org.jf.dexlib2.iface.*;

import java.util.*;
import java.util.stream.Collectors;

public class DexClassLoader {

    private JavaView view;

    public DexClassLoader(JavaView view){
        this.view = view;
    }

    public JavaTaggedSootClass makeSootClass(JavaTaggedSootClass sc, ClassDef defItem, MultiDexContainer.DexEntry<? extends DexFile> dexEntry){
        DexAnnotation da = createDexAnnotation(sc);
        JavaTaggedSootClass sootClass = sc;
        // get the fields of the class
        List<JavaTaggedSootField> fields = new ArrayList<>();
        for (Field sf : defItem.getStaticFields()) {
            JavaTaggedSootField sootField = loadField(sootClass, da, sf);
            fields.add(sootField);
        }
        for (Field f : defItem.getInstanceFields()) {
            JavaTaggedSootField sootField = loadField(sootClass, da, f);
            fields.add(sootField);
        }
        Collection<SootField> sootFields = fields.stream().map(JavaTaggedSootField::getSootField).collect(Collectors.toSet());
        JavaTaggedSootClass taggedClass = new JavaTaggedSootClass(sootClass.getSootClass().withFields(sootFields));
        taggedClass.addAllTagsOf(sootClass);
        return taggedClass;
    }

    protected DexAnnotation createDexAnnotation(JavaTaggedSootClass clazz) {
        return new DexAnnotation(clazz);
    }

    protected JavaTaggedSootField loadField(JavaTaggedSootClass declaringClass, DexAnnotation annotations, Field sf) {
        // TODO: KKwip checking first causes infinite loop
//        if(declaringClass.getSootClass().getField(sf.getName()).isPresent()){
//            return declaringClass;
//        }

        //TODO: KKwip
//        if (declaringClass.declaresField(sf.getName(), DexType.toSoot(sf.getType()))) {
//            return;
//        }

        JavaTaggedSootField sootField = DexField.makeSootField(declaringClass, sf, view);
        annotations.handleFieldAnnotation(sootField, sf);
        return sootField;
    }
}
