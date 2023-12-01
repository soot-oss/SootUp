package dexpler;

import Util.SourceLocator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;

public class DexClassProvider implements ClassProvider<JavaSootClass> {

  private static final Logger logger = LoggerFactory.getLogger(DexClassProvider.class);

  @Nonnull private final View<?> view;

  public DexClassProvider(@Nonnull View<?> view) {
    this.view = view;
  }

  @Override
  public Optional<SootClassSource<JavaSootClass>> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> inputLocation,
      Path sourcePath,
      ClassType classSignature) {
    ensureDexIndex();
    return Optional.of(new DexClassSource(inputLocation, classSignature, sourcePath));
  }

  private void ensureDexIndex() {
    SourceLocator sourceLocator = SourceLocator.getInstance();
    Map<String, File> index = sourceLocator.dexClassIndex();
    if (index == null) {
      index = new HashMap<String, File>();
      buildDexIndex(index, sourceLocator.getClassPath());
      sourceLocator.setDexClassIndex(index);
    }
  }

  private void buildDexIndex(Map<String, File> index, List<String> classPath) {
    for (String path : classPath) {
      try {
        File dexFile = new File(path);
        if (dexFile.exists()) {
          for (DexFileProvider.DexContainer<? extends DexFile> container :
              new DexFileProvider()
                  .getDexFromSource(dexFile, getAPI_VERSIONFromClassPath(classPath))) {
            for (String className : classesOfDex(container.getBase().getDexFile())) {
              if (!index.containsKey(className)) {
                index.put(className, container.getFilePath());
              }
            }
          }
        }
      } catch (IOException e) {
        logger.warn("IO error while processing dex file '" + path + "'");
        logger.debug("Exception: " + e);
      } catch (Exception e) {
        logger.warn("exception while processing dex file '" + path + "'");
        logger.debug("Exception: " + e);
      }
    }
  }

  public static Set<String> classesOfDex(DexFile dexFile) {
    Set<String> classes = new HashSet<String>();
    for (ClassDef c : dexFile.getClasses()) {
      classes.add(Util.Util.dottedClassName(c.getType()));
    }
    return classes;
  }

  public int getAPI_VERSIONFromClassPath(List<String> classPath) {
    int API_VERSION = -1;
    for (String path : classPath) {
      if (path.contains("android.jar")) {
        // Define a pattern to match the number
        Pattern pattern = Pattern.compile("\\d+");

        // Create a matcher with the input string
        Matcher matcher = pattern.matcher(path);

        // Find the first match
        if (matcher.find()) {
          // Extract and print the matched number
          API_VERSION = Integer.parseInt(matcher.group());
        }
      }
    }
    return API_VERSION;
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.DEX;
  }
}
