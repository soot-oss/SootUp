package sootup.apk.frontend.resources;

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

import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pxb.android.ResConst;
import pxb.android.StringItems;

/**
 * Parses an APK's compiled resource table ({@code resources.arsc}) far enough to resolve resource
 * IDs of a given type (e.g. {@code layout}) back to the zip entry name they point at — the piece
 * {@code AndroidLayoutParser}'s own class doc originally flagged as missing, needed to map a layout
 * XML file to the specific Activity that inflates it instead of checking every extracted {@code
 * android:onClick} name against every manifest-declared activity.
 *
 * <h2>Why this isn't just a call to {@code de.upb.cs.swt:axml}'s own {@code
 * pxb.android.arsc.ArscParser}</h2>
 *
 * This module already depends on that library for {@code AndroidManifest.xml} and layout XML
 * parsing (both use its {@code AxmlReader}), and its {@code arsc} package looks like the obvious
 * reuse — but its {@code ArscParser#readPackage} assumes the four package-header offset fields
 * ({@code typeStrings}/{@code lastPublicType}/{@code keyStrings}/{@code lastPublicKey}) are
 * immediately followed by the type-name string pool, with no gap. That was true for the
 * "gingerbread"-era format the library's own class doc references, but newer {@code aapt2} output
 * (essentially all APKs built with a current Android Gradle Plugin) inserts an additional {@code
 * typeIdOffset} field into {@code ResTable_package} before the string pools, which this library's
 * parser doesn't know about — confirmed directly against this module's own checked-in {@code
 * Crypto.apk} test resource (a real, if small, aapt2-built APK): {@code ArscParser} throws a bare
 * {@code RuntimeException} on it (a 4-byte position mismatch between where it expects the type-name
 * string pool and where the package header's own {@code typeStrings} offset field says it actually
 * is).
 *
 * <p>Rather than fork/vendor a fix into third-party code, this reads the same self-describing chunk
 * structure ({@code ResChunk_header}: type/headerSize/size) but always locates the type-name and
 * key-name string pools via the package header's own {@code typeStrings}/{@code keyStrings} byte
 * offsets (relative to the package chunk's own start) instead of assuming adjacency — robust to any
 * such extra header field, present or not, without needing to special-case a header-size check.
 * Every other chunk boundary in this format is self-describing via its own {@code size} field, so
 * this reader resyncs to the next sibling chunk after each one regardless of how much of a chunk's
 * body it actually understood, the same resilience property the fixed library code already had
 * everywhere except that one offset assumption.
 *
 * <p>Deliberately narrow, not a general-purpose resource table model: only simple ({@code
 * TYPE_STRING}-valued) entries of one caller-chosen type name are extracted, since that's all a
 * file-path lookup needs. Complex ("bag") entries, resource aliases ({@code TYPE_REFERENCE}), and
 * every other value type are skipped. A type's per-config entry chunk flagged {@code FLAG_SPARSE} or
 * {@code FLAG_OFFSET16} (a more compact entry-offset encoding some {@code aapt2} builds use for
 * large/sparse resource ID spaces) is skipped too, best-effort, rather than mis-parsed — logged at
 * debug level, the same "one bad chunk shouldn't sink the whole scan" posture {@link
 * sootup.apk.frontend.layout.AndroidLayoutParser} already takes for unparsable layout entries.
 * Multiple configuration variants of the same resource ID (e.g. {@code layout/x.xml} vs {@code
 * layout-land/x.xml}) are deliberately all returned rather than picking one — which variant a device
 * actually inflates at runtime isn't statically known, so returning the union keeps this sound.
 */
public final class AndroidResourceTableParser {

  private static final Logger logger = LoggerFactory.getLogger(AndroidResourceTableParser.class);

  /** {@code Res_value::dataType} for a string/file-reference value. */
  private static final int TYPE_STRING = 0x03;

  private static final int NO_ENTRY = 0xFFFFFFFF;
  private static final int ENTRY_FLAG_COMPLEX = 0x0001;
  private static final int TYPE_FLAG_SPARSE = 0x01;
  private static final int TYPE_FLAG_OFFSET16 = 0x02;

  private AndroidResourceTableParser() {}

  /**
   * Extracts {@code resources.arsc} from {@code apkPath} and resolves every resource ID of type
   * {@code typeName} (e.g. {@code "layout"}) to the zip entry name(s) it points at. Returns an empty
   * map, logging at debug level, if the APK has no {@code resources.arsc} entry or it can't be
   * parsed — the caller's fallback is to treat every candidate as unresolved, the same posture
   * {@link sootup.apk.frontend.layout.AndroidLayoutParser} already takes.
   */
  @NonNull
  public static Map<Integer, Set<String>> parseFileNamesByResourceIdFromApk(
      @NonNull Path apkPath, @NonNull String typeName) {
    try (ZipFile archive = new ZipFile(apkPath.toFile())) {
      ZipEntry arscEntry = archive.getEntry("resources.arsc");
      if (arscEntry == null) {
        return new LinkedHashMap<>();
      }
      try (InputStream in = archive.getInputStream(arscEntry)) {
        return parseFileNamesByResourceId(ByteStreams.toByteArray(in), typeName);
      }
    } catch (IOException | RuntimeException e) {
      logger.debug("Could not read resources.arsc from {}: {}", apkPath, e.toString());
      return new LinkedHashMap<>();
    }
  }

  /** Same as {@link #parseFileNamesByResourceIdFromApk}, given already-extracted arsc bytes. */
  @NonNull
  public static Map<Integer, Set<String>> parseFileNamesByResourceId(
      @NonNull byte[] arscBytes, @NonNull String typeName) {
    try {
      return new Parser(arscBytes, typeName).parse();
    } catch (RuntimeException e) {
      logger.debug("Could not parse resources.arsc: {}", e.toString());
      return new LinkedHashMap<>();
    }
  }

  /** One-shot, stateful chunk reader — mirrors the shape of the (buggy) library parser it replaces. */
  private static final class Parser {
    private final ByteBuffer in;
    private final String targetTypeName;
    private final Map<Integer, Set<String>> filePathsByResourceId = new LinkedHashMap<>();
    private String[] globalStrings = new String[0];

    Parser(byte[] arscBytes, String targetTypeName) {
      this.in = ByteBuffer.wrap(arscBytes).order(ByteOrder.LITTLE_ENDIAN);
      this.targetTypeName = targetTypeName;
    }

    Map<Integer, Set<String>> parse() {
      Chunk root = new Chunk();
      if (root.type != ResConst.RES_TABLE_TYPE) {
        throw new IllegalArgumentException("Not a resources.arsc table (root chunk type mismatch)");
      }
      in.getInt(); // package count - each package's own chunks are self-describing, not needed

      while (in.hasRemaining() && in.position() < root.location + root.size) {
        Chunk chunk = new Chunk();
        try {
          if (chunk.type == ResConst.RES_STRING_POOL_TYPE) {
            globalStrings = readStringItems();
          } else if (chunk.type == ResConst.RES_TABLE_PACKAGE_TYPE) {
            readPackage(chunk);
          }
        } catch (RuntimeException e) {
          logger.debug("Skipping unparsable resources.arsc chunk at {}: {}", chunk.location, e);
        }
        in.position(chunk.location + chunk.size);
      }
      return filePathsByResourceId;
    }

    private void readPackage(Chunk pkgChunk) {
      int packageId = in.getInt() & 0xFF;

      int namePosition = in.position();
      in.position(namePosition + 128 * 2); // fixed-width 128 UTF-16 code unit name field

      int typeStringsOffset = in.getInt();
      in.getInt(); // lastPublicType - unused
      int keyStringsOffset = in.getInt();
      in.getInt(); // lastPublicKey - unused
      // A newer aapt2-emitted ResTable_package may carry an additional typeIdOffset field here
      // (or other future padding) before the type-name string pool; unlike the upstream library
      // this replaces, position is never assumed adjacent to these four ints - always seek via
      // the offsets the header itself gives us, which is robust to any such extra field.

      String[] typeNames = readStringPoolAt(pkgChunk.location + typeStringsOffset);
      String[] keyNames = readStringPoolAt(pkgChunk.location + keyStringsOffset);

      int packageEnd = pkgChunk.location + pkgChunk.size;
      // Position is unconstrained after the two string-pool seeks above; the type/spec chunks
      // that follow aren't at a known fixed offset, but each is self-describing (its own
      // ResChunk_header), so scanning forward chunk-by-chunk from here is correct regardless of
      // exactly where "here" landed - the actual format has them immediately follow the key
      // string pool chunk, which readStringPoolAt already advanced past.
      while (in.hasRemaining() && in.position() < packageEnd) {
        Chunk chunk = new Chunk();
        if (chunk.type == ResConst.RES_TABLE_TYPE_TYPE) {
          try {
            readType(chunk, packageId, typeNames, keyNames);
          } catch (RuntimeException e) {
            logger.debug("Skipping unparsable resource type chunk at {}: {}", chunk.location, e);
          }
        }
        // RES_TABLE_TYPE_SPEC_TYPE and anything else: the resource's own name comes from the key
        // string pool via each entry's own key index (read in readEntry), not from the spec
        // chunk, so nothing here needs it - skip straight to the next sibling chunk.
        in.position(chunk.location + chunk.size);
      }
    }

    @NonNull
    private String[] readStringPoolAt(int absolutePosition) {
      in.position(absolutePosition);
      Chunk chunk = new Chunk();
      if (chunk.type != ResConst.RES_STRING_POOL_TYPE) {
        throw new IllegalArgumentException(
            "Expected a string pool chunk at " + absolutePosition + ", found type " + chunk.type);
      }
      String[] strings = readStringItems();
      in.position(chunk.location + chunk.size);
      return strings;
    }

    /** {@link StringItems#read} declares a checked {@link IOException}; never actually IO here. */
    @NonNull
    private String[] readStringItems() {
      try {
        return StringItems.read(in);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    }

    private void readType(
        @NonNull Chunk chunk, int packageId, @NonNull String[] typeNames, @NonNull String[] keyNames) {
      int typeId = in.get() & 0xFF;
      int flags = in.get() & 0xFF;
      in.getShort(); // reserved
      int entryCount = in.getInt();
      int entriesStart = in.getInt();

      String typeName = typeId >= 1 && typeId <= typeNames.length ? typeNames[typeId - 1] : null;
      if (!targetTypeName.equals(typeName)) {
        return;
      }
      if ((flags & (TYPE_FLAG_SPARSE | TYPE_FLAG_OFFSET16)) != 0) {
        // A more compact entry-offset encoding (sparse index, or 16-bit offsets) some aapt2
        // builds use for large/sparse resource ID spaces - not implemented; skip this
        // type/config combination rather than misinterpret its dense-format-shaped offset table.
        logger.debug(
            "Skipping sparse/offset16 resource type chunk (type={}, flags={})", typeName, flags);
        return;
      }

      // What's left of the header after entryCount/entriesStart is the ResTable_config struct,
      // whose own leading size field tells us how many bytes it occupies - not needed for a
      // file-path lookup (a config only selects *which* device qualifiers this variant applies
      // to), so skip straight to the end of the chunk header via its own headSize.
      in.position(chunk.location + chunk.headSize);

      int[] entryOffsets = new int[entryCount];
      for (int i = 0; i < entryCount; i++) {
        entryOffsets[i] = in.getInt();
      }
      for (int entryId = 0; entryId < entryCount; entryId++) {
        int offset = entryOffsets[entryId];
        if (offset == NO_ENTRY) {
          continue;
        }
        in.position(chunk.location + entriesStart + offset);
        readEntry(packageId, typeId, entryId, keyNames);
      }
    }

    private void readEntry(int packageId, int typeId, int entryId, @NonNull String[] keyNames) {
      in.getShort(); // ResTable_entry.size
      int entryFlags = in.getShort() & 0xFFFF;
      in.getInt(); // key index into keyNames - the resource's own name, not needed for path lookup
      if ((entryFlags & ENTRY_FLAG_COMPLEX) != 0) {
        return; // a bag/complex entry (e.g. a style or array), never a plain file reference
      }

      in.getShort(); // Res_value.size
      in.get(); // Res_value.res0, always 0
      int dataType = in.get() & 0xFF;
      int data = in.getInt();
      if (dataType != TYPE_STRING || data < 0 || data >= globalStrings.length) {
        return; // not a file reference (e.g. a TYPE_REFERENCE alias to another resource)
      }

      int resourceId = (packageId << 24) | (typeId << 16) | entryId;
      filePathsByResourceId
          .computeIfAbsent(resourceId, id -> new LinkedHashSet<>())
          .add(globalStrings[data]);
    }

    /** A {@code ResChunk_header}: type/headerSize/size, plus the absolute position it started at. */
    private final class Chunk {
      final int location;
      final int type;
      final int headSize;
      final int size;

      Chunk() {
        location = in.position();
        type = in.getShort() & 0xFFFF;
        headSize = in.getShort() & 0xFFFF;
        size = in.getInt();
      }
    }
  }
}
