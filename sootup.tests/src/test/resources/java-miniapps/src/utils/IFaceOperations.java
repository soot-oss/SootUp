package utils;

import ds.Department;
import ds.Employee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface IFaceOperations {
    List<Department> departmentList= new ArrayList<>();
    List<Employee> employeeList= new ArrayList<>();

    void addDepartment(Department department);
    void addEmployee(Employee employee);
    void readFromFile(String filepath) throws IOException;
}
