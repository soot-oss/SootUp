package sootup.java.bytecode.interceptors.typeresolving;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

public class TypeAssignerTestSuite {

  JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  JavaView view;
  ClassType classType;
  JavaSootClass clazz;
  Body.BodyBuilder builder;

  public void buildView(String baseDir, String className) {

    JavaClassPathAnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(baseDir);
    JavaClassPathAnalysisInputLocation rtJar =
        new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "/lib/rt.jar");
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(analysisInputLocation)
            .addInputLocation(rtJar)
            .build();
    view = project.createOnDemandView();
    classType = identifierFactory.getClassType(className);
    clazz = view.getClass(classType).get();
  }

  public StmtGraph<?> getMethod(String methodName, String returnType) {
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            classType, methodName, returnType, Collections.emptyList());
    Optional<JavaSootMethod> methodOptional = clazz.getMethod(methodSignature.getSubSignature());
    JavaSootMethod method = methodOptional.get();
    Body body = method.getBody();
    builder = Body.builder(body, Collections.emptySet());
    return builder.getStmtGraph();
  }

  public Typing createTyping(Map<String, Type> map) {
    final Set<Local> locals = builder.getLocals();
    Typing typing = new Typing(locals);
    for (Local l : typing.getLocals()) {
      // FIXME: [ZW] body contains null local!!! (shift)
      if (l == null) {
        continue;
      }
      if (map.containsKey(l.getName())) {
        typing.set(l, map.get(l.getName()));
      }
    }
    return typing;
  }
}
