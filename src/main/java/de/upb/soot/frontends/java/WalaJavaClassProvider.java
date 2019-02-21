package de.upb.soot.frontends.java;

import java.nio.file.Path;

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.JavaClassSignature;

/**
 * 
 * @author Linghui Luo
 *
 */
public class WalaJavaClassProvider implements IClassProvider {

  @Override
  public ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, JavaClassSignature classSignature) {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    // TODO Auto-generated methodRef stub
    return null;
  }

  @Override
  public IClassSourceContent getContent(ClassSource classSource) {
    // TODO Auto-generated methodRef stub
    return null;
  }

}
