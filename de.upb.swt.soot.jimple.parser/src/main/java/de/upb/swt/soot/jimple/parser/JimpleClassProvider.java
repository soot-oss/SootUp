package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import java.io.IOException;
import java.nio.file.Path;
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
