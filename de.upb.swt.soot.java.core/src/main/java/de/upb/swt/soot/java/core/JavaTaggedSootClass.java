package de.upb.swt.soot.java.core;


import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.java.core.tag.AbstractHost;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class JavaTaggedSootClass extends AbstractHost {

  private JavaSootClass sootClass;

  public JavaTaggedSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    this.sootClass = new JavaSootClass(classSource, sourceType);
  }

  public JavaSootClass getSootClass() {
    return sootClass;
  }


  public Set<JavaTaggedSootMethod> getMethods() {
    Set<? extends JavaSootMethod> methods = getSootClass().getMethods();
    Set<JavaTaggedSootMethod> result = new HashSet<>();
    methods.forEach(m->result.add(new JavaTaggedSootMethod(m)));
    return result;
  }


  public JavaTaggedSootMethod getMethodByNameUnsafe(String key) {
    Set<JavaTaggedSootMethod> methods = getMethods();
    Optional<JavaTaggedSootMethod> first = methods.stream().filter(m -> m.getSootMethod().getName().equals(key)).findFirst();
    if(first.isPresent()){
      return first.get();
    }
    throw new RuntimeException("Ambiguous method" + key);
  }
}
