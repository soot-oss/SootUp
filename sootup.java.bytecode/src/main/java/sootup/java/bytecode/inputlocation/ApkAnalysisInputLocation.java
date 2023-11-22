package sootup.java.bytecode.inputlocation;

import com.googlecode.dex2jar.tools.Dex2jarCmd;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.model.SourceType;

public class ApkAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

  public ApkAnalysisInputLocation(@Nonnull Path path, @Nullable SourceType srcType) {
    super(path, srcType);
    String jarPath = dex2jar(path);
    this.path = Paths.get(jarPath);
  }

  private String dex2jar(Path path) {
    String apkPath = path.toAbsolutePath().toString();
    String outDir = "./tmp/";
    int start = apkPath.lastIndexOf(File.separator);
    int end = apkPath.lastIndexOf(".apk");
    String outputFile = outDir + apkPath.substring(start + 1, end) + ".jar";
    Dex2jarCmd.main("-f", apkPath, "-o", outputFile);
    return outputFile;
  }
}
