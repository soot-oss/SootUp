package de.upb.soot.ns;

import java.util.EnumSet;

/**
 * @author Manuel Benz created on 07.06.18
 */
public enum FileType {
  JAR("jar"), ZIP("zip"), APK("apk"), CLASS("class"), JAVA("java"), JIMPLE("jimple");

  public static EnumSet<FileType> ARCHIVE_TYPES = EnumSet.of(JAR, ZIP, APK);

  private final String extension;

  FileType(String fileExtension) {
    this.extension = fileExtension;
  }

  public String getExtension() {
    return extension;
  }
}
