package httpd.v5.model;


public class CalcEngine {

  private static CalcEngine singleton = null;
  
  private CalcEngine() { }
  
  public String compute(String op, String aStr, String bStr) {
    double a = Double.parseDouble(aStr);
    double b = Double.parseDouble(bStr);

    switch (op) {
      case "add":      return "" + (a + b);
      case "subtract": return "" + (a - b);
      case "multiply": return "" + (a * b);
      case "divide":   return "" + (a / b);
      case "exponent": return "" + Math.pow(a, b);
      default:
        return null;
    }    
  }

  public static CalcEngine getInstance() {
    if (singleton == null) {
      singleton = new CalcEngine();
    }
    return singleton;
  }
}
