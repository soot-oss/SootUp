package de.upb.soot.frontends.java;

import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;

import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
public class DebuggingInformationTag implements Tag {

  private DebuggingInformation debugInfo;

  public DebuggingInformationTag(DebuggingInformation debugInfo) {
    this.debugInfo = debugInfo;
  }

  @Override
  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public byte[] getValue() throws AttributeValueException {
    return null;
  }

}
