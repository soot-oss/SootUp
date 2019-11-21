import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

class MultiTryCatch {

  public void printFile() throws Exception {
    BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"));
    try {
      String data = "";
      int divisor = 10/5;
      System.out.println(divisor);
      while ((data = bufferedReader.readLine()) != null) {
        System.out.println(data);
      }
    }
    catch( IOException | NumberFormatException e){

    }catch (Exception e){

    }finally {
      try {
        bufferedReader.close();
      } catch (IOException e) {
      }
    }
  }
}