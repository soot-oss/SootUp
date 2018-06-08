package de.upb.soot.namespaces;

import de.upb.soot.signatures.ClassSignature;
import org.junit.Test;

import static org.junit.Assert.*;

public class JrtFSNamespaceTest extends AbstractNamespaceTest{

    @Test
    public void getClassSource() {
        JrtFSNamespace ns = new JrtFSNamespace(getClassProvider());
        final ClassSignature sig = getSignatureFactory().getClassSignature("java.lang.System");

        ns.getClassSource(sig);
    }

    @Test
    public void getClassSources() {
    }
}