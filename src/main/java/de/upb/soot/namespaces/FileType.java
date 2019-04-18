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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import javax.annotation.Nonnull;

/**
 * An enumeration of common file types used for class loading/writing and other purposes.
 *
 * @author Manuel Benz
 * @author Markus Schmidt
 *
 */
public enum FileType {
  JAR("jar"),
  ZIP("zip"),
  APK("apk"),         // 50 4B 03 04    for jar, zip, apk
  CLASS("class"),     // CA FE BA BE
  JAVA("java"),       // none
  JIMPLE("jimple");   // none

  public static final @Nonnull EnumSet<FileType> ARCHIVE_TYPES = EnumSet.of(JAR, ZIP, APK);

  private final @Nonnull String extension;

  FileType(@Nonnull String fileExtension) {
    this.extension = fileExtension;
  }

  public @Nonnull String getExtension() {
    return extension;
  }

  // TODO: test
  //  TODO: [ms] is archive type enough info if not determinable? if its not sufficient refactor to FileType
  public static EnumSet<FileType> getFileType(File file ) throws IOException {

    EnumSet<FileType> foundType = EnumSet.noneOf(FileType.class);
    BufferedReader buffer = new BufferedReader(new FileReader(file));
    char [] fileHead = new char[4];
    // use magic byte where possible
    if( buffer.read(fileHead, 0, 4) == 4 ){

      if(Arrays.equals(fileHead, new char[]{ 0xCA, 0xFE, 0xBA, 0xBE }) ) {
        foundType = EnumSet.of(FileType.CLASS);
      }else if( Arrays.equals(fileHead, new char[]{ 0x50, 0x4B, 0x03, 0x04}) ){
        FileType fileType = getArchiveTypeByExtension( file.getName() );
        foundType = (fileType == null)? FileType.ARCHIVE_TYPES : EnumSet.of(fileType);
      }

    }

    // otherwise use filename to determine type
    if( foundType.isEmpty()){
      FileType type = getTypeByExtension(file.getName());
      if( type != null ) {
        return EnumSet.of( type );
      }
    }

    return foundType;
  }

  private static FileType getTypeByExtension( String filename ){
    if( filename.endsWith(".class") ){
      return FileType.CLASS;
    }else if( filename.endsWith(".jimple") ){
      return FileType.JIMPLE;
    }else if( filename.endsWith(".java") ){
      return FileType.JAVA;
    }

    return getArchiveTypeByExtension( filename );
  }

  private static FileType getArchiveTypeByExtension( String filename ) {
    if( filename.endsWith(".jar") ){
      return FileType.JAR;
    }else if( filename.endsWith(".zip") ){
      return FileType.ZIP;
    }else if( filename.endsWith(".apk") ){
      return FileType.APK;
    }
    return null;
  }
}
