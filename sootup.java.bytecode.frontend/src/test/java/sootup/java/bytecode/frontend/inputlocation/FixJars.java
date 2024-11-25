package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJars extends BaseFixJarsTest {

@Test
public void executesdktoolsjvmalphajar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/money/terra/sdk-tools-jvm/0.20.6-alpha1/sdk-tools-jvm-0.20.6-alpha1.jar";
    String methodSignature = "<money.terra.sdk.tools.transaction.LocalSemaphoreProvider$acquire$1: java.lang.Object invokeSuspend(java.lang.Object)>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}