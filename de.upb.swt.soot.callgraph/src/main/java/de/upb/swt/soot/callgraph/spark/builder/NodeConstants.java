package de.upb.swt.soot.callgraph.spark.builder;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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
public final class NodeConstants {
  private NodeConstants() {}

  public static final String OBJECT = "java.lang.Object";
  public static final String CLASS = "java.lang.Class";
  public static final String EXCEPTION = "java.lang.Exception";
  public static final String CLASS_LOADER = "java.lang.ClassLoader";
  public static final String STRING = "java.lang.String";
  public static final String HASH_SET = "java.util.HashSet";
  public static final String HASH_MAP = "java.util.HashMap";
  public static final String LINKED_LIST = "java.util.LinkedList";
  public static final String HASH_TABLE_EMPTY_ITERATOR = "java.util.Hashtable$EmptyIterator";
  public static final String HASH_TABLE_EMPTY_NUMERATOR = "java.util.Hashtable$EmptyEnumerator";
  public static final String THREAD = "java.lang.Thread";
  public static final String THREAD_GROUP = "java.lang.ThreadGroup";
  public static final String THROWABLE = "java.lang.Throwable";
  public static final String PRIVILEGED_ACTION_EXCEPTION = "java.security.PrivilegedActionException";
  public static final String FINALIZER = "java.lang.ref.Finalizer";
}
