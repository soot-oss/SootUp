package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

public class FixJars extends BaseFixJarsTest {

@Test
public void executedatabasemysqlimplRCjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/eu/cloudnetservice/cloudnet/database-mysql-impl/4.0.0-RC12/database-mysql-impl-4.0.0-RC12.jar";
    String methodSignature = "";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}