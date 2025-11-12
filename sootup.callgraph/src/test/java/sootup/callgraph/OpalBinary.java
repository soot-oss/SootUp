package sootup.callgraph;

import org.objectweb.asm.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class OpalBinary {

    public static void main(String[] args) throws IOException {
        Path srcDir = Paths.get("/mnt/c/Users/morit/SootUp/sootup.callgraph/src/test/resources/callgraph/Implicit/binary/bachelor");
        Path destDir = Paths.get("/mnt/c/Users/morit/SootUp/sootup.callgraph/opal-copy");
        Path suspiciousDir = Paths.get("/mnt/c/Users/morit/SootUp/sootup.callgraph/opal-suspicious");

        copyRecursively(srcDir, destDir);
        System.out.println("Copied from " + srcDir + " to " + destDir);

        moveSuspiciousClasses(destDir, suspiciousDir);
        System.out.println("Suspicious classes moved to " + suspiciousDir);

        String convertedPath;
        try{
            convertedPath = OpalConverter.convertWithOpal("/mnt/c/Users/morit/SootUp/sootup.callgraph/src/main/resources/Implicit/OPALInvokedynamicRectifier.jar", "/mnt/c/Users/morit/SootUp/sootup.callgraph/opal-copy");
            System.out.println("Converted path: " + convertedPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Step 1: Copy directory structure recursively */
    private static void copyRecursively(Path srcDir, Path destDir) throws IOException {
        Files.walk(srcDir).forEach(source -> {
            try {
                Path dest = destDir.resolve(srcDir.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(dest);
                } else {
                    Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /** Step 2: Detect suspicious .class files and move them */
    private static void moveSuspiciousClasses(Path opalCopyDir, Path suspiciousDir) throws IOException {
        Files.createDirectories(suspiciousDir);

        // Find all class files
        var classFiles = Files.walk(opalCopyDir)
                .filter(p -> p.toString().endsWith(".class"))
                .collect(Collectors.toList());

        for (Path classFile : classFiles) {
            try (InputStream in = Files.newInputStream(classFile)) {
                ClassReader reader = new ClassReader(in);
                boolean[] isSuspicious = {false};

                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                        boolean hasArrayInSig = desc.contains("[");
                        if (!hasArrayInSig) return null;

                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override
                            public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
                                isSuspicious[0] = true;
                            }
                        };
                    }
                }, 0);

                if (isSuspicious[0]) {
                    Path dest = suspiciousDir.resolve(opalCopyDir.relativize(classFile));
                    Files.createDirectories(dest.getParent());
                    Files.copy(classFile, dest, StandardCopyOption.REPLACE_EXISTING);

                    // Remove the suspicious class from opal-copy
                    Files.deleteIfExists(classFile);
                    System.out.println("Moved suspicious file: " + classFile.getFileName());
                }
            } catch (Exception e) {
                System.err.println("Error processing " + classFile + ": " + e.getMessage());
            }
        }

        // Optional cleanup: remove empty directories left behind
        cleanEmptyDirs(opalCopyDir);
    }

    /** Step 3: Delete empty directories after moving suspicious files */
    private static void cleanEmptyDirs(Path rootDir) throws IOException {
        List<Path> dirs = Files.walk(rootDir)
                .sorted(Comparator.reverseOrder()) // delete children first
                .filter(Files::isDirectory)
                .collect(Collectors.toList());

        for (Path dir : dirs) {
            try (var entries = Files.list(dir)) {
                if (!entries.findFirst().isPresent()) {
                    Files.deleteIfExists(dir);
                }
            }
        }
    }
}
