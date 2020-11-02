import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

class TypeInference{

    public void printFile() throws Exception{
        var fileName="file.txt";
        var data = "";
        var fileReader= new FileReader(fileName);
        var bufferedReader= new BufferedReader(fileReader);
            while( (data= bufferedReader.readLine()) != null ){
                System.out.println(data);
            }
        bufferedReader.close();
    }

  public static void main(String[] args) throws Exception{
    TypeInference typeInference = new TypeInference();
    typeInference.printFile();
  }
}