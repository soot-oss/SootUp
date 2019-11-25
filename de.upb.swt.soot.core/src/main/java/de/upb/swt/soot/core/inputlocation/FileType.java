package de.upb.swt.soot.core.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 07.06.2018 Manuel Benz
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

import java.util.EnumSet;
import javax.annotation.Nonnull;

/**
 * An enumeration of common file types used for class loading/writing and other purposes.
 *
 * @author Manuel Benz
 */
public enum FileType {
  JAR("jar"),
  ZIP("zip"),
  APK("apk"),
  CLASS("class"),
  JAVA("java"),
  JIMPLE("jimple");

  public static final @Nonnull EnumSet<FileType> ARCHIVE_TYPES = EnumSet.of(JAR, ZIP, APK);

  private final @Nonnull String extension;

  FileType(@Nonnull String fileExtension) {
    this.extension = fileExtension;
  }

  public @Nonnull String getExtension() {
    return extension;
  }
}
