import ds.Department;
import ds.Employee;
import utils.Operations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MiniApp {

    public static void main(String[] args) throws IOException {
        Operations operations= new Operations();
        operations.readFromFile(args[0]);
        operations.addEmployee(new Employee("Irina",5000));
        operations.addDepartment(new Department(new Employee("Irina",5000),"Accounting"));
        System.out.println(operations.getDepartmentList().toString());;

    }
}
