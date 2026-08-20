package sootup.apk.backend;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.writer.io.FileDataStore;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.apk.frontend.Util.DexUtil;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.views.View;

public class DexOutputLocation {

  private static final Logger log = LoggerFactory.getLogger(DexOutputLocation.class);

  private final View view;
  private final File apkFile;
  private Opcodes opcodes;
  private DexPool currentDexPool;
  private final List<DexPool> dexPoolList = new LinkedList<>();

  public DexOutputLocation(View view, File apkFile) {
    this.view = view;
    this.apkFile = apkFile;
  }

  public void writeDex(String outputDir) {
    AndroidVersionInfo androidVersionInfo = DexUtil.getAndroidVersionInfo();
    int minSdkVersion = androidVersionInfo.minSdkVersion;

    opcodes = Opcodes.forApi(minSdkVersion);
    DexClassBuilder dexClassBuilder = new DexClassBuilder(this);
    currentDexPool = new DexPool(opcodes);
    dexPoolList.add(currentDexPool);

    var classes = view.getClasses().toList();
    classes.forEach(
        c -> {
          if (c.getName().startsWith("com.example.exampleapp.MainActivity")) {
            dexClassBuilder.createClass(c);
          }
        });
    try {
      writeDexFiles(outputDir);
    } catch (IOException e) {
      throw new RuntimeException("An exception occurred during creation of dex files");
    }
  }

  public void writeApk(String outputDir) {
    AndroidVersionInfo androidVersionInfo = DexUtil.getAndroidVersionInfo();
    int minSdkVersion = androidVersionInfo.minSdkVersion;

    opcodes = Opcodes.forApi(minSdkVersion);
    DexClassBuilder dexClassBuilder = new DexClassBuilder(this);
    currentDexPool = new DexPool(opcodes);
    dexPoolList.add(currentDexPool);

    view.getClasses()
        .forEach(
            c -> {
              if (c.getName().startsWith("com.example.exampleapp.MainActivity")) {
                dexClassBuilder.createClass(c);
              }
            });

    String newApkName = apkFile == null ? "out.apk" : apkFile.getName();
    Path outputFile = Paths.get(outputDir, newApkName);

    int i = 1;
    while (Files.exists(outputFile, LinkOption.NOFOLLOW_LINKS) && i < 100) {
      newApkName =
          apkFile == null
              ? "out"
              : apkFile.getName().substring(0, apkFile.getName().indexOf(".")) + i + ".apk";
      outputFile = Paths.get(outputDir, newApkName);
      i++;
    }
    if (Files.exists(outputFile, LinkOption.NOFOLLOW_LINKS)) {
      throw new IllegalStateException("Output file is already existent");
    }

    try {
      try (ZipOutputStream zipOutputStream =
          new ZipOutputStream(Files.newOutputStream(outputFile, StandardOpenOption.CREATE_NEW))) {

        if (apkFile != null) {
          copyExistingFiles(zipOutputStream);
        }

        addExistingClasses();
        writeDexFiles(zipOutputStream);
        log.info(
            "APK successfully created. The .apk still needs to be aligned with zipalign and signed with jarsigner");
      }
    } catch (IOException e) {
      throw new RuntimeException("An exception occurred during creation of .apk file");
    }
  }

  // adds all classes to classes.dex except from com.example.exampleapp.MainActivity
  private void addExistingClasses() throws IOException {

    try (ZipFile apk = new ZipFile(apkFile)) {

      Enumeration<? extends ZipEntry> entries = apk.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();

        if (entry.getName().matches("classes(\\d*)\\.dex")) {

          File dexTemp = File.createTempFile("classes", ".dex");

          try (InputStream in = apk.getInputStream(entry);
              OutputStream out = new FileOutputStream(dexTemp)) {

            in.transferTo(out);
          }

          DexFile dexFile = DexFileFactory.loadDexFile(dexTemp, opcodes);

          for (ClassDef classDef : dexFile.getClasses()) {
            String dexClassName = classDef.getType();
            if (!dexClassName.startsWith(("Lcom/example/exampleapp/MainActivity"))) {
              addClass(classDef);
            }
          }

          dexTemp.delete();
        }
      }
    }
  }

  // copy all existing files of the previous apk to the new apk except the classes.dex and metadata
  private void copyExistingFiles(ZipOutputStream zipOutputStream) throws IOException {
    try (ZipFile sourceApk = new ZipFile(apkFile)) {

      Enumeration<? extends ZipEntry> entries = sourceApk.entries();

      while (entries.hasMoreElements()) {
        ZipEntry sourceEntry = entries.nextElement();
        String name = sourceEntry.getName();

        if (name.matches("classes(\\d+)?\\.dex")) {
          continue;
        }

        if (name.startsWith("META-INF/")) {
          continue;
        }

        ZipEntry targetEntry = new ZipEntry(name);

        byte[] data;

        try (InputStream in = sourceApk.getInputStream(sourceEntry)) {
          data = in.readAllBytes();
        }

        if ("resources.arsc".equals(name)) {
          CRC32 crc = new CRC32();
          crc.update(data);

          targetEntry.setMethod(ZipEntry.STORED);
          targetEntry.setSize(data.length);
          targetEntry.setCompressedSize(data.length);
          targetEntry.setCrc(crc.getValue());
        }

        zipOutputStream.putNextEntry(targetEntry);
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
      }
    }
  }

  private void writeDexFiles(ZipOutputStream zipOutputStream) throws IOException {

    for (int i = 0; i < dexPoolList.size(); i++) {
      MemoryDataStore dataStore = new MemoryDataStore();
      dexPoolList.get(i).writeTo(dataStore);

      String dexName = (i == 0) ? "classes.dex" : "classes" + (i + 1) + ".dex";
      ZipEntry entry = new ZipEntry(dexName);
      zipOutputStream.putNextEntry(entry);

      zipOutputStream.write(dataStore.getData());
      zipOutputStream.closeEntry();
    }
  }

  private void writeDexFiles(String folder) throws IOException {
    for (int i = 0; i < dexPoolList.size(); i++) {
      DexPool dexPool = dexPoolList.get(i);
      File file = new File(folder, (i == 0) ? "classes.dex" : "classes" + (i + 1) + ".dex");
      FileDataStore fds = new FileDataStore(file);
      dexPool.writeTo(fds);
      fds.close();
    }
  }

  protected void addClass(final ClassDef classDef) {
    currentDexPool.mark();
    currentDexPool.internClass(classDef);
    log.info("Class {} added to dexPool", classDef.getType());
    if (currentDexPool.hasOverflowed()) {
      if (!opcodes.isArt()) {
        log.warn(
            "Multiple classes.dex created. Multidex is only supported after Android 5.0 (Api level 21) "
                + "and will therefore not run on older devices");
      }
      currentDexPool.reset();
      currentDexPool = new DexPool(opcodes);
      dexPoolList.add(currentDexPool);
      currentDexPool.internClass(classDef);
      if (currentDexPool.hasOverflowed()) {
        throw new RuntimeException(
            "Class " + classDef.getType() + " has too many methods (> 65536)");
      }
    }
  }

  protected View getView() {
    return view;
  }
}
