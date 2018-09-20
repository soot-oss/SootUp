package de.upb.soot.namespaces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class FileTypeTest {

  @Test
  public void testFileTypeJar() throws URISyntaxException {
    URL url = getClass().getResource("demo.jar");
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
  public void testFileTypeApkAsJar() throws URISyntaxException {
    URL url = getClass().getResource("demo.apk");
    Path path = Paths.get(url.toURI());
    assertFalse(FileType.isFileType(path, FileType.JAR));
  }

  @Test
  public void testFileTypeApkWithoutDex() throws URISyntaxException {
    URL url = getClass().getResource("NoClassesDex.apk");
    Path path = Paths.get(url.toURI());
    assertFalse(FileType.isFileType(path, FileType.APK));
  }

  @Test
  public void testFileTypeZip() throws URISyntaxException {
    URL url = getClass().getResource("demo.zip");
    Path path = Paths.get(url.toURI());
    assertTrue(FileType.isFileType(path, FileType.ZIP));
  }

  @Test
  public void testFileTypeJimple() throws URISyntaxException {
    URL url = getClass().getResource("PrimitiveLocals.jimple");
    Path path = Paths.get(url.toURI());
    assertTrue(FileType.isFileType(path, FileType.JIMPLE));
  }

  @Test
  public void testFileTypeJimpleAsJava() throws URISyntaxException {
    URL url = getClass().getResource("PrimitiveLocals.jimple");
    Path path = Paths.get(url.toURI());
    assertFalse(FileType.isFileType(path, FileType.JAVA));
  }
}