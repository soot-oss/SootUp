package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.io.File;
import java.util.Optional;

/**
 * Allows reading a JAR file
 *
 * @author Linghui Luo
 * @author Ben Hermann
 *
 */
public class JarFileNamespace implements INamespace {
    public JarFileNamespace(File jarFile) {

    }

    @Override
    public Optional<ClassSource> getClassSource(ClassSignature signature) {
        return Optional.empty();
    }
}
