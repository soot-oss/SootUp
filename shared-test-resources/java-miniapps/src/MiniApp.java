import ds.Department;
import ds.Employee;
import utils.Operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MiniApp {

    public static void main(String[] args) throws IOException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        Operations operations= new Operations();
        operations.readFromFile("E:/Masters_CS_UPB/WHB/FutureSoot/testsuite/shared-test-resources/java-miniapps/src/testFile.txt");
        operations.addEmployee(new Employee("Irina",5000));
        operations.addDepartment(new Department(new Employee("Irina",5000),"Accounting"));
        System.out.println(operations.getDepartmentList().toString());;

    }
}
