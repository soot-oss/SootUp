/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.test.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import qilin.core.config.PointerAnalysisConfig;
import qilin.driver.PTAOption;
import qilin.driver.PTAPattern;

/**
 * Covers the CLI translation layer itself: {@link PTAOption} parses commons-cli args and builds a
 * {@link PointerAnalysisConfig} - it must not mutate any global/singleton state, so each test
 * constructs its own {@link PTAOption} instance.
 */
public class PTAOptionCliTest {

  @Test
  public void defaultsToInsensitiveWithNoArgs() {
    PTAOption option = new PTAOption();
    option.parseCommandLine(new String[] {});

    assertEquals(".", option.getAppPath());
    assertEquals("insensitive", option.getPtaPattern().toString());
    PointerAnalysisConfig config = option.getPointerAnalysisConfig();
    assertFalse(config.isSingleEntry());
    assertFalse(config.isPreciseExceptions());
    assertEquals(
        PointerAnalysisConfig.HeapAbstractionPolicy.ALLOC_SITE, config.getHeapAbstractionPolicy());
  }

  @Test
  public void parsesObjectSensitivePattern() {
    PTAOption option = new PTAOption();
    option.parseCommandLine(new String[] {"-pta", "2o1h", "-mainclass", "some.Main"});

    assertEquals("some.Main", option.getMainClass());
    PTAPattern pattern = option.getPtaPattern();
    assertEquals(2, pattern.getContextDepth());
    assertEquals(1, pattern.getHeapContextDepth());
  }

  @Test
  public void parsesCoreAnalysisFlags() {
    PTAOption option = new PTAOption();
    option.parseCommandLine(
        new String[] {
          "-singleentry", "-mergeheap", "-preciseexceptions", "-pae", "-sc", "-lcs",
        });

    PointerAnalysisConfig config = option.getPointerAnalysisConfig();
    assertTrue(config.isSingleEntry());
    assertTrue(config.isPreciseExceptions());
    assertTrue(config.isPreciseArrayElement());
    assertTrue(config.isStringConstants());
    assertTrue(config.isEnforceEmptyCtxForIgnoreTypes());
    assertEquals(
        PointerAnalysisConfig.HeapAbstractionPolicy.HEURISTIC_MERGE,
        config.getHeapAbstractionPolicy());
  }

  @Test
  public void parsesPathsAndReflectionLog() {
    PTAOption option = new PTAOption();
    option.parseCommandLine(
        new String[] {
          "-apppath", "/tmp/app", "-libpath", "/tmp/lib", "-jre", "/tmp/jre",
          "-reflectionlog", "/tmp/reflection.log",
        });

    assertEquals("/tmp/app", option.getAppPath());
    assertEquals("/tmp/lib", option.getLibPath());
    assertEquals("/tmp/jre", option.getJrePath());
    assertEquals("/tmp/reflection.log", option.getPointerAnalysisConfig().getReflectionLogPath());
  }

  @Test
  public void independentInstancesDoNotShareState() {
    PTAOption first = new PTAOption();
    first.parseCommandLine(new String[] {"-mainclass", "first.Main"});

    PTAOption second = new PTAOption();
    second.parseCommandLine(new String[] {"-mainclass", "second.Main"});

    assertEquals("first.Main", first.getMainClass());
    assertEquals("second.Main", second.getMainClass());
  }
}
