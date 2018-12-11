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

import java.util.Collection;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.internal.matchers.GreaterOrEqual;
import org.mockito.internal.matchers.LessOrEqual;

/**
 * @author Manuel Benz created on 07.06.18
 */
public abstract class AbstractNamespaceTest {

  protected static final int CLASSES_IN_JAR = 25;
  private SignatureFactory signatureFactory;
  private IClassProvider classProvider;

  @Before
  public void setUp() {
    signatureFactory = createSignatureFactory();
    classProvider = createClassProvider();
  }

  protected SignatureFactory getSignatureFactory() {
    return signatureFactory;
  }

  protected IClassProvider getClassProvider() {
    return classProvider;
  }

  protected SignatureFactory createSignatureFactory() {
    return new SignatureFactory() {
    };
  }

  protected IClassProvider createClassProvider() {
    return new DummyClassProvider(getSignatureFactory());
  }

  protected void testClassReceival(AbstractNamespace ns, ClassSignature sig, int minClassesFound) {
    testClassReceival(ns, sig, minClassesFound, -1);
  }

  protected void testClassReceival(AbstractNamespace ns, ClassSignature sig, int minClassesFound, int maxClassesFound) {
    final Optional<ClassSource> clazz = ns.getClassSource(sig);

    Assert.assertTrue(clazz.isPresent());
    Assert.assertEquals(sig, clazz.get().getClassSignature());

    final Collection<ClassSource> classSources = ns.getClassSources();
    Assert.assertNotNull(classSources);
    Assert.assertFalse(classSources.isEmpty());
    Assert.assertThat(classSources.size(), new GreaterOrEqual<>(minClassesFound));
    if (maxClassesFound != -1) {
      Assert.assertThat(classSources.size(), new LessOrEqual<>(maxClassesFound));
    }
  }
}
