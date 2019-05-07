package de.upb.soot.frontends.java;

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassProvider;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;

/** @author Linghui Luo */
public class WalaJavaClassProvider implements IClassProvider {

  @Override
  public ClassSource createClassSource(
      INamespace srcNamespace, Path sourcePath, JavaClassType type) {
    return new WalaClassLoader(sourcePath.toString())
        .getClassSource(type)
        .orElseThrow(() -> new ResolveException("Could not resolve " + type + " in " + sourcePath));
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JAVA;
  }

  // TODO We might don't need this anymore?
  @Override
  public IClassSourceContent getContent(ClassSource classSource) {
    return classSource.getContent();
  }
}
