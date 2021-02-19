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

  public static final String JAVA_LANG = "java.lang";
  public static final String JAVA_UTIL = "java.util";
  public static final String JAVA_SECURITY = "java.security";
  public static final String OBJECT = "Object";
  public static final String CLASS = "Class";
  public static final String CLASS_LOADER = "ClassLoader";
  public static final String STRING = "String";
  public static final String HASH_SET = "HashSet";
  public static final String HASH_MAP = "HashMap";
  public static final String LINKED_LIST = "LinkedList";
  public static final String HASH_TABLE_EMPTY_ITERATOR = "Hashtable$EmptyIterator";
  public static final String HASH_TABLE_EMPTY_NUMERATOR = "Hashtable$EmptyEnumerator";
  public static final String THREAD = "Thread";
  public static final String THREAD_GROUP = "ThreadGroup";
  public static final String THROWABLE = "Throwable";
  public static final String PRIVILEGED_ACTION_EXCEPTION = "PrivilegedActionException";
}
