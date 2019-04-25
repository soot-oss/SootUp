package de.upb.soot.namespaces;

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


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import javax.annotation.Nonnull;

/**
 * An enumeration of common file types used for class loading/writing and other purposes.
 *
 * @author Manuel Benz
 * @author Markus Schmidt
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

  public static FileType getFileType(File file) throws IOException {

    FileType foundType = null;
    // use magic number where possible
    FileInputStream fis = new FileInputStream(file);
    DataInputStream dis = new DataInputStream(fis);
    int[] magicNumber = new int[4];
    for (int i = 0; dis.available() > 0 && i < 4; i++) {
      magicNumber[i] = dis.readUnsignedByte();
    }
    dis.close();
    fis.close();

    // TODO: [ms] shall we support deprecated pack2000 format? (-> CAFED00D)
    if (Arrays.equals(magicNumber, new int[] {0xCA, 0xFE, 0xBA, 0xBE})) {
      foundType = FileType.CLASS;
    } else if (Arrays.equals(magicNumber, new int[] {0x50, 0x4B, 0x03, 0x04})) {
      foundType = getArchiveTypeByExtension(file.getName());
    }

    // otherwise use filename to determine plaintext type
    if (foundType == null) {
      foundType = getPlainTypeByExtension(file.getName());
    }

    return foundType;
  }

  public static FileType getTypeByExtension(String filename) {
    FileType type;
    if (filename.endsWith(".class")) {
      type = FileType.CLASS;
    } else {
      type = getPlainTypeByExtension(filename);
    }
    if (type == null) {
      type = getArchiveTypeByExtension(filename);
    }
    return type;
  }

  private static FileType getPlainTypeByExtension(String filename) {
    if (filename.endsWith(".jimple")) {
      return FileType.JIMPLE;
    } else if (filename.endsWith(".java")) {
      return FileType.JAVA;
    }
    return null;
  }

  private static FileType getArchiveTypeByExtension(String filename) {
    if (filename.endsWith(".jar")) {
      return FileType.JAR;
    } else if (filename.endsWith(".zip")) {
      return FileType.ZIP;
    } else if (filename.endsWith(".apk")) {
      return FileType.APK;
    }
    return null;
  }
}
