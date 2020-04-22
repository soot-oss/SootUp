package utils;

import ds.Department;
import ds.Employee;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Operations implements IFaceOperations{

    @Override
    public void addDepartment(Department department){
        departmentList.add(department);
    }

    public void removeDepartment(Department department) {
        departmentList.remove(department);
    }

    @Override
    public void addEmployee(Employee employee){
        employeeList.add(employee);
    }

    public void removeEmployee(Employee employee) {
        employeeList.remove(employee);
    }

    public List<Department> getDepartmentList() {
        return departmentList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void readFromFile(String filepath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(filepath)));

        String st ;
        while ((st = br.readLine()) != null){
            String[] array = st.split(" ",3);
            Employee employee = new Employee(array[0], Integer.parseInt(array[1]));
            addEmployee(employee);
            addDepartment(new Department(employee,array[2]));
        }
    }
}