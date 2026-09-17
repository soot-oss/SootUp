package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableDexFile;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.writer.pool.DexPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sootup.apk.frontend.dexpler.DexFileProvider;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.model.SootMethod;
import sootup.java.core.views.JavaView;

public class DexFileProviderTest {

  @TempDir static Path tempDir;

  /** A duplicated class is taken from the dex file Android's class loader searches first. */
  @Test
  public void duplicateClassComesFromClassesDex() throws IOException {
    Map<String, String> dexToMethod = new LinkedHashMap<>();
    dexToMethod.put("classes10.dex", "fromClasses10");
    dexToMethod.put("classes.dex", "fromClasses");
    dexToMethod.put("classes2.dex", "fromClasses2");
    dexToMethod.put("assets/payload.dex", "fromPayload");
    assertEquals(Set.of("fromClasses"), methodsOfDuplicate(apkWith("WithClassesDex", dexToMethod)));
  }

  @Test
  public void duplicateClassComesFromLowestSecondaryDex() throws IOException {
    Map<String, String> dexToMethod = new LinkedHashMap<>();
    dexToMethod.put("classes10.dex", "fromClasses10");
    dexToMethod.put("payload.dex", "fromPayload");
    dexToMethod.put("classes9.dex", "fromClasses9");
    assertEquals(
        Set.of("fromClasses9"), methodsOfDuplicate(apkWith("WithoutClassesDex", dexToMethod)));
  }

  /** The opcode set follows the dex header, not the newest android.jar that happens to exist. */
  @Test
  public void opcodesFollowTheDexVersion() throws IOException {
    Path dex = tempDir.resolve("Api28.dex");
    DexPool.writeTo(
        dex.toString(), new ImmutableDexFile(Opcodes.forApi(28), List.of(dupClass("run"))));

    DexBackedDexFile loaded =
        (DexBackedDexFile)
            DexFileProvider.getInstance()
                .getDexFromSource(dex.toFile(), 19)
                .get(0)
                .getBase()
                .getDexFile();
    assertEquals(28, loaded.getOpcodes().api);
  }

  private static Set<String> methodsOfDuplicate(Path apk) {
    JavaView view =
        new JavaView(
            List.of(
                new ApkAnalysisInputLocation(
                    apk,
                    new AndroidVersionInfo(apk, ""),
                    DexBodyInterceptors.Default.bodyInterceptors())));
    return view
        .getClass(view.getIdentifierFactory().getClassType("dex.Dup"))
        .get()
        .getMethods()
        .stream()
        .map(SootMethod::getName)
        .collect(Collectors.toSet());
  }

  private static Path apkWith(String name, Map<String, String> dexToMethod) throws IOException {
    Path apk = tempDir.resolve(name + ".apk");
    try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(apk))) {
      for (Map.Entry<String, String> entry : dexToMethod.entrySet()) {
        Path dex = Files.createTempFile(tempDir, "dex", ".dex");
        DexPool.writeTo(
            dex.toString(),
            new ImmutableDexFile(Opcodes.forApi(15), List.of(dupClass(entry.getValue()))));
        zip.putNextEntry(new ZipEntry(entry.getKey()));
        Files.copy(dex, zip);
        zip.closeEntry();
      }
    }
    return apk;
  }

  private static ImmutableClassDef dupClass(String methodName) {
    ImmutableMethod method =
        new ImmutableMethod(
            "Ldex/Dup;",
            methodName,
            null,
            "V",
            AccessFlags.PUBLIC.getValue()
                | AccessFlags.STATIC.getValue()
                | AccessFlags.NATIVE.getValue(),
            null,
            null,
            null);
    return new ImmutableClassDef(
        "Ldex/Dup;",
        AccessFlags.PUBLIC.getValue(),
        "Ljava/lang/Object;",
        null,
        null,
        null,
        null,
        null,
        List.of(method),
        null);
  }
}
