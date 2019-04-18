package de.upb.soot.namespaces;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
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

import de.upb.soot.frontends.ClassSource;
import de.upb.soot.signatures.JavaClassSignature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

import static org.junit.Assert.assertEquals;

/** @author Markus Schmidt */
@Category(Java8Test.class)
public class FileTypeTest {

  private void testType( FileType ft, String path) throws IOException {
    File file = new File( "target/"+path );
    assertEquals( EnumSet.of(ft) , FileType.getFileType( file ) );
  }

  @Test
  public void testJar() throws IOException{
    testType( FileType.JAR, "test-classes/de/upb/soot/namespaces/Soot-4.0-SNAPSHOT.jar");
  }

  @Ignore
  public void testZip() throws IOException{
    testType( FileType.ZIP,"");
  }

  @Ignore
  public void testApk() throws IOException {
    testType( FileType.APK, "");
  }

  @Test
  public void testClass() throws IOException {
    testType( FileType.CLASS, "classes/de/upb/soot/Scope.class");
  }

  @Test
  public void testJimple() throws IOException {
    testType( FileType.JIMPLE, "test-classes/jimple-target/BinaryOperations.jimple");
  }

  @Test
  public void testJava() throws IOException {
    testType( FileType.JAVA, "test-classes/java-target/BinaryOperations.java");
  }
}
