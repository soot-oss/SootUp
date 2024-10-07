package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJars extends BaseFixJarsTest {

@Test
public void executesqlancerjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/com/sqlancer/sqlancer/2.0.0/sqlancer-2.0.0.jar";
    String methodSignature = "<sqlancer.Main$DBMSExecutor: void testConnection()>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}