package de.upb.soot.signatures;

import java.nio.file.Path;
import java.nio.file.Paths;

/** @author Manuel Benz created on 06.06.18 */
public class SignatureUtil {
  public static Path toPath(ClassSignature signature) {
    return Paths.get(signature.getFullyQualifiedName().replace('.', '/'));
  }

  public static ClassSignature fromPath(Path path, SignatureFactory fac) {
    return fac.getClassSignature(path.toString().replace('/', '.'));
  }
}
