package de.upb.soot.namespaces;

import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class FileTypeTest {

    @Test
    public void testFileTypeJar() throws URISyntaxException {
        URL url = getClass().getResource("Soot-4.0-SNAPSHOT.jar");
        Path path = Paths.get(url.toURI());
        assertTrue(FileType.isFileType(path, FileType.JAR));
    }

    @Test
    public void testFileTypeJava() throws URISyntaxException {
        URL url = getClass().getResource("PrimitiveLocals.java");
        Path path = Paths.get(url.toURI());
        assertTrue(FileType.isFileType(path, FileType.JAVA));
    }

    @Test
    public void testFileTypeClass() throws URISyntaxException {
        URL url = getClass().getResource("PrimitiveLocals.class");
        Path path = Paths.get(url.toURI());
        assertTrue(FileType.isFileType(path, FileType.CLASS));
    }

    @Test
    public void testFileTypeApk() throws URISyntaxException {
        URL url = getClass().getResource("demo.apk");
        Path path = Paths.get(url.toURI());
        assertTrue(FileType.isFileType(path, FileType.APK));
    }

    @Test
    public void testFileTypeZip() throws URISyntaxException {
        URL url = getClass().getResource("google-java-format.zip");
        Path path = Paths.get(url.toURI());
        assertTrue(FileType.isFileType(path, FileType.ZIP));
    }

    @Test
    public void testFileTypeJimple() throws URISyntaxException {
        URL url = getClass().getResource("PrimitiveLocals.jimple");
        Path path = Paths.get(url.toURI());
        assertTrue(FileType.isFileType(path, FileType.JIMPLE));
    }
}