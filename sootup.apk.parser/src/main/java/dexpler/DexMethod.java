package dexpler;

import Util.DexUtil;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import main.DexBody;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.signatures.MethodSignature;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.JavaSootMethod;

public class DexMethod {

  protected final MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
  protected final ClassType declaringclassType;

  public DexMethod(
      final MultiDexContainer.DexEntry<? extends DexFile> dexFile, final ClassType declaringClass) {
    this.dexEntry = dexFile;
    this.declaringclassType = declaringClass;
  }

  public JavaSootMethod makeSootMethod(
      final Method method, List<BodyInterceptor> bodyInterceptors, @Nonnull View view) {
    int modifierFlags = method.getAccessFlags();
    if (Modifier.isAbstract(modifierFlags) || Modifier.isNative(modifierFlags)) {
      String className = declaringclassType.getClassName();
      if (Util.Util.isByteCodeClassName(className)) {
        className = Util.Util.dottedClassName(className);
      }
      MethodSignature methodSignature =
          new MethodSignature(
              declaringclassType,
              className,
              Collections.emptyList(),
              DexUtil.toSootType(method.getReturnType(), 0));
      DexMethodSource dexMethodSource =
          new DexMethodSource(
              Collections.emptySet(),
              methodSignature,
              new MutableBlockStmtGraph(),
              method,
              bodyInterceptors,
              view);
      return dexMethodSource.makeSootMethod();
    } else {
      DexBody dexBody = new DexBody(method, dexEntry, declaringclassType);
      return dexBody.makeSootMethod(method, declaringclassType, bodyInterceptors, view);
    }
  }
}
