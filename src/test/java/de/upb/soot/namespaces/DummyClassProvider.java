package de.upb.soot.namespaces;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 07.06.2018 Manuel Benz
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

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.signatures.SignatureFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Manuel Benz created on 07.06.18
 */
class DummyClassProvider implements IClassProvider {
  private final SignatureFactory signatureFactory;

  public DummyClassProvider(SignatureFactory signatureFactory) {
    this.signatureFactory = signatureFactory;
  }

  @Override
  public Optional<ClassSource> getClass(INamespace ns, Path sourcePath) {
    Path sigPath = null;
    // if it is not in target, it is located in a zip archive
    if (!sourcePath.startsWith("target")) {
      sigPath = sourcePath.getRoot().relativize(sourcePath);
    } else {
      sigPath = Paths.get("target/classes").relativize(sourcePath);
    }

    return Optional.of(new ClassSource(ns, ClassSignature.fromPath(sigPath, signatureFactory)) {
    });
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
