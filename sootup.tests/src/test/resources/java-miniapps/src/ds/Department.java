package ds;

public class Department extends AbstractDataStrcture{
    private Employee employee;
    private String deptName;

    public Department(Employee employee, String deptName) {
        this.employee = employee;
        this.deptName = deptName;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    @Override
    public String toString() {
        return "Department{" +employee +
                "," + deptName + ":" +
                '}';
    }
}