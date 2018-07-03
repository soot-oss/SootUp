package de.upb.soot.namespaces;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;

/**
 * @author Manuel Benz created on 07.06.18
 */
class DummyClassProvider implements IClassProvider {

  public DummyClassProvider() {
  }

  @Override
  public SootClass getSootClass(ClassSource classSource) {
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
