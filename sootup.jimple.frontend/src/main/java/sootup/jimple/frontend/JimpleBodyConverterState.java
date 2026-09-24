package sootup.jimple.frontend;

/*-
 * #%L
 * SootUp\
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.types.ClassType;
import sootup.core.types.UnknownType;

public class JimpleBodyConverterState {
  @NonNull private final Path path;
  @NonNull private final JimpleConverterUtil util;
  @NonNull private final ClassType clazz;
  @NonNull private final IdentifierFactory identifierFactory;
  @NonNull private final Map<BranchingStmt, List<String>> unresolvedBranches;
  @NonNull private final Map<String, Local> locals;

  public JimpleBodyConverterState(
      @NonNull Path path,
      @NonNull JimpleConverterUtil util,
      @NonNull ClassType clazz,
      @NonNull IdentifierFactory identifierFactory,
      @NonNull Map<BranchingStmt, List<String>> unresolvedBranches,
      @NonNull Map<String, Local> locals) {
    this.path = path;
    this.util = util;
    this.clazz = clazz;
    this.identifierFactory = identifierFactory;
    this.unresolvedBranches = unresolvedBranches;
    this.locals = locals;
  }

  @NonNull
  public Path getPath() {
    return path;
  }

  @NonNull
  public JimpleConverterUtil getUtil() {
    return util;
  }

  @NonNull
  public ClassType getClazz() {
    return clazz;
  }

  @NonNull
  public IdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  @NonNull
  public Map<BranchingStmt, List<String>> getUnresolvedBranches() {
    return unresolvedBranches;
  }

  @NonNull
  public Map<String, Local> getLocals() {
    return locals;
  }

  @NonNull
  public Local getLocal(@NonNull String name) {
    return locals.computeIfAbsent(name, (ignored) -> new Local(name, UnknownType.getInstance()));
  }
}
