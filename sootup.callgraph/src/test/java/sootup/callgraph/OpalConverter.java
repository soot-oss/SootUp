package sootup.callgraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class OpalConverter {
    /**
     * Uses the OPAL jar to convert all .class files under the given classPath
     * into a new directory (not overwriting originals). Returns the output directory as String. *
     * @param opalJarPath path to the OPAL jar file
     * @param classPath path to the directory (or jar) containing .class files
     * @return String path to the newly created directory containing converted classes
     * @throws IOException if I/O fails
     * @throws InterruptedException if process execution is interrupted
     */
    public static String convertWithOpal(String opalJarPath, String classPath) throws IOException, InterruptedException {
        // 1) Prepare output dir
        Path input = Paths.get(classPath).toAbsolutePath();
        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Input classPath does not exist: " + classPath);
        }
        // Use a unique output directory name to avoid overwriting
        Path parent = input.getParent() != null ? input.getParent() : input;
        String outDirName = "opal-out-" + UUID.randomUUID();
        Path outDir = parent.resolve(outDirName);
        Files.createDirectories(outDir);
        // 2) Run OPAL jar
        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar",
                opalJarPath,
                "--outputDir",
                outDir.toString(),
                "--cp",
                classPath
        );
        pb.inheritIO(); // optional: show OPAL output
        Process p = pb.start();
        int exit = p.waitFor();
        if (exit != 0) {
            throw new RuntimeException("OPAL conversion failed with exit code " + exit);
        }
        // 3) The OPAL result likely is under outDir + "/classes/project.zip" (depending on tool version)
        Path projectZip = outDir.resolve("classes").resolve("project.zip");
        Path finalDir = outDir.resolve("classes").resolve("project");
        if (Files.exists(projectZip)) {
            // unzip project.zip into a new path
            Files.createDirectories(finalDir);
            OpalConverter opalConverter = new OpalConverter();
            try (FileSystem zipFs = FileSystems.newFileSystem(projectZip, opalConverter.getClass().getClassLoader())) {
                for (Path root : zipFs.getRootDirectories()) {
                    Files.walk(root).forEach(source -> {
                        try {
                            Path rel = root.relativize(source);
                            Path dest = finalDir.resolve(rel.toString());
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(dest);
                            } else {
                                Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Unzip failed", e);
                        }
                    });
                }
            }
            return finalDir.toString();
        }
        // if no project.zip, possibly OPAL wrote classes directly
        Path classesDir = outDir.resolve("classes");
        if (Files.exists(classesDir)) {
            return classesDir.toString();
        }
        // fallback: return outDir
        return outDir.toString();
    }
}