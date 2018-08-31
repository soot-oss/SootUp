package de.upb.soot.views;

import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.SignatureFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class SceneTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getSootClass() {
        SignatureFactory factory = new DefaultSignatureFactory(){};
        ClassSignature classSignature = factory.getClassSignature("java.lang.System");
        Scene project = new Scene();
        Optional<SootClass> sootClass = project.getClass(classSignature);
        assertTrue(sootClass.isPresent());


    }
}