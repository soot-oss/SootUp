import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

/** @author Kaustubh Kelkar */
public class TryWithResourcesConcise{

    public void printFile() throws Exception {
        final BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"));
        try(bufferedReader) {
            String data = "";
            while( (data= bufferedReader.readLine()) != null) {
                System.out.println(data);
            }
        }
    }

  public static void main(String[] args) throws  Exception{
      TryWithResourcesConcise tryWithResourcesConcise = new TryWithResourcesConcise();
      tryWithResourcesConcise.printFile();
    }
}