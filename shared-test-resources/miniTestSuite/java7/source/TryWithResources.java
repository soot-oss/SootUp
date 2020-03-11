import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;

class TryWithResources{

    public void printFile() throws Exception{
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("file.txt"))){
            String data = "";
            while( (data= bufferedReader.readLine()) != null ){
                System.out.println(data);
            }
        }
    }
}