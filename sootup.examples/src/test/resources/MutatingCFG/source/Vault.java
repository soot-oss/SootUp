/** A fictional vault whose withdraw method gets hardened in the "Modify a ControlFlowGraph" docs. */
public class Vault {
  private int balance = 100;

  public int withdraw(int amount) {
    Legacy.check(amount);
    System.out.println(amount);
    balance = balance - amount;
    return balance;
  }
}
