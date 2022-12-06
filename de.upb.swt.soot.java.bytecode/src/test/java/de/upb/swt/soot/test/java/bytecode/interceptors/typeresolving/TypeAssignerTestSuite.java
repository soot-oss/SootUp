package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.Typing;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TypeAssignerTestSuite {

  JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  JavaView view;
  ClassType classType;
  SootClass clazz;
  Body body;
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

  public void setMethodBody(String methodName, String returnType, List<Type> paras) {
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            classType, methodName, returnType, Collections.emptyList());
    Optional<JavaSootMethod> methodOptional = clazz.getMethod(methodSignature.getSubSignature());
    JavaSootMethod method = methodOptional.get();
    body = method.getBody();
    builder = new Body.BodyBuilder(body, Collections.emptySet());
  }

  public Typing createTyping(Map<String, Type> map) {
    Typing typing = new Typing(body.getLocals());
    for (Local l : typing.getLocals()) {
      // todo: [problem] body contains null local!!! (shift)
      if (l == null) {
        continue;
      }
      if (map.keySet().contains(l.getName())) {
        typing.set(l, map.get(l.getName()));
      }
    }
    return typing;
  }
}
