package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

public class FixJars extends BaseFixJarsTest {

@Test
public void executegantgroovyjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/org/codehaus/gant/gant_groovy1.9/1.9.6/gant_groovy1.9-1.9.6.jar";
    String methodSignature = "<gant.Gant$_dispatch_closure4: java.lang.Object doCall()>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}