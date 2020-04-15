
/** @author Kaustubh Kelkar */
class Employee{
    private int salary;
    public Employee(int salary){
        this.salary=salary;
    }
    public int getSalary(){ return salary;}
}
class TempEmployee extends Employee{
    private int bonus;
    public TempEmployee(int salary, int bonus){
        super(salary);
        this.bonus=bonus;
    }
    public int getSalary(){
        return super.getSalary()+bonus;
    }
}
class RegEmployee extends Employee{
    private int raise;
    public RegEmployee(int salary, int raise){
        super(salary);
        this.raise=raise;
    }
    public int getSalary(){
        return super.getSalary()+raise;
    }
}

class VirtualMethod{
    public void virtualMethodDemo(){
        Employee e1= new TempEmployee(1500,150);
        Employee e2= new RegEmployee(1500,500);
        System.out.println(e1.getSalary());
        System.out.println(e2.getSalary());
    }

  public static void main(String[] args) {
    VirtualMethod obj = new VirtualMethod();
    obj.virtualMethodDemo();
  }
}