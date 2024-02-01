package org.sootup.java.codepropertygraph.evaluation;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DualPrintStream extends OutputStream {
  private PrintStream fileStream;
  private PrintStream consoleStream;

  public DualPrintStream(String filePath) throws FileNotFoundException {
    this.fileStream = new PrintStream(filePath);
    this.consoleStream = System.out;
  }

  @Override
  public void write(int b) {
    fileStream.write(b);
    consoleStream.write(b);
  }

  @Override
  public void flush() {
    fileStream.flush();
    consoleStream.flush();
  }

  @Override
  public void close() throws FileNotFoundException {
    fileStream.close();
  }
}
