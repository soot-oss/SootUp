package de.upb.soot.frontends.java;

import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.IClassSourceContent;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class source for resolving from .java files using wala java source front-end.
 *
 * @author Linghui Luo
 */
public class EagerJavaClassSource extends ClassSource {

  private final IClassSourceContent content;

  public EagerJavaClassSource(
      INamespace srcNamespace,
      Path sourcePath,
      JavaClassType classType,
      JavaClassType superClass,
      Set<JavaClassType> interfaces,
      JavaClassType outerClass,
      Set<SootField> sootFields,
      Set<SootMethod> sootMethods,
      Position position,
      EnumSet<Modifier> modifiers) {
    super(srcNamespace, sourcePath, classType);
    content =
        new EagerJavaClassSourceContent(
            superClass,
            interfaces,
            outerClass,
            sootFields,
            sootMethods,
            position,
            modifiers,
            classType);
  }

  @Override
  public IClassSourceContent getContent() {
    return content;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    EagerJavaClassSource that = (EagerJavaClassSource) o;
    return Objects.equals(content, that.content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), content);
  }

  @Override
  public String toString() {
    return "EagerJavaClassSource{" + "content=" + content + '}';
  }
}
