package dexpler;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import main.DexBody;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MultiDexContainer;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.model.SootMethod;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class DexMethod {

  protected final MultiDexContainer.DexEntry<? extends DexFile> dexEntry;
  protected final ClassType declaringclassType;

  public DexMethod(
      final MultiDexContainer.DexEntry<? extends DexFile> dexFile, final ClassType declaringClass) {
    this.dexEntry = dexFile;
    this.declaringclassType = declaringClass;
  }

  public SootMethod makeSootMethod(
      final Method method, List<BodyInterceptor> bodyInterceptors, @Nonnull View<?> view) {
    //        System.out.println(method.getName() + "    " +method.getDefiningClass() + "\n" +
    // "**********");
    int modifierFlags = method.getAccessFlags();
    if (Modifier.isAbstract(modifierFlags) || Modifier.isNative(modifierFlags)) {
      DexMethodSource dexMethodSource =
          new DexMethodSource(
              declaringclassType,
              Collections.emptySet(),
              new MutableBlockStmtGraph(),
              method,
              Collections.emptyList(),
              bodyInterceptors,
              view);
      return dexMethodSource.makeSootMethod();
    } else {
      DexBody dexBody = new DexBody(method, dexEntry, declaringclassType);
      SootMethod sootMethod =
          dexBody.makeSootMethod(method, declaringclassType, bodyInterceptors, view);
      //            System.out.println(sootMethod.getBody() + "\n" + "*********");
      return sootMethod;
    }
  }
}
