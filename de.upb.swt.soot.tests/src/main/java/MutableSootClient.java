import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.MutableJavaView;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class MutableSootClient {

    public static void main (String[] args) {
        String javaClassPath = "./shared-test-resources/miniTestSuite/java6/binary/";

        AnalysisInputLocation<JavaSootClass> cpBased =
                new JavaClassPathAnalysisInputLocation(javaClassPath);

        JavaProject p = JavaProject.builder(new JavaLanguage(8)).addInputLocation(cpBased).build();
        MutableJavaView mv = p.createMutableFullView();

        ClassType classType = p.getIdentifierFactory().getClassType("TransientVariable");
        Optional<JavaSootClass> tvOpt = mv.getClass(classType);
        JavaSootClass tv = tvOpt.get();
        Set<? extends JavaSootMethod> methods = tv.getMethods();
        MethodSignature tvSig = p.getIdentifierFactory().getMethodSignature("transientVariable", classType, "void", Collections.emptyList());
        methods.removeIf(method -> method.getSignature().equals(tvSig));
        System.out.println(methods);

    }
}
