package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJars extends BaseFixJarsTest {

@Test
public void executegantgroovyjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/org/codehaus/gant/gant_groovy2.1/1.9.11/gant_groovy2.1-1.9.11.jar";
    String methodSignature = "<gant.Gant: java.lang.Integer processArgs(java.lang.String[])>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}