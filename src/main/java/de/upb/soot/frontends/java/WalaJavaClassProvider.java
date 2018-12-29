package de.upb.soot.frontends.java;

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.namespaces.classprovider.IClassSourceContent;
import de.upb.soot.signatures.JavaClassSignature;

import java.nio.file.Path;

public class WalaJavaClassProvider implements IClassProvider {

  @Override
  public ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, JavaClassSignature classSignature) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IClassSourceContent getContent(ClassSource classSource) {
    // TODO Auto-generated method stub
    return null;
  }

}
