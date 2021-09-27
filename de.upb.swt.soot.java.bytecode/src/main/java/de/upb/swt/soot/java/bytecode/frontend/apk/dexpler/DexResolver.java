package de.upb.swt.soot.java.bytecode.frontend.apk.dexpler;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 *
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 *
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

import soot.G;
import soot.Singletons;
import soot.SootClass;
import soot.javaToJimple.IInitialResolver.Dependencies;
import soot.tagkit.SourceFileTag;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class DexResolver {

  protected Map<File, soot.dexpler.DexlibWrapper> cache = new TreeMap<File, soot.dexpler.DexlibWrapper>();

  public DexResolver(Singletons.Global g) {
  }

  public static DexResolver v() {
    return G.v().soot_dexpler_DexResolver();
  }

  /**
   * Resolve the class contained in file into the passed soot class.
   *
   * @param file
   *          the path to the dex/apk file to resolve
   * @param className
   *          the name of the class to resolve
   * @param sc
   *          the soot class that will represent the class
   * @return the dependencies of this class.
   */
  public Dependencies resolveFromFile(File file, String className, SootClass sc) {
    soot.dexpler.DexlibWrapper wrapper = initializeDexFile(file);
    Dependencies deps = wrapper.makeSootClass(sc, className);
    addSourceFileTag(sc, "dalvik_source_" + file.getName());

    return deps;
  }

  /**
   * Initializes the dex wrapper for the given dex file
   * 
   * @param file
   *          The dex file to load
   * @return The wrapper object for the given dex file
   */
  protected soot.dexpler.DexlibWrapper initializeDexFile(File file) {
    soot.dexpler.DexlibWrapper wrapper = cache.get(file);
    if (wrapper == null) {
      wrapper = new soot.dexpler.DexlibWrapper(file);
      cache.put(file, wrapper);
      wrapper.initialize();
    }
    return wrapper;
  }

  /**
   * adds source file tag to each sootclass
   */
  protected static void addSourceFileTag(SootClass sc, String fileName) {
    soot.tagkit.SourceFileTag tag = null;
    if (sc.hasTag(SourceFileTag.NAME)) {
      return; // do not add tag if original class already has debug
      // information
    } else {
      tag = new soot.tagkit.SourceFileTag();
      sc.addTag(tag);
    }
    tag.setSourceFile(fileName);
  }
}
