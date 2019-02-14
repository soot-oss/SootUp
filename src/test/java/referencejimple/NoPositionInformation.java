package referencejimple;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;

import com.ibm.wala.classLoader.IMethod.SourcePosition;


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
        return 1;
    }

    @Override
    public int getLastLine() {
        return 1;
    }

    @Override
    public int getFirstCol() {
        return 1;
    }

    @Override
    public int getLastCol() {
        return 1;
    }

    @Override
    public int getFirstOffset() {
        return 1;
    }

    @Override
    public int getLastOffset() {
        return 1;
    }

    @Override
    public int compareTo(SourcePosition arg0) {
      // TODO Auto-generated method stub
      return 0;
    }
}
