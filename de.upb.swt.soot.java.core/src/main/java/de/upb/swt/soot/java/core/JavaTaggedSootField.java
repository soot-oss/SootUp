package de.upb.swt.soot.java.core;

import de.upb.swt.soot.java.core.tag.AbstractHost;

public class JavaTaggedSootField extends AbstractHost {

    private JavaSootField sootField;

    public JavaTaggedSootField(JavaSootField sootField){
        this.sootField = sootField;
    }

    public JavaSootField getSootField() {
        return sootField;
    }
}
