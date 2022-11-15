package sootup.jimple.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStreams;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SootClass;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;

/** @author Markus Schmidt */
public class JimpleClassProvider<T extends SootClass<? extends SootClassSource<T>>>
    implements ClassProvider<T> {

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  public JimpleClassProvider(List<BodyInterceptor> bodyInterceptors) {
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  public SootClassSource<T> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> inputlocation,
      Path sourcePath,
      ClassType classSignature) {

    try {
      final JimpleConverter jimpleConverter = new JimpleConverter();
      return jimpleConverter.run(
          CharStreams.fromPath(sourcePath), inputlocation, sourcePath, bodyInterceptors);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JIMPLE;
  }
}
