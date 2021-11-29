package de.upb.swt.soot.java.core.tag;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 Archie L. Cobbs
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


import de.upb.swt.soot.core.jimple.common.constant.StringConstant;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StringConstantValueTag extends ConstantValueTag {

  public static final String NAME = "StringConstantValueTag";

  private final String value;
  private static final Logger logger = LoggerFactory.getLogger(StringConstantValueTag.class);

  public StringConstantValueTag(String value) {
    super(toUtf8(value));
    this.value = value;
  }

  public String getStringValue() {
    return value;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String toString() {
    return "ConstantValue: " + value;
  }

  @Override
  public StringConstant getConstant() {
    return new StringConstant(value, JavaIdentifierFactory.getInstance().getClassType("java.lang.String"));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    StringConstantValueTag other = (StringConstantValueTag) obj;
    if (this.value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!this.value.equals(other.value)) {
      return false;
    }
    return true;
  }

  private static byte[] toUtf8(String s) {
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

}
