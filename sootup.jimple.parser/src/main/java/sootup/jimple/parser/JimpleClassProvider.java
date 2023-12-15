package sootup.jimple.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.ResolveException;
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

  private static final @Nonnull Logger logger = LoggerFactory.getLogger(JimpleClassProvider.class);

  public JimpleClassProvider(List<BodyInterceptor> bodyInterceptors) {
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  public Optional<SootClassSource<T>> createClassSource(
      AnalysisInputLocation<? extends SootClass<?>> inputlocation,
      Path sourcePath,
      ClassType classSignature) {

    try {
      final JimpleConverter jimpleConverter = new JimpleConverter();
      return Optional.of(
          jimpleConverter.run(
              CharStreams.fromPath(sourcePath), inputlocation, sourcePath, bodyInterceptors));
    } catch (IOException | ResolveException e) {
      logger.warn(
          "The jimple file of "
              + classSignature
              + " in path: "
              + sourcePath
              + " could not be converted because of: "
              + e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JIMPLE;
  }
}
