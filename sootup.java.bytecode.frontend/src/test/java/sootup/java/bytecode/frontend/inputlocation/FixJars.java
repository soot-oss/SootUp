package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

public class FixJars extends BaseFixJarsTest {

@Test
public void executesouthboundkarafBerylliumSRjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/org/opendaylight/ovsdb/southbound-karaf/1.2.2-Beryllium-SR1/southbound-karaf-1.2.2-Beryllium-SR1.jar";
    String methodSignature = "";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}