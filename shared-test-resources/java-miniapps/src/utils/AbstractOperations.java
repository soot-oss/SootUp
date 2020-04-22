package utils;

import ds.Department;
import ds.Employee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractOperations {
    List<Department> departmentList= new ArrayList<>();
    List<Employee> employeeList= new ArrayList<>();

    public abstract void addDepartment(Department department);
    public abstract void addEmployee(Employee employee);
    public abstract void readFromFile(String filepath) throws IOException;
}
