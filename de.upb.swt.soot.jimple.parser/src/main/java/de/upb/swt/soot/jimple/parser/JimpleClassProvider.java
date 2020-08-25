package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import java.io.IOException;
import java.nio.file.Path;
import org.antlr.v4.runtime.CharStreams;

public class JimpleClassProvider implements ClassProvider {

  @Override
  public AbstractClassSource createClassSource(
      AnalysisInputLocation inputlocation, Path sourcePath, ClassType classSignature) {

    try {

      /*
          CharStream input = CharStreams.fromString(source);
          CharStream input = CharStreams.fromPath(path);
      */

      new JimpleReader().run(CharStreams.fromPath(sourcePath));

      /*
      return new OverridingClassSource(
              inputlocation,
              sourcePath,
              clazz,
              superclass,
              interfaces,
              outerclass,
              fields,
              methods,
              position,
              modifier);
      */

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
