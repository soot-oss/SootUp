package de.upb.soot.frontends.java;

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;

class DebuggingInformationTag implements Tag {

  private DebuggingInformation debugInfo;

  public DebuggingInformationTag(DebuggingInformation debugInfo) {
    this.debugInfo = debugInfo;
  }

  @Override
  public String getName() {
    return "DebugInfo";
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return this.debugInfo.toString().getBytes();
  }

  public DebuggingInformation getDebugInfo() {
    return this.debugInfo;

  }
}
