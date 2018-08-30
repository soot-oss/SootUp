package de.upb.soot.namespaces;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.MagicNumberFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * An enumeration of common file types used for class loading/writing and other purposes.
 *
 * @author Manuel Benz created on 07.06.18
 */
public enum FileType {

    JAR("jar", new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}), ZIP("zip", new byte[]{(byte) 0x50, (byte) 0x4B, (byte) 0x03, (byte) 0x04}),
    APK("apk", new byte[]{}), CLASS("class", new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}), JAVA("java", new byte[]{(byte) 0x70, (byte) 0x61, (byte) 0x63, (byte) 0x6B}),
    JIMPLE("jimple", new byte[]{(byte) 0x70, (byte) 0x75, (byte) 0x62, (byte) 0x6C});

    public static final EnumSet<FileType> ARCHIVE_TYPES = EnumSet.of(JAR, ZIP, APK);

    public static boolean isFileType(Path path, FileType fileType) {
        // converts path to a file object
        String pth = path.toString().toLowerCase();
        File file = new File(pth);
        MagicNumberFileFilter fileFilter = new MagicNumberFileFilter(fileType.magic_bytes);

        if (isArchive(path, fileType) && hasClassesDex(file)) {
            System.out.println("TRUE");
            return fileType == APK;
        }

        else if (isArchive(path, fileType)) {
            if (fileFilter.accept(file)) {
                System.out.println("File name utils: " + FilenameUtils.getExtension(pth) + " File type extension " + fileType.extension);
                return fileType.extension.equals(FilenameUtils.getExtension(pth));
            }
        }

        // If file extension is .jimple
        else if(FilenameUtils.getExtension(pth).equals("jimple")) {
            return fileType.extension.equals(FilenameUtils.getExtension(pth));
        }

        // If magic byte of file matches
        else if (fileFilter.accept(file)) {
            return fileType.extension.equals(FilenameUtils.getExtension(pth));
        }

        return false;
    }

    //TODO method that gives exact type of file?

    // Checks if the file is an archive
    private static boolean isArchive(Path path, FileType fileType) {
        MagicNumberFileFilter fileFilter = new MagicNumberFileFilter(fileType.magic_bytes);
        String str_path = path.toString().toLowerCase();
        String fileExt = FilenameUtils.getExtension(str_path);
        // converts path to a file object
        File file = new File(str_path);
        if (fileFilter.accept(file)) {
            System.out.println("FiletoString" + fileFilter.hashCode());
        }
        for (FileType f : ARCHIVE_TYPES) {
            if (f == fileType && fileExt.equals(fileType.extension)) {
                return true;
            }
        }
        return false;
    }

    // Checks if the archive contains classes.dex files
    private static boolean hasClassesDex(File file) {
        ZipFile zf = null;
        try {
            zf = new ZipFile(file);
            Enumeration<?> en = zf.entries();
            while (en.hasMoreElements()) {
                ZipEntry z = (ZipEntry) en.nextElement();
                String name = z.getName();
                if (name.equals("classes.dex")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zf != null) {
                try {
                    zf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isFileType(Paths.get("E:\\Job\\FileType\\gradle-4.7-all.zip"), ZIP));
    }

    private String extension;
    private byte magic_bytes[];

    FileType(String fileExtension, byte magic_bytes[]) {
        this.magic_bytes = magic_bytes;
        this.extension = fileExtension;
    }

    public String getExtension() {
        return extension;
    }
}
