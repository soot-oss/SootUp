package dexpler;

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class DexClassProvider implements ClassProvider {

  //  private static final Logger logger = LoggerFactory.getLogger(DexClassProvider.class);

  @Nonnull private final View view;

  public DexClassProvider(@Nonnull View view) {
    this.view = view;
  }

  @Override
  public Optional<SootClassSource> createClassSource(
      AnalysisInputLocation inputLocation, Path sourcePath, ClassType classSignature) {
    // TODO : In soot, they wrote this. This code makes sense, but dont know where to add this or
    // what will happen if we dont use it.
    //    ensureDexIndex();
    return Optional.of(new DexClassSource(view, inputLocation, classSignature, sourcePath));
  }

  //  private void ensureDexIndex() {
  //    SourceLocator sourceLocator = SourceLocator.getInstance();
  //    Map<String, File> index = sourceLocator.dexClassIndex();
  //    if (index == null) {
  //      index = new HashMap<String, File>();
  //      buildDexIndex(index, sourceLocator.getClassPath());
  //      sourceLocator.setDexClassIndex(index);
  //    }
  //  }
  //
  //  private void buildDexIndex(Map<String, File> index, List<String> classPath) {
  //    for (String path : classPath) {
  //      try {
  //        File dexFile = new File(path);
  //        if (dexFile.exists()) {
  //          for (DexFileProvider.DexContainer<? extends DexFile> container :
  //              DexFileProvider.getInstance()
  //                  .getDexFromSource(dexFile, getAPI_VERSIONFromClassPath(classPath))) {
  //            for (String className : classesOfDex(container.getBase().getDexFile())) {
  //              if (!index.containsKey(className)) {
  //                index.put(className, container.getFilePath());
  //              }
  //            }
  //          }
  //        }
  //      } catch (IOException e) {
  //        logger.warn("IO error while processing dex file '" + path + "'");
  //        logger.debug("Exception: " + e);
  //      } catch (Exception e) {
  //        logger.warn("exception while processing dex file '" + path + "'");
  //        logger.debug("Exception: " + e);
  //      }
  //    }
  //  }
  //
  //  public static Set<String> classesOfDex(DexFile dexFile) {
  //    Set<String> classes = new HashSet<String>();
  //    for (ClassDef c : dexFile.getClasses()) {
  //      classes.add(Util.Util.dottedClassName(c.getType()));
  //    }
  //    return classes;
  //  }
  //
  //  public int getAPI_VERSIONFromClassPath(List<String> classPath) {
  //    int API_VERSION = -1;
  //    for (String path : classPath) {
  //      if (path.contains("android.jar")) {
  //        // Define a pattern to match the number
  //        Pattern pattern = Pattern.compile("\\d+");
  //
  //        // Create a matcher with the input string
  //        Matcher matcher = pattern.matcher(path);
  //
  //        // Find the first match
  //        if (matcher.find()) {
  //          // Extract and print the matched number
  //          API_VERSION = Integer.parseInt(matcher.group());
  //        }
  //      }
  //    }
  //    return API_VERSION;
  //  }

  @Override
  public FileType getHandledFileType() {
    return FileType.DEX;
  }
}
