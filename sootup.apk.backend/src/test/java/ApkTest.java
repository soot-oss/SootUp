import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import sootup.apk.backend.DexOutputLocation;
import sootup.apk.frontend.ApkAnalysisInputLocation;
import sootup.apk.frontend.DexBodyInterceptors;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.views.View;
import sootup.java.core.views.JavaView;

public class ApkTest {

  View createView(String apk, String androidPlatformsPath) {
    Path apkPath = Paths.get(apk);
    ApkAnalysisInputLocation inputLocation =
        new ApkAnalysisInputLocation(
            apkPath,
            new AndroidVersionInfo(apkPath, androidPlatformsPath),
            DexBodyInterceptors.Default.bodyInterceptors());
    return new JavaView(inputLocation);
  }

  // @Test
  void apkTest1() throws IOException {
    String apk = "src/test/resources/FlowSensitivity1.apk";
    Path apkPath = Paths.get(apk);
    String androidPlatformsPath = "src/test/resources/platforms/android-19/android.jar";
    View view = createView(apk, androidPlatformsPath);
    long classCount = view.getClasses().count();
    AtomicLong methodCount = new AtomicLong();
    AtomicLong stmtCount = new AtomicLong();

    view.getClasses()
        .forEach(
            clazz -> {
              clazz
                  .getMethods()
                  .forEach(
                      method -> {
                        methodCount.incrementAndGet();

                        if (method.hasBody()) {
                          stmtCount.addAndGet(method.getBody().getStmts().size());
                        }
                      });
            });
    assertEquals(740, classCount);
    assertEquals(6220, methodCount.get());
    assertEquals(77745, stmtCount.get());
    DexOutputLocation dexOutputLocation = new DexOutputLocation(view, apkPath.toFile());
    dexOutputLocation.writeApk(
        "src/test/resources", new AndroidVersionInfo(apkPath, androidPlatformsPath));
    String new_apk = "src/test/resources/FlowSensitivity11.apk";
    View view_new = createView(new_apk, androidPlatformsPath);
    long classCount_new = view_new.getClasses().count();
    AtomicLong methodCount_new = new AtomicLong();
    AtomicLong stmtCount_new = new AtomicLong();

    view_new
        .getClasses()
        .forEach(
            clazz -> {
              clazz
                  .getMethods()
                  .forEach(
                      method -> {
                        methodCount_new.incrementAndGet();

                        if (method.hasBody()) {
                          stmtCount_new.addAndGet(method.getBody().getStmts().size());
                        }
                      });
            });

    assertEquals(classCount, classCount_new);
    assertEquals(methodCount.get(), methodCount_new.get());
    System.out.println(stmtCount_new.get());
    Files.delete(Paths.get(new_apk));
  }

  // @Test
  void apkTest2() throws IOException {
    String apk = "src/test/resources/Crypto.apk";
    Path apkPath = Paths.get(apk);
    String androidPlatformsPath = "src/test/resources/platforms/android-19/android.jar";
    View view = createView(apk, androidPlatformsPath);
    long classCount = view.getClasses().count();
    AtomicLong methodCount = new AtomicLong();
    AtomicLong stmtCount = new AtomicLong();

    view.getClasses()
        .forEach(
            clazz -> {
              clazz
                  .getMethods()
                  .forEach(
                      method -> {
                        methodCount.incrementAndGet();

                        if (method.hasBody()) {
                          stmtCount.addAndGet(method.getBody().getStmts().size());
                        }
                      });
            });
    DexOutputLocation dexOutputLocation = new DexOutputLocation(view, apkPath.toFile());
    dexOutputLocation.writeApk(
        "src/test/resources", new AndroidVersionInfo(apkPath, androidPlatformsPath));
    String new_apk = "src/test/resources/Crypto1.apk";
    View view_new = createView(new_apk, androidPlatformsPath);
    long classCount_new = view_new.getClasses().count();
    AtomicLong methodCount_new = new AtomicLong();
    AtomicLong stmtCount_new = new AtomicLong();

    view_new
        .getClasses()
        .forEach(
            clazz -> {
              clazz
                  .getMethods()
                  .forEach(
                      method -> {
                        methodCount_new.incrementAndGet();

                        if (method.hasBody()) {
                          stmtCount_new.addAndGet(method.getBody().getStmts().size());
                        }
                      });
            });

    assertEquals(classCount, classCount_new);
    assertEquals(methodCount.get(), methodCount_new.get());
    System.out.println(stmtCount.get());
    System.out.println(stmtCount_new.get());
    Files.delete(Paths.get(new_apk));
  }

  // @Test
  void apkTest3() throws IOException {
    String apk = "src/test/resources/LocationLeak1.apk";
    Path apkPath = Paths.get(apk);
    String androidPlatformsPath = "src/test/resources/platforms/android-19/android.jar";
    View view = createView(apk, androidPlatformsPath);
    long classCount = view.getClasses().count();
    AtomicLong methodCount = new AtomicLong();
    AtomicLong stmtCount = new AtomicLong();

    view.getClasses()
        .forEach(
            clazz -> {
              clazz
                  .getMethods()
                  .forEach(
                      method -> {
                        methodCount.incrementAndGet();

                        if (method.hasBody()) {
                          stmtCount.addAndGet(method.getBody().getStmts().size());
                        }
                      });
            });
    DexOutputLocation dexOutputLocation = new DexOutputLocation(view, apkPath.toFile());
    dexOutputLocation.writeApk(
        "src/test/resources", new AndroidVersionInfo(apkPath, androidPlatformsPath));
    String new_apk = "src/test/resources/LocationLeak11.apk";
    View view_new = createView(new_apk, androidPlatformsPath);
    long classCount_new = view_new.getClasses().count();
    AtomicLong methodCount_new = new AtomicLong();
    AtomicLong stmtCount_new = new AtomicLong();

    view_new
        .getClasses()
        .forEach(
            clazz -> {
              clazz
                  .getMethods()
                  .forEach(
                      method -> {
                        methodCount_new.incrementAndGet();

                        if (method.hasBody()) {
                          stmtCount_new.addAndGet(method.getBody().getStmts().size());
                        }
                      });
            });

    assertEquals(classCount, classCount_new);
    assertEquals(methodCount.get(), methodCount_new.get());
    System.out.println(stmtCount.get());
    System.out.println(stmtCount_new.get());
    Files.delete(Paths.get(new_apk));
  }
}
