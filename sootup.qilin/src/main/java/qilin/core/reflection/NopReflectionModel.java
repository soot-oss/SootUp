/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.reflection;

import java.util.Collection;
import java.util.Collections;
import sootup.core.jimple.common.stmt.Stmt;

/*
 * This is an empty reflection model which does nothing for reflection statements.
 * */

public class NopReflectionModel extends ReflectionModel {

  @Override
  Collection<Stmt> transformClassForName(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformClassNewInstance(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformContructorNewInstance(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformMethodInvoke(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformFieldSet(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformFieldGet(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformArrayNewInstance(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformArrayGet(Stmt s) {
    return Collections.emptySet();
  }

  @Override
  Collection<Stmt> transformArraySet(Stmt s) {
    return Collections.emptySet();
  }
}
