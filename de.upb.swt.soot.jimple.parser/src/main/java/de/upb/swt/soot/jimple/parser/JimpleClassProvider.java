package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.CharStreams;

public class JimpleClassProvider implements ClassProvider {

  @Nonnull private final List<BodyInterceptor> bodyInterceptors;

  public JimpleClassProvider(List<BodyInterceptor> bodyInterceptors) {
    this.bodyInterceptors = bodyInterceptors;
  }

  @Override
  public AbstractClassSource createClassSource(
      AnalysisInputLocation inputlocation, Path sourcePath, ClassType classSignature) {

    // TODO: create new OverridingClassSource instance

    try {

      /*
          CharStream input = CharStreams.fromString(source);
          CharStream input = CharStreams.fromPath(path);
      */

      final JimpleReader jimpleReader = new JimpleReader();
      return jimpleReader.run(
          CharStreams.fromPath(sourcePath), inputlocation, sourcePath, classSignature);
    } catch (IOException e) {
      // TODO exception
      throw new RuntimeException(e);
    }
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JIMPLE;
  }
}
