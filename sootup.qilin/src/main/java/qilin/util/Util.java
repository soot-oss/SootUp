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

package qilin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Util {
    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static byte[] toUtf8(String s) {
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream(s.length());
            DataOutputStream d = new DataOutputStream(bs);
            d.writeUTF(s);
            return bs.toByteArray();
        } catch (IOException e) {
            logger.debug("Some sort of IO exception in toUtf8 with " + s);
        }
        return null;
    }

    public static <K, V> boolean addToMap(Map<K, Set<V>> map, K key, V value) {
        return map.computeIfAbsent(key, k -> DataFactory.createSet()).add(value);
    }

    public static <K, V> boolean removeFromMap(Map<K, Set<V>> map, K key, V value) {
        if (!map.containsKey(key)) {
            return false;
        }
        return map.get(key).remove(value);
    }

    public static <T> void add(Map<String, T> map, T name, String... aliases) {
        for (String alias : aliases) {
            map.put(alias, name);
        }
    }

    public static String[] concat(String[] a, String[] b) {
        String[] c = new String[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static void writeToFile(String file, String content) {
        try {
            File mfile = new File(file);
            if (!mfile.exists()) {
                System.out.println(file);
                mfile.createNewFile();
            }
            FileWriter writer = new FileWriter(mfile);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final static Pattern qPat = Pattern.compile("'");

    public static String stripQuotes(CharSequence s) {
        return qPat.matcher(s).replaceAll("");
    }
}
