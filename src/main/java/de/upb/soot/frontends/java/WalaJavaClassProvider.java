package de.upb.soot.frontends.java;

import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootMethod;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;

public class WalaJavaClassProvider implements IClassProvider {

  @Override
  public AbstractClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object getContent(AbstractClassSource classSource) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SootClass reify(AbstractClassSource classSource) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SootClass resolve(SootClass sootClass) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SootMethod resolveMethodBody(SootMethod sootMethod) {
    // TODO Auto-generated method stub
    return null;
  }

}
