/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
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

package referencejimple;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public abstract class JimpleInstructionsTestBase {

  public View view;
  protected SootClass sootClass;
  String JimpleReferencePathPrefix =
      "src/test/java8/resources/reference-jimple/de.upb.soot.instructions.";

  protected void build() {
    Assume.assumeTrue(false);
    Assert.fail("build() has to be overriden in a subclass");
  }

  @Test
  public void compareGeneratedJimpleWithReferenceJimpleTest() throws IOException {

    // generate filename from Testfilename
    String name = this.getClass().getSimpleName();
    name = name.substring(0, name.length() - 4);

    // build directory name
    String directory = this.getClass().getName();
    int endPos = directory.lastIndexOf('.');
    int startPos = directory.lastIndexOf('.', endPos - 1) + 1;
    directory = directory.substring(startPos, endPos);

    // soot config
    Project project =
        JavaProject.builder(new JavaLanguage(8)).addClassPath(new EagerInputLocation()).build();
    view = project.createOnDemandView();

    // build class structure
    build();

    StringWriter output = new StringWriter();
    PrintWriter printWriter = new PrintWriter(output);

    // print jimple to memory
    new Printer().printTo(sootClass, printWriter);

    printWriter.flush();
    printWriter.close();

    String referencJimple =
        FileUtils.readFileToString(
            new File(JimpleReferencePathPrefix + directory + "." + name + ".jimple"), "UTF-8");
    Assert.assertEquals(referencJimple, output.toString());
  }
}
