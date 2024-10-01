package sootup.java.bytecode.frontend.interceptors.typeresolving;

import java.util.*;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;
import sootup.interceptors.typeresolving.Typing;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.views.JavaView;

public class TypeAssignerTestSuite {

  JavaView view;
  ClassType classType;
  JavaSootClass clazz;

  public void buildView(String baseDir, String className) {

    AnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(
            baseDir, SourceType.Application, Collections.emptyList());
    AnalysisInputLocation rtJar =
        new DefaultRuntimeAnalysisInputLocation(SourceType.Application, Collections.emptyList());

    view = new JavaView(Arrays.asList(analysisInputLocation, rtJar));

    classType = view.getIdentifierFactory().getClassType(className);
    clazz = view.getClass(classType).get();
  }

  public Body.BodyBuilder createMethodsBuilder(String methodName, String returnType) {
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature(classType, methodName, returnType, Collections.emptyList());
    Optional<JavaSootMethod> methodOptional = clazz.getMethod(methodSignature.getSubSignature());
    JavaSootMethod method = methodOptional.get();
    Body body = method.getBody();
    return Body.builder(body, Collections.emptySet());
  }

  public Typing createTyping(Set<Local> locals, Map<String, Type> map) {
    /*
      // test creation helper to find missing type mappings
      final Optional<Local> foundOpt =
          locals.stream()
              .filter(local -> !map.containsKey(local.toString()))
              .peek(i -> System.out.println("TEST: missing mapping for: " + i))
              .findAny();
      Assert.assertFalse(foundOpt.isPresent());
    */

    Typing typing = new Typing(locals);
    for (Local l : typing.getLocals()) {
      if (map.containsKey(l.getName())) {
        typing.set(l, map.get(l.getName()));
      }
    }
    return typing;
  }
}
