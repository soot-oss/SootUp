package sootup.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Markus Schmidt and others
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

import com.googlecode.dex2jar.tools.Dex2jarCmd;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.model.SourceType;

public class ApkAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

  public ApkAnalysisInputLocation(@Nonnull Path path, @Nullable SourceType srcType) {
    super(path, srcType);
    String jarPath = dex2jar(path);
    this.path = Paths.get(jarPath);
  }

  private String dex2jar(Path path) {
    String apkPath = path.toAbsolutePath().toString();
    String outDir = "./tmp/";
    int start = apkPath.lastIndexOf(File.separator);
    int end = apkPath.lastIndexOf(".apk");
    String outputFile = outDir + apkPath.substring(start + 1, end) + ".jar";
    Dex2jarCmd.main("-f", apkPath, "-o", outputFile);
    return outputFile;
  }
}
