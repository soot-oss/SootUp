import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import sootup.apk.parser.main.AndroidVersionInfo;

@RunWith(MockitoJUnitRunner.class)
public class AndroidVersionInfoTest {

  @Mock private File mockJarsFile;

  @Mock private File mockJarPathFile;

  @InjectMocks private AndroidVersionInfo androidVersionInfo;

  public AndroidVersionInfoTest() {}

  @Before
  public void setUp() {
    // Setting up mocks
    mockJarsFile = mock(File.class);
    mockJarPathFile = mock(File.class);

    // Initialize the class with mock data
    androidVersionInfo =
        new AndroidVersionInfo(Paths.get("resources/FlowSensitivity1.apk"), "mock/jar/path");
  }

  @Test
  public void testGetApiVersion() {
    // Mock File behavior for jars path
    String jarsPath = "mock/jar/path";
    when(mockJarsFile.exists()).thenReturn(true);

    // Mock File behavior for jarPath
    String jarPath =
        jarsPath + File.separatorChar + "android-" + 19 + File.separatorChar + "android.jar";
    when(mockJarPathFile.isFile()).thenReturn(true);

    // Create a spy to mock the newFile method
    AndroidVersionInfo spyAndroidVersionInfo = Mockito.spy(androidVersionInfo);
    doReturn(mockJarsFile).when(spyAndroidVersionInfo).newFile(jarsPath);
    doReturn(mockJarPathFile).when(spyAndroidVersionInfo).newFile(jarPath);

    // Call the method to test
    int apiVersion = spyAndroidVersionInfo.getApi_version();

    // Verify the method interactions and assert the expected result
    verify(spyAndroidVersionInfo, times(1)).getAndroidJarPath(anyString(), any());
    assertEquals(19, apiVersion);
  }
}
