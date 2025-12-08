package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

public class FixJars extends BaseFixJarsTest {

@Test
public void executeetsgpkgjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/org/opengis/cite/ets-gpkg12/1.3/ets-gpkg12-1.3.jar";
    String methodSignature = "<org.opengis.cite.gpkg12.core.SQLiteContainerTests: void sqlCheck()>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}