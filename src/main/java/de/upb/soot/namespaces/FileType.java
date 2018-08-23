package de.upb.soot.namespaces;

import java.io.File;
import java.util.EnumSet;

import org.apache.commons.io.filefilter.MagicNumberFileFilter;

/**
 * An enumeration of common file types used for class loading/writing and other purposes.
 *
 * @author Manuel Benz created on 07.06.18
 */
public enum FileType {
  JAR("jar"), ZIP("zip"), APK("apk"), CLASS("class"), JAVA("java"), JIMPLE("jimple"), UNDEFINED("undefined");

  public static final EnumSet<FileType> ARCHIVE_TYPES = EnumSet.of(JAR, ZIP, APK);

  public static FileType fileTypeCheck(File file) {

    // Check if .class file
    MagicNumberFileFilter fileFilter
        = new MagicNumberFileFilter(new byte[] { (byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE });
    if (fileFilter.accept(file)) {
      return CLASS;
    }

    // Check if .zip file
    fileFilter = new MagicNumberFileFilter(new byte[] { (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04 });
    if (fileFilter.accept(file)) {
      return ZIP;
    }

    // Check if .apk file
    fileFilter = new MagicNumberFileFilter(new byte[] { (byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04 });
    if (fileFilter.accept(file)) {
      return APK;
    }

    // Check if .java file
    fileFilter = new MagicNumberFileFilter(new byte[] { (byte) 0x70, (byte) 0x61, (byte) 0x63, (byte) 0x6B });
    if (fileFilter.accept(file)) {
      return JAVA;
    }

    // Check if .jimple file
    // TODO: Jimple files have different magic bytes
    fileFilter = new MagicNumberFileFilter(new byte[] { (byte) 0x63, (byte) 0x6C, (byte) 0x61, (byte) 0x73 });
    if (fileFilter.accept(file)) {
      return JIMPLE;
    }

    // TODO: JAR files also showing different magic bytes
    return UNDEFINED;
  }

  private final String extension;

  FileType(String fileExtension) {
    this.extension = fileExtension;
  }

  public String getExtension() {
    return extension;
  }
}
