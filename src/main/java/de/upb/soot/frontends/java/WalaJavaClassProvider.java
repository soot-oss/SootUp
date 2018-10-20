package de.upb.soot.frontends.java;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.JavaClassSignature;

import java.nio.file.Path;

public class WalaJavaClassProvider implements IClassProvider {

  @Override
  public AbstractClassSource createClassSource(INamespace srcNamespace, Path sourcePath, JavaClassSignature classSignature) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public de.upb.soot.namespaces.classprovider.ISourceContent getContent(AbstractClassSource classSource) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SootClass reify(AbstractClassSource classSource) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public AbstractClass resolve(AbstractClass sootClass) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SootMethod resolveMethodBody(SootMethod sootMethod) {
    // TODO Auto-generated method stub
    return null;
  }

}
