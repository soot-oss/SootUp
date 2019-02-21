package de.upb.soot.jimple.basic;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.classLoader.IMethod.SourcePosition;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/**
 * This class represents the case when there is no position.
 * 
 * @author Linghui Luo
 *
 */
public class NoPositionInformation implements CAstSourcePositionMap.Position {

  @Override
  public URL getURL() {
    return null;
  }

  @Override
  public Reader getReader() throws IOException {
    return null;
  }

  @Override
  public int getFirstLine() {
    return -1;
  }

  @Override
  public int getLastLine() {
    return -1;
  }

  @Override
  public int getFirstCol() {
    return -1;
  }

  @Override
  public int getLastCol() {
    return -1;
  }

  @Override
  public int getFirstOffset() {
    return -1;
  }

  @Override
  public int getLastOffset() {
    return -1;
  }

  @Override
  public int compareTo(SourcePosition arg0) {
    return 0;
  }

  @Override
  public String toString() {
    return "No position info";
  }
}
