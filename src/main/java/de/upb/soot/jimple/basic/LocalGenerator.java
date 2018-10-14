package de.upb.soot.jimple.basic;

import de.upb.soot.core.Body;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.common.type.BooleanType;
import de.upb.soot.jimple.common.type.ByteType;
import de.upb.soot.jimple.common.type.CharType;
import de.upb.soot.jimple.common.type.DoubleType;
import de.upb.soot.jimple.common.type.FloatType;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.LongType;
import de.upb.soot.jimple.common.type.RefLikeType;
import de.upb.soot.jimple.common.type.ShortType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.UnknownType;
import de.upb.soot.jimple.common.type.VoidType;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2004 Jennifer Lhotak
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

import java.util.HashSet;
import java.util.Set;

/**
 * Generates locals for Body.
 * 
 * @author Linghui Luo
 *
 */
public class LocalGenerator {

  protected final Body body;

  public LocalGenerator(Body b) {
    body = b;
  }

  protected transient Set<String> localNames = null;

  protected boolean bodyContainsLocal(String name) {
    return localNames.contains(name);
  }

  private void initLocalNames() {
    localNames = new HashSet<String>();
    for (Local l : body.getLocals()) {
      localNames.add(l.getName());
    }
  }

  /**
   * generates a new soot local given the type
   */
  public Local generateLocal(Type type) {

    // store local names for enhanced performance
    initLocalNames();

    String name = "v";
    if (type instanceof IntType) {
      while (true) {
        name = nextIntName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof ByteType) {
      while (true) {
        name = nextByteName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof ShortType) {
      while (true) {
        name = nextShortName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof BooleanType) {
      while (true) {
        name = nextBooleanName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof VoidType) {
      while (true) {
        name = nextVoidName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof CharType) {
      while (true) {
        name = nextCharName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
      type = CharType.getInstance();
    } else if (type instanceof DoubleType) {
      while (true) {
        name = nextDoubleName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof FloatType) {
      while (true) {
        name = nextFloatName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof LongType) {
      while (true) {
        name = nextLongName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof RefLikeType) {
      while (true) {
        name = nextRefLikeTypeName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else if (type instanceof UnknownType) {
      while (true) {
        name = nextUnknownTypeName();
        if (!bodyContainsLocal(name)) {
          break;
        }
      }
    } else {
      localNames = null;
      throw new RuntimeException("Unhandled Type of Local variable to Generate - Not Implemented");
    }

    localNames = null;
    return createLocal(name, type);
  }

  private int tempInt = -1;
  private int tempVoid = -1;
  private int tempBoolean = -1;
  private int tempLong = -1;
  private int tempDouble = -1;
  private int tempFloat = -1;
  private int tempRefLikeType = -1;
  private int tempByte = -1;
  private int tempShort = -1;
  private int tempChar = -1;
  private int tempUnknownType = -1;

  private String nextIntName() {
    tempInt++;
    return "$i" + tempInt;
  }

  private String nextCharName() {
    tempChar++;
    return "$c" + tempChar;
  }

  private String nextVoidName() {
    tempVoid++;
    return "$v" + tempVoid;
  }

  private String nextByteName() {
    tempByte++;
    return "$b" + tempByte;
  }

  private String nextShortName() {
    tempShort++;
    return "$s" + tempShort;
  }

  private String nextBooleanName() {
    tempBoolean++;
    return "$z" + tempBoolean;
  }

  private String nextDoubleName() {
    tempDouble++;
    return "$d" + tempDouble;
  }

  private String nextFloatName() {
    tempFloat++;
    return "$f" + tempFloat;
  }

  private String nextLongName() {
    tempLong++;
    return "$l" + tempLong;
  }

  private String nextRefLikeTypeName() {
    tempRefLikeType++;
    return "$r" + tempRefLikeType;
  }

  private String nextUnknownTypeName() {
    tempUnknownType++;
    return "$u" + tempUnknownType;
  }

  // this should be used for generated locals only
  protected Local createLocal(String name, Type sootType) {
    Local sootLocal = Jimple.newLocal(name, sootType);
    body.addLocal(sootLocal);
    return sootLocal;
  }
}
