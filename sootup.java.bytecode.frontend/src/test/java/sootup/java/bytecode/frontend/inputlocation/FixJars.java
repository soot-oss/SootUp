package sootup.java.bytecode.frontend.inputlocation;

import categories.TestCategories;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class FixJars extends BaseFixJarsTest {

@Test
public void executeexamcorejar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/io/github/adven27/exam-core/2024.0.10/exam-core-2024.0.10.jar";
    String methodSignature = "<io.github.adven27.concordion.extensions.exam.core.logger.LoggingFormatterExtension$LoggingFormatterListener: void afterExample(org.concordion.api.listener.ExampleEvent)>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

@Test
public void executeaudiofileRCjar(){
	String jarDownloadUrl = "https://repo1.maven.org/maven2/de/sciss/audiofile_3.0.0-RC2/2.3.3/audiofile_3.0.0-RC2-2.3.3.jar";
    String methodSignature = "<de.sciss.audiofile.AudioFile$AsyncBasic: scala.concurrent.Future close()>";
    JavaView javaView = supplyJavaView(jarDownloadUrl);
    assertMethodConversion(javaView,methodSignature);
    assertJar(javaView);
}

}