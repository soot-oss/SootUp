package de.upb.sse.sootup.jimple.parser;

import de.upb.sse.sootup.core.frontend.ClassProvider;
import de.upb.sse.sootup.core.frontend.SootClassSource;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.core.inputlocation.FileType;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.transform.BodyInterceptor;
import de.upb.sse.sootup.core.types.ClassType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStreams;

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
