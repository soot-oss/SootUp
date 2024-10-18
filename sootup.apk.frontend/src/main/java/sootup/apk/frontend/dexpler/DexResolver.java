package sootup.apk.frontend.dexpler;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DexResolver {
  protected Map<File, DexLibWrapper> cache = new HashMap<>();

  private static DexResolver instance;

  public static DexResolver getInstance() {
    if (instance == null) {
      instance = new DexResolver();
    }
    return instance;
  }

  public DexLibWrapper initializeDexFile(File file) {
    DexLibWrapper wrapper = cache.get(file);
    if (wrapper == null) {
      wrapper = new DexLibWrapper(file);
      cache.put(file, wrapper);
      wrapper.initialize();
    }
    return wrapper;
  }
}
