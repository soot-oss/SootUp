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

import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.frontend.PathbasedClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.types.ClassType;
import sootup.core.views.View;

public class DexClassProvider implements PathbasedClassProvider {
  @NonNull private final View view;

  public DexClassProvider(@NonNull View view) {
    this.view = view;
  }

  @Override
  public Optional<SootClassSource> createClassSource(
      @NonNull AnalysisInputLocation inputLocation,
      @NonNull Path sourcePath,
      @NonNull ClassType classSignature) {
    DexClassSource dexClassSource =
        new DexClassSource(view, inputLocation, classSignature, sourcePath);
    if (dexClassSource.classInformation != null) {
      return Optional.of(dexClassSource);
    }
    return Optional.empty();
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.DEX;
  }
}
