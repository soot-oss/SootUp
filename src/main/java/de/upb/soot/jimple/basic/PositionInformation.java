package de.upb.soot.jimple.basic;

import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

/** @author Markus Schmidt */
public class PositionInformation extends AbstractSourcePosition {

  URL url = null;
  Reader reader = null;

  int firstLine, lastLine;
  int firstCol, lastCol;

  int firstOffset = -1;
  int lastOffset = -1;

  public PositionInformation(
      URL url,
      Reader reader,
      int firstLine,
      int firstCol,
      int lastLine,
      int lastCol,
      int firstOffset,
      int lastOffset) {
    this(firstLine, firstCol, lastLine, lastCol);
    this.url = url;
    this.reader = reader;
    this.firstOffset = firstOffset;
    this.lastOffset = lastOffset;
  }

  public PositionInformation(int firstLine, int firstCol, int lastLine, int lastCol) {
    this.firstLine = firstLine;
    this.lastLine = lastLine;
    this.firstCol = firstCol;
    this.lastCol = lastCol;
  }

  @Override
  public URL getURL() {
    return url;
  }

  @Override
  public Reader getReader() throws IOException {
    return reader;
  }

  @Override
  public int getFirstLine() {
    return firstLine;
  }

  @Override
  public int getLastLine() {
    return lastLine;
  }

  @Override
  public int getFirstCol() {
    return firstCol;
  }

  @Override
  public int getLastCol() {
    return lastCol;
  }

  @Override
  public int getFirstOffset() {
    return firstOffset;
  }

  @Override
  public int getLastOffset() {
    return lastOffset;
  }
}
