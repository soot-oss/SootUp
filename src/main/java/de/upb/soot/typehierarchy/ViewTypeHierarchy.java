package de.upb.soot.typehierarchy;

import com.google.common.base.Suppliers;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.AbstractClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.types.JavaClassType;
import de.upb.soot.views.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ViewTypeHierarchy implements TypeHierarchy {

  private static final Logger log = LoggerFactory.getLogger(ViewTypeHierarchy.class);

  private final Supplier<ScanResult> lazyScanResult = Suppliers.memoize(this::scanView);
  @Nonnull private final View view;

  ViewTypeHierarchy(@Nonnull View view) {
    this.view = view;
  }

  @Nonnull
  @Override
  public Set<JavaClassType> implementersOf(@Nonnull JavaClassType interfaceType) {
    return lazyScanResult.get().interfaceToImplementers.get(interfaceType);
  }

  @Nonnull
  @Override
  public Set<JavaClassType> subclassesOf(@Nonnull JavaClassType classType) {
    return lazyScanResult.get().classToSubclasses.get(classType);
  }

  @Nonnull
  @Override
  public Set<JavaClassType> implementedInterfacesOf(@Nonnull JavaClassType classType) {
    return sootClassFor(classType).getInterfaces();
  }

  @Nullable
  @Override
  public JavaClassType superClassOf(@Nonnull JavaClassType classType) {
    return sootClassFor(classType).getSuperclass().orElse(null);
  }

  @Nonnull
  private SootClass sootClassFor(@Nonnull JavaClassType classType) {
    AbstractClass<? extends AbstractClassSource> aClass =
        view.getClass(classType)
            .orElseThrow(
                () -> new ResolveException("Could not find " + classType + " in view " + view));
    if (!(aClass instanceof SootClass)) {
      throw new ResolveException("" + classType + " is not a regular Java class");
    }
    return (SootClass) aClass;
  }

  private ScanResult scanView() {
    long startNanos = System.nanoTime();
    Map<JavaClassType, Set<JavaClassType>> interfaceToImplementers = new HashMap<>();
    Map<JavaClassType, Set<JavaClassType>> classToSubclasses = new HashMap<>();

    for (AbstractClass<? extends AbstractClassSource> aClass : view.getClasses()) {
      if (!(aClass instanceof SootClass)) {
        continue;
      }

      SootClass sootClass = (SootClass) aClass;

      for (JavaClassType superClass : selfAndSuperClassesOf(sootClass)) {
        if (!superClass.equals(sootClass.getType())) {
          // This is an actual superClass of sootClass, so add sootClass as subclass of superClass
          classToSubclasses
              .computeIfAbsent(superClass, key -> new HashSet<>())
              .add(sootClass.getType());
        }

        // Iterate over interfaces implemented directly
        for (JavaClassType implementedInterface : sootClassFor(superClass).getInterfaces()) {
          // Add sootClass as implementer of this interface
          interfaceToImplementers
              .computeIfAbsent(implementedInterface, key -> new HashSet<>())
              .add(sootClass.getType());

          // Interfaces may extend other interfaces, so we iterate over those as well
          Set<JavaClassType> extendedInterfacesOfImplementedInterfaces =
              sootClassFor(implementedInterface).getInterfaces();
          for (JavaClassType extendedInterface : extendedInterfacesOfImplementedInterfaces) {
            interfaceToImplementers
                .computeIfAbsent(extendedInterface, key -> new HashSet<>())
                .add(sootClass.getType());
          }
        }
      }
    }

    double runtimeMs = (System.nanoTime() - startNanos) / 1e6;
    log.info("Type hierarchy scan took " + runtimeMs + " ms");
    return new ScanResult(interfaceToImplementers, classToSubclasses);
  }

  private List<JavaClassType> selfAndSuperClassesOf(SootClass sootClass) {
    JavaClassType superClass = sootClass.getSuperclass().orElse(null);
    List<JavaClassType> superClasses = new ArrayList<>();
    superClasses.add(sootClass.getType());
    while (superClass != null) {
      superClasses.add(superClass);
      superClass = sootClassFor(superClass).getSuperclass().orElse(null);
    }
    return superClasses;
  }

  private static class ScanResult {
    final Map<JavaClassType, Set<JavaClassType>> interfaceToImplementers;
    final Map<JavaClassType, Set<JavaClassType>> classToSubclasses;

    private ScanResult(
        Map<JavaClassType, Set<JavaClassType>> interfaceToImplementers,
        Map<JavaClassType, Set<JavaClassType>> classToSubclasses) {
      this.interfaceToImplementers = Collections.unmodifiableMap(interfaceToImplementers);
      this.classToSubclasses = Collections.unmodifiableMap(classToSubclasses);
    }
  }
}
