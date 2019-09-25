/**
 * @author Linghui Luo
 * @see https://docs.oracle.com/javase/7/docs/technotes/guides/language/strings-switch.html
 */
public class StringsInSwitch {
  public String getTypeOfDayWithSwitchStatement(String dayOfWeekArg) {
    String typeOfDay;
    switch (dayOfWeekArg) {
      case "Monday":
        typeOfDay = "Start of work week";
        break;
      case "Tuesday":
      case "Wednesday":
      case "Thursday":
        typeOfDay = "Midweek";
        break;
      case "Friday":
        typeOfDay = "End of work week";
        break;
      case "Saturday":
      case "Sunday":
        typeOfDay = "Weekend";
        break;
      default:
        throw new IllegalArgumentException("Invalid day of the week: " + dayOfWeekArg);
    }
    return typeOfDay;
  }
}
