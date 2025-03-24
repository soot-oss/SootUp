package sootup.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2021 Bastian Haverkamp
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;

public class JavaAnnotationSootClass extends JavaSootClass {

  public JavaAnnotationSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
  }

  @NonNull
  public Set<JavaAnnotationSootMethod> getAnnotationMethods() {
    return super.getMethods().stream()
        .map(method -> (JavaAnnotationSootMethod) method)
        .collect(Collectors.toSet());
  }

  @NonNull
  public Map<String, Object> getDefaultValues() {
    return getAnnotationMethods().stream()
        .collect(Collectors.toMap(SootMethod::getName, JavaAnnotationSootMethod::getDefaultValue));
  }
}
