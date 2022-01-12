package de.upb.swt.soot.test.core.views;

import static org.junit.Assert.fail;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import de.upb.swt.soot.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;

public class ValidateFieldsInClass {
	
	@Test
	public void test() {
		String className = "ViewTest";
		double version = Double.parseDouble(System.getProperty("java.specification.version"));
        if (version > 1.8) {
            fail("The rt.jar is not available after Java 8. You are using version " + version);
        }
        
        JavaProject javaProject =
                JavaProject.builder(new JavaLanguage(8))
                        .addInputLocation(
                                new JavaClassPathAnalysisInputLocation(
                                       System.getProperty("java.home") + "/lib/rt.jar"))
                        .addInputLocation(new JavaSourcePathAnalysisInputLocation("src/test/resources/views"))
                        .build();
        
        JavaView view = javaProject.createOnDemandView();
        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        JavaClassType mainClassSignature = identifierFactory.getClassType(className);
        
        //Defining fieldSubClass present in sub class
        FieldSignature fieldSign = new FieldSignature(mainClassSignature, "fieldSubClass", PrimitiveType.getInt());
        Optional<? extends SootField> field = view.getField(fieldSign);
        Assert.assertTrue(field.isPresent());
        
        //Defining fieldSuper present in super class
        FieldSignature fieldsignSuper = new FieldSignature(mainClassSignature, "fieldSuper", PrimitiveType.getInt());
        Optional<? extends SootField> fieldSuper = view.getField(fieldsignSuper);
        Assert.assertTrue(fieldSuper.isPresent());
        
        //Defining fieldRandom not present in any class
        FieldSignature fieldSignRandom = new FieldSignature(mainClassSignature, "fieldRandom", PrimitiveType.getInt());
        Optional<? extends SootField> fieldRandom = view.getField(fieldSignRandom);
        Assert.assertFalse(fieldRandom.isPresent());
        
        
	}

}
